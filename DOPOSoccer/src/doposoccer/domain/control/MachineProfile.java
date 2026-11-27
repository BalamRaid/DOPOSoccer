package doposoccer.domain.control;

import java.util.function.Supplier;

/**
 * Perfiles de jugador maquina que se pueden elegir al configurar un partido.
 *
 * <p>Punto de extension: para agregar un perfil nuevo se crea una subclase de
 * {@link AiController} y se agrega una constante aqui.</p>
 */
public enum MachineProfile {

    /** Escoge al azar sus movimientos. */
    CLUMSY("Torpe", ClumsyMachine::new),

    /** Su prioridad es anotar. */
    OFFENSIVE("Ofensiva", OffensiveMachine::new),

    /** Su prioridad es defender el arco. */
    DEFENSIVE("Defensiva", DefensiveMachine::new),

    /** Decide la accion segun el estado del partido. */
    EXPERT("Experta", ExpertMachine::new);

    private final String displayName;
    private final Supplier<AiController> factory;

    MachineProfile(String displayName, Supplier<AiController> factory) {
        this.displayName = displayName;
        this.factory = factory;
    }

    /**
     * Crea un controlador nuevo de este perfil.
     *
     * @return maquina lista para jugar
     */
    public AiController create() {
        return factory.get();
    }

    /**
     * Nombre visible del perfil.
     *
     * @return nombre en espanol
     */
    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
