package GUI;

import Core.Memory;
import Core.VideoInterface;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoArea extends JPanel {
    Memory memory;
    int frame_count = 0;

    public VideoArea(Memory mem) {
        memory = mem;
    }

    @Override
    public void paint(Graphics g) {
        BufferedImage db = memory.getImage();

        frame_count++;

        g.drawImage(db, 0, 0, this);

        g.setColor(new Color(255,255,255));
        g.drawString("Frame Count: " + Integer.toString(frame_count), 5, 15);
    }
}
