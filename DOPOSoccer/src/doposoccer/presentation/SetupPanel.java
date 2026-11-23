package doposoccer.presentation;

import doposoccer.application.GameMode;
import doposoccer.application.MatchConfig;
import doposoccer.domain.DOPOSoccerException;
import doposoccer.domain.GoalMatch;
import doposoccer.domain.MatchType;
import doposoccer.domain.Profile;
import doposoccer.domain.Stat;
import doposoccer.domain.Stats;
import doposoccer.domain.SurvivalMatch;
import doposoccer.domain.TimeMatch;
import doposoccer.domain.character.CharacterCatalog;
import doposoccer.domain.character.CharacterDefinition;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
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
import java.util.EnumMap;
import java.util.Map;

/**
 * Pantalla de configuracion arcade: modo de juego, tipo de partido, escenario,
 * personajes y mejoras de estadisticas.
 *
 * <p>La pantalla solo construye la configuracion de aplicacion y delega el
 * inicio en {@link DOPOSoccerGUI}. Los perfiles se conservan entre partidos,
 * igual que los puntos y las mejoras compradas.</p>
 */
@SuppressWarnings("serial")
class SetupPanel extends AtmospherePanel {

    private final DOPOSoccerGUI gui;
    private final CharacterCatalog catalog = CharacterCatalog.defaultCatalog();
    private final Profile p1;
    private final Profile p2;
    private final Profile machine1;
    private final Profile machine2;

    private final ArcadeComboBox<GameMode> mode = new ArcadeComboBox<>(GameMode.values());
    private final ArcadeComboBox<Resolution> resolution = new ArcadeComboBox<>(Resolution.values());
    private final OptionCard byTime = new OptionCard("Por tiempo",
            "60 segundos oficiales. Empate = muerte subita.");
    private final OptionCard byGoals = new OptionCard("Por goles",
            "Gana el primero en llegar a la meta.");
    private final OptionCard bySurvival = new OptionCard("Supervivencia",
            "Solo P vs M. Pierde quien se quede sin balones.");
    private final ArcadeComboBox<Integer> goals = new ArcadeComboBox<>(
            new Integer[]{3, 5, 7});
    private final ArcadeStepper survivalBalls = new ArcadeStepper(
            SurvivalMatch.DEFAULT_BALLS, SurvivalMatch.MIN_BALLS,
            SurvivalMatch.MAX_BALLS, 1);
    private final JLabel timeInfo = new JLabel("60 segundos por partido");
    private final JLabel goalsInfo = new JLabel("Meta oficial");
    private final JLabel ballsInfo = new JLabel("Balones por jugador");
    private final JPanel timeRow = new JPanel(new BorderLayout());
    private final JPanel goalsRow = new JPanel(new BorderLayout(8, 0));
    private final JPanel ballsRow = new JPanel(new BorderLayout(8, 0));
    private final StageCard stadium = new StageCard(Stage.STADIUM);
    private final StageCard campus = new StageCard(Stage.CAMPUS);
    private final StageCard sunset = new StageCard(Stage.SUNSET);
    private final JLabel stageInfo = new JLabel();
    private final PlayerSetup left;
    private final PlayerSetup right;
    private Stage selectedStage = Stage.STADIUM;

