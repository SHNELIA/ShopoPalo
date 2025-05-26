package org.projectplatformer.lwjgl3.Menu;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.projectplatformer.lwjgl3.Main;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.*;


public class GameMenu extends JFrame implements ActionListener, ComponentListener {

    // UI Components
    private JButton playButton;
    private JButton guideButton;
    private JButton settingsButton;
    private JButton quitButton;
    private JButton musicButton;
    private JButton soundButton;
    private JLabel titleLabel;
    private JLabel madeByLabel;

    // Play Menu Components
    private JDialog playMenuDialog;
    private JLabel playMenuTitle;
    private JButton dialogContinueButton;
    private JButton dialogNewGameButton;
    private JButton dialogChooseSaveButton;
    private JButton dialogBackToMainButton;


    // Colors
    private static final Color FANTASY_DARK_WOOD = new Color(60, 40, 20);
    private static final Color FANTASY_BROWN_LEATHER = new Color(100, 70, 40);
    private static final Color FANTASY_BRONZE = new Color(180, 110, 50);
    private static final Color FANTASY_GREY_STONE = new Color(90, 90, 90);
    private static final Color FANTASY_TEXT_LIGHT = new Color(240, 230, 200);
    private static final Color FANTASY_TEXT_DARK = new Color(30, 30, 30);
    private static final Color FANTASY_BORDER_COLOR = new Color(40, 30, 10);

    // Кольори для кнопок стану (Music/Sounds)
    private static final Color BUTTON_ON_COLOR = new Color(70, 120, 70);
    private static final Color BUTTON_OFF_COLOR = new Color(120, 70, 70);
    private static final Color BUTTON_HOVER_COLOR_LIGHT = new Color(130, 95, 50);
    private static final Color BUTTON_PRESSED_COLOR_DARK = new Color(70, 45, 15);

    // Базові розміри для масштабування
    private static final int BASE_WIDTH = 1024;
    private static final int BASE_HEIGHT = 768;
    private static final int DIALOG_BASE_WIDTH = 500;
    private static final int DIALOG_BASE_HEIGHT = 600;

    private static Font FONT_MAIN_TITLE_BASE;
    private static Font FONT_BUTTON_LARGE_BASE;
    private static Font FONT_BUTTON_SMALL_BASE;
    private static Font FONT_MADE_BY_BASE;
    private static Font FONT_DIALOG_TITLE_BASE;
    private static Font FONT_DIALOG_TEXT_BASE;

