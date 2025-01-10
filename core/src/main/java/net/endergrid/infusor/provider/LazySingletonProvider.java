package net.endergrid.infusor.provider;

import lombok.NonNull;
import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.infusor.InfusableObjectProvider;
import net.endergrid.infusor.InfuseResolveResult;
import net.endergrid.infusor.InfusorContext;
import net.endergrid.infusor.spec.InfusableClass;
import net.endergrid.infusor.spec.InfusorClassSpecRegistry;

public class LazySingletonProvider<T> extends AbstractThreadSafeProvider<T> {
    private final InfusableObjectProvider<T> provider;

    public LazySingletonProvider(@NonNull InfusableObjectProvider<T> provider) {
        this.provider = provider;
    }

    public LazySingletonProvider(Class<T> clazz) {
        final InfusableClass<T> infusableClass = InfusorClassSpecRegistry.get().get(clazz);
        this.provider = (context) -> infusableClass.infuse(context);
    }

    @Override
    public TypeToken<T> getTypeToken() {
        throw new IllegalStateException("not supported");
    }

    @Override
    public boolean needsInfusing() {
        return false;
    }

    @Override
    protected InfuseResolveResult<T> resolve0(InfusorContext context) {
        return this.provider.create(context);
    }
}
