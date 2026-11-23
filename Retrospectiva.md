# DOPOSoccer – Versión uno (Presentación + Dominio)

## Retrospectiva

**1. ¿Cuál fue el tiempo total invertido por cada uno de ustedes? (horas/hombre)**
- Integrante 1: ___ horas
- Integrante 2: ___ horas

**2. ¿Cuál es el estado actual del proyecto en términos de mini-ciclos? ¿Por qué?**
La versión uno está completa:
- Configuración del juego: modalidad, resolución de la cancha (3 opciones), personaje de cada jugador, tipo de partido (por tiempo o por goles, con su valor) y mejora de estadísticas.
- Partido Jugador vs Jugador en nivel básico, con movimiento, salto, patada, barra de poder, aura, los cuatro tiros de poder del Anexo 1 con sus efectos, daño, aturdimiento, goles, muerte súbita, pausa, terminar en cualquier momento y mensaje con el ganador y los puntos.
- 36 pruebas de unidad del dominio, todas pasando.


**3. ¿Cuál consideran fue el mayor logro? ¿Por qué?**
Separar las reglas del juego (dominio) de la visualización. Todo el comportamiento se puede probar sin abrir la ventana porque el dominio avanza con `step(dt)`. Los puntos de extensión (`Character`, `Effect`, `MatchType` y el catálogo `Characters`) permiten agregar personajes, tiros de poder y tipos de partido sin tocar la clase `DOPOSoccer`.

**4. ¿Cuál consideran que fue el mayor problema técnico? ¿Qué hicieron para resolverlo?**
La física del balón (gravedad, rebotes en el suelo, los bordes y el travesaño) y decidir cuándo un tiro de poder "toca" al rival. Lo resolvimos con medidas lógicas fijas de la cancha (`Field`), que la presentación escala a la resolución elegida, y con pruebas de unidad para cada regla: gol, travesaño, golpe del tiro, gol de poder sin daño.

**5. ¿Qué hicieron bien como equipo? ¿Qué se comprometen a hacer para mejorar los resultados?**
- Bien: ___
- Para mejorar: ___

**6. ¿Qué referencias usaron? ¿Cuál fue la más útil? Incluyan citas con estándares adecuados.**
- Enunciado DOPOSoccer 2026-2, Escuela Colombiana de Ingeniería.
- Oracle. *Java SE 21 API Documentation* (Swing, java.awt).
- JUnit Team. *JUnit 5 User Guide*.
- ___
