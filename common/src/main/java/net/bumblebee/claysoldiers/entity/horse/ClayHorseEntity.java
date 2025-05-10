package net.bumblebee.claysoldiers.entity.horse;

import net.bumblebee.claysoldiers.entity.variant.ClayHorseVariants;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ClayHorseEntity extends AbstractClayHorse {
    public static final float SCALE = 0.25f;

    public ClayHorseEntity(EntityType<? extends ClayHorseEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    @Override
    public ItemStack getPickResult() {
        return ClayHorseVariants.clayHorseByVariant(this.getVariant()).get().getDefaultInstance();
    }

    public static AttributeSupplier createBaseHorseAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.JUMP_STRENGTH)
                .add(Attributes.MAX_HEALTH, 53.0)
                .add(Attributes.FLYING_SPEED, 0.6f)
                .add(Attributes.MOVEMENT_SPEED, 0.225F).build();
    }
}
