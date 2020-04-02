package Core.GUI;

import Core.Memory;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

public class VideoArea extends JPanel {
    Memory memory;
    int frame_count = 0;

    public VideoArea(Memory mem) {
        memory = mem;
        setLayout(null);
        removeAll();
        setIgnoreRepaint(true);
        setBackground(Color.black);
    }

    public void setVideoMemory(Memory mem) {
        memory = mem;
    }

    @Override
    public void paintComponent(Graphics g) {
        BufferedImage bf = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        AffineTransform at = AffineTransform.getScaleInstance(1.8, 1.8);
        AffineTransformOp ato = new AffineTransformOp(at, AffineTransformOp.TYPE_BICUBIC);
        bf = ato.filter(memory.getImage(), bf);

        g.drawImage(bf, 0, 0, null);
    }
}
