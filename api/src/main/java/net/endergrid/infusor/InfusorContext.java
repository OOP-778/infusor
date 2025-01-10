package net.endergrid.infusor;

import lombok.NonNull;
import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.infusor.scope.InfusorComposedScope;
import net.endergrid.infusor.scope.InfusorScope;
import org.jetbrains.annotations.Nullable;

public interface InfusorContext {
    Infusor getInitiator();
    InfusorComposedScope getScope();

    <T> InfuseResolveResult<T> tryResolve(@NonNull TypeToken<T> typeToken, @Nullable InfusorScope scope);
}