    /**
     * Crea la pantalla de configuracion.
     *
     * @param gui ventana principal que inicia el partido y muestra errores
     * @param p1 perfil del jugador 1
     * @param p2 perfil del jugador 2
     * @param machine1 perfil de la primera maquina
     * @param machine2 perfil de la segunda maquina
     */
    SetupPanel(DOPOSoccerGUI gui, Profile p1, Profile p2, Profile machine1,
            Profile machine2) {
        super(new BorderLayout(0, 16), 0.7);
        this.gui = gui;
        this.p1 = p1;
        this.p2 = p2;
        this.machine1 = machine1;
        this.machine2 = machine2;
        setPreferredSize(new Dimension(1180, 760));
        setBorder(BorderFactory.createEmptyBorder(20, 30, 18, 30));

        left = new PlayerSetup(p1, false, "JUGADOR 1 · IZQUIERDA", Theme.RED, 0);
        right = new PlayerSetup(p2, false, "JUGADOR 2 · DERECHA", Theme.VIOLET, 1);

        add(buildHeader(), BorderLayout.NORTH);
        add(buildBody(), BorderLayout.CENTER);
        add(buildFooter(), BorderLayout.SOUTH);

        mode.setSelectedItem(GameMode.PVP);
        resolution.setSelectedItem(Resolution.MEDIUM);
        byTime.setSelected(true);
        mode.addActionListener(e -> updateMode());
        updateMode();
        selectStage(Stage.STADIUM);
        updateMatchFields();
        refresh();
    }

    private JComponent buildHeader() {
        JPanel header = new JPanel(new BorderLayout(18, 0));
        header.setOpaque(false);

        ArcadeButton back = new ArcadeButton("<  MENU", ArcadeButton.Style.GHOST);
        back.setCompactSize(120, 40);
        back.addActionListener(e -> gui.showMainMenu());
        header.add(back, BorderLayout.WEST);

        JPanel titles = new JPanel();
        titles.setOpaque(false);
        titles.setLayout(new BoxLayout(titles, BoxLayout.Y_AXIS));
        JLabel kicker = new JLabel("CONFIGURACION DEL PARTIDO");
        kicker.setFont(Theme.font(11, Font.BOLD));
        kicker.setForeground(Theme.alpha(Theme.GOLD, 220));
        JLabel title = new JLabel("ARMA TU PARTIDO");
        title.setFont(Theme.display(30));
        title.setForeground(Theme.TEXT);
        JLabel subtitle = new JLabel("Elige modo, escenario y leyendas. Los puntos se conservan.");
        subtitle.setFont(Theme.font(12, Font.PLAIN));
        subtitle.setForeground(Theme.MUTED);
        titles.add(kicker);
        titles.add(Box.createVerticalStrut(2));
        titles.add(title);
        titles.add(Box.createVerticalStrut(2));
        titles.add(subtitle);
        header.add(titles, BorderLayout.CENTER);

        JLabel edition = new JLabel("DOPO // ARCADE");
        edition.setFont(Theme.font(11, Font.BOLD));
        edition.setForeground(Theme.DIM);
        edition.setHorizontalAlignment(SwingConstants.RIGHT);
        header.add(edition, BorderLayout.EAST);
        return header;
    }

    private JComponent buildBody() {
        JPanel players = new JPanel(new GridLayout(1, 2, 18, 0));
        players.setOpaque(false);
        players.add(left);
        players.add(right);

        JPanel body = new JPanel(new BorderLayout(18, 0));
        body.setOpaque(false);
        body.add(players, BorderLayout.CENTER);
        body.add(buildOptions(), BorderLayout.EAST);
        return body;
    }

