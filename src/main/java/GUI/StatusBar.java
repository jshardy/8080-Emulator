package GUI;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;

public class StatusBar extends JPanel {
    int size = 10;
    JLabel[] labels = new JLabel[size];

    public StatusBar() {
        setBorder(new BevelBorder(BevelBorder.LOWERED));
        setPreferredSize(new Dimension(new Dimension(getWidth(), 16)));
        //setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    }

    public void setLabelText(int index, String text) {
        if (index > 0 && index < size) {
            labels[index].setText(text);
        }
    }

    public void setVisible(int index, boolean hideORnot) {
        if(index > 0 && index < size) {
            labels[index].setVisible(hideORnot);
        }
    }
}
