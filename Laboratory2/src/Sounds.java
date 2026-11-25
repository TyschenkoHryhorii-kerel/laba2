import acm.util.MediaTools;
import java.applet.AudioClip;

/** Клас для завантаження та відтворення звуків і фонової музики гри. */
public class Sounds {
    private static AudioClip hitSound = loadClip("hit.wav");
    private static AudioClip loseLifeSound = loadClip("loseLife.wav");
    private static AudioClip loseSound = loadClip("lose.wav");
    private static AudioClip winSound = loadClip("win.wav");
    private static AudioClip bonusGoodSound = loadClip("bonusGood.wav");
    private static AudioClip bonusBadSound = loadClip("bonusBad.wav");
    private static AudioClip bgMusic = loadClip("bgMusic.wav");
    private static AudioClip level1Music = loadClip("level1Music.wav");
    private static AudioClip level2Music = loadClip("level2Music.wav");
    private static AudioClip kerelMusic = loadClip("kerel.wav");

    /** Безпечно завантажує аудіофайл із кореня проєкту. */
    private static AudioClip loadClip(String fileName) {
        if (fileName == null || fileName.isEmpty()) {
            return null;
        }
        try {
            return MediaTools.loadAudioClip(fileName);
        } catch (Exception e) {
            return null;
        }
    }

    /** Відтворює кліп, якщо він завантажений. */
    private static void playClip(AudioClip clip) {
        if (clip != null) {
            clip.play();
        }
    }

    /** Зупиняє кліп, якщо він завантажений. */
    private static void stopClip(AudioClip clip) {
        if (clip != null) {
            clip.stop();
        }
    }

    /** Звук відбиття м'яча. */
    public static void playHit() { playClip(hitSound); }

    /** Звук втрати життя. */
    public static void playLoseLife() { playClip(loseLifeSound); }

    /** Звук поразки. */
    public static void playLose() { playClip(loseSound); }

    /** Звук перемоги. */
    public static void playWinSound() { playClip(winSound); }

    /** Звук корисного бонусу. */
    public static void playBonusGood() { playClip(bonusGoodSound); }

    /** Звук ускладнюючого бонусу. */
    public static void playBonusBad() { playClip(bonusBadSound); }

    /** Запускає зациклену фонову музику головного меню. */
    public static void playBgMusic() {
        if (bgMusic != null) {
            bgMusic.stop();
            bgMusic.loop();
        }
    }

    /** Запускає зациклену музику 1-го рівня. */
    public static void playLevel1Music() {
        if (level1Music != null) {
            level1Music.stop();
            level1Music.loop();
        }
    }

    /** Запускає зациклену музику 2-го рівня. */
    public static void playLevel2Music() {
        if (level2Music != null) {
            level2Music.stop();
            level2Music.loop();
        }
    }

    /** Зупиняє фонову музику меню. */
    public static void stopBgMusic() { stopClip(bgMusic); }

    /** Запускає музику боса Керела на 3-му рівні. */
    public static void playKerelMusic() { playClip(kerelMusic); }

    /** Зупиняє всі звуки та музику. */
    public static void stopAll() {
        stopClip(hitSound);
        stopClip(loseLifeSound);
        stopClip(loseSound);
        stopClip(winSound);
        stopClip(bonusGoodSound);
        stopClip(bonusBadSound);
        stopClip(bgMusic);
        stopClip(level1Music);
        stopClip(level2Music);
        stopClip(kerelMusic);
    }
}
