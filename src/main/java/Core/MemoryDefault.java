package Core;

import java.awt.image.BufferedImage;

public class MemoryDefault implements Memory {
    private final int MEMORY_SIZE = 0x16000;
    private int[] memory = new int[MEMORY_SIZE];
    public final int VRAM = 0x2400;

    public MemoryDefault(byte [] mem) {
        loadMemory(mem);
    }

    @Override
    public void loadMemory(byte [] mem) {
        for(int i = 0; i < mem.length; i++) {
            memory[i] = mem[i] & 0xff;
        }
    }

    @Override
    public int readByte(int address) {
        return memory[address];
    }

    @Override
    public void writeByte(int address, int value) {
        memory[address] = value;
    }

    @Override
    public int readWord(int address) {
        return (memory[address + 1] << 8) | memory[address];
    }

    @Override
    public void writeWord(int address, int value) {
        int low = 0xff & value;
        int high = (value >>> 8) & 0xff;
        writeWord(address, low, high);
    }

    @Override
    public void writeWord(int address, int low, int high) {
        writeByte(address, low);
        writeByte(address + 1, high);
    }

    @Override
    public BufferedImage getImage() {
        return null;
    }
}
