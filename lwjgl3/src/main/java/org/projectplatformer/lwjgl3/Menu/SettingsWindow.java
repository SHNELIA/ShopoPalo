package org.projectplatformer.lwjgl3.Menu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;


public class SettingsWindow extends JFrame {
    private GameMenu gameMenu;
    private String initialLanguageCode; // Зберігаємо початкову мову
    private String selectedLanguageCode;

    private static final Color FANTASY_DARK_STONE = new Color(50, 50, 50);
    private static final Color FANTASY_GREY_STONE_LIGHTER = new Color(80, 80, 80);
    private static final Color FANTASY_BRONZE_ACCENT = new Color(200, 140, 80);
    private static final Color FANTASY_TEXT_LIGHT = new Color(240, 230, 200);
    private static final Color FANTASY_BORDER_COLOR = new Color(40, 30, 10);
    private static final Color FANTASY_TEXT_DARK = new Color(30, 30, 30);

    private static Font FONT_TITLE;
    private static Font FONT_LABEL;
    private static Font FONT_BUTTON;
    private static Font FONT_SMALL_LABEL;

    static {
        try {
            // ВИПРАВЛЕННЯ: Використання SettingsWindow.class замість settingsWindow.class
            InputStream is = SettingsWindow.class.getResourceAsStream("/fonts/MinecraftEven.ttf");
            if (is == null) {
                System.err.println("Font file not found: /fonts/MinecraftEven.ttf. Using Arial.");
                FONT_TITLE = new Font("Arial", Font.BOLD, 36);
                FONT_LABEL = new Font("Arial", Font.PLAIN, 18);
                FONT_BUTTON = new Font("Arial", Font.BOLD, 22);
                FONT_SMALL_LABEL = new Font("Arial", Font.PLAIN, 12);
            } else {
                Font minecraftEven = Font.createFont(Font.TRUETYPE_FONT, is);
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(minecraftEven);

                FONT_TITLE = minecraftEven.deriveFont(Font.BOLD, 36f);
                FONT_LABEL = minecraftEven.deriveFont(Font.PLAIN, 18f);
                FONT_BUTTON = minecraftEven.deriveFont(Font.BOLD, 22f);
                FONT_SMALL_LABEL = minecraftEven.deriveFont(Font.PLAIN, 12f);
            }
        } catch (FontFormatException | IOException e) {
            System.err.println("Error loading font. Using Arial. " + e.getMessage());
            FONT_TITLE = new Font("Arial", Font.BOLD, 36);
            FONT_LABEL = new Font("Arial", Font.PLAIN, 18);
            FONT_BUTTON = new Font("Arial", Font.BOLD, 22);
            FONT_SMALL_LABEL = new Font("Arial", Font.PLAIN, 12);
        }
    }

    private JLabel titleLabel;
    private JLabel musicVolumeLabel;
    private JLabel effectsVolumeLabel;
    private JLabel windowSizeLabel;
    private JLabel brightnessLabel;
    private JLabel languageLabel;
    private JButton applyButton;
    private JButton backButton;
    private JComboBox<String> languageComboBox;
    private JComboBox<String> resolutionComboBox;

    // ВИПРАВЛЕННЯ: Це повинен бути конструктор, а не метод з void
    public SettingsWindow(GameMenu gameMenu) {
        this.gameMenu = gameMenu;
        // ВИПРАВЛЕННЯ: Змінено на LanguageManager.getCurrentLanguageCode()
        this.initialLanguageCode = LanguageManager.getCurrentLanguageCode();
        this.selectedLanguageCode = initialLanguageCode;
        initializeUI();
        loadCurrentSettings();
        updateLanguageForCurrentWindow();
    }

