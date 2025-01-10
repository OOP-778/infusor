package dev.oop778.infusor.registration;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.atom.typetoken.TypeTokenMatcher;
import dev.oop778.infusor.context.InfusorMultiContextImpl;
import dev.oop778.infusor.InfusorImpl;
import dev.oop778.infusor.InfusorNodes;
import dev.oop778.infusor.provider.EagerSingletonProvider;
import dev.oop778.infusor.provider.InfusorProvider;

import java.util.ArrayList;
import java.util.List;

public class InfusorPendingRegistrationImpl implements InfusorPendingRegistration {
    private final InfusorImpl infusor;
    private final List<PendingRegistration> pendingRegistrationList = new ArrayList<>();

    public InfusorPendingRegistrationImpl(InfusorImpl infusor) {
        this.infusor = infusor;
    }

    @Override
    public <T> InfusorRegistrationStaged<T, InfusorPendingRegistration> registerMatching(TypeTokenMatcher<TypeToken<? super T>> typeTokenMatcher) {
        final MatchingRegistration<T> matchingRegistration = new MatchingRegistration<>(typeTokenMatcher);
        this.pendingRegistrationList.add(matchingRegistration);

        return new InfusorPendingRegistrationPart<>((completeConfig) -> {
            matchingRegistration.setConfig(completeConfig);
            return this;
        });
    }

    @Override
    public <T> InfusorRegistrationStaged<T, InfusorPendingRegistration> registerExact(TypeToken<T> typeToken) {
        final ExactRegistration<T> exactRegistration = new ExactRegistration<>(typeToken);
        this.pendingRegistrationList.add(exactRegistration);

        return new InfusorPendingRegistrationPart<>((completeConfig) -> {
            exactRegistration.setConfig(completeConfig);
            return this;
        });
    }

    @Override
    public InfusorPendingRegistration scan(ClassLoader classLoader) {
        return this;
    }

    @Override
    public InfusorPendingRegistration scan(Class<?>... classes) {
        return this;
    }

    @Override
    public void complete() {
        System.out.printf("registering %s registrations%n", this.pendingRegistrationList.size());

        final List<PendingRegistration<?>> toResolve = new ArrayList<>(2);

        // Register all the pending registrations
        for (final PendingRegistration<?> pendingRegistration : this.pendingRegistrationList) {
            pendingRegistration.register(this.infusor.getNodes());
            final InfusorProvider provider = pendingRegistration.getConfig().getProvider();
            if (!(provider instanceof EagerSingletonProvider)) {
                continue;
            }

            if (!provider.needsInfusing()) {
                continue;
            }

            toResolve.add(pendingRegistration);
        }

        final InfusorMultiContextImpl context = new InfusorMultiContextImpl(this.infusor);

        // Resolve eager registrations
        for (final PendingRegistration<?> pendingRegistration : toResolve) {
            final TypeToken typeToken = pendingRegistration.getConfig().getProvider().getTypeToken();
            context.tryResolve(typeToken, pendingRegistration.getConfig().getScope(), null);
        }

        context.resolve();
    }

    interface PendingRegistration<T> {
        void register(InfusorNodes nodes);

        InfusorPendingRegistrationPart<T, ?> getConfig();
    }

    @Setter
    @RequiredArgsConstructor
    @Getter
    private class MatchingRegistration<T> implements PendingRegistration<T> {
        private final TypeTokenMatcher<?> typeTokenMatcher;
        private InfusorPendingRegistrationPart<T, ?> config;

        @Override
        public void register(InfusorNodes nodes) {
            nodes.registerMatching(this.typeTokenMatcher, this.config.getScope(), this.config.getProvider());
        }
    }

    @Setter
    @RequiredArgsConstructor
    @Getter
    private class ExactRegistration<T> implements PendingRegistration<T> {
        private final TypeToken<?> typeToken;
        private InfusorPendingRegistrationPart<T, ?> config;

        @Override
        public void register(InfusorNodes nodes) {
            nodes.registerExact(this.typeToken, this.config.getScope(), this.config.getProvider());
        }
    }
}
