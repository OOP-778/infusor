package dev.oop778.infusor.provider;

import dev.oop778.infusor.InfuseResolveResult;
import dev.oop778.infusor.InfusorContext;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public abstract class AbstractThreadSafeProvider<T> implements InfusorProvider<T> {
    protected final AtomicReference<Object> reference = new AtomicReference<>(NOT_SET);
    protected final AtomicBoolean inside = new AtomicBoolean(false);

    private static final Object NOT_SET = new Object();

    @Override
    public InfuseResolveResult<T> resolve(InfusorContext context) {
        final Object current = this.reference.get();
        if (current != NOT_SET) {
            return InfuseResolveResult.resolved((T) current);
        }

        // Will come back to this later if we're inside ;>
        if (!this.inside.compareAndSet(false, true)) {
            return InfuseResolveResult.unresolved();
        }

        try {
            final InfuseResolveResult<T> result = this.resolve0(context);
            if (result instanceof InfuseResolveResult.Resolved) {
                this.reference.set(((InfuseResolveResult.Resolved<T>) result).get());
                return result;
            }

            return result;
        } finally {
            this.inside.set(false);
        }
    }

    @Override
    public boolean isResolved() {
        return this.reference.get() != NOT_SET;
    }

    protected abstract InfuseResolveResult<T> resolve0(InfusorContext context);
}
