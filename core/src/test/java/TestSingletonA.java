import net.endergrid.infusor.annotation.Infuse;

public class TestSingletonA {

    public TestSingletonA(@Infuse TestSingletonB singletonB, @Infuse TestSingletonC singletonC) {
        System.out.println(singletonB);
    }
}