    static {
        // !!!!!!!!! ВАЖЛИВО !!!!!!!!!
        // Якщо ви використовуєте власний шрифт, переконайтеся, що файл шрифту
        // коректно розміщений у resources/fonts/MinecraftEven.ttf та доступний.
        // Закоментований блок показує, як це зробити.
        // Якщо шрифт не завантажується, буде використано Arial як запасний.
        try {
            // InputStream is = GameMenu.class.getResourceAsStream("/fonts/MinecraftEven.ttf");
            // if (is != null) {
            //     Font minecraftEven = Font.createFont(Font.TRUETYPE_FONT, is);
            //     GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            //     ge.registerFont(minecraftEven);
            //     FONT_MAIN_TITLE_BASE = minecraftEven.deriveFont(Font.BOLD, 60f);
            //     FONT_BUTTON_LARGE_BASE = minecraftEven.deriveFont(Font.BOLD, 28f);
            //     FONT_BUTTON_SMALL_BASE = minecraftEven.deriveFont(Font.BOLD, 14f);
            //     FONT_MADE_BY_BASE = minecraftEven.deriveFont(Font.PLAIN, 14f);
            //     FONT_DIALOG_TITLE_BASE = minecraftEven.deriveFont(Font.BOLD, 36f);
            //     FONT_DIALOG_TEXT_BASE = minecraftEven.deriveFont(Font.PLAIN, 18f);
            // } else {
            //     System.err.println("Font 'MinecraftEven.ttf' not found. Using Arial.");
            //     FONT_MAIN_TITLE_BASE = new Font("Arial", Font.BOLD, 60);
            //     FONT_BUTTON_LARGE_BASE = new Font("Arial", Font.BOLD, 28);
            //     FONT_BUTTON_SMALL_BASE = new Font("Arial", Font.BOLD, 14);
            //     FONT_MADE_BY_BASE = new Font("Arial", Font.PLAIN, 14);
            //     FONT_DIALOG_TITLE_BASE = new Font("Arial", Font.BOLD, 36);
            //     FONT_DIALOG_TEXT_BASE = new Font("Arial", Font.PLAIN, 18);
            // }
            // Якщо ви не використовуєте спеціальні шрифти, просто залиште Arial:
            FONT_MAIN_TITLE_BASE = new Font("Arial", Font.BOLD, 60);
            FONT_BUTTON_LARGE_BASE = new Font("Arial", Font.BOLD, 28);
            FONT_BUTTON_SMALL_BASE = new Font("Arial", Font.BOLD, 14);
            FONT_MADE_BY_BASE = new Font("Arial", Font.PLAIN, 14);
            FONT_DIALOG_TITLE_BASE = new Font("Arial", Font.BOLD, 36);
            FONT_DIALOG_TEXT_BASE = new Font("Arial", Font.PLAIN, 18);
        } catch (Exception e) {
            System.err.println("Error loading font. Using Arial. " + e.getMessage());
            // Fallback to Arial if font loading fails
            FONT_MAIN_TITLE_BASE = new Font("Arial", Font.BOLD, 60);
            FONT_BUTTON_LARGE_BASE = new Font("Arial", Font.BOLD, 28);
            FONT_BUTTON_SMALL_BASE = new Font("Arial", Font.BOLD, 14);
            FONT_MADE_BY_BASE = new Font("Arial", Font.PLAIN, 14);
            FONT_DIALOG_TITLE_BASE = new Font("Arial", Font.BOLD, 36);
            FONT_DIALOG_TEXT_BASE = new Font("Arial", Font.PLAIN, 18);
        }
    }


