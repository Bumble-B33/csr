package net.bumblebee.claysoldiers.entity.goal.horse;

import net.bumblebee.claysoldiers.entity.horse.AbstractClayHorse;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.goal.Goal;

public class ClayHorseRandomStandGoal extends Goal {
    private final AbstractClayHorse horse;
    private int nextStand;

    public ClayHorseRandomStandGoal(AbstractClayHorse pHorse) {
        this.horse = pHorse;
        this.resetStandInterval(pHorse);
    }

    @Override
    public void start() {
        this.horse.standIfPossible();
        this.playStandSound();
    }

    private void playStandSound() {
        SoundEvent soundevent = this.horse.getAmbientStandSound();
        if (soundevent != null) {
            this.horse.playSound(soundevent);
        }
    }

    public boolean canContinueToUse() {
        return false;
    }

    @Override
    public boolean canUse() {
        if (!horse.canPerformRearing()) {
            return false;
        }

        ++this.nextStand;
        if (this.nextStand > 0 && this.horse.getRandom().nextInt(1000) < this.nextStand) {
            this.resetStandInterval(this.horse);
            return !this.horse.isImmobile() && this.horse.getRandom().nextInt(10) == 0;
        } else {
            return false;
        }
    }

    private void resetStandInterval(AbstractClayHorse pHorse) {
        this.nextStand = -pHorse.getAmbientStandInterval();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }
}
