package net.bumblebee.claysoldiers.entity.soldier;

import com.google.common.collect.Iterables;
import com.mojang.logging.LogUtils;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.AssignablePoiCapability;
import net.bumblebee.claysoldiers.capability.ThrowableItemCapability;
import net.bumblebee.claysoldiers.clayremovalcondition.RemovalCondition;
import net.bumblebee.claysoldiers.clayremovalcondition.RemovalConditionContext;
import net.bumblebee.claysoldiers.datamap.SoldierEquipmentSlot;
import net.bumblebee.claysoldiers.datamap.SoldierHoldableEffect;
import net.bumblebee.claysoldiers.datamap.SoldierSlotCallback;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.ClayMobTeamOwnerEntity;
import net.bumblebee.claysoldiers.entity.ClaySoldierRideableMap;
import net.bumblebee.claysoldiers.entity.ClayWraithEntity;
import net.bumblebee.claysoldiers.entity.goal.*;
import net.bumblebee.claysoldiers.entity.goal.target.*;
import net.bumblebee.claysoldiers.entity.goal.workgoal.*;
import net.bumblebee.claysoldiers.entity.goal.workgoal.dig.DigHoleGoal;
import net.bumblebee.claysoldiers.entity.soldier.status.SoldierStatusHolder;
import net.bumblebee.claysoldiers.entity.soldier.status.SoldierStatusManager;
import net.bumblebee.claysoldiers.entity.throwables.ClaySoldierThrowableItemEntity;
import net.bumblebee.claysoldiers.init.ModDamageTypes;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.item.itemeffectholder.ItemStackWithEffect;
import net.bumblebee.claysoldiers.menu.soldier.ClaySoldierMenu;
import net.bumblebee.claysoldiers.networking.ClaySoldierReviveCooldownPayload;
import net.bumblebee.claysoldiers.networking.SoldierCarriedChangePayload;
import net.bumblebee.claysoldiers.networking.SoldierItemChangePayload;
import net.bumblebee.claysoldiers.networking.spawnpayloads.ClaySoldierSpawnPayload;
import net.bumblebee.claysoldiers.soldierpoi.FindNearestPoiGoal;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyMapReader;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyTypes;
import net.bumblebee.claysoldiers.soldierproperties.SoldierVehicleProperties;
import net.bumblebee.claysoldiers.soldierproperties.combined.SoldierHoldablePropertiesCombiner;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.RangedAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.WraithProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveProperty;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveResult;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.revive.ReviveType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttack;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialAttackType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.specialattack.SpecialEffectCategory;
import net.bumblebee.claysoldiers.soldierproperties.types.BreathHoldPropertyType;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.bumblebee.claysoldiers.util.color.EntityDataColorWrapper;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.CombatRules;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.navigation.AmphibiousPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AbstractClaySoldierEntity extends ClayMobTeamOwnerEntity implements RangedAttackMob,
        ClaySoldierInventoryHandler, ClaySoldierLike {
    public static final String DEFENDING_AREA_LANG = IWorkGoal.JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "defending_area");
    public static final String PROTECTING_OWNER_LANG = IWorkGoal.JOB_LANG_KEY.formatted(ClaySoldiersCommon.MOD_ID, "protecting_owner");

    public static final String BACKPACK_ITEMS_TAG = "SoldierBackpackItems";
    public static final String HAND_ITEMS_TAG = "SoldierHandItems";
    public static final String ARMOR_ITEMS_TAG = "SoldierArmorItems";
    public static final String OFFSET_COLOR_TAG = "OffsetColor";
    public static final String REVIVE_TYPE_COOLDOWN_TAG = "revive_type_cooldown";
    public static final String SKIN_VARIANT_ID_TAG = "SkinVariantId";
    public static final String CARRIED_ITEM_TAG = "CarriedItem";

    public static final String FUSE_TAG = "Fuse";
    public static final String EXPLOSION_RADIUS_TAG = "ExplosionRadius";
    public static final String IGNITED_TAG = "ignited";

    private static final EntityDataAccessor<Byte> DATA_WORK_STATUS = SynchedEntityData.defineId(AbstractClaySoldierEntity.class, EntityDataSerializers.BYTE);
    private static final EntityDataAccessor<Integer> DATA_SWELL_DIR = SynchedEntityData.defineId(AbstractClaySoldierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_IGNITED = SynchedEntityData.defineId(AbstractClaySoldierEntity.class, EntityDataSerializers.BOOLEAN);
    private static final Logger LOGGER = LogUtils.getLogger();

    private static final EntityDataAccessor<Integer> DATA_OFFSET_COLOR = SynchedEntityData.defineId(AbstractClaySoldierEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_IS_JEB = SynchedEntityData.defineId(AbstractClaySoldierEntity.class, EntityDataSerializers.BOOLEAN);

    protected static final byte EXPLODE_FIREWORK_EVENT = 81;

    private static final int MIN_POWER_FOR_SPECIAL_ATTACKS = 1;

    protected final AttackTypeProperty defaultAttackType;
    private static final byte NO_GLIDE = -1;
    private static final byte GLIDE_UNCHECKED = -2;
    /**
     * Store the last ordinal of the equipment slot a glider was in.
     * <p>{@value NO_GLIDE} == to No Glider.</p>
     * <p>{@value GLIDE_UNCHECKED} == to not checked if there is a glider</p>
     */
    private byte lastGliderSlot = GLIDE_UNCHECKED;

    private final NonNullList<ItemStackWithEffect> soldierHandItems = NonNullList.withSize(2, ItemStackWithEffect.EMPTY);
    private final NonNullList<ItemStackWithEffect> soldierArmorItems = NonNullList.withSize(5, ItemStackWithEffect.EMPTY);
    private final NonNullList<ItemStackWithEffect> soldierBackpackItems = NonNullList.withSize(2, ItemStackWithEffect.EMPTY);
    protected final SoldierHoldablePropertiesCombiner propertyCombiner;
    public float oBob;
    public float bob;
    private static final double MAGIC_CAPE_NUMBER = 10.0;
    public double xCloakO;
    public double yCloakO;
    public double zCloakO;
    public double xCloak;
    public double yCloak;
    public double zCloak;
    private float delayedScale;
    private int oldSwell;
    private int swell;
    private int maxSwell = 30;
    private float explosionRadius = 1;
    @Nullable
    private ItemStackWithEffect stackWithProjectile;
    private int skinVariantId;
    private WorkSelectorGoal workSelector;
    private ItemStack carriedStack = ItemStack.EMPTY;
    private final SoldierStatusHolder statusManger;
    private final EntityDataColorWrapper colorGetter;
    private final Map<ReviveType, Long> reviveTypeCooldown = new EnumMap<>(ReviveType.class);

    private final PathNavigation waterNavigation;
    private final PathNavigation groundNavigation;

    protected AbstractClaySoldierEntity(EntityType<? extends AbstractClaySoldierEntity> pEntityType, Level pLevel, AttackTypeProperty defaultAttackType) {
        this(pEntityType, pLevel, defaultAttackType, s -> SoldierStatusManager.initDefault(s, () -> s.workSelector));
        this.setCanPickUpLoot(true);
        this.setSpawnedFrom(ModItems.CLAY_SOLDIER.get().getDefaultInstance(), true);
        this.inWallDamage = (w, e) -> {
            if (workSelector.isWorking()) {
                return 0.2f;
            }
            return w ? 0.5f : 1f;
        };
        this.delayedScale = getSoldierSize();
    }

    protected AbstractClaySoldierEntity(EntityType<? extends AbstractClaySoldierEntity> pEntityType, Level pLevel, AttackTypeProperty defaultAttackType, Function<AbstractClaySoldierEntity, SoldierStatusHolder> statusManger) {
        super(pEntityType, pLevel);
        this.defaultAttackType = defaultAttackType;
        this.stackWithProjectile = null;
        this.propertyCombiner = new SoldierHoldablePropertiesCombiner(this, this.getAttributes());
        this.skinVariantId = getRandom().nextInt(2) == 1 ? 1 : 0;
        this.statusManger = statusManger.apply(this);
        this.colorGetter = new EntityDataColorWrapper(DATA_OFFSET_COLOR, DATA_IS_JEB, this::getEntityData);
        this.waterNavigation = new AmphibiousPathNavigation(this, pLevel);
        this.groundNavigation = createNavigation(pLevel);

        this.moveControl = new ClaySoldierMoveControl(this, this::canSwim, 85, 10, false);

        this.workSelector = getOrCreateWorkSelectorGoal();
    }

    public static AttributeSupplier setSoldierAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.ARMOR, 0D)
                .add(Attributes.ATTACK_DAMAGE, 2.5f)
                .add(Attributes.ATTACK_SPEED, 0.1D)
                .add(Attributes.MOVEMENT_SPEED, 0.3D)
                .add(Attributes.FOLLOW_RANGE, 16.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.5)
                .build();
    }



    private static Iterable<ItemStack> convertToStack(List<ItemStackWithEffect> stackWithEffects) {
        return stackWithEffects.stream().map(ItemStackWithEffect::stack).toList();
    }

    /**
     * @return a {@code WorkSelectorGoal} with all available jobs
     */
    protected WorkSelectorGoal getOrCreateWorkSelectorGoal() {
        if (workSelector != null) {
            return workSelector;
        }
        List<Goal> goals = new ArrayList<>(List.of(
                new BreakCropGoal(this, 1, 16, () -> workSelector),
                new PickUpItemsGoal(this, () -> workSelector),
                new PlaceSeedsGoal(this, () -> workSelector, 16),
                new DigHoleGoal(this, () -> workSelector)));

        if (ClaySoldiersCommon.COMMON_HOOKS.isBlueprintEnabled(level().enabledFeatures())) {
            goals.add(new BuildBlueprintGoal(this, () -> workSelector, 8));
        }
        workSelector = new WorkSelectorGoal(this, goals);
        return workSelector;
    }


    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new ClaySodlierBreathAirGoal(this));
        this.goalSelector.addGoal(2, new ClayMobSitGoal(this));
        this.goalSelector.addGoal(3, getOrCreateWorkSelectorGoal());
        this.goalSelector.addGoal(4, new UseAssignedPoiGoal(this, 1.2));
        this.goalSelector.addGoal(4, new FindNearestPoiGoal(this));
        this.goalSelector.addGoal(4, new ClaySoldierMountGoal(this, 1.2f, false));

        this.goalSelector.addGoal(5, new ClaySoldierRangedAttackGoal(this, 1.25, 10.0F));
        this.goalSelector.addGoal(5, new ClaySoldierMeleeAttackGoal(this, 1f, false));
        this.goalSelector.addGoal(6, new ClaySoldierMeleeSupportGoal(this, 1f, true));
        this.goalSelector.addGoal(7, new ClayMobFollowTeamOwner(this, 1, 5.5f, 3F));
        this.goalSelector.addGoal(8, new TemptGoal(this, 1.2f, this::isClayFood, false));

        this.goalSelector.addGoal(9, new RandomStrollGoal(this, 1f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));

        this.targetSelector.addGoal(0, new ClaySoldierHurtByTargetGoal(this));
        this.targetSelector.addGoal(1, new NearestRideableTargetGoal(this));
        this.targetSelector.addGoal(2, new ClayMobOwnerHurtByTarget(this));
        this.targetSelector.addGoal(3, new ClayMobOwnerTarget(this));
        this.targetSelector.addGoal(4, new ClaySoldierNearestTargetGoal(this, false, this::targetPredicate, this::specificTargetPredicate));
    }



    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        addArmorSaveData(pCompound);

        pCompound.putShort(FUSE_TAG, (short) this.maxSwell);
        pCompound.putByte(EXPLOSION_RADIUS_TAG, (byte) this.explosionRadius);
        pCompound.putBoolean(IGNITED_TAG, this.isIgnited());
        getOffsetColor().writeToTag(OFFSET_COLOR_TAG, pCompound);

        CompoundTag reviveCooldownMapNBT = new CompoundTag();
        for (var entry : reviveTypeCooldown.entrySet()) {
            var value = entry.getValue();
            if (value != null) {
                reviveCooldownMapNBT.putLong(entry.getKey().getSerializedName(), value);
            }
        }
        if (!reviveCooldownMapNBT.isEmpty()) {
            pCompound.put(REVIVE_TYPE_COOLDOWN_TAG, reviveCooldownMapNBT);
        }
        pCompound.putInt(SKIN_VARIANT_ID_TAG, skinVariantId);
        workSelector.saveToTag(pCompound);
        workSelector.resetGoal();
        if (!carriedStack.isEmpty()) {
            pCompound.put(CARRIED_ITEM_TAG, carriedStack.save(registryAccess()));
        }
    }

    protected void addArmorSaveData(CompoundTag tag) {
        tag.put(HAND_ITEMS_TAG, createItemListTag(soldierHandItems));
        tag.put(ARMOR_ITEMS_TAG, createItemListTag(soldierArmorItems));
        tag.put(BACKPACK_ITEMS_TAG, createItemListTag(soldierBackpackItems));
    }

    private ListTag createItemListTag(List<ItemStackWithEffect> itemStacks) {
        ListTag listTag = new ListTag();
        for (ItemStackWithEffect stackWithEffect : itemStacks) {
            if (!stackWithEffect.isEmpty()) {
                listTag.add(stackWithEffect.save(this.registryAccess()));
            } else {
                listTag.add(new CompoundTag());
            }
        }
        return listTag;
    }


    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        readArmorSaveData(pCompound);

        if (pCompound.contains(FUSE_TAG, Tag.TAG_ANY_NUMERIC)) {
            this.maxSwell = pCompound.getShort(FUSE_TAG);
        }

        if (pCompound.contains(EXPLOSION_RADIUS_TAG, 99)) {
            this.explosionRadius = pCompound.getByte(EXPLOSION_RADIUS_TAG);
        }

        if (pCompound.getBoolean(IGNITED_TAG)) {
            this.ignite();
        }
        this.setOffsetColor(ColorHelper.getFromTag(OFFSET_COLOR_TAG, pCompound));


        querySpecialProperties();

        initCombinedProperties();
        updateOtherProperties();
        if (pCompound.contains(REVIVE_TYPE_COOLDOWN_TAG, Tag.TAG_COMPOUND)) {
            CompoundTag reviveTypeNbt = pCompound.getCompound(REVIVE_TYPE_COOLDOWN_TAG);
            for (String key : reviveTypeNbt.getAllKeys()) {
                ReviveType.getFromString(key).ifPresent(type -> reviveTypeCooldown.put(type, reviveTypeNbt.getLong(key)));
            }
        }
        skinVariantId = pCompound.getInt(SKIN_VARIANT_ID_TAG);
        workSelector.readFromTag(pCompound);
        if (pCompound.contains(CARRIED_ITEM_TAG)) {
            carriedStack = ItemStack.parseOptional(registryAccess(), pCompound.getCompound(CARRIED_ITEM_TAG));
        }
    }

    protected void readArmorSaveData(CompoundTag tag) {
        getFromTag(tag, HAND_ITEMS_TAG, soldierHandItems::set, this.registryAccess());
        getFromTag(tag, ARMOR_ITEMS_TAG, soldierArmorItems::set, this.registryAccess());
        getFromTag(tag, BACKPACK_ITEMS_TAG, soldierBackpackItems::set, this.registryAccess());
    }

    /**
     * Populates a list of {@link ItemStackWithEffect} objects from a given {@link CompoundTag}.
     * <p>
     * If the specified key exists in the tag as a list, the method reads its elements and updates
     * the provided list of items by parsing each entry from the tag.
     * <p>
     *
     * @param tag            The {@code CompoundTag} containing the serialized list of item stacks.
     * @param key            The key under which the item stack data is stored.
     * @param listSetter     Called for each parsed {@code ItemStackWithEffect} and its index.
     */
    public static void getFromTag(CompoundTag tag, String key, BiConsumer<Integer, ItemStackWithEffect> listSetter, RegistryAccess registryAccess) {
        if (tag.contains(key, Tag.TAG_LIST)) {
            ListTag listTag = tag.getList(key, Tag.TAG_COMPOUND);

            for (int index = 0; index < listTag.size(); ++index) {
                listSetter.accept(index, ItemStackWithEffect.parseOptional(registryAccess, listTag.getCompound(index)));
            }
        }
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_SWELL_DIR, -1);
        builder.define(DATA_IS_IGNITED, false);

        builder.define(DATA_WORK_STATUS, WorkSelectorGoal.encodeWorkStatusToByte(WorkSelectorGoal.RESTING_INDEX, 0));

        EntityDataColorWrapper.define(builder, DATA_OFFSET_COLOR, DATA_IS_JEB);
    }

    // Team
    protected void handleTeamChange(ResourceLocation teamId) {
        propertyCombiner.addPropertyFromTeam(ClayMobTeamManger.getFromKeyAssumeValid(teamId, registryAccess()));
        propertyCombiner.combine();
    }

    /**
     * Returns the damage multiplier against the target.
     * This also determines knockBack and ignite ticks.
     * A value >= {@value MIN_POWER_FOR_SPECIAL_ATTACKS}, will trigger special attacks on the target.
     *
     * @param target the target attacking
     * @return the damage multiplier against the target
     */
    protected float getAttackPower(Entity target) {
        return target instanceof ClayMobEntity ? 1f : 0.2f;
    }

    @Override
    public boolean doHurtTarget(Entity target) {
        final float power = getAttackPower(target);
        DamageSource damagesource = this.damageSources().mobAttack(this);


        float attackDamage = getBaseAttackDamage();
        if (this.level() instanceof ServerLevel serverlevel) {
            attackDamage = EnchantmentHelper.modifyDamage(serverlevel, getItemBySlot(SoldierEquipmentSlot.MAINHAND).stack(), target, damagesource, attackDamage);
            attackDamage = EnchantmentHelper.modifyDamage(serverlevel, getItemBySlot(SoldierEquipmentSlot.OFFHAND).stack(), target, damagesource, attackDamage);
        }

        float knockBack = getKnockback(target, damagesource) * power;
        int secOnFireInTicks = 0;

        attackDamage += allProperties().damage();
        secOnFireInTicks += allProperties().setOnFire();

        if (secOnFireInTicks > 0) {
            target.igniteForTicks((int) (secOnFireInTicks * power));
        }
        var specialAttacks = allProperties().specialAttacks(SpecialAttackType.MELEE, SpecialEffectCategory.HARMFUL);
        attackDamage += (float) specialAttacks.stream().mapToDouble(specialAttackType -> specialAttackType.getBonusDamage(this, target)).sum();

        boolean wasHurt = target.hurt(damagesource, attackDamage * power);
        if (wasHurt) {
            if (knockBack > 0.0F && target instanceof LivingEntity) {
                ((LivingEntity) target)
                        .knockback(
                                knockBack * 0.5F,
                                Mth.sin(this.getYRot() * (float) (Math.PI / 180.0)),
                                -Mth.cos(this.getYRot() * (float) (Math.PI / 180.0))
                        );
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.6, 1.0, 0.6));
            }

            if (this.level() instanceof ServerLevel serverlevel) {
                EnchantmentHelper.doPostAttackEffects(serverlevel, target, damagesource);
            }
            this.setLastHurtMob(target);
            this.playAttackSound();

            if (power >= MIN_POWER_FOR_SPECIAL_ATTACKS) {
                for (SpecialAttack<?> specialAttack : specialAttacks) {
                    specialAttack.performAttackEffect(this, target);
                }
                specializedAttack(target);
            }

            if (target instanceof AbstractClaySoldierEntity soldierTarget) {
                soldierTarget.performCounterAttacks(this, SpecialAttackType.MELEE);
            }
        }

        return wasHurt;
    }

    /**
     * Called when this {@code ClaySoldier} attacks.
     */
    protected void specializedAttack(Entity target) {
    }

    /**
     * Returns the base attack damage of this {@code Soldier}.
     * This is called before any increases from {@code SoldierProperties} and such.
     */
    protected float getBaseAttackDamage() {
        return (float) this.getAttributeValue(Attributes.ATTACK_DAMAGE);
    }

    private void performCounterAttacks(AbstractClaySoldierEntity target, SpecialAttackType type) {
        float sumDamage = 0f;
        for (SpecialAttack<?> counterAttack : allProperties().counterAttacks(type)) {
            counterAttack.performAttackEffect(this, target);
            sumDamage += counterAttack.getBonusDamage(this, target);
        }
        if (sumDamage > 0) {
            target.hurt(damageSources().thorns(this), sumDamage);
        }
    }

    @Override
    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        ItemStack itemInHand = pPlayer.getItemInHand(pHand);
        if (getAttackType().isRoyalty() && isClayFood(itemInHand) && !pPlayer.equals(getClayTeamOwner())) {
            if (!level().isClientSide) {
                if (tryClaimingTeam(pPlayer)) {
                    level().broadcastEntityEvent(this, SPAWN_HAPPY_EVENT);
                } else {
                    level().broadcastEntityEvent(this, SPAWN_ANGRY_EVENT);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return super.mobInteract(pPlayer, pHand);
    }

    @Override
    protected boolean clayBrushEffect(ClayBrushItem.Mode mode, ItemStack itemInHand, Player player) {
        if (super.clayBrushEffect(mode, itemInHand, player)) {
            return true;
        }

        if (mode == ClayBrushItem.Mode.WORK) {
            if (!level().isClientSide && player instanceof ServerPlayer serverPlayer) {
                if (this.getAttackType().canWork()) {
                    workSelector.cycleWorkMode();
                    serverPlayer.sendSystemMessage(workSelector.getWorkDisplayName(), true);
                } else {
                    serverPlayer.sendSystemMessage(getCombatDisplayName(), true);
                }
            }
        }
        return true;
    }

    // Pick Up
    @Override
    public boolean canHoldItem(ItemStack pStack) {
        if (!level().isClientSide && workSelector.workRequiresItemPickUp()) {
            return true;
        }
        return pStack.is(ModTags.Items.SOLDIER_HOLDABLE);
    }

    @Override
    public boolean wantsToPickUp(ItemStack pStack) {
        return canHoldItem(pStack);
    }

    @Override
    @NotNull
    public ItemStack equipItemIfPossible(ItemStack pStack) {
        return equipItemIfPossible(pStack, SoldierSlotCallback.NULL);
    }

    /**
     * Equip the give {@code ItemStack} if possible and return the equip one.
     *
     * @param stack        the {@code ItemStack} to equip.
     * @param slotCallBack callback in which {@code SoldierEquipmentSlot} the {@code ItemStack} was equipped.
     * @return the equipped {@code ItemStack}.
     */
    public ItemStack equipItemIfPossible(ItemStack stack, SoldierSlotCallback slotCallBack) {
        var equipped = equipSoldierItem(stack, slotCallBack);
        if (equipped.isEmpty()) {
            return equipGoalItem(stack, slotCallBack);
        }
        return equipped;
    }

    private ItemStack equipSoldierItem(ItemStack pStack, SoldierSlotCallback slotCallBack) {
        ItemStackWithEffect newItem = new ItemStackWithEffect(pStack);
        SoldierHoldableEffect effect = newItem.effect();
        if (effect == null) {
            return ItemStack.EMPTY;
        }
        if (!couldEquipStack(effect)) {
            return ItemStack.EMPTY;
        }

        List<SoldierEquipmentSlot> possibleSlots = effect.slots();

        if (possibleSlots.isEmpty()) {
            var capEquip = effect.getCustomEquipCapability(pStack);
            ItemStack resultStack;
            if (capEquip != null) {
                resultStack = capEquip.equip(this);
            } else {
                resultStack = ItemStack.EMPTY;
            }
            slotCallBack.capability();
            return resultStack;
        }

        ItemStackWithEffect currentItem = null;
        boolean canReplaceCurrentItem = false;
        SoldierEquipmentSlot equipmentSlot = null;

        for (SoldierEquipmentSlot slot : possibleSlots) {
            equipmentSlot = slot;
            currentItem = this.getItemBySlot(equipmentSlot);
            canReplaceCurrentItem = currentItem.canBeReplaced(newItem);
            if (canReplaceCurrentItem) {
                break;
            }
        }

        if (canReplaceCurrentItem && this.canHoldItem(pStack)) {
            if (!currentItem.isEmpty()) {
                this.dropItemStackWithChance(currentItem);
            }
            ItemStack copied = pStack;
            if (pStack.getCount() > effect.getMaxStackSize()) {
                copied = pStack.copyWithCount(effect.getMaxStackSize());
            }

            this.setItemSlot(equipmentSlot, copied);
            slotCallBack.slot(equipmentSlot);
            effect.executePickUpFunctions(this, copied);
            return copied;
        } else {
            return ItemStack.EMPTY;
        }
    }

    private ItemStack equipGoalItem(ItemStack stack, SoldierSlotCallback slotCallback) {
        if (workSelector.workRequiresItemPickUp() && carriedStack.isEmpty()) {
            slotCallback.carried();
            setCarriedStack(stack);
            return stack;
        } else {
            return ItemStack.EMPTY;
        }
    }

    @Override
    public boolean couldEquipStack(@NotNull SoldierHoldableEffect effect) {
        var attackTypeSelf = getAttackType();
        var attackTypeStack = effect.properties().attackType();
        if (!attackTypeSelf.compatibleWith(attackTypeStack)) {
            return false;
        }

        if (effect.throwable()) {
            if (attackTypeStack.isSupportive()) {
                return effect.throwableType(RangedAttackType.HELPING);
            }
            if (attackTypeStack.fightsBack()) {
                return effect.throwableType(RangedAttackType.HARM);
            }
        }

        return true;
    }

    @Override
    public ItemEntity dropItemStack(ItemStack stack) {
        return this.spawnAtLocation(stack);
    }

    // Items
    public ItemStackWithEffect getItemBySlot(SoldierEquipmentSlot pSlot) {
        return switch (pSlot.getType()) {
            case HAND -> this.soldierHandItems.get(pSlot.getIndex());
            case ARMOR -> this.soldierArmorItems.get(pSlot.getIndex());
            case BACKPACK -> this.soldierBackpackItems.get(pSlot.getIndex());
        };
    }

    @Override
    public void setItemSlot(SoldierEquipmentSlot pSlot, ItemStack pStack) {
        setItemSlot(pSlot, new ItemStackWithEffect(pStack));
    }

    @Override
    public void setItemSlot(SoldierEquipmentSlot pSlot, ItemStackWithEffect pStack) {
        verifyEquippedItem(pStack.stack());
        switch (pSlot.getType()) {
            case HAND -> this.soldierHandItems.set(pSlot.getIndex(), pStack);
            case ARMOR -> this.soldierArmorItems.set(pSlot.getIndex(), pStack);
            case BACKPACK -> this.soldierBackpackItems.set(pSlot.getIndex(), pStack);
        }
        handleSlotChange(pSlot, pStack);
    }

    private void handleSlotChange(SoldierEquipmentSlot slot, ItemStackWithEffect stackWithEffect) {
        if (!level().isClientSide()) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingEntity(this, new SoldierItemChangePayload(this.getId(), slot, stackWithEffect.stack()));
        }
        updateSpecialProperties(stackWithEffect, slot);
        updateCombinedProperties(slot, stackWithEffect.effect());
        updateOtherProperties();
    }

    private void updateCombinedProperties(SoldierEquipmentSlot slot, @Nullable SoldierHoldableEffect effect) {
        if (effect != null) {
            propertyCombiner.addProperty(effect.predicate(), effect.properties(), slot);
        } else {
            propertyCombiner.removeProperty(slot);
        }
        propertyCombiner.addPropertyFromTeam(getClayTeam());
        propertyCombiner.combine();
    }

    private void initCombinedProperties() {
        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            var effect = getItemBySlot(slot).effect();
            if (effect != null) {
                propertyCombiner.addProperty(effect.predicate(), effect.properties(), slot);
            }
        }
        if (!this.level().isClientSide()) {
            this.level().broadcastEntityEvent(this, TEAM_CHANGE_EVENT);
        }

        propertyCombiner.addPropertyFromTeam(getClayTeam());
        propertyCombiner.combine();
    }

    /**
     * Use {@link #setItemSlot(SoldierEquipmentSlot, ItemStack)}
     */
    @Override
    @Deprecated
    public void setItemSlot(EquipmentSlot pSlot, ItemStack pStack) {
        SoldierEquipmentSlot.getFromSlot(pSlot).ifPresent(soldierSlot -> setItemSlot(soldierSlot, pStack));
    }

    /**
     * Use {@link AbstractClaySoldierEntity#getItemBySlot(SoldierEquipmentSlot)}
     */
    @Override
    @Deprecated
    @NotNull
    public ItemStack getItemBySlot(@NotNull EquipmentSlot pSlot) {
        return SoldierEquipmentSlot.getFromSlot(pSlot).map(this::getItemBySlot).orElse(ItemStackWithEffect.EMPTY).stack();
    }

    @Override
    public Iterable<ItemStack> getHandSlots() {
        return convertToStack(soldierHandItems);
    }

    @Override
    public Iterable<ItemStack> getArmorSlots() {
        return convertToStack(soldierArmorItems);
    }

    public Iterable<ItemStack> getBackpackSlots() {
        return convertToStack(soldierBackpackItems);
    }

    @Override
    public Iterable<ItemStack> getAllSlots() {
        return Iterables.concat(this.getHandSlots(), this.getArmorSlots(), this.getBackpackSlots());
    }

    public Iterable<ItemStackWithEffect> getAllSlotsWithEffect() {
        return Iterables.concat(soldierHandItems, soldierArmorItems, soldierBackpackItems);
    }

    @Override
    public SoldierPropertyMapReader allProperties() {
        return propertyCombiner;
    }

    // Shoot

    private void updateSpecialProperties(ItemStackWithEffect effect, SoldierEquipmentSlot slot) {
        if (effect.isThrowable()) {
            stackWithProjectile = effect;
        }

        if (stackWithProjectile == null) {
            querySpecialProperties();
        }
    }

    private void querySpecialProperties() {
        for (ItemStackWithEffect stackWithEffect : getAllSlotsWithEffect()) {
            if (stackWithEffect.isThrowable()) {
                stackWithProjectile = stackWithEffect;
            }
        }
    }

    /**
     * Returns whether this soldier can perform a ranged attack.
     *
     * @return whether this soldier can perform a ranged attack
     */
    public boolean canPerformRangeAttack() {
        return stackWithProjectile != null;
    }

    @Override
    public void performRangedAttack(@NotNull LivingEntity pTarget, float pVelocity) {
        if (stackWithProjectile == null) {
            return;
        }
        ThrowableItemCapability capability = stackWithProjectile.getThrowableCap();
        var holdableEffect = stackWithProjectile.effect();
        if (holdableEffect == null) {
            stackWithProjectile = null;
            return;
        }
        if (capability == null) {
            capability = new ThrowableItemCapability() {
                @Override
                public @NotNull Projectile createProjectile(Level level, LivingEntity shooter, SoldierHoldableEffect holdableEffect) {
                    var pr = new ClaySoldierThrowableItemEntity(level, shooter, stackWithProjectile);
                    pr.setItem(stackWithProjectile.stack());
                    return pr;
                }
            };
        }

        capability.performRangedAttack(this, this.level(), pTarget, holdableEffect, pVelocity);
        holdableEffect.getRemovalConditions().forEach(condition -> {
            if (condition.shouldRemove(this, RemovalConditionContext.useRanged(stackWithProjectile.stack()))) {
                if (stackWithProjectile.shrink(1) <= 0) {
                    stackWithProjectile = ItemStackWithEffect.EMPTY;
                }
            }
        });
    }

    /**
     * Called when the ClaySoldier performs a {@link ClaySoldierMeleeGoal melee action} and test whether any
     * Items should be removed.
     */
    public void indicateMeleeItemUse() {
        testForRemoval(RemovalConditionContext::useMelee, false);
    }

    private void testForRemoval(Function<ItemStack, RemovalConditionContext> contextBuilder, boolean onlyOne) {
        if (level().isClientSide()) {
            return;
        }

        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            var stack = getItemBySlot(slot);
            var effect = stack.effect();
            if (effect != null) {
                var context = contextBuilder.apply(stack.stack());
                for (RemovalCondition condition : effect.getRemovalConditions()) {
                    if (condition.shouldRemove(this, context)) {
                        setItemSlot(slot, stack.shrink(1) <= 0 ? ItemStackWithEffect.EMPTY : stack);
                        if (onlyOne) {
                            return;
                        }
                    }
                }
            }
        }
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!(source.getEntity() instanceof Player) && source.is(ModTags.DamageTypes.CLAY_SOLDIER_DAMAGE)) {
            if (getHealth() * 3 < getMaxHealth()) {
                if (allProperties().hasEvacuationProperty()) {
                    if (level() instanceof ServerLevel serverLevel) {
                        allProperties().getEvacuationProperty().evacuate(serverLevel, this);
                        testForRemoval(RemovalConditionContext::fireworkRocket, true);
                    }

                    return false;
                }
            }
        }

        testForRemoval(stack -> RemovalConditionContext.hurt(source, stack), false);

        if (source.is(DamageTypes.THROWN) && source.getEntity() instanceof AbstractClaySoldierEntity soldierThrower) {
            performCounterAttacks(soldierThrower, SpecialAttackType.MELEE);
            if (allProperties().canTeleport()) {
                teleportSoldier(soldierThrower);
            }
        }

        if (source.is(DamageTypeTags.IS_EXPLOSION) && !source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount = amount - (amount * Math.clamp(allProperties().explosionResistance() * 0.08f, 0, 1));
        }

        if (!level().isClientSide()) {
            amount = allProperties().damageBlock().blocked(this.random, amount, source.is(DamageTypes.THROWN) || source.is(ModDamageTypes.CLAY_HURT));
        }

        return super.hurt(source, amount);
    }

    // Swim
    public boolean canSwim() {
        return allProperties().canSwim();
    }

    @Override
    public int getMaxAirSupply() {
        int breathHold = allProperties() != null ? allProperties().breathHoldDuration() : 0;
        return Math.max(10, super.getMaxAirSupply() + breathHold * 100);
    }

    @Override
    protected int decreaseAirSupply(int pCurrentAir) {
        if (allProperties().breathHoldDuration() >= BreathHoldPropertyType.MAX_BREATH_HOLD) {
            return pCurrentAir;
        } else if (allProperties().breathHoldDuration() <= BreathHoldPropertyType.NO_BREATH_HOLD) {
            return super.decreaseAirSupply(pCurrentAir) * 30;
        }
        return super.decreaseAirSupply(pCurrentAir);
    }

    @Override
    protected int increaseAirSupply(int currentAir) {
        return super.increaseAirSupply(Math.min(allProperties().breathHoldDuration(), 0) + currentAir);
    }

    @Override
    public double getDefaultAttackReach() {
        float blindnessMultiplier = 1f;
        var blindnessEffect = getMobEffect(MobEffects.BLINDNESS);
        if (blindnessEffect != null) {
            blindnessMultiplier = (calcBlindnessReach(blindnessEffect.getAmplifier()));
        }
        return (DEFAULT_ATTACK_REACH * getSoldierSize() + allProperties().bonusAttackRange()) * blindnessMultiplier;
    }

    private static float calcBlindnessReach(int amp) {
        return 1f - (float) ((-Math.pow(2f, -(amp + 1f))) + 1f);
    }

    // Passive
    private void updateOtherProperties() {
        if (allProperties() != null) {
            if (allProperties().canSwim()) {
                setPathfindingMalus(PathType.WATER, 0);
                setPathfindingMalus(PathType.WATER_BORDER, 0);
                this.navigation = waterNavigation;
            } else {
                setPathfindingMalus(PathType.WATER, PathType.WATER.getMalus());
                setPathfindingMalus(PathType.WATER_BORDER, PathType.WATER_BORDER.getMalus());
                this.navigation = groundNavigation;
            }
        }
    }

    @Override
    public boolean isCurrentlyGlowing() {
        return super.isCurrentlyGlowing() || allProperties().hasGlowOutline();
    }

    @Override
    public boolean sameTeamAs(Entity other) {
        final AttackTypeProperty attackType = getAttackType();
        if (attackType == AttackTypeProperty.AGGRESSIVE) {
            return false;
        }
        if (other instanceof AbstractClaySoldierEntity claySoldier) {
            if (attackType.isRoyalty()) {
                if (claySoldier.getAttackType() == attackType) {
                    return false;
                }
            } else if (claySoldier.getAttackType() == AttackTypeProperty.AGGRESSIVE) {
                return false;
            }
        }

        return super.sameTeamAs(other);
    }

    public boolean canSeeInvis() {
        return allProperties().canSeeInvis();
    }

    @Override
    public double getVisibilityPercent(@Nullable Entity pLookingEntity) {
        if (pLookingEntity instanceof AbstractClaySoldierEntity claySoldier && claySoldier.canSeeInvis()) {
            return 1f;
        }
        return super.getVisibilityPercent(pLookingEntity);
    }

    @Override
    protected void updateInvisibilityStatus() {
        super.updateInvisibilityStatus();
    }

    // Explode
    private void explodeTick() {
        this.oldSwell = this.swell;
        if (this.isIgnited()) {
            this.setSwellDir(1);
        }

        int swellDir = this.getSwellDir();
        if (swellDir > 0 && this.swell == 0) {
            this.playSound(SoundEvents.CREEPER_PRIMED, 1.0F, 0.5F);
            this.gameEvent(GameEvent.PRIME_FUSE);
        }

        this.swell += swellDir;
        if (this.swell < 0) {
            this.swell = 0;
        }

        if (this.swell >= this.maxSwell) {
            this.swell = this.maxSwell;
            this.explodeSoldier();
        }

    }

    @Override
    public void die(DamageSource damageSource) {
        if (ClaySoldiersCommon.COMMON_HOOKS.onLivingDeath(this, damageSource)) return;
        workSelector.resetGoal();

        if (!this.isRemoved() && !this.dead) {

            if (allProperties().hasPropertyType(SoldierPropertyTypes.DEATH_EXPLOSION.get())) {
                ignite();
                if (swell < maxSwell) {
                    return;
                }
            } else {
                spawnDeathEffect();
                if (level().isClientSide()) {
                    this.spawnFireWorks();
                }

            }
            WraithProperty wraithProperty = allProperties().wraith();

            Entity entity = damageSource.getEntity();
            LivingEntity livingentity = this.getKillCredit();
            if (this.deathScore >= 0 && livingentity != null) {
                livingentity.awardKillScore(this, this.deathScore, damageSource);
            }

            if (this.isSleeping()) {
                this.stopSleeping();
            }

            if (!this.level().isClientSide && this.hasCustomName()) {
                LOGGER.info("Named entity {} died: {}", this, this.getCombatTracker().getDeathMessage().getString());
            }
            this.dead = true;

            this.getCombatTracker().recheckStatus();
            if (level() instanceof ServerLevel serverlevel) {
                if (entity == null || entity.killedEntity(serverlevel, this)) {
                    ReviveResult reviveResult = reviveSelf(serverlevel);
                    if (reviveResult.dropInventory()) {
                        this.dropAllDeathLoot(serverlevel, damageSource);
                    }
                    this.gameEvent(GameEvent.ENTITY_DIE);
                    this.createWitherRose(livingentity);
                    if (reviveResult == ReviveResult.FAIL && wraithProperty != null) {
                        ClayWraithEntity.spawnWraith(serverlevel, this, wraithProperty.duration(), wraithProperty.onSpawnEffect());
                    }
                }

                this.level().broadcastEntityEvent(this, (byte) 3);
            }

            this.setPose(Pose.DYING);
        }
    }

    protected boolean dropInventoryOnDeath() {
        return true;
    }

    @Override
    protected void dropCustomDeathLoot(ServerLevel level, DamageSource pSource, boolean pRecentlyHit) {
        super.dropCustomDeathLoot(level, pSource, pRecentlyHit);
        if (dropInventoryOnDeath()) {
            /*for (SoldierEquipmentSlot slots : SoldierEquipmentSlot.values()) {
                ItemStackWithEffect stackWithEffect = this.getItemBySlot(slots);
                if (!stackWithEffect.isEmpty() && !EnchantmentHelper.has(stackWithEffect.stack(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP)) {
                    if (this.dropItemStackWithChance(stackWithEffect) != null) {
                        this.setItemSlot(slots, ItemStackWithEffect.EMPTY);
                    }
                }
            }*/
            dropInventory(level, this::getItemBySlot, (slot, stack) -> {
                this.spawnAtLocation(stack);
                this.setItemSlot(slot, ItemStackWithEffect.EMPTY);
            });
            if (!getCarriedStack().isEmpty()) {
                this.dropItemStack(getCarriedStack());
            }
        }
    }

    public static void dropInventory(ServerLevel level, Function<SoldierEquipmentSlot, ItemStackWithEffect> equipment, BiConsumer<SoldierEquipmentSlot, ItemStack> dropInWorld) {
        if (!level.getGameRules().getBoolean(ClaySoldiersCommon.CLAY_SOLDIER_INVENTORY_DROP_RULE)) {
            return;
        }

        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            var stack = equipment.apply(slot);
            if (!stack.isEmpty() && !EnchantmentHelper.has(stack.stack(), EnchantmentEffectComponents.PREVENT_EQUIPMENT_DROP) && level.getRandom().nextFloat() < stack.dropRate()) {
                dropInWorld.accept(slot, stack.stack());
            }
        }
    }

    @Override
    protected void tickDeath() {
        if (isIgnited() && swell <= maxSwell) {
            explodeTick();
            return;
        }

        ++this.deathTime;
        if (this.deathTime >= 20 && !this.level().isClientSide() && !this.isRemoved()) {
            this.level().broadcastEntityEvent(this, (byte) 60);
            this.remove(RemovalReason.KILLED);
        }
    }

    /**
     * @param pPartialTicks: Render tick.
     * @return Returns the intensity of the soldier's flash when it is ignited.
     */
    public float getSwelling(float pPartialTicks) {
        return Mth.lerp(pPartialTicks, (float) this.oldSwell, (float) this.swell) / (float) (this.maxSwell - 2);
    }

    /**
     * @return the current state of soldier, -1 is idle, 1 is 'in fuse'
     */
    public int getSwellDir() {
        return this.entityData.get(DATA_SWELL_DIR);
    }

    /**
     * Sets the state of soldier, -1 to idle and 1 to be 'in fuse'
     */
    public void setSwellDir(int pState) {
        this.entityData.set(DATA_SWELL_DIR, pState);
    }

    private void explodeSoldier() {
        if (!this.level().isClientSide) {
            level().broadcastEntityEvent(this, EXPLODE_FIREWORK_EVENT);
            float explosionPower = allProperties().getValueOrDfault(SoldierPropertyTypes.DEATH_EXPLOSION);
            this.dead = true;
            this.level().explode(this, this.getX(), this.getY(), this.getZ(), this.explosionRadius * explosionPower, Level.ExplosionInteraction.NONE);
            this.discard();
            this.spawnDeathEffect();
        }
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EXPLODE_FIREWORK_EVENT) {
            this.spawnFireWorks();
        } else {
            super.handleEntityEvent(id);
        }
    }

    public boolean isIgnited() {
        return this.entityData.get(DATA_IS_IGNITED);
    }

    public void ignite() {
        this.entityData.set(DATA_IS_IGNITED, true);
    }

    private void spawnDeathEffect() {
        if (level().isClientSide()) {
            return;
        }

        List<MobEffectInstance> collection = allProperties().getDeathCloudEffects();
        if (!collection.isEmpty()) {
            AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level(), this.getX(), this.getY(), this.getZ());
            areaeffectcloud.setRadius(1.5F);
            areaeffectcloud.setRadiusOnUse(-0.5F);
            areaeffectcloud.setWaitTime(10);
            areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
            areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float) areaeffectcloud.getDuration());

            for (MobEffectInstance mobeffectinstance : collection) {
                areaeffectcloud.addEffect(new MobEffectInstance(mobeffectinstance));
            }

            this.level().addFreshEntity(areaeffectcloud);
        }

    }

    private void spawnFireWorks() {
        List<FireworkExplosion> fireworkExplosions = new ArrayList<>();
        for (ItemStack stack : getAllSlots()) {
            var exp = stack.get(DataComponents.FIREWORK_EXPLOSION);
            if (exp != null) {
                fireworkExplosions.add(exp);
            }
        }
        if (!fireworkExplosions.isEmpty()) {
            level().createFireworks(this.getX(), this.getY(), this.getZ(), 0, 1, 0, fireworkExplosions);
        }

    }

    // Scale

    /**
     * Returns the size increase of the {@code ClaySoldier} from {@code SoldierProperties}.
     * Should not be used for visual scale. Use {@link #getScale()} instead.
     */
    public float getSoldierSize() {
        if (allProperties() == null) {
            return 1f;
        }
        return Math.clamp(0.2f, allProperties().getSoldierSize(), 3f);
    }

    /**
     * Returns the visual scale of this {@code ClaySoldier}.
     * This includes the {@link Attributes#SCALE} and {@link #getSoldierSize() SoldierSize}.
     */
    @Override
    public float getScale() {
        if (!level().isClientSide()) {
            return super.getScale() * this.getSoldierSize();
        }
        return delayedScale * super.getScale();
    }

    // Healer
    public boolean fightsBack() {
        return getAttackType().fightsBack();
    }

    @Override
    protected boolean targetPredicate(LivingEntity target) {
        return getAttackType().canAttack(this, target);
    }

    /**
     * A target predicate to test if a target meets special conditions.
     * The normal {@link #targetPredicate targetPredicate} has always been true already.
     * For example: Used to not apply supportive Effects from special attacks to only the same entity.
     * However, soldier will keep attacking the same target and switch of until it dies.
     */
    protected boolean specificTargetPredicate(LivingEntity target) {
        if (canPerformRangeAttack()) {
            return allProperties().specialAttacks(SpecialAttackType.RANGED, getAttackType().forType()).stream().anyMatch(s -> s.shouldAttackTarget(target));
        }
        return allProperties().specialAttacks(SpecialAttackType.MELEE, getAttackType().forType()).stream().anyMatch(s -> s.shouldAttackTarget(target));
    }

    @Override
    public AttackTypeProperty getAttackType() {
        if (allProperties() != null) {
            var attackType = allProperties().attackType();
            return attackType == AttackTypeProperty.NORMAL ? defaultAttackType : attackType;
        }
        return defaultAttackType;
    }


    // Cape

    @Override
    public void tick() {
        super.tick();
        if (level().isClientSide()) {
            moveCloak(this.getX(), this.getY(), this.getZ());
            delaySizeScale();
        }
    }

    private void delaySizeScale() {
        float actualSize = getSoldierSize();
        if (delayedScale == actualSize) {
            return;
        }
        if (delayedScale < actualSize) {
            delayedScale += 0.1f;
            if (delayedScale > actualSize) {
                delayedScale = actualSize;
            }
        } else {
            delayedScale -= 0.1f;
            if (delayedScale < actualSize) {
                delayedScale = actualSize;
            }
        }
    }

    @Override
    public void rideTick() {
        if (level().isClientSide()) {
            this.oBob = this.bob;
            this.bob = 0.0F;
        }
        matchBodyRotToRidden();

        super.rideTick();
    }

    private void matchBodyRotToRidden() {
        if (this.getRidingPose().shouldMatchBodyRot()) {
            if (getVehicle() instanceof LivingEntity entity) {
                this.yBodyRot = entity.yBodyRot;
            }
        }
    }

    @Override
    public void aiStep() {
        if (level().isClientSide()) {
            this.oBob = this.bob;

            float movementSpeed;
            if (this.onGround() && !this.isDeadOrDying() && !this.isSwimming()) {
                movementSpeed = Math.min(0.1F, (float) this.getDeltaMovement().horizontalDistance());
            } else {
                movementSpeed = 0.0F;
            }
            this.bob += (movementSpeed - this.bob) * 0.4F;
        } else {
            if (this.tickCount % 20 == 0) {
                if (this.getVehicle() instanceof Mob living) {
                    living.setTarget(this.getTarget());
                }
            }
            if (tickCount % 100 == 0) {
                if (this.getVehicle() instanceof Mob vehicle) {
                    if (!canRideEntity(vehicle)) {
                        this.stopRiding();
                    }
                }
            }
            Vec3 movement = this.getDeltaMovement();
            if (isFalling() && allProperties().canGlide()) {
                this.setDeltaMovement(movement.multiply(1.0, 0.7, 1.0));
            }

            dropCarriedTick();
        }
        updateSwingTime();

        super.aiStep();
    }

    protected void moveCloak(double x, double y, double z) {
        this.xCloakO = this.xCloak;
        this.yCloakO = this.yCloak;
        this.zCloakO = this.zCloak;
        double deltaX = x - this.xCloak;
        double deltaY = y - this.yCloak;
        double deltaZ = z - this.zCloak;
        if (deltaX > MAGIC_CAPE_NUMBER) {
            this.xCloak = this.getX();
            this.xCloakO = this.xCloak;
        }

        if (deltaZ > MAGIC_CAPE_NUMBER) {
            this.zCloak = z;
            this.zCloakO = this.zCloak;
        }

        if (deltaY > MAGIC_CAPE_NUMBER) {
            this.yCloak = y;
            this.yCloakO = this.yCloak;
        }

        if (deltaX < -MAGIC_CAPE_NUMBER) {
            this.xCloak = x;
            this.xCloakO = this.xCloak;
        }

        if (deltaZ < -MAGIC_CAPE_NUMBER) {
            this.zCloak = z;
            this.zCloakO = this.zCloak;
        }

        if (deltaY < -MAGIC_CAPE_NUMBER) {
            this.yCloak = y;
            this.yCloakO = this.yCloak;
        }

        this.xCloak += deltaX * 0.25;
        this.zCloak += deltaZ * 0.25;
        this.yCloak += deltaY * 0.25;
    }

    // Sync to client on spawn
    public void handleSpawnPayload(ClaySoldierSpawnPayload payload) {
        setItemSlot(SoldierEquipmentSlot.CAPE, payload.getCape());
        setItemSlot(SoldierEquipmentSlot.BACKPACK, payload.getBackpack1());
        setItemSlot(SoldierEquipmentSlot.BACKPACK_PASSIVE, payload.getBackpack2());

        for (ReviveType type : ReviveType.values()) {
            setReviveOnCooldown(type, payload.getReviveCooldowns().get(type.ordinal()));
        }
        skinVariantId = payload.getSkinId();
        setCarriedStack(payload.getCarried());
        initCombinedProperties();

        setDataWorkStatus(payload.getWorkStatus());
    }

    @Override
    public void sendSpawnPayload(ServerPlayer tracking) {
        ClaySoldiersCommon.NETWORK_MANGER.sendToPlayer(tracking, new ClaySoldierSpawnPayload(this));
    }

    // Riding
    @Override
    public boolean canMountEntity(LivingEntity livingEntity) {
        if (!livingEntity.getPassengers().isEmpty()) {
            return false;
        }
        return canRideEntity(livingEntity);
    }

    public boolean canRideEntity(LivingEntity livingEntity) {
        return ClaySoldierRideableMap.test(livingEntity, this);
    }

    @Override
    public boolean isAbleToRide() {
        return !this.isPassenger() && this.getSoldierSize() <= 1.3f;
    }

    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity pEntity, EntityDimensions pDimensions, float pPartialTick) {
        return super.getPassengerAttachmentPoint(pEntity, pDimensions, pPartialTick).add(0, 0.02 * getScale(), 0);
    }

    @Override
    public @Nullable LivingEntity getControllingPassenger() {
        LivingEntity controller = super.getControllingPassenger();

        return controller instanceof ClayMobEntity ? null : controller;
    }

    // Armor Protection
    @Override
    public float getCustomArmorValue() {
        return allProperties().protection();
    }

    public float getCustomArmorToughness() {
        return allProperties().heavy();
    }

    @Override
    protected float getDamageAfterArmorAbsorb(DamageSource pDamageSource, float pDamageAmount) {
        if (!pDamageSource.is(DamageTypeTags.BYPASSES_ARMOR)) {
            this.hurtArmor(pDamageSource, pDamageAmount);
            pDamageAmount = CombatRules.getDamageAfterAbsorb(this,
                    pDamageAmount, pDamageSource, this.getArmorValue(),
                    (float) this.getAttributeValue(Attributes.ARMOR_TOUGHNESS) + getCustomArmorToughness());
        }
        return pDamageAmount;
    }

    @Override
    public int getArmorValue() {
        return super.getArmorValue() + (int) getCustomArmorValue();
    }




    public boolean hasShieldInHand(InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND) {
            return getItemBySlot(SoldierEquipmentSlot.MAINHAND).isShield();
        } else {
            return getItemBySlot(SoldierEquipmentSlot.OFFHAND).isShield();
        }
    }

    @Override
    public int getTeamColor() {
        return getClayTeam().getColor(this, 0);
    }

    @Override
    public void setOffsetColor(ColorHelper color) {
        colorGetter.setColor(color);
    }

    @Override
    public void addOffsetColor(ColorHelper color) {
        dyeSoldier(color);
    }

    @Override
    public ColorHelper getOffsetColor() {
        return colorGetter.getColor();
    }

    private void dyeSoldier(ColorHelper color) {
        setOffsetColor(getOffsetColor().addColor(color));
    }


    /**
     * Returns whether this soldier can revive other soldiers.
     *
     * @return whether this soldier can revive other soldiers.
     */
    public boolean canRevive() {
        return reviveProperty().canRevive();
    }

    /**
     * Returns whether this soldier can be revived.
     *
     * @return whether this soldier can be revived
     */
    public boolean canBeRevived() {
        return getAttackType().canBeRevived();
    }

    private ReviveProperty reviveProperty() {
        return allProperties().reviveType();
    }

    protected ReviveResult reviveSelf(ServerLevel serverLevel) {
        if (canBeRevived()) {
            var iterator = findReviver(new AABB(this.blockPosition()).inflate(4d));
            while (iterator.hasNext()) {
                var reviver = iterator.next();
                ReviveResult result = reviver.reviveProperty().reviveSoldier(serverLevel, this, reviver);
                if (result.success()) {
                    return result;
                }
            }
        }

        return ReviveResult.FAIL;
    }

    private Iterator<AbstractClaySoldierEntity> findReviver(AABB area) {
        return level().getEntitiesOfClass(AbstractClaySoldierEntity.class, area, AbstractClaySoldierEntity::canRevive).stream().filter(s -> !s.equals(this)).sorted(this::createReviveComparator).iterator();
    }

    private int createReviveComparator(AbstractClaySoldierEntity o1, AbstractClaySoldierEntity o2) {
        return (int) ((o2.distanceTo(this) + reviveValue(o2)) - (o1.distanceTo(this) + reviveValue(o1)));
    }

    private int reviveValue(AbstractClaySoldierEntity soldier) {
        return soldier.sameTeamAs(this) ? -1 : 0;
    }

    // Revive Cooldown

    /**
     * Sets a cooldown period for a specific revive type.
     *
     * @param type     the revive type to set on cooldown
     * @param cooldown the duration of the cooldown in ticks
     */
    public void setReviveOnCooldown(ReviveType type, int cooldown) {
        if (cooldown <= 0) {
            return;
        }
        if (!level().isClientSide) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingEntity(this, new ClaySoldierReviveCooldownPayload(this.getId(), type, cooldown));
        }
        reviveTypeCooldown.put(type, level().getGameTime() + cooldown);
    }

    /**
     * Returns whether the given revive type is off cooldown.
     *
     * @param type type to test
     * @return whether the revive type is off cooldown
     */
    public boolean isReviveTypeOffCooldown(ReviveType type) {
        Long timeStamp = reviveTypeCooldown.get(type);
        if (timeStamp == null) {
            return true;
        }
        return timeStamp <= level().getGameTime();
    }

    /**
     * Returns a map of the current cooldown in ticks of each revive type.
     *
     * @return a map of the current cooldown in ticks of each revive type
     */
    public Map<ReviveType, Integer> getReviveTypeCooldown() {
        return reviveTypeCooldown.entrySet().stream()
                .filter(e -> e.getValue() != null)
                .map(e -> Map.entry(e.getKey(), e.getValue() - level().getGameTime()))
                .filter(e -> e.getValue() > 0)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> e.getValue().intValue(),
                        (e1, e2) -> e1,
                        () -> new EnumMap<>(ReviveType.class)
                ));
    }

    /**
     * @return whether this {@code ClaySoldier} is considered a Zombie.
     */
    public boolean isZombie() {
        return getAttackType() == AttackTypeProperty.ZOMBIE;
    }

    @Override
    public ClayMobEntity asClayMob() {
        return this;
    }


    @Override
    public <T extends ClaySoldierInventoryHandler> void copyInventory(T toCopyTo) {
        for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
            var item = getItemBySlot(slot);
            var effect = item.effect();
            if (item.isEmpty()) {
                continue;
            }
            if (effect != null && toCopyTo.couldEquipStack(effect)) {
                toCopyTo.setItemSlot(slot, item.copy());
                effect.executePickUpFunctions(toCopyTo, item.stack());
            } else {
                dropItemSlotWithChance(slot);
            }
        }
    }

    @Override
    protected OptionalInt openMenuScreen(Player player) {
        return ClaySoldiersCommon.COMMON_HOOKS.openMenu(player,
                new SimpleMenuProvider((id, inventory, player1) -> new ClaySoldierMenu(id, inventory, this), getInventoryName()),
                this.getId()
        );
    }

    @Override
    protected Component getInventoryName() {
        var name = Component.empty();
        var attackTypeDisplayName = getAttackType().getAnimatedDisplayName(this);
        if (attackTypeDisplayName != null) {
            name.append(attackTypeDisplayName);
            name.append(CommonComponents.SPACE);
        }
        name.append(getDisplayName());
        return name;
    }

    // Gliding

    @Override
    public boolean causeFallDamage(float pFallDistance, float pMultiplier, DamageSource pSource) {
        if (allProperties().canGlide()) {
            return false;
        }

        return super.causeFallDamage(pFallDistance, pMultiplier, pSource);
    }

    /**
     * Returns the ordinal of the {@code Slot} a Glider is in.
     * Returns {@value NO_GLIDE} If there is no Glider
     *
     * @return the ordinal of the {@code Slot} a Glider is in
     */
    protected byte getGliderItemSlot() {
        if (lastGliderSlot == GLIDE_UNCHECKED) {
            for (SoldierEquipmentSlot slot : SoldierEquipmentSlot.values()) {
                if (getItemBySlot(slot).isGlider()) {
                    lastGliderSlot = (byte) slot.ordinal();
                    return lastGliderSlot;
                }
            }
            lastGliderSlot = NO_GLIDE;
            return lastGliderSlot;
        }
        if (lastGliderSlot >= 0) {
            if (getItemBySlot(SoldierEquipmentSlot.values()[lastGliderSlot]).isGlider()) {
                return lastGliderSlot;
            }
        }
        lastGliderSlot = NO_GLIDE;
        return lastGliderSlot;
    }

    /**
     * Returns whether this soldier is falling with a Glider.
     *
     * @return whether this soldier is falling with a Glider
     */
    public boolean isFallingWithGlider() {
        return isFalling() && getGliderItemSlot() >= 0 && getVehicle() == null;
    }

    /**
     * Returns whether this Soldier is currently falling.
     */
    public boolean isFalling() {
        return !onGround() && getDeltaMovement().y < 0;
    }

    /**
     * Returns {@code true} if this Soldier is falling with a Glider in the given slot.
     *
     * @param slot the slot to check if there is a Glider.
     */
    public boolean isFallingWithGlider(SoldierEquipmentSlot slot) {
        return isFallingWithGlider() && getGliderItemSlot() == (byte) slot.ordinal();
    }

    /**
     * Returns whether the main and offhand are currently occupied by other items.
     */
    public boolean handsOccupied(SoldierEquipmentSlot slot) {
        return isFallingWithGlider() || !getCarriedStack().isEmpty() || getItemBySlot(slot).isShield();
    }

    /**
     * Returns the {@code ItemStack} this soldier can use as a Glider.
     * Returns an {@code EMPTY ItemStack} if there is none
     *
     * @return the Item this soldier can use as a Glider
     */
    public ItemStack getGliderItem() {
        byte slot = getGliderItemSlot();
        if (slot < 0) {
            return ItemStack.EMPTY;
        } else {
            return getItemBySlot(SoldierEquipmentSlot.values()[slot]).stack();
        }
    }

    // Immunity

    @Override
    public boolean canBeAffected(MobEffectInstance effectInstance) {
        if (allProperties().immunity().isImmune(effectInstance.getEffect())) {
            return false;
        }
        return super.canBeAffected(effectInstance);
    }

    @Override
    public boolean addMobEffect(MobEffectInstance pEffectInstance, @Nullable Entity pEntity) {
        return addEffect(pEffectInstance, pEntity);
    }

    @Override
    public @Nullable MobEffectInstance getMobEffect(Holder<MobEffect> pEffect) {
        return getEffect(pEffect);
    }

    @Override
    public boolean removeMobEffect(Holder<MobEffect> pEffect) {
        return removeEffect(pEffect);
    }

    @Override
    public boolean removeEffect(Holder<MobEffect> pEffect) {
        if (allProperties().immunity().isPersistent(pEffect)) {
            return false;
        }
        return super.removeEffect(pEffect);
    }

    // Spawned From AdditionalData

    /**
     * Reads data from the given {@code Tag} when spawned by an {@code Item}.
     */
    @Override
    public void readItemPersistentData(CompoundTag tag) {
    }

    @Override
    protected void tryToSit(@Nullable Player player, boolean sitting) {
        if (player != null && getAttackType().isRoyalty()) {
            level().getEntitiesOfClass(
                    AbstractClaySoldierEntity.class,
                    new AABB(getOnPos()).inflate(5),
                    c -> c != this && c.isOwnedBy(player)).forEach(soldier -> soldier.tryToSit(null, sitting)
            );

        }
        super.tryToSit(player, sitting);
    }

    @Override
    protected void modifyPickResult(ItemStack stack) {
        ClaySoldierSpawnItem.setClayMobTeam(stack, getClayTeamType(), level().registryAccess());
    }

    public RidingPose getRidingPose() {
        var vehicle = getVehicle();
        if (vehicle != null) {
            if (vehicle.getType() == EntityType.RABBIT) {
                return RidingPose.RABBIT;
            }
            if (vehicle.getType() == EntityType.FIREWORK_ROCKET) {
                return RidingPose.FIREWORK;
            }
            if (vehicle.getType() == EntityType.ENDERMITE) {
                return RidingPose.ENDERMITE;
            }
        }
        return RidingPose.DEFAULT;
    }

    @Override
    protected EntityDimensions getDefaultDimensions(Pose pPose) {
        if (Pose.SITTING == pPose && !isPassenger()) {
            return super.getDefaultDimensions(pPose).scale(2f, 0.7f);
        }

        return super.getDefaultDimensions(pPose);
    }


    public enum RidingPose {
        DEFAULT(false),
        FIREWORK(false),
        RABBIT(true),
        ENDERMITE(true);

        private final boolean matchBody;

        RidingPose(boolean matchBody) {
            this.matchBody = matchBody;
        }

        public boolean shouldMatchBodyRot() {
            return matchBody;
        }
    }

    public int getSkinVariant() {
        return skinVariantId;
    }

    public ItemStack getCarriedStack() {
        return carriedStack;
    }

    public void setCarriedStack(ItemStack carriedStack) {
        this.carriedStack = carriedStack;
        if (!level().isClientSide()) {
            ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingEntity(this, new SoldierCarriedChangePayload(this.getId(), carriedStack.getItemHolder()));
        }
    }

    public void dropCarried() {
        this.dropItemStack(this.getCarriedStack().copy());
        this.setCarriedStack(ItemStack.EMPTY);
    }

    public void dropCarriedTick() {
        if (!workSelector.workRequiresItemCarrying()) {
            dropCarried();
        }
    }

    @Override
    public boolean unableToMoveToOwner() {
        return workSelector.shouldStayAtWork() || super.unableToMoveToOwner();
    }

    @Nullable
    @Override
    public Component getWorkStatus() {
        return statusManger.getStatusDisplayName();
    }

    @Override
    public @Nullable AssignablePoiCapability getPoiCapability() {
        return workSelector.isWorking() ? null : super.getPoiCapability();
    }

    public void setDataWorkStatus(byte statusCode) {
        entityData.set(DATA_WORK_STATUS, statusCode);
    }

    public byte getDataWorkStatus() {
        return entityData.get(DATA_WORK_STATUS);
    }


    public Component getCombatDisplayName() {
        if (hasPoiPos()) {
            return Component.translatable(DEFENDING_AREA_LANG);
        } else {
            return Component.translatable(PROTECTING_OWNER_LANG);
        }
    }

    @Override
    public int unpackDynamicColor(ColorHelper color, float partialTicks) {
        return color.getColor(this, 0);
    }

    /**
     * Teleports this Soldier to the given target and set it as it {@link #setTarget new attack target}.
     * If the Soldier has an AttackType of {@link AttackTypeProperty#PACIFIST Pacifist} it will randomly teleport away.
     *
     * @param target to teleport to
     */
    private void teleportSoldier(LivingEntity target) {
        if (getAttackType().fightsBack()) {
            Entity moving = getVehicle();
            if (moving == null) {
                testForRemoval(stack -> RemovalConditionContext.teleportation(stack, RemovalConditionContext.MovementType.TO_TARGET), true);
                moving = this;
            }
            moving.teleportTo(target.getX(), target.getY() + 0.3, target.getZ());
            setTarget(target);

            level().gameEvent(GameEvent.TELEPORT, this.position(), GameEvent.Context.of(this));
            SoundSource soundsource;
            SoundEvent soundevent;

            soundevent = SoundEvents.CHORUS_FRUIT_TELEPORT;
            soundsource = SoundSource.NEUTRAL;

            level().playSound(null, this.getX(), this.getY(), this.getZ(), soundevent, soundsource);
            this.resetFallDistance();

        } else {
            Items.CHORUS_FRUIT.finishUsingItem(Items.CHORUS_FRUIT.getDefaultInstance(), level(), this);
            testForRemoval(stack -> RemovalConditionContext.teleportation(stack, RemovalConditionContext.MovementType.TO_SAFETY), true);
        }
    }

    @Override
    public boolean shouldTryTeleportToOwner() {
        if (!allProperties().canTeleportToOwner()) {
            return false;
        }

        LivingEntity livingentity = this.getClayTeamOwner();
        return livingentity != null && this.distanceToSqr(this.getClayTeamOwner()) >= 144.0;
    }

    @Override
    public boolean tryToTeleportToOwner() {
        if (super.tryToTeleportToOwner()) {
            testForRemoval(stack -> RemovalConditionContext.teleportation(stack, RemovalConditionContext.MovementType.TO_OWNER), true);
            return true;
        }
        return false;
    }

    private boolean inCombat = false;

    /**
     * @return whether this Soldier is in combat
     */
    public boolean isInCombat() {
        return inCombat;
    }

    @Override
    public void onEnterCombat() {
        super.onEnterCombat();
        inCombat = true;
    }

    @Override
    public void onLeaveCombat() {
        super.onLeaveCombat();
        inCombat = false;
    }

    /**
     * @return the {@code WalkAnimationState} of this Soldier
     */
    public WalkAnimationState getWalkAnimation() {
        return walkAnimation;
    }

    @Override
    public List<String> getInfoState() {
        var list = super.getInfoState();
        list.add("Properties: " + allProperties());
        list.add("Status: " + statusManger);
        list.add("WorkSelector: " + workSelector);
        list.add("ReviveCooldown: " + reviveTypeCooldown);
        return list;
    }

    @Override
    public boolean startRiding(Entity entity, boolean force) {
        var result = super.startRiding(entity, force);
        updateRidingProperties();
        ClaySoldierRideableMap.onRide(entity, this);
        return result;
    }

    @Override
    public void stopRiding() {
        super.stopRiding();
        updateRidingProperties();
    }

    private void updateRidingProperties() {
        Entity vehicle = getVehicle();
        SoldierVehicleProperties properties = vehicle != null ? Objects.requireNonNullElse(ClaySoldiersCommon.DATA_MAP.getVehicleProperties(vehicle.getType()), SoldierVehicleProperties.EMPTY) : SoldierVehicleProperties.EMPTY;
        propertyCombiner.addVehicle(properties);
        propertyCombiner.combine();
    }

    public void onBounce() {
        testForRemoval(RemovalConditionContext::bounce, true);
    }

    @Override
    public @NotNull RandomSource getClaySoldierRandom() {
        return getRandom();
    }
}