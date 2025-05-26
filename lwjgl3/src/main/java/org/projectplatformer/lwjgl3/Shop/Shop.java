package org.projectplatformer.lwjgl3.Shop;

import org.projectplatformer.lwjgl3.SaveData;

import javax.swing.*;
import java.awt.*;

public class Shop extends JFrame {
    private SaveData saveData;
    private boolean hasKey = false;
    private boolean swordBought = true;
    private boolean spearBought = false;
    private boolean bowBought = false;
    private boolean chestOpened = false;

    private JLabel coinsLabel;
    private JPanel weaponsPanel;
    private JPanel healsPanel;

    private JLabel superText;
    private JLabel propositionText;
    private JLabel findText;
    private JLabel aKeyText;

    public Shop(SaveData saveData) {
        this.saveData = saveData;

        setTitle("PP-Shop");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(null);
        setResizable(false);
        setLocationRelativeTo(null);
        getContentPane().setBackground(new Color(102, 51, 0));

        coinsLabel = new JLabel("Coins: " + saveData.getCoins());
        coinsLabel.setForeground(Color.WHITE);
        coinsLabel.setFont(new Font("Arial", Font.BOLD, 16));
        coinsLabel.setBounds(700, 10, 100, 30);
        add(coinsLabel);

        JLabel titleLabel = new JLabel("WELCOME TO THE PP-SHOP!", SwingConstants.CENTER);
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 26));
        titleLabel.setBounds(200, 20, 400, 40);
        add(titleLabel);

        int buttonSize = 130;
        int chestSize = 160;
        int iconSmall = 40;

        JButton swordButton = new JButton(scaleIcon("sword.png", 130, 130));
        swordButton.setBounds(130, 80, 130, 130);
        swordButton.setContentAreaFilled(false);
        swordButton.setBorderPainted(false);
        swordButton.setFocusPainted(false);
        add(swordButton);

        JButton spearButton = new JButton(scaleIcon("spear_dark.png", buttonSize, buttonSize));
        spearButton.setBounds(330, 80, buttonSize, buttonSize);
        spearButton.setContentAreaFilled(false);
        spearButton.setBorderPainted(false);
        spearButton.setFocusPainted(false);
        add(spearButton);

        JLabel spearPrice = new JLabel("3 COINS", SwingConstants.CENTER);
        spearPrice.setForeground(Color.WHITE);
        spearPrice.setFont(new Font("Arial", Font.BOLD, 20));
        spearPrice.setBounds(330, 165, buttonSize, buttonSize);
        add(spearPrice);

        JButton bowButton = new JButton(scaleIcon("bow_dark.png", buttonSize, buttonSize));
        bowButton.setBounds(530, 80, buttonSize, buttonSize);
        bowButton.setContentAreaFilled(false);
        bowButton.setBorderPainted(false);
        bowButton.setFocusPainted(false);
        add(bowButton);

        JLabel bowPrice = new JLabel("5 COINS", SwingConstants.CENTER);
        bowPrice.setForeground(Color.WHITE);
        bowPrice.setFont(new Font("Arial", Font.BOLD, 20));
        bowPrice.setBounds(530, 165, buttonSize, buttonSize);
        add(bowPrice);

        JButton chestButton = new JButton(scaleIcon("chest_closed.png", chestSize, chestSize));
        chestButton.setBounds(320, 300, chestSize, chestSize);
        chestButton.setContentAreaFilled(false);
        chestButton.setBorderPainted(false);
        chestButton.setFocusPainted(false);
        add(chestButton);

        // Ліва сторона текст
        superText = new JLabel("SUPER", SwingConstants.RIGHT);
        superText.setForeground(Color.WHITE);
        superText.setFont(new Font("Arial", Font.BOLD, 18));
        superText.setBounds(235, 360, 80, 20);
        add(superText);

        propositionText = new JLabel("PROPOSITION", SwingConstants.RIGHT);
        propositionText.setForeground(Color.WHITE);
        propositionText.setFont(new Font("Arial", Font.BOLD, 16));
        propositionText.setBounds(165, 380, 150, 20);
        add(propositionText);

        // Права сторона текст
        findText = new JLabel("JUST", SwingConstants.LEFT);
        findText.setForeground(Color.WHITE);
        findText.setFont(new Font("Arial", Font.BOLD, 18));
        findText.setBounds(482, 360, 80, 20);
        add(findText);

        aKeyText = new JLabel("FIND A KEY", SwingConstants.LEFT);
        aKeyText.setForeground(Color.WHITE);
        aKeyText.setFont(new Font("Arial", Font.BOLD, 16));
        aKeyText.setBounds(482, 380, 100, 20);
        add(aKeyText);

        JLabel weaponsTitle = new JLabel("Inventory:");
        weaponsTitle.setForeground(Color.WHITE);
        weaponsTitle.setBounds(93, 518, 80, 20);
        add(weaponsTitle);

        weaponsPanel = new JPanel();
        weaponsPanel.setBounds(150, 510, 300, 50);
        weaponsPanel.setBackground(new Color(102, 51, 0));
        weaponsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        add(weaponsPanel);
        weaponsPanel.add(new JLabel(scaleIcon("sword.png", iconSmall, iconSmall)));

        JLabel healsTitle = new JLabel("Heals:");
        healsTitle.setForeground(Color.WHITE);
        healsTitle.setBounds(515, 518, 80, 20);
        add(healsTitle);

        healsPanel = new JPanel();
        healsPanel.setBounds(550, 510, 200, 50);
        healsPanel.setBackground(new Color(102, 51, 0));
        healsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 0));
        add(healsPanel);

        spearButton.addActionListener(e -> {
            if (!spearBought && saveData.getCoins() >= 3) {
                saveData.spendCoins(3);
                spearBought = true;
                updateCoinsLabel();
                spearButton.setIcon(scaleIcon("spear.png", buttonSize, buttonSize));
                weaponsPanel.add(new JLabel(scaleIcon("spear.png", iconSmall, iconSmall)));
                weaponsPanel.revalidate();
                weaponsPanel.repaint();
                spearPrice.setVisible(false);
                JOptionPane.showMessageDialog(this, "Spear bought!");
            } else if (spearBought) {
                JOptionPane.showMessageDialog(this, "You already bought the spear!");
            } else {
                JOptionPane.showMessageDialog(this, "Not enough coins!");
            }
        });

        bowButton.addActionListener(e -> {
            if (!bowBought && saveData.getCoins() >= 5) {
                saveData.spendCoins(5);
                bowBought = true;
                updateCoinsLabel();
                bowButton.setIcon(scaleIcon("bow.png", buttonSize, buttonSize));
                weaponsPanel.add(new JLabel(scaleIcon("bow.png", iconSmall, iconSmall)));
                weaponsPanel.revalidate();
                weaponsPanel.repaint();
                bowPrice.setVisible(false);
                JOptionPane.showMessageDialog(this, "Bow bought!");
            } else if (bowBought) {
                JOptionPane.showMessageDialog(this, "You already bought the bow!");
            } else {
                JOptionPane.showMessageDialog(this, "Not enough coins!");
            }
        });

        chestButton.addActionListener(e -> {
            if (!hasKey) {
                JOptionPane.showMessageDialog(this, "Find the key first!");
            } else if (!chestOpened) {
                chestOpened = true;
                chestButton.setIcon(scaleIcon("chest_opened.png", chestSize, chestSize));
                healsPanel.add(new JLabel(scaleIcon("beer.png", iconSmall, iconSmall)));
                healsPanel.revalidate();
                healsPanel.repaint();
                superText.setVisible(false);
                propositionText.setVisible(false);
                findText.setVisible(false);
                aKeyText.setVisible(false);
                JOptionPane.showMessageDialog(this, "You found a healing beer!");
            } else {
                JOptionPane.showMessageDialog(this, "Chest is already opened!");
            }
        });

        JButton addKeyButton = new JButton("Add Key");
        addKeyButton.setBounds(680, 400, 100, 30);
        add(addKeyButton);
        addKeyButton.addActionListener(e -> {
            hasKey = true;
            JOptionPane.showMessageDialog(this, "You now have a key!");
        });

        JButton addCoinsButton = new JButton("Add Coins");
        addCoinsButton.setBounds(680, 440, 100, 30);
        add(addCoinsButton);
        addCoinsButton.addActionListener(e -> {
            saveData.addCoins(5);
            updateCoinsLabel();
        });
    }

    private void updateCoinsLabel() {
        coinsLabel.setText("Coins: " + saveData.getCoins());
    }

    private ImageIcon scaleIcon(String fileName, int width, int height) {
        java.net.URL imgURL = getClass().getClassLoader().getResource(fileName);
        if (imgURL != null) {
            ImageIcon icon = new ImageIcon(imgURL);
            Image scaled = icon.getImage().getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } else {
            System.err.println("Could not find file: " + fileName);
            return new ImageIcon();
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            SaveData saveData = new SaveData();
            saveData.addCoins(0);
            new Shop(saveData).setVisible(true);
        });
    }
}

