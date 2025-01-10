package net.endergrid.infusor.registration;

import net.endergrid.infusor.InfusableObjectProvider;
import net.endergrid.infusor.scope.InfusorScope;

public interface InfusorRegistrationStaged<T, RETURN> {

    FactoryStage<T, RETURN> factory();
    SingletonStage<T, RETURN> singleton();

    interface FactoryStage<T, RETURN> {
        ScopeSelectorStage<T, RETURN> provider(InfusableObjectProvider<T> factory);
    }

    interface ScopeSelectorStage<T, RETURN> {
        BuildStage<T, RETURN> withScope(InfusorScope... scopes);

        BuildStage<T, RETURN> withDefaultScopes();
    }

    interface BuildStage<T, RETURN> {
        RETURN complete();
    }

    interface SingletonStage<T, RETURN> {
        ScopeSelectorStage<T, RETURN> eagerProvided(T object);
        ScopeSelectorStage<T, RETURN> eagerProvidedInfused(T object);

        ScopeSelectorStage<T, RETURN> eager(Class<T> clazz);

        ScopeSelectorStage<T, RETURN> lazy(Class<T> clazz);
        ScopeSelectorStage<T, RETURN> lazy(InfusableObjectProvider<T> factory);
    }
}
