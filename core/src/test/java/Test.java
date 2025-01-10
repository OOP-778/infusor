import net.endergrid.atom.AtomImpl;
import net.endergrid.atom.typetoken.TypeToken;
import net.endergrid.atom.typetoken.TypeTokenMatcher;
import net.endergrid.infusor.InfusorImpl;
import net.endergrid.infusor.scope.InfusorDefaultScopes;
import net.endergrid.infusor.scope.InfusorDefinedScope;

public class Test {

    public static void main(String[] args) {
        final AtomImpl build = AtomImpl.builder().build();
        final InfusorImpl infusor = new InfusorImpl();

        infusor.defaultScope(InfusorDefinedScope.Context.class, InfusorDefaultScopes.Context.GLOBAL);
        infusor.defaultScope(InfusorDefinedScope.Platform.class, InfusorDefaultScopes.Platform.GLOBAL);

        infusor.createRegistration((configurator) -> configurator
                .registerExact(TypeToken.convert(TestSingletonA.class))
                .singleton()
                .eager(TestSingletonA.class)
                .withDefaultScopes()
                .complete()

                .registerExact(TypeToken.convert(TestSingletonB.class))
                .singleton()
                .eager(TestSingletonB.class)
                .withDefaultScopes()
                .complete()

                .registerMatching(TypeTokenMatcher.isSuperClass(TestSingletonC.class))
                .singleton()
                .lazy(TestSingletonC.class)
                .withDefaultScopes()
                .complete()
        ).complete();
    }
}
