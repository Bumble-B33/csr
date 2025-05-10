package net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack;

public enum SpecialEffectCategory {
    BENEFICIAL,
    HARMFUL,
    BOTH;

    public boolean isHarmful() {
        return this != BENEFICIAL;
    }
    public boolean isSupportive() {
        return this != HARMFUL;
    }
}
