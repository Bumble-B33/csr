package net.bumblebee.claysoldiers.entity.common.programmable;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.function.BooleanSupplier;

public class ClaySoldierFishingTicker {
    private static final int BREAK_TIME_TICK = 200;
    private static final int FISHING_START = 100;
    public static final String FISHING_LANG = "clay_soldier_fishing_ticker.%s.fishing".formatted(ClaySoldiersCommon.MOD_ID);
    public static final String BREAK_LANG = "clay_soldier_fishing_ticker.%s.break".formatted(ClaySoldiersCommon.MOD_ID);

    private final ProgrammableClaySoldierEntity soldier;
    private final RandomSource random;
    private final FakeFishingHook fishingHook;
    private final Level level;
    private int fishingTick = -1;
    private int breakTick = -1;
    private float fishAngle;

    public ClaySoldierFishingTicker(ProgrammableClaySoldierEntity soldier) {
        this.soldier = soldier;
        this.level = soldier.level();
        this.random = soldier.getRandom();
        this.fishingHook = new FakeFishingHook(level, this::canFishTreasure);
    }

    public void tick() {
        if (breakTick > 0) {
            breakTick--;
            fishingTick = -1;
        }

        if (fishingTick > 0) {
            fishingTick--;
            if (fishingTick > 2 && level.isRainingAt(soldier.blockPosition())) {
                fishingTick--;
            }
        }
        if (fishingTick == 0) {
            if (level instanceof ServerLevel serverLevel) {
                retrieve(serverLevel, soldier.getFishingPos());
            } else {
                soldier.playSound(SoundEvents.FISHING_BOBBER_SPLASH, 0.25F, 1.0F + (getRandom().nextFloat() - getRandom().nextFloat()) * 0.4F);
            }

            soldier.stopFishing();
        }
        if (fishingTick == FISHING_START) {
            this.fishAngle = Mth.nextFloat(getRandom(), 0.0F, 360.0F);
        } else if (fishingTick > 0 && fishingTick < FISHING_START && level.isClientSide()) {
            displayFish();
        }

    }

    private void displayFish() {
        Vec3 fishPos = getFishingPos();
        this.fishAngle = this.fishAngle + (float)getRandom().triangle(0.0, 9.188);
        float angle = this.fishAngle * (float) (Math.PI / 180.0);
        float angleSin = Mth.sin(angle);
        float angleCos = Mth.cos(angle);
        double fishX = fishPos.x() + angleSin * this.fishingTick * 0.1F;
        double fishY = Mth.floor(fishPos.y()) + 1.0F;
        double fishZ = fishPos.z() + angleCos * this.fishingTick * 0.1F;
        BlockState splashBlockState = level.getBlockState(BlockPos.containing(fishX, fishY - 1.0, fishZ));
        if (splashBlockState.is(Blocks.WATER)) {
            if (getRandom().nextFloat() < 0.15F) {
                displayFishingParticle(ParticleTypes.BUBBLE, fishX, fishY - 0.1f, fishZ, angleSin, 0.1, angleCos, 0);
            }

            float particleXMovement = angleSin * 0.04F;
            float particleZMovement = angleCos * 0.04F;
            displayFishingParticle(ParticleTypes.FISHING, fishX, fishY, fishZ, particleZMovement, 0.01, -particleXMovement, 1.0);
            displayFishingParticle(ParticleTypes.FISHING, fishX, fishY, fishZ, -particleZMovement, 0.01, particleXMovement, 1.0);
        }
    }

    private void displayFishingParticle(ParticleOptions particle, double fishX, double fishY, double fishZ, double xDist, double yDist, double zDist, double speed) {
        double xa = speed * xDist;
        double ya = speed * yDist;
        double za = speed * zDist;
        level.addParticle(particle, fishX, fishY, fishZ, xa, ya, za);
    }

    public String getInfo() {
        return "FishingTick(Break: %s, Fishing: %s)".formatted(breakTick, fishingTick);
    }

    public Optional<Component> getDisplayName() {
        if (breakTick > 0) {
            return Optional.of(Component.translatable(BREAK_LANG, breakTick / 20));
        }
        if (fishingTick > 0) {
            return Optional.of(Component.translatable(FISHING_LANG, fishingTick / 20));
        }
        return Optional.empty();
    }

    public boolean canFish() {
        return breakTick <= 0 && fishingTick <= 0;
    }

    public void startFishing() {

        fishingTick = getFishingTime() + Math.max(breakTick, 0);
        breakTick = -1;
    }

    public void stopFishing() {
        fishingTick = -1;
        breakTick = getBreakTime();
    }

    private Vec3 getFishingPos() {
        return soldier.getFishingPos();
    }

    private void retrieve(ServerLevel level, Vec3 fishingPos) {

        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.ORIGIN, fishingPos)
                .withParameter(LootContextParams.TOOL, soldier.getCarriedStack())
                .withParameter(LootContextParams.ATTACKING_ENTITY, soldier)
                .withParameter(LootContextParams.THIS_ENTITY, fishingHook)
                .withLuck(soldier.getLuck())
                .create(LootContextParamSets.FISHING);
        LootTable lootTable = level.getServer().reloadableRegistries().getLootTable(BuiltInLootTables.FISHING);
        List<ItemStack> items = lootTable.getRandomItems(params);

        for (ItemStack itemStack : items) {
            ItemEntity entity = new ItemEntity(level, fishingPos.x(), fishingPos.y() + 1, fishingPos.z(), itemStack);
            double xa = soldier.getX() - fishingPos.x();
            double ya = soldier.getY() - fishingPos.y();
            double za = soldier.getZ() - fishingPos.z();
            double speed = 0.1;
            entity.setDeltaMovement(xa * speed, ya * speed + Math.sqrt(Math.sqrt(xa * xa + ya * ya + za * za)) * 0.08, za * speed);
            level.addFreshEntity(entity);
        }
    }

    private int getBreakTime() {
        return soldier.isAllowedBreak() ? BREAK_TIME_TICK : -1;
    }

    private int getFishingTime() {
        int time = Math.max(500 - accelerationAddonCount() * 100, 0);


        return FISHING_START + time; //+ (((int) (level.getGameTime()) % 12) * 9);
    }

    private boolean canFishTreasure() {
        return soldier.getInstalledChip().hasAddon(ClaySoldierChipAddons.FISH_TREASURE_ADDON);
    }

    private int accelerationAddonCount() {
        return soldier.getInstalledChip().addonCount(ClaySoldierChipAddons.ACCELERATION_ADDON) + 1;
    }

    private RandomSource getRandom() {
        return random;
    }

    private static class FakeFishingHook extends FishingHook {
        private final BooleanSupplier inOpenWater;

        public FakeFishingHook(Level level, BooleanSupplier inOpenWater) {
            super(EntityType.FISHING_BOBBER, level);
            this.inOpenWater = inOpenWater;
        }

        @Override
        public boolean isOpenWaterFishing() {
            return inOpenWater.getAsBoolean();
        }
    }
}
