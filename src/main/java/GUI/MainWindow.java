package GUI;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

import Core.*;
import Main.SerializeState;
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
    private KeyboardFocusManager kbManager;
    private boolean fromSavedGame = false;
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
            requestFocusInWindow();
        });

        debugArea.setPlayActionListener(actionEvent -> {
            if(debugArea.getStepCheckBox()) {
                cpuManager.stop();
                cpuThreadMonitor.stop();
                cpu.stepExecute();
                debugArea.Updated(cpu.previousState);
                videoArea.paintImmediately(videoArea.getVisibleRect());
            } else {
                cpuThreadMonitor.start();
                cpuManager.start();
            }
            requestFocusInWindow();
        });

        debugArea.setStopActionListener(actionEvent -> {
            cpuManager.stop();
            cpuThreadMonitor.stop();
            requestFocusInWindow();
        });

        debugArea.setRestartActionListener(actionEvent -> {
            cpuManager.stop();
            cpuThreadMonitor.stop();
            memory = new SpaceInvadersMemory(memoryByteArray);
            io = new SpaceInvadersIO();
            cpu = new CPU(memory, io);
            videoArea.setVideoMemory(memory);
            cpuManager = new Timing(cpu, videoArea);
            cpuThreadMonitor = new CPUThreadMonitor(cpu, debugArea);
            cpuThreadMonitor.start();
            cpuManager.start();
            requestFocusInWindow();
        });

        memoryByteArray = SettingsFile.loadROM("./src/roms/space_invaders.rom");
        memory = new SpaceInvadersMemory(memoryByteArray);
        cpu = new CPU(memory, io);
        videoArea = new VideoArea(cpu.getMemory());

        cpuThreadMonitor = new CPUThreadMonitor(cpu, debugArea);

        add(videoArea);
        setVisible(true);

        cpuManager = new Timing(cpu, videoArea);
        //this.addKeyListener(new keyInput());
        //this.addFocusListener(new FocusInput());

        kbManager = KeyboardFocusManager.getCurrentKeyboardFocusManager();
        kbManager.addKeyEventDispatcher(new keyBoardHook());
        this.setFocusable(true);
        mnuAction.toggleDebugBarVisible();
    }

    public void loadRom(String filename) {
        memoryByteArray = SettingsFile.loadROM(filename);
        //memory = new MemoryDefault(memoryByteArray);
        memory = new SpaceInvadersMemory(memoryByteArray);
        io = new SpaceInvadersIO();
        cpu = new CPU(memory, io);
        cpuThreadMonitor = new CPUThreadMonitor(cpu, debugArea);
        videoArea = new VideoArea(cpu.getMemory());
        cpuManager = new Timing(cpu, videoArea);
    }

    public class keyBoardHook implements KeyEventDispatcher {
        boolean started = false; // Coin inserted and p1 playing

        @Override
        public boolean dispatchKeyEvent(KeyEvent keyEvent) {
            if(keyEvent.getID() == KeyEvent.KEY_PRESSED) {
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
                    case KeyEvent.VK_C:
                        io.getPort1().setBit(0, true);
                        break;
                    case KeyEvent.VK_L:
                        // Add lives via changing variable.
                        int lives = memory.readByte(0x21ff);
                        memory.writeByte(0x21ff, (++lives) & 0xff); // add lives
                        break;
                    case KeyEvent.VK_ESCAPE:
                        memory.writeByte(0x20f1, 1);
                        memory.writeByte(0x20f2, 20);
                        memory.writeByte(0x20f3, 20);
                    default:
                        // There are two starts in Space Invaders - insert quarter, and press p1 start
                        // I've set them up to be "any" key

                        if(!fromSavedGame) {
                            if (started) {
                                io.getPort1().setBit(2, true);
                            }

                            if (!started) {
                                io.getPort1().setPort(0x1);
                                io.getPort2().setPort(0x0);
                                started = true;
                            }
                        } else {
                            started = true;
                        }
                        break;
                }
                return true;
            } else if(keyEvent.getID() == KeyEvent.KEY_RELEASED) {
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
                    case KeyEvent.VK_C:
                        io.getPort1().setBit(0, false);
                        break;
                }
                return true;
            }
            return false;
        }
    }

    class MenuActionListener implements ActionListener {
        private boolean debugVisible = false;
        private Dimension previous_size = new Dimension(800,500);

        @Override
        public void actionPerformed(ActionEvent actionEvent) {
            String filename;
            int retVal;
            JFileChooser jf = new JFileChooser();

            switch(actionEvent.getActionCommand()) {
                case "Load Game...":
                    cpuManager.stop();
                    cpuThreadMonitor.stop();
                    fromSavedGame = true;

                    retVal = jf.showOpenDialog(MainWindow.this);
                    if(retVal == JFileChooser.CANCEL_OPTION) {
                        fromSavedGame = false;
                        return;
                    }

                    filename = jf.getSelectedFile().toString();

                    // Get just the name, remove the dot extension
                    int index = filename.lastIndexOf('.');
                    filename = filename.substring(0, index);

                    cpu = SerializeState.loadCPU(filename + ".cpu");
                    //io = SerializeState.loadIO(filename + ".io");
                    //memory = SerializeState.loadMemory(filename + ".mem");
                    io = (SpaceInvadersIO) cpu.getInputOutput();
                    memory = (SpaceInvadersMemory) cpu.getMemory();

                    //cpu.setInputOutput(io);
                    //cpu.setMemory(memory);
                    videoArea.setVideoMemory(memory);
                    cpuManager = new Timing(cpu, videoArea);
                    cpuThreadMonitor = new CPUThreadMonitor(cpu, debugArea);
                    debugArea.Updated(cpu.previousState);
                    /*
                    try {
                        memory = new SpaceInvadersMemory(SettingsFile.loadMemory(new FileInputStream(new File(filename + ".mem"))));
                        io = SettingsFile.loadIO(new FileInputStream(new File(filename + ".io")));
                        cpu = SettingsFile.loadCPU(new FileInputStream(new File(filename + ".reg")), memory, io);
                        videoArea.setVideoMemory(memory);
                        cpuManager = new Timing(cpu, videoArea);
                        cpuThreadMonitor = new CPUThreadMonitor(cpu, debugArea);
                        debugArea.Updated(cpu.previousState);
                        //cpuThreadMonitor.start();
                        //cpuManager.start();
                    } catch (FileNotFoundException e) {
                        System.out.println("File not found - mnuAction");
                    }
                    */
                    break;
                case "Save Game...":
                    cpuManager.stop();
                    cpuThreadMonitor.stop();

                    retVal = jf.showSaveDialog(MainWindow.this);
                    if(retVal == JFileChooser.CANCEL_OPTION) {
                        return;
                    }
                    filename = jf.getSelectedFile().toString();

                    SerializeState.saveCPU(filename + ".cpu", cpu);
                    //SerializeState.saveMemory(filename + ".mem", memory);
                    //SerializeState.saveIO(filename + ".io", io);

                    //SettingsFile.saveState(filename, memory, cpu, io);
                    break;
                case "Exit":
                    System.exit(0);
                    break;
                case "Rom...":
                    fromSavedGame = false;
                    retVal = jf.showOpenDialog(MainWindow.this);
                    filename = jf.getSelectedFile().toString();
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
