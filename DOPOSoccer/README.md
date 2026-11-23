# DOPOSoccer

Juego arcade de fútbol en Java 21 con Swing / Java2D. Incluye modalidades
Jugador vs Jugador, Jugador vs Máquina y Máquina vs Máquina; partidos por
goles, por tiempo y supervivencia; personajes con poderes y efectos.

## Requisitos

- JDK 21 (verificado con OpenJDK 21.0.13).
- JUnit 5 en `lib/junit-platform-console-standalone-1.10.2.jar` (incluido).
- No se necesita Maven ni Gradle.

## Ejecutar

Desde esta carpeta:

```bash
./run.sh          # Linux / WSL / Git Bash
run.bat           # Windows CMD / PowerShell
```

También puedes abrir `DOPOSoccerGUI.java` y ejecutar el `main`:

```text
doposoccer.presentation.DOPOSoccerGUI
```

## Pruebas

```bash
./test.sh         # compila y ejecuta la suite completa
test.bat          # Windows
```

La suite tiene **278 pruebas JUnit** organizadas por responsabilidad
(física, personajes, efectos, control, reglas, aplicación e integración).
En IntelliJ: click derecho sobre `src/doposoccer/test` →
*Run tests in 'doposoccer.test'*.

## Controles

| Acción | Jugador 1 | Jugador 2 |
|---|---|---|
| Mover | `A` / `D` | `←` / `→` |
| Saltar | `W` | `↑` |
| Patear | `S` | `↓` |
| Poder | `Q` | `Ctrl` derecho |
| Pausa | `P` | `P` |
| Terminar partido | `Esc` | `Esc` |

## Estructura

```text
DopoSoccer/
├── DOPOSoccer.asta          Modelo UML (Astah)
├── Retrospectiva.md         Retrospectiva del proyecto
├── AUDITORIA_DOPOSOCCER.txt Auditoría técnica y reparaciones
└── DOPOSoccer/
    ├── src/doposoccer/
    │   ├── domain/          Reglas del juego (núcleo, sin Swing ni IO)
    │   │   ├── character/   Personajes, tiros y poderes
    │   │   ├── control/     Intenciones y vista de solo lectura
    │   │   ├── effect/      Efectos temporales
    │   │   ├── match/       Cancha, arcos, reloj y marcador
    │   │   └── math/        Vec2
    │   ├── application/     Configuración, sesión y resultados
    │   ├── infrastructure/  Registro de errores en archivo
    │   ├── presentation/    GUI Swing, teclado y render Java2D
    │   └── test/            Pruebas JUnit y dobles de prueba
    ├── docs/                Arquitectura y diagrama de clases (PlantUML)
    ├── lib/                 JUnit standalone
    ├── run.sh / run.bat
    └── test.sh / test.bat
```

## Diseño

Las dependencias apuntan hacia `domain`. El dominio no importa Swing ni
`java.io`: recibe `PlayerIntent` y expone `MatchView`. Los personajes se
definen por composición (`CharacterDefinition` + `SpecialAbility` +
`PowerShot`), las reglas de fin de partido son una estrategia (`MatchType`)
y los efectos se crean mediante una fábrica (`Supplier<Effect>`).
La simulación avanza en pasos fijos de `1/60` s, independientes del render.

Ver `docs/ARQUITECTURA.md` y `docs/class-diagram.puml` para el detalle.

## Autores

- Integrante 1: _(completar)_
- Integrante 2: _(completar)_

Proyecto: Escuela Colombiana de Ingeniería — DOPOSoccer 2026-2.
