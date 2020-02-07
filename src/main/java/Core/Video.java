package Core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Video {
    private double fps = 30.0;
    private long timePerFrame = (long) (1000000000.0 / fps);
    Memory memory;
    BufferedImage db = new BufferedImage(256, 224, BufferedImage.TYPE_INT_RGB);

    public Video(Memory mem) {
        memory = mem;
    }

    //public void setPixel(address, value) {
        //Graphics g = db.getGraphics();
        //db.setRGB(x, y, rgb);
        //g.drawBytes();
    //}

    //public update() {

    //}
}
