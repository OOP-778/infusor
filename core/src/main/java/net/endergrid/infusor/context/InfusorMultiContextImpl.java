package net.endergrid.infusor.context;

import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.infusor.InfuseResolveResult;
import net.endergrid.infusor.InfusorContext;
import net.endergrid.infusor.InfusorImpl;
import net.endergrid.infusor.PathProvider;
import net.endergrid.infusor.scope.InfusorComposedScope;
import net.endergrid.infusor.scope.InfusorScope;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.util.*;

@Getter
@Slf4j
public class InfusorMultiContextImpl implements InfusorContext {
    protected final InfusorImpl initiator;
    protected final Map<TypeToken<?>, WaitingResolveEntry> waitingResolveEntryMap = new HashMap<>();
    protected final Queue<WaitingResolveEntry> waitingResolveStack = new LinkedList<>();
    protected final Stack<Object> tracing;
    protected final Set<String> errors;

    @Setter
    private WaitingResolveEntry currentResolveEntry;

    public InfusorMultiContextImpl(InfusorImpl initiator) {
        this.initiator = initiator;
        this.errors = new LinkedHashSet<>();
        this.tracing = new Stack<>();
    }

    @Override
    public InfusorComposedScope getScope() {
        return this.initiator.getDefaultScope();
    }

    @Override
    public <T> InfuseResolveResult<T> tryResolve(@NonNull TypeToken<T> typeToken, @Nullable InfusorScope scope) {
        return this.tryResolve(typeToken, scope, null);
    }

    public void addError(String message, Object... extraPath) {
        final Object peek = this.tracing.peek();
        if (peek instanceof WaitingResolveEntry) {
            final WaitingResolveEntry entry = (WaitingResolveEntry) peek;
            if (!entry.errorsReported.add(message)) {
                return;
            }
        }

        this.errors.add(this.buildPath(extraPath) + ": " + message);
    }

    public String buildString(Object pathElement) {
        if (pathElement instanceof WaitingResolveEntry) {
            final WaitingResolveEntry waitingResolveEntry = (WaitingResolveEntry) pathElement;
            return waitingResolveEntry.typeToken.toString(false);
        } else if (pathElement instanceof PathProvider) {
            final PathProvider pathProvider = (PathProvider) pathElement;
            return pathProvider.toString();
        }

        return pathElement.toString();
    }

    public String buildPath(Object... extraPath) {
        final InfusorScope scope = this.getScope();
        final StringBuilder stringBuilder = new StringBuilder();

        final Iterator<Object> iterator = this.tracing.iterator();
        while (iterator.hasNext()) {
            final Object object = iterator.next();
            stringBuilder.append(this.buildString(object));

            if (iterator.hasNext()) {
                stringBuilder.append(" -> ");
            }
        }

        if (extraPath.length > 0) {
            stringBuilder.append(" -> ");
            for (int i = 0; i < extraPath.length; i++) {
                stringBuilder.append(this.buildString(extraPath[i]));

                if (i < extraPath.length - 1) {
                    stringBuilder.append(" -> ");
                }
            }
        }

        return stringBuilder.toString();
    }

    public <T> InfuseResolveResult<T> tryResolve(TypeToken<T> typeToken, @Nullable InfusorScope scope, PathProvider caller) {
        if (scope == null) {
            scope = this.getScope();
        }

        @Nullable InfusorScope finalScope = scope;
        final WaitingResolveEntry waitingToBeResolved = this.waitingResolveEntryMap.computeIfAbsent(typeToken, ($) -> {
            final WaitingResolveEntry waitingResolveEntry = new WaitingResolveEntry(typeToken, finalScope);
            this.waitingResolveStack.add(waitingResolveEntry);

            return waitingResolveEntry;
        });

        final WaitingResolveEntry previous = this.currentResolveEntry;
        try {
            this.currentResolveEntry = waitingToBeResolved;
            if (caller != null) {
                this.tracing.push(caller);
            }

            if (this.tracing.contains(waitingToBeResolved)) {
                this.addError("Circular", waitingToBeResolved);
                return InfuseResolveResult.error();
            }

            this.tracing.push(waitingToBeResolved);

            if (waitingToBeResolved.result == null) {
                waitingToBeResolved.tryUpdateResult();
            }
        } finally {
            this.currentResolveEntry = previous;
            if (!this.tracing.isEmpty()) {
                this.tracing.pop();
                if (!this.tracing.isEmpty()) {
                    if (caller != null) {
                        this.tracing.pop();
                    }
                }
            }
        }

        return (InfuseResolveResult<T>) waitingToBeResolved.result;
    }

    public <T> T resolve() {
        this.awaitResolve();

        if (!this.errors.isEmpty()) {
            final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            final PrintWriter writer = new PrintWriter(byteArrayOutputStream);
            writer.println();

            for (final String error : this.errors) {
                writer.println("- " + error);
            }

            writer.flush();
            log.error("Failed to resolve: {}", byteArrayOutputStream);
        }

        return null;
    }

    protected void awaitResolve() {
        while (!this.waitingResolveStack.isEmpty()) {
            final WaitingResolveEntry waitingResolveEntry = this.waitingResolveStack.poll();
            this.tryResolve(waitingResolveEntry);
        }
    }

    private void tryResolve(WaitingResolveEntry waitingResolveEntry) {
        this.currentResolveEntry = waitingResolveEntry;
        this.tracing.add(this.currentResolveEntry);
        try {
            waitingResolveEntry.tryUpdateResult();
        } finally {
            this.currentResolveEntry = null;
            if (!tracing.isEmpty()) {
                this.tracing.pop();
            }
        }

        final InfuseResolveResult<?> result = waitingResolveEntry.getResult();
        if (result == InfuseResolveResult.UNKNOWN) {
            return;
        }

        if (!(result instanceof InfuseResolveResult.Resolved || result == InfuseResolveResult.ERROR)) {
            this.waitingResolveStack.add(waitingResolveEntry);
        }
    }

    @RequiredArgsConstructor
    public class WaitingResolveEntry {
        private final TypeToken<?> typeToken;
        private final InfusorScope scope;
        private final Set<String> errorsReported = new HashSet<>();

        @Setter
        @Getter
        private InfuseResolveResult<?> result;

        @Override
        public String toString() {
            return "WaitingResolveEntry{" +
                    "typeToken=" + this.typeToken +
                    ", scope=" + this.scope +
                    ", result=" + this.result +
                    '}';
        }

        public void tryUpdateResult() {
            this.result = InfusorMultiContextImpl.this.initiator.getNodes().tryResolve(this.typeToken, this.scope, InfusorMultiContextImpl.this);
        }
    }
}
