package net.bumblebee.claysoldiers.item.claystaff;

public class ClayStaffRenderState {
    public boolean hideAmmo;
    public float scale;
    public boolean hasDoll;
    public float cubeRotation;

    public ClayStaffRenderState(boolean hideAmmo, float scale, boolean hasDoll, float cubeRotation) {
        this.hideAmmo = hideAmmo;
        this.scale = scale;
        this.hasDoll = hasDoll;
        this.cubeRotation = cubeRotation;
    }
}
