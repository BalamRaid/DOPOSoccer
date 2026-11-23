# Arquitectura de DOPOSoccer

## Capas

| Capa | Paquete | Responsabilidad |
|---|---|---|
| Dominio | `doposoccer.domain` | Reglas y estado del partido, cancha, jugadores, balon, goles, efectos y tipos de partido. |
| Aplicacion | `doposoccer.application` | Configuracion, sesion de juego, resultados y series de supervivencia. |
| Infraestructura | `doposoccer.infrastructure` | Servicios tecnicos, actualmente el registro de errores en archivo. |
| Presentacion | `doposoccer.presentation` | Ventana Swing, paneles, teclado, dibujo Java2D y adaptadores de entrada. |
| Pruebas | `doposoccer.test` | Pruebas JUnit y utilidades compartidas. |

Las dependencias apuntan hacia el dominio. La capa de presentacion tambien
puede consultar la aplicacion, pero el dominio no importa Swing, archivos ni
detalles de la interfaz.

## Decisiones de diseno

### Personajes por composicion

No existe una subclase por pais. Un personaje es una
`CharacterDefinition` formada por:

- un identificador y un nombre visible;
- una clave visual para el renderizador;
- `CharacterStats` con velocidad, salto y fuerza;
- una `SpecialAbility` con dano, nombre del efecto y una estrategia de tiro.

Agregar un personaje nuevo consiste en registrar otra definicion en
`CharacterCatalog`. No hay que modificar `Look`, `Player`, `DOPOSoccer` ni la
pantalla de configuracion.

### Poderes reutilizables

La trayectoria del balon se delega en `PowerShot`:

- `StraightShot`: tiro recto sin gravedad;
- `TornadoShot`: sale desde el suelo y activa el modo tornado;
- `IllusionShot`: genera balones falsos.

El efecto del impacto se crea con una fabrica (`Supplier<Effect>`) y se
reutiliza entre partidos. Los efectos viven en `doposoccer.domain.effect` y
pueden combinarse con cualquier personaje.

### Herencia limitada

Solo se conserva herencia donde el comportamiento realmente cambia:

- `MatchType` define el contrato de finalizacion y lo implementan
  `GoalMatch`, `TimeMatch` y `SurvivalMatch`.
- `Effect` define el contrato temporal y lo implementan los efectos concretos.
- `PowerShot` define la trayectoria y lo implementan los tiros concretos.

El jugador, el balon y el partido son clases finales con colaboradores.

### Control por intenciones

`PlayerController` produce un `PlayerIntent` con direccion, salto, patada y
poder. `HumanController` traduce el teclado y `AiController` lee una
`MatchView` de solo lectura. El partido no sabe si el lado es humano o maquina:
solo aplica intenciones.

### Presentacion con paso fijo

`GamePanel` usa un `Timer` de Swing como pulso visual, pero acumula tiempo y
avanza la simulacion en pasos fijos de `1/60` segundos. Esto mantiene la fisica
determinista y desacoplada de la velocidad de la ventana.

## Flujo de una partida

1. `SetupPanel` crea un `MatchConfig` con modalidad, perfiles, personajes y
   tipo de partido.
2. `GameSession.create` valida la combinacion y crea el agregado `DOPOSoccer`.
3. `GameSession` instala `AiController` en los lados de maquina.
4. `GamePanel` instala `HumanController` en los lados humanos.
5. En cada paso, `DOPOSoccer.step` actualiza reloj, controladores, jugadores,
   patadas, balon, contactos, tiros de poder, goles y fin del partido.
6. Al terminar, `GameSession.result` construye un `MatchResult` inmutable.
7. En supervivencia, `SurvivalSeries` registra el resultado y decide si hay
   otra ronda.

## Reglas implementadas

- Partidos oficiales por goles: 3, 5 o 7.
- Partido por tiempo: 60 segundos y gol de oro en caso de empate.
- Supervivencia: solo Jugador vs Maquina; el humano ocupa la izquierda.
- Gol valido solo con el balon completamente dentro del arco.
- P1: `A`/`D`, `W`, `S`, `Q`. P2: flechas y `Ctrl` derecho.
- Barra de poder: 10 segundos; 25 % adicional al conceder un gol.
- Aturdimiento: 3 segundos, tanto por impacto de poder como al llegar a vida
  cero.
- Los efectos no son objetos de la cancha: pertenecen al estado del jugador.

## Pruebas

La suite se organiza por responsabilidad:

- `BallTest`, `ArenaGoalTest` y `FieldTest`: fisica y geometria.
- `CharacterCatalogTest`, `CharacterStatsTest` y `SpecialAbilityTest`:
  composicion de personajes y poderes.
- `EffectTest`, `PowerShotStateTest`, `PlayerTest` y `StatsProfileTest`: estado
  del jugador, tiros de poder y progresion.
- `AiControllerTest`, `HumanControllerTest` y `KeyboardStateTest`: entrada y
  control.
- `MatchTypeTest`, `GameSessionTest`, `ApplicationModelTest` y
  `SurvivalSeriesTest`: reglas y aplicacion.
- `DOPOSoccerTest` e `IntegrationTest`: integracion de las tres modalidades y
  escenarios completos.

Para ejecutar todo:

```bash
./test.sh
```