    private void initializeUI() {
        setTitle(LanguageManager.get("settingsWindow_title"));
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setResizable(true);
        setPreferredSize(new Dimension(700, 800));

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent windowEvent) {
                // ВИПРАВЛЕННЯ: Змінено на LanguageManager.getCurrentLanguageCode()
                LanguageManager.setLanguage(initialLanguageCode);
                gameMenu.showGameMenu();
            }
        });

        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(FANTASY_DARK_STONE);
        mainPanel.setLayout(new BorderLayout(15, 15));
        mainPanel.setBorder(new EmptyBorder(30, 30, 30, 30));

        titleLabel = new JLabel(LanguageManager.get("settingsButton"), SwingConstants.CENTER);
        titleLabel.setFont(FONT_TITLE);
        titleLabel.setForeground(FANTASY_BRONZE_ACCENT);
        titleLabel.setBorder(new EmptyBorder(0, 0, 30, 0));
        mainPanel.add(titleLabel, BorderLayout.NORTH);

        JPanel settingsGridPanel = new JPanel(new GridBagLayout());
        settingsGridPanel.setBackground(FANTASY_DARK_STONE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(15, 10, 15, 10);
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.4;
        gbc.anchor = GridBagConstraints.LINE_START;

        musicVolumeLabel = new JLabel();
        addSettingComponent(settingsGridPanel, gbc, musicVolumeLabel, createFantasySlider());

        effectsVolumeLabel = new JLabel();
        addSettingComponent(settingsGridPanel, gbc, effectsVolumeLabel, createFantasySlider());
        String[] resolutions = {"800x600", "1024x768", "1280x720", "1920x1080 (Full HD)", "Fullscreen"};

        windowSizeLabel = new JLabel();
        resolutionComboBox = createFantasyComboBox(resolutions);
        addSettingComponent(settingsGridPanel, gbc, windowSizeLabel, resolutionComboBox);

        brightnessLabel = new JLabel();
        addSettingComponent(settingsGridPanel, gbc, brightnessLabel, createFantasySlider());

        // !! ВАЖЛИВО: Якщо LanguageManager підтримує більше мов,
        // цей список має бути динамічно сформований на основі доступних мов.
        // Наприклад: LanguageManager.getAvailableLanguageNames()
        String[] languages = {"English"}; // Припустимо, що у вас поки тільки англійська
        languageLabel = new JLabel();
        languageComboBox = createFantasyComboBox(languages);
        languageComboBox.addActionListener(e -> {
            String selectedLangName = (String) languageComboBox.getSelectedItem();
            if ("English".equals(selectedLangName)) {
                selectedLanguageCode = "en";
            }
            // Додайте тут інші мови, наприклад:
            // else if ("Українська".equals(selectedLangName)) {
            //     selectedLanguageCode = "uk";
            // }
            updateLanguageOnSelection();
        });
        addSettingComponent(settingsGridPanel, gbc, languageLabel, languageComboBox);

        JScrollPane scrollPane = new JScrollPane(settingsGridPanel);

        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setBorder(BorderFactory.createLineBorder(FANTASY_BORDER_COLOR, 3));
        scrollPane.getViewport().setBackground(FANTASY_DARK_STONE);

        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 20));
        buttonPanel.setBackground(FANTASY_DARK_STONE);

        applyButton = createFantasyButton(LanguageManager.get("settingsWindow_apply"));
        applyButton.addActionListener(e -> applySettings());
        buttonPanel.add(applyButton);
        backButton = createFantasyButton(LanguageManager.get("settingsWindow_back"));
        backButton.addActionListener(e -> goBackToMenu());
        buttonPanel.add(backButton);

        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void addSettingComponent(JPanel panel, GridBagConstraints gbc, JLabel label, JComponent component) {
        label.setFont(FONT_LABEL);
        label.setForeground(FANTASY_TEXT_LIGHT);
        gbc.gridx = 0;
        gbc.weightx = 0.4;
        panel.add(label, gbc);
        gbc.gridx = 1;
        gbc.weightx = 0.6;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        panel.add(component, gbc);
        gbc.gridy++;
    }

    private void updateLanguageForCurrentWindow() {
        setTitle(LanguageManager.get("settingsWindow_title"));
        titleLabel.setText(LanguageManager.get("settingsButton"));
        musicVolumeLabel.setText(LanguageManager.get("settingsWindow_musicVolume"));
        effectsVolumeLabel.setText(LanguageManager.get("settingsWindow_effectsVolume"));
        windowSizeLabel.setText(LanguageManager.get("settingsWindow_windowSize"));
        brightnessLabel.setText(LanguageManager.get("settingsWindow_brightness"));
        languageLabel.setText(LanguageManager.get("settingsWindow_language"));
        applyButton.setText(LanguageManager.get("settingsWindow_apply"));
        backButton.setText(LanguageManager.get("settingsWindow_back"));

        // ВИПРАВЛЕННЯ: Змінено на LanguageManager.getCurrentLanguageCode()
        String currentLangCode = LanguageManager.getCurrentLanguageCode();
        if ("en".equals(currentLangCode)) {
            languageComboBox.setSelectedItem("English");
        }
        // Додайте інші мовні відповідності, якщо є
        // else if ("uk".equals(currentLangCode)) {
        //     languageComboBox.setSelectedItem("Українська");
        // }

        revalidate();
        repaint();
    }

    private void updateLanguageOnSelection() {
        LanguageManager.setLanguage(selectedLanguageCode);
        updateLanguageForCurrentWindow();

        if (gameMenu != null) {
            // Переконайтеся, що gameMenu має публічний метод updateMenuComponentSizes()
            // або будь-який інший метод для оновлення UI після зміни мови.
            gameMenu.updateMenuComponentSizes();
        }
    }

    private void loadCurrentSettings() {
        // ВИПРАВЛЕННЯ: Змінено на LanguageManager.getCurrentLanguageCode()
        String currentLang = LanguageManager.getCurrentLanguageCode();
        if ("en".equals(currentLang)) {
            languageComboBox.setSelectedItem("English");
        }
        // Додайте інші мовні відповідності, якщо є

        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
        if (gd.getFullScreenWindow() == gameMenu) {
            resolutionComboBox.setSelectedItem("Fullscreen");
        } else {
            Dimension currentSize = gameMenu.getSize();
            String currentResolution = currentSize.width + "x" + currentSize.height;
            boolean found = false;
            for (int i = 0; i < resolutionComboBox.getItemCount(); i++) {
                if (resolutionComboBox.getItemAt(i).startsWith(currentResolution)) {
                    resolutionComboBox.setSelectedIndex(i);
                    found = true;
                    break;
                }
            }
            if (!found) {
                // Встановлення резолюції за замовчуванням, якщо поточна не знайдена в списку
                resolutionComboBox.setSelectedItem("1024x768");
            }
        }
    }

    private JSlider createFantasySlider() {
        JSlider slider = new JSlider(0, 100, 50);
        slider.setBackground(FANTASY_DARK_STONE);
        slider.setForeground(FANTASY_TEXT_LIGHT);
        slider.setMajorTickSpacing(25);
        slider.setMinorTickSpacing(5);
        slider.setPaintTicks(true);
        slider.setPaintLabels(true);
        slider.setFont(FONT_SMALL_LABEL);
        slider.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Встановлення UI менеджерів для повзунка, щоб контролювати кольори
        UIManager.put("Slider.trackForeground", FANTASY_GREY_STONE_LIGHTER);
        UIManager.put("Slider.thumbForeground", FANTASY_BRONZE_ACCENT);
        // Обов'язково викликати updateUI() для застосування змін, якщо ви змінюєте UIManager
        // slider.updateUI(); // Можливо, знадобиться, якщо зміни не застосовуються одразу
        return slider;
    }

    private JComboBox<String> createFantasyComboBox(String[] items) {
        JComboBox<String> comboBox = new JComboBox<>(items);
        comboBox.setFont(FONT_LABEL);
        comboBox.setBackground(FANTASY_GREY_STONE_LIGHTER);
        comboBox.setForeground(FANTASY_TEXT_DARK);
        comboBox.setFocusable(false);
        comboBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
        comboBox.setBorder(new LineBorder(FANTASY_BORDER_COLOR, 1));
        comboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                label.setBackground(isSelected ? FANTASY_BRONZE_ACCENT : FANTASY_GREY_STONE_LIGHTER);
                label.setForeground(isSelected ? FANTASY_TEXT_DARK : FANTASY_TEXT_DARK);
                label.setBorder(new EmptyBorder(8, 8, 8, 8));
                return label;
            }
        });
        return comboBox;
    }

    private JButton createFantasyButton(String text) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(FANTASY_BRONZE_ACCENT);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);
                g2.setColor(FANTASY_BORDER_COLOR);
                g2.setStroke(new BasicStroke(2));
                g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 15, 15);
                g2.setColor(getForeground());
                g2.setFont(getFont());
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(text)) / 2;
                int y = (getHeight() - fm.getHeight()) / 2 + fm.getAscent();
                g2.drawString(text, x, y);
                g2.dispose();
            }
            @Override
            protected void paintBorder(Graphics g) {
            }
        };

        button.setFont(FONT_BUTTON);
        button.setForeground(FANTASY_TEXT_DARK);
        button.setFocusPainted(false);
        button.setContentAreaFilled(false);
        button.setBorderPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMinimumSize(new Dimension(100, 40));
        button.setMaximumSize(new Dimension(300, 60));
        return button;
    }

    private void applySettings() {
        LanguageManager.setLanguage(selectedLanguageCode);
        applyResolutionSettings();
        JOptionPane.showMessageDialog(this,
            LanguageManager.get("settingsWindow_settingsApplied"),
            LanguageManager.get("settingsWindow_saved"),
            JOptionPane.INFORMATION_MESSAGE);

        goBackToMenu();
    }
    private void applyResolutionSettings() {
        String selectedResolution = (String) resolutionComboBox.getSelectedItem();
        if (selectedResolution == null) return;
        GraphicsDevice gd = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();

        // Приховати, звільнити ресурси та скинути undecorated перед застосуванням нових налаштувань
        gameMenu.setVisible(false);
        gameMenu.dispose();
        gameMenu.setUndecorated(false); // Завжди скидати, щоб уникнути проблем, якщо попередньо був повноекранний режим

        if (selectedResolution.equals("Fullscreen")) {
            gameMenu.setUndecorated(true);
            if (gd.isFullScreenSupported()) {
                gd.setFullScreenWindow(gameMenu);
            } else {
                // Якщо повноекранний режим не підтримується, максимізуємо вікно
                gameMenu.setExtendedState(JFrame.MAXIMIZED_BOTH);
                gameMenu.pack();
                gameMenu.setLocationRelativeTo(null);
            }
        } else {
            try {
                String[] dimensions = selectedResolution.split("x");
                int width = Integer.parseInt(dimensions[0]);
                int height = Integer.parseInt(dimensions[1].split(" ")[0]); // Обробка "1920x1080 (Full HD)"
                gameMenu.setPreferredSize(new Dimension(width, height));
                gameMenu.pack();
                gameMenu.setLocationRelativeTo(null);
            } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                System.err.println("Invalid resolution format: " + selectedResolution + ". Error: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Failed to apply resolution: " + selectedResolution + "\nUsing default 1024x768.", "Error", JOptionPane.ERROR_MESSAGE);
                gameMenu.setPreferredSize(new Dimension(1024, 768)); // Повернення до розміру за замовчуванням
                gameMenu.pack();
                gameMenu.setLocationRelativeTo(null);
            }
        }

        gameMenu.setVisible(true);
        gameMenu.revalidate();
        gameMenu.repaint();
    }


    private void goBackToMenu() {
        this.dispose();
        gameMenu.showGameMenu();
    }

    public static void main(String[] args) {
        // Це тестовий блок для запуску вікна налаштувань окремо.
        // У реальній грі, ви будете викликати конструктор SettingsWindow з вашого GameMenu.
        SwingUtilities.invokeLater(() -> {
            try {
                // Рекомендовано для кращого рендерингу шрифтів на деяких системах
                System.setProperty("awt.useSystemAAFontSettings", "on");
                System.setProperty("swing.aatext", "true");
            } catch (Exception e) {
                e.printStackTrace();
            }

            // Створення фіктивного GameMenu для тестування
            // У реальній грі, ви передасте сюди справжній об'єкт GameMenu
            GameMenu dummyMenu = new GameMenu();
            dummyMenu.setVisible(false); // Приховати фіктивне меню, якщо не потрібно його показувати
            new SettingsWindow(dummyMenu); // Виклик конструктора
        });
    }
}
