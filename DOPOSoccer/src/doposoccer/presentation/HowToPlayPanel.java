package doposoccer.presentation;

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
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;

/**
 * Pantalla de instrucciones: reglas oficiales, controles de ambos jugadores y
 * resumen de los poderes del plantel.
 */
@SuppressWarnings("serial")
class HowToPlayPanel extends AtmospherePanel {

    private final DOPOSoccerGUI gui;

    HowToPlayPanel(DOPOSoccerGUI gui) {
        super(new BorderLayout(0, 20), 2.7);
        this.gui = gui;
        setPreferredSize(new Dimension(1180, 760));
        setBorder(BorderFactory.createEmptyBorder(22, 34, 22, 34));
        add(buildHeader(), BorderLayout.NORTH);
        add(buildContent(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(16, 0));
        header.setOpaque(false);

        ArcadeButton back = new ArcadeButton("<  MENU", ArcadeButton.Style.GHOST);
        back.setCompactSize(120, 40);
        back.addActionListener(e -> gui.showMainMenu());
        header.add(back, BorderLayout.WEST);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel kicker = new JLabel("MANUAL DE LA CANCHA");
        kicker.setFont(Theme.font(11, Font.BOLD));
        kicker.setForeground(Theme.alpha(Theme.GOLD, 220));
        JLabel title = new JLabel("COMO JUGAR");
        title.setFont(Theme.display(32));
        title.setForeground(Theme.TEXT);
        JLabel subtitle = new JLabel("Reglas oficiales, controles y poderes.");
        subtitle.setFont(Theme.font(12, Font.PLAIN));
        subtitle.setForeground(Theme.MUTED);
        titles.add(kicker);
        titles.add(Box.createVerticalStrut(3));
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);
        header.add(titles, BorderLayout.CENTER);

        JLabel badge = new JLabel("60 s  ·  3 / 5 / 7 GOLES  ·  SUPERVIVENCIA");
        badge.setFont(Theme.font(11, Font.BOLD));
        badge.setForeground(Theme.DIM);
        badge.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(badge, BorderLayout.EAST);
        return header;
    }

    private JComponent buildContent() {
        JPanel content = new JPanel(new GridLayout(1, 2, 20, 0));
        content.setOpaque(false);
        content.add(buildRules());
        content.add(buildControls());
        return content;
    }

    private JComponent buildRules() {
        PanelSurface card = new PanelSurface(null);
        card.setAccent(Theme.CRIMSON);
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(section("REGLAS OFICIALES", Theme.CRIMSON));
        card.add(Box.createVerticalStrut(14));
        card.add(rule("Por tiempo", "Un minuto de juego. Si hay empate, se juega muerte subita: el primero que anote gana."));
        card.add(Box.createVerticalStrut(11));
        card.add(rule("Por goles", "Gana el primero en llegar a 3, 5 o 7 goles. Sin limite de tiempo."));
        card.add(Box.createVerticalStrut(11));
        card.add(rule("Supervivencia", "Solo en Jugador vs Maquina. Cada gol rival te quita un balon; pierde quien se quede sin balones."));
        card.add(Box.createVerticalStrut(11));
        card.add(rule("Vida y aturdimiento", "Al quedarte sin vida quedas aturdido 3 segundos y luego recuperas la vida completa."));
        card.add(Box.createVerticalStrut(11));
        card.add(rule("Poderes", "Cada pais posee un tiro especial. La barra de poder se llena sola y tambien recibe un impulso cuando te anotan."));
        card.add(Box.createVerticalGlue());

        JLabel note = new JLabel("<html><body style='width:430px'>"
                + "Corea del Sur empuja, Camerun electrocuta, Nigeria noquea, "
                + "Estados Unidos confunde, Canada congela y China reduce a su rival."
                + "</body></html>");
        note.setFont(Theme.font(11, Font.PLAIN));
        note.setForeground(Theme.DIM);
        card.add(note);
        return card;
    }

    private JComponent buildControls() {
        PanelSurface card = new PanelSurface(null);
        card.setAccent(Theme.PURPLE);
        card.setBorder(BorderFactory.createEmptyBorder(22, 24, 22, 24));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        card.add(section("CONTROLES", Theme.PURPLE));
        card.add(Box.createVerticalStrut(14));
        card.add(playerControls("JUGADOR 1", "Izquierda", Theme.RED,
                new String[]{"A", "D"}, "Mover",
                new String[]{"W"}, "Saltar",
                new String[]{"S"}, "Patear",
                new String[]{"Q"}, "Poder"));
        card.add(Box.createVerticalStrut(18));
        card.add(playerControls("JUGADOR 2", "Derecha", Theme.VIOLET,
                new String[]{"<", ">"}, "Mover",
                new String[]{"^"}, "Saltar",
                new String[]{"v"}, "Patear",
                new String[]{"CTRL", "DER"}, "Poder"));
        card.add(Box.createVerticalStrut(18));

        card.add(section("DURANTE EL PARTIDO", Theme.GOLD));
        card.add(Box.createVerticalStrut(9));
        card.add(keyLine(new String[]{"P"}, "Pausar o continuar"));
        card.add(Box.createVerticalStrut(7));
        card.add(keyLine(new String[]{"ESC"}, "Terminar el partido"));
        card.add(Box.createVerticalGlue());

        JLabel tip = new JLabel("<html><body style='width:430px'>"
                + "El balon se controla con el cuerpo y el pie. Salta para "
                + "rematar de cabeza y patea cerca del balon para disparar."
                + "</body></html>");
        tip.setFont(Theme.font(11, Font.PLAIN));
        tip.setForeground(Theme.DIM);
        card.add(tip);
        return card;
    }

