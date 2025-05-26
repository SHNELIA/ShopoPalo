package org.projectplatformer.lwjgl3;

import java.util.HashSet;
import java.util.Set;

/**
 * Дані збереження гри: монети, зібрані предмети,
 * придбані предмети та зброя, а також поточна зброя.
 */
public class SaveData {
    private int coins;
    private Set<String> collectedItems;
    private Set<String> purchasedItems;
    private Set<String> unlockedWeapons;
    private String equippedWeaponId;

    public SaveData() {
        coins = 0;
        collectedItems   = new HashSet<>();
        purchasedItems   = new HashSet<>();
        unlockedWeapons  = new HashSet<>();
        equippedWeaponId = null;
    }

    // ===== Coins =====

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public void spendCoins(int amount) {
        coins = Math.max(0, coins - amount);
    }

    // ===== Collected items =====

    public void collectItem(String itemId) {
        collectedItems.add(itemId);
    }

    public boolean isItemCollected(String itemId) {
        return collectedItems.contains(itemId);
    }

    public Set<String> getCollectedItems() {
        return collectedItems;
    }

    // ===== Purchased items (non‐weapon) =====

    public void purchaseItem(String itemId) {
        purchasedItems.add(itemId);
    }

    public boolean isItemPurchased(String itemId) {
        return purchasedItems.contains(itemId);
    }

    public Set<String> getPurchasedItems() {
        return purchasedItems;
    }

    // ===== Weapons =====

    /** Розблокувати (купити) зброю */
    public void unlockWeapon(String weaponId) {
        unlockedWeapons.add(weaponId);
    }

    /** Чи розблокована (куплена) зброя */
    public boolean isWeaponUnlocked(String weaponId) {
        return unlockedWeapons.contains(weaponId);
    }

    /** Повертає набір усіх розблокованих зброї */
    public Set<String> getUnlockedWeapons() {
        return unlockedWeapons;
    }

    /** Обрати поточну зброю (лише з уже розблокованих) */
    public void equipWeapon(String weaponId) {
        if (isWeaponUnlocked(weaponId)) {
            equippedWeaponId = weaponId;
        }
    }

    /** Яка зброя зараз обрана? */
    public String getEquippedWeapon() {
        return equippedWeaponId;
    }

    // ===== Reset helpers =====

    /** Повністю очистити всі дані збереження */
    public void reset() {
        coins = 0;
        collectedItems.clear();
        purchasedItems.clear();
        unlockedWeapons.clear();
        equippedWeaponId = null;
    }

    /** Очистити лише зібрані предмети */
    public void resetCollectedItems() {
        collectedItems.clear();
    }

    /** Очистити лише придбані (не-зброю) предмети */
    public void resetPurchasedItems() {
        purchasedItems.clear();
    }

    /** Скинути монети */
    public void resetCoins() {
        coins = 0;
    }

    /** Скинути розблоковану зброю та обладнання */
    public void resetWeapons() {
        unlockedWeapons.clear();
        equippedWeaponId = null;
    }
}
