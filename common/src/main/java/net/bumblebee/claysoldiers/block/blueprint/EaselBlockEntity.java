package net.bumblebee.claysoldiers.block.blueprint;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.*;
import net.bumblebee.claysoldiers.blueprint.plan.BlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.plan.ServerBlueprintPlan;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.StatInfoDisplay;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModCritirions;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.networking.BlueprintPlacePayload;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EaselBlockEntity extends BlockEntity implements StatInfoDisplay {
    private static final String MIRROR_TAG = "mirror";
    private static final String PLANT_TAG = "blueprint_plan";
    @Nullable
    private BlueprintData data;
    @Nullable
    private BlueprintPlan blueprintPlan;
    private final BlueprintRequestHandler blueprintRequestHandler = new BlueprintRequestHandler() {
        @Override
        public @Nullable BlueprintRequest getRequest(Predicate<BlockPos> canReach) {
            return getBlueprintRequest(canReach);
        }

        @Override
        public BlueprintRequestResult doRequest(@Nullable BlueprintRequest request, ClayMobEntity placer) {
            return doBlueprintRequest(request, placer);
        }
    };

    private Mirror mirror = Mirror.NONE;

    public EaselBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.EASEL_BLOCK_ENTITY.get(), pPos, pBlockState);
    }


    public void setBlueprintData(@NotNull BlueprintData data) {
        this.data = data;
        if (level instanceof ServerLevel serverLevel) {
            this.blueprintPlan = data.createServerPlan(serverLevel).orElseThrow(IllegalArgumentException::new);
        } else {
            this.blueprintPlan = data.createClientPlan().orElseThrow(IllegalArgumentException::new);
        }
        setChanged();
    }

    public void clearBlueprintData() {
        data = null;
        blueprintPlan = null;
        setChanged();
    }

    public ItemStack getBlueprintItem() {
        return BlueprintManager.createBlueprintItem(data, level.registryAccess());
    }


    public boolean hasBlueprintData() {
        return data != null;
    }

    @Nullable
    public BlueprintData getBlueprintData() {
        return data;
    }

    public List<ItemStack> getRequiredItems() {
        return blueprintPlan == null ? List.of() : blueprintPlan.getNeededItems();
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        super.saveAdditional(tag);
        if (!hasLevel()) {
            throw new IllegalStateException("Saving with out level");
        }

        if (data != null) {
            data.save(tag, level.registryAccess());
            if (blueprintPlan instanceof ServerBlueprintPlan serverTemplate) {
                tag.store(PLANT_TAG, BlueprintPlan.CODEC, blueprintPlan.asBuilder(false));
                //serverTemplate.save(tag);
            }
        }

        saveMirror(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        TagValueOutput tag = TagValueOutput.createWithContext(ClaySoldiersCommon.PROBLEM_REPORTER, registries);
        if (data != null) {
            data.save(tag, registries);
        }
        if (blueprintPlan != null) {
            tag.store(PLANT_TAG, BlueprintPlan.CODEC, blueprintPlan.asBuilder(true));
        }
        return tag.buildResult();
    }


    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        loadMirror(tag);

        data = BlueprintData.load(tag, tag.lookup());
        blueprintPlan = tag.read(PLANT_TAG, BlueprintPlan.CODEC).map(s -> s.build(tag.lookup())).orElse(null);
    }


    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void saveMirror(ValueOutput tag) {
        tag.store(MIRROR_TAG, Mirror.CODEC, mirror);
    }

    private void loadMirror(ValueInput tag) {
        this.mirror = tag.read(MIRROR_TAG, Mirror.CODEC).orElse(Mirror.NONE);
    }

    public boolean cycleMirror() {
        if (hasStarted()) {
            return false;
        }
        this.mirror = mirror == Mirror.NONE ? Mirror.FRONT_BACK : Mirror.NONE;
        return true;
    }

    public Mirror getMirror() {
        return mirror;
    }

    public boolean hasStarted() {
        return blueprintPlan != null && blueprintPlan.hasStarted();
    }

    public boolean isFinished() {
        return blueprintPlan != null && blueprintPlan.isFinished();
    }

    @Nullable
    public BlueprintTemplateSettings getTemplateSettings() {
        if (blueprintPlan == null) {
            return null;
        }
        return new BlueprintTemplateSettings(blueprintPlan.getSize(), mirror, fromDirection(getFacing()));
    }

    public Direction getFacing() {
        return getBlockState().getValue(EaselBlock.FACING);
    }

    private static Rotation fromDirection(Direction direction) {
        return switch (direction) {
            case NORTH -> Rotation.NONE;
            case EAST -> Rotation.CLOCKWISE_90;
            case SOUTH -> Rotation.CLOCKWISE_180;
            case WEST -> Rotation.COUNTERCLOCKWISE_90;
            default ->
                    throw new IllegalStateException("Easel Block Entity should never have a BlockState Facing of:" + direction);
        };
    }

    private @Nullable BlueprintRequest getBlueprintRequest(Predicate<BlockPos> canReachDestination) {
        if (blueprintPlan == null || blueprintPlan.isFinished()) {
            return null;
        }
        var settings = getTemplateSettings();
        var request = ((ServerBlueprintPlan) blueprintPlan).getRequest((ServerLevel) level, getTemplateBase(settings), getTemplateSettings(), canReachDestination);
        /*if (request != null && !canReachDestination.test(request.getPos())) {
            request.cancel();
            return null;
        }*/
        return request;
    }

    private BlockPos getTemplateBase(BlueprintTemplateSettings settings) {
        return worldPosition.offset(
                BlockPos.ZERO.relative(getFacing().getOpposite(), 2).relative(getFacing().getClockWise(), settings.getDistanceToCenter())
        );
    }

    private BlueprintRequestResult doBlueprintRequest(@Nullable BlueprintRequest request, ClayMobEntity placer) {
        if (request == null) {
            return BlueprintRequestResult.fail();
        }
        return tryPlacingSoldier(request.getItem().getDefaultInstance(), placer);
    }

    public BlueprintRequestResult tryPlacingSoldier(ItemStack item, @Nullable LivingEntity placer) {
        if (blueprintPlan == null) {
            return BlueprintRequestResult.fail();
        }

        var settings = getTemplateSettings();
        var result = blueprintPlan.tryPlacing(level, item,
                getTemplateBase(settings),
                settings
        );

        if (result.isSuccess()) {
            if (!level.isClientSide()) {
                ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingBlockEntity(this, new BlueprintPlacePayload(worldPosition, item.getItem()));
                triggerAdvancement(placer, blueprintPlan.isFinished());
            }
            setChanged();
        }
        return result;
    }

    private void triggerAdvancement(@Nullable LivingEntity placer, boolean isFinished) {
        ResourceKey<BlueprintData> key = placer.registryAccess().lookupOrThrow(ModRegistries.BLUEPRINTS).getResourceKey(data).orElseThrow(() -> new IllegalStateException("Key for BlueprintData does not exist"));

        if (placer instanceof ServerPlayer serverPlayer) {
            ModCritirions.BLUEPRINT_COMPLETION_TRIGGER.get().trigger(serverPlayer, key, isFinished);
        } else if (placer instanceof ClayMobEntity clayMob && clayMob.getClayTeamOwner() instanceof ServerPlayer serverPlayer) {
            ModCritirions.BLUEPRINT_COMPLETION_TRIGGER.get().trigger(serverPlayer, key, isFinished);
        }
    }

    public BlueprintRequestHandler getBlueprintRequestHandler() {
        return blueprintRequestHandler;
    }

    @Override
    public String toString() {
        String levelName;
        if (level != null) {
            levelName = level.isClientSide() ? "Client" : "Server";
        } else {
            levelName = "Null";
        }
        return "EaselBlockEntity(%s, %s)".formatted(levelName, getBlockPos());
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        if (data != null) {
            list.add(Component.translatable(STRUCTURE, data.getDisplayName()));
            if (blueprintPlan != null) {
                BlueprintTemplateSettings settings = getTemplateSettings();

                list.add(Component.translatable(BLUEPRINT_SETTINGS, settings.structureSize().toShortString(), getMirrorLang(settings.mirror())).withStyle(ChatFormatting.GRAY));
                list.add(Component.translatable(ITEMS_NEEDED).withStyle(ChatFormatting.GRAY));
                blueprintPlan.forEachItemRequired((item, count) -> {
                    Component name = item.components().getOrDefault(DataComponents.ITEM_NAME, CommonComponents.EMPTY);
                    list.add(CommonComponents.space().append(name).append(": " + count + "x").withStyle(ChatFormatting.GRAY));
                });
            }
        } else {
            list.add(Component.translatable(NO_STRUCTURE));
        }
    }

    private static Component getMirrorLang(Mirror mirror) {
        return switch (mirror) {
            case LEFT_RIGHT -> Component.translatable(MIRROR_LEFT_RIGHT);
            case FRONT_BACK -> Component.translatable(MIRROR_FRONT_BACK);
            case null, default -> Component.translatable(MIRROR_NONE);
        };
    }

    public List<String> getInfoState() {
        List<String> info = new ArrayList<>(3);
        info.add("Data: " + data);
        info.add("Template: " + blueprintPlan);
        info.add("Settings: " + getTemplateSettings());
        if (level instanceof ServerLevel serverLevel) {
            info.add("Blueprint Cap: " + ClaySoldiersCommon.CAPABILITY_MANGER.createPoiCache(serverLevel, worldPosition));
        }
        return info;
    }
}
