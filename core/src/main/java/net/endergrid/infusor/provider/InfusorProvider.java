package net.endergrid.infusor.provider;

import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.infusor.InfuseResolveResult;
import net.endergrid.infusor.InfusorContext;

public interface InfusorProvider<T> {
    TypeToken<T> getTypeToken();

    boolean needsInfusing();

    InfuseResolveResult<T> resolve(InfusorContext context);

    boolean isResolved();
}
