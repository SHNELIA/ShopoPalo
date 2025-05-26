package org.projectplatformer.Menu;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Esc {

    public static class GameWindow extends JFrame implements KeyListener, ComponentListener {

        private JPanel menuPanel;
        private boolean menuVisible = false;
        private GameMenu gameMenuInstance;

        private JButton musicButton;
        private JButton soundsButton;

        private static final Color PAUSE_MENU_BG_COLOR = new Color(51, 34, 25);
        private static final Color PAUSE_MENU_TITLE_COLOR = new Color(153, 102, 51);
        private static final Color BUTTON_TEXT_COLOR = new Color(255, 255, 255);
        private static final Color BUTTON_BG_COLOR = new Color(102, 68, 34);

        private static final Color BUTTON_ON_COLOR = new Color(70, 120, 70);
        private static final Color BUTTON_OFF_COLOR = new Color(120, 70, 70);
        private static final Color BUTTON_HOVER_COLOR_LIGHT = new Color(130, 95, 50);
        private static final Color BUTTON_PRESSED_COLOR_DARK = new Color(70, 45, 15);

        private static final Color BUTTON_BORDER_OUTER = new Color(128, 85, 42);
        private static final Color BUTTON_BORDER_INNER = new Color(77, 51, 26);

        private static final int BASE_WIDTH = 800;
        private static final int BASE_HEIGHT = 600;

        private Font FONT_PAUSE_TITLE_BASE;
        private Font FONT_BUTTON_PAUSE_BASE;

        public GameWindow(GameMenu gameMenu) {
            this.gameMenuInstance = gameMenu;
            initializeFonts();
            initializeUI();

            // ======================================
            // ТУТ БІЛЬШЕ НЕ ЗАВАНТАЖУЄМО ІГРОВІ ЗВУКИ ЧЕРЕЗ AudioManager
            // Вони будуть завантажуватися через AssetManager в LibGDX частині гри.
            // ======================================
            // AudioManager.loadSoundEffect("jump", "/audio/jump.wav"); // ВИДАЛЕНО
            // ... інші звуки ...

            // Якщо музика гри завантажується в LibGDX, також приберіть це:
            // AudioManager.loadMusic("/audio/game_background_music.wav"); // ВИДАЛЕНО
            // AudioManager.playMusic(); // ВИДАЛЕНО (Музика гри буде керуватися LibGDX)

            // Важливо: Якщо ви використовуєте Swing меню і LibGDX для гри,
            // то AudioManager тут повинен керувати лише музикою/звуками меню.
            // Музика гри буде запущена/зупинена в LibGDX GameScreen.
            // При поверненні в меню, ми можемо перезапустити музику меню.
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    // Якщо закривається вікно гри, і музика гри керується LibGDX,
                    // то AudioManager (для меню) не повинен впливати на музику гри.
                    // Якщо GameWindow є вікном LibGDX, то dispose() буде викликано.
                    // Якщо це Swing-вікно, то тут ми могли б зупинити фонову музику меню,
                    // якщо вона грала, але оскільки ми переходимо в меню, то нічого не робимо.
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    // Якщо GameWindow закривається і повертаємося в головне меню
                    if (gameMenuInstance != null) {
                        System.out.println("GameWindow closed. Showing GameMenu...");
                        // Припускаємо, що музика меню керується цим AudioManager.
                        // Якщо музика меню була зупинена (наприклад, коли гра стартувала),
                        // то тепер її можна відновити.
                        AudioManager.loadMusic("/audio/background_music.wav"); // Завантажуємо музику меню
                        AudioManager.playMusic(); // Запускаємо музику меню
                        gameMenuInstance.showGameMenu();
                    }
                }
            });
        }

        private void initializeFonts() {
            FONT_PAUSE_TITLE_BASE = new Font("SansSerif", Font.BOLD, 36);
            FONT_BUTTON_PAUSE_BASE = new Font("SansSerif", Font.BOLD, 22);
        }

        private void initializeUI() {
            setTitle(LanguageManager.get("gameWindow_title"));
            setSize(BASE_WIDTH, BASE_HEIGHT);
            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            setLocationRelativeTo(null);

            addKeyListener(this);
            setFocusable(true);
            setFocusTraversalKeysEnabled(false);

            addComponentListener((ComponentListener) this);

            JPanel mainContentPanel = new JPanel();
            mainContentPanel.setBackground(PAUSE_MENU_BG_COLOR);
            mainContentPanel.setLayout(new BorderLayout());
            add(mainContentPanel, BorderLayout.CENTER);

            initMenuPanel();

            updateMenuComponentSizes();

            setVisible(true);
        }

        private void initMenuPanel() {
            menuPanel = new JPanel();
            menuPanel.setBackground(PAUSE_MENU_BG_COLOR);
            menuPanel.setLayout(new GridBagLayout());
            menuPanel.setPreferredSize(new Dimension(400, 550));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.insets = new Insets(15, 0, 15, 0);
            gbc.anchor = GridBagConstraints.CENTER;
            gbc.gridx = 0;
            gbc.weightx = 0;

            JLabel titleLabel = new JLabel("PAUSE");
            titleLabel.setForeground(PAUSE_MENU_TITLE_COLOR);

            gbc.gridy = 0;
            gbc.insets = new Insets(0, 0, 50, 0);
            menuPanel.add(titleLabel, gbc);

            gbc.insets = new Insets(15, 0, 15, 0);

            JButton continueButton = createFantasyButton("CONTINUE", BUTTON_BG_COLOR);
            JButton guideButton = createFantasyButton("GUIDE", BUTTON_BG_COLOR);
            JButton goToMenuButton = createFantasyButton("GO TO MENU", BUTTON_BG_COLOR);

            musicButton = createFantasyButton(AudioManager.getMusicButtonText(),
                AudioManager.isMusicEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR);
            soundsButton = createFantasyButton(AudioManager.getSoundsButtonText(),
                AudioManager.isSoundsEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR);


            continueButton.addActionListener(e -> {
                System.out.println("Продовжити гру/додаток");
                toggleMenuVisibility();
            });

            guideButton.addActionListener(e -> {
                System.out.println("Відкрити довідник/посібник");
                GuideWindow guideWindow = new GuideWindow(this);
                guideWindow.setVisible(true);
            });

            goToMenuButton.addActionListener(e -> {
                System.out.println("Перейти до головного меню");
                // Якщо ви запускаєте LibGDX гру, то музика гри керується там.
                // При поверненні в меню, ми можемо зупинити її або дозволити LibGDX це зробити.
                // І потім відновити музику меню.
                AudioManager.loadMusic("/audio/background_music.wav"); // Перезавантажуємо музику меню
                AudioManager.playMusic(); // Запускаємо музику меню

                dispose(); // Закриваємо поточне вікно гри
                if (gameMenuInstance != null) {
                    gameMenuInstance.showGameMenu(); // Повертаємося до головного меню
                }
            });

            musicButton.addActionListener(e -> {
                AudioManager.toggleMusic();
                musicButton.setText(AudioManager.getMusicButtonText());
                ((FantasyButton) musicButton).setCurrentBackgroundColor(
                    AudioManager.isMusicEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR
                );
                revalidate();
                repaint();
            });

            soundsButton.addActionListener(e -> {
                AudioManager.toggleSounds();
                soundsButton.setText(AudioManager.getSoundsButtonText());
                ((FantasyButton) soundsButton).setCurrentBackgroundColor(
                    AudioManager.isSoundsEnabled() ? BUTTON_ON_COLOR : BUTTON_OFF_COLOR
                );
                revalidate();
                repaint();
            });

            gbc.gridy = 1;
            menuPanel.add(continueButton, gbc);

            gbc.gridy = 2;
            menuPanel.add(guideButton, gbc);

            gbc.gridy = 3;
            menuPanel.add(goToMenuButton, gbc);

            gbc.gridy = 4;
            menuPanel.add(musicButton, gbc);

            gbc.gridy = 5;
            menuPanel.add(soundsButton, gbc);

            gbc.gridx = 0;
            gbc.gridy = 6;
            gbc.weightx = 1.0;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            menuPanel.add(Box.createHorizontalGlue(), gbc);

            menuPanel.setVisible(false);
            JLayeredPane layeredPane = getLayeredPane();
            layeredPane.add(menuPanel, JLayeredPane.PALETTE_LAYER);
        }

        private class FantasyButton extends JButton {
            private Color currentBackgroundColor;
            private Color defaultBackgroundColor;
            private boolean isHovered = false;
            private boolean isPressed = false;

            public FantasyButton(String text, Color defaultBgColor) {
                super(text);
                this.defaultBackgroundColor = defaultBgColor;
                this.currentBackgroundColor = defaultBgColor;
                setForeground(BUTTON_TEXT_COLOR);
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
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                g2.setColor(BUTTON_BORDER_OUTER);
                g2.setStroke(new BasicStroke(4));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);

                g2.setColor(BUTTON_BORDER_INNER);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(2, 2, getWidth() - 5, getHeight() - 5, 10, 10);

                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(getText())) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(getText(), x, y);
                g2.dispose();
            }

            @Override
            protected void paintBorder(Graphics g) {
            }
        }

        private JButton createFantasyButton(String text, Color defaultBgColor) {
            return new FantasyButton(text, defaultBgColor);
        }

        private void updateMenuComponentSizes() {
            int currentWidth = getWidth();
            int currentHeight = getHeight();

            double scaleX = (double) currentWidth / BASE_WIDTH;
            double scaleY = (double) currentHeight / BASE_HEIGHT;
            double overallScale = Math.min(scaleX, scaleY);

            JLabel titleLabel = (JLabel) menuPanel.getComponent(0);
            Font currentTitleFont = FONT_PAUSE_TITLE_BASE.deriveFont((float) (FONT_PAUSE_TITLE_BASE.getSize2D() * overallScale));
            titleLabel.setFont(currentTitleFont);

            Font currentButtonFont = FONT_BUTTON_PAUSE_BASE.deriveFont((float) (FONT_BUTTON_PAUSE_BASE.getSize2D() * overallScale));

            for (int i = 1; i < menuPanel.getComponentCount() - 1; i++) {
                Component comp = menuPanel.getComponent(i);
                if (comp instanceof JButton) {
                    JButton button = (JButton) comp;
                    button.setFont(currentButtonFont);

                    Dimension baseButtonSize = new Dimension(200, 50);
                    Dimension newButtonSize = new Dimension(
                        (int) (baseButtonSize.width * overallScale),
                        (int) (baseButtonSize.height * overallScale)
                    );
                    button.setPreferredSize(newButtonSize);
                    button.setMinimumSize(newButtonSize);
                    button.setMaximumSize(newButtonSize);
                }
            }

            menuPanel.setPreferredSize(new Dimension(
                (int)(400 * overallScale),
                (int)(550 * overallScale)
            ));

            centerMenuPanel();
            revalidate();
            repaint();
        }

        private void centerMenuPanel() {
            if (menuVisible) {
                int panelWidth = menuPanel.getPreferredSize().width;
                int panelHeight = menuPanel.getPreferredSize().height;

                int frameWidth = getContentPane().getWidth();
                int frameHeight = getContentPane().getHeight();

                int x = (frameWidth - panelWidth) / 2;
                int y = (frameHeight - panelHeight) / 2;

                menuPanel.setBounds(x, y, panelWidth, panelHeight);
                menuPanel.revalidate();
                menuPanel.repaint();
            }
        }

        private void toggleMenuVisibility() {
            menuVisible = !menuVisible;
            menuPanel.setVisible(menuVisible);
            centerMenuPanel();
            if (!menuVisible) {
                setFocusable(true);
                requestFocusInWindow();
                // Якщо ви запускаєте LibGDX гру, тут можна відновити музику гри
                // Gdx.audio.getMusic().play(); // Або аналогічний виклик
            } else {
                // При показі меню паузи, зупиняємо музику гри
                // Gdx.audio.getMusic().pause(); // Або аналогічний виклик
            }
        }

        @Override
        public void keyTyped(KeyEvent e) {
        }

        @Override
        public void keyPressed(KeyEvent e) {
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                toggleMenuVisibility();
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
        }

        @Override
        public void componentResized(ComponentEvent e) {
            updateMenuComponentSizes();
        }

        @Override
        public void componentMoved(ComponentEvent e) {
        }

        @Override
        public void componentShown(ComponentEvent e) {
        }

        @Override
        public void componentHidden(ComponentEvent e) {
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
            } catch (Exception e) {
                e.printStackTrace();
            }
            // Музика головного меню (WAV)
            AudioManager.loadMusic("/audio/background_music.wav");
            AudioManager.playMusic();

            GameMenu mainMenu = new GameMenu();
            mainMenu.setVisible(true);

            mainMenu.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosing(WindowEvent e) {
                    AudioManager.cleanup(); // Очищаємо аудіоресурси Swing
                    System.exit(0);
                }
            });
        });
    }
}
