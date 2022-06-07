package utils;

import static org.lwjgl.glfw.GLFW.glfwGetTime;

public class Time {
    private float deltaTime;
    private float lastFrame;
    private float totalTime;

    public Time() {
        deltaTime = 0;
        lastFrame = 0;
        totalTime = 0;
    }
    public void syncTime() {
        final float currentFrame = (float)glfwGetTime();
        deltaTime = currentFrame - lastFrame;
        lastFrame = currentFrame;
        totalTime += deltaTime;
    }

    public float getDeltaTime() {
        return deltaTime;
    }

    public void setDeltaTime(float deltaTime) {
        this.deltaTime = deltaTime;
    }

    public float getLastFrame() {
        return lastFrame;
    }

    public void setLastFrame(float lastFrame) {
        this.lastFrame = lastFrame;
    }

    public float getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(float totalTime) {
        this.totalTime = totalTime;
    }
}
