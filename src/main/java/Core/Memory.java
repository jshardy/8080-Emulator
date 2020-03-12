package Core;

import Utilities.Utils.nString;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public interface Memory {
    void loadMemory(byte [] mem);
    int readByte(int address);
    void writeByte(int address, int value);
    int readWord(int address);
    void writeWord(int address, int value);
    void writeWord(int address, int low, int high);
    BufferedImage getImage();
}
