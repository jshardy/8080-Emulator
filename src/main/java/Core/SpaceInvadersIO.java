package Core;

public class SpaceInvadersIO implements InputOutput {
    private Port port1 = new Port();
    private Port port2 = new Port();
    private Port port3 = new Port();
    private int shiftRegister = 0;
    private int shiftOffset = 0;
    private static final int sound_count = 18;
    private static String[] soundFiles = new String[sound_count];
    private Sound[] sounds = new Sound[sound_count];

    public int getShiftRegister() {
        return shiftRegister;
    }
    public int getShiftOffset() {
        return shiftOffset;
    }
    public Port getPort1() { return port1; }
    public Port getPort2() { return port2; }
    public Port getPort3() { return port3; }
    static long timeMillis = System.currentTimeMillis();

    public SpaceInvadersIO() {
        generateSoundFileNames();
        loadSounds();
    }

    @Override
    public void out(int data, int registerA) {
        // data has the device # in it.
        /*
        *The game will play fine without sound files*
        The sound chip for Space Invaders is analog, because of this
        I'm fairly certain the CPU keeps calling the chip over and over
        again, trying to hold that analog circuit open, so that it goes
        through it's sound.

        This means that playing waves file, over and over again will not
        work appropriately. This is "okay", but could be better. For the
        analog shooting sound, I have limited the sound to once every
        second, so that it doesn't sound like it's shooting over and over
        again.
         */
        switch(data) {
            case 2:
                shiftOffset = 8 - registerA;
                break;
            case 3:
                // Sound
                switch (registerA & 0x1f) {
                    case 0x01:
                        sounds[0].setLoop(20);
                        sounds[0].play();
                        break;
                    case 0x02:
                        if((System.currentTimeMillis() - timeMillis) > 1000) {
                            sounds[1].play();
                            timeMillis = System.currentTimeMillis();
                        }
                        break;
                    case 0x04:
                        sounds[2].play();
                        break;
                    case 0x08:
                        sounds[3].play();
                        break;
                    case 0x1f:
                        sounds[9].play();
                        break;
                }
                break;
            case 4:
                shiftRegister >>>= 8;
                shiftRegister |= registerA << 8;
                break;
            case 5:
                // Sound
                switch(registerA & 0x1f) {
                    case 0x1:
                        sounds[4].play();
                        break;
                    case 0x02:
                        sounds[5].play();
                        break;
                    case 0x04:
                        sounds[6].play();
                        break;
                    case 0x08:
                        sounds[7].play();
                        break;
                    case 0x10:
                        sounds[8].play();
                        break;
                }
                break;
            case 6:
                // Debug?
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

    private void generateSoundFileNames() {
        for(int i = 0; i < sound_count; i++) {
            soundFiles[i] = "./src/sounds/" + i + ".wav";
        }
    }

    private void loadSounds() {
        for(int i = 0; i < sound_count; i++) {
            sounds[i] = new Sound(soundFiles[i]);
        }
    }
}
