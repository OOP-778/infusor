package dev.oop778.infusor.registration;

import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.atom.typetoken.TypeTokenMatcher;

public interface InfusorPendingRegistration {
    <T> InfusorRegistrationStaged<T, InfusorPendingRegistration> registerMatching(TypeTokenMatcher<TypeToken<? super T>> typeTokenMatcher);
    <T> InfusorRegistrationStaged<T, InfusorPendingRegistration> registerExact(TypeToken<T> typeTokenMatcher);

    InfusorPendingRegistration scan(ClassLoader classLoader);

    InfusorPendingRegistration scan(Class<?> ...classes);

    void complete();
}
