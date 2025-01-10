package dev.oop778.infusor.scope;

import lombok.RequiredArgsConstructor;
import dev.oop778.infusor.InfusorScopeRegistryImpl;

import java.util.Arrays;

@RequiredArgsConstructor
public class ComposedScopeImpl implements InfusorComposedScope {
    private final InfusorDefinedScope[] definedScopes;

    public static ComposedScopeImpl create(InfusorScope... scopes) {
        final InfusorDefinedScope[] composed = new InfusorDefinedScope[InfusorScopeRegistryImpl.INSTANCE.size()];
        for (final InfusorScope scope : scopes) {
            visit(scope, composed);
        }

        return new ComposedScopeImpl(composed);
    }

    private static void visit(InfusorScope scope, InfusorDefinedScope[] scopes) {
        if (scope instanceof InfusorDefinedScope) {
            final InfusorDefinedScope definedScope = (InfusorDefinedScope) scope;
            final int index = InfusorScopeRegistryImpl.INSTANCE.indexOf(definedScope);
            if (index == -1) {
                throw new IllegalArgumentException("Scope " + definedScope.getClass() + " parent is not registered");
            }

            final InfusorDefinedScope alreadyDefined = scopes[index];
            if (alreadyDefined != null) {
                return;
            }

            scopes[index] = definedScope;
            return;
        }

        if (scope instanceof InfusorComposedScope) {
            final InfusorComposedScope composedScope = (InfusorComposedScope) scope;
            for (final InfusorDefinedScope definedScope : composedScope.getDefinedScopes()) {
                visit(definedScope, scopes);
            }
        }
    }

    @Override
    public String toString() {
        return "ComposedScopeImpl{" +
                "definedScopes=" + Arrays.toString(this.definedScopes) +
                '}';
    }

    @Override
    public InfusorDefinedScope[] getDefinedScopes() {
        return new InfusorDefinedScope[0];
    }
}
