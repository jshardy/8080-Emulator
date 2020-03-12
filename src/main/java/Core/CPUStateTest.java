package Core;

import java.io.*;

public class CPUStateTest {
    int stateCount = 0;
    FileWriter f;
    BufferedWriter bf;
    FileReader fin;
    BufferedReader bfin;

    CPU cpu;
    CPUState previousState;

    public CPUStateTest(CPU c) {
        cpu = c;
        try {
            fin = new FileReader("/home/jshardy/Projects/SpaceInvaderTest/CPUText.txt");
            bfin = new BufferedReader(fin);
        } catch (FileNotFoundException e) {
            System.out.println("Unable to open CPUText.txt");
        }
    }

    public void checkState() {
        if(cpu != null && bfin != null) {
            try {
                int PC = Integer.parseInt(bfin.readLine());
                printCompare("PC", PC, cpu.getPC());

                int SP = Integer.parseInt(bfin.readLine());
                printCompare("SP", SP, cpu.getSP());

                int A = Integer.parseInt(bfin.readLine());
                printCompare("A", A, cpu.getA());
                int B = Integer.parseInt(bfin.readLine());
                printCompare("B", B, cpu.getB());
                int C = Integer.parseInt(bfin.readLine());
                printCompare("C", C, cpu.getC());
                int D = Integer.parseInt(bfin.readLine());
                printCompare("D", D, cpu.getD());
                int E = Integer.parseInt(bfin.readLine());
                printCompare("E", E, cpu.getE());
                int H = Integer.parseInt(bfin.readLine());
                printCompare("H", H, cpu.getH());
                int L = Integer.parseInt(bfin.readLine());
                printCompare("L", L, cpu.getL());

                boolean carry = Boolean.parseBoolean(bfin.readLine());
                printCompare("Carry", carry, cpu.getCarry());
                boolean zero = Boolean.parseBoolean(bfin.readLine());
                printCompare("Zero", zero, cpu.getZero());
                boolean sign = Boolean.parseBoolean(bfin.readLine());
                printCompare("Sign", sign, cpu.getSign());
                boolean parity = Boolean.parseBoolean(bfin.readLine());
                printCompare("Parity", parity, cpu.getParity());
                boolean auxcarry = Boolean.parseBoolean(bfin.readLine());
                printCompare("AuxCarry", auxcarry, cpu.getAuxCarry());
                int conditionBits = Integer.parseInt(bfin.readLine());
                printCompare("Condition Bits", conditionBits, cpu.getPSW());
                String stateNum = bfin.readLine();
                stateCount++;
                previousState = new CPUState(cpu);
                //printCompare("STATE", stateNum, "State: " + (stateCount++) + "\n");
            } catch (IOException e) {
                System.out.println("Error reading from CPUText.txt");
            }
        }
    }

    public void printCompare(String stringNames, int value1, int value2) {
        if(value1 != value2) {
            previousState.printState();
            previousState = new CPUState(cpu);
            previousState.printState();
            System.out.println("State: " + stateCount);
            System.out.println(stringNames + "1=" + value1 + " " + stringNames + "2=" + value2);
            System.out.println(cpu.getCurrentInstruction());
            System.exit(-1);
        }
    }

    public void printCompare(String stringNames, boolean value1, boolean value2) {
        if(value1 != value2) {
            previousState.printState();
            previousState = new CPUState(cpu);
            previousState.printState();
            System.out.println("State: " + stateCount);
            System.out.println(stringNames + "1=" + value1 + " " + stringNames + "2=" + value2);
            System.out.println(cpu.getCurrentInstruction());
            System.exit(-1);
        }
    }

    public void printCompare(String stringNames, String value1, String value2) {
        if(!value1.equals(value2)) {
            previousState.printState();
            previousState = new CPUState(cpu);
            previousState.printState();
            System.out.println("State: " + stateCount);
            System.out.println(stringNames + "1=" + value1 + " " + stringNames + "2=" + value2);
            System.out.println(cpu.getCurrentInstruction());
            System.exit(-1);
        }
    }

    /*public void writeState() {
        if(bf == null) {
            f = new FileWriter("CPUTest.txt");
            bf = new BufferedWriter(f);
        }

        try {
            // Registers
            bf.write(PC + "\n");
            bf.write(SP + "\n");

            bf.write(A + "\n");
            bf.write(B + "\n");
            bf.write(C + "\n");
            bf.write(D + "\n");
            bf.write(E + "\n");
            bf.write(H + "\n");
            bf.write(L + "\n");

            // Flags
            bf.write(Carry + "\n");
            bf.write(Zero + "\n");
            bf.write(Sign + "\n");
            bf.write(Parity + "\n");
            bf.write(AuxCarry + "\n");
            bf.write(getConditionBits() + "\n");
            bf.write("State: " + (stateCount++) + "\n");
        } catch (IOException e) {
            System.out.println("Unable to write to CPUTest.txt");
        }
    }*/


}
