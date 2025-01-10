package net.endergrid.infusor.scope;

public interface InfusorDefinedScope extends InfusorScope {
    boolean canBeAssignedFrom(InfusorDefinedScope other);

    interface Context extends InfusorDefinedScope {}

    interface Platform extends InfusorDefinedScope {}
}
