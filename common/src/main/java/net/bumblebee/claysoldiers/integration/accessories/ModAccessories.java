package net.bumblebee.claysoldiers.integration.accessories;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.function.Predicate;
import java.util.function.Supplier;

public class ModAccessories {
    private enum Accessories /*implements Accessory*/ {
        CLAY_GOGGLES(ModItems.CLAY_GOGGLES),
        STATOMETER(ModItems.STATOMETER),
        CLAY_SOLDIER(ModItems.CLAY_SOLDIER) {
            /*@Override
            public boolean canEquip(ItemStack stack, SlotReference reference) {
                if (!(reference.entity() instanceof Player player)) {
                    return false;
                }
                var team = stack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get());
                return team != null && ClaySoldierSpawnItem.canEquipClaySoldier(player, team);
            }

            @Override
            public int maxStackSize(ItemStack stack) {
                return 1;
            }

            @Override
            public void onEquip(ItemStack stack, SlotReference reference) {
                if (reference.entity() instanceof ServerPlayer serverPlayer) {
                    ModCriterions.CLAY_SOLDIER_ON_HEAD_TRIGGER.get().trigger(serverPlayer);
                }
            }*/
        };

        private final Supplier<? extends Item> item;

        Accessories(Supplier<? extends Item> item) {
            this.item = item;
        }
    }

    public static void init() {
        for (Accessories accessory : Accessories.values()) {
            //AccessoryRegistry.register(accessory.item.get(), accessory);
        }

        ClaySoldiersCommon.IS_WEARING_GOGGLES.add(p -> hasAccessory(p, stack -> stack.is(ModTags.Items.CLAY_GOGGLES_ITEM)));
        ClaySoldiersCommon.IS_WEARING_CLAY_SOLDIER.add(e -> hasAccessory(e, stack -> stack.is(ModItems.CLAY_SOLDIER.get())));
        ClaySoldiersCommon.IS_WEARING_STATOMETER.add(e -> hasAccessory(e, stack -> stack.is(ModTags.Items.STAT_ITEM)));
    }

    public static boolean hasAccessory(Entity pEntity, Predicate<ItemStack> condition) {
        /*if (pEntity instanceof LivingEntity livingEntity) {
            return AccessoriesCapability.getOptionally(livingEntity)
                    .map(c -> c.getFirstEquipped(condition))
                    .isPresent();
        }*/
        return false;
    }
}
