package net.endergrid.infusor.scope;

public interface InfusorComposedScope extends InfusorScope {

    InfusorDefinedScope[] getDefinedScopes();

    static InfusorComposedScope compose(InfusorDefinedScope ...definedScopes) {
        return null;
    }
}
