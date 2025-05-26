package org.projectplatformer.lwjgl3.Menu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;


    public class SaveSelectionDialog extends JDialog {

        private static final Color FANTASY_DARK_WOOD = new Color(60, 40, 20);
        private static final Color FANTASY_BROWN_LEATHER = new Color(100, 70, 40);
        private static final Color FANTASY_BRONZE = new Color(180, 110, 50);
        private static final Color FANTASY_TEXT_LIGHT = new Color(240, 230, 200);
        private static final Color FANTASY_BORDER_COLOR = new Color(40, 30, 10);
        private static final Color BUTTON_HOVER_COLOR_LIGHT = new Color(130, 95, 50);
        private static final Color BUTTON_PRESSED_COLOR_DARK = new Color(70, 45, 15);

        private static final int DIALOG_BASE_WIDTH = 800;
        private static final int DIALOG_BASE_HEIGHT = 600;

        private static Font FONT_DIALOG_TITLE_BASE;
        private static Font FONT_SAVE_BUTTON_BASE;
        private static Font FONT_SAVE_DETAILS_BASE;

        static {
            FONT_DIALOG_TITLE_BASE = new Font("Arial", Font.BOLD, 48);
            FONT_SAVE_BUTTON_BASE = new Font("Arial", Font.BOLD, 24);
            FONT_SAVE_DETAILS_BASE = new Font("Arial", Font.PLAIN, 16);
        }

        private GameMenu parentMenu;
        private JLabel dialogTitle;
        private JButton[] saveButtons;

        public SaveSelectionDialog(GameMenu parent) {
            super(parent, LanguageManager.get("loadGame_title"), true);
            this.parentMenu = parent;
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

            dialogTitle = new JLabel(LanguageManager.get("loadGame_title"), SwingConstants.CENTER);
            dialogTitle.setForeground(FANTASY_BRONZE);
            dialogTitle.setBorder(new EmptyBorder(10, 0, 40, 0));
            mainPanel.add(dialogTitle, BorderLayout.NORTH);

            JPanel buttonsPanel = new JPanel(new GridLayout(2, 2, 20, 20));
            buttonsPanel.setOpaque(false);
            buttonsPanel.setBorder(new EmptyBorder(0, 0, 20, 0));

            String[] saveFiles = {
                "Save 1",
                "Save 2",
                "Save 3",
                "Empty Slot<br>New Game"
            };

            saveButtons = new JButton[saveFiles.length];
            for (int i = 0; i < saveFiles.length; i++) {
                final String saveInfo = saveFiles[i];
                JButton saveButton = createFantasyButton("<html><center>" + saveInfo + "</center></html>", FANTASY_BROWN_LEATHER);
                saveButton.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        // Dispose of the current dialog
                        dispose();

                        if (saveInfo.contains("Empty Slot")) {
                            int choice = JOptionPane.showConfirmDialog(
                                parentMenu, // Use parentMenu for confirmation dialog
                                LanguageManager.get("newGameConfirm_message"),
                                LanguageManager.get("newGameConfirm_title"),
                                JOptionPane.YES_NO_OPTION,
                                JOptionPane.WARNING_MESSAGE
                            );
                            if (choice == JOptionPane.YES_OPTION) {
                                // Hide the main menu, then show GameWorldWindow
                                parentMenu.setVisible(false); // Hide GameMenu
                                // CORRECTED LINE 95: Pass parentMenu to the constructor
                                new GameWorldWindow(parentMenu); // Create and show GameWorldWindow
                            }
                        } else {
                            JOptionPane.showMessageDialog(
                                parentMenu,
                                LanguageManager.get("loadGame_loading") + ": " + saveInfo.split("<br>")[0],
                                LanguageManager.get("loadGame_title"),
                                JOptionPane.INFORMATION_MESSAGE
                            );
                            // Hide the main menu, then show GameWorldWindow
                            parentMenu.setVisible(false); // Hide GameMenu
                            // CORRECTED LINE 106: Pass parentMenu to the constructor
                            new GameWorldWindow(parentMenu); // Create and show GameWorldWindow
                        }
                    }
                });
                saveButtons[i] = saveButton;
                buttonsPanel.add(saveButton);
            }

            mainPanel.add(buttonsPanel, BorderLayout.CENTER);

            JButton backButton = createFantasyButton(LanguageManager.get("backButton"), FANTASY_BRONZE.darker());
            backButton.addActionListener(e -> dispose());

            JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
            southPanel.setOpaque(false);
            southPanel.add(backButton);
            mainPanel.add(southPanel, BorderLayout.SOUTH);

            add(mainPanel);
            pack();
            updateComponentSizes();
        }

        private void updateComponentSizes() {
            int currentWidth = getWidth();
            int currentHeight = getHeight();

            double scaleX = (double) currentWidth / DIALOG_BASE_WIDTH;
            double scaleY = (double) currentHeight / DIALOG_BASE_HEIGHT;
            double overallScale = Math.min(scaleX, scaleY);

            dialogTitle.setFont(FONT_DIALOG_TITLE_BASE.deriveFont((float) (FONT_DIALOG_TITLE_BASE.getSize2D() * overallScale)));

            Font currentSaveButtonFont = FONT_SAVE_BUTTON_BASE.deriveFont((float) (FONT_SAVE_BUTTON_BASE.getSize2D() * overallScale));

            for (JButton button : saveButtons) {
                button.setFont(currentSaveButtonFont);

                int baseSize = 200;
                int newSize = (int) (baseSize * overallScale);
                Dimension buttonDim = new Dimension(newSize, newSize);
                button.setPreferredSize(buttonDim);
                button.setMinimumSize(buttonDim);
                button.setMaximumSize(buttonDim);
            }

            if (getComponents().length > 0) {
                JPanel mainContentPanel = (JPanel) getContentPane().getComponent(0);
                JPanel southPanel = (JPanel) mainContentPanel.getComponent(mainContentPanel.getComponentCount() - 1);
                if (southPanel.getComponentCount() > 0 && southPanel.getComponent(0) instanceof JButton) {
                    JButton backButton = (JButton) southPanel.getComponent(0);
                    Font currentBackButtonFont = FONT_SAVE_BUTTON_BASE.deriveFont((float) (FONT_SAVE_BUTTON_BASE.getSize2D() * overallScale * 0.7));
                    backButton.setFont(currentBackButtonFont);
                    Dimension backButtonBaseSize = new Dimension(150, 50);
                    Dimension newBackButtonSize = new Dimension(
                        (int) (backButtonBaseSize.width * overallScale),
                        (int) (backButtonBaseSize.height * overallScale)
                    );
                    backButton.setPreferredSize(newBackButtonSize);
                    backButton.setMinimumSize(newBackButtonSize);
                    backButton.setMaximumSize(newBackButtonSize);
                }
            }

            revalidate();
            repaint();
        }

        private JButton createFantasyButton(String text, Color bgColor) {
            FantasyButton button = new FantasyButton(text, bgColor, FONT_SAVE_BUTTON_BASE);
            button.setPreferredSize(new Dimension(200, 200));
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
