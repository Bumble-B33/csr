package net.bumblebee.claysoldiers.platform.services;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public interface IClientHooks {
    default Model getArmorModel(LivingEntity entityLiving, ItemStack itemStack, EquipmentSlot slot, HumanoidModel<?> _default) {
        return _default;
    }

    default boolean isNameplateInRenderDistance(Entity entity, double distanceToSqr) {
        return !(distanceToSqr > 4096.0f);
    }

    boolean hasSoldierTabOpen();
}
