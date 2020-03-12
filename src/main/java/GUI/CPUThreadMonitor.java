package GUI;

import Core.CPU;

public class CPUThreadMonitor implements Runnable {
    CPU cpu;
    DebugArea debugArea;
    Thread thread;
    boolean running = false;
    int count = 0;

    public CPUThreadMonitor(CPU c, DebugArea d) {
        cpu = c;
        debugArea = d;
    }

    @Override
    public void run() {
        while(running) {
            try {
                cpu.cpuBusy.acquire();
                debugArea.Updated(cpu.previousState);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void start() {
        running = !running;
        if(running) {
            thread = new Thread(this, "CPU Thread Monitor");
            thread.start();
        }
    }
}
