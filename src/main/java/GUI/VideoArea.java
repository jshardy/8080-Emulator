package GUI;

import Core.Memory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;

public class VideoArea extends Canvas {
    Memory memory;
    int frame_count = 0;
    static long frames = 0;
    BufferStrategy bs;
    GraphicsEnvironment ge;
    GraphicsDevice gd;
    GraphicsConfiguration gc;
    BufferedImage bf;

    public VideoArea(Memory mem) {
        memory = mem;
        //setIgnoreRepaint(true);
        setBackground(Color.black);
        //createBufferStrategy(2); // Back buffer
        //bs = getBufferStrategy();
        //ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        //gd = ge.getDefaultScreenDevice();
        //gc = gd.getDefaultConfiguration();
        //bf = gc.createCompatibleImage(getWidth(), getHeight());
        setIgnoreRepaint(true);
        setVisible(true);
    }

    public void setVideoMemory(Memory mem) {
        memory = mem;
    }

    public void draw() {
        if(bs == null) {
            createBufferStrategy(2); // Back buffer
            bs = getBufferStrategy();
            ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            gd = ge.getDefaultScreenDevice();
            gc = gd.getDefaultConfiguration();
            bf = gc.createCompatibleImage(getWidth(), getHeight());
        }

        Graphics g = bs.getDrawGraphics();
        //bf = gc.createCompatibleImage(getWidth(), getHeight());
        bf = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = AffineTransform.getScaleInstance(1.8, 1.8);
        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        bf = ato.filter(memory.getImage(), bf);

        g.drawImage(bf, 0, 0, null);
        g.setColor(Color.red);
        g.drawString("Frames: " + frames, 20,50);
        if(!getBufferStrategy().contentsLost()) {
            bs.show();
        }
        Thread.yield();
        System.out.println("Draw called " + ++frames);
    }
}
