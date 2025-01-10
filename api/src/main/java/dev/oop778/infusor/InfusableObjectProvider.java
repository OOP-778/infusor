package dev.oop778.infusor;

@FunctionalInterface
public interface InfusableObjectProvider<T> {
    InfuseResolveResult<T> create(InfusorContext context);
}
