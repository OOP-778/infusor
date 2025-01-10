package dev.oop778.infusor.provider;

import net.endergrid.atom.typetoken.TypeToken;
import dev.oop778.infusor.InfuseResolveResult;
import dev.oop778.infusor.InfusorContext;

public class FactoryProvider<T> implements InfusorProvider<T> {

    @Override
    public boolean needsInfusing() {
        return false;
    }

    @Override
    public InfuseResolveResult<T> resolve(InfusorContext context) {
        return null;
    }

    @Override
    public boolean isResolved() {
        return false;
    }

    @Override
    public TypeToken<T> getTypeToken() {
        return null;
    }
}
