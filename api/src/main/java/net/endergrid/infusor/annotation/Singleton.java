package net.endergrid.infusor.annotation;

public @interface Singleton {
    boolean eager() default true;
}
