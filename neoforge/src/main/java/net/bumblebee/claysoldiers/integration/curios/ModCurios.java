package net.bumblebee.claysoldiers.integration.curios;

import net.neoforged.bus.api.IEventBus;

public class ModCurios {
    public ModCurios(IEventBus modEventBus) {
        //modEventBus.addListener(this::registerCapabilities);
        //modEventBus.addListener(this::commonSetup);
    }

    /*private void commonSetup(final FMLCommonSetupEvent event) {
        ClaySoldiersCommon.IS_WEARING_GOGGLES = ClaySoldiersCommon.IS_WEARING_GOGGLES.or(p -> hasItem(p, ModItems.CLAY_GOGGLES.get()));
        ClaySoldiersCommon.IS_WEARING_CLAY_SOLDIER.add(p -> hasItem(p, ModItems.CLAY_SOLDIER.get()));
    }

    private static boolean hasItem(Player player, Item item) {
        var cap = player.getCapability(CuriosCapability.INVENTORY);
        if (cap == null) {
            return false;
        }

        for (ICurioStacksHandler stacksHandler : cap.getCurios().values()) {
            int slots = stacksHandler.getSlots();
            for (int slot = 0; slot < slots; slot++) {
                if (stacksHandler.getStacks().getStackInSlot(slot).is(item)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void registerCapabilities(final RegisterCapabilitiesEvent evt) {
        evt.registerItem(
                CuriosCapability.ITEM,
                (stack, context) -> SoldierCurios.create(stack),
                ModItems.CLAY_SOLDIER);
    }

    private record SoldierCurios(ItemStack stack, ResourceLocation team) implements ICurio {
        public static @Nullable SoldierCurios create(ItemStack stack) {
            var team = ClaySoldierSpawnItem.getTeamFromStack(stack);
            if (team == null) {
                return null;
            }
            return new SoldierCurios(stack, team);
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public List<Component> getSlotsTooltip(List<Component> tooltips) {
            return ICurio.super.getSlotsTooltip(tooltips);
        }

        @Override
        public boolean canEquip(SlotContext slotContext) {
            if (!(slotContext.entity() instanceof Player player)) {
                return false;
            }
            return ClaySoldierSpawnItem.canEquipClaySoldier(player, team);
        }

        @Override
        public void onEquip(SlotContext slotContext, ItemStack prevStack) {
            if (slotContext.entity() instanceof ServerPlayer serverPlayer) {
                ModCriterions.CLAY_SOLDIER_ON_HEAD_TRIGGER.get().trigger(serverPlayer);
            }
        }
    }*/
}
