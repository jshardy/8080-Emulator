package GUI;

import Core.CPU;
import Core.CPUChanged;
import Utilities.Utils.nString;

import javax.swing.*;
import java.awt.*;

public class DebugWindow extends JFrame implements CPUChanged {
    private JTextArea txtDebug = new JTextArea();
    private JScrollPane areaScrollPane = new JScrollPane(txtDebug);

    public DebugWindow() {
        setLayout(new GridLayout());

        setSize(600,300);
        setLocation(1400,1000);

        txtDebug.setLineWrap(true);
        txtDebug.setWrapStyleWord(true);

        add(areaScrollPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void print(String message) {
        txtDebug.append(message);
        txtDebug.setSelectionStart(txtDebug.getText().length());
    }

    public void printLine(String message) {
        txtDebug.append(message + "\n");
        areaScrollPane.getViewport().setViewPosition(new Point(0, this.getHeight() * 3));

    }

    @Override
    public void Updated(CPU cpu) {
        StringBuilder sb = new StringBuilder();
        sb.append("A=");
        sb.append(nString.hexToString8(cpu.getA()).substring(2));
        sb.append(" BC=");
        sb.append(nString.hexToString16(cpu.getBC()).substring(2));
        sb.append(" DE=");
        sb.append(nString.hexToString16(cpu.getDE()).substring(2));
        sb.append(" HL=");
        sb.append(nString.hexToString16(cpu.getHL()).substring(2));
        sb.append(" SP=");
        sb.append(nString.hexToString16(cpu.getSP()).substring(2));
        sb.append(" I");
        sb.append(cpu.getInterrupts() ? "1" : "0");
        sb.append(" S");
        sb.append(cpu.getSign() ? "1" : "0");
        sb.append(" Z");
        sb.append(cpu.getZero() ? "1" : "0");
        sb.append(" A");
        sb.append(cpu.getAuxCarry() ? "1" : "0");
        sb.append(" P");
        sb.append(cpu.getParity() ? "1" : "0");
        sb.append(" C");
        sb.append(cpu.getCarry() ? "1" : "0");
        printLine(sb.toString());
        sb = new StringBuilder();
        sb.append(nString.hexToString16(cpu.getCurrentInstructionAddress()).substring(2));
        sb.append(" ").append(cpu.getRAW()).append("\t");
        sb.append(cpu.getCurrentInstruction());
        printLine(sb.toString());
    }
}
