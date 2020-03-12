package Core;

import Utilities.Utils;

public class SpaceInvadersIO implements InputOutput {
    private Port port1 = new Port();
    private Port port2 = new Port();
    private int shiftRegister = 0;
    private int shiftOffset = 0;

    public int getShiftRegister() {
        return shiftRegister;
    }

    public int getShiftOffset() {
        return shiftOffset;
    }

    public Port getPort1() { return port1; }
    public Port getPort2() { return port2; }

    @Override
    public void out(int data, int registerA) {
        // data has the device # in it.
        switch(data) {
            case 2:
                shiftOffset = 8 - registerA;
                break;
            case 3:
                // Sound
                break;
            case 4:
                shiftRegister >>>= 8;
                shiftRegister |= registerA << 8;
                break;
            case 5:
                // Sound
                break;
        }
    }

    @Override
    public int in(int data) {
        int registerA = 0;

        switch(data) {
            case 1:
                registerA = port1.getPort();
                port1.setPort(port1.getPort() & 0xfe); // turn everything but first bit on
                break;
            case 2:
                registerA = port2.getPort();
                break;
            case 3:
                registerA = (shiftRegister >>> shiftOffset) & 0xff; // keep it 8 bits
                break;
        }
        return registerA;
    }
}
