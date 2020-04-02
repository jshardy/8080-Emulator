package Core.GUI;

import javax.swing.*;
import java.awt.event.ActionListener;

public class MenuBar extends JMenuBar {
    public MenuBar(ActionListener newAction) {
        // Preferences
        JMenu preferences = new JMenu("Virtual Machine");
        JMenuItem loadGame = new JMenuItem("Load Game...");
        loadGame.addActionListener(newAction);
        preferences.add(loadGame);

        JMenuItem disk = new JMenuItem("Save Game...");
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

        JMenuItem exitVM = new JMenuItem("Exit");
        exitVM.addActionListener(newAction);
        preferences.addSeparator();
        preferences.add(exitVM);

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
