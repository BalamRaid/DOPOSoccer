package doposoccer.presentation;

import doposoccer.domain.character.CharacterCatalog;
import doposoccer.domain.character.CharacterDefinition;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.GlyphVector;

/**
 * Menu principal de DOPOSoccer. Presenta la identidad arcade, el acceso al
 * juego y la galeria de personajes disponibles.
 */
@SuppressWarnings("serial")
class MainMenuPanel extends AtmospherePanel {

    private final DOPOSoccerGUI gui;

    MainMenuPanel(DOPOSoccerGUI gui) {
        super(new BorderLayout(), 0.0);
        this.gui = gui;
        setPreferredSize(new Dimension(1180, 760));
        setBorder(BorderFactory.createEmptyBorder(18, 34, 18, 34));
        add(buildTopBar(), BorderLayout.NORTH);
        add(buildCenter(), BorderLayout.CENTER);
        add(buildRoster(), BorderLayout.SOUTH);
    }

    private JComponent buildTopBar() {
        JPanel bar = new JPanel(new BorderLayout());
        bar.setOpaque(false);
        bar.setPreferredSize(new Dimension(10, 8));
        return bar;
    }

    private JComponent buildCenter() {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
        content.add(new LogoPanel());
        content.add(Box.createVerticalStrut(4));

        content.add(Box.createVerticalStrut(26));

        ArcadeButton play = new ArcadeButton("JUGAR", ArcadeButton.Style.PRIMARY);
        play.setCompactSize(340, 58);
        play.setAlignmentX(CENTER_ALIGNMENT);
        play.addActionListener(e -> gui.showSetup());
        content.add(play);
        content.add(Box.createVerticalStrut(13));

        ArcadeButton open = new ArcadeButton("ABRIR PARTIDA",
                ArcadeButton.Style.SECONDARY);
        open.setCompactSize(340, 50);
        open.setAlignmentX(CENTER_ALIGNMENT);
        open.addActionListener(e -> gui.openGame());
        content.add(open);
        content.add(Box.createVerticalStrut(9));

        ArcadeButton howTo = new ArcadeButton("COMO JUGAR",
                ArcadeButton.Style.SECONDARY);
        howTo.setCompactSize(340, 50);
        howTo.setAlignmentX(CENTER_ALIGNMENT);
        howTo.addActionListener(e -> gui.showHowToPlay());
        content.add(howTo);
        content.add(Box.createVerticalStrut(9));

        ArcadeButton exit = new ArcadeButton("SALIR", ArcadeButton.Style.GHOST);
        exit.setCompactSize(340, 38);
        exit.setAlignmentX(CENTER_ALIGNMENT);
        exit.addActionListener(e -> gui.exitApplication());
        content.add(exit);
        content.add(Box.createVerticalStrut(14));

        JPanel resolutionRow = new JPanel(new BorderLayout(10, 0));
        resolutionRow.setOpaque(false);
        JLabel resolutionLabel = new JLabel("Resolucion");
        resolutionLabel.setFont(Theme.font(12, Font.BOLD));
        resolutionLabel.setForeground(Theme.MUTED);
        ArcadeComboBox<Resolution> resolution = new ArcadeComboBox<>(Resolution.values());
        resolution.setSelectedItem(gui.getResolution());
        resolution.addActionListener(e -> gui.setResolution((Resolution) resolution.getSelectedItem()));
        resolutionRow.add(resolutionLabel, BorderLayout.WEST);
        resolutionRow.add(resolution, BorderLayout.CENTER);
        resolutionRow.setMaximumSize(new Dimension(340, 40));
        resolutionRow.setPreferredSize(new Dimension(340, 40));
        resolutionRow.setAlignmentX(CENTER_ALIGNMENT);
        content.add(resolutionRow);

        center.add(content);
        return center;
    }

    private JComponent buildRoster() {
        JPanel wrapper = new JPanel(new BorderLayout(0, 10));
        wrapper.setOpaque(false);


        PanelSurface surface = new PanelSurface(new BorderLayout());
        surface.setAccent(Theme.PURPLE);
        surface.setBorder(BorderFactory.createEmptyBorder(12, 16, 12, 16));

        JPanel roster = new JPanel(new GridLayout(1, 6, 12, 0));
        roster.setOpaque(false);
        CharacterCatalog catalog = CharacterCatalog.defaultCatalog();
        for (CharacterDefinition character : catalog.all()) {
            roster.add(characterChip(character));
        }
        surface.add(roster, BorderLayout.CENTER);
        wrapper.add(surface, BorderLayout.CENTER);
        return wrapper;
    }

    private JComponent characterChip(CharacterDefinition character) {
        JPanel chip = new JPanel();
        chip.setOpaque(false);
        chip.setLayout(new BoxLayout(chip, BoxLayout.Y_AXIS));

        CharacterPortrait portrait = new CharacterPortrait(character);
        portrait.setRingColor(Look.aura(character));
        portrait.setPreferredSize(new Dimension(76, 76));
        portrait.setMaximumSize(new Dimension(76, 76));
        portrait.setMinimumSize(new Dimension(76, 76));
        portrait.setAlignmentX(CENTER_ALIGNMENT);
        chip.add(portrait);

        JLabel name = new JLabel(character.displayName(), SwingConstants.CENTER);
        name.setAlignmentX(CENTER_ALIGNMENT);
        name.setFont(Theme.font(11, Font.BOLD));
        name.setForeground(Theme.TEXT);
        chip.add(Box.createVerticalStrut(5));
        chip.add(name);

        JLabel power = new JLabel(character.specialAbility().name(),
                SwingConstants.CENTER);
        power.setAlignmentX(CENTER_ALIGNMENT);
        power.setFont(Theme.font(9, Font.PLAIN));
        power.setForeground(Theme.alpha(Look.aura(character), 220));
        chip.add(Box.createVerticalStrut(1));
        chip.add(power);
        return chip;
    }

    /**
     * Logotipo dibujado con Java2D. No depende de imagenes ni fuentes
     * externas; usa la misma familia que el resto de la interfaz.
     */
    private static final class LogoPanel extends JComponent {

        LogoPanel() {
            setOpaque(false);
            setPreferredSize(new Dimension(640, 208));
            setMaximumSize(new Dimension(640, 208));
            setMinimumSize(new Dimension(400, 150));
            setAlignmentX(CENTER_ALIGNMENT);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();

            Theme.drawSoccerBall(g, width / 2.0, 30, 24, true);

            Font titleFont = Theme.display(78);
            Font subFont = Theme.display(46);
            drawGradientText(g, "DOPO", width / 2.0, 132, titleFont,
                    Theme.TEXT, Theme.TEXT);
            drawGradientText(g, "SOCCER", width / 2.0, 183, subFont,
                    Theme.RED, Theme.RED);
            g.dispose();
        }

        private void drawGradientText(Graphics2D g, String text, double centerX,
                float baseline, Font font, Color from, Color to) {
            FontMetrics metrics = g.getFontMetrics(font);
            float x = (float) (centerX - metrics.stringWidth(text) / 2.0);
            GlyphVector glyphs = font.createGlyphVector(g.getFontRenderContext(),
                    text);
            Shape outline = glyphs.getOutline(x, baseline);

            g.setPaint(new GradientPaint(0, baseline - metrics.getAscent(), from,
                    0, baseline + metrics.getDescent(), to));
            g.fill(outline);
            g.setColor(new Color(0, 0, 0, 70));
            g.setStroke(new java.awt.BasicStroke(1.2f));
            g.draw(outline);
        }
    }
}
