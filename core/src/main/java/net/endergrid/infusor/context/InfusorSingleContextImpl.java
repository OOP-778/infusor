package net.endergrid.infusor.context;

import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.infusor.InfuseResolveResult;
import net.endergrid.infusor.InfusorImpl;
import net.endergrid.infusor.scope.InfusorScope;
import org.jetbrains.annotations.Nullable;

public class InfusorSingleContextImpl extends InfusorMultiContextImpl {

    public InfusorSingleContextImpl(InfusorImpl initiator) {
        super(initiator);
    }

    public <T> T resolve(TypeToken<T> typeToken, @Nullable InfusorScope scope) {
        final WaitingResolveEntry entry = new WaitingResolveEntry(typeToken, scope);
        this.waitingResolveEntryMap.put(typeToken, entry);
        this.waitingResolveStack.add(entry);

        this.resolve();

        final InfuseResolveResult<?> result = entry.getResult();
        if (result instanceof InfuseResolveResult.Resolved) {
            return (T) ((InfuseResolveResult.Resolved<?>) result).get();
        }

        throw new IllegalStateException("failed to resolve");
    }
}
