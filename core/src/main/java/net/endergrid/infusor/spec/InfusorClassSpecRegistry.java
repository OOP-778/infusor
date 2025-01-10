package net.endergrid.infusor.spec;

import net.endergrid.infusor.annotation.Infuse;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InfusorClassSpecRegistry {
    private final Map<Class<?>, InfusableClass<?>> specs = new ConcurrentHashMap<>();
    private static final InfusorClassSpecRegistry INSTANCE = new InfusorClassSpecRegistry();

    public static InfusorClassSpecRegistry get() {
        return INSTANCE;
    }

    @Nullable
    public <T> InfusableClass<T> get(Class<?> clazz) {
        return (InfusableClass<T>) this.specs.compute(clazz, ($, spec) -> {
            if (spec != null) {
                return spec;
            }

            return this.createOrNull(clazz);
        });
    }

    public boolean isApplicable(Constructor<?> constructor) {
        return Arrays.stream(constructor.getAnnotatedParameterTypes())
                .anyMatch(this::isApplicable);
    }

    public boolean isApplicable(AnnotatedType annotatedType) {
        return Arrays.stream(annotatedType.getAnnotations())
                .anyMatch(this::isApplicable);
    }

    private <T> InfusableClass<T> createOrNull(Class<T> clazz) {
        InfusableClass<T> infusableClass;

        // First look through constructors, cause there's less of them technically.
        if ((infusableClass = this.tryLookForApplicableConstructor(clazz)) != null) {
            return infusableClass;
        }

        // If there's no applicable constructor, look through fields.
        if ((infusableClass = this.tryLookForApplicableFields(clazz)) != null) {
            return infusableClass;
        }

        return new InfusableClass.None<>(clazz);
    }

    private <T> InfusableClass<T> tryLookForApplicableFields(Class<T> clazz) {
        final List<Field> fields = new ArrayList<>(2);

        Class<?> currentClass = clazz;
        while (currentClass != null) {

            for (final Field declaredField : currentClass.getDeclaredFields()) {
                if (!this.isApplicable(declaredField)) {
                    continue;
                }

                fields.add(declaredField);
            }

            currentClass = currentClass.getSuperclass();
        }

        return fields.isEmpty() ? null : new InfusableClass.ByFields<>(clazz, fields);
    }

    private <T> InfusableClass.ByConstructor<T> tryLookForApplicableConstructor(Class<T> clazz) {
        for (final Constructor<?> declaredConstructor : clazz.getDeclaredConstructors()) {
            final Annotation[][] parameterAnnotations = declaredConstructor.getParameterAnnotations();
            if (Arrays.stream(parameterAnnotations).flatMap(Arrays::stream).noneMatch(this::isApplicable)) {
                continue;
            }

            return new InfusableClass.ByConstructor<>(clazz, (Constructor<T>) declaredConstructor);
        }

        return null;
    }

    private boolean isApplicable(Annotation annotation) {
        return annotation instanceof Infuse;
    }

    private boolean isApplicable(Field field) {
        return Arrays.stream(field.getAnnotations()).anyMatch(this::isApplicable);
    }
}
