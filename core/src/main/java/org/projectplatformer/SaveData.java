package org.projectplatformer;

import java.util.HashSet;
import java.util.Set;

public class SaveData {
    private int coins;
    private Set<String> collectedItems;
    private Set<String> purchasedItems;

    public SaveData() {
        coins = 0;
        collectedItems = new HashSet<>();
        purchasedItems = new HashSet<>();
    }

    public int getCoins() {
        return coins;
    }

    public void addCoins(int amount) {
        coins += amount;
    }

    public void spendCoins(int amount) {
        coins -= amount;
    }

    public void collectItem(String itemId) {
        collectedItems.add(itemId);
    }

    public boolean isItemCollected(String itemId) {
        return collectedItems.contains(itemId);
    }

    public void purchaseItem(String itemId) {
        purchasedItems.add(itemId);
    }

    public boolean isItemPurchased(String itemId) {
        return purchasedItems.contains(itemId);
    }

    public void reset() {
        coins = 0;
        collectedItems.clear();
        purchasedItems.clear();
    }

    public void resetCollectedItems() {
        collectedItems.clear();
    }

    public void resetCoins() {
        coins = 0;
    }


}
