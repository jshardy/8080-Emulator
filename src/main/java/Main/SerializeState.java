package Main;

import Core.CPU;
import Core.Memory;
import Core.SpaceInvadersIO;

import java.io.*;

public class SerializeState {
    public static void saveCPU(String filename, CPU cpu) {
        try {
            FileOutputStream cpuFile = new FileOutputStream(filename);
            ObjectOutputStream cpuOBJ = new ObjectOutputStream(cpuFile);
            cpuOBJ.writeObject(cpu);
            cpuOBJ.close();
            cpuFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("saveCPU() - file not found - " + filename);
        } catch (IOException e) {
            System.out.println("saveCPU() - unkown IO error");
            e.printStackTrace();
        }
    }

    public static CPU loadCPU(String filename) {
        CPU cpu = null;
        try {
            FileInputStream cpuFile = new FileInputStream(filename);
            ObjectInputStream cpuOBJ = new ObjectInputStream(cpuFile);
            cpu = (CPU) cpuOBJ.readObject();
            cpuOBJ.close();
            cpuFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("loadCPU() - file not found - " + filename);
        } catch (IOException e) {
            System.out.println("loadCPU() - unknown IO error");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("loadCPU() - unknown serialized object");
        }
        return cpu;
    }

    public static void saveMemory(String filename, Memory memory) {
        try {
            FileOutputStream memFile = new FileOutputStream(filename);
            ObjectOutputStream memOBJ = new ObjectOutputStream(memFile);
            memOBJ.writeObject(memory);
            memOBJ.close();
            memFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("saveMemory() - file not found - " + filename);
        } catch (IOException e) {
            System.out.println("saveMemory() - unknown IO error");
            e.printStackTrace();
        }
    }

    public static Memory loadMemory(String filename) {
        Memory memory = null;
        try {
            FileInputStream memFile = new FileInputStream(filename);
            ObjectInputStream memOBJ = new ObjectInputStream(memFile);
            memory = (Memory) memOBJ.readObject();
            memOBJ.close();
            memFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("loadCPU() - file not found - " + filename);
        } catch (IOException e) {
            System.out.println("loadCPU() - unknown IO error");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("loadCPU() - unknown serialized object");
            e.printStackTrace();
        }
        return memory;
    }

    public static void saveIO(String filename, SpaceInvadersIO io) {
        try {
            FileOutputStream ioFile = new FileOutputStream(filename);
            ObjectOutputStream ioOBJ = new ObjectOutputStream(ioFile);
            ioOBJ.writeObject(io);
            ioOBJ.close();
            ioFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("saveIO() - file not found - " + filename);
        } catch (IOException e) {
            System.out.println("saveIO() - unknown IO error");
            e.printStackTrace();
        }
    }

    public static SpaceInvadersIO loadIO(String filename) {
        SpaceInvadersIO io = null;
        try {
            FileInputStream ioFile = new FileInputStream(filename);
            ObjectInputStream ioOBJ = new ObjectInputStream(ioFile);
            io = (SpaceInvadersIO) ioOBJ.readObject();
            ioOBJ.close();
            ioFile.close();
        } catch (FileNotFoundException e) {
            System.out.println("loadIO() - file not found - " + filename);
        } catch (IOException e) {
            System.out.println("loadIO() - unknown IO error");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            System.out.println("loadIO() - unknown serialized object");
            e.printStackTrace();
        }
        return io;
    }
}
