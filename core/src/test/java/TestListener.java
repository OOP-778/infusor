import dev.oop778.infusor.Infusor;
import dev.oop778.infusor.annotation.Infusable;
import dev.oop778.infusor.annotation.Infuse;
import dev.oop778.infusor.annotation.Scoped;

@Infusable
public class TestListener {
    private final Infusor infusor;

    private TestListener(@Scoped("platform.global") @Infuse Infusor infusor) {
        this.infusor = infusor;
    }
}
