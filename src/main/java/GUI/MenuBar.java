package GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MenuBar extends JMenuBar {
    public MenuBar(ActionListener newAction) {
        JMenu virtualMachine = new JMenu("Virtual Machine");
        JMenuItem createVM = new JMenuItem("Create VM");
        createVM.addActionListener(newAction);

        JMenuItem openVM = new JMenuItem("Open VM");
        openVM.addActionListener(newAction);

        JMenuItem saveVM = new JMenuItem("Save VM");
        saveVM.addActionListener(newAction);

        JMenuItem saveAsVM = new JMenuItem("Save VM As");
        saveAsVM.addActionListener(newAction);

        JMenuItem exitVM = new JMenuItem("Exit");
        exitVM.addActionListener(newAction);

        virtualMachine.add(createVM);
        virtualMachine.add(openVM);
        virtualMachine.addSeparator();
        virtualMachine.add(saveVM);
        virtualMachine.add(saveAsVM);
        virtualMachine.addSeparator();
        virtualMachine.add(exitVM);
        add(virtualMachine);

        // Preferences
        JMenu preferences = new JMenu("Preferences");
        JMenuItem rom = new JMenuItem("Rom...");
        rom.addActionListener(newAction);
        preferences.add(rom);

        JMenuItem disk = new JMenuItem("Disk...");
        disk.addActionListener(newAction);
        preferences.add(disk);

        // Preferences->Ram - NOT SET TO ACTION LISTENER
        JMenu ram = new JMenu("RAM");
        JMenuItem ram16k = new JMenuItem("16K");
        ram.add(ram16k);
        preferences.add(ram);

        // Preferences->Speed - NOT SET TO ACTION LISTENER
        JMenu speed = new JMenu("Speed");
        JMenuItem mhz = new JMenuItem("2 Mhz");
        speed.add(mhz);
        preferences.add(speed);
        add(preferences);

        // Run State
        JMenu runState = new JMenu("Run State");
        JMenuItem playPause = new JMenuItem("Play/Pause");
        playPause.addActionListener(newAction);

        JMenuItem stop = new JMenuItem("Stop");
        stop.addActionListener(newAction);

        JMenuItem restart = new JMenuItem("Restart");
        restart.addActionListener(newAction);

        runState.add(playPause);
        runState.add(stop);
        runState.add(restart);
        add(runState);

        JMenu debug = new JMenu("Debug");
        JMenuItem debugBar = new JMenuItem("Debug Bar");
        debugBar.addActionListener(newAction);
        debug.add(debugBar);

        add(debug);
    }
}
