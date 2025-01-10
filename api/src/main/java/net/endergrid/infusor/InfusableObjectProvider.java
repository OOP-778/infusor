package net.endergrid.infusor;

@FunctionalInterface
public interface InfusableObjectProvider<T> {
    InfuseResolveResult<T> create(InfusorContext context);
}
