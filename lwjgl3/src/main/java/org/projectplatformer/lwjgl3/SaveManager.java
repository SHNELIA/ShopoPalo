package org.projectplatformer.lwjgl3;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;

import java.util.ArrayList;
import java.util.List;

public class SaveManager {
    public static final String SAVE_DIR = "saves/";

    /** Завантажити SaveData із слота */
    public static SaveData load(int slot) {
        String path = SAVE_DIR + "slot" + slot + ".json";
        FileHandle f = Gdx.files.local(path);
        if (!f.exists()) return new SaveData(); // або null
        return new com.badlogic.gdx.utils.Json().fromJson(SaveData.class, f.readString());
    }

    /** Зберегти SaveData у слот */
    public static void save(int slot, SaveData data) {
        String path = SAVE_DIR + "slot" + slot + ".json";
        String json = new com.badlogic.gdx.utils.Json().toJson(data);
        Gdx.files.local(path).writeString(json, false);
    }

    /** Повернути список номерів слотів, у яких є існуючий файл */
    public static List<Integer> availableSlots() {
        List<Integer> list = new ArrayList<>();
        for (int i = 1; i <= 4; i++) {
            String path = SAVE_DIR + "slot" + i + ".json";
            if (Gdx.files.local(path).exists()) list.add(i);
        }
        return list;
    }
}
