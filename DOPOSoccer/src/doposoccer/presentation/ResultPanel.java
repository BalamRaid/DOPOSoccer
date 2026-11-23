package doposoccer.presentation;

import doposoccer.application.MatchConfig;
import doposoccer.application.MatchResult;
import doposoccer.domain.Side;
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
import java.awt.GridBagLayout;

/**
 * Pantalla de resultado con marcador, retratos, puntos y acciones para
 * revancha, cambio de personajes o vuelta al menu.
 */
@SuppressWarnings("serial")
class ResultPanel extends AtmospherePanel {

    private final MatchResult result;
    private final MatchConfig config;

    ResultPanel(String title, String subtitle, String extraLine, MatchResult result,
            MatchConfig config, String primaryLabel, Runnable primary,
            Runnable secondary, Runnable menu) {
        super(new BorderLayout(0, 22), 1.3);
        this.result = result;
        this.config = config;
        setPreferredSize(new Dimension(1180, 760));
        setBorder(BorderFactory.createEmptyBorder(30, 46, 26, 46));
        add(buildHeader(title, subtitle), BorderLayout.NORTH);
        add(buildBoard(extraLine), BorderLayout.CENTER);
        add(buildActions(primaryLabel, primary, secondary, menu),
                BorderLayout.SOUTH);
    }

