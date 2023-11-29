package src.SmartWarehouse;

public class Radar {
    private boolean isHealty = true;
    private boolean reading;

    public Radar(boolean isHealty) {
        this.isHealty = isHealty;
    }

    public boolean isHealty() {
        return isHealty;
    }

    public void setIsHealty(boolean isHealty) {
        this.isHealty = isHealty;
    }

    public boolean hasFreeSpaceScan(Object[] space) {
        boolean available_space = false;
        for (Object object : space) {
            if (object == null)
                available_space = true;
        }
        if (isHealty)
            reading = available_space;
        return reading;
    }
}
