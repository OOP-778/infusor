package dev.oop778.infusor.registration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import dev.oop778.infusor.InfusableObjectProvider;
import dev.oop778.infusor.provider.EagerSingletonProvider;
import dev.oop778.infusor.provider.FactoryProvider;
import dev.oop778.infusor.provider.InfusorProvider;
import dev.oop778.infusor.provider.LazySingletonProvider;
import dev.oop778.infusor.scope.ComposedScopeImpl;
import dev.oop778.infusor.scope.InfusorComposedScope;
import dev.oop778.infusor.scope.InfusorScope;

import java.util.function.Function;

@RequiredArgsConstructor
@Getter
public class InfusorPendingRegistrationPart<T, RETURN> implements InfusorRegistrationStaged<T, RETURN>, InfusorRegistrationStaged.FactoryStage<T, RETURN>, InfusorRegistrationStaged.SingletonStage<T, RETURN>, InfusorRegistrationStaged.ScopeSelectorStage<T, RETURN>, InfusorRegistrationStaged.BuildStage<T, RETURN> {
    private final Function<InfusorPendingRegistrationPart<T, RETURN>, RETURN> completion;
    private boolean isSingleton;
    private InfusorProvider provider;
    private InfusorComposedScope scope;

    @Override
    public RETURN complete() {
        return this.completion.apply(this);
    }

    @Override
    public InfusorRegistrationStaged.ScopeSelectorStage<T, RETURN> provider(InfusableObjectProvider<T> factory) {
        this.provider = new FactoryProvider();
        return this;
    }

    @Override
    public InfusorRegistrationStaged.BuildStage<T, RETURN> withScope(InfusorScope... scopes) {
        this.scope = ComposedScopeImpl.create(scopes);
        return this;
    }

    @Override
    public InfusorRegistrationStaged.BuildStage<T, RETURN> withDefaultScopes() {
        this.scope = null;
        return this;
    }

    @Override
    public InfusorRegistrationStaged.ScopeSelectorStage<T, RETURN> eagerProvided(T object) {
        this.provider = new EagerSingletonProvider<>((Class<T>) object.getClass(), object, false);
        return this;
    }

    @Override
    public ScopeSelectorStage<T, RETURN> eagerProvidedInfused(T object) {
        this.provider = new EagerSingletonProvider<>((Class<T>) object.getClass(), object, true);
        return this;
    }

    @Override
    public InfusorRegistrationStaged.ScopeSelectorStage<T, RETURN> eager(Class<T> clazz) {
        this.provider = new EagerSingletonProvider<>(clazz, null, true);
        return this;
    }

    @Override
    public InfusorRegistrationStaged.ScopeSelectorStage<T, RETURN> lazy(Class<T> clazz) {
        this.provider = new LazySingletonProvider<>(clazz);
        return this;
    }

    @Override
    public InfusorRegistrationStaged.ScopeSelectorStage<T, RETURN> lazy(InfusableObjectProvider<T> factory) {
        this.provider = new LazySingletonProvider<>(factory);
        return this;
    }

    @Override
    public FactoryStage<T, RETURN> factory() {
        this.isSingleton = false;
        return this;
    }

    @Override
    public SingletonStage<T, RETURN> singleton() {
        this.isSingleton = true;
        return this;
    }
}
