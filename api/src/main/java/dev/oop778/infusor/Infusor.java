package dev.oop778.infusor;

import lombok.NonNull;
import dev.oop778.infusor.registration.InfusorPendingRegistration;
import dev.oop778.infusor.scope.InfusorComposedScope;
import dev.oop778.infusor.scope.InfusorDefinedScope;

import java.util.function.UnaryOperator;

public interface Infusor {
    <T> T infuse(@NonNull Class<T> clazz);
    <T> T infuse(@NonNull T object);

    <T extends InfusorDefinedScope> Infusor defaultScope(@NonNull Class<T> scope, @NonNull T value);

    InfusorComposedScope getDefaultScope();

    InfusorPendingRegistration createRegistration(@NonNull UnaryOperator<InfusorPendingRegistration> registrationConfigurator);
}
