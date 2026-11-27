package doposoccer.domain.character;

import doposoccer.domain.Ball;
import doposoccer.domain.Side;
import doposoccer.domain.effect.Effect;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Poder especial de un personaje. Combina tres piezas reutilizables: el nombre
 * visible, el dano, el efecto que recibe el rival y la trayectoria del balon.
 *
 * <p>La clase es inmutable. Dos personajes pueden compartir la misma
 * trayectoria o el mismo efecto sin heredar entre si; por ejemplo, Canada y
 * China usan un tiro recto y solo cambian el efecto.</p>
 */
public final class SpecialAbility {

    private final String name;
    private final int damage;
    private final String effectName;
    private final Supplier<Effect> effectFactory;
    private final PowerShot shot;

    /**
     * Crea un poder especial.
     *
     * @param name nombre visible del poder
     * @param damage dano base del impacto
     * @param effectName nombre visible del efecto
     * @param effectFactory fabrica que crea una instancia nueva del efecto
     * @param shot trayectoria del balon
     */
    public SpecialAbility(String name, int damage, String effectName,
            Supplier<Effect> effectFactory, PowerShot shot) {
        this.name = requireText(name, "name");
        this.effectName = requireText(effectName, "effectName");
        if (damage < 0) {
            throw new IllegalArgumentException("El dano no puede ser negativo.");
        }
        this.damage = damage;
        this.effectFactory = Objects.requireNonNull(effectFactory, "effectFactory no puede ser null.");
        this.shot = Objects.requireNonNull(shot, "shot no puede ser null.");
    }

    public String name() {
        return name;
    }

    public int damage() {
        return damage;
    }

    public String effectName() {
        return effectName;
    }

    /**
     * Crea una instancia nueva del efecto para que cada impacto tenga su
     * propio temporizador.
     *
     * @return efecto listo para aplicarse al rival
     */
    public Effect createEffect() {
        return effectFactory.get();
    }

    /**
     * Lanza el balon con la trayectoria del poder.
     *
     * @param ball balon real del partido
     * @param shooter lado del jugador que ejecuta el tiro
     * @param world partido que recibe elementos adicionales
     */
    public void launch(Ball ball, Side shooter, PowerShotWorld world) {
        shot.launch(ball, shooter, world);
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " no puede estar vacio.");
        }
        return value;
    }
}
