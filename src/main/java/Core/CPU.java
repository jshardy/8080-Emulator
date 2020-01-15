package Core;

public class CPU {
    Memory memory;
    Registers registers;
    int pc = 0;

    public CPU(byte[] mem) {
        memory = new Memory(mem);
        memory.printOutMemory();
    }

    public int getPC() {
        return pc;
    }

    public String step_disassemble() {
        StringBuilder sb = new StringBuilder();
        int instruction = memory.getAddress(pc);

        switch(instruction) {
            case 0x00:
                sb.append("NOP");
                pc++;
                break;
            case 0x01:
                sb.append("LXI B, ");
                pc += 3;
                break;
            case 0x02:
                sb.append("STAX B");
                pc++;
                break;
            case 0x03:
                sb.append("INX B");
                pc++;
                break;
            case 0x04:
                sb.append("INR B");
                pc++;
                break;
            case 0x05:
                sb.append("DCR B");
                pc++;
                break;
            case 0x06:
                sb.append("MVI B, ");
                pc += 2;
                break;
            case 0x07:
                sb.append("RLC");
                pc++;
                break;
            case 0x08:
                sb.append("NOT AN INSTRUCTION");
                pc++;
                break;
            case 0x09:
                sb.append("DAD B");
                pc++;
                break;
            case 0x0a:
                sb.append("LDAX B");
                pc++;
                break;
            case 0x0b:
                sb.append("DCX B");
                pc++;
                break;
            case 0x0c:
                sb.append("INR C");
                pc++;
                break;
            case 0x0d:
                sb.append("DCR C");
                pc++;
                break;
            case 0x0e:
                sb.append("MVI C");
                pc += 2;
                break;
            case 0x0f:
                sb.append("RRC");
                pc++;
                break;
            case 0x10:
                sb.append("NOT AN INSTRUCTION");
                pc++;
                break;
            case 0x11:
                sb.append("LXI D, ");
                pc += 3;
                break;
            case 0x12:
                sb.append("STAX D");
                pc++;
                break;
            case 0x13:
                sb.append("INX D");
                pc++;
                break;
            case 0x14:
                sb.append("INR D");
                pc++;
                break;
            case 0x15:
                sb.append("DCR D");
                pc++;
                break;
            case 0x16:
                sb.append("MVI D, ");
                pc += 2;
                break;
            case 0x17:
                sb.append("RAL");
                pc++;
                break;
            case 0x18:
                sb.append("NOT AN INSTRUCTION");
                pc++;
                break;
            case 0x19:
                sb.append("DAD D");
                pc++;
                break;
            case 0x1a:
                sb.append("LDAX D");
                pc++;
                break;
            case 0x1b:
                sb.append("DCX D");
                pc++;
                break;
            case 0x1c:
                sb.append("INR E");
                pc++;
                break;
            case 0x1d:
                sb.append("DCR E");
                pc++;
                break;
            case 0x1e:
                sb.append("MVI E, ");
                pc += 2;
                break;
            case 0x1f:
                sb.append("RAR");
                pc++;
                break;
            case 0x20:
                sb.append("NOT AN INSTRUCTION");
                pc++;
                break;
            case 0x21:
                sb.append("LXI H, ");
                pc += 3;
                break;
            case 0x22:
                sb.append("SHLD adr");
                pc += 3;
                break;
            case 0x23:
                sb.append("INX H");
                pc++;
                break;
            case 0x24:
                sb.append("INR H");
                pc++;
                break;
            case 0x25:
                sb.append("DCR H");
                pc++;
                break;
            case 0x26:
                sb.append("MVI H, ");
                pc += 2;
                break;
            case 0x27:
                sb.append("DAA");
                pc++;
                break;
            case 0x28:
                sb.append("NOT AN INSTRUCTION");
                pc++;
                break;
            case 0x29:
                sb.append("DAD H");
                pc++;
                break;
            case 0x2a:
                sb.append("LHLD adr");
                pc += 3;
                break;
            case 0x2b:
                sb.append("DCX H");
                pc++;
                break;
            case 0x2c:
                sb.append("INR L");
                pc++;
                break;
            case 0x2d:
                sb.append("DCR L");
                pc++;
                break;
            case 0x2e:
                sb.append("MVI L, ");
                pc += 2;
                break;
            case 0x2f:
                sb.append("CMA");
                pc++;
                break;
            case 0x30:
                sb.append("NOT AN INSTRUCTION");
                pc++;
                break;
            case 0x31:
                sb.append("LXI SP, ");
                pc += 3;
                break;
            case 0x32:
                sb.append("STA adr");
                pc += 3;
                break;
            case 0x33:
                sb.append("INX SP");
                pc++;
                break;
            case 0x34:
                sb.append("INR M");
                pc++;
                break;
            case 0x35:
                sb.append("DCR M");
                pc++;
                break;
            case 0x36:
                sb.append("MVI M, ");
                pc += 2;
                break;
            case 0x37:
                sb.append("STC");
                pc++;
                break;
            case 0x38:
                sb.append("NOT AN INSTRUCTION");
                pc++;
                break;
            case 0x39:
                sb.append("DAD SP");
                pc++;
                break;
            case 0x3a:
                sb.append("LDA adr");
                pc += 3;
                break;
            case 0x3b:
                sb.append("DCX SP");
                pc++;
                break;
            case 0x3c:
                sb.append("INR A");
                pc++;
                break;
            case 0x3d:
                sb.append("DCR A");
                pc++;
                break;
            case 0x3e:
                sb.append("MVI A, ");
                pc += 2;
                break;
            case 0x3f:
                sb.append("CMC");
                pc++;
                break;
            case 0x40:
                sb.append("MOV B, B");
                pc++;
                break;
            case 0x41:
                sb.append("MOV B, C");
                pc++;
                break;
            case 0x42:
                sb.append("MOV B, D");
                pc++;
                break;
            case 0x43:
                sb.append("MOV B, E");
                pc++;
                break;
            case 0x44:
                sb.append("MOV B, H");
                pc++;
                break;
            case 0x45:
                sb.append("MOV B, L");
                pc++;
                break;
            case 0x46:
                sb.append("MOV B, M");
                pc++;
                break;
            case 0x47:
                sb.append("MOV B, A");
                pc++;
                break;
            case 0x48:
                sb.append("MOV C, B");
                pc++;
                break;
            case 0x49:
                sb.append("MOV C, C");
                pc++;
                break;
            case 0x4a:
                sb.append("MOV C, D");
                pc++;
                break;
            case 0x4b:
                sb.append("MOV C, E");
                pc++;
                break;
            case 0x4c:
                sb.append("MOV C, H");
                pc++;
                break;
            case 0x4d:
                sb.append("MOV C, L");
                pc++;
                break;
            case 0x4e:
                sb.append("MOV C, M");
                pc++;
                break;
            case 0x4f:
                sb.append("MOV C, A");
                pc++;
                break;
            case 0x50:
                sb.append("MOV D, B");
                pc++;
                break;
            case 0x51:
                sb.append("MOV D, C");
                pc++;
                break;
            case 0x52:
                sb.append("MOV D, D");
                pc++;
                break;
            case 0x53:
                sb.append("MOV D, E");
                pc++;
                break;
            case 0x54:
                sb.append("MOV D, H");
                pc++;
                break;
            case 0x55:
                sb.append("MOV D, L");
                pc++;
                break;
            case 0x56:
                sb.append("MOV D, M");
                pc++;
                break;
            case 0x57:
                sb.append("MOV D, A");
                pc++;
                break;
            case 0x58:
                sb.append("MOV E, B");
                pc++;
                break;
            case 0x59:
                sb.append("MOV E, C");
                pc++;
                break;
            case 0x5a:
                sb.append("MOV E, D");
                pc++;
                break;
            case 0x5b:
                sb.append("MOV E, E");
                pc++;
                break;
            case 0x5c:
                sb.append("MOV E, H");
                pc++;
                break;
            case 0x5d:
                sb.append("MOV E, L");
                pc++;
                break;
            case 0x5e:
                sb.append("MOV E, M");
                pc++;
                break;
            case 0x5f:
                sb.append("MOV E, A");
                pc++;
                break;
            case 0x60:
                sb.append("MOV E, B");
                pc++;
                break;
            case 0x61:
                sb.append("MOV E, C");
                pc++;
                break;
            case 0x62:
                sb.append("MOV E, D");
                pc++;
                break;
            case 0x63:
                sb.append("MOV H, E");
                pc++;
                break;
            case 0x64:
                sb.append("MOV H, H");
                pc++;
                break;
            case 0x65:
                sb.append("MOV H, L");
                pc++;
                break;
            case 0x66:
                sb.append("MOV H, M");
                pc++;
                break;
            case 0x67:
                sb.append("MOV H, A");
                pc++;
                break;
            case 0x68:
                sb.append("MOV L, B");
                pc++;
                break;
            case 0x69:
                sb.append("MOV L, C");
                pc++;
                break;
            case 0x6a:
                sb.append("MOV L, D");
                pc++;
                break;
            case 0x6b:
                sb.append("MOV L, E");
                pc++;
                break;
            case 0x6c:
                sb.append("MOV L, H");
                pc++;
                break;
            case 0x6d:
                sb.append("MOV L, L");
                pc++;
                break;
            case 0x6e:
                sb.append("MOV L, M");
                pc++;
                break;
            case 0x6f:
                sb.append("MOV L, A");
                pc++;
                break;
            case 0x70:
                sb.append("MOV M, B");
                pc++;
                break;
            case 0x71:
                sb.append("MOV M, C");
                pc++;
                break;
            case 0x72:
                sb.append("MOV M, D");
                pc++;
                break;
            case 0x73:
                sb.append("MOV M, E");
                pc++;
                break;
            case 0x74:
                sb.append("MOV M, H");
                pc++;
                break;
            case 0x75:
                sb.append("MOV M, L");
                pc++;
                break;
            case 0x76:
                sb.append("HLT");
                pc++;
                break;
            case 0x77:
                sb.append("MOV M, A");
                pc++;
                break;
            case 0x78:
                sb.append("MOV A, B");
                pc++;
                break;
            case 0x79:
                sb.append("MOV A, C");
                pc++;
                break;
            case 0x7a:
                sb.append("MOV A, D");
                pc++;
                break;
            case 0x7b:
                sb.append("MOV A, E");
                pc++;
                break;
            case 0x7c:
                sb.append("MOV A, H");
                pc++;
                break;
            case 0x7d:
                sb.append("MOV A, L");
                pc++;
                break;
            case 0x7e:
                sb.append("MOV A, M");
                pc++;
                break;
            case 0x7f:
                sb.append("MOV A, A");
                pc++;
                break;
            case 0x80:
                sb.append("ADD B");
                pc++;
                break;
            case 0x81:
                sb.append("ADD C");
                pc++;
                break;
            case 0x82:
                sb.append("ADD D");
                pc++;
                break;
            case 0x83:
                sb.append("ADD E");
                pc++;
                break;
            case 0x84:
                sb.append("ADD H");
                pc++;
                break;
            case 0x85:
                sb.append("ADD L");
                pc++;
                break;
            case 0x86:
                sb.append("ADD M");
                pc++;
                break;
            case 0x87:
                sb.append("ADD A");
                pc++;
                break;
            case 0x88:
                sb.append("ADC B");
                pc++;
                break;
            case 0x89:
                sb.append("ADC C");
                pc++;
                break;
            case 0x8a:
                sb.append("ADC D");
                pc++;
                break;
            case 0x8b:
                sb.append("ADC E");
                pc++;
                break;
            case 0x8c:
                sb.append("ADC H");
                pc++;
                break;
            case 0x8d:
                sb.append("ADC L");
                pc++;
                break;
            case 0x8e:
                sb.append("ADC M");
                pc++;
                break;
            case 0x8f:
                sb.append("ADC A");
                pc++;
                break;
            case 0x90:
                sb.append("SUB B");
                pc++;
                break;
            case 0x91:
                sb.append("SUB C");
                pc++;
                break;
            case 0x92:
                sb.append("SUB D");
                pc++;
                break;
            case 0x93:
                sb.append("SUB E");
                pc++;
                break;
            case 0x94:
                sb.append("SUB H");
                pc++;
                break;
            case 0x95:
                sb.append("SUB L");
                pc++;
                break;
            case 0x96:
                sb.append("SUB M");
                pc++;
                break;
            case 0x97:
                sb.append("SUB A");
                pc++;
                break;
            case 0x98:
                sb.append("SBB B");
                pc++;
                break;
            case 0x99:
                sb.append("SBB C");
                pc++;
                break;
            case 0x9a:
                sb.append("SBB D");
                pc++;
                break;
            case 0x9b:
                sb.append("SBB E");
                pc++;
                break;
            case 0x9c:
                sb.append("SBB H");
                pc++;
                break;
            case 0x9d:
                sb.append("SBB L");
                pc++;
                break;
            case 0x9e:
                sb.append("SBB M");
                pc++;
                break;
            case 0x9f:
                sb.append("SBB A");
                pc++;
                break;
            case 0xa0:
                sb.append("ANA B");
                pc++;
                break;
            case 0xa1:
                sb.append("ANA C");
                pc++;
                break;
            case 0xa2:
                sb.append("ANA D");
                pc++;
                break;
            case 0xa3:
                sb.append("ANA E");
                pc++;
                break;
            case 0xa4:
                sb.append("ANA H");
                pc++;
                break;
            case 0xa5:
                sb.append("ANA L");
                pc++;
                break;
            case 0xa6:
                sb.append("ANA M");
                pc++;
                break;
            case 0xa7:
                sb.append("ANA A");
                pc++;
                break;
            case 0xa8:
                sb.append("XRA B");
                pc++;
                break;
            case 0xa9:
                sb.append("XRA C");
                pc++;
                break;
            case 0xaa:
                sb.append("XRA D");
                pc++;
                break;
            case 0xab:
                sb.append("XRA E");
                pc++;
                break;
            case 0xac:
                sb.append("XRA H");
                pc++;
                break;
            case 0xad:
                sb.append("XRA L");
                pc++;
                break;
            case 0xae:
                sb.append("XRA M");
                pc++;
                break;
            case 0xaf:
                sb.append("XRA A");
                pc++;
                break;
            case 0xb0:
                sb.append("ORA B");
                pc++;
                break;
            case 0xb1:
                sb.append("ORA C");
                pc++;
                break;
            case 0xb2:
                sb.append("ORA D");
                pc++;
                break;
            case 0xb3:
                sb.append("ORA E");
                pc++;
                break;
            case 0xb4:
                sb.append("ORA H");
                pc++;
                break;
            case 0xb5:
                sb.append("ORA L");
                pc++;
                break;
            case 0xb6:
                sb.append("ORA M");
                pc++;
                break;
            case 0xb7:
                sb.append("ORA A");
                pc++;
                break;
            case 0xb8:
                sb.append("CMP B");
                pc++;
                break;
            case 0xb9:
                sb.append("CMP C");
                pc++;
                break;
            case 0xba:
                sb.append("CMP D");
                pc++;
                break;
            case 0xbb:
                sb.append("CMP E");
                pc++;
                break;
            case 0xbc:
                sb.append("CMP H");
                pc++;
                break;
            case 0xbd:
                sb.append("CMP L");
                pc++;
                break;
            case 0xbe:
                sb.append("CMP M");
                pc++;
                break;
            case 0xbf:
                sb.append("CMP A");
                pc++;
                break;
            case 0xc0:
                sb.append("RNZ");
                pc++;
                break;
            case 0xc1:
                sb.append("POP B");
                pc++;
                break;
            case 0xc2:
                sb.append("JNZ adr");
                pc += 3;
                break;
            case 0xc3:
                sb.append("JMP adr");
                pc += 3;
                break;
            case 0xc4:
                sb.append("CNZ adr");
                pc += 3;
                break;
            case 0xc5:
                sb.append("PUSH B");
                pc++;
                break;
            case 0xc6:
                sb.append("ADI");
                pc += 2;
                break;
            case 0xc7:
                sb.append("RST 0");
                pc++;
                break;
            case 0xc8:
                sb.append("RZ");
                pc++;
                break;
            case 0xc9:
                sb.append("RET");
                pc++;
                break;
            case 0xca:
                sb.append("JZ adr");
                pc += 3;
                break;
            case 0xcb:
                sb.append("*JMP adr");
                pc += 3;
                break;
            case 0xcc:
                sb.append("CZ adr");
                pc += 3;
                break;
            case 0xcd:
                sb.append("CALL adr");
                pc += 3;
                break;
            case 0xce:
                sb.append("ACI");
                pc += 2;
                break;
            case 0xcf:
                sb.append("RST 1");
                pc++;
                break;
            case 0xd0:
                sb.append("RNC");
                pc++;
                break;
            case 0xd1:
                sb.append("POP D");
                pc++;
                break;
            case 0xd2:
                sb.append("JNC adr");
                pc += 3;
                break;
            case 0xd3:
                sb.append("OUT");
                pc += 2;
                break;
            case 0xd4:
                sb.append("CNC adr");
                pc += 3;
                break;
            case 0xd5:
                sb.append("PUSH D");
                pc++;
                break;
            case 0xd6:
                sb.append("SUI");
                pc += 2;
                break;
            case 0xd7:
                sb.append("RST 2");
                pc++;
                break;
            case 0xd8:
                sb.append("RC");
                pc++;
                break;
            case 0xd9:
                sb.append("*RET");
                pc++;
                break;
            case 0xda:
                sb.append("JC adr");
                pc += 3;
                break;
            case 0xdb:
                sb.append("IN");
                pc += 2;
                break;
            case 0xdc:
                sb.append("CC adr");
                pc += 3;
                break;
            case 0xdd:
                sb.append("*CALL");
                pc += 3;
                break;
            case 0xde:
                sb.append("SBI");
                pc += 2;
                break;
            case 0xdf:
                sb.append("RST 3");
                pc++;
                break;
            case 0xe0:
                sb.append("RPO");
                pc++;
                break;
            case 0xe1:
                sb.append("POP H");
                pc++;
                break;
            case 0xe2:
                sb.append("JPO adr");
                pc += 3;
                break;
            case 0xe3:
                sb.append("XHTL");
                pc++;
                break;
            case 0xe4:
                sb.append("CPO adr");
                pc += 3;
                break;
            case 0xe5:
                sb.append("PUSH H");
                pc++;
                break;
            case 0xe6:
                sb.append("ANI");
                pc += 2;
                break;
            case 0xe7:
                sb.append("RST 4");
                pc++;
                break;
            case 0xe8:
                sb.append("RPE");
                pc++;
                break;
            case 0xe9:
                sb.append("PCHL");
                pc++;
                break;
            case 0xea:
                sb.append("JPE adr");
                pc += 3;
                break;
            case 0xeb:
                sb.append("XCHG");
                pc++;
                break;
            case 0xec:
                sb.append("CPE adr");
                pc += 3;
                break;
            case 0xed:
                sb.append("*CALL");
                pc += 3;
                break;
            case 0xee:
                sb.append("XRI");
                pc += 2;
                break;
            case 0xef:
                sb.append("RST 5");
                pc++;
                break;
            case 0xf0:
                sb.append("RP");
                pc++;
                break;
            case 0xf1:
                sb.append("POP PSW");
                pc++;
                break;
            case 0xf2:
                sb.append("JP adr");
                pc += 3;
                break;
            case 0xf3:
                sb.append("DI");
                pc++;
                break;
            case 0xf4:
                sb.append("CP adr");
                pc += 3;
                break;
            case 0xf5:
                sb.append("PUSH PSW");
                pc++;
                break;
            case 0xf6:
                sb.append("ORI");
                pc += 2;
                break;
            case 0xf7:
                sb.append("RST 6");
                pc++;
                break;
            case 0xf8:
                sb.append("RM");
                pc++;
                break;
            case 0xf9:
                sb.append("SPHL");
                pc++;
                break;
            case 0xfa:
                sb.append("JM adr");
                pc += 3;
                break;
            case 0xfb:
                sb.append("EI");
                pc++;
                break;
            case 0xfc:
                sb.append("CM adr");
                pc += 3;
                break;
            case 0xfd:
                sb.append("*CALL");
                pc += 3;
                break;
            case 0xfe:
                sb.append("CPI");
                pc += 2;
                break;
            case 0xff:
                sb.append("RST 7");
                pc++;
                break;
        }
        return sb.toString();
    }
}
