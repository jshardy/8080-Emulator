package Core;

import Utilities.Utils.nString;

public class Memory {
    private final int MEMORY_SIZE = 0x4000;
    private int[] memory = new int[MEMORY_SIZE];

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
        if(address >= 0x4000) {
            // Space Invaders only
            address &= 0x3ff;
        }
        memory[address] = value;

        //f(address >= 0x2400) {
            // Video
            // Paint on screen the value at (address - 0x2400)
        //}
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

    public void printMemoryAccess(int address, boolean read_or_write) {
        String strAddress = nString.hexToString16(address);
        String strValue = nString.hexToString8(memory[address]);
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