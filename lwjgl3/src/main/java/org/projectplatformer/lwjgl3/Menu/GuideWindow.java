package org.projectplatformer.lwjgl3.Menu;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;




public class GuideWindow extends JDialog {

        private static final Color FANTASY_DARK_WOOD = new Color(60, 40, 20);
        private static final Color FANTASY_TEXT_LIGHT = new Color(240, 230, 200);
        private static final Color FANTASY_BORDER_COLOR = new Color(40, 30, 10);
        private static final Color FANTASY_BRONZE = new Color(180, 110, 50);

        private static final int BASE_WIDTH = 600;
        private static final int BASE_HEIGHT = 500;

        private Font FONT_TITLE_BASE;
        private Font FONT_TEXT_BASE;

        private JTextArea guideText;
        private JLabel titleLabel;

        public GuideWindow(Frame owner) {
            super(owner, LanguageManager.get("guideTitle"), true);
            initializeFonts();
            initializeUI();
        }

        private void initializeFonts() {
            // Використовуємо стандартні шрифти
            FONT_TITLE_BASE = new Font("Serif", Font.BOLD, 36);
            FONT_TEXT_BASE = new Font("Serif", Font.PLAIN, 18);
            // Можна спробувати також "Arial" або "SansSerif"
        }

        private void initializeUI() {
            setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
            setResizable(true);

            if (getOwner() != null) {
                int ownerWidth = getOwner().getWidth();
                int ownerHeight = getOwner().getHeight();
                setSize((int) (ownerWidth * 0.6), (int) (ownerHeight * 0.7));
            } else {
                setSize(BASE_WIDTH, BASE_HEIGHT);
            }
            setLocationRelativeTo(getOwner());

            JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
            mainPanel.setBackground(FANTASY_DARK_WOOD.brighter());
            mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

            titleLabel = new JLabel(LanguageManager.get("guideTitle"), SwingConstants.CENTER);
            titleLabel.setForeground(FANTASY_BRONZE);
            titleLabel.setBorder(new EmptyBorder(0, 0, 15, 0));
            mainPanel.add(titleLabel, BorderLayout.NORTH);

            guideText = new JTextArea(LanguageManager.get("guideText"));
            guideText.setForeground(FANTASY_TEXT_LIGHT);
            guideText.setWrapStyleWord(true);
            guideText.setLineWrap(true);
            guideText.setEditable(false);
            guideText.setOpaque(false);
            guideText.setBorder(new EmptyBorder(10, 10, 10, 10));

            JScrollPane scrollPane = new JScrollPane(guideText);
            scrollPane.getViewport().setOpaque(false);
            scrollPane.setOpaque(false);
            scrollPane.setBorder(new LineBorder(FANTASY_BORDER_COLOR, 2));
            scrollPane.getVerticalScrollBar().setUnitIncrement(16);

            mainPanel.add(scrollPane, BorderLayout.CENTER);
            add(mainPanel);

            addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    updateGuideWindowComponentSizes();
                }
            });

            updateGuideWindowComponentSizes();
        }

        private void updateGuideWindowComponentSizes() {
            int currentWidth = getWidth();
            int currentHeight = getHeight();

            double scale = Math.min((double) currentWidth / BASE_WIDTH, (double) currentHeight / BASE_HEIGHT);

            Font currentTitleFont = FONT_TITLE_BASE.deriveFont((float) (FONT_TITLE_BASE.getSize2D() * scale));
            titleLabel.setFont(currentTitleFont);

            Font currentTextFont = FONT_TEXT_BASE.deriveFont((float) (FONT_TEXT_BASE.getSize2D() * scale));
            guideText.setFont(currentTextFont);

            revalidate();
            repaint();
        }
    }
