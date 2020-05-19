package Core;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.InputStream;

public class Sound implements LineListener, Runnable {
    static int index_count = 0;
    private int index = index_count++;
    private boolean isLoaded = false;
    private boolean isPlaying = false;
    private Clip clip;
    private Thread thread = null;
    private boolean running = false;
    private boolean continuous = false;
    private boolean soundOn = true;

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

    public Sound(InputStream inSound) {
        try {
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(new BufferedInputStream(inSound));
            AudioFormat format = audioStream.getFormat();
            DataLine.Info info = new DataLine.Info(Clip.class, format);
            clip = (Clip)AudioSystem.getLine(info);
            clip.addLineListener(this);
            clip.open(audioStream);
            isLoaded = true;
        } catch(Exception e) {
            System.out.println("Sound file not working: " + inSound);
        }
    }

    public void play() {
        // Don't play if file not loaded
        // only play if its not already playing
        if(soundOn == false)
            return;

        if(isLoaded && !isPlaying) {
            isPlaying = true;
            clip.setFramePosition(0); // reset start time
            clip.start();
        }
    }

    public void setLoop(int loop) {
        if(soundOn == false)
            return;

        if (isLoaded) {
            continuous = true;
            clip.loop(loop);
        }
    }

    public void toggleSound() {
        soundOn = !soundOn;
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

    @Override
    public void run() {
        if(thread != null) {
            if(!continuous) {
                play();
                thread = null;
            } else {
                setLoop(15);
            }
        }
    }

    public void start() {
        if(thread == null) {
            thread = new Thread(this, "Sound");
            thread.start();
        }
    }
}
