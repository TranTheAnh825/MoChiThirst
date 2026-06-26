package org.moChiThirst.data;

public class PlayerData {
    private int thirstLevel;
    private long lastUpdated;

    public PlayerData(int thirstLevel, long lastUpdated) {
        this.thirstLevel = thirstLevel;
        this.lastUpdated = lastUpdated;
    }

    public int getThirstLevel() { return thirstLevel; }
    public void setThirstLevel(int thirstLevel) { this.thirstLevel = thirstLevel; }

    public long getLastUpdated() { return lastUpdated; }
    public void setLastUpdated(long lastUpdated) { this.lastUpdated = lastUpdated; }
}