    private JComponent buildHeader(String title, String subtitle) {
        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));

        JLabel kicker = new JLabel("DOPO // RESULTADO OFICIAL");
        kicker.setAlignmentX(CENTER_ALIGNMENT);
        kicker.setFont(Theme.font(11, Font.BOLD));
        kicker.setForeground(Theme.alpha(Theme.GOLD, 220));
        header.add(kicker);
        header.add(Box.createVerticalStrut(6));

        JLabel titleLabel = new JLabel(title.toUpperCase(java.util.Locale.ROOT));
        titleLabel.setAlignmentX(CENTER_ALIGNMENT);
        titleLabel.setFont(Theme.display(34));
        titleLabel.setForeground(Theme.TEXT);
        header.add(titleLabel);
        header.add(Box.createVerticalStrut(5));

        JLabel subtitleLabel = new JLabel(subtitle, SwingConstants.CENTER);
        subtitleLabel.setAlignmentX(CENTER_ALIGNMENT);
        subtitleLabel.setFont(Theme.font(14, Font.PLAIN));
        subtitleLabel.setForeground(Theme.MUTED);
        header.add(subtitleLabel);
        return header;
    }

    private JComponent buildBoard(String extraLine) {
        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        PanelSurface board = new PanelSurface(new BorderLayout(26, 0));
        board.setAccent(result.isDraw() ? Theme.GOLD : Theme.CRIMSON);
        board.setStrong(true);
        board.setBorder(BorderFactory.createEmptyBorder(28, 30, 28, 30));
        board.setPreferredSize(new Dimension(880, 340));

        board.add(playerColumn(Side.LEFT), BorderLayout.WEST);
        board.add(playerColumn(Side.RIGHT), BorderLayout.EAST);
        board.add(scoreColumn(extraLine), BorderLayout.CENTER);

        center.add(board);
        return center;
    }

    private JComponent playerColumn(Side side) {
        boolean left = side == Side.LEFT;
        CharacterDefinition character = config.characterOf(side);
        Color accent = left ? Theme.RED : Theme.VIOLET;

        JPanel column = new JPanel();
        column.setOpaque(false);
        column.setLayout(new BoxLayout(column, BoxLayout.Y_AXIS));
        column.setPreferredSize(new Dimension(230, 320));

        boolean winner = result.winner() == side;
        CharacterPortrait portrait = new CharacterPortrait(character);
        portrait.setRingColor(winner ? Theme.GOLD : accent);
        portrait.setPreferredSize(new Dimension(112, 112));
        portrait.setMaximumSize(new Dimension(112, 112));
        portrait.setMinimumSize(new Dimension(112, 112));
        portrait.setAlignmentX(CENTER_ALIGNMENT);
        column.add(portrait);
        column.add(Box.createVerticalStrut(10));

        JLabel role = new JLabel(winner ? "GANADOR" : (left ? "LADO IZQUIERDO"
                : "LADO DERECHO"));
        role.setAlignmentX(CENTER_ALIGNMENT);
        role.setFont(Theme.font(9, Font.BOLD));
        role.setForeground(winner ? Theme.GOLD : Theme.DIM);
        column.add(role);
        column.add(Box.createVerticalStrut(3));

        JLabel name = new JLabel(config.profileOf(side).getName(),
                SwingConstants.CENTER);
        name.setAlignmentX(CENTER_ALIGNMENT);
        name.setFont(Theme.font(17, Font.BOLD));
        name.setForeground(Theme.TEXT);
        column.add(name);

        JLabel country = new JLabel(character.displayName(), SwingConstants.CENTER);
        country.setAlignmentX(CENTER_ALIGNMENT);
        country.setFont(Theme.font(12, Font.PLAIN));
        country.setForeground(Theme.alpha(accent, 230));
        column.add(Box.createVerticalStrut(2));
        column.add(country);
        column.add(Box.createVerticalStrut(12));

        int points = left ? result.leftPoints() : result.rightPoints();
        JLabel pointsLabel = new JLabel("+" + points + " PUNTOS");
        pointsLabel.setAlignmentX(CENTER_ALIGNMENT);
        pointsLabel.setFont(Theme.font(11, Font.BOLD));
        pointsLabel.setForeground(Theme.GOLD);
        column.add(pointsLabel);

        if (result.survival()) {
            int balls = left ? result.leftBalls() : result.rightBalls();
            JLabel ballsLabel = new JLabel("BALONES " + balls);
            ballsLabel.setAlignmentX(CENTER_ALIGNMENT);
            ballsLabel.setFont(Theme.font(10, Font.BOLD));
            ballsLabel.setForeground(Theme.MUTED);
            column.add(Box.createVerticalStrut(5));
            column.add(ballsLabel);
        }
        column.add(Box.createVerticalGlue());
        return column;
    }

    private JComponent scoreColumn(String extraLine) {
        JPanel center = new JPanel();
        center.setOpaque(false);
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JPanel score = new JPanel();
        score.setOpaque(false);
        score.setLayout(new BoxLayout(score, BoxLayout.X_AXIS));
        score.setAlignmentX(CENTER_ALIGNMENT);
        JLabel left = new JLabel(String.valueOf(result.leftGoals()));
        left.setFont(Theme.display(76));
        left.setForeground(Theme.RED);
        JLabel dash = new JLabel("  -  ");
        dash.setFont(Theme.display(50));
        dash.setForeground(Theme.MUTED);
        JLabel right = new JLabel(String.valueOf(result.rightGoals()));
        right.setFont(Theme.display(76));
        right.setForeground(Theme.VIOLET);
        score.add(left);
        score.add(dash);
        score.add(right);
        center.add(score);

        if (result.survival()) {
            JLabel ballsCaption = new JLabel("BALONES");
            ballsCaption.setAlignmentX(CENTER_ALIGNMENT);
            ballsCaption.setFont(Theme.font(10, Font.BOLD));
            ballsCaption.setForeground(Theme.DIM);
            center.add(ballsCaption);
            JLabel balls = new JLabel(result.leftBalls() + "  -  "
                    + result.rightBalls());
            balls.setAlignmentX(CENTER_ALIGNMENT);
            balls.setFont(Theme.display(44));
            balls.setForeground(Theme.TEXT);
            center.add(balls);
        }

        center.add(Box.createVerticalStrut(14));
        JLabel description = new JLabel("<html><body style='width:240px;"
                + "text-align:center'>" + result.description() + "</body></html>",
                SwingConstants.CENTER);
        description.setAlignmentX(CENTER_ALIGNMENT);
        description.setFont(Theme.font(11, Font.PLAIN));
        description.setForeground(Theme.MUTED);
        center.add(description);

        if (extraLine != null && !extraLine.isBlank()) {
            center.add(Box.createVerticalStrut(12));
            JLabel extra = new JLabel(extraLine, SwingConstants.CENTER);
            extra.setAlignmentX(CENTER_ALIGNMENT);
            extra.setFont(Theme.font(11, Font.BOLD));
            extra.setForeground(Theme.GOLD);
            center.add(extra);
        }
        return center;
    }

    private JComponent buildActions(String primaryLabel, Runnable primary,
            Runnable secondary, Runnable menu) {
        JPanel footer = new JPanel(new BorderLayout(20, 0));
        footer.setOpaque(false);

        JLabel hint = new JLabel("Los puntos ya fueron sumados a tu perfil.");
        hint.setFont(Theme.font(11, Font.PLAIN));
        hint.setForeground(Theme.DIM);
        footer.add(hint, BorderLayout.WEST);

        JPanel actions = new JPanel();
        actions.setOpaque(false);
        actions.setLayout(new BoxLayout(actions, BoxLayout.X_AXIS));

        ArcadeButton menuButton = new ArcadeButton("MENU", ArcadeButton.Style.GHOST);
        menuButton.setCompactSize(150, 44);
        menuButton.addActionListener(e -> menu.run());
        actions.add(menuButton);
        actions.add(Box.createHorizontalStrut(10));

        ArcadeButton secondaryButton = new ArcadeButton("CAMBIAR PERSONAJES",
                ArcadeButton.Style.SECONDARY);
        secondaryButton.setCompactSize(240, 48);
        secondaryButton.addActionListener(e -> secondary.run());
        actions.add(secondaryButton);
        actions.add(Box.createHorizontalStrut(12));

        if (primaryLabel != null && primary != null) {
            ArcadeButton primaryButton = new ArcadeButton(primaryLabel,
                    ArcadeButton.Style.PRIMARY);
            primaryButton.setCompactSize(250, 50);
            primaryButton.addActionListener(e -> primary.run());
            actions.add(primaryButton);
        }
        footer.add(actions, BorderLayout.EAST);
        return footer;
    }
}
