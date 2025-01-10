package dev.oop778.infusor.scope;

public interface InfusorDefaultScopes {
    enum Context implements InfusorDefinedScope.Context {
        GLOBAL,
        LOCAL;

        @Override
        public boolean canBeAssignedFrom(InfusorDefinedScope other) {
            return this == GLOBAL || other == this;
        }

        @Override
        public String toString() {
            return String.format("%s{%s}", this.getClass().getSimpleName(), this.name());
        }
    }

    enum Platform implements InfusorDefinedScope.Platform {
        GLOBAL,
        SPIGOT
        ;

        @Override
        public boolean canBeAssignedFrom(InfusorDefinedScope other) {
            return this == GLOBAL || other == this;
        }

        @Override
        public String toString() {
            return String.format("%s{%s}", this.getClass().getSimpleName(), this.name());
        }
    }
}
