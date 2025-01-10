import lombok.Getter;
import dev.oop778.infusor.annotation.Infuse;

@Getter
public class TestSingletonB {
    @Infuse
    private TestSingletonC gg;

}
