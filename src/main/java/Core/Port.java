package Core;

public class Port {
    boolean[] bits = new boolean[8];

    public boolean setBit(int bit, boolean value) {
        return bits[bit] = value;
    }

    public boolean getBit(int bit) {
        return bits[bit];
    }

    public void setPort(int port) {
        bits[0] = (port & 0x1) > 0;
        bits[1] = (port & 1 << 1) > 0;
        bits[2] = (port & 1 << 2) > 0;
        bits[3] = (port & 1 << 3) > 0;
        bits[4] = (port & 1 << 4) > 0;
        bits[5] = (port & 1 << 5) > 0;
        bits[6] = (port & 1 << 6) > 0;
        bits[7] = (port & 1 << 7) > 0;
    }

    public int getPort() {
        int data = 0;
        data |= bits[0] ? 1 : 0;
        data |= bits[1] ? 1 << 1 : 0;
        data |= bits[2] ? 1 << 2 : 0;
        data |= bits[3] ? 1 << 3 : 0;
        data |= bits[4] ? 1 << 4 : 0;
        data |= bits[5] ? 1 << 5 : 0;
        data |= bits[6] ? 1 << 6 : 0;
        data |= bits[7] ? 1 << 7 : 0;
        return data;
    }
}

