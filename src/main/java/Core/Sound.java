package Core;

import javax.sound.sampled.*;
import java.io.File;

public class Sound implements LineListener {
    static int index_count = 0;
    private int index = index_count++;
    private boolean isLoaded = false;
    private boolean isPlaying = false;
    private Clip clip;

    public Sound(String filename) {
            File actualFileData = new File(filename);
            try {
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(actualFileData);
                AudioFormat format = audioStream.getFormat();
                DataLine.Info info = new DataLine.Info(Clip.class, format);
                clip = (Clip)AudioSystem.getLine(info);
                clip.addLineListener(this);
                clip.open(audioStream);
                isLoaded = true;
            } catch(Exception e) {
                System.out.println("Sound file not working: " + filename);
            }
    }

    public void play() {
        // Don't play if file not loaded
        // only play if its not already playing
        if(isLoaded && !isPlaying) {
            System.out.println("Sound " + index + " playing");
            isPlaying = true;
            clip.setFramePosition(0); // reset start time
            clip.start();
        }
    }

    public void setLoop(int loop) {
        if (isLoaded) {
            clip.loop(loop);
        }
    }

    public int getIndex() {
        return index;
    }

    public int getTotalSoundCount() {
        return index_count;
    }

    @Override
    public void update(LineEvent lineEvent) {
        // Audio playing?
        if(lineEvent.getType() == LineEvent.Type.START) {
            isPlaying = true;
        } else if(lineEvent.getType() == LineEvent.Type.STOP) {
            isPlaying = false;
        }
    }
}
