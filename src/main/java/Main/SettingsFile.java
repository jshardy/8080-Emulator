package Main;

import Core.*;
import java.io.*;

public class SettingsFile {
    public static byte[] loadROM(String ROMFile) {
        byte[] memBytes = new byte[0x4000];

        try {
            FileInputStream file = new FileInputStream(ROMFile);
            try {
                memBytes = file.readAllBytes();
            } catch (IOException e) {
                System.out.println("Error reading ROM file: " + e.getMessage());
            }
        } catch(FileNotFoundException e) {
            System.out.println("File not found: " + e.getMessage());
        }
        return memBytes;
    }

    public static void saveState(String StateFile, Memory memory, CPU cpu, SpaceInvadersIO io) {
        try {
            // memory file
            File file = new File(StateFile + ".mem");
            file.createNewFile();
            FileOutputStream memFile = new FileOutputStream(file);
            saveMemory(memory, memFile);
            memFile.flush();
            memFile.close();

            // CPU registers file
            file = new File(StateFile + ".reg");
            file.createNewFile();
            FileOutputStream regFile = new FileOutputStream(file);
            saveCPU(cpu, regFile);
            regFile.flush();
            regFile.close();

            // IO file
            file = new File(StateFile + ".io");
            file.createNewFile();
            FileOutputStream ioFile = new FileOutputStream(file);
            saveIO(ioFile, io);
            ioFile.flush();
            ioFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("Error: saveState() - file not found - " + StateFile);
        } catch (IOException e) {
            System.out.println("Error: saveState() exception");
        }
    }

    public static void saveMemory(Memory memory, FileOutputStream memFile) {
        byte[] memBytes = new byte[0x4000];

        for(int i = 0; i < memBytes.length; i++) {
            memBytes[i] = (byte) (0xff & memory.readByte(i));
        }

        try {
            memFile.write(memBytes);
        } catch (IOException e) {
            System.out.println("Error: saveMemory()");
        }
    }

    public static byte[] loadMemory(FileInputStream memFile) {
        byte[] memBytes;

        try {
            memBytes = memFile.readAllBytes();
        } catch (IOException e) {
            System.out.println("Error: loadMemory()");
            return null;
        }

        return memBytes;
    }

    public static void saveCPU(CPU cpu, FileOutputStream cpuFile) {
        try {
            byte temp;
            temp = (byte) (cpu.getA());
            cpuFile.write(temp);

            temp = (byte) (cpu.getB());
            cpuFile.write(temp);

            temp = (byte) (cpu.getC());
            cpuFile.write(temp);

            temp = (byte) (cpu.getD());
            cpuFile.write(temp);

            temp = (byte) (cpu.getE());
            cpuFile.write(temp);

            temp = (byte) (cpu.getH());
            cpuFile.write(temp);

            temp = (byte) (cpu.getL());
            cpuFile.write(temp);

            temp = (byte) (cpu.getPSW());
            cpuFile.write(temp);

            temp = (byte) (cpu.getPC());
            cpuFile.write(temp);

            temp = (byte) (cpu.getSP());
            cpuFile.write(temp);

            temp = (byte) (cpu.getInterrupts() ? 1 : 0);
            cpuFile.write(temp);

        } catch (IOException e) {
            System.out.println("Error: saveCPU()");
        }
    }

    public static CPU loadCPU(FileInputStream cpuFile, Memory memory, InputOutput io) {
        byte[] cpuBytes = new byte[11];

        try {
            cpuBytes = cpuFile.readAllBytes();
        } catch (IOException e) {
            System.out.println("Error: loadCPU() - couldn't load file - " + cpuFile);
            return null;
        }
        CPU cpu = new CPU(memory, io);

        cpu.setA(0xff & cpuBytes[0]);
        cpu.setB(0xff & cpuBytes[1]);
        cpu.setC(0xff & cpuBytes[2]);
        cpu.setD(0xff & cpuBytes[3]);
        cpu.setE(0xff & cpuBytes[4]);
        cpu.setH(0xff & cpuBytes[5]);
        cpu.setL(0xff & cpuBytes[6]);
        cpu.setPSW(0xff & cpuBytes[7]);
        cpu.setPC(0xff & cpuBytes[8]);
        cpu.setSP(0xff & cpuBytes[9]);
        cpu.setInterrupts(cpuBytes[10] > 0);
        cpu.previousState = new CPUState(cpu);

        return cpu;
    }

    public static SpaceInvadersIO loadIO(FileInputStream ioFile) {
        byte[] ioBytes;
        SpaceInvadersIO io = null;

        try {
            ioBytes = ioFile.readAllBytes();
            io = new SpaceInvadersIO();
            io.getPort1().setPort(0xff & ioBytes[0]);
            io.getPort2().setPort(0xff & ioBytes[1]);
            io.getPort3().setPort(0xff & ioBytes[2]);
            io.setShiftRegister(ioBytes[3] | (ioBytes[4] << 8));
            io.setShiftOffset(ioBytes[5] | (ioBytes[6] << 8));

        } catch (IOException e) {
            System.out.println("Error: loadIO() - File not found - " + ioFile);
        }

        return io;
    }

    public static void saveIO(FileOutputStream ioFile, SpaceInvadersIO io) {
        byte[] ioBytes = new byte[7];
        ioBytes[0] = (byte) (0xff & io.getPort1().getPort());
        ioBytes[1] = (byte) (0xff & io.getPort2().getPort());
        ioBytes[2] = (byte) (0xff & io.getPort3().getPort());

        byte hi, lo;
        int shiftRegister = io.getShiftRegister();
        hi = (byte) (shiftRegister >>> 8);
        lo = (byte) (0xff & shiftRegister);
        ioBytes[3] = lo; // little endian
        ioBytes[4] = hi;

        int shiftOffset = io.getShiftOffset();
        hi = (byte) (shiftOffset >>> 8);
        lo = (byte) (0xff & shiftOffset);
        ioBytes[5] = lo;
        ioBytes[6] = hi;

        try {
            ioFile.write(ioBytes);
        } catch (IOException e) {
            System.out.println("Error: saveIO()");
        }
    }
}