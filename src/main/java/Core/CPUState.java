package Core;

import Core.GUI.Utilities.Utils;

public class CPUState implements java.io.Serializable {
    public int A;
    public int B;
    public int C;
    public int D;
    public int E;
    public int H;
    public int L;

    public int SP;
    public int PC;

    public boolean carry;
    public boolean zero;
    public boolean sign;
    public boolean parity;
    public boolean auxcarry;

    public String instruction;

    public CPUState(CPU cpu) {
        A = cpu.getA();
        B = cpu.getB();
        C = cpu.getC();
        D = cpu.getD();
        E = cpu.getE();
        H = cpu.getH();
        L = cpu.getL();

        PC = cpu.getPC();
        SP = cpu.getSP();

        carry = cpu.getCarry();
        zero = cpu.getZero();
        sign = cpu.getSign();
        parity = cpu.getParity();
        auxcarry = cpu.getAuxCarry();

        instruction = cpu.getCurrentInstruction();
    }

    public String getA() {
        return Utils.nString.hexToString8(A);
    }

    public String getB() {
        return Utils.nString.hexToString8(B);
    }

    public String getC() {
        return Utils.nString.hexToString8(C);
    }

    public String getBC() {
        return Utils.nString.hexToString16((B << 8) | C);
    }

    public String getD() {
        return Utils.nString.hexToString8(D);
    }

    public String getE() {
        return Utils.nString.hexToString8(E);
    }

    public String getDE() {
        return Utils.nString.hexToString16((D << 8) | E);
    }

    public String getH() {
        return Utils.nString.hexToString8(H);
    }

    public String getL() {
        return Utils.nString.hexToString8(L);
    }

    public String getHL() {
        return Utils.nString.hexToString16((H << 8) | L);
    }

    public String getInstruction() {
        return instruction;
    }

    void printState() {
        System.out.println(instruction);
        System.out.print("A=" + A + " ");
        System.out.print("B=" + B + " ");
        System.out.print("C=" + C + " ");
        System.out.print("D=" + D + " ");
        System.out.print("E=" + E + " ");
        System.out.print("H=" + H + " ");
        System.out.print("L=" + L + " ");
        System.out.print("\n");
        System.out.print("PC=" + PC + " ");
        System.out.print("SP=" + SP + " ");
        System.out.print("\n");
        System.out.println("Carry=" + carry + " Zero=" + zero + " Sign=" + sign + " Parity=" + parity + " AuxCarry=" + auxcarry);
    }
}
