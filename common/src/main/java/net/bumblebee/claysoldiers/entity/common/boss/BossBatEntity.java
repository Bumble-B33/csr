package net.bumblebee.claysoldiers.entity.common.boss;

import net.minecraft.core.NonNullList;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.List;

public class BossBatEntity extends Bat {
    private NonNullList<ItemStack> inventory = NonNullList.createWithCapacity(0);

    public BossBatEntity(EntityType<? extends BossBatEntity> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier createBatAttributes() {
        return Mob.createMobAttributes().add(Attributes.MAX_HEALTH, 6.0).build();
    }

    public void setLoot(List<ItemStack> loot) {
        inventory = NonNullList.createWithCapacity(loot.size());
        inventory.addAll(loot);
    }

    @Override
    public void addAdditionalSaveData(ValueOutput compound) {
        super.addAdditionalSaveData(compound);
        ContainerHelper.saveAllItems(compound, inventory, false);
    }

    @Override
    public void readAdditionalSaveData(ValueInput compound) {
        super.readAdditionalSaveData(compound);
        inventory = NonNullList.create();
        ContainerHelper.loadAllItems(compound, inventory);

    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource damageSource, boolean recentlyHit) {
        super.dropCustomDeathLoot(level, damageSource, recentlyHit);
        inventory.forEach(stack -> spawnAtLocation(level, stack));
    }
}
