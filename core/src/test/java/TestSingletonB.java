import lombok.Getter;
import net.endergrid.infusor.annotation.Infuse;

@Getter
public class TestSingletonB {
    @Infuse
    private TestSingletonC gay;

}
