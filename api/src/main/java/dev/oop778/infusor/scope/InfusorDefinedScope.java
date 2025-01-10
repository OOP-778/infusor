package dev.oop778.infusor.scope;

public interface InfusorDefinedScope extends InfusorScope {
    boolean canBeAssignedFrom(InfusorDefinedScope other);

    interface Context extends InfusorDefinedScope {}

    interface Platform extends InfusorDefinedScope {}
}
