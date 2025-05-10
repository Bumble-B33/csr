package net.bumblebee.claysoldiers.entity.soldier;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;

import java.util.function.BooleanSupplier;

public class ClaySoldierMoveControl extends MoveControl {
    private final BooleanSupplier canSwim;
    private final int maxTurnX;
    private final int maxTurnY;
    private final float inWaterSpeedModifier;
    private final float outsideWaterSpeedModifier;
    private final boolean applyGravity;

    public ClaySoldierMoveControl(AbstractClaySoldierEntity mob, BooleanSupplier canSwim, int maxTurnX, int maxTurnY, boolean applyGravity) {
        super(mob);
        this.canSwim = canSwim;
        this.maxTurnX = maxTurnX;
        this.maxTurnY = maxTurnY;
        this.inWaterSpeedModifier = 4f;
        this.outsideWaterSpeedModifier = 1;
        this.applyGravity = applyGravity;
    }

    private boolean shouldUseSwimControl() {
        return mob.isInWater() && canSwim.getAsBoolean();
    }

    @Override
    public void tick() {
        if (shouldUseSwimControl()) {
            waterTick();
        } else {
            super.tick();
        }
    }

    private void waterTick() {
        if (this.applyGravity && this.mob.isInWater()) {
            this.mob.setDeltaMovement(this.mob.getDeltaMovement().add(0.0, 0.005, 0.0));
        }

        if (this.operation == MoveControl.Operation.MOVE_TO && !this.mob.getNavigation().isDone()) {
            double difWantedX = this.wantedX - this.mob.getX();
            double difWantedY = this.wantedY - this.mob.getY();
            double difWantedZ = this.wantedZ - this.mob.getZ();
            double d3 = difWantedX * difWantedX + difWantedY * difWantedY + difWantedZ * difWantedZ;
            if (d3 < 2.5000003E-7F) {
                this.mob.setZza(0.0F);
            } else {
                float f = (float) (Mth.atan2(difWantedZ, difWantedX) * 180.0F / (float) Math.PI) - 90.0F;
                this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f, (float) this.maxTurnY));
                this.mob.yBodyRot = this.mob.getYRot();
                this.mob.yHeadRot = this.mob.getYRot();
                float mobSpeed = (float) (this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED));
                if (this.mob.isInWater()) {
                    this.mob.setSpeed(mobSpeed * this.inWaterSpeedModifier);
                    double flatDistance = Math.sqrt(difWantedX * difWantedX + difWantedZ * difWantedZ);
                    if (Math.abs(difWantedY) > 1.0E-5F || Math.abs(flatDistance) > 1.0E-5F) {
                        float f3 = -((float) (Mth.atan2(difWantedY, flatDistance) * 180.0F / (float) Math.PI));
                        f3 = Mth.clamp(Mth.wrapDegrees(f3), (float) (-this.maxTurnX), (float) this.maxTurnX);
                        this.mob.setXRot(this.rotlerp(this.mob.getXRot(), f3, 5.0F));
                    }

                    float f6 = Mth.cos(this.mob.getXRot() * (float) (Math.PI / 180.0));
                    float f4 = Mth.sin(this.mob.getXRot() * (float) (Math.PI / 180.0));
                    this.mob.zza = f6 * mobSpeed;
                    this.mob.yya = -f4 * mobSpeed;
                } else {
                    float f5 = Math.abs(Mth.wrapDegrees(this.mob.getYRot() - f));
                    float f2 = getTurningSpeedFactor(f5);
                    this.mob.setSpeed(mobSpeed * this.outsideWaterSpeedModifier * f2);
                }
            }
        } else {
            this.mob.setSpeed(0.0F);
            this.mob.setXxa(0.0F);
            this.mob.setYya(0.0F);
            this.mob.setZza(0.0F);
        }
    }


    private static float getTurningSpeedFactor(float p_249853_) {
        return 1.0F - Mth.clamp((p_249853_ - 10.0F) / 50.0F, 0.0F, 1.0F);
    }
}
