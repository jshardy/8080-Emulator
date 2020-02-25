package Core;

import java.awt.*;
import java.awt.image.BufferedImage;

public class Video implements VideoInterface {
    final int HEIGHT = 224;
    final int WIDTH = 256;
    final int VRAM = 0x2400;

    Memory memory;
    BufferedImage db = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);

    public Video(Memory mem) {
        memory = mem;
    }

    private void setPixel(int x, int y, Color rgb) {
        db.setRGB(x, y, rgb.getRGB());
    }

    @Override
    public BufferedImage drawVideoMemory() {
        if(memory == null)
            return null;

        for(int y = 0; y < HEIGHT; y++) {
            // x < 32 -- 32*8 = 256 - Each pixel is stored as a bit
            for(int x = 0; x < (WIDTH / 8); x++) {
                // base address + (y * 32) + x
                // Each bit is a pixel
                int index = VRAM + ((y * (WIDTH / 8)) + x);
                int pixelGroup = memory.readByte(index);

                for(int i = 0; i < 8; i++) {
                    // calculate x pixel | x * 8 + i
                    int pixelx = (x * 8) + i;

                    // Rather than erase the buffer area, write each pixel each time.
                    // My goal is to help slow the emulator down by doing more work rather
                    // than speed tricks. This may cause some serious tearing problems if
                    // timing is off. -- Will fix in future
                    if (((pixelGroup >>> i) & 0x1) == 1) {
                        setPixel(pixelx, y, new Color(0, 255, 65));
                    } else {
                        setPixel(pixelx, y, new Color(0, 0, 0));
                    }
                }
            }
        }
        return db;
    }
}
