
package org.projectplatformer.Menu;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class AudioManager {

    private static Clip musicClip;
    private static Map<String, Clip> soundEffectClips = new HashMap<>();

    private static FloatControl musicVolumeControl;
    private static FloatControl soundEffectsVolumeControl;

    private static boolean musicEnabled = true;
    private static boolean soundsEnabled = true;

    private static float musicVolume = 70.0f;
    private static float soundEffectsVolume = 85.0f;

    private AudioManager() {}

    private static float linearToDecibels(float linearVolume) {
        if (linearVolume <= 0.0f) {
            return -80.0f;
        }
        return (float) (20.0 * Math.log10(linearVolume / 100.0));
    }
    private static float decibelsToLinear(float decibels) {
        if (decibels <= -79.0f) {
            return 0.0f;
        }
        return (float) (Math.pow(10.0, decibels / 20.0) * 100.0);
    }


    public static void loadMusic(String filePath) {
        try {
            InputStream audioSrc = AudioManager.class.getResourceAsStream(filePath);
            if (audioSrc == null) {
                System.err.println("Music file not found: " + filePath);
                return;
            }
            BufferedInputStream bis = new BufferedInputStream(audioSrc);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);

            if (musicClip != null) {
                musicClip.stop();
                musicClip.close();
            }
            musicClip = AudioSystem.getClip();
            musicClip.open(audioInputStream);

            if (musicClip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                musicVolumeControl = (FloatControl) musicClip.getControl(FloatControl.Type.MASTER_GAIN);
                setMusicVolume(musicVolume);
                if (!musicEnabled) {
                    musicVolumeControl.setValue(musicVolumeControl.getMinimum());
                }
            }
            System.out.println("Music loaded: " + filePath);
        } catch (Exception e) {
            System.err.println("Error loading music: " + filePath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }


    public static void loadSoundEffect(String name, String filePath) {
        try {
            InputStream audioSrc = AudioManager.class.getResourceAsStream(filePath);
            if (audioSrc == null) {
                System.err.println("Sound effect file not found: " + filePath);
                return;
            }
            BufferedInputStream bis = new BufferedInputStream(audioSrc);
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(bis);

            Clip clip = AudioSystem.getClip();
            clip.open(audioInputStream);

            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                volumeControl.setValue(linearToDecibels(soundEffectsVolume));
            }
            soundEffectClips.put(name, clip);
            System.out.println("Sound effect loaded: " + name + " from " + filePath);
        } catch (Exception e) {
            System.err.println("Error loading sound effect: " + name + " from " + filePath + " - " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void playMusic() {
        if (musicClip != null && musicEnabled) {
            if (musicClip.isRunning()) {
                musicClip.stop();
            }
            musicClip.setFramePosition(0);
            musicClip.loop(Clip.LOOP_CONTINUOUSLY);
            System.out.println("Music started.");
        } else if (musicClip == null) {
            System.out.println("Music clip is not loaded. Cannot play.");
        } else if (!musicEnabled) {
            System.out.println("Music is disabled. Cannot play.");
        }
    }


    public static void stopMusic() {
        if (musicClip != null && musicClip.isRunning()) {
            musicClip.stop();
            System.out.println("Music stopped.");
        }
    }

    public static void playSoundEffect(String name) {
        if (!soundsEnabled) {
            return;
        }
        Clip clip = soundEffectClips.get(name);
        if (clip != null) {
            if (clip.isRunning()) {
                clip.stop();
            }
            clip.setFramePosition(0);
            clip.start();
            System.out.println("Playing sound effect: " + name);
        } else {
            System.err.println("Sound effect not found: " + name);
        }
    }


    public static void toggleMusic() {
        musicEnabled = !musicEnabled;
        if (musicEnabled) {
            playMusic();
            setMusicVolume(musicVolume);
        } else {
            stopMusic();
            if (musicVolumeControl != null) {
                musicVolumeControl.setValue(musicVolumeControl.getMinimum());
            }
        }
        System.out.println("Music " + (musicEnabled ? "ON" : "OFF"));
    }


    public static void toggleSounds() {
        soundsEnabled = !soundsEnabled;
        for (Clip clip : soundEffectClips.values()) {
            if (clip.isRunning()) {
                clip.stop();
            }
        }
        System.out.println("Sounds " + (soundsEnabled ? "ON" : "OFF"));
    }

    public static boolean isMusicEnabled() {
        return musicEnabled;
    }

    public static boolean isSoundsEnabled() {
        return soundsEnabled;
    }

    public static String getMusicButtonText() {
        return "Music: " + (musicEnabled ? "ON" : "OFF");
    }

    public static String getSoundsButtonText() {
        return "Sounds: " + (soundsEnabled ? "ON" : "OFF");
    }

    public static void setMusicVolume(float volume) {
        musicVolume = Math.max(0.0f, Math.min(100.0f, volume));
        if (musicVolumeControl != null) {
            float decibels = linearToDecibels(musicVolume);
            float min = musicVolumeControl.getMinimum();
            float max = musicVolumeControl.getMaximum();
            musicVolumeControl.setValue(Math.max(min, Math.min(max, decibels)));
            System.out.println("Music volume set to: " + musicVolume + " (dB: " + musicVolumeControl.getValue() + ")");
        }
    }

    public static void setSoundEffectsVolume(float volume) {
        soundEffectsVolume = Math.max(0.0f, Math.min(100.0f, volume));
        for (Clip clip : soundEffectClips.values()) {
            if (clip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl volumeControl = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
                float decibels = linearToDecibels(soundEffectsVolume);
                float min = volumeControl.getMinimum();
                float max = volumeControl.getMaximum();
                volumeControl.setValue(Math.max(min, Math.min(max, decibels)));
            }
        }
        System.out.println("Sound effects volume set to: " + soundEffectsVolume);
    }

    public static float getMusicVolume() {
        if (musicVolumeControl != null) {
            return decibelsToLinear(musicVolumeControl.getValue());
        }
        return musicVolume;
    }

    public static float getSoundEffectsVolume() {
        return soundEffectsVolume;
    }

    public static void cleanup() {
        if (musicClip != null) {
            musicClip.stop();
            musicClip.close();
            musicClip = null;
        }
        for (Clip clip : soundEffectClips.values()) {
            clip.stop();
            clip.close();
        }
        soundEffectClips.clear();
        System.out.println("Audio clips cleaned up.");
    }
}
