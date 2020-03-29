package GUI;

import Core.CPU;
import Core.CPUChanged;
import Core.CPUState;
import Utilities.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class DebugArea extends JPanel implements CPUChanged {
    private InstructionPanel instructionPanel = new InstructionPanel();
    private OptionsPanel optionsPanel = new OptionsPanel();
    private RegisterPanel registerPanel = new RegisterPanel();
    private FlagsPanel flagsPanel = new FlagsPanel();

    public DebugArea() {
        //setMinimumSize(new Dimension(200,600));
        JPanel child = new JPanel();
        child.setLayout(new GridLayout(4, 1));
        setLayout(new FlowLayout());

        child.add(optionsPanel);
        child.add(registerPanel);
        child.add(flagsPanel);
        child.add(instructionPanel);
        add(child);
    }

    public void setPC(String text) {
        instructionPanel.txtPC.setText(text);
    }

    public void setNmemonic1Text(String text) {
        instructionPanel.txtRAW.setText(instructionPanel.txtMnemonic2.getText());
        instructionPanel.txtMnemonic2.setText(instructionPanel.txtMnemonic1.getText());
        instructionPanel.txtMnemonic1.setText(text);
    }

    public void setNmemonic2Text(String text) {
        instructionPanel.txtMnemonic2.setText(text);
    }

    public void setNmemonic3Text(String text) {
        instructionPanel.txtRAW.setText(text);
    }

    public void setStepActionListener(ActionListener ac) {
        optionsPanel.step.addActionListener(ac);
    }

    public void setPlayActionListener(ActionListener ac) {
        optionsPanel.playPause.addActionListener(ac);
    }

    public void setStopActionListener(ActionListener ac) {
        optionsPanel.stop.addActionListener(ac);
    }

    public void setRestartActionListener(ActionListener ac) {
        optionsPanel.restart.addActionListener(ac);
    }

    public boolean getStepCheckBox() {
        return optionsPanel.singleStep.isSelected();
    }

    @Override
    public void Updated(CPUState cpuState) {
        // registers
        registerPanel.a.setText(Utils.nString.hexToString8(cpuState.A));
        registerPanel.b.setText(Utils.nString.hexToString8(cpuState.B));
        registerPanel.c.setText(Utils.nString.hexToString8(cpuState.C));
        registerPanel.d.setText(Utils.nString.hexToString8(cpuState.D));
        registerPanel.e.setText(Utils.nString.hexToString8(cpuState.E));
        registerPanel.h.setText(Utils.nString.hexToString8(cpuState.H));
        registerPanel.l.setText(Utils.nString.hexToString8(cpuState.L));

        setNmemonic1Text(cpuState.instruction);
        instructionPanel.txtPC.setText(Utils.nString.hexToString16(cpuState.PC));
        //instructionPanel.txtRAW.setText(cpuState.getRAW3Byte());
        // flagPanel
        flagsPanel.z.setText(cpuState.zero ? "true" : "false");
        flagsPanel.s.setText(cpuState.sign ? "true" : "false");
        flagsPanel.c.setText(cpuState.carry ? "true" : "false");
        flagsPanel.ac.setText(cpuState.auxcarry ? "true" : "false");
        flagsPanel.p.setText(cpuState.parity ? "true" : "false");
    }

    private class OptionsPanel extends JPanel {
        public JButton playPause = new JButton("Play/Pause");
        public JButton stop = new JButton("Stop");
        public JButton restart = new JButton("Restart");
        public JButton step = new JButton("Step");
        public JCheckBox singleStep = new JCheckBox("Single Step");

        public OptionsPanel() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 0, 0));
            JPanel child = new JPanel();
            child.setLayout(new GridLayout(3,2));

            setBorder(BorderFactory.createTitledBorder("Options"));

            child.add(playPause);
            child.add(stop);
            child.add(step);
            child.add(restart);
            child.add(singleStep);
            add(child);
        }
    }

    private class RegisterPanel extends JPanel {
        public JTextField a = new JTextField();
        public JTextField b = new JTextField();
        public JTextField c = new JTextField();
        public JTextField d = new JTextField();
        public JTextField e = new JTextField();
        public JTextField h = new JTextField();
        public JTextField l = new JTextField();

        public RegisterPanel() {
            setLayout(new FlowLayout());
            setBorder(BorderFactory.createTitledBorder("Registers"));

            JPanel child = new JPanel();
            child.setLayout(new GridLayout(4, 2));

            a.setEnabled(false);
            b.setEnabled(false);
            c.setEnabled(false);
            d.setEnabled(false);
            e.setEnabled(false);
            h.setEnabled(false);
            l.setEnabled(false);

            // Set size of text boxes
            Dimension labelDim = new Dimension(55, 10);
            a.setPreferredSize(labelDim);
            b.setPreferredSize(labelDim);
            c.setPreferredSize(labelDim);
            d.setPreferredSize(labelDim);
            e.setPreferredSize(labelDim);
            h.setPreferredSize(labelDim);
            l.setPreferredSize(labelDim);

            // A & E on same line
            JPanel aePanel = new JPanel();
            aePanel.setLayout(new GridLayout(1,2));
            aePanel.add(new JLabel("A:"));
            aePanel.add(a);
            aePanel.add(new JLabel("  E:"));
            aePanel.add(e);
            child.add(aePanel);

            // B & H on same line
            JPanel bhPanel = new JPanel();
            bhPanel.setLayout(new GridLayout(1, 2));
            bhPanel.add(new JLabel("B:"));
            bhPanel.add(b);
            bhPanel.add(new JLabel("  H:"));
            bhPanel.add(h);
            child.add(bhPanel);

            // C & L on the same line
            JPanel clPanel = new JPanel();
            clPanel.setLayout(new GridLayout(1,2));
            clPanel.add(new JLabel("C:"));
            clPanel.add(c);
            clPanel.add(new JLabel("  L:"));
            clPanel.add(l);
            child.add(clPanel);

            // D by itself
            JPanel dPanel = new JPanel();
            dPanel.setLayout(new GridLayout(1,2));
            JLabel dLabel = new JLabel("D:");
            dPanel.add(dLabel);
            dPanel.add(d);
            dPanel.add(new JLabel());
            JTextField blankTextField = new JTextField();
            dPanel.add(blankTextField);
            blankTextField.setVisible(false);
            child.add(dPanel);
            add(child);
        }
    }

    private class FlagsPanel extends JPanel {
        public JTextField z = new JTextField();
        public JTextField s = new JTextField();
        public JTextField p = new JTextField();
        public JTextField c = new JTextField();
        public JTextField ac = new JTextField();

        public FlagsPanel() {
            setLayout(new FlowLayout());
            JPanel child = new JPanel();
            child.setLayout(new GridLayout(4, 1));

            setBorder(BorderFactory.createTitledBorder("Flags"));
            z.setEnabled(false);
            s.setEnabled(false);
            p.setEnabled(false);
            c.setEnabled(false);
            ac.setEnabled(false);

            Dimension labelDim = new Dimension(55, 10);
            z.setPreferredSize(labelDim);
            s.setPreferredSize(labelDim);
            p.setPreferredSize(labelDim);
            c.setPreferredSize(labelDim);
            ac.setPreferredSize(labelDim);

            // Z & AC on same line
            JPanel zacPanel = new JPanel();
            zacPanel.setLayout(new GridLayout(1,2));
            zacPanel.add(new JLabel("Z:"));
            zacPanel.add(z);
            zacPanel.add(new JLabel("  AC:"));
            zacPanel.add(ac);
            child.add(zacPanel);

            // S panel
            JPanel sPanel = new JPanel();
            sPanel.setLayout(new GridLayout(1, 2));
            sPanel.add(new JLabel("S:"));
            sPanel.add(s);
            JLabel blank1Label = new JLabel("X:");
            blank1Label.setVisible(false);
            sPanel.add(blank1Label);
            JTextField blank1Text = new JTextField();
            blank1Text.setVisible(false);
            sPanel.add(blank1Text);
            child.add(sPanel);

            // P panel
            JPanel pPanel = new JPanel();
            pPanel.setLayout(new GridLayout(1,2));
            pPanel.add(new JLabel("P:"));
            pPanel.add(p);
            JLabel blank2Label = new JLabel();
            blank2Label.setVisible(false);
            pPanel.add(blank2Label);
            JTextField blank2Text = new JTextField();
            blank2Text.setVisible(false);
            pPanel.add(blank2Text);
            child.add(pPanel);

            // C panel
            JPanel cPanel = new JPanel();
            cPanel.setLayout(new GridLayout(1,2));
            cPanel.add(new JLabel("C:"));
            cPanel.add(c);
            JLabel blank3Label = new JLabel();
            blank3Label.setVisible(false);
            cPanel.add(blank3Label);
            JTextField blank3Text = new JTextField();
            blank3Text.setVisible(false);
            cPanel.add(blank3Text);
            child.add(cPanel);
            add(child);
        }
    }

    private class InstructionPanel extends JPanel {
        public JTextField txtMnemonic1 = new JTextField();
        public JTextField txtMnemonic2 = new JTextField();
        public JTextField txtRAW = new JTextField();
        public JTextField txtPC = new JTextField();
        public JList<String> listInstructions = new JList();

        public InstructionPanel() {
            setBorder(BorderFactory.createTitledBorder("PC"));
            setLayout(new FlowLayout());
            JPanel child = new JPanel();

            child.setLayout(new GridLayout(4, 2));

            Dimension textDim = new Dimension(110,10);
            txtMnemonic1.setEnabled(false);
            txtPC.setEnabled(false);
            txtMnemonic1.setPreferredSize(textDim);
            txtPC.setPreferredSize(textDim);

            // Mnemonic label/text
            JPanel mnemonicPanel = new JPanel();
            mnemonicPanel.setLayout(new GridLayout(1, 2));
            mnemonicPanel.add(new JLabel("Current Inst:"));
            mnemonicPanel.add(txtMnemonic1);
            child.add(mnemonicPanel);

            // Next instruction 2
            JPanel pc2Panel = new JPanel();
            pc2Panel.setLayout(new GridLayout(1,2));
            pc2Panel.add(new JLabel("Prev - 1:"));
            pc2Panel.add(txtMnemonic2);
            child.add(pc2Panel);

            // Next instruction 3
            JPanel pc3Panel = new JPanel();
            pc3Panel.setLayout(new GridLayout(1,2));
            pc3Panel.add(new JLabel("Prev - 2:"));
            pc3Panel.add(txtRAW);
            child.add(pc3Panel);

            // PC pointer
            JPanel pcPanel = new JPanel();
            pcPanel.setLayout(new GridLayout(1,2));
            pcPanel.add(new JLabel("PC:"));
            pcPanel.add(txtPC);
            child.add(pcPanel);

            // Next instructions list
//            JPanel listPane = new JPanel();
//            listPane.setLayout(new GridBagLayout());
//
//            listInstructions.setLayoutOrientation(JList.VERTICAL);
//            listInstructions.setVisibleRowCount(-1);
//            String[] list = {"test1", "test2", "test3"};
//            listInstructions.setListData(list);
//
//            JScrollPane listScroller = new JScrollPane(listInstructions,
//                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
//            listScroller.setAlignmentX(LEFT_ALIGNMENT);
//            listScroller.setPreferredSize(new Dimension(100, 50));
//            GridBagConstraints gbc = new GridBagConstraints();
//            gbc.gridx = 0;
//            gbc.gridy = 0;
//            gbc.weightx = 1;
//            gbc.weighty = 1;
//            gbc.fill = GridBagConstraints.BOTH;
//            listPane.add(listScroller, gbc);
//
//            add(listPane);
            add(child);
        }
    }
}
