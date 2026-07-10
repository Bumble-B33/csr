package net.bumblebee.claysoldiers.entity.common.soldier;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.VampireSubjugate;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.bumblebee.claysoldiers.init.ModEffects;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.chip.ClaySoldierChipItem;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class ClaySoldierEntity extends AbstractClaySoldierEntity implements VampireSubjugate {
    @Nullable
    private UUID vampOwnerUUID;
    @Nullable
    private ClayMobEntity cachedVampOwner;

    public ClaySoldierEntity(EntityType<? extends AbstractClaySoldierEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel, AttackTypeProperty.NORMAL);
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        addVampOwner(output);
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        readVampOwner(input);
    }

    @Override
    public void setVampOwner(@Nullable ClayMobEntity pOwner) {
        this.vampOwnerUUID = pOwner != null ? pOwner.getUUID() : null;
        this.cachedVampOwner = pOwner;
    }

    @Nullable
    @Override
    public ClayMobEntity getVampOwner() {
        if (!hasVampiricConversionEffect()) {
            return null;
        }

        if (this.cachedVampOwner != null && !this.cachedVampOwner.isRemoved()) {
            return this.cachedVampOwner;
        } else if (this.vampOwnerUUID != null && this.level() instanceof ServerLevel serverlevel) {
            Entity entity = serverlevel.getEntity(this.vampOwnerUUID);
            if (entity instanceof ClayMobEntity clayMob) {
                cachedVampOwner = clayMob;
                return clayMob;
            }
            return null;
        } else {
            return null;
        }
    }

    @Override
    public boolean hasVampiricConversionEffect() {
        return hasEffect(ModEffects.VAMPIRE_CONVERSION);
    }

    @Override
    public void applyConversionEffect(ClayMobEntity source) {
        if (!hasVampiricConversionEffect()) {
            addEffect(new MobEffectInstance(ModEffects.VAMPIRE_CONVERSION, 200, 4));
        }
        setVampOwner(source);
    }

    private void addVampOwner(ValueOutput compound) {
        if (this.vampOwnerUUID != null || hasVampiricConversionEffect()) {
            compound.store(VAMPIRIC_OWNER_TAG, UUIDUtil.CODEC, vampOwnerUUID);
        }
    }

    private void readVampOwner(ValueInput compound) {
        vampOwnerUUID = compound.read(VAMPIRIC_OWNER_TAG, UUIDUtil.CODEC).orElse(null);
        this.cachedVampOwner = null;
    }

    @Override
    public void convertToVampire() {
        VampireClaySoldierEntity vampire = ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get().create(level(), EntitySpawnReason.CONVERSION);
        if (vampire != null) {
            vampire.snapTo(getX(), getY(), getZ(), getYRot(), getXRot());
            vampire.setIsAlpha(false);
            copyBasePropertiesTo(vampire, false);
            if (getVampOwner() != null) {
                vampire.setClayTeamType(getVampOwner().getClayTeamHolder());
            } else {
                vampire.setClayTeamType(this.getClayTeamHolder());
            }
            level().addFreshEntity(vampire);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        ClaySoldierChip<?> chip = ClaySoldierChipItem.getChipFromItem(itemInHand);
        if (chip != null) {

            if (level() instanceof ServerLevel) {
                if (ClaySoldiersCommon.CONFIG.getServerConfig().chipRequiresLoyalty() && !isOwnedBy(player)) {
                    return InteractionResult.FAIL;
                }
                BlockPos pos = ClayBrushItem.getPoiPos(itemInHand);

                convertToSoldier(ModEntityTypes.PROGRAMMABLE_CLAY_SOLDIER_ENTITY.get(), s -> {
                    s.setupChip(chip);
                    s.setClayTeamType(getClayTeamHolder());
                    s.stopRiding();
                    s.setOwnerUUID(player.getUUID());
                    s.setPoiPos(pos);
                });
                if (player instanceof ServerPlayer serverPlayer) {
                    ModCritirions.FEED_CLAY_SOLDIER_TRIGGER.get().triggerChip(serverPlayer, itemInHand);
                }
                itemInHand.shrink(1);

                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.CONSUME;
        }

        return super.mobInteract(player, hand);
    }
}
