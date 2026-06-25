package org.moChiThirst.data;

public class PlayerData {
    private int thirstLevel;
    private double time;

    public PlayerData(int thirstLevel, double time) {
        this.thirstLevel = thirstLevel;
        this.time = time;
    }

    // Getters và Setters
    public int getThirstLevel() { return thirstLevel; }
    public void setThirstLevel(int thirstLevel) { this.thirstLevel = thirstLevel; }

    public double getTime() { return time; }
    public void setTime(double time) { this.time = time; }
}
