package net.endergrid.infusor;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.atom.typetoken.TypeTokenMatchedRegistry;
import net.endergrid.atom.typetoken.TypeTokenMatcher;
import net.endergrid.infusor.context.InfusorMultiContextImpl;
import net.endergrid.infusor.provider.InfusorProvider;
import net.endergrid.infusor.scope.InfusorComposedScope;
import net.endergrid.infusor.scope.InfusorDefinedScope;
import net.endergrid.infusor.scope.InfusorScope;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InfusorNodes {
    private final Map<TypeToken<?>, InfusorNode> nodes = new ConcurrentHashMap<>();
    private final TypeTokenMatchedRegistry<InfusorNode> typeTokenMatchedNodes = new TypeTokenMatchedRegistry<>();

    public void registerMatching(TypeTokenMatcher<?> typeTokenMatcher, InfusorComposedScope scope, InfusorProvider provider) {
        this.typeTokenMatchedNodes.registerFirst(typeTokenMatcher, new InfusorNode(scope, provider));
    }

    public void registerExact(TypeToken<?> typeToken, InfusorComposedScope scope, InfusorProvider provider) {
        this.nodes.put(typeToken, new InfusorNode(scope, provider));
    }

    public <T> InfuseResolveResult<T> tryResolve(TypeToken<T> typeToken, @Nullable InfusorScope scope, InfusorMultiContextImpl infusorContext) {
        final InfusorNode infusorNode = this.nodes.get(typeToken);
        if (infusorNode != null) {
            return this.tryResolveNode(infusorNode, scope, infusorContext);
        }

        for (final InfusorNode node : this.typeTokenMatchedNodes.find(typeToken)) {
            final InfuseResolveResult<T> result = this.tryResolveNode(node, scope, infusorContext);
            if (result instanceof InfuseResolveResult.Resolved) {
                return result;
            }
        }

        return InfuseResolveResult.unknown();
    }

    private <T> InfuseResolveResult<T> tryResolveNode(InfusorNode node, @Nullable InfusorScope scope, InfusorMultiContextImpl infusorContext) {
        return (InfuseResolveResult<T>) node.provider.resolve(infusorContext);
    }

    @RequiredArgsConstructor
    public static class InfusorNode {
        private final InfusorComposedScope scope;
        private final InfusorProvider<?> provider;

        public boolean isExact(@NonNull InfusorComposedScope otherScope) {
            return this.scope.equals(otherScope);
        }

        public boolean isAssignableFrom(@NonNull InfusorComposedScope otherScope) {
            final InfusorDefinedScope[] otherScopes = otherScope.getDefinedScopes();
            final InfusorDefinedScope[] myScopes = this.scope.getDefinedScopes();

            for (int i = 0; i < myScopes.length; i++) {
                if (i == otherScopes.length) {
                    return false;
                }

                if (!myScopes[i].canBeAssignedFrom(otherScopes[i])) {
                    return false;
                }
            }

            return true;
        }
    }
}