    private JComponent playerControls(String title, String side, Color accent,
            String[] moveKeys, String move,
            String[] jumpKeys, String jump,
            String[] kickKeys, String kick,
            String[] powerKeys, String power) {
        JPanel block = new JPanel();
        block.setOpaque(false);
        block.setLayout(new BoxLayout(block, BoxLayout.Y_AXIS));

        JPanel heading = new JPanel(new BorderLayout());
        heading.setOpaque(false);
        JLabel name = new JLabel(title);
        name.setFont(Theme.font(13, Font.BOLD));
        name.setForeground(accent);
        JLabel role = new JLabel(side.toUpperCase(java.util.Locale.ROOT));
        role.setFont(Theme.font(9, Font.BOLD));
        role.setForeground(Theme.DIM);
        role.setHorizontalAlignment(SwingConstants.RIGHT);
        heading.add(name, BorderLayout.WEST);
        heading.add(role, BorderLayout.EAST);
        heading.setMaximumSize(new Dimension(Integer.MAX_VALUE, 20));
        block.add(heading);
        block.add(Box.createVerticalStrut(8));
        block.add(keyLine(moveKeys, move));
        block.add(Box.createVerticalStrut(6));
        block.add(keyLine(jumpKeys, jump));
        block.add(Box.createVerticalStrut(6));
        block.add(keyLine(kickKeys, kick));
        block.add(Box.createVerticalStrut(6));
        block.add(keyLine(powerKeys, power));
        block.setMaximumSize(new Dimension(Integer.MAX_VALUE, 130));
        return block;
    }

    private JComponent keyLine(String[] keys, String action) {
        JPanel line = new JPanel(new BorderLayout(10, 0));
        line.setOpaque(false);
        line.setMaximumSize(new Dimension(Integer.MAX_VALUE, 26));

        JPanel caps = new JPanel();
        caps.setOpaque(false);
        caps.setLayout(new BoxLayout(caps, BoxLayout.X_AXIS));
        for (String key : keys) {
            KeyCap cap = new KeyCap(key);
            caps.add(cap);
            caps.add(Box.createHorizontalStrut(4));
        }
        line.add(caps, BorderLayout.WEST);

        JLabel label = new JLabel(action);
        label.setFont(Theme.font(12, Font.PLAIN));
        label.setForeground(Theme.MUTED);
        line.add(label, BorderLayout.CENTER);
        return line;
    }

    private JComponent rule(String title, String detail) {
        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        JLabel marker = new JLabel("+");
        marker.setFont(Theme.display(16));
        marker.setForeground(Theme.RED);
        marker.setVerticalAlignment(SwingConstants.TOP);
        row.add(marker, BorderLayout.WEST);
        JLabel text = new JLabel("<html><body style='width:390px'><b>"
                + title + "</b><br><span style='color:#b8aaca'>" + detail
                + "</span></body></html>");
        text.setFont(Theme.font(12, Font.PLAIN));
        text.setForeground(Theme.TEXT);
        row.add(text, BorderLayout.CENTER);
        return row;
    }

    private JLabel section(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(12, Font.BOLD));
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(18, 0));
        footer.setOpaque(false);
        JLabel tip = new JLabel("Puedes cambiar de personaje y de estadio antes de cada partido.");
        tip.setFont(Theme.font(11, Font.PLAIN));
        tip.setForeground(Theme.DIM);
        footer.add(tip, BorderLayout.WEST);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));
        ArcadeButton back = new ArcadeButton("VOLVER", ArcadeButton.Style.SECONDARY);
        back.setCompactSize(160, 44);
        back.addActionListener(e -> gui.showMainMenu());
        ArcadeButton play = new ArcadeButton("A JUGAR", ArcadeButton.Style.PRIMARY);
        play.setCompactSize(220, 48);
        play.addActionListener(e -> gui.showSetup());
        actions.add(back);
        actions.add(Box.createHorizontalStrut(10));
        actions.add(play);
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }

    /** Tecla dibujada como una pequena capsula. */
    private static final class KeyCap extends JComponent {

        private final String text;

        KeyCap(String text) {
            this.text = text;
            setOpaque(false);
            int width = Math.max(28, 12 + text.length() * 8);
            setPreferredSize(new Dimension(width, 22));
            setMaximumSize(new Dimension(width, 22));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            RoundRectangle2D shape = new RoundRectangle2D.Double(0.5, 0.5,
                    getWidth() - 1, getHeight() - 1, 7, 7);
            g.setColor(Theme.alpha(Theme.PANEL_LIGHT, 245));
            g.fill(shape);
            g.setColor(Theme.alpha(Theme.TEXT, 70));
            g.draw(shape);
            g.setFont(Theme.font(9, Font.BOLD));
            g.setColor(Theme.TEXT);
            java.awt.FontMetrics metrics = g.getFontMetrics();
            g.drawString(text, (getWidth() - metrics.stringWidth(text)) / 2,
                    (getHeight() - metrics.getHeight()) / 2 + metrics.getAscent());
            g.dispose();
        }
    }
}
