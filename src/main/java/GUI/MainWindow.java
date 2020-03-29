package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

import Core.*;
import Main.SettingsFile;

public class MainWindow extends JFrame {
    JFrame mainWindow = this;
    VideoArea videoArea;
    DebugArea debugArea = new DebugArea();
    MenuActionListener mnuAction = new MenuActionListener();
    //DebugWindow debugWindow = new DebugWindow();
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, videoArea, debugArea);

    CPU cpu;
    Memory memory;
    SpaceInvadersIO io = new SpaceInvadersIO();
    Timing cpuManager;
    CPUThreadMonitor cpuThreadMonitor;

    private byte[] memoryByteArray;

    public MainWindow() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //setSize(800, 500);
        setSize(403, 224 * 2);
        setLocation(300,200);

        MenuBar menuBar = new MenuBar(mnuAction);
        setJMenuBar(menuBar);

        splitPane.setResizeWeight(1);
        splitPane.setVisible(true);

        debugArea.setStepActionListener(actionEvent -> {
            cpu.stepExecute();
            debugArea.Updated(cpu.previousState);
            videoArea.paintImmediately(videoArea.getVisibleRect());
            mainWindow.requestFocus();
        });

        debugArea.setPlayActionListener(actionEvent -> {
            if(debugArea.getStepCheckBox()) {
                cpuManager.stop();
                cpu.stepExecute();
                debugArea.Updated(cpu.previousState);
                videoArea.paintImmediately(videoArea.getVisibleRect());
            } else {
                cpuThreadMonitor.start();
                cpuManager.start();
            }
            mainWindow.requestFocus();
        });

        debugArea.setStopActionListener(actionEvent -> {
            cpuManager.stop();
            mainWindow.requestFocus();
        });

        debugArea.setRestartActionListener(actionEvent -> {
            cpuManager.stop();
            memory = new SpaceInvadersMemory(memoryByteArray);
            io = new SpaceInvadersIO();
            cpu = new CPU(memory, io);
            videoArea.setVideoMemory(memory);
            cpuManager = new Timing(cpu, videoArea);
            cpuThreadMonitor = new CPUThreadMonitor(cpu, debugArea);

            cpuManager.start();
        });
        /*
        JFileChooser jf = new JFileChooser();
        int retVal = jf.showOpenDialog(MainWindow.this);
        String filename = jf.getSelectedFile().toString();
        */
        memoryByteArray = SettingsFile.LoadROM("./src/roms/space_invaders.rom");
        //memoryByteArray = SettingsFile.LoadROM(filename);
        memory = new SpaceInvadersMemory(memoryByteArray);
        cpu = new CPU(memory, io);
        videoArea = new VideoArea(cpu.getMemory());

        cpuThreadMonitor = new CPUThreadMonitor(cpu, debugArea);

        add(videoArea);
        setVisible(true);

        cpuManager = new Timing(cpu, videoArea);
        this.addKeyListener(new keyInput());
        this.addFocusListener(new FocusInput());
        mnuAction.toggleDebugBarVisible();
    }

    public void loadRom(String filename) {
        memoryByteArray = SettingsFile.LoadROM(filename);
        //memory = new MemoryDefault(memoryByteArray);
        memory = new SpaceInvadersMemory(memoryByteArray);
        io = new SpaceInvadersIO();
        cpu = new CPU(memory, io);
        cpuThreadMonitor = new CPUThreadMonitor(cpu, debugArea);
        videoArea = new VideoArea(cpu.getMemory());
        cpu.setCPUChanged(debugArea);
        cpuManager = new Timing(cpu, videoArea);
    }

    public class keyInput implements KeyListener {
        Boolean started = false;
        @Override
        public void keyTyped(KeyEvent keyEvent) {
            // Not using, but must be here
        }

        @Override
        public void keyPressed(KeyEvent keyEvent) {
            //System.out.println(keyEvent.getKeyChar());
            switch(keyEvent.getKeyCode()) {
                case KeyEvent.VK_SPACE:
                    io.getPort1().setBit(4, true);
                    break;
                case KeyEvent.VK_LEFT:
                    io.getPort1().setBit(5, true);
                    break;
                case KeyEvent.VK_RIGHT:
                    io.getPort1().setBit(6, true);
                    break;
                default:
                    // There are two starts in Space Invaders
                    // I've set them up to be "any" key
                    if(started) {
                        io.getPort1().setBit(2, true);
                    }

                    if(!started) {
                        io.getPort1().setPort(0x1);
                        io.getPort2().setPort(0x0);
                        started = true;
                    }
                    break;
            }
        }

        @Override
        public void keyReleased(KeyEvent keyEvent) {
            switch(keyEvent.getKeyCode()) {
                case KeyEvent.VK_SPACE:
                    io.getPort1().setBit(4, false);
                    break;
                case KeyEvent.VK_LEFT:
                    io.getPort1().setBit(5, false);
                    break;
                case KeyEvent.VK_RIGHT:
                    io.getPort1().setBit(6, false);
                    break;
            }
        }
    }

    public class FocusInput implements FocusListener {
        @Override
        public void focusGained(FocusEvent focusEvent) {
            // has to be here, but I'm not using it.
        }

        @Override
        public void focusLost(FocusEvent focusEvent) {
            // Don't lose focus, then we lose keyboard input
            mainWindow.requestFocus();
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
                    System.exit(0);
                    break;
                case "Rom...":
                    JFileChooser jf = new JFileChooser();
                    int retVal = jf.showOpenDialog(MainWindow.this);
                    String filename = jf.getSelectedFile().toString();
                    loadRom(filename);
                    break;
                case "Disk...":

                    break;
                case "Play/Pause":
                    if(debugArea.getStepCheckBox()) {
                        cpuManager.stop();
                        cpu.stepExecute();
                        debugArea.Updated(cpu.previousState);
                        videoArea.paintImmediately(videoArea.getVisibleRect());
                    } else {
                        cpuManager.start();
                        cpuThreadMonitor.start();
                    }
                    break;
                case "Stop":
                    cpuManager.stop();
                    break;
                case "Restart":
                    cpuManager.stop();
                    memory = new SpaceInvadersMemory(memoryByteArray);
                    io = new SpaceInvadersIO();
                    cpu = new CPU(memory, io);
                    videoArea.setVideoMemory(memory);
                    cpuManager = new Timing(cpu, videoArea);
                    cpuManager.start();
                    break;
                case "Debug Bar":
                    toggleDebugBarVisible();
                    break;
                default:
                    System.out.println("Uncaught: " + actionEvent.getActionCommand());
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
                Dimension d = mainWindow.getSize();
                d.width -= 135;
                mainWindow.setSize(d);
            } else {
                previous_size = temp_prev_size;
                mainWindow.add(videoArea);
            }

            mainWindow.repaint();
            mainWindow.requestFocus();
            debugVisible = !debugVisible;
        }
    }
}
