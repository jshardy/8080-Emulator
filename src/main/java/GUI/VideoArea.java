package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class VideoArea extends JPanel {

    BufferedImage db = new BufferedImage(256, 224, BufferedImage.TYPE_INT_RGB);
    @Override
    public void paint(Graphics g) {
        super.paint(g);
        g.drawString("VideoArea", 15, 15);
        System.out.println("paint() - VideoArea");
    }
}