    private JComponent buildOptions() {
        PanelSurface options = new PanelSurface(null);
        options.setAccent(Theme.GOLD);
        options.setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
        options.setLayout(new BoxLayout(options, BoxLayout.Y_AXIS));
        options.setPreferredSize(new Dimension(356, 560));

        options.add(section("MODO DE JUEGO", Theme.GOLD));
        options.add(Box.createVerticalStrut(6));
        addFull(options, mode);
        options.add(Box.createVerticalStrut(13));

        options.add(section("TIPO DE PARTIDO", Theme.GOLD));
        options.add(Box.createVerticalStrut(6));
        ButtonGroup types = new ButtonGroup();
        types.add(byTime);
        types.add(byGoals);
        types.add(bySurvival);
        options.add(byTime);
        options.add(Box.createVerticalStrut(6));
        options.add(byGoals);
        options.add(Box.createVerticalStrut(6));
        options.add(bySurvival);
        options.add(Box.createVerticalStrut(8));

        options.add(buildMatchDetail());
        options.add(Box.createVerticalStrut(12));

        options.add(section("ESCENARIO", Theme.GOLD));
        options.add(Box.createVerticalStrut(6));
        options.add(buildStages());
        options.add(Box.createVerticalStrut(3));
        stageInfo.setFont(Theme.font(9, Font.PLAIN));
        stageInfo.setForeground(Theme.DIM);
        stageInfo.setAlignmentX(LEFT_ALIGNMENT);
        options.add(stageInfo);
        options.add(Box.createVerticalStrut(11));

        options.add(section("RESOLUCION", Theme.GOLD));
        options.add(Box.createVerticalStrut(6));
        addFull(options, resolution);
        options.add(Box.createVerticalGlue());

        byTime.addActionListener(e -> updateMatchFields());
        byGoals.addActionListener(e -> updateMatchFields());
        bySurvival.addActionListener(e -> updateMatchFields());
        return options;
    }

