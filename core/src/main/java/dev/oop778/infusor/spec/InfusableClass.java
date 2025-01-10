package dev.oop778.infusor.spec;

import dev.oop778.infusor.InfuseResolveResult;
import dev.oop778.infusor.InfusorContext;
import dev.oop778.infusor.InfusorScopeRegistryImpl;
import dev.oop778.infusor.PathProvider;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.infusor.*;
import dev.oop778.infusor.annotation.Scoped;
import dev.oop778.infusor.context.InfusorMultiContextImpl;
import dev.oop778.infusor.scope.ComposedScopeImpl;
import dev.oop778.infusor.scope.InfusorDefinedScope;
import dev.oop778.infusor.scope.InfusorScope;

import java.lang.annotation.Annotation;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.*;
import java.util.*;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public abstract class InfusableClass<T> {
    protected final Class<T> clazz;

    public abstract InfuseResolveResult<T> infuse(T object, InfusorContext context);

    public abstract InfuseResolveResult<T> infuse(InfusorContext context);

    public static class ByConstructor<T> extends InfusableClass<T> {
        private final MethodHandle initializer;
        private final Parameter<?>[] parameters;

        public ByConstructor(Class<T> clazz, Constructor<T> constructor) {
            super(clazz);
            this.initializer = this.initializeInitializer(constructor);
            this.parameters = this.initializeParameters(constructor);
        }

        @Override
        public InfuseResolveResult<T> infuse(T object, InfusorContext context) {
            throw new UnsupportedOperationException("Cant infuse already constructed object with a constructor");
        }

        @Override
        @SneakyThrows
        public InfuseResolveResult<T> infuse(InfusorContext context) {
            final List<Object> args = new ArrayList<>(this.parameters.length);
            boolean allResolved = true;

            for (final Parameter<?> parameter : this.parameters) {
                final InfuseResolveResult<?> resolve = parameter.resolve((InfusorMultiContextImpl) context);

                if (resolve == InfuseResolveResult.ERROR) {
                    return InfuseResolveResult.error();
                }

                if (resolve == InfuseResolveResult.UNKNOWN) {
                    if (parameter.isOptional()) {
                        args.add(Optional.empty());
                        continue;
                    } else {
                        ((InfusorMultiContextImpl) context).addError(String.format("Failed to find required parameter of type: %s, location: %s", parameter.getTypeToken().toString(false), parameter.where));
                        return InfuseResolveResult.unknown();
                    }
                }

                if (resolve instanceof InfuseResolveResult.Resolved) {
                    args.add(((InfuseResolveResult.Resolved<?>) resolve).get());
                } else {
                    allResolved = false;
                }
            }

            if (!allResolved) {
                return InfuseResolveResult.unresolved();
            }

            return InfuseResolveResult.resolved((T) this.initializer.invokeWithArguments(args));
        }

        private Parameter<?>[] initializeParameters(Constructor<T> constructor) {
            final int parameterCount = constructor.getParameterCount();
            final Parameter<?>[] parameters = new Parameter[parameterCount];
            final Annotation[][] parameterAnnotations = constructor.getParameterAnnotations();

            for (int i = 0; i < parameterCount; i++) {
                final Type genericParameterType = constructor.getGenericParameterTypes()[i];

                final boolean optional = Optional.class.isAssignableFrom(constructor.getParameterTypes()[i]);

                final TypeToken<?> convert = optional ? TypeToken.create(((ParameterizedType) genericParameterType).getActualTypeArguments()[0], parameterAnnotations[i]) : TypeToken.create(genericParameterType, parameterAnnotations[i]);
                if (convert == null) {
                    continue;
                }

                final Parameter<?> parameter = new Parameter<>(convert, this.constructorToString(constructor) + "[" + i + "]", optional);
                parameters[i] = parameter;
            }

            return parameters;
        }

        private String constructorToString(Constructor<?> constructor) {
            final Class<?> declaringClass = constructor.getDeclaringClass();
            final String collect = Arrays.stream(constructor.getGenericParameterTypes()).map(Object::toString).collect(Collectors.joining(", "));
            return String.format("Constructor %s(%s)", declaringClass.getName(), collect);
        }

        @SneakyThrows
        private MethodHandle initializeInitializer(Constructor<T> constructor) {
            constructor.setAccessible(true);
            return MethodHandles.lookup().unreflectConstructor(constructor);
        }
    }

    public static class None<T> extends InfusableClass<T> {
        public None(Class<T> clazz) {
            super(clazz);
        }

        @Override
        public InfuseResolveResult<T> infuse(T object, InfusorContext context) {
            return InfuseResolveResult.resolved(object);
        }

        @Override
        @SneakyThrows
        public InfuseResolveResult<T> infuse(InfusorContext context) {
            return InfuseResolveResult.resolved(this.clazz.newInstance());
        }
    }

    public static class ByFields<T> extends InfusableClass<T> {
        private final List<ParameterField<?>> fields;
        private final MethodHandle construct;

        public ByFields(Class<T> clazz, List<Field> fields) {
            super(clazz);

            this.fields = new ArrayList<>(fields.size());
            for (final Field field : fields) {
                final Type genericType = field.getGenericType();
                final Annotation[] annotations = field.getAnnotations();

                final boolean optional = Optional.class.isAssignableFrom(field.getType());
                final TypeToken<?> typeToken = optional ? TypeToken.create(((ParameterizedType) genericType).getActualTypeArguments()[0], annotations) : TypeToken.create(field.getGenericType(), annotations);
                final ParameterField<?> parameter = new ParameterField<>(field, typeToken, String.format("Field %s", field.getName()), optional);

                this.fields.add(parameter);
            }

            this.construct = this.initializeConstructor();
        }

        @Override
        public InfuseResolveResult<T> infuse(T object, InfusorContext context) {
            final Map<ParameterField<?>, Object> resolved = new HashMap<>(this.fields.size());

            for (final ParameterField<?> parameter : this.fields) {
                final InfuseResolveResult<?> resolve = parameter.resolve((InfusorMultiContextImpl) context);

                if (resolve == InfuseResolveResult.ERROR) {
                    return InfuseResolveResult.error();
                }

                if (resolve == InfuseResolveResult.UNKNOWN) {
                    if (parameter.isOptional()) {
                        continue;
                    } else {
                        ((InfusorMultiContextImpl) context).addError(String.format("Failed to find required parameter of type: %s, location: %s", parameter.getTypeToken().toString(false), parameter.getWhere()));
                        return InfuseResolveResult.unknown();
                    }
                }

                if (resolve instanceof InfuseResolveResult.Resolved) {
                    resolved.put(parameter, ((InfuseResolveResult.Resolved<?>) resolve).get());
                }
            }

            if (this.fields.size() != resolved.size()) {
                return InfuseResolveResult.unresolved();
            }

            for (final Map.Entry<ParameterField<?>, Object> fieldToValue : resolved.entrySet()) {
                final ParameterField<?> parameter = fieldToValue.getKey();
                final Object value = fieldToValue.getValue();

                parameter.set(object, value);
            }

            return InfuseResolveResult.resolved(object);
        }

        @Override
        @SneakyThrows
        public InfuseResolveResult<T> infuse(InfusorContext context) {
            final Map<ParameterField<?>, Object> resolved = new HashMap<>(this.fields.size());

            for (final ParameterField<?> parameter : this.fields) {
                final InfuseResolveResult<?> resolve = parameter.resolve((InfusorMultiContextImpl) context);

                if (resolve == InfuseResolveResult.ERROR) {
                    return InfuseResolveResult.error();
                }

                if (resolve == InfuseResolveResult.UNKNOWN) {
                    if (parameter.isOptional()) {
                        continue;
                    } else {
                        ((InfusorMultiContextImpl) context).addError(String.format("Failed to find required parameter of type: %s, location: %s", parameter.getTypeToken().toString(false), parameter.getWhere()));
                        return InfuseResolveResult.unknown();
                    }
                }

                if (resolve instanceof InfuseResolveResult.Resolved) {
                    resolved.put(parameter, ((InfuseResolveResult.Resolved<?>) resolve).get());
                }
            }

            if (this.fields.size() != resolved.size()) {
                return InfuseResolveResult.unresolved();
            }

            final T object = (T) this.construct.invoke();
            for (final Map.Entry<ParameterField<?>, Object> fieldToValue : resolved.entrySet()) {
                final ParameterField<?> parameter = fieldToValue.getKey();
                final Object value = fieldToValue.getValue();

                parameter.set(object, value);
            }

            return InfuseResolveResult.resolved(object);
        }

        @SneakyThrows
        private MethodHandle initializeConstructor() {
            final Constructor<T> declaredConstructor = this.clazz.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);

            return MethodHandles.lookup().unreflectConstructor(declaredConstructor);
        }
    }

    public static class ParameterField<T> extends Parameter<T> {
        private final MethodHandle setter;

        @SneakyThrows
        public ParameterField(Field field, TypeToken<T> typeToken, String where, boolean isOptional) {
            super(typeToken, where, isOptional);

            field.setAccessible(true);
            this.setter = MethodHandles.lookup().unreflectSetter(field);
        }

        @SneakyThrows
        public void set(Object object, Object value) {
            this.setter.invoke(object, value);
        }
    }

    @Getter
    public static class Parameter<T> implements PathProvider {
        private final TypeToken<T> typeToken;
        private final String where;
        private final boolean isOptional;
        private final InfusorScope scope;

        public Parameter(TypeToken<T> typeToken, String where, boolean isOptional) {
            this.typeToken = typeToken;
            this.where = where;
            this.isOptional = isOptional;
            final Scoped declaredAnnotation = this.typeToken.getDeclaredAnnotation(Scoped.class);
            if (declaredAnnotation != null) {
                this.scope = this.initializeScope(declaredAnnotation.value());
            } else {
                this.scope = null;
            }
        }

        @Override
        public String toString() {
            return this.where;
        }

        public InfuseResolveResult<T> resolve(InfusorMultiContextImpl context) {
            return context.tryResolve(this.typeToken, this.scope, this);
        }

        private InfusorScope initializeScope(String value) {
            final String[] split = value.split(",");
            final List<InfusorDefinedScope> scopes = new ArrayList<>();

            for (final String path : split) {
                final InfusorDefinedScope infusorDefinedScope = InfusorScopeRegistryImpl.get().find(path);
                if (infusorDefinedScope == null) {
                    throw new IllegalStateException(String.format("Failed to find defined scope at path: %s", path));
                }

                scopes.add(infusorDefinedScope);
            }

            return ComposedScopeImpl.create(scopes.toArray(new InfusorScope[0]));
        }
    }
}
