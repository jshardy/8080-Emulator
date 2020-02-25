package Core;

import Utilities.Utils.nString;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class Memory {
    private boolean debug = true;

    private final int MEMORY_SIZE = 0x4000;
    private int[] memory = new int[MEMORY_SIZE];

    final int HEIGHT = 256;
    final int WIDTH = 224;

    public static final int CLR_WHITE = 0xffffff;
    public static final int CLR_RED = 0xff0000;
    public static final int CLR_GREEN = 0x00ff00;
    public static final int BLACK = 0x000000;

    private BufferedImage db = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
    private int[] pixels = ((DataBufferInt) db.getRaster().getDataBuffer()).getData();

    public Memory(byte [] mem) {
        for(int i = 0; i < mem.length; i++) {
            memory[i] = mem[i] & 0xff;
        }
    }

    public int readByte(int address) throws IllegalAccessError {
        printMemoryAccess(address, false);
        return memory[address];
    }

    public void writeByte(int address, int value) throws IllegalAccessError {
        if(address < 0x2400) {
            memory[address] = value;
        } else if(address < 0x4000) {
            memory[address] = value;
            setPixel(address - 0x2400, value);
        } else { // mirroring
            address = 0x2000 | (address & 0x3ff);
            memory[address] = value;
        }
        //if(address >= 0x4000) {
            // Space Invaders only - mirror ram
        //    address &= 0x3ff;
        //}

        // ROM Area: 0-0x1fff
        // Video Area: 2400-0x3fff
        //memory[address] = value;

        printMemoryAccess(address, true);
    }

    public int readWord(int address) {
        return (memory[address + 1] << 8) | memory[address];
    }

    public void writeWord(int address, int value) {
        int low = 0xff & value;
        int high = (value >>> 8) & 0xff;
        writeWord(address, low, high);
    }

    public void writeWord(int address, int low, int high) {
        writeByte(address, low);
        writeByte(address + 1, high);
    }

    public void setPixel(int offset, int value) {
        final int x = offset >>> 5;
        final int y = 248 - ((offset & 0x1f) << 3);

        final int color = (y >= 32 && y < 64) ? CLR_RED
                : (y >= 184 && y < 240) ? CLR_GREEN
                : (y >= 240 && x >= 16 && x < 134) ? CLR_GREEN
                : CLR_WHITE;

        int currentLocation = y * WIDTH + x;
        pixels[currentLocation] = (value & 0x80) != 0 ? color : BLACK;
        currentLocation += WIDTH;
        pixels[currentLocation] = (value & 0x40) != 0 ? color : BLACK;
        currentLocation += WIDTH;
        pixels[currentLocation] = (value & 0x20) != 0 ? color : BLACK;
        currentLocation += WIDTH;
        pixels[currentLocation] = (value & 0x10) != 0 ? color : BLACK;
        currentLocation += WIDTH;
        pixels[currentLocation] = (value & 0x08) != 0 ? color : BLACK;
        currentLocation += WIDTH;
        pixels[currentLocation] = (value & 0x04) != 0 ? color : BLACK;
        currentLocation += WIDTH;
        pixels[currentLocation] = (value & 0x02) != 0 ? color : BLACK;
        currentLocation += WIDTH;
        pixels[currentLocation] = (value & 0x01) != 0 ? color : BLACK;
    }

    public BufferedImage getImage() {
        return db;
    }

    public void printMemoryAccess(int address, boolean read_or_write) {
        if(!debug)
            return;

        String strAddress = nString.hexToString16(address);
        String strValue = nString.hexToString8(memory[address]);

        if(address >= 0x2400 && address <= 0x3fff) {
            System.out.print("VIDEO ");
        } else {
            System.out.print("CPU ");
        }

        System.out.println((read_or_write ? "WRITE " : "READ ") + "ACCESS: " + strAddress + " = " + strValue);
    }

    public void printOutMemory() {
        // Don't print it all out yet, there is mirroring to do
        for(int i = 0; i < 2049; i++) {
            String address = nString.hexToString16(i);
            String data = nString.hexToString8(memory[i]);
            System.out.println(address + " : " + data);
        }
    }
}