    private JComponent buildMatchDetail() {
        JPanel detail = new JPanel();
        detail.setOpaque(false);
        detail.setLayout(new BoxLayout(detail, BoxLayout.Y_AXIS));
        detail.setAlignmentX(LEFT_ALIGNMENT);
        detail.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));

        timeInfo.setFont(Theme.font(10, Font.PLAIN));
        timeInfo.setForeground(Theme.DIM);
        timeRow.setOpaque(false);
        timeRow.add(timeInfo, BorderLayout.WEST);

        goalsInfo.setFont(Theme.font(10, Font.BOLD));
        goalsInfo.setForeground(Theme.MUTED);
        goalsRow.setOpaque(false);
        goalsRow.add(goalsInfo, BorderLayout.WEST);
        goalsRow.add(goals, BorderLayout.CENTER);

        ballsInfo.setFont(Theme.font(10, Font.BOLD));
        ballsInfo.setForeground(Theme.MUTED);
        ballsRow.setOpaque(false);
        ballsRow.add(ballsInfo, BorderLayout.WEST);
        ballsRow.add(survivalBalls, BorderLayout.EAST);

        for (JPanel row : new JPanel[]{timeRow, goalsRow, ballsRow}) {
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
            detail.add(row);
        }
        return detail;
    }

    private JComponent buildStages() {
        JPanel row = new JPanel(new GridLayout(1, 3, 8, 0));
        row.setOpaque(false);
        row.setAlignmentX(LEFT_ALIGNMENT);
        row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 84));
        ButtonGroup group = new ButtonGroup();
        for (StageCard card : new StageCard[]{stadium, campus, sunset}) {
            group.add(card);
            row.add(card);
            card.addActionListener(e -> selectStage(card.stage()));
        }
        return row;
    }

    private JComponent buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(18, 0));
        footer.setOpaque(false);

        JLabel hint = new JLabel("P pausa  ·  Esc termina el partido  ·  "
                + "El perfil conserva puntos y mejoras");
        hint.setFont(Theme.font(10, Font.PLAIN));
        hint.setForeground(Theme.DIM);
        footer.add(hint, BorderLayout.WEST);

        ArcadeButton play = new ArcadeButton("JUGAR", ArcadeButton.Style.PRIMARY);
        play.setCompactSize(280, 52);
        play.addActionListener(e -> startGame());
        footer.add(play, BorderLayout.EAST);
        return footer;
    }

    private void selectStage(Stage stage) {
        selectedStage = stage;
        switch (stage) {
            case STADIUM -> stadium.setSelected(true);
            case CAMPUS -> campus.setSelected(true);
            case SUNSET -> sunset.setSelected(true);
        }
        stageInfo.setText(stage.subtitle() + "  ·  "
                + (stage.hasPhoto() ? "fotografia cargada" : "arte procedural"));
    }

    private void updateMode() {
        GameMode selected = (GameMode) mode.getSelectedItem();
        boolean survivalAvailable = selected == GameMode.PVM;
        bySurvival.setEnabled(survivalAvailable);
        if (!survivalAvailable && bySurvival.isSelected()) {
            byTime.setSelected(true);
        }
        if (selected == GameMode.PVP) {
            left.configure(p1, false, "JUGADOR 1 · IZQUIERDA");
            right.configure(p2, false, "JUGADOR 2 · DERECHA");
        } else if (selected == GameMode.PVM) {
            left.configure(p1, false, "JUGADOR 1 · IZQUIERDA");
            right.configure(machine1, true, "MAQUINA · DERECHA");
        } else {
            left.configure(machine1, true, "MAQUINA 1 · IZQUIERDA");
            right.configure(machine2, true, "MAQUINA 2 · DERECHA");
        }
        updateMatchFields();
    }

    private void updateMatchFields() {
        boolean time = byTime.isSelected();
        boolean goalMode = byGoals.isSelected();
        boolean survival = bySurvival.isSelected() && bySurvival.isEnabled();
        timeRow.setVisible(time);
        goalsRow.setVisible(goalMode);
        ballsRow.setVisible(survival);
        goals.setEnabled(goalMode);
        survivalBalls.setEnabled(survival);
        revalidate();
        repaint();
    }

    /**
     * Actualiza los puntos y las estadisticas mostradas para ambos jugadores.
     */
    void refresh() {
        left.refresh();
        right.refresh();
    }

    private void startGame() {
        try {
            GameMode selectedMode = (GameMode) mode.getSelectedItem();
            MatchType type;
            if (bySurvival.isSelected() && bySurvival.isEnabled()) {
                type = new SurvivalMatch((Integer) survivalBalls.getValue());
            } else if (byGoals.isSelected()) {
                type = GoalMatch.standard((Integer) goals.getSelectedItem());
            } else if (byTime.isSelected()) {
                type = TimeMatch.standard();
            } else {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_MODE);
            }
            if (!selectedMode.supports(type)) {
                throw new DOPOSoccerException(DOPOSoccerException.INVALID_MODE);
            }
            MatchConfig config = new MatchConfig(
                    selectedMode,
                    type,
                    left.getProfile(),
                    left.getCharacter(),
                    right.getProfile(),
                    right.getCharacter());
            gui.startGame(config, (Resolution) resolution.getSelectedItem(),
                    selectedStage);
        } catch (DOPOSoccerException e) {
            gui.showError(e);
        }
    }

    private JLabel section(String text, Color color) {
        JLabel label = new JLabel(text);
        label.setFont(Theme.font(10, Font.BOLD));
        label.setForeground(color);
        label.setAlignmentX(LEFT_ALIGNMENT);
        return label;
    }

    private void addFull(JPanel parent, JComponent component) {
        component.setAlignmentX(LEFT_ALIGNMENT);
        if (component.getMaximumSize().height > 44) {
            component.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        }
        parent.add(component);
    }

    /**
     * Panel de cada jugador: retrato, personaje, poder y mejoras.
     */
    @SuppressWarnings("serial")
    private final class PlayerSetup extends PanelSurface {

        private Profile profile;
        private boolean machine;
        private String roleText;
        private final Color accent;
        private final JLabel role = new JLabel();
        private final JLabel name = new JLabel();
        private final JLabel points = new JLabel();
        private final JLabel power = new JLabel();
        private final CharacterPortrait portrait = new CharacterPortrait(null);
        private final ArcadeComboBox<CharacterDefinition> character;
        private final Map<Stat, JLabel> statLabels = new EnumMap<>(Stat.class);
        private final Map<Stat, StatBar> statBars = new EnumMap<>(Stat.class);
        private final Map<Stat, ArcadeButton> upgradeButtons = new EnumMap<>(Stat.class);

        PlayerSetup(Profile profile, boolean machine, String roleText, Color accent,
                int defaultCharacter) {
            super(null);
            this.profile = profile;
            this.machine = machine;
            this.roleText = roleText;
            this.accent = accent;
            setAccent(accent);
            setBorder(BorderFactory.createEmptyBorder(18, 20, 18, 20));
            setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));

            character = new ArcadeComboBox<>(
                    catalog.all().toArray(new CharacterDefinition[0]));
            if (defaultCharacter >= 0 && defaultCharacter < character.getItemCount()) {
                character.setSelectedIndex(defaultCharacter);
            }

            add(buildHeader());
            add(Box.createVerticalStrut(12));
            add(section("PERSONAJE", Theme.MUTED));
            add(Box.createVerticalStrut(6));
            addFull(this, character);
            power.setFont(Theme.font(10, Font.PLAIN));
            power.setForeground(Theme.DIM);
            power.setAlignmentX(LEFT_ALIGNMENT);
            power.setBorder(BorderFactory.createEmptyBorder(6, 2, 2, 2));
            add(power);
            add(Box.createVerticalStrut(8));
            add(section("ENTRENAMIENTO", Theme.MUTED));
            add(Box.createVerticalStrut(6));
            for (Stat stat : Stat.values()) {
                add(statRow(stat));
            }
            add(Box.createVerticalGlue());

            character.addActionListener(e -> updateCharacter());
            updateCharacter();
            refresh();
        }

        private JComponent buildHeader() {
            JPanel header = new JPanel(new BorderLayout(14, 0));
            header.setOpaque(false);
            header.setAlignmentX(LEFT_ALIGNMENT);
            header.setMaximumSize(new Dimension(Integer.MAX_VALUE, 108));

            portrait.setPreferredSize(new Dimension(104, 104));
            portrait.setMaximumSize(new Dimension(104, 104));
            portrait.setMinimumSize(new Dimension(104, 104));
            portrait.setRingColor(accent);
            header.add(portrait, BorderLayout.WEST);

            JPanel info = new JPanel();
            info.setOpaque(false);
            info.setLayout(new BoxLayout(info, BoxLayout.Y_AXIS));
            role.setFont(Theme.font(9, Font.BOLD));
            role.setForeground(accent);
            role.setAlignmentX(LEFT_ALIGNMENT);
            name.setFont(Theme.font(22, Font.BOLD));
            name.setForeground(Theme.TEXT);
            name.setAlignmentX(LEFT_ALIGNMENT);
            points.setFont(Theme.font(11, Font.BOLD));
            points.setForeground(Theme.GOLD);
            points.setAlignmentX(LEFT_ALIGNMENT);
            info.add(role);
            info.add(Box.createVerticalStrut(4));
            info.add(name);
            info.add(Box.createVerticalStrut(4));
            info.add(points);
            header.add(info, BorderLayout.CENTER);
            return header;
        }

        private JComponent statRow(Stat stat) {
            JPanel row = new JPanel(new BorderLayout(8, 0));
            row.setOpaque(false);
            row.setAlignmentX(LEFT_ALIGNMENT);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 31));

            JLabel label = new JLabel();
            label.setFont(Theme.font(9, Font.BOLD));
            label.setForeground(Theme.MUTED);
            label.setPreferredSize(new Dimension(62, 24));
            statLabels.put(stat, label);
            row.add(label, BorderLayout.WEST);

            StatBar bar = new StatBar(Theme.statColor(stat));
            statBars.put(stat, bar);
            row.add(bar, BorderLayout.CENTER);

            ArcadeButton upgrade = new ArcadeButton("MEJORAR",
                    ArcadeButton.Style.SMALL);
            upgrade.setCompactSize(126, 26);
            upgrade.addActionListener(e -> upgrade(stat));
            upgradeButtons.put(stat, upgrade);
            row.add(upgrade, BorderLayout.EAST);
            return row;
        }

        private void updateCharacter() {
            CharacterDefinition definition = getCharacter();
            portrait.setCharacter(definition);
            portrait.setRingColor(Look.aura(definition));
            if (definition == null) {
                power.setText(" ");
                return;
            }
            power.setText("<html><body style='width:300px'>"
                    + "<b>" + definition.specialAbility().name() + "</b> · "
                    + definition.specialAbility().effectName()
                    + " · dano " + definition.specialAbility().damage()
                    + "<br><span style='color:#766888'>Tiro de poder: "
                    + definition.specialAbility().name() + "</span></body></html>");
        }

        void configure(Profile profile, boolean machine, String roleText) {
            this.profile = profile;
            this.machine = machine;
            this.roleText = roleText;
            refresh();
        }

        Profile getProfile() {
            return profile;
        }

        CharacterDefinition getCharacter() {
            return (CharacterDefinition) character.getSelectedItem();
        }

        private void upgrade(Stat stat) {
            if (machine) {
                return;
            }
            try {
                profile.upgrade(stat);
                refresh();
            } catch (DOPOSoccerException e) {
                gui.showError(e);
            }
        }

        void refresh() {
            role.setText(machine ? "CONTROLADO POR LA MAQUINA" : roleText);
            name.setText(profile.getName());
            points.setText(machine ? "Perfil automatico"
                    : "PUNTOS DISPONIBLES · " + profile.getPoints());
            for (Stat stat : Stat.values()) {
                int value = profile.getStats().get(stat);
                statLabels.get(stat).setText(Theme.statAbbreviation(stat) + "  " + value);
                statBars.get(stat).setValue(value);
                ArcadeButton button = upgradeButtons.get(stat);
                if (machine) {
                    button.setText("AUTOMATICO");
                    button.setEnabled(false);
                    button.setToolTipText("La maquina no compra mejoras.");
                } else if (value >= Stats.MAX) {
                    button.setText("NIVEL MAXIMO");
                    button.setEnabled(false);
                    button.setToolTipText("Esta estadistica ya esta al maximo.");
                } else {
                    int cost = profile.getStats().upgradeCost(stat);
                    button.setText("MEJORAR · " + cost);
                    button.setEnabled(profile.getPoints() >= cost);
                    button.setToolTipText(profile.getPoints() >= cost
                            ? "Comprar +1 " + Theme.statName(stat)
                            : "Te faltan " + (cost - profile.getPoints()) + " puntos.");
                }
            }
            updateCharacter();
        }
    }

    /**
     * Barra segmentada de una estadistica del perfil.
     */
    private static final class StatBar extends JComponent {

        private final Color color;
        private int value = Stats.MIN;

        StatBar(Color color) {
            this.color = color;
            setOpaque(false);
            setPreferredSize(new Dimension(80, 12));
            setMaximumSize(new Dimension(Integer.MAX_VALUE, 12));
            setMinimumSize(new Dimension(30, 12));
        }

        void setValue(int value) {
            this.value = Math.max(Stats.MIN, Math.min(Stats.MAX, value));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            int width = getWidth();
            int height = getHeight();
            int gap = 3;
            int segments = Stats.MAX;
            double segmentWidth = Math.max(2,
                    (width - gap * (segments - 1)) / (double) segments);
            for (int i = 0; i < segments; i++) {
                double x = i * (segmentWidth + gap);
                RoundRectangle2D shape = new RoundRectangle2D.Double(x, 1,
                        segmentWidth, height - 2, 3, 3);
                if (i < value) {
                    g.setColor(Theme.blend(color, Theme.TEXT, i == value - 1 ? 0.18 : 0.0));
                } else {
                    g.setColor(new Color(255, 255, 255, 18));
                }
                g.fill(shape);
            }
            g.dispose();
        }
    }
}
