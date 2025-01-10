import net.endergrid.infusor.Infusor;
import net.endergrid.infusor.annotation.Infusable;
import net.endergrid.infusor.annotation.Infuse;
import net.endergrid.infusor.annotation.Scoped;

@Infusable
public class TestListener {
    private final Infusor infusor;

    private TestListener(@Scoped("platform.global") @Infuse Infusor infusor) {
        this.infusor = infusor;
    }
}
