package doposoccer.application;

import doposoccer.domain.Side;

/**
 * Resultado inmutable de un partido terminado. La presentacion lo usa para
 * mostrar el ganador, el marcador y los puntos sin depender de los detalles
 * internos del agregado.
 *
 * @param winner lado ganador, o {@code null} si hubo empate
 * @param leftGoals goles del lado izquierdo
 * @param rightGoals goles del lado derecho
 * @param leftBalls balones restantes del lado izquierdo
 * @param rightBalls balones restantes del lado derecho
 * @param leftPoints puntos de perfil ganados por el lado izquierdo
 * @param rightPoints puntos de perfil ganados por el lado derecho
 * @param survival indica si el resultado pertenece a supervivencia
 * @param description descripcion legible del tipo de partido
 */
public record MatchResult(
        Side winner,
        int leftGoals,
        int rightGoals,
        int leftBalls,
        int rightBalls,
        int leftPoints,
        int rightPoints,
        boolean survival,
        String description) {

    /**
     * Valida que los contadores no sean negativos.
     */
    public MatchResult {
        if (leftGoals < 0 || rightGoals < 0 || leftBalls < 0 || rightBalls < 0) {
            throw new IllegalArgumentException("Los contadores del resultado no pueden ser negativos.");
        }
        if (leftPoints < 0 || rightPoints < 0) {
            throw new IllegalArgumentException("Los puntos del resultado no pueden ser negativos.");
        }
        if (description == null || description.isBlank()) {
            throw new IllegalArgumentException("La descripcion no puede estar vacia.");
        }
    }

    /**
     * Indica si el partido termino empatado.
     *
     * @return {@code true} si no hay ganador
     */
    public boolean isDraw() {
        return winner == null;
    }
}
