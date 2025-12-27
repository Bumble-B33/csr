package net.bumblebee.claysoldiers.util;

import net.bumblebee.claysoldiers.claysoldierpredicate.ClaySoldierInventoryQuery;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public final class SlimeBootsUtil {
    public static boolean canBounce(Entity entity, boolean hurt) {
        if (entity instanceof LivingEntity livingEntity) {
            ItemStack stack = livingEntity.getItemBySlot(EquipmentSlot.FEET);
            if (stack.is(ModItems.SLIME_BOOTS.get())) {
                if (!livingEntity.isCrouching()) {
                    if (hurt) {
                        stack.hurtAndBreak(1, livingEntity, EquipmentSlot.FEET);
                    }
                    return true;
                }
            }
        }
        return entity instanceof ClaySoldierInventoryQuery soldier && soldier.allProperties().canBounce();
    }



    private SlimeBootsUtil() {
    }
}
