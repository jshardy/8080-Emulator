package GUI;

import Core.CPU;
import Core.CPUStateTest;

public class Timing implements Runnable {
    boolean running = false;
    CPU cpu;
    VideoArea videoArea;
    double fps = 59.54;
    Thread thread;
    //CPUStateTest test;
    final int vblankHalfCycles = 32768;

    public Timing(CPU cpuUsed, VideoArea videoAreaUsed) {
        cpu = cpuUsed;
        //test = new CPUStateTest(cpu);
        videoArea = videoAreaUsed;
    }

    @Override
    public void run() {
        long timeNext = System.nanoTime();
        long timeFrame = (long) (1000000000.0 / fps);

        while(running) {
            do {
                update();
                timeNext += timeFrame;
            } while(System.nanoTime() - timeNext >= timeFrame);

            videoArea.paintImmediately(videoArea.getVisibleRect());
            long sleepTime = (timeNext - System.nanoTime()) / 1000000;
            if(sleepTime > 0) {
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.out.println("Spurious wakeup");
                }
            }
        }
    }

    public void update() {
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

    public synchronized void pause() {
        running = false;
        thread = null;
    }

    public synchronized void resume() {
        start();
    }

    public void stop() {
        running = false;
        thread = null;
    }
}
