package dev.oop778.infusor;

import lombok.Getter;
import lombok.NonNull;
import net.endergrid.atom.typetoken.TypeToken;
import dev.oop778.infusor.context.InfusorSingleContextImpl;
import dev.oop778.infusor.registration.InfusorPendingRegistration;
import dev.oop778.infusor.registration.InfusorPendingRegistrationImpl;
import dev.oop778.infusor.scope.ComposedScopeImpl;
import dev.oop778.infusor.scope.InfusorComposedScope;
import dev.oop778.infusor.scope.InfusorDefinedScope;

import java.util.Arrays;
import java.util.function.UnaryOperator;

@Getter
public class InfusorImpl implements Infusor {
    private final InfusorNodes nodes = new InfusorNodes();
    private InfusorDefinedScope[] defaultScopes;
    private InfusorImpl parent;

    public InfusorImpl() {
        this.defaultScopes = new InfusorDefinedScope[InfusorScopeRegistryImpl.get().size()];
    }

    public InfusorImpl(InfusorDefinedScope[] defaultScopes) {
        this.defaultScopes = Arrays.copyOf(defaultScopes, defaultScopes.length);
    }

    @Override
    public <T> T infuse(@NonNull Class<T> clazz) {
        final InfusorSingleContextImpl infusorSingleContext = new InfusorSingleContextImpl(this);
        return infusorSingleContext.resolve(TypeToken.convert(clazz), this.getDefaultScope());
    }

    @Override
    public <T> T infuse(@NonNull T object) {
        return null;
    }

    @Override
    public <T extends InfusorDefinedScope> Infusor defaultScope(@NonNull Class<T> scope, @NonNull T value) {
        final InfusorScopeRegistryImpl infusorScopeRegistry = InfusorScopeRegistryImpl.get();
        final int i = infusorScopeRegistry.indexOf(scope);
        if (i == -1) {
            throw new IllegalArgumentException("Scope " + scope + " parent is not registered");
        }

        if (this.defaultScopes.length < i) {
            final InfusorDefinedScope[] newScopes = new InfusorDefinedScope[i + 1];
            System.arraycopy(this.defaultScopes, 0, newScopes, 0, this.defaultScopes.length);
            this.defaultScopes = newScopes;
        }

        this.defaultScopes[i] = value;
        return this;
    }

    @Override
    public InfusorComposedScope getDefaultScope() {
        return ComposedScopeImpl.create(this.defaultScopes);
    }

    @Override
    public InfusorPendingRegistration createRegistration(@NonNull UnaryOperator<InfusorPendingRegistration> registrationConfigurator) {
        return registrationConfigurator.apply(new InfusorPendingRegistrationImpl(this));
    }
}
