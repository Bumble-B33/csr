package net.bumblebee.claysoldiers.entity.common.programmable;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.EnergyCapability;
import net.bumblebee.claysoldiers.claysoldierchips.ClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.EmptyClaySoldierChip;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.bumblebee.claysoldiers.claysoldierchips.work.ElectricianChip;
import net.bumblebee.claysoldiers.datamap.SoldierSlotCallback;
import net.bumblebee.claysoldiers.entity.common.StatInfoDisplay;
import net.bumblebee.claysoldiers.entity.common.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.common.soldier.status.SoldierStatusHolder;
import net.bumblebee.claysoldiers.entity.goal.workgoal.ClaySoldierFishGoal;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.bumblebee.claysoldiers.init.ModEntitySerializers;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.chip.ClaySoldierChipItem;
import net.bumblebee.claysoldiers.menu.soldier.ProgrammableClaySoldierMenu;
import net.bumblebee.claysoldiers.networking.ClaySoldierChipUpdatePayload;
import net.bumblebee.claysoldiers.networking.SoldierCarriedChangePayload;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ProgrammableClaySoldierSpawnPayload;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.team.OwnerQuery;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;

public class ProgrammableClaySoldierEntity extends AbstractClaySoldierEntity implements ProgrammableClayMobAccess {
    private static final EntityDataAccessor<Optional<EntityReference<LivingEntity>>> DATA_OWNER_UUID = SynchedEntityData.defineId(ProgrammableClaySoldierEntity.class, EntityDataSerializers.OPTIONAL_LIVING_ENTITY_REFERENCE);
    private static final EntityDataAccessor<Byte> DATA_WORK_STATUS = SynchedEntityData.defineId(ProgrammableClaySoldierEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Boolean> IS_FISHING_ANKER = SynchedEntityData.defineId(ProgrammableClaySoldierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<ClaySoldierFishingData> FISHING_POS = SynchedEntityData.defineId(ProgrammableClaySoldierEntity.class, ModEntitySerializers.FISHING_DATA);

    public static final String UNKNOWN_OWNER_LANG = "programmable_clay_soldier." + ClaySoldiersCommon.MOD_ID + ".owner.unknown";
    public static final String NO_OWNER_LANG = "programmable_clay_soldier." + ClaySoldiersCommon.MOD_ID + ".owner.none";

    public static final String OWNER_UUID_TAG = "owner_uuid";
    public static final String CHIP_TAG = "clay_soldier_chip";

    @NotNull
    private ClaySoldierChip brain;
    private ItemStack carriedStack = ItemStack.EMPTY;

    @Nullable
    private Vec3 clientBobberPos = null;
    private final ClaySoldierFishingTicker fishingTicker;
    private final EnergyCapability energyCapability;

    public ProgrammableClaySoldierEntity(EntityType<? extends AbstractClaySoldierEntity> entityType, Level level) {
        super(entityType, level, AttackTypeProperty.ROBOT, (s) -> SoldierStatusHolder.initProgrammable((ProgrammableClaySoldierEntity) s));
        this.inWallDamage = (w, e) -> w ? 0.1f : 0.2f;
        this.setCanPickUpLoot(true);
        this.brain = EmptyClaySoldierChip.EMPTY;
        this.fishingTicker = new ClaySoldierFishingTicker(this);
        this.energyCapability = new EnergyCapability() {
            @Override
            public int insert(int amount) {
                ItemStack carriedStack = getCarriedStack();
                if (carriedStack.isEmpty()) {
                    return 0;
                }
                return ClaySoldiersCommon.ENERGY_HELPER.insert(carriedStack, amount);
            }

            @Override
            public int extract(int wanted) {
                if (carriedStack.isEmpty()) {
                    return 0;
                }
                return ClaySoldiersCommon.ENERGY_HELPER.extract(carriedStack, wanted);
            }
        };
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public void addAdditionalSaveData(@NonNull ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.store(CHIP_TAG, ClaySoldierChip.CODEC, brain);
        brain.reset();

        if (!carriedStack.isEmpty()) {
            output.store(CARRIED_ITEM_TAG, ItemStack.CODEC, carriedStack);
        }

        getOwnerUUID().ifPresent(uuid -> output.store(OWNER_UUID_TAG, UUIDUtil.CODEC, uuid));
    }

    @Override
    public void readAdditionalSaveData(@NonNull ValueInput input) {
        super.readAdditionalSaveData(input);
        setupChip(input.read(CHIP_TAG, ClaySoldierChip.CODEC).orElse(EmptyClaySoldierChip.EMPTY));

        carriedStack = input.read(CARRIED_ITEM_TAG, ItemStack.CODEC).orElse(ItemStack.EMPTY);
        setOwnerUUID(input.read(OWNER_UUID_TAG, UUIDUtil.CODEC).orElse(null));
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_OWNER_UUID, Optional.empty());
        builder.define(DATA_WORK_STATUS, (byte) 0);
        builder.define(FISHING_POS, ClaySoldierFishingData.EMPTY);
        builder.define(IS_FISHING_ANKER, false);
    }

    @Override
    public boolean canHoldItem(ItemStack stack) {
        if (!level().isClientSide() && brain.requiresItemPickUp(stack)) {
            return true;
        }
        return super.canHoldItem(stack);
    }

    @Override
    public boolean wantsToPickUp(ServerLevel level, ItemStack pStack) {
        return canHoldItem(pStack);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemInHand = player.getItemInHand(hand);
        ClaySoldierChip chip = ClaySoldierChipItem.getChipFromItem(itemInHand);
        if (chip != null) {
            if (level() instanceof ServerLevel) {
                if (ClaySoldiersCommon.CONFIG.getServerConfig().chipRequiresLoyalty() && !isOwnedBy(player)) {
                    return InteractionResult.FAIL;
                }
                this.setPoiInfo(ClayBrushItem.getPoiPos(itemInHand));

                itemInHand.shrink(1);
                player.addItem(ClaySoldierChipItem.create(getInstalledChip()));
                setupChip(chip);
                if (player instanceof ServerPlayer serverPlayer) {
                    ModCritirions.FEED_CLAY_SOLDIER_TRIGGER.get().triggerChip(serverPlayer, itemInHand);
                }

                return InteractionResult.SUCCESS_SERVER;
            }
            return InteractionResult.CONSUME;
        }

        return super.mobInteract(player, hand);
    }

    private ItemStack equipGoalItem(ItemStack stack, SoldierSlotCallback slotCallback) {
        if (brain.requiresItemPickUp(stack) && carriedStack.isEmpty()) {
            slotCallback.carried();
            setCarriedStack(stack);
            return stack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public ItemStack equipItemIfPossible(ItemStack stack, SoldierSlotCallback slotCallBack) {
        var equipped = super.equipItemIfPossible(stack, slotCallBack);
        if (equipped.isEmpty()) {
            return equipGoalItem(stack, slotCallBack);
        }
        return equipped;
    }

    @Override
    public void die(DamageSource damageSource) {
        if (ClaySoldiersCommon.COMMON_HOOKS.onLivingDeath(this, damageSource)) return;

        super.die(damageSource);
        brain.reset();
    }

    @Override
    public ItemStack getCarriedStack() {
        return carriedStack;
    }

    public void setCarriedStack(ItemStack carriedStack) {
        this.carriedStack = carriedStack;
        updateCarriedStack();
    }

    public void updateCarriedStack() {
        if (!level().isClientSide()) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingEntity(this, new SoldierCarriedChangePayload(this.getId(), carriedStack));
        }
    }

    public void dropCarried() {
        this.dropItemStack(this.getCarriedStack().copy());
        this.setCarriedStack(ItemStack.EMPTY);
    }

    @Override
    public void enteredClayContainer() {
        dropCarried();
    }

    private void dropCarriedTick() {
        if (!brain.requiresItemCarrying(getCarriedStack())) {
            dropCarried();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!level().isClientSide()) {
            dropCarriedTick();
        }
    }

    public void setDataWorkStatus(byte statusCode) {
        entityData.set(DATA_WORK_STATUS, statusCode);
    }

    public byte getDataWorkStatus() {
        return entityData.get(DATA_WORK_STATUS);
    }


    @Override
    public boolean canPickUpLoot() {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource pSource, boolean pRecentlyHit) {
        super.dropCustomDeathLoot(level, pSource, pRecentlyHit);
        if (!getCarriedStack().isEmpty()) {
            this.dropItemStack(getCarriedStack());
        }
        ClaySoldierChip chip = getInstalledChip();
        ItemStack stack = ClaySoldierChipItem.create(getInstalledChip());
        if (chip.is(ModTags.ClaySoldierChips.REQUIRES_POI_POS)) {
            ClaySoldierChipItem.setPoiInfo(stack, getPoiInfo());
        }
        this.dropItemStack(stack);

    }

    @Override
    public boolean unableToMoveToOwner() {
        return brain.shouldStayAtWork() || super.unableToMoveToOwner();
    }

    private void clearChip() {
        brain = EmptyClaySoldierChip.EMPTY;
        if (!level().isClientSide()) {
            targetSelector.removeAllGoals(p -> true);
            goalSelector.removeAllGoals(p -> true);
        }

    }

    public void setupChip(@NotNull ClaySoldierChip chip) {
        clearChip();
        brain = chip.withSoldier(this);
        if (level() instanceof ServerLevel serverLevel) {
            chip.addGoals(serverLevel, this, goalSelector::addGoal, targetSelector::addGoal);
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingEntity(this, new ClaySoldierChipUpdatePayload(this.getId(), chip));
        }
    }

    public Optional<UUID> getOwnerUUID() {
        return entityData.get(DATA_OWNER_UUID).map(EntityReference::getUUID);
    }

    @Override
    public @Nullable EntityReference<LivingEntity> getOwnerReference() {
        return entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public void setOwnerUUID(@NotNull LivingEntity player) {
        entityData.set(DATA_OWNER_UUID, Optional.of(EntityReference.of(player)));
    }

    public void setOwnerUUID(@Nullable UUID ownerUUID) {
        if (ownerUUID == null) {
            entityData.set(DATA_OWNER_UUID, Optional.empty());
        } else {
            entityData.set(DATA_OWNER_UUID, Optional.of(EntityReference.of(ownerUUID)));
        }
    }


    @Override
    @NotNull
    public ClaySoldierChip getInstalledChip() {
        return brain;
    }

    public boolean isAllowedBreak() {
        return !brain.getAddons().contains(ClaySoldierChipAddons.NO_BREAK_ADDON);
    }

    @Override
    public @Nullable UUID getClayTeamOwnerUUID() {
        return getOwnerUUID().orElse(null);
    }

    @Override
    public OwnerQuery createOwnerQuery() {
        return getOwnerUUID().map(OwnerQuery::owner).orElse(OwnerQuery.none());
    }

    @Override
    public Component getOwnerDisplayName() {
        UUID owner = getOwnerUUID().orElse(null);
        if (owner == null) {
            return Component.translatable(NO_OWNER_LANG);
        }
        Player player = level().getPlayerByUUID(owner);
        if (player != null) {
            return player.getDisplayName();
        }
        return Component.translatable(UNKNOWN_OWNER_LANG, owner);
    }

    @Override
    protected boolean isClayFood(ItemStack stack) {
        return false;
    }

    @Override
    protected Component getInventoryName() {
        return this.getDisplayName();
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        super.getStatDisplay(list, viewer);
        list.add(CommonComponents.space().append(Component.translatable(MODULE, brain.info(this)).withStyle(ChatFormatting.GRAY)));

        list.add(Component.literal("  ").append(brain.getWorkStatusDisplayName(this)).withStyle(ChatFormatting.GRAY));

        list.add(CommonComponents.space().append(Component.translatable(StatInfoDisplay.FISHING, getFishingData().displayName(isFishingAnker())).withStyle(ChatFormatting.GRAY)));

        fishingTicker.getDisplayName().ifPresent(c -> list.add(CommonComponents.space().append(c).withStyle(ChatFormatting.GRAY)));
    }

    @Override
    public List<String> getInfoState() {
        var info = super.getInfoState();
        brain.appendDebugInfo(info::add);
        info.add("Fishing Anker: " + isFishingAnker());
        info.add("Fishing Data: " + getFishingData());
        info.add("Fishing Ticker: " + fishingTicker.getInfo());

        return info;
    }

    @Override
    public void sendSpawnPayload(ServerPlayer tracking) {
        super.sendSpawnPayload(tracking);
        ClaySoldiersCommon.NETWORK_MANGER.sendToPlayer(tracking, new ProgrammableClaySoldierSpawnPayload(this, brain));
    }

    public boolean isFishingAnker() {
        return entityData.get(IS_FISHING_ANKER);
    }

    public void setFishingAnker(boolean fishingAnker) {
        entityData.set(IS_FISHING_ANKER, fishingAnker);
        if (!fishingAnker) {
            stopFishing();
        }
        if (fishingAnker && !ClaySoldierFishGoal.hasFishingRod(this)) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Tried to set as Fishing Anker with out a Fishing Rod");
        }
    }

    protected ClaySoldierFishingData getFishingData() {
        return entityData.get(FISHING_POS);
    }

    public Vec3 getFishingPos() {
        return getFishingData().getPos();
    }

    public Vec3 getRelativeClientBobberPos() {
        if (clientBobberPos == null) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Tried getting invalid Client Bobber");
            return Vec3.ZERO;
        }
        return clientBobberPos.subtract(position());
    }

    public boolean isFishing() {
        return getFishingData().isFishing();
    }

    public void setFishingIfPossible(BlockPos pos) {
        if (fishingTicker.canFish()) {
            entityData.set(FISHING_POS, ClaySoldierFishingData.at(
                    Vec3.atLowerCornerWithOffset(pos,
                            getRandom().nextDouble(),
                            0.9,
                            getRandom().nextDouble())
            ));
        }
    }

    public void stopFishing() {
        entityData.set(FISHING_POS, ClaySoldierFishingData.EMPTY);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> accessor) {
        super.onSyncedDataUpdated(accessor);
        if (accessor.equals(FISHING_POS)) {
            if (isFishing()) {
                fishingTicker.startFishing();
                clientBobberPos = position();
            } else {
                fishingTicker.stopFishing();
                clientBobberPos = null;
            }
        }
    }

    public boolean holdsBattery() {
        return getCarriedStack().is(ModTags.Items.BATTERY);
    }

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide() && isFishing()) {
            moveBobberTowards(getFishingPos(), 0.6);
        }
        fishingTicker.tick();
    }

