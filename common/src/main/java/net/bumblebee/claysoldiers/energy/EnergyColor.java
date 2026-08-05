package net.bumblebee.claysoldiers.energy;

public enum EnergyColor {
    GREEN(0xFF48FFA9),
    ORANGE(0xFFFF7D00),
    BLUE(0xFF00EFFF);

    private final int color;

    EnergyColor(int color) {
        this.color = color;
    }

    public int getColor() {
        return color;
    }
}
