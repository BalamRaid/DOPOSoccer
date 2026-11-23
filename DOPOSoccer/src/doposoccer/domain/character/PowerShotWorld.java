package doposoccer.domain.character;

import doposoccer.domain.Ball;

/**
 * Operaciones del partido que un tiro de poder puede necesitar.
 *
 * <p>La interfaz mantiene los tiros desacoplados del agregado concreto del
 * partido. Hoy solo permite agregar balones falsos, que es lo que necesita el
 * poder Ilusion; si en el futuro un poder crea otra entidad, se amplia esta
 * frontera y no la clase del personaje.</p>
 */
public interface PowerShotWorld {

    /**
     * Agrega un balon falso al partido.
     *
     * @param fakeBall balon temporal creado por el tiro
     */
    void addFakeBall(Ball fakeBall);
}
