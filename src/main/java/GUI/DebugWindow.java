package GUI;

import sun.security.util.Debug;

import javax.swing.*;
import java.awt.*;

public class DebugWindow extends JFrame {
    private JTextArea txtDebug = new JTextArea();
    private JScrollPane areaScrollPane = new JScrollPane(txtDebug);

    public DebugWindow() {
        setLayout(new GridLayout());

        setSize(400,300);
        setLocation(0,1000);

        txtDebug.setLineWrap(true);
        txtDebug.setWrapStyleWord(true);

        add(areaScrollPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);
    }

    public void print(String message) {
        txtDebug.append(message);
        txtDebug.setSelectionStart(txtDebug.getText().length());
        areaScrollPane.getViewport().setViewPosition(new Point(0, this.getHeight() * 3));
    }

    public void printLine(String message) {
        txtDebug.append(message + "\n");
        areaScrollPane.getViewport().setViewPosition(new Point(0, this.getHeight() * 3));
    }
}
