package net.endergrid.infusor.annotation;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.TYPE, ElementType.PARAMETER})
@Inherited
public @interface Infuse {
}
