package src.SmartWarehouse;

import java.util.Random;

public class WeightSensor {
    private boolean isHealty = true;
    private double reading = 0;
    private Random random = new Random();
    private Packet[] storageSpace;
    
    public WeightSensor(boolean isHealty, Packet[] storageSpace) {
        this.isHealty = isHealty;
        this.storageSpace = storageSpace;
    }

    public boolean isHealty() {
        return isHealty;
    }
    public void setIsHealty(boolean isHealty) {
        this.isHealty = isHealty;
    }

    public double getReading() {
        if (isHealty)
            reading = 0;
            if (storageSpace[0] != null)
                reading = storageSpace[0].weight() + random.nextGaussian() * 0.1;
        return reading;
    }
}
