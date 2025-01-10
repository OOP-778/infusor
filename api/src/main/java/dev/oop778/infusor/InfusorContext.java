package dev.oop778.infusor;

import lombok.NonNull;
import net.endergrid.atom.typetoken.TypeToken;
import dev.oop778.infusor.scope.InfusorComposedScope;
import dev.oop778.infusor.scope.InfusorScope;
import org.jetbrains.annotations.Nullable;

public interface InfusorContext {
    Infusor getInitiator();
    InfusorComposedScope getScope();

    <T> InfuseResolveResult<T> tryResolve(@NonNull TypeToken<T> typeToken, @Nullable InfusorScope scope);
}
