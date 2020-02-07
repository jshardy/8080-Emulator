package GUI;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Core.CPU;
import Main.SettingsFile;

public class MainWindow extends JFrame {
    JFrame mainWindow = this;
    VideoArea videoArea = new VideoArea();
    DebugArea debugArea = new DebugArea();
    MenuBar menuBar = new MenuBar(new MenuActionListener());
    DebugWindow debugWindow = new DebugWindow();
    JSplitPane splitPane = new JSplitPane();

    CPU cpu;
    SettingsFile settingsFile = new SettingsFile();

    public MainWindow() {
        /*
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch(Exception e) {
            System.out.println("Unable to set Look and Feel to default UI for OS.");
        }
         */
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setSize(800, 500);
        setSize(256 * 2, 224 * 2);
        setLocation(300,200);

        setJMenuBar(menuBar);

        splitPane = new JSplitPane(SwingConstants.VERTICAL, videoArea, debugArea);
        splitPane.setResizeWeight(1);
        splitPane.setVisible(true);

        debugArea.setStepActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                int cycle = cpu.stepExecute();
            }
        });

        add(videoArea);
        setVisible(true);

        byte[] memory = settingsFile.LoadROM("./src/roms/space_invaders.rom");
        cpu = new CPU(memory);

        cpu.addUpdateCallback(debugArea);
        cpu.addUpdateCallback(debugWindow);
    }

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

    public class VideoArea extends JPanel {

        @Override
        public void paint(Graphics g) {
            super.paint(g);
            g.drawString("VideoArea", 15, 15);
            System.out.println("paint() - VideoArea");
        }
    }

    class MenuActionListener implements ActionListener {
        private boolean debugVisible = false;
        private Dimension previous_size = new Dimension(800,500);

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            switch(actionEvent.getActionCommand()) {
                case "Create VM":
                    break;
                case "Open VM":
                    break;
                case "Save VM":
                    break;
                case "Save VM As":
                    break;
                case "Exit":
                    break;
                case "ROM...":
                    break;
                case "Disk...":
                    break;
                case "Play/Pause":
                    break;
                case "Stop":
                    break;
                case "Restart":
                    break;
                case "Debug Bar":
                    toggleDebugBarVisible();
                    break;
            }
        }

        public void toggleDebugBarVisible() {
            Dimension temp_prev_size = mainWindow.getSize();

            mainWindow.setSize(previous_size);
            mainWindow.getContentPane().removeAll();

            if(!debugVisible) {
                previous_size = temp_prev_size;
                mainWindow.add(splitPane);
                splitPane.add(videoArea, 0);
            }
            else if(debugVisible)
            {
                previous_size = temp_prev_size;
                mainWindow.add(videoArea);
            }
            
            mainWindow.repaint();
            debugVisible = !debugVisible;
        }
    }
}
