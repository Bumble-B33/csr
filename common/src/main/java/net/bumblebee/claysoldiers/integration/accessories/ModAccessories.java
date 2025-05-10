package net.bumblebee.claysoldiers.integration.accessories;

import io.wispforest.accessories.api.AccessoriesAPI;
import io.wispforest.accessories.api.AccessoriesCapability;
import io.wispforest.accessories.api.Accessory;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModAccessories {
    private enum Accessories implements Accessory {
        CLAY_GOGGLES(ModItems.CLAY_GOGGLES);

        private final Supplier<? extends Item> item;

        Accessories(Supplier<? extends Item> item) {
            this.item = item;
        }
    }

    public static void init() {
        for (Accessories accessory : Accessories.values()) {
            AccessoriesAPI.registerAccessory(accessory.item.get(), accessory);
        }

        ClaySoldiersCommon.IS_WEARING_GOGGLES = ClaySoldiersCommon.IS_WEARING_GOGGLES.or(p ->
                hasAccessory(p, stack -> stack.is(ModItems.CLAY_GOGGLES.get()))
        );
    }

    public static boolean hasAccessory(Entity pEntity, Predicate<ItemStack> condition) {
        if (pEntity instanceof LivingEntity livingEntity) {
            return AccessoriesCapability.getOptionally(livingEntity)
                    .map(c -> c.getFirstEquipped(condition))
                    .isPresent();
        }
        return false;
    }
}
