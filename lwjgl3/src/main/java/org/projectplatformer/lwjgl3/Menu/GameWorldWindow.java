package org.projectplatformer.lwjgl3.Menu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;

public class GameWorldWindow extends JFrame implements ActionListener {

    // Fantasy Colors (copied for consistency)
    private static final Color FANTASY_DARK_WOOD = new Color(60, 40, 20);
    private static final Color FANTASY_BROWN_LEATHER = new Color(100, 70, 40);
    private static final Color FANTASY_BRONZE = new Color(180, 110, 50);
    private static final Color FANTASY_TEXT_LIGHT = new Color(240, 230, 200);
    private static final Color FANTASY_BORDER_COLOR = new Color(40, 30, 10);
    private static final Color BUTTON_HOVER_COLOR_LIGHT = new Color(130, 95, 50);
    private static final Color BUTTON_PRESSED_COLOR_DARK = new Color(70, 45, 15);

    private static final int BASE_WIDTH = 800;
    private static final int BASE_HEIGHT = 600;

    private static Font FONT_TITLE_BASE;
    private static Font FONT_BUTTON_BASE;

    static {
        // Додаємо System.setProperty для кращого рендерингу шрифтів (як у GameMenu/Esc)
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }
        FONT_TITLE_BASE = new Font("Arial", Font.BOLD, 60);
        FONT_BUTTON_BASE = new Font("Arial", Font.BOLD, 32);
    }

    private JLabel titleLabel;
    private JButton shopButton;
    private JButton equipmentButton;
    private JButton goToLevelsButton;
    private JButton backToMenuButton;

    private GameMenu parentMenu; // Reference to the main GameMenu to return to it

    public GameWorldWindow(GameMenu parent) {
        this.parentMenu = parent;
        // ОНОВЛЕНО: Використання LanguageManager для заголовка вікна
        setTitle(LanguageManager.get("title") + " - " + LanguageManager.get("gameWorld_title"));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(BASE_WIDTH, BASE_HEIGHT));

        setLocationRelativeTo(null);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateComponentSizes();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(30, 20));
        mainPanel.setBackground(FANTASY_DARK_WOOD);
        mainPanel.setBorder(new EmptyBorder(40, 60, 30, 60));

        titleLabel = new JLabel(LanguageManager.get("gameWorld_title"), SwingConstants.CENTER);
        titleLabel.setForeground(FANTASY_BRONZE);
        titleLabel.setBorder(new EmptyBorder(10, 0, 40, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel buttonsContainerPanel = new JPanel(new GridBagLayout());
        buttonsContainerPanel.setOpaque(false);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.insets = new Insets(15, 0, 15, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        gbc.anchor = GridBagConstraints.CENTER;

        shopButton = createFantasyButton(LanguageManager.get("gameWorld_shop"), FANTASY_BROWN_LEATHER, FONT_BUTTON_BASE);
        equipmentButton = createFantasyButton(LanguageManager.get("gameWorld_equipment"), FANTASY_BROWN_LEATHER, FONT_BUTTON_BASE);
        goToLevelsButton = createFantasyButton(LanguageManager.get("gameWorld_levels"), FANTASY_BROWN_LEATHER, FONT_BUTTON_BASE);
        backToMenuButton = createFantasyButton(LanguageManager.get("backToMainMenu"), FANTASY_BRONZE.darker(), FONT_BUTTON_BASE.deriveFont(Font.PLAIN, 24f));


        shopButton.addActionListener(this);
        equipmentButton.addActionListener(this); // Ця кнопка тепер відкриватиме EquipmentWindow
        goToLevelsButton.addActionListener(this);
        backToMenuButton.addActionListener(this);

        gbc.gridy = 0;
        buttonsContainerPanel.add(shopButton, gbc);
        gbc.gridy = 1;
        buttonsContainerPanel.add(equipmentButton, gbc);
        gbc.gridy = 2;
        buttonsContainerPanel.add(goToLevelsButton, gbc);

        gbc.gridy = 3;
        gbc.weighty = 1.0;
        buttonsContainerPanel.add(Box.createVerticalGlue(), gbc);
        // FIX: Removed duplicate gbc.gridy = -1; and gbc.weighty = 1.0;
        // gbc.gridy = -1; // This line is problematic and potentially sets gridY to an invalid value
        // gbc.weighty = 1.0; // This line is a duplicate if the above was intended to be gbc.gridy = 4;
        // If you need more vertical space, consider adding a fixed-size rigid area or adjusting insets/weights on existing components.


        mainPanel.add(buttonsContainerPanel, BorderLayout.CENTER);

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
        southPanel.add(backToMenuButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        updateComponentSizes();
        setVisible(true);
    }

    private void updateComponentSizes() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        double scaleX = (double) currentWidth / BASE_WIDTH;
        double scaleY = (double) currentHeight / BASE_HEIGHT;
        double overallScale = Math.min(scaleX, scaleY);

        titleLabel.setFont(FONT_TITLE_BASE.deriveFont((float) (FONT_TITLE_BASE.getSize2D() * overallScale)));

        Font currentButtonFont = FONT_BUTTON_BASE.deriveFont((float) (FONT_BUTTON_BASE.getSize2D() * overallScale));
        shopButton.setFont(currentButtonFont);
        equipmentButton.setFont(currentButtonFont);
        goToLevelsButton.setFont(currentButtonFont);

        Dimension baseBtnSize = new Dimension(400, 80);
        Dimension newBtnSize = new Dimension(
            (int) (baseBtnSize.width * overallScale),
            (int) (baseBtnSize.height * overallScale)
        );
        shopButton.setPreferredSize(newBtnSize);
        equipmentButton.setPreferredSize(newBtnSize);
        goToLevelsButton.setPreferredSize(newBtnSize);

        Font currentBackBtnFont = FONT_BUTTON_BASE.deriveFont(Font.PLAIN, (float) (FONT_BUTTON_BASE.getSize2D() * overallScale * 0.7));
        backToMenuButton.setFont(currentBackBtnFont);
        Dimension baseBackBtnSize = new Dimension(250, 60);
        Dimension newBackBtnSize = new Dimension(
            (int) (baseBackBtnSize.width * overallScale),
            (int) (baseBackBtnSize.height * overallScale)
        );
        backToMenuButton.setPreferredSize(newBackBtnSize);

        revalidate();
        repaint();
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == shopButton) {
            JOptionPane.showMessageDialog(this, LanguageManager.get("gameWorld_shop_message"));

        } else if (e.getSource() == equipmentButton) {
            this.setVisible(false); // Ховаємо поточне вікно GameWorldWindow
            new EquipmentWindow(this); // Створюємо та показуємо EquipmentWindow
            // Після закриття EquipmentWindow, GameWorldWindow не буде автоматично показано.
            // Якщо ви хочете, щоб GameWorldWindow знову з'явилось після закриття EquipmentWindow,
            // вам потрібно буде додати логіку в EquipmentWindow або використовувати WindowListener.
            // Наразі, EquipmentWindow є модальним, тому виконання actionPerformed призупиниться до його закриття.
            // Після закриття, ви можете знову показати GameWorldWindow:
            this.setVisible(true); // Показуємо GameWorldWindow після закриття EquipmentWindow
            this.revalidate();
            this.repaint();

        } else if (e.getSource() == goToLevelsButton) {
            this.setVisible(false); // Ховаємо поточне вікно
            new LevelsWindow(this); // ВІРНЕ СТВОРЕННЯ: передаємо поточний GameWorldWindow як батьківський
        } else if (e.getSource() == backToMenuButton) {
            this.dispose(); // Закриваємо поточне вікно
            if (parentMenu != null) {
                parentMenu.showGameMenu(); // Показуємо головне меню знову
            } else {
                System.exit(0);
            }
        }
    }

    private JButton createFantasyButton(String text, Color bgColor, Font font) {
        FantasyButton button = new FantasyButton(text, bgColor, font);
        button.setPreferredSize(new Dimension(300, 70));
        button.setMinimumSize(new Dimension(200, 60));
        button.setMaximumSize(new Dimension(500, 100));
        return button;
    }

    // Внутрішній клас для кастомної кнопки - ЗАЛИШИВСЯ НЕЗМІННИМ
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
}
