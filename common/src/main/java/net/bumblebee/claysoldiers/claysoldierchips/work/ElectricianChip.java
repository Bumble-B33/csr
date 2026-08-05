package net.bumblebee.claysoldiers.claysoldierchips.work;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.AddonInfo;
import net.bumblebee.claysoldiers.claysoldierchips.ClayMobWorkAccess;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChips;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.workgoal.ElectricianGoal;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Consumer;

public class ElectricianChip extends WorkGoalChip<ElectricianGoal> {
    private static final Identifier ASSET = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "electrician");
    public static final AddonInfo ADDON_INFO = new AddonInfo() {
        @Override
        public boolean canBeApplied(List<ClaySoldierChipAddon> presentAddons, ClaySoldierChipAddon addon) {
            return addon == ClaySoldierChipAddons.ACCELERATION_ADDON || addon == ClaySoldierChipAddons.UPGRADED_ACCELERATION_ADDON;
        }

        @Override
        public int getAllowedAddonsCount() {
            return 2;
        }
    };
    private static final ElectricianChip NO_ADDON = new ElectricianChip(List.of());
    public static final String TRANSFER_RATE_LANG = LANG_PREFIX + "electrician.transfer_rate";


    protected ElectricianChip(List<ClaySoldierChipAddon> addons) {
        super(addons, ASSET, 0xFF353535, 0xFFAA0E01);
    }

    public static ElectricianChip create(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return NO_ADDON;
        }
        return new ElectricianChip(addons);
    }

    public static ElectricianChip create() {
        return NO_ADDON;
    }

    @Override
    protected ElectricianGoal createGoal(ProgrammableClaySoldierEntity soldier, ClayMobWorkAccess workAccess) {
        return new ElectricianGoal(soldier, workAccess, getTransferRate());
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.ELECTRICIAN_TYPE.get();
    }

    public int getTransferRate() {
        int baseRate = ClaySoldiersCommon.CONFIG.getCommonConfig().baseSoldierEnergyTransferRate();
        int addonCount = getAccelerationAddonCount();

        long result = (long) addonCount * baseRate + baseRate;
        return result > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) result;}

    @Override
    public void appendItemHoverText(ItemStack stack, Consumer<Component> tooltipAdder) {
        tooltipAdder.accept(CommonComponents.space().append(Component.translatable(TRANSFER_RATE_LANG, getTransferRate(), ClaySoldiersCommon.ENERGY_HELPER.getEnergyUnitName())).withStyle(ChatFormatting.GRAY));
    }
}
