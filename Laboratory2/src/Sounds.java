import acm.util.MediaTools;
import java.applet.AudioClip;
public class Sounds {
    private static AudioClip hitSound = MediaTools.loadAudioClip("");
    private static AudioClip loseLifeSound = MediaTools.loadAudioClip("");
    private static AudioClip loseSound = MediaTools.loadAudioClip("");
    private static AudioClip winSound = MediaTools.loadAudioClip("");
    private static AudioClip kerelMusic = MediaTools.loadAudioClip("kerel.wav");
    public static void playHit() {
        hitSound.play();
    }
    public static void playLoseLife() {
        loseLifeSound.play();
    }
    public static void playLose() {
        loseSound.play();
    }
    public static void playWinSound() {
        winSound.play();
    }
    public static void playKerelMusic() {
        kerelMusic.play();
    }
    public static void stopAll() {
        hitSound.stop();
        loseLifeSound.stop();
        loseSound.stop();
        winSound.stop();
        kerelMusic.stop();
    }
}

