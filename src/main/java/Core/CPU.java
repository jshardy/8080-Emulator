package Core;

import Utilities.Utils.nString;

import java.io.IOException;
import java.util.concurrent.Semaphore;

public class CPU implements java.io.Serializable {
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        cpuBusy = new Semaphore(1);
    }

    private class Registers implements java.io.Serializable {
        public int B; // 0
        public int C; // 1
        public int D; // 2
        public int E; // 3
        public int H; // 4
        public int L; // 5
        // M = memory // 6
        public int A; // 7

        public int BC() {
            return (B << 8) | C;
        }

        public void BC(int value) {
            B = (value >>> 8) & 0xff;
            C = value & 0xff;
        }

        public int DE() {
            return (D << 8) | E;
        }

        public void DE(int value) {
            D = (value >>> 8) & 0xff;
            E = value & 0xff;
        }

        public int HL() {
            return (H << 8) | L;
        }

        public void HL(int value) {
            H = (value >>> 8) & 0xff;
            L = value & 0xff;
        }
    }

    private class Flags implements java.io.Serializable {
        public boolean carry;
        public boolean zero;
        public boolean sign;
        public boolean parity;
        public boolean auxCarry;

        public int getPSW() {
            Port p = new Port();
            p.setBit(0, carry);
            p.setBit(1, true);
            p.setBit(2, parity);
            p.setBit(3, false);
            p.setBit(4, auxCarry);
            p.setBit(5, false);
            p.setBit(6, zero);
            p.setBit(7, sign);
            return p.getPort();
        }

        public void setPSW(int value) {
            Port p = new Port(value);
            carry = p.getBit(0);
            parity = p.getBit(2);
            auxCarry = p.getBit(4);
            zero = p.getBit(6);
            sign = p.getBit(7);
        }
    }

    private Memory memory;
    private Registers register = new Registers();
    public CPUState previousState;
    public InputOutput io;
    private Flags flag = new Flags();
    private String currentInstruction;
    private int PC = 0;
    private int oldPC = 0;
    private int length = 0;
    private int SP = 0;
    private boolean interrupts = false;
    private boolean halt = false;
    public transient Semaphore cpuBusy = new Semaphore(1);

    public CPU(Memory mem, InputOutput inout) {
        memory = mem;
        io = inout;
        previousState = new CPUState(this);
    }

    public void setInputOutput(InputOutput inputOutput) {
        io = inputOutput;
    }
    public InputOutput getInputOutput() { return io; }
    public void setMemory(Memory mem) { memory = mem; }
    public Memory getMemory() {
        return memory;
    }

    public int getPC() { return PC; }
    public void setPC(int newPC) { PC = newPC; }
    public int getCurrentInstructionAddress() { return oldPC; }
    public int getSP() { return SP; }
    public void setSP(int newSP) { SP = newSP; }
    public int getA() { return register.A; }
    public void setA(int newA) { register.A = newA; }
    public int getB() { return register.B; }
    public void setB(int newB) { register.B = newB; }
    public int getC() { return register.C; }
    public void setC(int newC) { register.C = newC; }
    public int getD() { return register.D; }
    public void setD(int newD) { register.D = newD; }
    public int getE() { return register.E; }
    public void setE(int newE) { register.E = newE; }
    public int getH() { return register.H; }
    public void setH(int newH) { register.H = newH; }
    public int getL() { return register.L; }
    public void setL(int newL) { register.L = newL; }
    public int getBC() { return register.BC(); }
    public void setBC(int newBC) { register.BC(newBC); }
    public int getDE() { return register.DE(); }
    public void setDE(int newDE) { register.DE(newDE); }
    public int getHL() { return register.HL(); }
    public void setHL(int newHL) { register.HL(newHL); }
    public int getPSW() { return flag.getPSW(); }
    public void setPSW(int value) { flag.setPSW(value); }

    public boolean getCarry() { return flag.carry; }
    public void setCarry(boolean value) { flag.carry = value; }
    public boolean getZero() { return flag.zero; }
    public void setZero(boolean value) { flag.zero = value; }
    public boolean getSign() { return flag.sign; }
    public void setSign(boolean value) { flag.sign = value; }
    public boolean getParity() { return flag.parity; }
    public void setParity(boolean value) { flag.parity = value; }
    public boolean getAuxCarry() { return flag.auxCarry; }
    public void setAuxCarry(boolean value) { flag.auxCarry = value; }
    public boolean getInterrupts() { return interrupts; }
    public void setInterrupts(boolean value) { interrupts = value; }

    public String getRAW3Byte() {
        StringBuilder sb = new StringBuilder();

        for(int i = 0; i < length; i++) {
            int value = memory.readByte(oldPC + i);
            sb.append(nString.hexToString8(value).substring(2));
        }

        return sb.toString();
    }

    public String getCurrentInstruction() {
        return currentInstruction;
    }

    private void inxBC() {
        if(++register.C > 0xff) {
            register.C = 0;
            if(++register.B > 0xff) {
                register.B = 0;
            }
        }
    }

    private void dcxBC() {
        if(--register.C < 0) {
            register.C = 0xff;
            if(--register.B < 0) {
                register.B = 0xff;
            }
        }
    }

    private void inxDE() {
        if(++register.E > 0xff) {
            register.E = 0;
            if(++register.D > 0xff) {
                register.D = 0;
            }
        }
    }

    private void dcxDE() {
        if(--register.E < 0) {
            register.E = 0xff;
            if(--register.D < 0) {
                register.D = 0xff;
            }
        }
    }

    private void inxHL() {
        if(++register.L > 0xff) {
            register.L = 0;
            if(++register.H > 0xff) {
                register.H = 0;
            }
        }
    }

    private void dcxHL() {
        if(--register.L < 0) {
            register.L = 0xff;
            if(--register.H < 0) {
                register.H = 0xff;
            }
        }
    }

    private int inr(int registerValue) {
        // S Z A P
        registerValue++;
        registerValue &= 0xff; // <255
        zero(registerValue);
        sign8(registerValue);
        flag.auxCarry = registerValue > 0x9;
        flag.parity = checkParity(registerValue, 8);

        return registerValue;
    }

    private int dcr(int registerValue) {
        // S Z A P
        int x = registerValue & 0xf0; // save high nibble
        registerValue--;
        registerValue &= 0xff; // <255
        sign8(registerValue);
        zero(registerValue);
        flag.auxCarry = registerValue > 0x9;
        flag.parity = checkParity(registerValue, 8);
        return registerValue;
    }

    private void add(int registerValue) {
        // S Z A P C
        int x = registerValue + register.A;
        flag.carry = (x & 0xf00) != 0;
        auxCarry8(register.A, registerValue);
        register.A += registerValue;
        register.A &= 0xff;
        sign8(register.A);
        zero(register.A);
        parity8(register.A);
    }

    private void adc(int registerValue) {
        // S Z A C P
        int x = register.A + registerValue;
        x += flag.carry ? 1 : 0;

        if(flag.carry) {
            flag.auxCarry = ((x & 0xf) + (registerValue & 0xf)) >= 0xf;
        } else {
            auxCarry8(x, registerValue);
        }
        flag.carry = (x & 0xff00) > 0;
        sign8(x);
        zero(x);
        parity8(x);
        register.A = x & 0xff;
    }

    private void sub(int registerValue) {
        // S Z A C P
        int x = register.A - registerValue;
        flag.carry = (((x & 0xff00) >= register.A) && (registerValue > 0));
        auxCarry8(register.A, registerValue);
        sign8(x);
        zero(x);
        parity8(x);
        register.A = (x & 0xff); // <256
    }

    private void sbb(int registerValue) {
        // S Z A C P
        int x = register.A - registerValue;

        if(flag.carry) {
            x -= 1;
        }
        if(flag.carry) {
            flag.auxCarry = (registerValue & 0xf) < (register.A & 0xf);
        } else {
            flag.auxCarry = (registerValue & 0xf) <= (register.A & 0xf);
        }
        //auxCarry8(register.A, registerValue);
        flag.carry = ((x & 0xff) >= register.A) && ((registerValue > 0) | flag.carry);

        x &= 0xff; // <256
        sign8(x);
        zero(x);
        parity8(x);
        register.A = x;
    }

    private void ana(int registerValue) {
        // S Z A P C
        flag.auxCarry = (registerValue & 0xf) <= (register.A & 0xf);
        flag.carry = false;
        register.A &= registerValue;
        sign8(register.A);
        zero(register.A);
        parity8(register.A);
    }

    private void xra(int registerValue) {
        // S Z A P C
        register.A ^= registerValue;
        sign8(register.A);
        zero(register.A);
        auxCarry8(register.A, registerValue);
        parity8(register.A);
        flag.carry = false;
    }

    private void ora(int registerValue) {
        // S Z A P C
        auxCarry8(register.A, registerValue);
        register.A |= registerValue;
        sign8(register.A);
        zero(register.A);
        parity8(register.A);
        flag.carry = false;
    }

    private void cmp(int registerValue) {
        // S Z A P C
        int x = register.A - registerValue;
        flag.carry = (x & 0xf00) != 0;
        auxCarry8(register.A, registerValue);
        sign8(x);
        zero(x);
        parity8(x);
    }

    private void auxCarry8(int value1, int value2) {
        // Add lower nibble together, does it cross byte 3 boundary?
        flag.auxCarry = ((value1 & 0xf) + (value2 & 0xf)) > 0xf;
    }

    private void sign8(int value) {
        flag.sign = (value & 0x80) != 0;
    }

    private void carry8(int value) {
        flag.carry = (value & 0x80) > 0;
    }

    private void carry16(int value) {
        flag.carry = (value & 0xFF00) > 0;
    }

    private void zero(int value) {
        flag.zero = (value == 0);
    }

    private void parity8(int value) {
        flag.parity = checkParity(value, 8);
    }

    private boolean checkParity(int value, int size) {
        int count = 0;
        for(int i = 0; i < size; i++) {
            if((value & 0x1) == 1) {
                count++;
            }
            value >>= 1;
        }
        return count % 2 == 0;
    }

    public void interrupt(int interrupt_num) {
        interrupts = false;

        SP -= 2;
        memory.writeWord(SP, PC);

        switch(interrupt_num) {
            case 0xc7: // RST 0
                PC = 0x00;
                break;
            case 0xcf: // RST 1
                PC = 0x0008;
                break;
            case 0xd7: // RST 2
                PC = 0x0010;
                break;
            case 0xdf: // RST 3
                PC = 0x0018;
                break;
            case 0xe7: // RST 4
                PC = 0x0020;
                break;
            case 0xef: // RST 5
                PC = 0x0028;
                break;
            case 0xf7: // RST 6
                PC = 0x0030;
                break;
            case 0xff: // RST 7
                PC = 0x0038;
                break;
        }
    }

    public int stepExecute() {
        StringBuilder sb = new StringBuilder();
        int instruction = memory.readByte(PC);
        int cycles = 0;
        int address = 0;
        int data = 0;
        oldPC = PC;

        switch(instruction) {
            case 0x00:
            case 0x08: // All NOP
            case 0x10:
            case 0x18:
            case 0x20:
            case 0x28:
            case 0x30:
            case 0x38:
                // Cycles 4, no flags
                // Do nothing
                length = 1;
                sb.append("NOP");
                cycles = 4;
                PC++;
                break;
            case 0x01:
                // Cycles 10, no flags
                // Load next two bytes into CD
                length = 3;
                sb.append("LXI BC, ");
                cycles = 10;
                register.C = memory.readByte(++PC);
                register.B = memory.readByte(++PC);
                sb.append(nString.hexToString16(register.BC()));
                PC++;
                break;
            case 0x02:
                // Cycles 7, no flags
                // Copy register A value into address at BC
                length = 1;
                sb.append("MOV [BC], A - STAX BC");
                cycles = 7;
                memory.writeByte(register.BC(), register.A);
                PC++;
                break;
            case 0x03:
                // Cycles 5, no flags
                // BC++
                length = 1;
                sb.append("INX BC");
                cycles = 5;
                inxBC();
                PC++;
                break;
            case 0x04:
                // Cycles 5, flags S Z A P
                // B++
                length = 1;
                sb.append("INR B");
                cycles = 5;
                register.B = inr(register.B);
                PC++;
                break;
            case 0x05:
                // Cycles 5. flags S Z A P
                // Decrements register R by one.  R=R-1
                length = 1;
                sb.append("DCR B");
                cycles = 5;
                register.B = dcr(register.B);
                PC++;
                break;
            case 0x06:
                // Cycles 7, no flags
                // Move immediate to register
                length = 2;
                sb.append("MVI B, ");
                cycles = 7;
                register.B = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.B));
                PC++;
                break;
            case 0x07:
                // Cycles 4, flags C
                // Rotate A left
                length = 1;
                sb.append("RLC");
                cycles = 4;
                carry8(register.A);
                register.A = (register.A << 1) | (register.A >> 7); // carry bit 7 back to bit 0
                PC++;
                break;
            case 0x09:
                // Cycles 10, flags C
                // Add register pair to HL (16 bit add)
                length = 1;
                sb.append("DAD HL, BC");
                cycles = 10;
                //carry16(register.BC() + register.HL());
                //flag.carry = ((register.BC() + register.HL()) & 0xFFFF0000) > 0; // did the calculation go over 16 bits?
                flag.carry = (register.BC() & 0xf0000) != 0;
                register.HL(register.BC() + register.HL());
                PC++;
                break;
            case 0x0a:
                // Cycle 7, no flags
                // Load indirect through BC or DE
                length = 1;
                sb.append("MOV A, B - LDAX B");
                cycles = 7;
                register.A = memory.readByte(register.BC());
                PC++;
                break;
            case 0x0b:
                // Cycles 5, no flags
                // Decrement register pair
                length = 1;
                sb.append("DCX BC");
                cycles = 5;
                dcxBC();
                //register.BC(register.BC() - 1);
                PC++;
                break;
            case 0x0c:
                // Cycles 5, flags S Z A P
                // Increment register
                length = 1;
                sb.append("INR C");
                cycles = 5;
                register.C = inr(register.C);
                PC++;
                break;
            case 0x0d:
                // Cycles 5, flags S Z A P
                // Decrement register
                length = 1;
                sb.append("DCR C");
                cycles = 5;
                register.C = dcr(register.C);
                PC++;
                break;
            case 0x0e:
                // Cycles 7, no flags
                // Move immediate to register
                length = 2;
                sb.append("MVI C, ");
                cycles = 7;
                register.C = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.C));
                PC++;
                break;
            case 0x0f:
                // Cycles 4, flags C
                // Rotate A right through carry
                length = 1;
                sb.append("RRC");
                cycles = 4;
                flag.carry = (register.A & 1) == 1; // check if first bit gets cut off
                register.A = register.A >>> 1;
                register.A |= flag.carry ? 0x80 : 0; // put first bit back as 7th(last) bit
                PC++;
                break;
            case 0x11:
                // Cycles 10, no flags
                // Load register pair immediate
                length = 3;
                sb.append("LXI DE, ");
                cycles = 10;
                register.E = memory.readByte(++PC);
                register.D = memory.readByte(++PC);
                sb.append(nString.hexToString16(register.DE()));
                PC++;
                break;
            case 0x12:
                // Cycles 7, no flags
                // Store indirect through BC or DE
                length = 1;
                sb.append("MOV [DE], A - STAX DE");
                cycles = 7;
                memory.writeByte(register.DE(), register.A);
                PC++;
                break;
            case 0x13:
                // Cycles 5, no flags
                // Increment register pair
                length = 1;
                sb.append("INX DE");
                cycles = 5;
                inxDE();
                //register.DE(register.DE() + 1);
                PC++;
                break;
            case 0x14:
                // Cycles 5, flags S Z A P
                // Increment register
                length = 1;
                sb.append("INR D");
                cycles = 5;
                register.D = inr(register.D);
                PC++;
                break;
            case 0x15:
                // Cycles 5, flags S Z A P
                // Decrement register
                length = 1;
                sb.append("DCR D");
                cycles = 5;
                register.D = dcr(register.D);
                PC++;
                break;
            case 0x16:
                // Cycles 7, no flags
                // Move immediate to register
                length = 2;
                sb.append("MVI D, ");
                cycles = 7;
                register.D = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.D));
                PC++;
                break;
            case 0x17:
                // Cycles 4, flags C
                // Rotate A left through carry
                length = 1;
                sb.append("RAL");
                cycles = 4;
                // Save original carry
                data = flag.carry ? 1 : 0;
                carry8(register.A);
                register.A = (register.A << 1) | data;
                register.A &= 0xff; // <255
                PC++;
                break;
            case 0x19:
                // Cycles 10, flags C
                // Add register pair to HL (16 bit add)
                length = 1;
                sb.append("DAD DE");
                cycles = 10;
                data = register.DE() + register.HL();
                flag.carry = (register.DE() & 0xf0000) != 0;
                register.HL(data);
                PC++;
                break;
            case 0x1a:
                // Cycles 7, no flags
                // Load indirect through BC or DE
                length = 1;
                sb.append("MOV A, [DE] - LDAX D, [DE]");
                cycles = 7;
                register.A = memory.readByte(register.DE());
                PC++;
                break;
            case 0x1b:
                // Cycles 5, no flags
                // Decrement register pair
                length = 1;
                sb.append("DCX D");
                cycles = 5;
                dcxDE();
                //register.DE(register.DE() - 1);
                PC++;
                break;
            case 0x1c:
                // Cycles 5, flags S Z A P
                // Increment register
                length = 1;
                sb.append("INR E");
                cycles = 5;
                register.E = inr(register.E);
                PC++;
                break;
            case 0x1d:
                // Cycles 5, S Z A P
                // Decrement register
                length = 1;
                sb.append("DCR E");
                cycles = 5;
                register.E = dcr(register.E);
                PC++;
                break;
            case 0x1e:
                // Cycles 7, no flags
                // Move immediate to register
                length = 2;
                sb.append("MVI E, ");
                cycles = 7;
                register.E = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.E));
                PC++;
                break;
            case 0x1f:
                // Cycles 4, flags C
                // Rotate A right through carry
                length = 1;
                sb.append("RAR");
                cycles = 4;
                data = flag.carry ? 0x80 : 0; // carry set?
                flag.carry = (register.A & 1) > 0;
                register.A = (register.A >>> 1) | data; // put carry back in at bit 7
                PC++;
                break;
            case 0x21:
                // Cycles 10, no flags
                // Load register pair immediate
                length = 3;
                sb.append("LXI H, ");
                cycles = 10;
                register.HL(memory.readWord(++PC));
                PC++;
                //register.L = memory.readByte(++PC);
                //register.H = memory.readByte(++PC);
                sb.append(nString.hexToString16(register.HL()));
                PC++;
                break;
            case 0x22:
                // Cycles 16
                // Store H:L to memory
                length = 3;
                sb.append("SHLD ");
                cycles = 16;
                address = memory.readWord(++PC);
                PC++;
                sb.append(nString.hexToString16(address));
                //memory.writeWord(address, register.L, register.H);
                memory.writeWord(address, register.HL());
                PC++;
                break;
            case 0x23:
                // Cycles 5, no flags
                // Increment register pair
                length = 1;
                sb.append("INX HL");
                cycles = 5;
                inxHL();
                //register.HL(register.HL() + 1);
                PC++;
                break;
            case 0x24:
                // Cycles 5, flags S Z A P
                // Increment register
                length = 1;
                sb.append("INR H");
                cycles = 5;
                register.H = inr(register.H);
                PC++;
                break;
            case 0x25:
                // Cycles 5, flags S Z A P
                // Decrement register
                length = 1;
                sb.append("DCR H");
                cycles = 5;
                register.H = dcr(register.H);
                PC++;
                break;
            case 0x26:
                // Cycles 7, no flags
                // Move immediate to register
                length = 2;
                sb.append("MVI H, ");
                cycles = 7;
                data = memory.readByte(++PC);
                register.H = data;
                sb.append(nString.hexToString8(data));
                PC++;
                break;
            case 0x27:
                // Cycles 4, S Z A C P
                // Decimal Adjust accumulator
                length = 1;
                sb.append("DAA - Not implemented");
                cycles = 4;
                PC++;
                break;
            case 0x29:
                // Cycles 10, flags C
                // Add register pair to HL (16 bit add)
                length = 1;
                sb.append("DAD HL");
                cycles = 10;
                data = register.HL() + register.HL();
                flag.carry = (register.HL() & 0x10000) != 0;
                //carry16(data);
                register.HL(data);
                PC++;
                break;
            case 0x2a:
                // Cycles 16, no flags
                // Load H:L from memory
                length = 3;
                sb.append("LHLD HL, [");
                cycles = 16;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address)).append("]");
                register.L = memory.readByte(address);
                register.H = memory.readByte(address + 1);
                PC += 2;
                break;
            case 0x2b:
                // Cycles 5, no flags
                // Decrement register pair
                length = 1;
                sb.append("DCX HL");
                cycles = 5;
                dcxHL();
                //register.HL(register.HL() - 1);
                PC++;
                break;
            case 0x2c:
                // Length 1
                // Cycles 5, flags S Z A P
                // Increment register
                length = 1;
                sb.append("INR L");
                cycles = 5;
                register.L = inr(register.L);
                PC++;
                break;
            case 0x2d:
                // Cycles 5, flags S Z A P
                // Decrement register
                length = 1;
                sb.append("DCR L");
                cycles = 5;
                register.L = dcr(register.L);
                PC++;
                break;
            case 0x2e:
                // Cycles 7
                // Move immediate to register
                length = 2;
                sb.append("MVI L, ");
                cycles = 7;
                register.L = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.L));
                PC++;
                break;
            case 0x2f:
                // Cycles 4, no flags
                // Compliment A
                length = 1;
                sb.append("CMA");
                cycles = 4;
                register.A = ~register.A & 0xff; // <256;
                PC++;
                break;
            case 0x31:
                // Cycles 10, no flags
                // Load register pair immediate
                length = 3;
                sb.append("LXI SP, ");
                cycles = 10;
                SP = memory.readWord(++PC);
                sb.append(nString.hexToString16(SP));
                PC += 2;
                break;
            case 0x32:
                // Cycles 13, no flags
                // Store A to memory
                length = 3;
                sb.append("STA ");
                cycles = 13;
                address = memory.readWord(++PC);
                sb.append("[" + nString.hexToString16(address) + "], A");
                memory.writeByte(address, register.A);
                PC += 2;
                break;
            case 0x33:
                // Cycles 5, no flags
                // Increment register
                length = 1;
                sb.append("INX SP");
                cycles = 5;
                ++SP;
                SP &= 0xffff; // <65535 - cut off upper bits because using 32 bit int
                PC++;
                break;
            case 0x34:
                // Cycles 10, flags S Z A P
                // Increment memory INR M
                length = 1;
                cycles = 10;
                sb.append("INR [HL]");
                data = memory.readByte(register.HL());
                data = inr(data);
                memory.writeByte(register.HL(), data);
                PC++;
                break;
            case 0x35:
                // Cycles 10, flags S Z A P
                // Decrement memory DCR M
                length = 1;
                sb.append("DCR [HL]");
                cycles = 10;
                address = register.HL();
                data = memory.readByte(address);
                data = dcr(data);
                memory.writeByte(address, data);
                PC++;
                break;
            case 0x36:
                // Cycles 10, no flags
                // Move immediate to memory
                length = 2;
                sb.append("MVI [HL], ");
                cycles = 10;
                address = register.HL();
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                memory.writeByte(address, data);
                PC++;
                break;
            case 0x37:
                // Cycles 4, flags C
                // Set Carry flag
                length = 1;
                sb.append("STC");
                cycles = 4;
                flag.carry = true;
                PC++;
                break;
            case 0x39:
                // Cycles 10, flags C
                // Add register pair to HL (16 bit add)
                length = 1;
                sb.append("ADD HL, SP - DAD SP");
                cycles = 10;
                data = SP + register.HL();
                carry16(data);
                register.HL(data);
                PC++;
                break;
            case 0x3a:
                // Cycles 13, no flags
                // Load A from memory
                length = 3;
                sb.append("LDA ");
                cycles = 13;
                address = memory.readWord(++PC);
                sb.append("[").append(address).append("]");
                register.A = memory.readByte(address);
                PC += 2;
                break;
            case 0x3b:
                // Cycles 5, no flags
                // Decrement register pair
                length = 1;
                sb.append("DCX SP");
                cycles = 5;
                --SP;
                SP &= 0xffff; // <65536
                PC++;
                break;
            case 0x3c:
                // Cycles 5, flags S Z A P
                // Increment register A
                length = 1;
                sb.append("INR A");
                cycles = 5;
                register.A = inr(register.A);
                PC++;
                break;
            case 0x3d:
                // Cycles 5, flags S Z A P
                // Decrement register
                length = 1;
                sb.append("DCR A");
                cycles = 5;
                register.A = dcr(register.A);
                PC++;
                break;
            case 0x3e:
                // Cycles 7, no flags
                // Move immediate to register
                length = 2;
                sb.append("MVI A, ");
                cycles = 7;
                register.A = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.A));
                PC++;
                break;
            case 0x3f:
                // Cycles 4, flags C
                // Compliment Carry flag
                length = 1;
                sb.append("CMC");
                cycles = 4;
                flag.carry = !flag.carry;
                PC++;
                break;
            case 0x40:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV B, B");
                cycles = 5;
                PC++;
                break;
            case 0x41:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV B, C");
                cycles = 5;
                register.B = register.C;
                PC++;
                break;
            case 0x42:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV B, D");
                cycles = 5;
                register.B = register.D;
                PC++;
                break;
            case 0x43:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV B, E");
                cycles = 5;
                register.B = register.E;
                PC++;
                break;
            case 0x44:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV B, H");
                cycles = 5;
                register.B = register.H;
                PC++;
                break;
            case 0x45:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV B, L");
                cycles = 5;
                register.B = register.L;
                PC++;
                break;
            case 0x46:
                // Cycles 7, no flags
                // Move memory to register
                length = 1;
                sb.append("MOV B, [HL]");
                cycles = 7;
                register.B = memory.readByte(register.HL());
                PC++;
                break;
            case 0x47:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV B, A");
                cycles = 5;
                register.B = register.A;
                PC++;
                break;
            case 0x48:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV C, B");
                cycles = 5;
                register.C = register.B;
                PC++;
                break;
            case 0x49:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV C, C");
                cycles = 5;
                PC++;
                break;
            case 0x4a:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV C, D");
                cycles = 5;
                register.C = register.D;
                PC++;
                break;
            case 0x4b:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV C, E");
                cycles = 5;
                register.C = register.E;
                PC++;
                break;
            case 0x4c:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV C, H");
                cycles = 5;
                register.C = register.H;
                PC++;
                break;
            case 0x4d:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV C, L");
                cycles = 5;
                register.C = register.L;
                PC++;
                break;
            case 0x4e:
                // Cycles 7, no flags
                // Move register to register
                length = 1;
                sb.append("MOV C, [HL]");
                cycles = 7;
                register.C = memory.readByte(register.HL());
                PC++;
                break;
            case 0x4f:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV C, A");
                cycles = 5;
                register.C = register.A;
                PC++;
                break;
            case 0x50:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV D, B");
                cycles = 5;
                register.D = register.B;
                PC++;
                break;
            case 0x51:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV D, C");
                cycles = 5;
                register.D = register.C;
                PC++;
                break;
            case 0x52:
                // Cycles 5, no flags
                // move register to register
                length = 1;
                sb.append("MOV D, D");
                cycles = 5;
                PC++;
                break;
            case 0x53:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV D, E");
                cycles = 5;
                register.D = register.E;
                PC++;
                break;
            case 0x54:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV D, H");
                cycles = 5;
                register.D = register.H;
                PC++;
                break;
            case 0x55:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV D, L");
                cycles = 5;
                register.D = register.L;
                PC++;
                break;
            case 0x56:
                // Cycles 7, no flags
                // Move memory to register
                length = 1;
                sb.append("MOV D, [HL]");
                cycles = 7;
                register.D = memory.readByte(register.HL());
                PC++;
                break;
            case 0x57:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV D, A");
                cycles = 5;
                register.D = register.A;
                PC++;
                break;
            case 0x58:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV E, B");
                cycles = 5;
                register.E = register.B;
                PC++;
                break;
            case 0x59:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV E, C");
                cycles = 5;
                register.E = register.C;
                PC++;
                break;
            case 0x5a:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV E, D");
                cycles = 5;
                register.E = register.D;
                PC++;
                break;
            case 0x5b:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV E, E");
                cycles = 5;
                PC++;
                break;
            case 0x5c:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV E, H");
                cycles = 5;
                register.E = register.H;
                PC++;
                break;
            case 0x5d:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV E, L");
                cycles = 5;
                register.E = register.L;
                PC++;
                break;
            case 0x5e:
                // Cycles 7, no flags
                // Move memory to register
                length = 1;
                sb.append("MOV E, [HL]");
                cycles = 7;
                register.E = memory.readByte(register.HL());
                PC++;
                break;
            case 0x5f:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV E, A");
                cycles = 5;
                register.E = register.A;
                PC++;
                break;
            case 0x60:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV H, B");
                cycles = 5;
                register.H = register.B;
                PC++;
                break;
            case 0x61:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV H, C");
                cycles = 5;
                register.H = register.C;
                PC++;
                break;
            case 0x62:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV H, D");
                cycles = 5;
                register.H = register.D;
                PC++;
                break;
            case 0x63:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV H, E");
                cycles = 5;
                register.H = register.E;
                PC++;
                break;
            case 0x64:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV H, H");
                cycles = 5;
                PC++;
                break;
            case 0x65:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV H, L");
                cycles = 5;
                register.H = register.L;
                PC++;
                break;
            case 0x66:
                // Cycles 7, no flags
                // Move memory to register
                length = 1;
                sb.append("MOV H, [HL]");
                cycles = 7;
                register.H = memory.readByte(register.HL());
                PC++;
                break;
            case 0x67:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV H, A");
                cycles = 5;
                register.H = register.A;
                PC++;
                break;
            case 0x68:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV L, B");
                cycles = 5;
                register.L = register.B;
                PC++;
                break;
            case 0x69:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV L, C");
                cycles = 5;
                register.L = register.C;
                PC++;
                break;
            case 0x6a:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV L, D");
                cycles = 5;
                register.L = register.D;
                PC++;
                break;
            case 0x6b:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV L, E");
                cycles = 5;
                register.L = register.E;
                PC++;
                break;
            case 0x6c:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV L, H");
                cycles = 5;
                register.L = register.H;
                PC++;
                break;
            case 0x6d:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV L, L");
                cycles = 5;
                PC++;
                break;
            case 0x6e:
                // Cycles 7, no flags
                // Move memory to register
                length = 1;
                sb.append("MOV L, [HL]");
                cycles = 7;
                register.L = memory.readByte(register.HL());
                PC++;
                break;
            case 0x6f:
                // Cycles 5, no flags
                // Move register to register
                length = 1;
                sb.append("MOV L, A");
                cycles = 5;
                register.L = register.A;
                PC++;
                break;
            case 0x70:
                // Cycles 7, no flags
                // Move register B to memory
                length = 1;
                sb.append("MOV [HL], B");
                cycles = 7;
                memory.writeByte(register.HL(), register.B);
                PC++;
                break;
            case 0x71:
                // Cycles 7, no flags
                // Move register C to memory
                length = 1;
                sb.append("MOV [HL], C");
                cycles = 7;
                memory.writeByte(register.HL(), register.C);
                PC++;
                break;
            case 0x72:
                // Cycles 7, no flags
                // Move register D to memory
                length = 1;
                sb.append("MOV [HL], D");
                cycles = 7;
                memory.writeByte(register.HL(), register.D);
                PC++;
                break;
            case 0x73:
                // Cycles 7, no flags
                // Move register E to memory
                length = 1;
                sb.append("MOV [HL], E");
                cycles = 7;
                memory.writeByte(register.HL(), register.E);
                PC++;
                break;
            case 0x74:
                // Cycles 7, no flags
                // Move register H to memory
                length = 1;
                sb.append("MOV [HL], H");
                cycles = 7;
                memory.writeByte(register.HL(), register.H);
                PC++;
                break;
            case 0x75:
                // Cycles 7, no flags
                // Move register L to memory
                length = 1;
                sb.append("MOV [HL], L");
                cycles = 7;
                memory.writeByte(register.HL(), register.L);
                PC++;
                break;
            case 0x76:
                // Cycles 7, no flags
                // Halt processor
                length = 1;
                sb.append("HLT");
                cycles = 7;
                halt = true;
                System.out.println("HALTED!");
                PC++;
                break;
            case 0x77:
                // Cycles 7, no flags
                // Move register A to memory
                length = 1;
                sb.append("MOV [HL], A");
                cycles = 7;
                memory.writeByte(register.HL(), register.A);
                PC++;
                break;
            case 0x78:
                // Cycles 5, no flags
                // Move register B to register A
                length = 1;
                sb.append("MOV A, B");
                cycles = 5;
                register.A = register.B;
                PC++;
                break;
            case 0x79:
                // Cycles 5, no flags
                // Move register C to register A
                length = 1;
                sb.append("MOV A, C");
                cycles = 5;
                register.A = register.C;
                PC++;
                break;
            case 0x7a:
                // Cycles 5, no flags
                // Move register D to register A
                length = 1;
                sb.append("MOV A, D");
                cycles = 5;
                register.A = register.D;
                PC++;
                break;
            case 0x7b:
                // Cycles 5, no flags
                // Move register E to register A
                length = 1;
                sb.append("MOV A, E");
                cycles = 5;
                register.A = register.E;
                PC++;
                break;
            case 0x7c:
                // Cycles 5, no flags
                // Move register H to register A
                length = 1;
                sb.append("MOV A, H");
                cycles = 5;
                register.A = register.H;
                PC++;
                break;
            case 0x7d:
                // Cycles 5, no flags
                // Move register L to register A
                length = 1;
                sb.append("MOV A, L");
                cycles = 5;
                register.A = register.L;
                PC++;
                break;
            case 0x7e:
                // Cycles 7, no flags
                // Move memory to register A
                length = 1;
                sb.append("MOV A, [HL]");
                cycles = 7;
                register.A = memory.readByte(register.HL());
                PC++;
                break;
            case 0x7f:
                // Cycles 5, no flags
                // Move register A to register A
                length = 1;
                sb.append("MOV A, A");
                cycles = 5;
                PC++;
                break;
            case 0x80:
                // Cycles 4, flags S Z A P C
                // Add register B to A
                length = 1;
                sb.append("ADD B");
                cycles = 4;
                add(register.B);
                PC++;
                break;
            case 0x81:
                // Cycles 4, flags S Z A P C
                // Add register C to A
                length = 1;
                sb.append("ADD C");
                cycles = 4;
                add(register.C);
                PC++;
                break;
            case 0x82:
                // Cycles 4, flags S Z A P C
                // Add register D to A
                length = 1;
                sb.append("ADD D");
                cycles = 4;
                add(register.D);
                PC++;
                break;
            case 0x83:
                // Cycles 4, flags S Z A P C
                // Add register E to A
                length = 1;
                sb.append("ADD E");
                cycles = 4;
                add(register.E);
                PC++;
                break;
            case 0x84:
                // Cycles 4, flags S Z A P C
                // Add register H to A
                length = 1;
                sb.append("ADD H");
                cycles = 4;
                add(register.H);
                PC++;
                break;
            case 0x85:
                // Cycles 4, flags S Z A P C
                // Add register L to A
                length = 1;
                sb.append("ADD L");
                cycles = 4;
                add(register.L);
                PC++;
                break;
            case 0x86:
                // Cycles 7, flags S Z A P C
                // Add memory M to A
                length = 1;
                sb.append("ADD A, [HL]");
                cycles = 7;
                data = memory.readByte(register.HL());
                add(data);
                PC++;
                break;
            case 0x87:
                // Cycles 4, flags S Z A P C
                // Add A to A
                length = 1;
                sb.append("ADD A");
                cycles = 4;
                add(register.A);
                PC++;
                break;
            case 0x88:
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                length = 1;
                sb.append("ADC B");
                cycles = 4;
                adc(register.B);
                PC++;
                break;
            case 0x89:
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                length = 1;
                sb.append("ADC C");
                cycles = 4;
                adc(register.C);
                PC++;
                break;
            case 0x8a:
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                length = 1;
                sb.append("ADC D");
                cycles = 4;
                adc(register.D);
                PC++;
                break;
            case 0x8b:
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                length = 1;
                sb.append("ADC E");
                cycles = 4;
                adc(register.E);
                PC++;
                break;
            case 0x8c:
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                length = 1;
                sb.append("ADC H");
                cycles = 4;
                adc(register.H);
                PC++;
                break;
            case 0x8d:
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                length = 1;
                sb.append("ADC L");
                cycles = 4;
                adc(register.L);
                PC++;
                break;
            case 0x8e:
                // Cycles 7, flags S Z A P C
                // Add memory to A with carry
                length = 1;
                sb.append("ADC A, [HL]");
                cycles = 7;
                data = memory.readByte(register.HL());
                adc(data);
                PC++;
                break;
            case 0x8f:
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                length = 1;
                sb.append("ADC A");
                cycles = 4;
                adc(register.A);
                PC++;
                break;
            case 0x90:
                // Cycles 4, S Z A P
                // Subtract B from A
                length = 1;
                sb.append("SUB B");
                cycles = 4;
                sub(register.B);
                PC++;
                break;
            case 0x91:
                // Cycles 4, S Z A P
                // Subtract C from A
                length = 1;
                sb.append("SUB C");
                cycles = 4;
                sub(register.C);
                PC++;
                break;
            case 0x92:
                // Cycles 4, S Z A P
                // Subtract D from A
                length = 1;
                sb.append("SUB D");
                cycles = 4;
                sub(register.D);
                PC++;
                break;
            case 0x93:
                // Cycles 4, S Z A P
                // Subtract E from A
                length = 1;
                sb.append("SUB E");
                cycles = 4;
                sub(register.E);
                PC++;
                break;
            case 0x94:
                // Cycles 4, S Z A P
                // Subtract H from A
                length = 1;
                sb.append("SUB H");
                cycles = 4;
                sub(register.H);
                PC++;
                break;
            case 0x95:
                // Cycles 4, S Z A P
                // Subtract L from A
                length = 1;
                sb.append("SUB L");
                cycles = 4;
                sub(register.L);
                PC++;
                break;
            case 0x96:
                // Cycles 7, S Z A P
                // Subtract M from A
                length = 1;
                sb.append("SUB A, [HL]");
                cycles = 7;
                data = memory.readByte(register.HL());
                sub(data);
                PC++;
                break;
            case 0x97:
                // Cycles 4, S Z A P
                // Subtract A from A
                length = 1;
                sb.append("SUB A, A");
                cycles = 4;
                sub(register.A);
                PC++;
                break;
            case 0x98:
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                length = 1;
                sb.append("SBB A, B");
                cycles = 4;
                sbb(register.B);
                PC++;
                break;
            case 0x99:
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                length = 1;
                sb.append("SBB A, C");
                cycles = 4;
                sbb(register.C);
                PC++;
                break;
            case 0x9a:
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                length = 1;
                sb.append("SBB A, D");
                cycles = 4;
                sbb(register.D);
                PC++;
                break;
            case 0x9b:
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                length = 1;
                sb.append("SBB A, E");
                cycles = 4;
                sbb(register.E);
                PC++;
                break;
            case 0x9c:
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                length = 1;
                sb.append("SBB A, H");
                cycles = 4;
                sbb(register.H);
                PC++;
                break;
            case 0x9d:
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                length = 1;
                sb.append("SBB A, L");
                cycles = 4;
                sbb(register.L);
                PC++;
                break;
            case 0x9e:
                // Cycles 7, flags S Z A C P
                // Subtract register from A with borrow
                length = 1;
                sb.append("SBB A, [HL]");
                cycles = 7;
                data = memory.readByte(register.HL());
                sbb(data);
                PC++;
                break;
            case 0x9f:
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                length = 1;
                sb.append("SBB A, A");
                cycles = 4;
                sbb(register.A);
                PC++;
                break;
            case 0xa0:
                // Cycle 4, flags S Z A C P
                // AND register with A
                length = 1;
                sb.append("ANA A, B");
                cycles = 4;
                ana(register.B);
                PC++;
                break;
            case 0xa1:
                // Cycle 4, flags S Z A C P
                // AND register with A
                length = 1;
                sb.append("ANA A, C");
                cycles = 4;
                ana(register.C);
                PC++;
                break;
            case 0xa2:
                // Cycle 4, flags S Z A C P
                // AND register with A
                length = 1;
                sb.append("ANA A, D");
                cycles = 4;
                ana(register.D);
                PC++;
                break;
            case 0xa3:
                // Cycle 4, flags S Z A C P
                // AND register with A
                length = 1;
                sb.append("ANA A, E");
                cycles = 4;
                ana(register.E);
                PC++;
                break;
            case 0xa4:
                // Cycle 4, flags S Z A C P
                // AND register with A
                length = 1;
                sb.append("ANA A, H");
                cycles = 4;
                ana(register.H);
                PC++;
                break;
            case 0xa5:
                // Cycle 4, flags S Z A C P
                // AND register with A
                length = 1;
                sb.append("ANA A, L");
                cycles = 4;
                ana(register.L);
                PC++;
                break;
            case 0xa6:
                // Cycle 7, flags S Z A C P
                // AND memory with A
                length = 1;
                sb.append("ANA A, [HL]");
                cycles = 7;
                data = memory.readByte(register.HL());
                ana(data);
                PC++;
                break;
            case 0xa7:
                // Cycle 4, flags S Z A P C
                // AND register with A
                length = 1;
                sb.append("ANA A");
                cycles = 4;
                ana(register.A);
                PC++;
                break;
            case 0xa8:
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                length = 1;
                sb.append("XRA A, B");
                cycles = 4;
                xra(register.B);
                PC++;
                break;
            case 0xa9:
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                length = 1;
                sb.append("XRA A, C");
                cycles = 4;
                xra(register.C);
                PC++;
                break;
            case 0xaa:
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                length = 1;
                sb.append("XRA A, D");
                cycles = 4;
                xra(register.D);
                PC++;
                break;
            case 0xab:
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                length = 1;
                sb.append("XRA A, E");
                cycles = 4;
                xra(register.E);
                PC++;
                break;
            case 0xac:
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                length = 1;
                sb.append("XRA A, H");
                cycles = 4;
                xra(register.H);
                PC++;
                break;
            case 0xad:
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                length = 1;
                sb.append("XRA A, L");
                cycles = 4;
                xra(register.L);
                PC++;
                break;
            case 0xae:
                // Cycles 7, flags S Z A P C
                // ExclusiveOR memory with A
                length = 1;
                sb.append("XRA A, [HL]");
                cycles = 7;
                data = memory.readByte(register.HL());
                xra(data);
                PC++;
                break;
            case 0xaf:
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                length = 1;
                sb.append("XRA A, A");
                cycles = 4;
                xra(register.A);
                PC++;
                break;
            case 0xb0:
                // Cycles 4, flags S Z A P C
                // OR  register with A
                length = 1;
                sb.append("ORA A, B");
                cycles = 4;
                ora(register.B);
                PC++;
                break;
            case 0xb1:
                // Cycles 4, flags S Z A P C
                // OR  register with A
                length = 1;
                sb.append("ORA A, C");
                cycles = 4;
                ora(register.C);
                PC++;
                break;
            case 0xb2:
                // Cycles 4, flags S Z A P C
                // OR  register with A
                length = 1;
                sb.append("ORA A, D");
                cycles = 4;
                ora(register.D);
                PC++;
                break;
            case 0xb3:
                // Cycles 4, flags S Z A P C
                // OR  register with A
                length = 1;
                sb.append("ORA A, E");
                cycles = 4;
                ora(register.E);
                PC++;
                break;
            case 0xb4:
                // Cycles 4, flags S Z A P C
                // OR  register with A
                length = 1;
                sb.append("ORA A, H");
                cycles = 4;
                ora(register.H);
                PC++;
                break;
            case 0xb5:
                // Cycles 4, flags S Z A P C
                // OR  register with A
                length = 1;
                sb.append("ORA A, L");
                cycles = 4;
                ora(register.L);
                PC++;
                break;
            case 0xb6:
                // Cycles 7, flags S Z A P C
                // OR  memory with A
                length = 1;
                sb.append("ORA A, [HL]");
                cycles = 7;
                data = memory.readByte(register.HL());
                ora(data);
                PC++;
                break;
            case 0xb7:
                // Cycles 4, flags S Z A P C
                // OR  register with A
                length = 1;
                sb.append("ORA A, A");
                cycles = 4;
                ora(register.A);
                PC++;
                break;
            case 0xb8:
                // Cycles 4, S C Z A P C
                // Compare register with A
                length = 1;
                sb.append("CMP A, B");
                cycles = 4;
                cmp(register.B);
                PC++;
                break;
            case 0xb9:
                // Cycles 4, S C Z A P C
                // Compare register with A
                length = 1;
                sb.append("CMP A, C");
                cycles = 4;
                cmp(register.C);
                PC++;
                break;
            case 0xba:
                // Cycles 4, S C Z A P C
                // Compare register with A
                length = 1;
                sb.append("CMP A, D");
                cycles = 4;
                cmp(register.D);
                PC++;
                break;
            case 0xbb:
                // Cycles 4, S C Z A P C
                // Compare register with A
                length = 1;
                sb.append("CMP A, E");
                cycles = 4;
                cmp(register.E);
                PC++;
                break;
            case 0xbc:
                // Cycles 4, S C Z A P C
                // Compare register with A
                length = 1;
                sb.append("CMP A, H");
                cycles = 4;
                cmp(register.H);
                PC++;
                break;
            case 0xbd:
                // Cycles 4, S C Z A P C
                // Compare register with A
                length = 1;
                sb.append("CMP A, L");
                cycles = 4;
                cmp(register.L);
                PC++;
                break;
            case 0xbe:
                // Cycles 7, S C Z A P C
                // Compare memory with A
                length = 1;
                sb.append("CMP A, [HL]");
                cycles = 7;
                data = memory.readByte(register.HL());
                cmp(data);
                PC++;
                break;
            case 0xbf:
                // Cycles 4, S C Z A P C
                // Compare register with A
                length = 1;
                sb.append("CMP A, A");
                cycles = 4;
                cmp(register.A);
                PC++;
                break;
            case 0xc0:
                // Cycles 11/5, no flags
                // Return if not zero
                // Conditional return from subroutine
                length = 1;
                sb.append("RNZ");
                if(!flag.zero) {
                    PC = memory.readWord(SP);
                    SP += 2;
                    cycles = 11;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xc1:
                // Cycles 10, no flags
                // POP stack into BC
                length = 1;
                sb.append("POP BC");
                cycles = 10;
                register.C = memory.readByte(SP++);
                register.B = memory.readByte(SP++);
                PC++;
                break;
            case 0xc2:
                // Cycles 10, no flags
                // Jump if NOT zero
                length = 3;
                sb.append("JNZ ");
                cycles = 10;
                address = memory.readWord(PC + 1);
                sb.append(nString.hexToString16(address));
                if(!flag.zero) {
                    PC = address;
                } else {
                    PC += 3;
                }
                break;
            case 0xc3:
                // Cycles 10, no flags
                // Jump to address
                length = 3;
                sb.append("JMP ");
                cycles = 10;
                PC = memory.readWord(++PC);
                sb.append(nString.hexToString16(PC));
                break;
            case 0xc4:
                // Cycles 17/11, no flags
                // Call on NOT zero
                length = 3;
                sb.append("CNZ ");
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(!flag.zero) {
                    SP -= 2;
                    // store return instruction on stack
                    memory.writeWord(SP, PC + 2);
                    PC = address;
                    cycles = 17;
                } else {
                    PC += 2;
                    cycles = 11;
                }
                break;
            case 0xc5:
                // Cycle 11, no flags
                // Push register pair BC onto stack
                length = 1;
                sb.append("PUSH BC");
                cycles = 11;
                // it's backwards because of -- vs ++
                memory.writeByte(--SP, register.B);
                memory.writeByte(--SP, register.C);
                PC++;
                break;
            case 0xc6:
                // Cycles 7, flags S Z A P C
                // Add immediate to A
                length = 2;
                sb.append("ADI A, ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                add(data);
                PC++;
                break;
            case 0xc7:
                // Cycles 11, no flags
                // Push PC onto stack
                length = 1;
                sb.append("RST 0");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, ++PC);
                PC = 0x00;
                break;
            case 0xc8:
                // Cycles 11/5, no flags
                // Return if zero
                length = 1;
                sb.append("RZ");
                if(flag.zero) {
                    PC = memory.readWord(SP);
                    SP += 2;
                    cycles = 11;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xc9:
                // Cycles 10, no flags
                // Return
                length = 1;
                sb.append("RET");
                cycles = 10;
                PC = memory.readWord(SP);
                SP += 2;
                break;
            case 0xca:
                // Cycles 10, no flags
                // Jump if zero
                length = 3;
                sb.append("JZ ");
                cycles = 10;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(flag.zero) {
                    PC = address;
                } else {
                    PC += 2;
                }
                break;
            case 0xcb:
                // Cycles 10, no flags
                // Jump
                length = 3;
                sb.append("*JMP ");
                cycles = 10;
                PC = memory.readWord(++PC);
                sb.append(nString.hexToString16(PC));
                // Don't need PC++, JMP changed PC
                break;
            case 0xcc:
                // Cycles 17/11
                // Call if zero
                length = 3;
                sb.append("CZ ");
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(flag.zero) {
                    cycles = 17;
                    SP -= 2;
                    // write return address to stack
                    memory.writeWord(SP, PC + 2);
                    // Get the pointer and put it in PC
                    PC = address;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xcd:
            case 0xfd:
            case 0xdd:
            case 0xed:
                // Cycles 17, no flags
                // Call subroutine
                length = 3;
                sb.append("CALL ");
                cycles = 17;
                // Write return address to stack
                SP -= 2;
                memory.writeWord(SP, PC + 3);
                PC = memory.readWord(++PC);
                sb.append(nString.hexToString16(PC));
                break;
            case 0xce:
                // Cycles 7, flags S Z A P C
                // Add with carry immediate
                length = 2;
                sb.append("ACI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                adc(data);
                PC++;
                break;
            case 0xcf:
                // Cycles 11, no flags
                // Call 0x0008
                length = 1;
                sb.append("RST 1");
                cycles = 11;
                // Save PC on stack
                SP -= 2;
                memory.writeWord(SP, ++PC);
                PC = 0x0008;
                break;
            case 0xd0:
                // Cycles 11/5, no flags
                // Return no carry
                length = 1;
                sb.append("RNC");
                if(!flag.carry) {
                    PC = memory.readWord(SP);
                    SP += 2;
                    cycles = 11;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xd1:
                // Cycles 10, no flags
                // POP D off stack
                length = 1;
                sb.append("POP DE");
                cycles = 10;
                register.DE(memory.readWord(SP));
                SP += 2;
                PC++;
                break;
            case 0xd2:
                // Cycles 10, no flags
                // Jump no carry
                length = 3;
                sb.append("JNC ");
                cycles = 10;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(!flag.carry) {
                    PC = address;
                } else {
                    PC += 2;
                }
                break;
            case 0xd3:
                // Cycles 10, no flags
                // Data from A register placed in Port.
                length = 2;
                sb.append("OUT ");
                cycles = 10;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString16(data));
                io.out(data, register.A);
                PC++;
                break;
            case 0xd4:
                // Cycles 17/11, no flags
                // Call no carry
                length = 3;
                sb.append("CNC ");
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(!flag.carry) {
                    cycles = 17;
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = address;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xd5:
                // Cycles 11, no flags
                // PUSH D
                length = 1;
                sb.append("PUSH D");
                cycles = 11;
                memory.writeByte(--SP, register.D);
                memory.writeByte(--SP, register.E);
                PC++;
                break;
            case 0xd6:
                // Cycles 7, flags S Z A P C
                // Subtracts 8 bit data from contents of A register.
                length = 2;
                sb.append("SUI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                sub(data);
                PC++;
                break;
            case 0xd7:
                // Cycles 11, no flags
                // PC = 0x0010
                length = 1;
                sb.append("RST 2");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, ++PC);
                PC = 0x0010;
                break;
            case 0xd8:
                // Cycles 11/5, no flags
                // Return carry
                length = 1;
                sb.append("RC ");
                address = memory.readWord(SP);
                sb.append(nString.hexToString16(address));
                if(flag.carry) {
                    cycles = 11;
                    // Get Return address
                    PC = address;
                    SP += 2;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xd9:
                // Cycles 10, no flags
                // Return
                length = 1;
                sb.append("*RET");
                cycles = 10;
                PC = memory.readWord(SP);
                SP += 2;
                break;
            case 0xda:
                // Cycles 10
                // Jump carry
                length = 3;
                sb.append("JC ");
                cycles = 10;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(flag.carry) {
                    PC = address;
                } else {
                    PC += 2;
                }
                break;
            case 0xdb:
                // Cycles 10, no flags
                // Data from Port placed in A register.
                length = 2;
                sb.append("IN ");
                cycles = 10;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                register.A = io.in(data);
                PC++;
                break;
            case 0xdc:
                // Cycles 17/11, no flags
                // Call carry
                length = 3;
                sb.append("CC ");
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(flag.carry) {
                    cycles = 17;
                    SP -= 2;
                    // Save return address on stack
                    memory.writeWord(SP, PC + 2);
                    PC = address;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xde: // Show this one to Professor Calvin
                // Cycles 7, flags S Z A P C
                // Subtract with borrow immediately
                length = 2;
                sb.append("SBI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                sbb(data);
                PC++;
                break;
            case 0xdf:
                // Cycles 11, no flags
                // PC = 0x0018
                length = 1;
                sb.append("RST 3");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, ++PC);
                PC = 0x0018;
                break;
            case 0xe0:
                // Cycles 11/5, no flags
                // Return if parity odd
                length = 1;
                sb.append("RPO");
                if(!flag.parity) {
                    cycles = 11;
                    PC = memory.readWord(SP);
                    SP += 2;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xe1:
                // Cycles 10, no flags
                // POP H
                length = 1;
                sb.append("POP HL");
                cycles = 10;
                register.HL(memory.readWord(SP));
                SP += 2;
                PC++;
                break;
            case 0xe2:
                // Cycles 10, no flags
                // Jump if parity odd
                length = 3;
                sb.append("JPO ");
                cycles = 10;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(!flag.parity) {
                    PC = address;
                } else {
                    PC += 2;
                }
                break;
            case 0xe3:
                // Cycles 18, no flags
                // Exchanges HL with top of stack
                length = 1;
                sb.append("XTHL");
                cycles = 18;
                // Swap Stack and L
                data = register.L;
                register.L = memory.readByte(SP);
                memory.writeByte(SP, data);
                // Swap Stack and H
                data = register.H;
                register.H = memory.readByte(SP + 1);
                memory.writeByte(SP + 1, data);
                PC++;
                break;
            case 0xe4:
                // Cycles 17/11, no flags
                // Carry if parity odd
                length = 3;
                sb.append("CPO ");
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(!flag.parity) {
                    cycles = 17;
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = address;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xe5:
                // Cycles 11, no flags
                // PUSH H
                length = 1;
                sb.append("PUSH HL");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, register.HL());
                PC++;
                break;
            case 0xe6:
                // Cycles 7, S Z A P C
                //  Logically ORs 8 bit data with contents of A register.
                length = 2;
                sb.append("ANI A, ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                ana(data);
                PC++;
                break;
            case 0xe7:
                // Cycles 11, no flags
                // PC = 0x0020
                length = 1;
                sb.append("RST 4");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, ++PC);
                PC = 0x0020;
                break;
            case 0xe8:
                // Cycles 11/5, no flags
                // Return if parity even
                length = 1;
                sb.append("RPE");
                if(flag.parity) {
                    cycles = 11;
                    PC = memory.readWord(SP);
                    SP += 2;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xe9:
                // Cycles 5, no flags
                // Puts contents of HL into PC (program counter) JMP HL.
                length = 1;
                sb.append("PCHL");
                cycles = 5;
                PC = register.HL();
                // Don't need to inc PC!
                break;
            case 0xea:
                // Cycles 10, no flags
                // Jump if parity even
                length = 3;
                sb.append("JPE ");
                cycles = 10;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(flag.parity) {
                    PC = address;
                } else {
                    PC += 2;
                }
                break;
            case 0xeb:
                // Cycles 5, no flags
                // Exchanges HL and DE
                length = 1;
                sb.append("XCHG HL, DE");
                cycles = 5;
                data = register.HL();
                register.HL(register.DE());
                register.DE(data);
                PC++;
                break;
            case 0xec:
                // Cycles 17/11, no flags
                // Call if parity even
                length = 3;
                sb.append("CPE ");
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(flag.parity) {
                    cycles = 17;
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = address;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xee:
                // Cycles 7, S Z A P C
                // Exclusive-OR 8 bit data with A register.
                length = 2;
                sb.append("XRI A, ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                xra(data);
                PC++;
                break;
            case 0xef:
                // Cycles 11, no flags
                // PC = 0x0028
                length = 1;
                sb.append("RST 5");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, ++PC);
                PC = 0x0028;
                PC++;
                break;
            case 0xf0:
                // Cycles 11/5, no flags
                // Return if not sign
                length = 1;
                sb.append("RP");
                if(!flag.sign) {
                    cycles = 11;
                    PC = memory.readWord(SP);
                    SP += 2;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xf1:
                // Cycles 10, S Z A P C
                // POP PSW + Register A
                // first byte on stack is flags,
                // second byte on stack is register A
                length = 1;
                sb.append("POP PSW");
                cycles = 10;
                data = memory.readByte(SP++);
                flag.setPSW(data);
                register.A = memory.readByte(SP++);
                PC++;
                break;
            case 0xf2:
                // Cycles 10, no flags
                // Jump if not sign
                length = 3;
                sb.append("JP ");
                cycles = 10;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(!flag.sign) {
                    PC = address;
                } else {
                    PC += 2;
                }
                break;
            case 0xf3:
                // Cycles 4
                // Disable Interrupts
                length = 1;
                sb.append("DI");
                cycles = 4;
                interrupts = false;
                PC++;
                break;
            case 0xf4:
                // Cycles 17/11, no flags
                // Call if not sign
                length = 3;
                sb.append("CP ");
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(!flag.sign) {
                    cycles = 17;
                    // Write return address to stack
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = address;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xf5:
                // Cycles 11, no flags
                // PUSH PSW + A
                length = 1;
                sb.append("PUSH PSW");
                cycles = 11;
                memory.writeByte(--SP, register.A);
                memory.writeByte(--SP, flag.getPSW());
                PC++;
                break;
            case 0xf6:
                // Cycles 7, flags S Z A P C
                // Logically ORs 8 bit data with contents of A register.
                length = 2;
                sb.append("ORI A, ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                ora(data);
                PC++;
                break;
            case 0xf7:
                // Cycles 11, no flags
                // PC = 0x0030
                length = 1;
                sb.append("RST 6");
                cycles = 11;
                // store pc
                SP -= 2;
                memory.writeWord(SP, ++PC);
                PC = 0x0030;
                break;
            case 0xf8:
                // Cycles 11/5, no flags
                // Return if sign
                length = 1;
                sb.append("RM");
                if(flag.sign) {
                    cycles = 11;
                    PC = memory.readWord(SP);
                    SP += 2;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xf9:
                // Cycles 5, no flags
                // Puts contents of HL into SP
                length = 1;
                sb.append("SPHL");
                cycles = 5;
                SP = register.HL();
                PC++;
                break;
            case 0xfa:
                // Cycles 10, no flags
                // Jump if sign
                length = 3;
                sb.append("JM ");
                cycles = 10;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(flag.sign) {
                    PC = address;
                } else {
                    PC += 2;
                }
                break;
            case 0xfb:
                // Cycles 4, no flags
                // Enable interrupts
                length = 1;
                sb.append("EI");
                cycles = 4;
                interrupts = true;
                PC++;
                break;
            case 0xfc:
                // Cycles 17/11, no flags
                // Call if sign
                length = 3;
                sb.append("CM ");
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                if(flag.sign) {
                    cycles = 17;
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = address;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xfe:
                // Cycles 7, flags S Z A P C
                // Compares 8 bit data with contents of A register.
                length = 2;
                sb.append("CPI A, ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                cmp(data);
                PC++;
                break;
            case 0xff:
                // Cycles 11, no flags
                // PC = 0x0038
                length = 1;
                sb.append("RST 7");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, ++PC);
                PC = 0x0038;
                break;
        }

        currentInstruction = sb.toString();
        previousState = new CPUState(this);
        cpuBusy.release();

        return cycles;
    }
}
