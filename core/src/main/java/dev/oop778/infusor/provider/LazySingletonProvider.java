package dev.oop778.infusor.provider;

import lombok.NonNull;
import net.endergrid.atom.typetoken.TypeToken;
import dev.oop778.infusor.InfusableObjectProvider;
import dev.oop778.infusor.InfuseResolveResult;
import dev.oop778.infusor.InfusorContext;
import dev.oop778.infusor.spec.InfusableClass;
import dev.oop778.infusor.spec.InfusorClassSpecRegistry;

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
