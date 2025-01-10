package dev.oop778.infusor;

import dev.oop778.infusor.scope.InfusorDefaultScopes;
import dev.oop778.infusor.scope.InfusorDefinedScope;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

public class InfusorScopeRegistryImpl {
    private final Map<String, InfusorDefinedScope> namedLookup = new ConcurrentHashMap<>();
    private final List<Class<? extends InfusorDefinedScope>> scopes = new CopyOnWriteArrayList<>();
    public static final InfusorScopeRegistryImpl INSTANCE = new InfusorScopeRegistryImpl();

    public InfusorScopeRegistryImpl() {
        this.registerScopeType(InfusorDefinedScope.Context.class);
        this.registerScopeType(InfusorDefinedScope.Platform.class);

        this.registerScope("context.local", InfusorDefaultScopes.Context.LOCAL);
        this.registerScope("context.global", InfusorDefaultScopes.Context.GLOBAL);

        this.registerScope("platform.global", InfusorDefaultScopes.Platform.GLOBAL);
        this.registerScope("platform.spigot", InfusorDefaultScopes.Platform.SPIGOT);
    }

    public static InfusorScopeRegistryImpl get() {
        return INSTANCE;
    }

    public int indexOf(Class<? extends InfusorDefinedScope> scope) {
        return this.scopes.indexOf(scope);
    }

    public int size() {
        return this.scopes.size();
    }

    public int indexOf(InfusorDefinedScope scope) {
        Class<?> currentClass = scope.getClass();
        while (currentClass != null) {
            for (final Class<?> anInterface : currentClass.getInterfaces()) {
                for (final Class<?> anInterfaceInterface : anInterface.getInterfaces()) {
                    if (anInterfaceInterface == InfusorDefinedScope.class) {
                        return this.indexOf((Class<? extends InfusorDefinedScope>) anInterface);
                    }
                }
            }

            currentClass = currentClass.getSuperclass();
        }

        return -1;
    }

    public void registerScopeType(Class<? extends InfusorDefinedScope> scope) {
        this.scopes.add(scope);
    }

    public void registerScope(String name, InfusorDefinedScope scope) {
        this.namedLookup.put(name.toLowerCase(), scope);
    }

    public InfusorDefinedScope find(String path) {
        return this.namedLookup.get(path.toLowerCase());
    }
}