    public GameMenu() {
        // Main window setup
        setTitle(LanguageManager.get("title") + " - Menu");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));

        // Додаємо ComponentListener до вікна
        addComponentListener(this);

        JPanel mainPanel = new JPanel(new BorderLayout(30, 20));
        mainPanel.setBackground(FANTASY_DARK_WOOD);
        mainPanel.setBorder(new EmptyBorder(40, 60, 30, 60));
        // Game title
        titleLabel = new JLabel(LanguageManager.get("title"), SwingConstants.CENTER);
        titleLabel.setForeground(FANTASY_BRONZE);
        titleLabel.setBorder(new EmptyBorder(10, 0, 40, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        // Main buttons panel
        JPanel buttonsContainerPanel = new JPanel(new GridBagLayout());
        buttonsContainerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        // Create buttons using the createFantasyButton method
        playButton = createFantasyButton(LanguageManager.get("playButton"), FANTASY_BROWN_LEATHER, FONT_BUTTON_LARGE_BASE);
        guideButton = createFantasyButton(LanguageManager.get("guideButton"), FANTASY_BROWN_LEATHER, FONT_BUTTON_LARGE_BASE);
        settingsButton = createFantasyButton(LanguageManager.get("settingsButton"), FANTASY_BROWN_LEATHER, FONT_BUTTON_LARGE_BASE);
        quitButton = createFantasyButton(LanguageManager.get("quitButton"), FANTASY_BROWN_LEATHER, FONT_BUTTON_LARGE_BASE);

        // Add action listeners
        playButton.addActionListener(this);
        guideButton.addActionListener(this);
        settingsButton.addActionListener(this);
        quitButton.addActionListener(this);

        gbc.gridy = 0;
        buttonsContainerPanel.add(playButton, gbc);
        gbc.gridy = 1;
        buttonsContainerPanel.add(guideButton, gbc);
        gbc.gridy = 2;
        buttonsContainerPanel.add(settingsButton, gbc);
        gbc.gridy = 3;
        buttonsContainerPanel.add(quitButton, gbc);

        // Add "stretchers" (weights) for centering
        gbc.gridy = 4;
        gbc.weighty = 1.0;
        buttonsContainerPanel.add(Box.createVerticalGlue(), gbc);
        gbc.gridy = -1;
        gbc.weighty = 1.0;
        buttonsContainerPanel.add(Box.createVerticalGlue(), gbc);

        mainPanel.add(buttonsContainerPanel, BorderLayout.CENTER);


        // Bottom panel - Використовуємо BorderLayout для контролю
        JPanel bottomPanel = new JPanel(new BorderLayout(30, 10));
        bottomPanel.setOpaque(false);
        bottomPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        JPanel soundTogglePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 0));
        soundTogglePanel.setOpaque(false);

        musicButton = createFantasyButton(AudioManager.getMusicButtonText(),
            AudioManager.isMusicEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR,
            FONT_BUTTON_SMALL_BASE);
        soundButton = createFantasyButton(AudioManager.getSoundsButtonText(),
            AudioManager.isSoundsEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR,
            FONT_BUTTON_SMALL_BASE);

        // Встановлюємо розміри для малих кнопок
        Dimension toggleSize = new Dimension(160, 40);
        musicButton.setPreferredSize(toggleSize);
        musicButton.setMinimumSize(new Dimension(80, 30));
        musicButton.setMaximumSize(toggleSize);

        soundButton.setPreferredSize(toggleSize);
        soundButton.setMinimumSize(new Dimension(80, 30));
        soundButton.setMaximumSize(toggleSize);


        musicButton.addActionListener(this);
        soundButton.addActionListener(this);

        soundTogglePanel.add(musicButton);
        soundTogglePanel.add(soundButton);
        bottomPanel.add(soundTogglePanel, BorderLayout.WEST);

        // MADE BY LABEL - Тепер з HTML-форматуванням
        JPanel madeByPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        madeByPanel.setOpaque(false);
        madeByLabel = new JLabel("<html><div style='text-align: right;'>Made by: <br>Shpuniar Nazar<br>Revenko Anna<br>Burma Sofia<br>Horyslavets Kateryna<br>Tsaprylova Irina</div></html>", SwingConstants.RIGHT);
        madeByLabel.setForeground(FANTASY_TEXT_LIGHT);
        madeByPanel.add(madeByLabel);
        bottomPanel.add(madeByPanel, BorderLayout.EAST);

        mainPanel.add(bottomPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);

        // Початкове оновлення розмірів компонентів
        updateMenuComponentSizes();
    }

    // Метод для динамічного оновлення розмірів компонентів головного меню та діалогу
    public void updateMenuComponentSizes() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        double scaleX = (double) currentWidth / BASE_WIDTH;
        double scaleY = (double) currentHeight / BASE_HEIGHT;
        double overallScale = Math.min(scaleX, scaleY);

        // Оновлення заголовка головного меню
        titleLabel.setFont(FONT_MAIN_TITLE_BASE.deriveFont((float) (FONT_MAIN_TITLE_BASE.getSize2D() * overallScale)));

        // Оновлення великих кнопок головного меню
        Font currentLargeButtonFont = FONT_BUTTON_LARGE_BASE.deriveFont((float) (FONT_BUTTON_LARGE_BASE.getSize2D() * overallScale));
        playButton.setFont(currentLargeButtonFont);
        guideButton.setFont(currentLargeButtonFont);
        settingsButton.setFont(currentLargeButtonFont);
        quitButton.setFont(currentLargeButtonFont);

        // Оновлення малих кнопок головного меню
        Font currentSmallButtonFont = FONT_BUTTON_SMALL_BASE.deriveFont((float) (FONT_BUTTON_SMALL_BASE.getSize2D() * overallScale));
        musicButton.setFont(currentSmallButtonFont);
        soundButton.setFont(currentSmallButtonFont);

        Dimension baseToggleSize = new Dimension(120, 40);
        Dimension newToggleSize = new Dimension(
            (int) (baseToggleSize.width * overallScale),
            (int) (baseToggleSize.height * overallScale)
        );
        musicButton.setPreferredSize(newToggleSize);
        musicButton.setMinimumSize(newToggleSize);
        musicButton.setMaximumSize(newToggleSize);

        soundButton.setPreferredSize(newToggleSize);
        soundButton.setMinimumSize(newToggleSize);
        soundButton.setMaximumSize(newToggleSize);

        // Оновлення madeByLabel - шрифт також масштабується
        Font currentMadeByFont = FONT_MADE_BY_BASE.deriveFont((float) (FONT_MADE_BY_BASE.getSize2D() * overallScale));
        madeByLabel.setFont(currentMadeByFont);

        // Якщо вікно дуже вузьке, зменшуємо відступи внизу
        JPanel mainContentPanel = (JPanel) getContentPane().getComponent(0);
        JPanel bottomPanel = (JPanel) mainContentPanel.getComponent(mainContentPanel.getComponentCount() - 1);

        if (currentWidth < BASE_WIDTH * 0.8) {
            int borderInset = Math.max(5, (int)(10 * overallScale));
            bottomPanel.setBorder(new EmptyBorder(borderInset, borderInset, borderInset, borderInset));
        } else {
            bottomPanel.setBorder(new EmptyBorder(10, 20, 10, 20));
        }

        // Оновлення компонентів діалогу (якщо він відкритий)
        if (playMenuDialog != null && playMenuDialog.isVisible()) {
            int dialogCurrentWidth = playMenuDialog.getWidth();
            int dialogCurrentHeight = playMenuDialog.getHeight();

            double dialogScaleX = (double) dialogCurrentWidth / DIALOG_BASE_WIDTH;
            double dialogScaleY = (double) dialogCurrentHeight / DIALOG_BASE_HEIGHT;
            double dialogOverallScale = Math.min(dialogScaleX, dialogScaleY);

            if (playMenuTitle != null) {
                playMenuTitle.setFont(FONT_MAIN_TITLE_BASE.deriveFont((float) (FONT_MAIN_TITLE_BASE.getSize2D() * dialogOverallScale * 0.8)));
            }

            Font dialogButtonFont = FONT_BUTTON_LARGE_BASE.deriveFont((float) (FONT_BUTTON_LARGE_BASE.getSize2D() * dialogOverallScale));
            if (dialogContinueButton != null) dialogContinueButton.setFont(dialogButtonFont);
            if (dialogNewGameButton != null) dialogNewGameButton.setFont(dialogButtonFont);
            if (dialogChooseSaveButton != null) dialogChooseSaveButton.setFont(dialogButtonFont);
            if (dialogBackToMainButton != null) dialogBackToMainButton.setFont(dialogButtonFont);

            // Оновлення розмірів кнопок у діалозі (аналогічно main buttons)
            Dimension dialogBtnMinSize = new Dimension((int)(200 * dialogOverallScale), (int)(60 * dialogOverallScale));
            Dimension dialogBtnMaxSize = new Dimension((int)(450 * dialogOverallScale), (int)(85 * dialogOverallScale));

            if (dialogContinueButton != null) {
                dialogContinueButton.setMinimumSize(dialogBtnMinSize);
                dialogContinueButton.setPreferredSize(dialogBtnMinSize);
                dialogContinueButton.setMaximumSize(dialogBtnMaxSize);
            }
            if (dialogNewGameButton != null) {
                dialogNewGameButton.setMinimumSize(dialogBtnMinSize);
                dialogNewGameButton.setPreferredSize(dialogBtnMinSize);
                dialogNewGameButton.setMaximumSize(dialogBtnMaxSize);
            }
            if (dialogChooseSaveButton != null) {
                dialogChooseSaveButton.setMinimumSize(dialogBtnMinSize);
                dialogChooseSaveButton.setPreferredSize(dialogBtnMinSize);
                dialogChooseSaveButton.setMaximumSize(dialogBtnMaxSize);
            }
            if (dialogBackToMainButton != null) {
                dialogBackToMainButton.setMinimumSize(dialogBtnMinSize);
                dialogBackToMainButton.setPreferredSize(dialogBtnMinSize);
                dialogBackToMainButton.setMaximumSize(dialogBtnMaxSize);
            }


            playMenuDialog.revalidate();
            playMenuDialog.repaint();
        }

        revalidate();
        repaint();
    }

    @Override
    public void componentResized(ComponentEvent e) {
        updateMenuComponentSizes();
    }

    @Override
    public void componentMoved(ComponentEvent e) {}

    @Override
    public void componentShown(ComponentEvent e) {}

    @Override
    public void componentHidden(ComponentEvent e) {}


    private class FantasyButton extends JButton {
        private Color currentBackgroundColor;
        private Color defaultBaseColor;
        private boolean isHovered = false;
        private boolean isPressed = false;

        public FantasyButton(String text, Color defaultBgColor, Font font) {
            super(text);
            this.defaultBaseColor = defaultBgColor;
            this.currentBackgroundColor = defaultBgColor;
            setForeground(FANTASY_TEXT_LIGHT);
            setFont(font);
            setFocusPainted(false);
            setContentAreaFilled(false);
            setBorderPainted(false);
            setCursor(new Cursor(Cursor.HAND_CURSOR));

            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    isHovered = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    isHovered = false;
                    repaint();
                }

                @Override
                public void mousePressed(MouseEvent e) {
                    isPressed = true;
                    repaint();
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    isPressed = false;
                    repaint();
                }
            });
        }

        public void setCurrentBackgroundColor(Color color) {
            this.currentBackgroundColor = color;
            repaint();
        }


        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color actualBgColor = currentBackgroundColor;
            if (isPressed) {
                actualBgColor = BUTTON_PRESSED_COLOR_DARK;
            } else if (isHovered) {
                actualBgColor = BUTTON_HOVER_COLOR_LIGHT;
            }

            g2.setColor(actualBgColor);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

            g2.setColor(FANTASY_BORDER_COLOR);
            g2.setStroke(new BasicStroke(3));
            g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 20, 20);

            g2.setColor(getForeground());
            g2.setFont(getFont());
            super.paintComponent(g);
            g2.dispose();
        }

        @Override
        protected void paintBorder(Graphics g) {
        }
    }


    private JButton createFantasyButton(String text, Color bgColor, Font font) {
        FantasyButton button = new FantasyButton(text, bgColor, font);
        Dimension minBtnSize = new Dimension(200, 60);
        Dimension maxBtnSize;
        if (text.contains("<html>")) {
            maxBtnSize = new Dimension(450, 120);
        } else {
            maxBtnSize = new Dimension(450, 85);
        }
        button.setMinimumSize(minBtnSize);
        button.setMaximumSize(maxBtnSize);
        return button;
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == playButton) {
            showPlayMenu();
        } else if (e.getSource() == guideButton) {
            showGuideDialog();
        } else if (e.getSource() == settingsButton) {
            this.setVisible(false);
            new SettingsWindow(this);
        } else if (e.getSource() == quitButton) {
            int choice = JOptionPane.showConfirmDialog(this,
                LanguageManager.get("quitConfirm"),
                LanguageManager.get("quitTitle"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
            if (choice == JOptionPane.YES_OPTION) {
                System.exit(0);
            }
        } else if (e.getSource() == musicButton) {
            AudioManager.toggleMusic();
            musicButton.setText(AudioManager.getMusicButtonText());
            ((FantasyButton) musicButton).setCurrentBackgroundColor(
                AudioManager.isMusicEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR
            );
            musicButton.repaint();
        } else if (e.getSource() == soundButton) {
            AudioManager.toggleSounds();
            soundButton.setText(AudioManager.getSoundsButtonText());
            ((FantasyButton) soundButton).setCurrentBackgroundColor(
                AudioManager.isSoundsEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR
            );
            soundButton.repaint();
        }
    }

    private void showPlayMenu() {
        playMenuDialog = new JDialog(this, "Game Options", true);
        playMenuDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        playMenuDialog.setResizable(true);
        playMenuDialog.setSize(DIALOG_BASE_WIDTH, DIALOG_BASE_HEIGHT);
        playMenuDialog.setLocationRelativeTo(this);

        playMenuDialog.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateMenuComponentSizes();
            }
        });

        playMenuDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                showGameMenu();
            }
        });


        JPanel playMenuPanel = new JPanel(new BorderLayout(30, 20));
        playMenuPanel.setBackground(FANTASY_DARK_WOOD);
        playMenuPanel.setBorder(new EmptyBorder(40, 60, 30, 60));
        // Title
        playMenuTitle = new JLabel("GAME OPTIONS", SwingConstants.CENTER);
        playMenuTitle.setFont(FONT_MAIN_TITLE_BASE.deriveFont(40f));
        playMenuTitle.setForeground(FANTASY_BRONZE);
        playMenuTitle.setBorder(new EmptyBorder(10, 0, 40, 0));
        playMenuPanel.add(playMenuTitle, BorderLayout.NORTH);

        // Buttons panel
        JPanel buttonsGridPanel = new JPanel(new GridBagLayout());
        buttonsGridPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;


        dialogContinueButton = createFantasyButton("CONTINUE", FANTASY_BROWN_LEATHER, FONT_BUTTON_LARGE_BASE);
        dialogNewGameButton = createFantasyButton("NEW GAME", FANTASY_BROWN_LEATHER, FONT_BUTTON_LARGE_BASE);
        dialogChooseSaveButton = createFantasyButton("CHOOSE SAVE", FANTASY_BROWN_LEATHER, FONT_BUTTON_LARGE_BASE);
        dialogBackToMainButton = createFantasyButton("BACK TO MENU", FANTASY_BROWN_LEATHER, FONT_BUTTON_LARGE_BASE);

        // Add action listeners
        dialogContinueButton.addActionListener(e -> {
            playMenuDialog.dispose();
            startGame(true);
        });

        dialogNewGameButton.addActionListener(e -> {
            int choice = JOptionPane.showConfirmDialog(
                playMenuDialog,
                LanguageManager.get("newGameConfirm_message"),
                LanguageManager.get("newGameConfirm_title"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
            );

            if (choice == JOptionPane.YES_OPTION) {
                playMenuDialog.dispose();
                startGame(false);
            }
        });

        dialogChooseSaveButton.addActionListener(e -> {
            playMenuDialog.dispose();
            showSaveSelection();
        });
        dialogBackToMainButton.addActionListener(e -> playMenuDialog.dispose());

        gbc.gridy = 0;
        buttonsGridPanel.add(dialogContinueButton, gbc);
        gbc.gridy = 1;
        buttonsGridPanel.add(dialogNewGameButton, gbc);
        gbc.gridy = 2;
        buttonsGridPanel.add(dialogChooseSaveButton, gbc);
        gbc.gridy = 3;
        buttonsGridPanel.add(dialogBackToMainButton, gbc);

        gbc.gridy = 4;
        gbc.weighty = 1.0;
        buttonsGridPanel.add(Box.createVerticalGlue(), gbc);
        gbc.gridy = -1;
        gbc.weighty = 1.0;
        buttonsGridPanel.add(Box.createVerticalGlue(), gbc);


        playMenuPanel.add(buttonsGridPanel, BorderLayout.CENTER);
        playMenuDialog.add(playMenuPanel);

        updateMenuComponentSizes();
        playMenuDialog.pack();
        playMenuDialog.setVisible(true);
    }

    public void startGame(boolean continueGame) {
        this.setVisible(false);


        String message = continueGame ?
            LanguageManager.get("startGame_loadingSaved") :
            LanguageManager.get("startGame_startingNew");

//        JOptionPane.showMessageDialog(
//            null,
//            message,
//            LanguageManager.get("startGame_title"),
//            JOptionPane.INFORMATION_MESSAGE
//        );

//        new Esc.GameWindow(this);
        Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
        configuration.setForegroundFPS(Lwjgl3ApplicationConfiguration.getDisplayMode().refreshRate + 1);
        configuration.setWindowedMode(640, 480);
        configuration.setTitle("Shopopalo");
        configuration.useVsync(true);
        new Lwjgl3Application(new Main(), configuration);
    }

    private void showSaveSelection() {
        this.setVisible(false);
        SaveSelectionDialog saveDialog = new SaveSelectionDialog(this);
        // WindowListener is already added inside SaveSelectionDialog's constructor
        saveDialog.setVisible(true);
    }

    private void showGuideDialog() {
        JDialog guideDialog = new JDialog(this, LanguageManager.get("guideTitle"), true);
        guideDialog.setSize(600, 500);
        guideDialog.setLocationRelativeTo(this);
        guideDialog.getContentPane().setBackground(FANTASY_DARK_WOOD.brighter());

        guideDialog.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                showGameMenu();
            }
        });

        JTextArea guideText = new JTextArea(LanguageManager.get("guideText"));
        guideText.setFont(FONT_DIALOG_TEXT_BASE);
        guideText.setForeground(FANTASY_TEXT_LIGHT);
        guideText.setWrapStyleWord(true);
        guideText.setLineWrap(true);
        guideText.setEditable(false);
        guideText.setOpaque(false);
        guideText.setBorder(new EmptyBorder(20, 20, 20, 20));

        JScrollPane scrollPane = new JScrollPane(guideText);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(new LineBorder(FANTASY_BORDER_COLOR, 2));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);

        guideDialog.add(scrollPane);
        guideDialog.setVisible(true);
    }

    public void showGameMenu() {
        this.setVisible(true);
        musicButton.setText(AudioManager.getMusicButtonText());
        ((FantasyButton)musicButton).setCurrentBackgroundColor(
            AudioManager.isMusicEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR
        );
        musicButton.repaint();

        soundButton.setText(AudioManager.getSoundsButtonText());
        ((FantasyButton)soundButton).setCurrentBackgroundColor(
            AudioManager.isSoundsEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR
        );
        soundButton.repaint();

        updateMenuComponentSizes();

        this.revalidate();
        this.repaint();
        this.pack();
        this.setLocationRelativeTo(null);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Create GameMenu instance, but do NOT make it visible immediately
            GameMenu gameMenu = new GameMenu();

            // Implement a simple splash screen or loading phase
            JFrame splashFrame = new JFrame("Loading Game...");
            splashFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            splashFrame.setUndecorated(true);
            splashFrame.setSize(400, 200);
            splashFrame.setLocationRelativeTo(null);
            splashFrame.getContentPane().setBackground(new Color(30, 20, 10));
            splashFrame.getContentPane().setLayout(new GridBagLayout());

            JLabel loadingLabel = new JLabel("<html><center><font color='#F0E6C8' size='+2'>Loading Tales of the World...</font><br><font color='#C88C50'>Please wait</font></center></html>", SwingConstants.CENTER);
            splashFrame.add(loadingLabel);

            splashFrame.setVisible(true);

            Timer timer = new Timer(3000, new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    splashFrame.dispose();
                    gameMenu.showGameMenu();
                }
            });
            timer.setRepeats(false);
            timer.start();
        });
    }
}
