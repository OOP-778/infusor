package dev.oop778.infusor.provider;

import net.endergrid.atom.typetoken.TypeToken;
import dev.oop778.infusor.InfuseResolveResult;
import dev.oop778.infusor.InfusorContext;

public interface InfusorProvider<T> {
    TypeToken<T> getTypeToken();

    boolean needsInfusing();

    InfuseResolveResult<T> resolve(InfusorContext context);

    boolean isResolved();
}
