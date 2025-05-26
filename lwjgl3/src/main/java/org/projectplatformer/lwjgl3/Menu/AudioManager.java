package org.projectplatformer.lwjgl3.Menu;

public class AudioManager {

        private static boolean musicEnabled = true; // Початковий стан: музика увімкнена
        private static boolean soundsEnabled = true; // Початковий стан: звуки увімкнені

        public static boolean isMusicEnabled() {
            return musicEnabled;
        }

        public static void toggleMusic() {
            musicEnabled = !musicEnabled;
            System.out.println("Music is now: " + (musicEnabled ? "ON" : "OFF"));
            // Тут можна додати логіку для фактичного вмикання/вимикання музики
        }

        public static boolean isSoundsEnabled() {
            return soundsEnabled;
        }

        public static void toggleSounds() {
            soundsEnabled = !soundsEnabled;
            System.out.println("Sounds are now: " + (soundsEnabled ? "ON" : "OFF"));
            // Тут можна додати логіку для фактичного вмикання/вимикання звукових ефектів
        }

        // Допоміжний метод для отримання тексту кнопки
        public static String getMusicButtonText() {
            return "MUSIC"; // Змінено: тепер повертає просто "MUSIC"
        }

        // Допоміжний метод для отримання тексту кнопки
        public static String getSoundsButtonText() {
            return "SOUNDS"; // Змінено: тепер повертає просто "SOUNDS"
        }
    }

