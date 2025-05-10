package net.bumblebee.claysoldiers.entity.horse;

import net.bumblebee.claysoldiers.entity.variant.ClayHorseVariants;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class ClayPegasusEntity extends AbstractClayHorse {

    public ClayPegasusEntity(EntityType<? extends ClayPegasusEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.moveControl = new FlyingMoveControl(this, 30, false);
    }

    public static AttributeSupplier createPegasusAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.JUMP_STRENGTH)
                .add(Attributes.MAX_HEALTH, 25.0)
                .add(Attributes.FLYING_SPEED, 0.3f)
                .add(Attributes.MOVEMENT_SPEED, 0.225F).build();
    }

    @Override
    public float getWalkTargetValue(BlockPos pPos, LevelReader pLevel) {
        return pLevel.getBlockState(pPos).isAir() ? 10.0F : 0.0F;
    }

    @Override
    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }

    @Override
    protected PathNavigation createNavigation(Level pLevel) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, pLevel);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return ClayHorseVariants.clayPegasusByVariant(this.getVariant()).get().getDefaultInstance();
    }

    @Override
    protected boolean canFlyToOwner() {
        if (getVehicle() != null) {
            return super.canFlyToOwner();
        }
        return true;
    }
}
