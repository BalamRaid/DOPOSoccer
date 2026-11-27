package doposoccer.presentation;

import javax.swing.JPanel;
import javax.swing.Timer;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;

/**
 * Panel con el fondo animado de estadio nocturno que comparten el menu, la
 * configuracion y la pantalla de instrucciones.
 *
 * <p>La animacion es puramente decorativa y se detiene cuando el panel sale
 * de la jerarquia visible para no consumir recursos durante un partido.</p>
 */
@SuppressWarnings("serial")
class AtmospherePanel extends JPanel {

    private final double phaseOffset;
    private final Timer timer;
    private long startedAt = System.nanoTime();

    AtmospherePanel(LayoutManager layout, double phaseOffset) {
        super(layout);
        this.phaseOffset = phaseOffset;
        setOpaque(true);
        setBackground(Theme.INK);
        timer = new Timer(40, e -> repaint());
    }

    @Override
    public void addNotify() {
        super.addNotify();
        startedAt = System.nanoTime();
        if (!timer.isRunning()) {
            timer.start();
        }
    }

    @Override
    public void removeNotify() {
        timer.stop();
        super.removeNotify();
    }

    private double phase() {
        return phaseOffset + (System.nanoTime() - startedAt) / 1_000_000_000.0;
    }

    @Override
    protected void paintComponent(Graphics background) {
        super.paintComponent(background);
        Graphics2D g = (Graphics2D) background.create();
        Theme.paintAtmosphere(g, getWidth(), getHeight(), phase(), 41);
        g.dispose();
    }
}
