package dev.oop778.infusor.provider;

import net.endergrid.atom.typetoken.TypeToken;
import dev.oop778.infusor.InfuseResolveResult;
import dev.oop778.infusor.InfusorContext;
import dev.oop778.infusor.spec.InfusableClass;
import dev.oop778.infusor.spec.InfusorClassSpecRegistry;

public class EagerSingletonProvider<T> extends AbstractThreadSafeProvider<T> {
    private final Class<T> type;
    private final boolean needsInfusing;
    private final InfusableClass<T> infusableClass;

    public EagerSingletonProvider(Class<T> type, T object, boolean needsInfusing) {
        this.type = type;
        if (object != null) {
            this.reference.set(object);
        }

        this.needsInfusing = needsInfusing;
        this.infusableClass = InfusorClassSpecRegistry.get().get(type);
    }

    @Override
    public TypeToken<T> getTypeToken() {
        return TypeToken.convert(this.type);
    }

    @Override
    public boolean needsInfusing() {
        return this.needsInfusing;
    }

    @Override
    protected InfuseResolveResult<T> resolve0(InfusorContext context) {
        return this.infusableClass.infuse(context);
    }
}
