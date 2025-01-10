package com.paralela.rh;

public class FPSCounter {

    int frames;
    int fps;
    long lastTime;

    public FPSCounter() {
        frames = 0;
        fps = 0;
        lastTime = System.currentTimeMillis();
    }

    public void update() {
        frames++;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastTime >= 1000) { // 1 second
            fps = frames;
            frames = 0;
            lastTime = currentTime;

            System.out.println("FPS: " + getFps());
        }
    }

    public int getFps() {
        return fps;
    }
}
