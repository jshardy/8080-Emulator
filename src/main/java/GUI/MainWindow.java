package GUI;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import Core.CPU;
import Core.Video;
import Main.SettingsFile;

public class MainWindow extends JFrame {
    JFrame mainWindow = this;
    VideoArea videoArea;
    //Video video;

    boolean running = false;

    DebugArea debugArea = new DebugArea();
    MenuBar menuBar = new MenuBar(new MenuActionListener());
    DebugWindow debugWindow = new DebugWindow();
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, videoArea, debugArea);

    CPU cpu;
    Timing cpu_manager;
    boolean started = false;

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

        splitPane.setResizeWeight(1);
        splitPane.setVisible(true);

        debugArea.setStepActionListener(actionEvent -> {
            cpu.stepExecute();
            videoArea.repaint();
        });

        debugArea.setPlayActionListener(actionEvent -> {
            System.out.println(cpu_manager.running());
            cpu_manager.start();
        });

        debugArea.setStopActionListener(actionEvent -> {
            cpu_manager.stop();
        });

        byte[] memory = SettingsFile.LoadROM("./src/roms/space_invaders.rom");
        cpu = new CPU(memory);
        //video = new Video(cpu.getMemory());

        videoArea = new VideoArea(cpu.getMemory());

        add(videoArea);
        setVisible(true);

        cpu.addUpdateCallback(debugArea);
        cpu.addUpdateCallback(debugWindow);

        cpu_manager = new Timing(cpu, videoArea);
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