    private void moveBobberTowards(Vec3 target, double speedPerTick) {
        if (clientBobberPos == null) {
            return;
        }

        Vec3 delta = target.subtract(clientBobberPos);
        double distance = delta.length();

        // Already at target
        if (distance < 1.0E-6) {
            clientBobberPos = target;
            return;
        }

        // Prevent overshooting
        if (distance <= speedPerTick) {
            clientBobberPos = target;
            return;
        }

        // Move toward target
        Vec3 movement = delta.normalize().scale(speedPerTick);
        clientBobberPos = clientBobberPos.add(movement);
    }

    @Override
    protected OrderedCommand cycleOrderedCommand() {
        if (orderedCommand == OrderedCommand.SITTING) {
            return OrderedCommand.FOLLOW_OWNER;
        }
        return OrderedCommand.SITTING;
    }

    @Override
    protected void setOrderedCommand(OrderedCommand command) {
        if (command == OrderedCommand.IGNORE_OWNER) {
            super.setOrderedCommand(OrderedCommand.FOLLOW_OWNER);
        } else {
            super.setOrderedCommand(command);
        }
    }

    @Override
    protected OptionalInt openMenuScreen(Player player) {
        return ClaySoldiersCommon.COMMON_HOOKS.openMenu(player,
                new SimpleMenuProvider((id, inventory, _) -> new ProgrammableClaySoldierMenu(id, inventory, this), getInventoryName()),
                this.getId()
        );
    }

    public int insertEnergy(int toInsert) {
        return energyCapability.insert(toInsert);
    }

    public int getEnergyForCharging() {
        return getInstalledChip() instanceof ElectricianChip energy ? energy.getTransferRate() : 0;
    }
}
