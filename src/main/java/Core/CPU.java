package Core;

import Utilities.Utils.nString;

public class CPU {
    private class Registers {
        public int B; // 0
        public int C; // 1
        public int D; // 2
        public int E; // 3
        public int H; // 4
        public int L; // 5
        //  M = memory // 6
        public int A; // 7

        public int BC() {
            return (B << 8) | C;
        }

        public void BC(int value) {
            B = (value >>> 8) & 0xff;
            C = value & 0xff;
        }

        public int DE() {
            return (D << 8) | D;
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

    private class Flags {
        public boolean carry;
        public boolean zero;
        public boolean sign;
        public boolean parity;
        public boolean auxCarry;

        public int PSW() {
            int data = 0;
            data = carry ? 0x1 : 0;
            data |= 0x2;    // always set
            data |= auxCarry ? 0x10 : 0;
            data |= zero ? 0x40 : 0;
            data |= sign ? 0x80 : 0;
            return data;
        }
    }


    private Memory memory;
    private Registers register = new Registers();
    private Flags flag = new Flags();
    private String currentInstruction;
    private int PC = 0;
    private int SP = 0;
    private boolean interrupts = true;
    private boolean halt = false;

    public CPU(byte[] mem) {
        memory = new Memory(mem);
        memory.printOutMemory();
    }

    public int getPC() {
        return PC;
    }

    public String getCurrentInstruction() {
        return currentInstruction;
    }

    private void inxB() {
        register.C++;
        if(register.C > 0b1111) { // we aren't actually 8 bit, so we can detect value over flow easily
            register.C = 0;
            register.B++;
            if(register.B > 0b1111) {
                register.B = 0;
            }
        }
    }

    private int inr(int registerValue) {
        int x = registerValue & 0xf0; // save high nibble

        registerValue++;
        registerValue &= 0xff; // <255
        flag.zero = (registerValue == 0);
        flag.sign = (registerValue & 0x80) != 0; // 0b1000 0000
        flag.auxCarry = x < (registerValue & 0xf0); // did bit 4 change?
        flag.parity = checkParity(registerValue, 8);

        return registerValue;
    }

    private int dcr(int registerValue) {
        int x = registerValue & 0xf0; // save high nibble

        registerValue--;
        registerValue &= 0xff; // <255
        flag.zero = (registerValue == 0);
        flag.sign = (registerValue & 0x80) != 0; // 0b1000 0000
        flag.auxCarry = x == (registerValue & 0xf0);
        flag.parity = checkParity(registerValue, 8);

        return registerValue;
    }

    private void add(int registerValue) {
        // S Z A P C
        register.A += registerValue;
        sign8(register.A);
        zero(register.A);
        parity8(register.A);
        carry16(register.A);
        register.A &= 0xff; // <256
        auxCarry8(registerValue, register.A);

    }

    private void adc(int registerValue) {
        // S Z A C P
        int x = register.A + registerValue + (flag.carry ? 1 : 0);

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
        int x = register.A - registerValue;

        flag.carry = (((x & 0xff00) >= register.A) && (registerValue > 0));
        flag.auxCarry = (registerValue & 0xf) <= (register.A & 0xf);
        sign8(x);
        zero(x);
        parity8(x);
        register.A = (x & 0xff); // <256
    }

    private void sbb(int registerValue) {
        int x = register.A - registerValue - (flag.carry ? 1 : 0);

        if(flag.carry) {
            flag.auxCarry = (registerValue & 0xf) < (register.A & 0xf);
        } else {
            flag.auxCarry = (registerValue & 0xf) <= (register.A & 0xf);
        }
        flag.carry = ((x & 0xff) >= register.A) && ((registerValue > 0) | flag.carry);

        x &= 0xff; // <256
        sign8(x);
        zero(x);
        parity8(x);
        register.A = x;
    }

    private void ana(int registerValue) {
        // S Z A P C
        flag.auxCarry = ((register.A | registerValue) & 0x08) > 0;
        register.A &= registerValue;
        sign8(register.A);
        zero(register.A);
        parity8(register.A);
        flag.carry = false;
    }

    private void xra(int registerValue) {
        // S Z A P C
        register.A ^= registerValue;
        sign8(register.A);
        zero(register.A);
        flag.auxCarry = false;
        parity8(register.A);
        flag.carry = false;
    }

    private void ora(int registerValue) {
        // S Z A P C
        register.A |= registerValue;
        sign8(register.A);
        zero(register.A);
        flag.auxCarry = false;
        parity8(register.A);
        flag.carry = false;
    }

    private void cmp(int registerValue) {
        // S Z A P C
        int x = register.A - registerValue;
        flag.carry = ((registerValue & 0xff) >= register.A) && (registerValue > 0);
        flag.auxCarry = (registerValue & 0xf) <= (register.A & 0xf);
        sign8(x);
        zero(x);
        parity8(x);
    }

    private void auxCarry8(int value1, int value2) {
        // Add lower nibble together, does it cross byte 3 boundary?
        flag.auxCarry = ((value1 & 0xf) + (value2 & 0xf)) > 0xf;
    }

    private void sign8(int value) {
        flag.sign = (value & 0x80) > 0;
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

    public int stepExecute() {
        StringBuilder sb = new StringBuilder();
        int instruction = memory.readByte(PC);
        int cycles = 0;
        int carry = 0;
        int address = 0;
        int data = 0;

        switch(instruction) {
            case 0x00:
                // Length 1 byte
                // Cycles 4, no flags
                // Do nothing
                sb.append("NOP");
                cycles = 4;
                PC++;
                break;
            case 0x01:
                // Length 3 byte
                // Cycles 10, no flags
                // Load next two bytes into CD
                sb.append("LXI B, ");
                cycles = 10;
                register.C = memory.readByte(++PC);
                register.B = memory.readByte(++PC);
                sb.append(nString.hexToString16(register.BC()));
                PC++;
                break;
            case 0x02:
                // Length 1 byte
                // Cycles 7, no flags
                // Copy register A value into address at BC
                sb.append("STAX B");
                cycles = 7;
                memory.writeByte(register.BC(), register.A);
                PC++;
                break;
            case 0x03:
                // Length 1
                // Cycles 5, no flags
                // BC++
                sb.append("INX B");
                cycles = 5;
                inxB();
                PC++;
                break;
            case 0x04:
                // Length 1
                // Cycles 5, flags S Z A P
                // B++
                sb.append("INR B");
                cycles = 5
            case 0x06:
                // Length 2;
                //                register.B = inr(register.B);
                //                PC++;
                //                break;
                //            case 0x05:
                //                // Length 1
                //                // Cycles 5, flags S Z A P
                //                // B--
                //                sb.append("DCR B");
                //                cycles = 5;
                //                register.B = dcr(register.B);
                //                PC++;
                //                break;
                // Cycles 7, no flags
                // Move immediate to register
                sb.append("MVI B, ");
                cycles = 7;
                register.B = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.B));
                PC++;
                break;
            case 0x07:
                // Length 1
                // Cycles 4, flags C
                // Rotate A left
                sb.append("RLC");
                cycles = 4;
                sign8(register.A);
                register.A = (register.A << 1) | (register.A >> 7); // carry bit 7 back to bit 0
                PC++;
                break;
            case 0x08:
            case 0x10:
            case 0x18:
            case 0x20:
            case 0x28:
            case 0x30:
            case 0x38:
                // Length 1
                // Cycles 4, no flags
                // NOP - Do Nothing
                sb.append("*NOP");
                cycles = 4;
                PC++;
                break;
            case 0x09:
                // Length 1
                // Cycles 10, flags C
                // Add register pair to HL (16 bit add)
                sb.append("DAD B");
                carry16(register.BC() + register.HL());
                //flag.carry = ((register.BC() + register.HL()) & 0xFFFF0000) > 0; // did the calculation go over 16 bits?
                register.HL(register.BC() + register.HL());
                cycles = 10;
                PC++;
                break;
            case 0x0a:
                // Length 1
                // Cycle 7, no flags
                // Load indirect through BC or DE
                sb.append("LDAX B");
                cycles = 7;
                register.A = memory.readByte(register.BC());
                PC++;
                break;
            case 0x0b:
                // Length 1
                // Cycles 5, no flags
                // Decrement register pair
                sb.append("DCX B");
                cycles = 5;
                register.BC(register.BC() - 1);
                PC++;
                break;
            case 0x0c:
                // Length 1
                // Cycles 5, flags S Z A P
                // Increment register
                sb.append("INR C");
                cycles = 5;
                register.C = inr(register.C);
                PC++;
                break;
            case 0x0d:
                // Length 1
                // Cycles 5, flags S Z A P
                // Decrement register
                sb.append("DCR C");
                register.C = dcr(register.C);
                PC++;
                break;
            case 0x0e:
                // Length 2
                // Cycles 7, no flags
                // Move immediate to register
                sb.append("MVI C, ");
                register.C = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.C));
                PC++;
                break;
            case 0x0f:
                // Length 1
                // Cycles 4, flags C
                // Rotate A right
                sb.append("RRC");
                cycles = 4;
                flag.carry = (register.A & 1) == 1;
                register.A = (register.A >>> 1);
                register.A = register.A | (flag.carry ? 1 : 0);
                PC++;
                break;
            case 0x11:
                // Length 3
                // Cycles 10, no flags
                // Load register pair immediate
                sb.append("LXI D, ");
                cycles = 10;
                register.E = memory.readByte(++PC);
                register.D = memory.readByte(++PC);
                sb.append(nString.hexToString16(register.DE()));
                PC++;
                break;
            case 0x12:
                // Length 1
                // Cycles 7, no flags
                // Store indirect through BC or DE
                sb.append("STAX D");
                cycles = 7;
                memory.writeByte(register.DE(), register.A);
                PC++;
                break;
            case 0x13:
                // Length 1
                // Cycles 5, no flags
                // Increment register pair
                sb.append("INX D");
                cycles = 5;
                register.DE(register.DE() - 1);
                PC++;
                break;
            case 0x14:
                // Length 1
                // Cycles 5, flags S Z A P
                // Increment register
                sb.append("INR D");
                register.D = inr(register.D);
                PC++;
                break;
            case 0x15:
                // Length 1
                // Cycles 5, flags S Z A P
                // Decrement register
                sb.append("DCR D");
                register.D = dcr(register.D);
                PC++;
                break;
            case 0x16:
                // Length 2
                // Cycles 7, no flags
                // Move immediate to register
                sb.append("MVI D, ");
                cycles = 7;
                register.D = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.D));
                PC++;
                break;
            case 0x17:
                // Length 1
                // Cycles 4, flags C
                // Rotate A left through carry
                sb.append("RAL");
                cycles = 4;
                // Save original value
                carry = flag.carry ? 1 : 0;
                carry8(register.A);
                //flag.carry = (register.A & 0x80) > 0;
                register.A = (register.A << 1) | carry;
                register.A &= 0xff; // <255
                PC++;
                break;
            case 0x19:
                // Length 1
                // Cycles 10, flags C
                // Add register pair to HL (16 bit add)
                sb.append("DAD D");
                cycles = 10;
                carry16(register.DE() + register.HL());
                register.HL(register.DE() + register.HL());
                PC++;
                break;
            case 0x1a:
                // Length 1
                // Cycles 7, no flags
                // Load indirect through BC or DE
                sb.append("LDAX D");
                cycles = 7;
                register.A = memory.readByte(register.DE());
                PC++;
                break;
            case 0x1b:
                // Length 1
                // Cycles 5, no flags
                // Decrement register pair
                sb.append("DCX D");
                cycles = 5;
                register.DE(register.DE() - 1);
                PC++;
                break;
            case 0x1c:
                // Length 1
                // Cycles 5, flags S Z A P
                // Increment register
                sb.append("INR E");
                cycles = 5;
                register.E = inr(register.E);
                PC++;
                break;
            case 0x1d:
                // Length 1
                // Cycles 5, S Z A P
                // Decrement register
                sb.append("DCR E");
                cycles = 5;
                register.E = dcr(register.E);
                PC++;
                break;
            case 0x1e:
                // Length 2
                // Cycles 7, no flags
                // Move immediate to register
                sb.append("MVI E, ");
                cycles = 7;
                register.E = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.E));
                PC++;
                break;
            case 0x1f:
                // Length 1
                // Cycles 4, flags C
                // Rotate A right through carry
                sb.append("RAR");
                cycles = 4;
                carry = flag.carry ? 0x80 : 0;
                flag.carry = (register.A & 1) > 0;
                register.A = (register.A >>> 1) | carry;
                PC++;
                break;
            case 0x21:
                // Length 3
                // Cycles 10, no flags
                // Load register pair immediate
                sb.append("LXI H, ");
                cycles = 10;
                register.L = memory.readByte(++PC);
                register.H = memory.readByte(++PC);
                sb.append(nString.hexToString16(register.HL()));
                PC++;
                break;
            case 0x22:
                // Length 3
                // Cycles 16
                // Store H:L to memory
                sb.append("SHLD ");
                cycles = 16;
                address = memory.readWord(++PC);
                PC++;
                sb.append(nString.hexToString16(address));
                memory.writeWord(address, register.L, register.H);
                PC++;
                break;
            case 0x23:
                // Length 1
                // Cycles 5, no flags
                // Increment register pair
                sb.append("INX H");
                register.HL(register.HL() + 1);
                PC++;
                break;
            case 0x24:
                // Length 1
                // Cycles 5, flags S Z A P
                // Increment register
                sb.append("INR H");
                register.H = inr(register.H);
                PC++;
                break;
            case 0x25:
                // Length 1
                // Cycles 5, flags S Z A P
                // Decrement register
                sb.append("DCR H");
                register.H = dcr(register.H);
                PC++;
                break;
            case 0x26:
                // Length 2
                // Cycles 7, no flags
                // Move immediate to register
                sb.append("MVI H, ");
                data = memory.readByte(++PC);
                register.H = data;
                sb.append(nString.hexToString8(data));
                PC++;
                break;
            case 0x27:
                // Length 1
                // Cycles 4, S Z A C P
                // Decimal Adjust accumulator
                sb.append("DAA - Not implemented");
                PC++;
                break;
            case 0x29:
                // Length 1
                // Cycles 10, flags C
                // Add register pair to HL (16 bit add)
                sb.append("DAD H");
                cycles = 10;
                data = register.HL() + register.HL();
                carry16(data);
                register.HL(data);
                PC++;
                break;
            case 0x2a:
                // Length 3
                // Cycles 16, no flags
                // Load H:L from memory
                sb.append("LHLD ");
                cycles = 16;
                address = memory.readWord(++PC);
                sb.append("[").append(nString.hexToString16(address)).append("]");
                register.L = memory.readByte(address);
                register.H = memory.readByte(address + 1);
                PC += 2;
                break;
            case 0x2b:
                // Length 1
                // Cycles 5, no flags
                // Decrement register pair
                sb.append("DCX H");
                cycles = 5;
                register.HL(register.HL() - 1);
                PC++;
                break;
            case 0x2c:
                // Length 1
                // Cycles 5, flags S Z A P
                // Increment register
                sb.append("INR L");
                register.L = inr(register.L);
                PC++;
                break;
            case 0x2d:
                // Length 1
                // Cycles 5, flags S Z A P
                // Decrement register
                sb.append("DCR L");
                cycles = 5;
                register.L = dcr(register.L);
                PC++;
                break;
            case 0x2e:
                // Length 2
                // Cycles 7
                // Move immediate to register
                sb.append("MVI L, ");
                cycles = 7;
                register.L = memory.readByte(++PC);
                sb.append(nString.hexToString8(register.L));
                PC++;
                break;
            case 0x2f:
                // Length 1
                // Cycles 4, no flags
                // Compliment A
                sb.append("CMA");
                cycles = 4;
                register.A = ~register.A & 0xff; // <256;
                PC++;
                break;
            case 0x31:
                // Length 3
                // Cycles 10, no flags
                // Load register pair immediate
                sb.append("LXI SP, ");
                cycles = 10;
                SP = memory.readWord(++PC);
                sb.append(nString.hexToString16(SP));
                PC += 2;
                break;
            case 0x32:
                // Length 3
                // Cycles 13, no flags
                // Store A to memory
                sb.append("STA ");
                cycles = 13;
                address = memory.readWord(++PC);
                sb.append(nString.hexToString16(address));
                memory.writeByte(address, register.A);
                PC += 2;
                break;
            case 0x33:
                // Length 1
                // Cycles 5, no flags
                // Increment register
                sb.append("INX SP");
                ++SP;
                SP &= 0xffff; // <65536 - cut off upper bits
                PC++;
                break;
            case 0x34:
                // Length 1
                // Cycles 10, flags S Z A P
                // Increment memory INR M
                sb.append("INR ");
                sb.append("[").append(nString.hexToString16(register.HL())).append("]");
                data = inr(register.HL());
                memory.writeByte(register.HL(), data);
                PC++;
                break;
            case 0x35:
                // Length 1
                // Cycles 10, flags S Z A P
                // Decrement memory DCR M
                sb.append("DCR ");
                cycles = 10;
                address = register.HL();
                sb.append("[").append(nString.hexToString16(address)).append("]");
                data = memory.readByte(address);
                data = dcr(data);
                memory.writeByte(address, data);
                PC++;
                break;
            case 0x36:
                // Length 2
                // Cycles 10, no flags
                // Move immediate to memory
                sb.append("MVI ");
                address = register.HL();
                sb.append(nString.hexToString16(address)).append(", ");
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                memory.writeByte(address, data);
                PC += 2;
                break;
            case 0x37:
                // Length 1
                // Cycles 4, flags C
                // Set Carry flag
                sb.append("STC");
                cycles = 4;
                flag.carry = true;
                PC++;
                break;
            case 0x39:
                // Length 1
                // Cycles 10, flags C
                // Add register pair to HL (16 bit add)
                sb.append("DAD SP");
                data = SP + register.HL();
                carry16(data);
                //flag.carry = ((SP + register.HL()) & 0xFFFF0000) > 0; // did the calculation go over 16 bits?
                register.HL(data);
                cycles = 10;
                PC++;
                break;
            case 0x3a:
                // Length 3
                // Cycles 13, no flags
                // Load A from memory
                sb.append("LDA ");
                cycles = 13;
                address = memory.readWord(++PC);
                sb.append("[").append(address).append("]");
                register.A = memory.readByte(address);
                PC += 2;
                break;
            case 0x3b:
                // Length 1
                // Cycles 5, no flags
                // Decrement register pair
                sb.append("DCX SP");
                cycles = 5;
                --SP;
                SP &= 0xffff; // <65536
                PC++;
                break;
            case 0x3c:
                // Length 1
                // Cycles 5, flags S Z A P
                // Increment register A
                sb.append("INR A");
                cycles = 5;
                register.A = inr(register.A);
                PC++;
                break;
            case 0x3d:
                // Length 1
                // Cycles 5, flags S Z A P
                // Decrement register
                sb.append("DCR A");
                cycles = 5;
                register.A = dcr(register.A);
                PC++;
                break;
            case 0x3e:
                // Length 2
                // Cycles 7, no flags
                // Move immediate to register
                sb.append("MVI A, ");
                register.A = memory.readByte(++PC);
                PC++;
                break;
            case 0x3f:
                // Length 1
                // Cycles 4, flags C
                // Compliment Carry flag
                sb.append("CMC");
                cycles = 4;
                flag.carry = !flag.carry;
                PC++;
                break;
            case 0x40:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV B, B");
                //register.B = register.B;
                cycles = 5;
                PC++;
                break;
            case 0x41:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV B, C");
                cycles = 5;
                register.B = register.C;
                PC++;
                break;
            case 0x42:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV B, D");
                cycles = 5;
                register.B = register.D;
                PC++;
                break;
            case 0x43:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV B, E");
                cycles = 5;
                register.B = register.E;
                PC++;
                break;
            case 0x44:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV B, H");
                register.B = register.H;
                PC++;
                break;
            case 0x45:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV B, L");
                cycles = 5;
                register.B = register.L;
                PC++;
                break;
            case 0x46:
                // Length 1
                // Cycles 7, no flags
                // Move memory to register
                sb.append("MOV B, M");
                cycles = 7;
                register.B = memory.readByte(register.HL());
                PC++;
                break;
            case 0x47:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV B, A");
                cycles = 5;
                register.B = register.A;
                PC++;
                break;
            case 0x48:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV C, B");
                cycles = 5;
                register.C = register.B;
                PC++;
                break;
            case 0x49:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV C, C");
                cycles = 5;
                //regsiter.C = register.C;
                PC++;
                break;
            case 0x4a:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV C, D");
                cycles = 5;
                register.C = register.D;
                PC++;
                break;
            case 0x4b:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV C, E");
                cycles = 5;
                register.C = register.E;
                PC++;
                break;
            case 0x4c:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV C, H");
                cycles = 5;
                register.C = register.H;
                PC++;
                break;
            case 0x4d:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV C, L");
                cycles = 5;
                register.C = register.L;
                PC++;
                break;
            case 0x4e:
                // Length 1
                // Cycles 7, no flags
                // Move register to register
                sb.append("MOV C, M");
                cycles = 7;
                register.C = memory.readByte(register.HL());
                PC++;
                break;
            case 0x4f:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV C, A");
                cycles = 5;
                register.C = register.A;
                PC++;
                break;
            case 0x50:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV D, B");
                cycles = 5;
                register.D = register.B;
                PC++;
                break;
            case 0x51:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV D, C");
                cycles = 5;
                register.D = register.C;
                PC++;
                break;
            case 0x52:
                // Length 1
                // Cycles 5, no flags
                // move register to register
                sb.append("MOV D, D");
                cycles = 5;
                PC++;
                break;
            case 0x53:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV D, E");
                cycles = 5;
                register.D = register.E;
                PC++;
                break;
            case 0x54:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV D, H");
                cycles = 5;
                register.D = register.H;
                PC++;
                break;
            case 0x55:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV D, L");
                cycles = 5;
                register.D = register.L;
                PC++;
                break;
            case 0x56:
                // Length 1
                // Cycles 7, no flags
                // Move memory to register
                sb.append("MOV D, M");
                cycles = 7;
                register.D = memory.readByte(register.HL());
                PC++;
                break;
            case 0x57:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV D, A");
                cycles = 5;
                register.D = register.A;
                PC++;
                break;
            case 0x58:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV E, B");
                cycles = 5;
                register.E = register.B;
                PC++;
                break;
            case 0x59:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV E, C");
                cycles = 5;
                register.E = register.C;
                PC++;
                break;
            case 0x5a:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV E, D");
                cycles = 5;
                register.E = register.D;
                PC++;
                break;
            case 0x5b:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV E, E");
                cycles = 5;
                //register.E = register.E
                PC++;
                break;
            case 0x5c:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV E, H");
                cycles = 5;
                register.E = register.H;
                PC++;
                break;
            case 0x5d:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV E, L");
                cycles = 5;
                register.E = register.L;
                PC++;
                break;
            case 0x5e:
                // Length 1
                // Cycles 7, no flags
                // Move memory to register
                sb.append("MOV E, M");
                cycles = 7;
                register.E = memory.readByte(register.HL());
                PC++;
                break;
            case 0x5f:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV E, A");
                cycles = 5;
                register.E = register.A;
                PC++;
                break;
            case 0x60:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV H, B");
                cycles = 5;
                register.H = register.B;
                PC++;
                break;
            case 0x61:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV H, C");
                cycles = 5;
                register.H = register.C;
                PC++;
                break;
            case 0x62:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV H, D");
                cycles = 5;
                register.H = register.D;
                PC++;
                break;
            case 0x63:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV H, E");
                cycles = 5;
                register.H = register.E;
                PC++;
                break;
            case 0x64:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV H, H");
                cycles = 5;
                //register.H = register.H
                PC++;
                break;
            case 0x65:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV H, L");
                cycles = 5;
                register.H = register.L;
                PC++;
                break;
            case 0x66:
                // Length 1
                // Cycles 7, no flags
                // Move memory to register
                sb.append("MOV H, M");
                cycles = 7;
                register.H = memory.readByte(register.HL());
                PC++;
                break;
            case 0x67:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV H, A");
                cycles = 5;
                register.H = register.A;
                PC++;
                break;
            case 0x68:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV L, B");
                cycles = 5;
                register.L = register.B;
                PC++;
                break;
            case 0x69:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV L, C");
                cycles = 5;
                register.L = register.C;
                PC++;
                break;
            case 0x6a:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV L, D");
                cycles = 5;
                register.L = register.D;
                PC++;
                break;
            case 0x6b:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV L, E");
                cycles = 5;
                register.L = register.E;
                PC++;
                break;
            case 0x6c:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV L, H");
                cycles = 5;
                register.L = register.H;
                PC++;
                break;
            case 0x6d:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV L, L");
                cycles = 5;
                //register.L = register.L;
                PC++;
                break;
            case 0x6e:
                // Length 1
                // Cycles 7, no flags
                // Move memory to register
                sb.append("MOV L, M");
                cycles = 7;
                register.L = memory.readByte(register.HL());
                PC++;
                break;
            case 0x6f:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV L, A");
                cycles = 5;
                register.L = register.A;
                PC++;
                break;
            case 0x70:
                // Length 1
                // Cycles 7, no flags
                // Move register to memory
                sb.append("MOV M, B");
                cycles = 7;
                memory.writeByte(register.HL(), register.B);
                PC++;
                break;
            case 0x71:
                // Length 1
                // Cycles 7, no flags
                // Move register to memory
                sb.append("MOV M, C");
                cycles = 7;
                memory.writeByte(register.HL(), register.C);
                PC++;
                break;
            case 0x72:
                // Length 1
                // Cycles 7, no flags
                // Move register to memory
                sb.append("MOV M, D");
                cycles = 7;
                memory.writeByte(register.HL(), register.D);
                PC++;
                break;
            case 0x73:
                // Length 1
                // Cycles 7, no flags
                // Move register to memory
                sb.append("MOV M, E");
                cycles = 7;
                memory.writeByte(register.HL(), register.E);
                PC++;
                break;
            case 0x74:
                // Length 1
                // Cycles 7, no flags
                // Move register to memory
                sb.append("MOV M, H");
                cycles = 7;
                memory.writeByte(register.HL(), register.H);
                PC++;
                break;
            case 0x75:
                // Length 1
                // Cycles 7, no flags
                // Move register to memory
                sb.append("MOV M, L");
                cycles = 7;
                memory.writeByte(register.HL(), register.L);
                PC++;
                break;
            case 0x76:
                // Length 1
                // Cycles 7, no flags
                // Halt processor
                sb.append("HLT");
                cycles = 7;
                halt = true;
                PC++;
                break;
            case 0x77:
                // Length 1
                // Cycles 7, no flags
                // Move register to memory
                sb.append("MOV M, A");
                cycles = 7;
                memory.writeByte(register.HL(), register.A);
                PC++;
                break;
            case 0x78:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV A, B");
                cycles = 5;
                register.A = register.B;
                PC++;
                break;
            case 0x79:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV A, C");
                cycles = 5;
                register.A = register.C;
                PC++;
                break;
            case 0x7a:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV A, D");
                cycles = 5;
                register.A = register.D;
                PC++;
                break;
            case 0x7b:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV A, E");
                cycles = 5;
                register.A = register.E;
                PC++;
                break;
            case 0x7c:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV A, H");
                cycles = 5;
                register.A = register.H;
                PC++;
                break;
            case 0x7d:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV A, L");
                cycles = 5;
                register.A = register.L;
                PC++;
                break;
            case 0x7e:
                // Length 1
                // Cycles 7, no flags
                // Move memory to register
                sb.append("MOV A, M");
                cycles = 7;
                register.A = memory.readByte(register.HL());
                PC++;
                break;
            case 0x7f:
                // Length 1
                // Cycles 5, no flags
                // Move register to register
                sb.append("MOV A, A");
                cycles = 5;
                //register.A = register.A;
                PC++;
                break;
            case 0x80:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A
                sb.append("ADD B");
                cycles = 4;
                add(register.B);
                PC++;
                break;
            case 0x81:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A
                sb.append("ADD C");
                cycles = 4;
                add(register.C);
                PC++;
                break;
            case 0x82:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A
                sb.append("ADD D");
                cycles = 4;
                add(register.D);
                PC++;
                break;
            case 0x83:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A
                sb.append("ADD E");
                cycles = 4;
                add(register.E);
                PC++;
                break;
            case 0x84:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A
                sb.append("ADD H");
                cycles = 4;
                add(register.H);
                PC++;
                break;
            case 0x85:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A
                sb.append("ADD L");
                cycles = 4;
                add(register.L);
                PC++;
                break;
            case 0x86:
                // Length 1
                // Cycles 7, flags S Z A P C
                // Add memory to A
                sb.append("ADD M");
                cycles = 7;
                data = memory.readByte(register.HL());
                add(data);
                PC++;
                break;
            case 0x87:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add A to A
                sb.append("ADD A");
                cycles = 4;
                add(register.A);
                PC++;
                break;
            case 0x88:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                sb.append("ADC B");
                cycles = 4;
                adc(register.B);
                PC++;
                break;
            case 0x89:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                sb.append("ADC C");
                cycles = 4;
                adc(register.C);
                PC++;
                break;
            case 0x8a:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                sb.append("ADC D");
                cycles = 4;
                adc(register.D);
                PC++;
                break;
            case 0x8b:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                sb.append("ADC E");
                cycles = 4;
                adc(register.E);
                PC++;
                break;
            case 0x8c:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                sb.append("ADC H");
                cycles = 4;
                adc(register.H);
                PC++;
                break;
            case 0x8d:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                sb.append("ADC L");
                cycles = 4;
                adc(register.L);
                PC++;
                break;
            case 0x8e:
                // Length 1
                // Cycles 7, flags S Z A P C
                // Add memory to A with carry
                sb.append("ADC M");
                cycles = 7;
                data = memory.readByte(register.HL());
                adc(data);
                PC++;
                break;
            case 0x8f:
                // Length 1
                // Cycles 4, flags S Z A P C
                // Add register to A with carry
                sb.append("ADC A");
                cycles = 4;
                adc(register.A);
                PC++;
                break;
            case 0x90:
                // Length 1
                // Cycles 4, S Z A P
                // Subtract B from A
                sb.append("SUB B");
                cycles = 4;
                sub(register.B);
                PC++;
                break;
            case 0x91:
                // Length 1
                // Cycles 4, S Z A P
                // Subtract C from A
                sb.append("SUB C");
                cycles = 4;
                sub(register.C);
                PC++;
                break;
            case 0x92:
                // Length 1
                // Cycles 4, S Z A P
                // Subtract D from A
                sb.append("SUB D");
                cycles = 4;
                sub(register.D);
                PC++;
                break;
            case 0x93:
                // Length 1
                // Cycles 4, S Z A P
                // Subtract E from A
                sb.append("SUB E");
                cycles = 4;
                sub(register.E);
                PC++;
                break;
            case 0x94:
                // Length 1
                // Cycles 4, S Z A P
                // Subtract H from A
                sb.append("SUB H");
                cycles = 4;
                sub(register.H);
                PC++;
                break;
            case 0x95:
                // Length 1
                // Cycles 4, S Z A P
                // Subtract L from A
                sb.append("SUB L");
                cycles = 4;
                sub(register.L);
                PC++;
                break;
            case 0x96:
                // Length 1
                // Cycles 7, S Z A P
                // Subtract M from A
                sb.append("SUB M");
                cycles = 7;
                data = memory.readByte(register.HL());
                sub(data);
                PC++;
                break;
            case 0x97:
                // Length 1
                // Cycles 4, S Z A P
                // Subtract A from A
                sb.append("SUB A");
                cycles = 4;
                sub(register.A);
                PC++;
                break;
            case 0x98:
                // Length 1
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                sb.append("SBB B");
                cycles = 4;
                sbb(register.B);
                PC++;
                break;
            case 0x99:
                // Length 1
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                sb.append("SBB C");
                cycles = 4;
                sbb(register.C);
                PC++;
                break;
            case 0x9a:
                // Length 1
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                sb.append("SBB D");
                cycles = 4;
                sbb(register.D);
                PC++;
                break;
            case 0x9b:
                // Length 1
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                sb.append("SBB E");
                cycles = 4;
                sbb(register.E);
                PC++;
                break;
            case 0x9c:
                // Length 1
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                sb.append("SBB H");
                cycles = 4;
                sbb(register.H);
                PC++;
                break;
            case 0x9d:
                // Length 1
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                sb.append("SBB L");
                cycles = 4;
                sbb(register.L);
                PC++;
                break;
            case 0x9e:
                // Length 1
                // Cycles 7, flags S Z A C P
                // Subtract register from A with borrow
                sb.append("SBB M");
                cycles = 7;
                data = memory.readByte(register.HL());
                sbb(data);
                PC++;
                break;
            case 0x9f:
                // Length 1
                // Cycles 4, flags S Z A C P
                // Subtract register from A with borrow
                sb.append("SBB A");
                cycles = 4;
                sbb(register.A);
                PC++;
                break;
            case 0xa0:
                // Length 1
                // Cycle 4, flags S Z A C P
                // AND register with A
                sb.append("ANA B");
                cycles = 4;
                ana(register.B);
                PC++;
                break;
            case 0xa1:
                // Length 1
                // Cycle 4, flags S Z A C P
                // AND register with A
                sb.append("ANA C");
                cycles = 4;
                ana(register.C);
                PC++;
                break;
            case 0xa2:
                // Length 1
                // Cycle 4, flags S Z A C P
                // AND register with A
                sb.append("ANA D");
                cycles = 4;
                ana(register.D);
                PC++;
                break;
            case 0xa3:
                // Length 1
                // Cycle 4, flags S Z A C P
                // AND register with A
                sb.append("ANA E");
                cycles = 4;
                ana(register.E);
                PC++;
                break;
            case 0xa4:
                // Length 1
                // Cycle 4, flags S Z A C P
                // AND register with A
                sb.append("ANA H");
                cycles = 4;
                ana(register.H);
                PC++;
                break;
            case 0xa5:
                // Length 1
                // Cycle 4, flags S Z A C P
                // AND register with A
                sb.append("ANA L");
                cycles = 4;
                ana(register.L);
                PC++;
                break;
            case 0xa6:
                // Length 1
                // Cycle 7, flags S Z A C P
                // AND register with A
                sb.append("ANA M");
                cycles = 7;
                data = memory.readByte(register.HL());
                ana(data);
                PC++;
                break;
            case 0xa7:
                // Length 1
                // Cycle 4, flags S Z A P C
                // AND register with A
                sb.append("ANA A");
                cycles = 4;
                ana(register.A);
                PC++;
                break;
            case 0xa8:
                // Length 1
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                sb.append("XRA B");
                cycles = 4;
                xra(register.B);
                PC++;
                break;
            case 0xa9:
                // Length 1
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                sb.append("XRA C");
                cycles = 4;
                xra(register.C);
                PC++;
                break;
            case 0xaa:
                // Length 1
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                sb.append("XRA D");
                cycles = 4;
                xra(register.D);
                PC++;
                break;
            case 0xab:
                // Length 1
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                sb.append("XRA E");
                cycles = 4;
                xra(register.E);
                PC++;
                break;
            case 0xac:
                // Length 1
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                sb.append("XRA H");
                cycles = 4;
                xra(register.H);
                PC++;
                break;
            case 0xad:
                // Length 1
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                sb.append("XRA L");
                cycles = 4;
                xra(register.L);
                PC++;
                break;
            case 0xae:
                // Length 1
                // Cycles 7, flags S Z A P C
                // ExclusiveOR memory with A
                sb.append("XRA M");
                cycles = 7;
                data = memory.readByte(register.HL());
                xra(data);
                PC++;
                break;
            case 0xaf:
                // Length 1
                // Cycles 4, flags S Z A P C
                // ExclusiveOR register with A
                sb.append("XRA A");
                cycles = 4;
                xra(register.A);
                PC++;
                break;
            case 0xb0:
                // Length 1
                // Cycles 4, flags S Z A P C
                // OR  register with A
                sb.append("ORA B");
                cycles = 4;
                ora(register.B);
                PC++;
                break;
            case 0xb1:
                // Length 1
                // Cycles 4, flags S Z A P C
                // OR  register with A
                sb.append("ORA C");
                cycles = 4;
                ora(register.C);
                PC++;
                break;
            case 0xb2:
                // Length 1
                // Cycles 4, flags S Z A P C
                // OR  register with A
                sb.append("ORA D");
                cycles = 4;
                ora(register.D);
                PC++;
                break;
            case 0xb3:
                // Length 1
                // Cycles 4, flags S Z A P C
                // OR  register with A
                sb.append("ORA E");
                cycles = 4;
                ora(register.E);
                PC++;
                break;
            case 0xb4:
                // Length 1
                // Cycles 4, flags S Z A P C
                // OR  register with A
                sb.append("ORA H");
                cycles = 4;
                ora(register.H);
                PC++;
                break;
            case 0xb5:
                // Length 1
                // Cycles 4, flags S Z A P C
                // OR  register with A
                sb.append("ORA L");
                cycles = 4;
                ora(register.L);
                PC++;
                break;
            case 0xb6:
                // Length 1
                // Cycles 7, flags S Z A P C
                // OR  register with A
                sb.append("ORA M");
                cycles = 7;
                data = memory.readByte(register.HL());
                ora(data);
                PC++;
                break;
            case 0xb7:
                // Length 1
                // Cycles 4, flags S Z A P C
                // OR  register with A
                sb.append("ORA A");
                cycles = 4;
                ora(register.A);
                PC++;
                break;
            case 0xb8:
                // Length 1
                // Cycles 4, S C Z A P C
                // Compare register with A
                sb.append("CMP B");
                cycles = 4;
                cmp(register.B);
                PC++;
                break;
            case 0xb9:
                // Length 1
                // Cycles 4, S C Z A P C
                // Compare register with A
                sb.append("CMP C");
                cycles = 4;
                cmp(register.C);
                PC++;
                break;
            case 0xba:
                // Length 1
                // Cycles 4, S C Z A P C
                // Compare register with A
                sb.append("CMP D");
                cycles = 4;
                cmp(register.D);
                PC++;
                break;
            case 0xbb:
                // Length 1
                // Cycles 4, S C Z A P C
                // Compare register with A
                sb.append("CMP E");
                cycles = 4;
                cmp(register.E);
                PC++;
                break;
            case 0xbc:
                // Length 1
                // Cycles 4, S C Z A P C
                // Compare register with A
                sb.append("CMP H");
                cycles = 4;
                cmp(register.H);
                PC++;
                break;
            case 0xbd:
                // Length 1
                // Cycles 4, S C Z A P C
                // Compare register with A
                sb.append("CMP L");
                cycles = 4;
                cmp(register.L);
                PC++;
                break;
            case 0xbe:
                // Length 1
                // Cycles 7, S C Z A P C
                // Compare memory with A
                sb.append("CMP M");
                cycles = 7;
                data = memory.readByte(register.HL());
                cmp(data);
                PC++;
                break;
            case 0xbf:
                // Length 1
                // Cycles 4, S C Z A P C
                // Compare register with A
                sb.append("CMP A");
                cycles = 4;
                cmp(register.A);
                PC++;
                break;
            case 0xc0:
                // Length 1
                // Cycles 11/5, no flags
                // Return if not zero
                // Conditional return from subroutine
                sb.append("RNZ");
                if(!flag.zero) {
                    PC = memory.readWord(++PC);
                    SP += 2;
                    cycles = 11;
                } else {
                    cycles = 5;
                }
                PC++;
                break;
            case 0xc1:
                // Length 1
                // Cycles 10, no flags
                // POP stack into B
                sb.append("POP B");
                cycles = 10;
                register.C = memory.readByte(SP++);
                register.B = memory.readByte(SP++);
                PC++;
                break;
            case 0xc2:
                // Length 3
                // Cycles 10, no flags
                // Jump of NOT zero
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
                // Length 3
                // Cycles 10, no flags
                // Jump to address
                sb.append("JMP ");
                cycles = 10;
                PC = memory.readWord(++PC);
                sb.append(nString.hexToString16(PC));
                break;
            case 0xc4:
                // Length 3
                // Cycles 17/11, no flags
                // Call on NOT zero
                sb.append("CNZ ");
                address = memory.readWord(++PC);
                if(!flag.zero) {
                    SP -= 2;
                    memory.writeWord(SP, address);
                    PC = memory.readWord(address);
                    cycles = 17;
                } else {
                    PC += 2;
                    cycles = 11;
                }
                break;
            case 0xc5:
                // Length 1
                // Cycle 11, no flags
                // Push register pair BC onto stack
                sb.append("PUSH B");
                // it's backwards because of -- vs ++
                memory.writeByte(--SP, register.B);
                memory.writeByte(--SP, register.C);
                PC++;
                break;
            case 0xc6:
                // Length 2
                // Cycles 7, flags S Z A P C
                // Add immediate to A
                sb.append("ADI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString16(data));
                add(data);
                PC += 2;
                break;
            case 0xc7:
                // Length 1
                // Cycles 11, no flags
                // Push PC onto stack
                sb.append("RST 0");
                SP -= 2;
                memory.writeWord(SP, PC);
                PC++;
                break;
            case 0xc8:
                // Length 1
                // Cycles 11/5, no flags
                // Return if zero
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
                // Length 1
                // Cycles 10, no flags
                // Return
                sb.append("RET");
                cycles = 10;
                PC = memory.readWord(SP);
                SP += 2;
                break;
            case 0xca:
                // Length 3
                // Cycles 10, no flags
                // Jump if zero
                sb.append("JZ ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(flag.zero) {
                    PC = data;
                    cycles = 10;
                } else {
                    cycles = 3; // questionable
                    PC += 2;
                }
                break;
            case 0xcb:
                // Length 3
                // Cycles 10, no flags
                // Jump
                sb.append("*JMP ");
                cycles = 10;
                PC = memory.readWord(++PC);
                sb.append(nString.hexToString16(PC));
                break;
            case 0xcc:
                // Length 3
                // Cycles 17/11
                // Call if zero
                sb.append("CZ ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(flag.zero) {
                    cycles = 17;
                    SP -= 2;
                    // write return address to stack
                    memory.writeWord(SP, PC + 2);
                    // Get the pointer and put it in PC
                    PC = memory.readWord(data);
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xcd:
                // Length 3
                // Cycles 17, no flags
                // Call subroutine
                sb.append("CALL ");
                cycles = 17;
                // Write return address to stack
                SP -= 2;
                memory.writeWord(SP, PC + 2);
                PC = memory.readWord(++PC);
                sb.append(nString.hexToString16(PC));
                break;
            case 0xce:
                // Length 2
                // Cycles 7, flags S Z A P C
                // Add with carry immediate
                sb.append("ACI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                adc(data);
                PC++;
                break;
            case 0xcf:
                // Length 1
                // Cycles 11, no flags
                // Call 0x0008
                sb.append("RST 1");
                // Save PC on stack
                SP -= 2;
                memory.writeWord(SP, PC);
                PC = 0x0008;
                break;
            case 0xd0:
                // Length 1
                // Cycles 11/5, no flags
                // Return no carry
                sb.append("RNC");
                if(!flag.carry) {
                    PC = memory.readWord(SP);
                    SP += 2;
                    cycles = 11;
                } else {
                    cycles = 5;
                    PC++;
                }
                PC++;
                break;
            case 0xd1:
                // Length 1
                // Cycles 10, no flags
                // POP D off stack
                sb.append("POP D");
                cycles = 10;
                register.DE(memory.readWord(SP));
                PC++;
                break;
            case 0xd2:
                // Length 3
                // Cycles 10, no flags
                // Jump no carry
                sb.append("JNC ");
                PC++;
                data = memory.readWord(PC);
                sb.append(nString.hexToString16(data));
                if(!flag.carry) {
                    cycles = 10;
                    PC = data;
                } else {
                    cycles = 3; // Questionable
                    PC += 2;
                }
                break;
            case 0xd3:
                // Length 2
                // Cycles 10, no flags
                // Data from A register placed in Port.
                sb.append("OUT ");
                data = memory.readByte(++PC);
                sb.append(nString.hexToString16(data));
                // data has the device # in it.
                // INCOMPLETE
                // Used for audio
                System.out.println("OUT DEV=" + nString.hexToString8(data) + " A=" + nString.hexToString8(register.A));
                PC++;
                break;
            case 0xd4:
                // Length 3
                // Cycles 17/11, no flags
                // Call no carry
                sb.append("CNC ");
                PC++;
                data = memory.readWord(PC);
                sb.append(nString.hexToString16(data));
                if(!flag.carry) {
                    cycles = 17;
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = memory.readWord(data);
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xd5:
                // Length 1
                // Cycles 11, no flags
                // PUSH D
                sb.append("PUSH D");
                cycles = 11;
                SP -= 2;
                register.DE(memory.readWord(SP));
                PC++;
                break;
            case 0xd6:
                // Length 2
                // Cycles 7, flags S Z A P C
                // Subtracts 8 bit data from contents of A register.
                sb.append("SUI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                sub(data);
                PC++;
                break;
            case 0xd7:
                // Length 1
                // Cycles 11, no flags
                // PC = 0x0010
                sb.append("RST 2");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, PC);
                PC = 0x0010;
                break;
            case 0xd8:
                // Length 1
                // Cycles 11/5, no flags
                // Return carry
                sb.append("RC ");
                data = memory.readWord(SP);
                sb.append(nString.hexToString16(data));
                if(flag.carry) {
                    cycles = 11;
                    // Get Return address
                    PC = data;
                    SP += 2;
                } else {
                    cycles = 5;
                    PC++;
                }
                break;
            case 0xd9:
                // Length 1
                // Cycles 10, no flags
                // Return
                sb.append("*RET");
                cycles = 10;
                PC = memory.readWord(SP);
                SP += 2;
                break;
            case 0xda:
                // Length 3
                // Cycles 10
                // Jump carry
                sb.append("JC ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(flag.carry) {
                    cycles = 10;
                    PC = data;
                } else {
                    cycles = 3; // questionable?
                    PC += 2;
                }
                break;
            case 0xdb:
                // Length 2
                // Cycles 10, no flags
                // Data from Port placed in A register.
                // INCOMPLETE!
                sb.append("IN ");
                cycles = 10;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                System.out.println("IN " + nString.hexToString8(data));
                PC += 2;
                break;
            case 0xdc:
                // Length 3
                // Cycles 17/11, no flags
                // Call carry
                sb.append("CC ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(flag.carry) {
                    cycles = 17;
                    SP -= 2;
                    // Save return address on stack
                    memory.writeWord(SP, PC + 2);
                    PC = data;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xdd:
                // Length 3
                // Cycles 17, no flags
                // Call
                sb.append("*CALL");
                cycles = 17;
                SP -= 2;
                PC++;
                memory.writeWord(SP, PC + 2);
                // Get pointer
                PC = memory.readWord(PC);
                sb.append(nString.hexToString16(PC));
                PC += 3;
                break;
            case 0xde:
                // Length 2
                // Cycles 7, flags S Z A P C
                // Subtract with borrow immediately
                sb.append("SBI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                sbb(data);
                PC++;
                break;
            case 0xdf:
                // Length 1
                // Cycles 11, no flags
                // PC = 0x0018
                sb.append("RST 3");
                SP -= 2;
                memory.writeWord(SP, PC);
                PC = 0x0018;
                break;
            case 0xe0:
                // Length 1
                // Cycles 11/5, no flags
                // Return if parity odd
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
                // Length 1
                // Cycles 10, no flags
                // POP H
                sb.append("POP H");
                register.HL(memory.readWord(SP));
                SP += 2;
                PC++;
                break;
            case 0xe2:
                // Length 3
                // Cycles 10, no flags
                // Jump if parity odd
                sb.append("JPO ");
                cycles = 10;
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(!flag.parity) {
                    PC = data;
                } else {
                    PC += 2;
                }
                break;
            case 0xe3:
                // Length 1
                // Cycles 18, no flags
                // Exchanges HL with top of stack
                sb.append("XTHL");
                cycles = 18;
                data = register.L;
                register.L = memory.readByte(SP);
                memory.writeByte(SP, data);
                data = register.H;
                register.H = memory.readByte(SP + 1);
                memory.writeByte(SP + 1, data);
                PC++;
                break;
            case 0xe4:
                // Length 3
                // Cycles 17/11, no flags
                // Carry if parity odd
                sb.append("CPO ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(!flag.parity) {
                    cycles = 17;
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = data;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xe5:
                // Length 1
                // Cycles 11, no flags
                // PUSH H
                sb.append("PUSH H");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, register.HL());
                PC++;
                break;
            case 0xe6:
                // Length 2
                // Cycles 7, S Z A P C
                sb.append("ANI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                ana(data);
                PC++;
                break;
            case 0xe7:
                // Length 1
                // Cycles 11, no flags
                // PC = 0x0020
                sb.append("RST 4");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, PC);
                PC = 0x0020;
                break;
            case 0xe8:
                // Length 1
                // Cycles 11/5, no flags
                // Return if parity even
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
                // Length 1
                // Cycles 5, no flags
                // Puts contents of HL into PC (program counter) [=JMP (HL].
                sb.append("PCHL");
                cycles = 5;
                PC = register.HL();
                break;
            case 0xea:
                // Length 3
                // Cycles 10, no flags
                // Jump if parity even
                sb.append("JPE ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                cycles = 10;
                if(flag.parity) {
                    PC = data;
                } else {
                    PC += 2;
                }
                break;
            case 0xeb:
                // Length 1
                // Cycles 5, no flags
                // Exchanges HL and DE
                sb.append("XCHG");
                data = register.HL();
                register.HL(register.DE());
                register.DE(data);
                PC++;
                break;
            case 0xec:
                // Length 3
                // Cycles 17/11, no flags
                // Call if parity even
                sb.append("CPE ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(flag.parity) {
                    cycles = 17;
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = data;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xed:
                // Length 3
                // Cycles 17, no flags
                // Call
                sb.append("*CALL ");
                cycles = 17;
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                SP -= 2;
                memory.writeWord(SP, PC + 2);
                PC = data;
                break;
            case 0xee:
                // Length 2
                // Cycles 7, S Z A P C
                // Exclusive-OR 8 bit data with A register.
                sb.append("XRI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                xra(data);
                PC++;
                break;
            case 0xef:
                // Length 1
                // Cycles 11, no flags
                // PC = 0x0028
                sb.append("RST 5");
                cycles = 11;
                SP -= 2;
                memory.writeWord(SP, PC);
                PC = 0x0028;
                PC++;
                break;
            case 0xf0:
                // Length 1
                // Cycles 11/5, no flags
                // Return if not sign
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
                // Length 1
                // Cycles 10, S Z A P C
                sb.append("POP PSW");
                cycles = 10;
                data = memory.readByte(SP++);
                flag.carry = (data & 0x01) > 0;
                flag.parity = (data & 0x04) > 0;
                flag.auxCarry = (data & 0x10) > 0;
                flag.zero = (data & 0x40) > 0;
                flag.sign = (data & 0x80) > 0;
                register.A = memory.readByte(SP++);
                PC++;
                break;
            case 0xf2:
                // Length 3
                // Cycles 10, no flags
                // Jump if not sign
                sb.append("JP ");
                cycles = 10;
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(!flag.sign) {
                    PC = data;
                } else {
                    PC += 2;
                }
                break;
            case 0xf3:
                // Length 1
                // Cycles 4
                // Disable Interrupts
                sb.append("DI");
                cycles = 4;
                interrupts = false;
                PC++;
                break;
            case 0xf4:
                // Length 3
                // Cycles 17/11, no flags
                // Call if not sign
                sb.append("CP ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(!flag.sign) {
                    cycles = 17;
                    // Write return address to stack
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = data;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xf5:
                // Length 1
                // Cycles 11, no flags
                // PUSH PSW + A
                sb.append("PUSH PSW");
                cycles = 11;
                memory.writeByte(--SP, register.A);
                memory.writeByte(--SP, flag.PSW());
                PC++;
                break;
            case 0xf6:
                // Length 2
                // Cycles 7, flags S Z A P C
                // Logically ORs 8 bit data with contents of A register.
                sb.append("ORI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                ora(data);
                PC++;
                break;
            case 0xf7:
                // Length 1
                // Cycles 11, no flags
                // PC = 0x0030
                sb.append("RST 6");
                // store pc
                SP -= 2;
                memory.writeWord(SP, PC);
                PC = 0x0030;
                break;
            case 0xf8:
                // Length 1
                // Cycles 11/5, no flags
                // Return if sign
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
                // Length 1
                // Cycles 5, no flags
                // Puts contents of HL into SP
                sb.append("SPHL");
                cycles = 5;
                SP = register.HL();
                PC++;
                break;
            case 0xfa:
                // Length 3
                // Cycles 10, no flags
                // Jump if sign
                sb.append("JM ");
                cycles = 10;
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(flag.sign) {
                    PC = data;
                } else {
                    PC += 2;
                }
                break;
            case 0xfb:
                // Length 1
                // Cycles 4, no flags
                // Enable interrupts
                sb.append("EI");
                cycles = 4;
                interrupts = true;
                PC++;
                break;
            case 0xfc:
                // Length 3
                // Cycles 17/11, no flags
                // Call if sign
                sb.append("CM ");
                data = memory.readWord(++PC);
                sb.append(nString.hexToString16(data));
                if(flag.sign) {
                    cycles = 17;
                    SP -= 2;
                    memory.writeWord(SP, PC + 2);
                    PC = data;
                } else {
                    cycles = 11;
                    PC += 2;
                }
                break;
            case 0xfd:
                // Length 3
                // Cycles 17, no flags
                // Call
                sb.append("*CALL ");
                cycles = 17;
                SP -= 2;
                memory.writeWord(SP, PC + 2);
                PC = memory.readWord(++PC);
                sb.append(nString.hexToString16(PC));
                break;
            case 0xfe:
                // Length 2
                // Cycles 7, flags S Z A P C
                // Compares 8 bit data with contents of A register.
                sb.append("CPI ");
                cycles = 7;
                data = memory.readByte(++PC);
                sb.append(nString.hexToString8(data));
                cmp(data);
                PC++;
                break;
            case 0xff:
                // Length 1
                // Cycles 11, no flags
                // PC = 0x0038
                sb.append("RST 7");
                SP -= 2;
                memory.writeWord(SP, PC);
                PC = 0x0038;
                break;
        }

        currentInstruction = sb.toString();
        return cycles;
    }
}
