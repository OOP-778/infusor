package net.endergrid.infusor;

import lombok.Getter;
import lombok.NonNull;
import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.infusor.context.InfusorSingleContextImpl;
import net.endergrid.infusor.registration.InfusorPendingRegistration;
import net.endergrid.infusor.registration.InfusorPendingRegistrationImpl;
import net.endergrid.infusor.scope.ComposedScopeImpl;
import net.endergrid.infusor.scope.InfusorComposedScope;
import net.endergrid.infusor.scope.InfusorDefinedScope;

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
