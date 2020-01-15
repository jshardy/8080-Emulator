package Core;

import Utilities.Utils.DebugStringConversions;

public class Memory {
    private final int MEMORY_SIZE = 0x4000;
    private int[] memory = new int[MEMORY_SIZE];

    public Memory() {

    }

    public Memory(byte [] mem) {
        for(int i = 0; i < mem.length; i++) {
            memory[i] = mem[i] & 0xff;
        }
    }

    public int getAddress(int address) throws IllegalAccessError {
        if(address >= MEMORY_SIZE) {
            throw new IllegalAccessError("Memory address out of range");
        }
        return memory[address];
    }

    public void printOutMemory() {
        // Don't print it all out yet, there is mirroring to do
        for(int i = 0; i < 2049; i++) {
            String address = DebugStringConversions.hexToString16(i);
            String data = DebugStringConversions.hexToString8(memory[i]);
            System.out.println(address + " : " + data);
        }
    }
}