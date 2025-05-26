package org.projectplatformer.lwjgl3.Menu;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;

public class EquipmentWindow extends JDialog {

    private static final Color FANTASY_DARK_WOOD = new Color(60, 40, 20);
    private static final Color FANTASY_BROWN_LEATHER = new Color(100, 70, 40);
    private static final Color FANTASY_BRONZE = new Color(180, 110, 50);
    private static final Color FANTASY_TEXT_LIGHT = new Color(240, 230, 200);
    private static final Color FANTASY_BORDER_COLOR = new Color(40, 30, 10);
    private static final Color BUTTON_HOVER_COLOR_LIGHT = new Color(130, 95, 50);
    private static final Color BUTTON_PRESSED_COLOR_DARK = new Color(70, 45, 15);

    private static final int DIALOG_BASE_WIDTH = 1000;
    private static final int DIALOG_BASE_HEIGHT = 700;

    private static Font FONT_DIALOG_TITLE_BASE;
    private static Font FONT_SECTION_TITLE_BASE;
    private static Font FONT_BUTTON_BASE;
    private static Font FONT_ITEM_TEXT_BASE;
    private static Font FONT_ITEM_NAME_BASE;

    private static Image SPEAR_IMAGE_BASE;
    private static Image BOW_IMAGE_BASE;
    private static Image SWORD_IMAGE_BASE; // NEW: Image for sword

    private static final int ITEM_IMAGE_BASE_SIZE = 75;

