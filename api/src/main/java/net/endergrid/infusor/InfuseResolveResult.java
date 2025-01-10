package net.endergrid.infusor;

public interface InfuseResolveResult<T> {
    InfuseResolveResult<Object> WAITING_FOR_PARAM_RESOLVE = new InfuseResolveResult<Object>() {
        @Override
        public String toString() {
            return "WAITING_FOR_PARAM_RESOLVE";
        }
    };

    InfuseResolveResult<Object> UNKNOWN = new InfuseResolveResult<Object>() {
        @Override
        public String toString() {
            return "UNKNOWN";
        }
    };

    InfuseResolveResult<Object> ERROR = new InfuseResolveResult<Object>() {
        @Override
        public String toString() {
            return "ERROR";
        }
    };

    @Override
    String toString();

    static <T> InfuseResolveResult<T> unresolved() {
        return (InfuseResolveResult<T>) WAITING_FOR_PARAM_RESOLVE;
    }

    static <T> InfuseResolveResult<T> unknown() {
        return (InfuseResolveResult<T>) UNKNOWN;
    }

    static <T> InfuseResolveResult<T> resolved(T object) {
        return new Resolved<>(object);
    }

    static <T> InfuseResolveResult<T> error() {
        return (InfuseResolveResult<T>) ERROR;
    }

    class Resolved<T> implements InfuseResolveResult<T> {
        private final T object;

        public Resolved(T object) {
            this.object = object;
        }

        public T get() {
            return this.object;
        }

        @Override
        public String toString() {
            return "RESOLVED";
        }
    }
}
