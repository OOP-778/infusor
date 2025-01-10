package net.endergrid.infusor.provider;

import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.infusor.InfuseResolveResult;
import net.endergrid.infusor.InfusorContext;

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
