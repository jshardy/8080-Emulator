package GUI;

import Core.CPU;

public class Timing implements Runnable {
    boolean running = false;
    CPU cpu;
    VideoArea videoArea;
    double fps = 59.54;
    Thread thread;

    public Timing(CPU cpuUsed, VideoArea videoAreaUsed) {
        cpu = cpuUsed;
        videoArea = videoAreaUsed;
    }

    @Override
    public void run() {
        long timeNext = System.nanoTime();
        long timeFrame = (long) (100000000000.0 / fps);

        while(running) {
            do {
                System.out.println("timeNext:" + timeNext + " " + System.nanoTime());
                update();
                timeNext += timeFrame;
            } while(System.nanoTime() >= timeNext);

            System.out.println("Thread - videoArea.repaint() - called");
            videoArea.repaint();
            long sleepTime = (timeNext - System.nanoTime()) / 1000000;
            if(sleepTime > 0) {
                try {
                    System.out.println("Sleeping for " + sleepTime + " ns");
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    System.out.println("Un-Paused");
                }
            }
        }
    }

    public void update() {
        int cycles = 0;
        //16768
        while(cycles < 16768) {
            cycles += cpu.stepExecute();
        }
        cycles = 0;
        // start vblank
        cpu.interrupt(0xcf);
        while(cycles < 16768) {
            cycles += cpu.stepExecute();
        }
        // end vblank
        cpu.interrupt(0xd7);
    }

    public void start() {
        running = !running;

        if(thread == null && running) {
            thread = new Thread(this, "CPU Manager");
            thread.start();
        } else if (thread != null && running) {
            thread.start();
        }
    }

    public boolean running() {
        return running;
    }

    public synchronized void pause() throws InterruptedException {
        running = false;
    }

    public synchronized void resume() {
        running = true;
        notify();
    }

    public void stop() {
        running = false;
        thread = null;
    }
}
