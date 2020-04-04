package GUI;

import Core.CPU;

public class Timing implements Runnable {
    boolean running = false;
    CPU cpu;
    VideoArea videoArea;
    double fps = 60;
    Thread thread;
    //CPUStateTest test;
    final int vblankHalfCycles = 32000; // time between half frames

    public Timing(CPU cpuUsed, VideoArea videoAreaUsed) {
        cpu = cpuUsed;
        //test = new CPUStateTest(cpu);
        videoArea = videoAreaUsed;
    }

    @Override
    public void run() {
        long timeNext = System.currentTimeMillis();
        long timeFrame = (long) (1000 / fps);

        while(running) {
            do {
                executeOneFrame();
                timeNext += timeFrame;
            } while(System.currentTimeMillis() - timeNext >= timeFrame);
            videoArea.draw();
            //videoArea.paintImmediately(videoArea.getVisibleRect());
            //videoArea.repaint(videoArea.getVisibleRect());
            long sleepTime = (timeNext - System.currentTimeMillis()) / 100;
            if(sleepTime > 0) {
                try {
                    System.out.println("SLEEPING");
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.out.println("Spurious wakeup");
                }
            }
        }
    }

    public void executeOneFrame() {
        int cycles = 0;
        while(cycles < vblankHalfCycles) {
            cycles += cpu.stepExecute();
        }

        cycles = 0;
        // start vblank
        cpu.interrupt(0xcf);
        while(cycles < vblankHalfCycles) {
            cycles += cpu.stepExecute();
        }
        // end vblank
        cpu.interrupt(0xd7);
    }

    public void start() {
        running = !running;

        if(running) {
            thread = new Thread(this, "CPU Manager");
            thread.start();
        }
    }

    public boolean running() {
        return running;
    }

    public void stop() {
        running = false;
        thread = null;
    }
}