    static {
        try {
            System.setProperty("awt.useSystemAAFontSettings", "on");
            System.setProperty("swing.aatext", "true");
        } catch (Exception e) {
            e.printStackTrace();
        }
        FONT_DIALOG_TITLE_BASE = new Font("Arial", Font.BOLD, 48);
        FONT_SECTION_TITLE_BASE = new Font("Arial", Font.BOLD, 36);
        FONT_BUTTON_BASE = new Font("Arial", Font.BOLD, 24);

        FONT_ITEM_TEXT_BASE = new Font("Arial", Font.PLAIN, 14);
        FONT_ITEM_NAME_BASE = new Font("Arial", Font.BOLD, 18);

        try {
            // Load Spear Image - CORRECTED PATH
            String spearImagePath = "/Spear equipped.png"; // Changed from "/images/Spear equipped.png"
            InputStream spearIs = EquipmentWindow.class.getResourceAsStream(spearImagePath);
            if (spearIs != null) {
                SPEAR_IMAGE_BASE = new ImageIcon(ImageIO.read(spearIs)).getImage();
                System.out.println("Spear image loaded successfully from: " + spearImagePath);
                spearIs.close();
            } else {
                System.err.println("Spear image not found: " + spearImagePath);
            }

            // Load Bow Image - CORRECTED PATH
            String bowImagePath = "/Bow equipped.png"; // Changed from "/images/Bow equipped.png"
            InputStream bowIs = EquipmentWindow.class.getResourceAsStream(bowImagePath);
            if (bowIs != null) {
                BOW_IMAGE_BASE = new ImageIcon(ImageIO.read(bowIs)).getImage();
                System.out.println("Bow image loaded successfully from: " + bowImagePath);
                bowIs.close();
            } else {
                System.err.println("Bow image not found: " + bowImagePath);
            }

            // NEW: Load Sword Image - CORRECTED PATH
            String swordImagePath = "/Sword equipped1.png"; // Changed from "/images/Sword equipped1.png"
            InputStream swordIs = EquipmentWindow.class.getResourceAsStream(swordImagePath);
            if (swordIs != null) {
                SWORD_IMAGE_BASE = new ImageIcon(ImageIO.read(swordIs)).getImage();
                System.out.println("Sword image loaded successfully from: " + swordImagePath);
                swordIs.close();
            } else {
                System.err.println("Sword image not found: " + swordImagePath);
            }

        } catch (IOException e) {
            System.err.println("Error loading item images: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private GameWorldWindow parentWindow;
    private JLabel dialogTitle;
    private JLabel rangedWeaponsTitle;
    private JLabel meleeWeaponsTitle;
    private JButton backButton;

    private JLabel spearImageLabel;
    private JLabel spearNameLabel;
    private JTextArea spearStatsTextArea;

    private JLabel bowImageLabel;
    private JLabel bowNameLabel;
    private JTextArea bowStatsTextArea;

    // NEW: Components for the sword
    private JLabel swordImageLabel;
    private JLabel swordNameLabel;
    private JTextArea swordStatsTextArea;

    private JPanel meleePanel;
    private JLabel meleePlaceholder; // This will now be replaced by the sword display

    public EquipmentWindow(GameWorldWindow parent) {
        super(parent, LanguageManager.get("equipmentWindow_title"), true);
        this.parentWindow = parent;
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setResizable(true);
        setPreferredSize(new Dimension(DIALOG_BASE_WIDTH, DIALOG_BASE_HEIGHT));
        setLocationRelativeTo(parent);

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                updateComponentSizes();
            }
        });

        JPanel mainPanel = new JPanel(new BorderLayout(30, 20));
        mainPanel.setBackground(FANTASY_DARK_WOOD);
        mainPanel.setBorder(new EmptyBorder(40, 60, 30, 60));

        dialogTitle = new JLabel(LanguageManager.get("equipmentWindow_title"), SwingConstants.CENTER);
        dialogTitle.setForeground(FANTASY_BRONZE);
        dialogTitle.setFont(FONT_DIALOG_TITLE_BASE);
        dialogTitle.setBorder(new EmptyBorder(10, 0, 40, 0));
        mainPanel.add(dialogTitle, BorderLayout.NORTH);

        JPanel contentPanel = new JPanel(new GridLayout(1, 2, 30, 0));
        contentPanel.setOpaque(false);

        // Ranged Weapons Section
        JPanel rangedPanel = new JPanel(new BorderLayout(10, 10));
        rangedPanel.setOpaque(false);
        rangedPanel.setBorder(new LineBorder(FANTASY_BORDER_COLOR, 3, true));
        rangedPanel.setBackground(FANTASY_BROWN_LEATHER.darker().darker());

        rangedWeaponsTitle = new JLabel(LanguageManager.get("equipmentWindow_rangedWeapons"), SwingConstants.CENTER);
        rangedWeaponsTitle.setForeground(FANTASY_BRONZE);
        rangedWeaponsTitle.setFont(FONT_SECTION_TITLE_BASE);
        rangedWeaponsTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        rangedPanel.add(rangedWeaponsTitle, BorderLayout.NORTH);

        JPanel rangedItemsListPanel = new JPanel();
        rangedItemsListPanel.setLayout(new BoxLayout(rangedItemsListPanel, BoxLayout.Y_AXIS));
        rangedItemsListPanel.setOpaque(false);
        rangedItemsListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));


        // --- Spear Item Display Block ---
        JPanel spearDisplayPanel = new JPanel(new GridBagLayout());
        spearDisplayPanel.setOpaque(false);
        spearDisplayPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        GridBagConstraints gbcSpear = new GridBagConstraints();
        gbcSpear.insets = new Insets(5, 5, 5, 5);
        gbcSpear.fill = GridBagConstraints.HORIZONTAL;

        spearImageLabel = new JLabel();
        spearImageLabel.setBorder(BorderFactory.createLineBorder(FANTASY_BORDER_COLOR.darker(), 2));
        gbcSpear.gridx = 0; gbcSpear.gridy = 0; gbcSpear.gridheight = 2;
        gbcSpear.anchor = GridBagConstraints.NORTHWEST; gbcSpear.weightx = 0;
        spearDisplayPanel.add(spearImageLabel, gbcSpear);

        spearNameLabel = new JLabel("Spear");
        spearNameLabel.setForeground(FANTASY_TEXT_LIGHT);
        spearNameLabel.setFont(FONT_ITEM_NAME_BASE);
        gbcSpear.gridx = 1; gbcSpear.gridy = 0; gbcSpear.gridheight = 1;
        gbcSpear.weightx = 1.0; gbcSpear.anchor = GridBagConstraints.WEST;
        spearDisplayPanel.add(spearNameLabel, gbcSpear);

        spearStatsTextArea = new JTextArea("Damage: 30\nRange: Medium\nAttack Speed: Medium");
        spearStatsTextArea.setEditable(false); spearStatsTextArea.setOpaque(false);
        spearStatsTextArea.setForeground(FANTASY_TEXT_LIGHT); spearStatsTextArea.setFont(FONT_ITEM_TEXT_BASE);
        spearStatsTextArea.setWrapStyleWord(true); spearStatsTextArea.setLineWrap(true);
        spearStatsTextArea.setMinimumSize(new Dimension(100, 30));
        spearStatsTextArea.setPreferredSize(new Dimension(150, 45));
        spearStatsTextArea.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane spearStatsScrollPane = new JScrollPane(spearStatsTextArea);
        spearStatsScrollPane.setOpaque(false); spearStatsScrollPane.getViewport().setOpaque(false);
        spearStatsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        spearStatsScrollPane.getViewport().setBackground(FANTASY_BROWN_LEATHER.darker().darker());

        gbcSpear.gridx = 1; gbcSpear.gridy = 1; gbcSpear.weighty = 1.0;
        gbcSpear.fill = GridBagConstraints.BOTH; gbcSpear.anchor = GridBagConstraints.NORTHWEST;
        spearDisplayPanel.add(spearStatsScrollPane, gbcSpear);

        rangedItemsListPanel.add(spearDisplayPanel);
        rangedItemsListPanel.add(Box.createVerticalStrut(15));

        // --- Bow Item Display Block ---
        JPanel bowDisplayPanel = new JPanel(new GridBagLayout());
        bowDisplayPanel.setOpaque(false);
        bowDisplayPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        GridBagConstraints gbcBow = new GridBagConstraints();
        gbcBow.insets = new Insets(5, 5, 5, 5);
        gbcBow.fill = GridBagConstraints.HORIZONTAL;

        bowImageLabel = new JLabel();
        bowImageLabel.setBorder(BorderFactory.createLineBorder(FANTASY_BORDER_COLOR.darker(), 2));
        gbcBow.gridx = 0; gbcBow.gridy = 0; gbcBow.gridheight = 2;
        gbcBow.anchor = GridBagConstraints.NORTHWEST; gbcBow.weightx = 0;
        bowDisplayPanel.add(bowImageLabel, gbcBow);

        bowNameLabel = new JLabel("Bow");
        bowNameLabel.setForeground(FANTASY_TEXT_LIGHT);
        bowNameLabel.setFont(FONT_ITEM_NAME_BASE);
        gbcBow.gridx = 1; gbcBow.gridy = 0; gbcBow.gridheight = 1;
        gbcBow.weightx = 1.0; gbcBow.anchor = GridBagConstraints.WEST;
        bowDisplayPanel.add(bowNameLabel, gbcBow);

        bowStatsTextArea = new JTextArea("Damage: 20\nRange: Long\nAttack Speed: Fast");
        bowStatsTextArea.setEditable(false); bowStatsTextArea.setOpaque(false);
        bowStatsTextArea.setForeground(FANTASY_TEXT_LIGHT); bowStatsTextArea.setFont(FONT_ITEM_TEXT_BASE);
        bowStatsTextArea.setWrapStyleWord(true); bowStatsTextArea.setLineWrap(true);
        bowStatsTextArea.setMinimumSize(new Dimension(100, 30));
        bowStatsTextArea.setPreferredSize(new Dimension(150, 45));
        bowStatsTextArea.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane bowStatsScrollPane = new JScrollPane(bowStatsTextArea);
        bowStatsScrollPane.setOpaque(false); bowStatsScrollPane.getViewport().setOpaque(false);
        bowStatsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        bowStatsScrollPane.getViewport().setBackground(FANTASY_BROWN_LEATHER.darker().darker());

        gbcBow.gridx = 1; gbcBow.gridy = 1; gbcBow.weighty = 1.0;
        gbcBow.fill = GridBagConstraints.BOTH; gbcBow.anchor = GridBagConstraints.NORTHWEST;
        bowDisplayPanel.add(bowStatsScrollPane, gbcBow);

        rangedItemsListPanel.add(bowDisplayPanel);

        JScrollPane rangedScrollPane = new JScrollPane(rangedItemsListPanel);
        rangedScrollPane.setOpaque(false);
        rangedScrollPane.getViewport().setOpaque(false);
        rangedScrollPane.setBorder(BorderFactory.createEmptyBorder());
        rangedScrollPane.getViewport().setBackground(FANTASY_BROWN_LEATHER.darker().darker());
        rangedScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
        rangedScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
        rangedPanel.add(rangedScrollPane, BorderLayout.CENTER);

        contentPanel.add(rangedPanel);

        // Melee Weapons Section (MODIFIED to add Sword display)
        meleePanel = new JPanel(new BorderLayout(10, 10));
        meleePanel.setOpaque(false);
        meleePanel.setBorder(new LineBorder(FANTASY_BORDER_COLOR, 3, true));
        meleePanel.setBackground(FANTASY_BROWN_LEATHER.darker().darker());

        meleeWeaponsTitle = new JLabel(LanguageManager.get("equipmentWindow_meleeWeapons"), SwingConstants.CENTER);
        meleeWeaponsTitle.setForeground(FANTASY_BRONZE);
        meleeWeaponsTitle.setFont(FONT_SECTION_TITLE_BASE);
        meleeWeaponsTitle.setBorder(new EmptyBorder(15, 0, 15, 0));
        meleePanel.add(meleeWeaponsTitle, BorderLayout.NORTH);

        // NEW: Panel to hold melee items vertically
        JPanel meleeItemsListPanel = new JPanel();
        meleeItemsListPanel.setLayout(new BoxLayout(meleeItemsListPanel, BoxLayout.Y_AXIS));
        meleeItemsListPanel.setOpaque(false);
        meleeItemsListPanel.setBorder(new EmptyBorder(10, 10, 10, 10));

        // --- Sword Item Display Block ---
        JPanel swordDisplayPanel = new JPanel(new GridBagLayout());
        swordDisplayPanel.setOpaque(false);
        swordDisplayPanel.setBorder(new EmptyBorder(10, 0, 10, 0)); // Padding for this specific item

        GridBagConstraints gbcSwordDisplay = new GridBagConstraints(); // Renamed to avoid conflict
        gbcSwordDisplay.insets = new Insets(5, 5, 5, 5);
        gbcSwordDisplay.fill = GridBagConstraints.HORIZONTAL;

        swordImageLabel = new JLabel();
        swordImageLabel.setBorder(BorderFactory.createLineBorder(FANTASY_BORDER_COLOR.darker(), 2));
        gbcSwordDisplay.gridx = 0; gbcSwordDisplay.gridy = 0; gbcSwordDisplay.gridheight = 2;
        gbcSwordDisplay.anchor = GridBagConstraints.NORTHWEST; gbcSwordDisplay.weightx = 0;
        swordDisplayPanel.add(swordImageLabel, gbcSwordDisplay);

        swordNameLabel = new JLabel("Sword");
        swordNameLabel.setForeground(FANTASY_TEXT_LIGHT);
        swordNameLabel.setFont(FONT_ITEM_NAME_BASE);
        gbcSwordDisplay.gridx = 1; gbcSwordDisplay.gridy = 0; gbcSwordDisplay.gridheight = 1;
        gbcSwordDisplay.weightx = 1.0; gbcSwordDisplay.anchor = GridBagConstraints.WEST;
        swordDisplayPanel.add(swordNameLabel, gbcSwordDisplay);

        swordStatsTextArea = new JTextArea("Damage: 25\nType: Slashing\nWeight: Medium");
        swordStatsTextArea.setEditable(false); swordStatsTextArea.setOpaque(false);
        swordStatsTextArea.setForeground(FANTASY_TEXT_LIGHT); swordStatsTextArea.setFont(FONT_ITEM_TEXT_BASE);
        swordStatsTextArea.setWrapStyleWord(true); swordStatsTextArea.setLineWrap(true);
        swordStatsTextArea.setMinimumSize(new Dimension(100, 30));
        swordStatsTextArea.setPreferredSize(new Dimension(150, 45));
        swordStatsTextArea.setBorder(new EmptyBorder(0, 0, 0, 0));

        JScrollPane swordStatsScrollPane = new JScrollPane(swordStatsTextArea);
        swordStatsScrollPane.setOpaque(false); swordStatsScrollPane.getViewport().setOpaque(false);
        swordStatsScrollPane.setBorder(BorderFactory.createEmptyBorder());
        swordStatsScrollPane.getViewport().setBackground(FANTASY_BROWN_LEATHER.darker().darker());

        gbcSwordDisplay.gridx = 1; gbcSwordDisplay.gridy = 1; gbcSwordDisplay.weighty = 1.0;
        gbcSwordDisplay.fill = GridBagConstraints.BOTH; gbcSwordDisplay.anchor = GridBagConstraints.NORTHWEST;
        swordDisplayPanel.add(swordStatsScrollPane, gbcSwordDisplay);

        meleeItemsListPanel.add(swordDisplayPanel);
        // meleeItemsListPanel.add(Box.createVerticalStrut(15)); // Add separator if more items are planned

        // Add meleeItemsListPanel to a JScrollPane (optional, if you want it to scroll)
        JScrollPane meleeScrollPane = new JScrollPane(meleeItemsListPanel);
        meleeScrollPane.setOpaque(false);
        meleeScrollPane.getViewport().setOpaque(false);
        meleeScrollPane.setBorder(BorderFactory.createEmptyBorder());
        meleeScrollPane.getViewport().setBackground(FANTASY_BROWN_LEATHER.darker().darker());
        meleeScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER); // Hide scrollbar for now
        meleeScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER); // Hide scrollbar for now

        meleePanel.add(meleeScrollPane, BorderLayout.CENTER); // Add the scroll pane to the melee panel


        contentPanel.add(meleePanel);

        mainPanel.add(contentPanel, BorderLayout.CENTER);

        backButton = createFantasyButton(LanguageManager.get("backButton"), FANTASY_BRONZE.darker(), FONT_BUTTON_BASE);
        backButton.addActionListener(e -> dispose());

        JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        southPanel.setOpaque(false);
        southPanel.setBorder(new EmptyBorder(20, 0, 10, 0));
        southPanel.add(backButton);
        mainPanel.add(southPanel, BorderLayout.SOUTH);

        add(mainPanel);
        pack();
        updateComponentSizes();
        setVisible(true);
    }

    private void updateComponentSizes() {
        int currentWidth = getWidth();
        int currentHeight = getHeight();

        double scaleX = (double) currentWidth / DIALOG_BASE_WIDTH;
        double scaleY = (double) currentHeight / DIALOG_BASE_HEIGHT;
        double overallScale = Math.min(scaleX, scaleY);

        dialogTitle.setFont(FONT_DIALOG_TITLE_BASE.deriveFont((float) (FONT_DIALOG_TITLE_BASE.getSize2D() * overallScale)));
        rangedWeaponsTitle.setFont(FONT_SECTION_TITLE_BASE.deriveFont((float) (FONT_SECTION_TITLE_BASE.getSize2D() * overallScale)));
        meleeWeaponsTitle.setFont(FONT_SECTION_TITLE_BASE.deriveFont((float) (FONT_SECTION_TITLE_BASE.getSize2D() * overallScale)));

        // Update sizes for Spear components
        if (SPEAR_IMAGE_BASE != null) {
            int targetImageSize = (int) (ITEM_IMAGE_BASE_SIZE * overallScale);
            Image scaledImage = SPEAR_IMAGE_BASE.getScaledInstance(targetImageSize, targetImageSize, Image.SCALE_SMOOTH);
            spearImageLabel.setIcon(new ImageIcon(scaledImage));
        }
        spearNameLabel.setFont(FONT_ITEM_NAME_BASE.deriveFont((float) (FONT_ITEM_NAME_BASE.getSize2D() * overallScale)));
        spearStatsTextArea.setFont(FONT_ITEM_TEXT_BASE.deriveFont((float) (FONT_ITEM_TEXT_BASE.getSize2D() * overallScale)));

        // Update sizes for Bow components
        if (BOW_IMAGE_BASE != null) {
            int targetImageSize = (int) (ITEM_IMAGE_BASE_SIZE * overallScale);
            Image scaledImage = BOW_IMAGE_BASE.getScaledInstance(targetImageSize, targetImageSize, Image.SCALE_SMOOTH);
            bowImageLabel.setIcon(new ImageIcon(scaledImage));
        }
        bowNameLabel.setFont(FONT_ITEM_NAME_BASE.deriveFont((float) (FONT_ITEM_NAME_BASE.getSize2D() * overallScale)));
        bowStatsTextArea.setFont(FONT_ITEM_TEXT_BASE.deriveFont((float) (FONT_ITEM_TEXT_BASE.getSize2D() * overallScale)));

        // NEW: Update sizes for Sword components
        if (SWORD_IMAGE_BASE != null) {
            int targetImageSize = (int) (ITEM_IMAGE_BASE_SIZE * overallScale);
            Image scaledImage = SWORD_IMAGE_BASE.getScaledInstance(targetImageSize, targetImageSize, Image.SCALE_SMOOTH);
            swordImageLabel.setIcon(new ImageIcon(scaledImage));
        }
        swordNameLabel.setFont(FONT_ITEM_NAME_BASE.deriveFont((float) (FONT_ITEM_NAME_BASE.getSize2D() * overallScale)));
        swordStatsTextArea.setFont(FONT_ITEM_TEXT_BASE.deriveFont((float) (FONT_ITEM_TEXT_BASE.getSize2D() * overallScale)));


        // meleePlaceholder is no longer directly added, so no need to update its font here unless re-added
        // If you plan to add the placeholder back for empty slots, uncomment and adjust:
        // if (meleePlaceholder != null) {
        //     meleePlaceholder.setFont(FONT_ITEM_TEXT_BASE.deriveFont((float) (FONT_ITEM_TEXT_BASE.getSize2D() * overallScale)));
        // }

        Font currentButtonFont = FONT_BUTTON_BASE.deriveFont((float) (FONT_BUTTON_BASE.getSize2D() * overallScale * 0.8));
        backButton.setFont(currentButtonFont);

        Dimension baseBackBtnSize = new Dimension(200, 50);
        Dimension newBackBtnSize = new Dimension(
            (int) (baseBackBtnSize.width * overallScale),
            (int) (baseBackBtnSize.height * overallScale)
        );
        backButton.setPreferredSize(newBackBtnSize);
        backButton.setMinimumSize(newBackBtnSize);
        backButton.setMaximumSize(newBackBtnSize);

        revalidate();
        repaint();
    }

    private JButton createFantasyButton(String text, Color bgColor, Font font) {
        FantasyButton button = new FantasyButton(text, bgColor, font);
        button.setPreferredSize(new Dimension(200, 60));
        return button;
    }

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
