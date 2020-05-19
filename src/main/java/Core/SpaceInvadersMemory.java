package Core;

import Utilities.Utils;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.io.IOException;

public class SpaceInvadersMemory implements Memory, java.io.Serializable {
    private boolean debug = false;
    private final int MEMORY_SIZE = 0x4000;
    private int[] memory = new int[MEMORY_SIZE];

    public final int HEIGHT = 256;
    public final int WIDTH = 224;
    public final int VRAM = 0x2400;

    public static final int WHITE = 0xffeffe;
    public static final int RED = 0xfe0e00;
    public static final int GREEN = 0x00fe0a;
    public static final int BLACK = 0x000000;

    private transient BufferedImage db = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private int[] rawPixelData = ((DataBufferInt) db.getRaster().getDataBuffer()).getData();

    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        db = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
        rawPixelData = ((DataBufferInt) db.getRaster().getDataBuffer()).getData();
        // Redraw EVERY pixel
        for(int i = VRAM; i < MEMORY_SIZE; i++) {
            setPixel(i, memory[i]);
        }
    }

    public SpaceInvadersMemory(byte [] mem) {
        loadMemory(mem);
    }

    public void loadMemory(byte [] mem) {
        for(int i = 0; i < mem.length; i++) {
            memory[i] = mem[i] & 0xff; // keep it 255 and below
        }
    }

    public void setBuffer(BufferedImage bf) {
        db = bf;
        rawPixelData = ((DataBufferInt) db.getRaster().getDataBuffer()).getData();
    }

    public int readByte(int address) throws IllegalAccessError {
        //printMemoryAccess(address, false);
        return memory[address];
    }

    public void writeByte(int address, int value) throws IllegalAccessError {
        // ROM Area: 0-0x1fff
        // Video Area: 2400-0x3fff
        if(address >= 0x0000 && address <= 0x1fff) {
            System.out.println("BUG: Writing to ROM: " + address);
            //return; // ROM do nothing
        }

        if(address >= VRAM && address < MEMORY_SIZE) {
            setPixel(address, value);
        } else {
            address = 0x2000 | (address & 0x03ff);
        }
        memory[address] = value;
        //printMemoryAccess(address, true);
    }

    public int readWord(int address) {
        return (memory[address + 1] << 8) | memory[address];
    }

    public void writeWord(int address, int value) {
        int low = 0xff & value;
        int high = (value >>> 8);
        writeWord(address, low, high);
    }

    public void writeWord(int address, int low, int high) {
        writeByte(address, low);
        writeByte(address + 1, high);
    }

    // setPixel(address, on/off)
    // Sets a pixel on or off
    // This method saves CPU cycles, but could just
    // as easily be done with a constant loop over each pixel each frame.
    public void setPixel(int address, int value) {
        int offset = address - VRAM;
        int x = offset >>> 5; // upper 5 bits are start of x
        int y = (offset & 0x1f); // lower 5 bits are start of y
        y = (HEIGHT - 8) - (y << 3); // because image is sideways in real machine

        int currentColor = WHITE;
        if(y > 31 && y <= 63) {
            currentColor = RED;
        } else if(y > 183 && y <= 239) {
            currentColor = GREEN;
        } else if(y >= 239 && x >= 15 && x < 135) {
            currentColor = GREEN;
        }

        // 8 bits to draw.
        int currentLocation = (y * WIDTH) + x;
        for(int i = 7; i >= 0; i--) {
            // Each bit represents on or off pixel
            // 1 byte = 8 pixels
            if((value & (1 << i)) != 0) {
                rawPixelData[currentLocation] = currentColor;
            } else {
                rawPixelData[currentLocation] = BLACK;
            }
            // Remember original Space Invaders
            // screen was sideways
            currentLocation += WIDTH;
        }
    }

    public BufferedImage getImage() {
        return db;
    }

    public void printMemoryAccess(int address, boolean read_or_write) {
        if(!debug)
            return;

        String strAddress = Utils.nString.hexToString16(address);
        String strValue = Utils.nString.hexToString8(memory[address]);

        if(address >= 0x2400 && address <= 0x3fff) {
            System.out.print("VIDEO ");
        } else {
            System.out.print("CPU ");
        }
        System.out.println((read_or_write ? "WRITE " : "READ ") + "ACCESS: " + strAddress + " = " + strValue);
    }

    public void printOutMemory() {
        for(int i = 0; i < 2049; i++) {
            String address = Utils.nString.hexToString16(i);
            String data = Utils.nString.hexToString8(memory[i]);
            System.out.println(address + " : " + data);
        }
    }
}