package dev.oop778.infusor.annotation;

public @interface Singleton {
    boolean eager() default true;
}
