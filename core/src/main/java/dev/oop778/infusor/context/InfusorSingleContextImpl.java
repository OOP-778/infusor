package dev.oop778.infusor.context;

import net.endergrid.atom.typetoken.TypeToken;
import dev.oop778.infusor.InfuseResolveResult;
import dev.oop778.infusor.InfusorImpl;
import dev.oop778.infusor.scope.InfusorScope;
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
