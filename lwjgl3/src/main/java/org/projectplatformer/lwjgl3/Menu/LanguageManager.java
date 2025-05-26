package org.projectplatformer.lwjgl3.Menu;

import java.util.HashMap;
import java.util.Map;

public class LanguageManager {

    private static final Map<String, String> english = new HashMap<>();

    static {
// English localization
        english.put("title", "ShopoPalo");
        english.put("playButton", "PLAY");
        english.put("guideButton", "GUIDE / MOVEMENT /CHARACTER CONTROL");
        english.put("settingsButton", "SETTINGS");
        english.put("quitButton", "QUIT");
        english.put("musicButton", "MUSIC");
        english.put("soundButton", "SOUNDS");
        english.put("madeBy", "made by: Shpuniar Nazar,\n" +
            " Burma Sonya,\n" +
            " Revenko Anna,\n" +
            " Horyslavets Kateryna,\n" +
            " Tsaprylova Irina.\n");
        english.put("playMessage", "Launching ShopoPalo game!");
        english.put("quitConfirm", "Are you sure you want to quit?");
        english.put("quitTitle", "Exit Game");
        english.put("musicOn", "Music ON");
        english.put("musicOff", "Music OFF");
        english.put("soundsOn", "Sounds ON");
        english.put("soundsOff", "Sounds OFF");
        english.put("guideTitle", "Guide - ShopoPalo");
        english.put("guideText", "Welcome to ShopoPalo!\n\n" +
            "Character Controls:\n" +
            "- Movement: A (left), D (right)\n" +
            "- Double jump - double click Space, Dash - double click A or D\n" +
            "- E - interaction with something\n" +
            "- Attack - mouse 1, Melee weapon - 1, Range weapon - 2\n" +
            "- Jump: Space\n" +
            "The game has 4 levels. During the game you can collect coins to buy new items \n");
        english.put("settingsWindow_title", "Settings - ShopoPalo");
        english.put("settingsWindow_musicVolume", "Music Volume:");
        english.put("settingsWindow_effectsVolume", "Effects Volume:");
        english.put("settingsWindow_windowSize", "Window Size:");
        english.put("settingsWindow_brightness", "Brightness:");
        english.put("settingsWindow_language", "Language:");
        english.put("settingsWindow_subtitles", "Enable Subtitles");
        english.put("settingsWindow_apply", "Apply");
        english.put("settingsWindow_back", "Back");
        english.put("settingsWindow_settingsApplied", "Settings Applied!");
        english.put("settingsWindow_saved", "Saved");
        english.put("newGameConfirm_message", "Starting a new game will erase unsaved progress. Continue?");
        english.put("newGameConfirm_title", "New Game Confirmation");
        english.put("gameWindow_title", "Game Screen");
        english.put("startGame_loadingSaved", "Loading saved game...");
        english.put("startGame_startingNew", "Starting new game...");
        english.put("startGame_title", "Game Start");
        english.put("loadGame_selectSave", "Select a save file:");
        english.put("loadGame_title", "Load Game");
        english.put("backButton", "Back");
        english.put("loadGame_loading", "Save loaded: ");
        english.put("gameWorld_title", "Choose Option");
        english.put("gameWorld_shop", "SHOP");
        english.put("gameWorld_equipment", "EQUIPMENT");
        english.put("gameWorld_levels", "GO TO LEVELS");
        english.put("backToMainMenu", "BACK");
        english.put("gameWorld_shop_message", "Opening the Shop!");
        english.put("gameWorld_equipment_message", "Opening your Equipment!");
        english.put("gameWorld_levels_message", "Preparing for Game!");

        // --- Additions for LevelsWindow ---
        english.put("levelsWindow_title", "Levels"); // Title for the Levels window itself
        english.put("levelsWindow_chooseLevel", "Choose Level"); // Text for the title label
        english.put("levelsWindow_level", "Level"); // Prefix for level buttons (e.g., "Level 1")
        english.put("levelsWindow_startingLevel", "Starting Level");
        english.put("equipmentWindow_title", "Equipment");
        english.put("equipmentWindow_rangedWeapons", "Ranged Weapons");
        english.put("equipmentWindow_meleeWeapons", "Melee Weapons");
    }

    public static String get(String key) {
        return english.getOrDefault(key, "KEY_NOT_FOUND");
    }

    // This method name is more accurate for returning the language code
    public static String getCurrentLanguageCode() {
        return "en"; // Currently only English is supported
    }

    // This method remains a no-op as per previous discussions (only English supported)
    public static void setLanguage(String langCode) {
        // No-op for now, as only English is implemented
    }
}
