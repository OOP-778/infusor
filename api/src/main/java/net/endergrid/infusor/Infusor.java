package net.endergrid.infusor;

import lombok.NonNull;
import net.endergrid.infusor.registration.InfusorPendingRegistration;
import net.endergrid.infusor.scope.InfusorComposedScope;
import net.endergrid.infusor.scope.InfusorDefinedScope;

import java.util.function.UnaryOperator;

public interface Infusor {
    <T> T infuse(@NonNull Class<T> clazz);
    <T> T infuse(@NonNull T object);

    <T extends InfusorDefinedScope> Infusor defaultScope(@NonNull Class<T> scope, @NonNull T value);

    InfusorComposedScope getDefaultScope();

    InfusorPendingRegistration createRegistration(@NonNull UnaryOperator<InfusorPendingRegistration> registrationConfigurator);
}
