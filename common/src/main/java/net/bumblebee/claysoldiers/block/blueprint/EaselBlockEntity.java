package net.bumblebee.claysoldiers.block.blueprint;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.blueprint.BlueprintManager;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequest;
import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.bumblebee.claysoldiers.blueprint.templates.BlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.templates.ClientBlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.templates.ServerBlueprintPlan;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModCriterions;
import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.networking.BlueprintPlacePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class EaselBlockEntity extends BlockEntity {
    private static final String INDICATE_TAG_ON_CLIENT = "Client";
    private static final String MIRROR_TAG = "mirror";
    private static final String TEMPLATE_TAG = "template";
    @Nullable
    private BlueprintData data;
    @Nullable
    private BlueprintPlan template;
    private final BlueprintRequestHandler blueprintRequestHandler = new BlueprintRequestHandler() {
        @Override
        public @Nullable BlueprintRequest getRequest(Predicate<BlockPos> canReach) {
            return getBlueprintRequest(canReach);
        }

        @Override
        public boolean doRequest(@Nullable BlueprintRequest request, ClayMobEntity placer) {
            return doBlueprintRequest(request, placer);
        }
    };

    private Mirror mirror = Mirror.NONE;

    public EaselBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ModBlockEntities.EASEL_BLOCK_ENTITY.get(), pPos, pBlockState);
    }

    public List<String> getInfoState() {
        List<String> info = new ArrayList<>(3);
        info.add("Data: " + data);
        info.add("Template: " + template);
        info.add("Settings: " + getTemplateSettings());
        if (level instanceof ServerLevel serverLevel) {
            info.add("Blueprint Cap: " + ClaySoldiersCommon.CAPABILITY_MANGER.createPoiCache(serverLevel, worldPosition));
        }
        return info;
    }

    public void setBlueprintData(@NotNull BlueprintData data) {
        this.data = data;
        if (!level.isClientSide()) {
            this.template = data.createServerPlan().orElseThrow(IllegalArgumentException::new);
        } else {
            this.template = data.createClientPlan().orElseThrow(IllegalArgumentException::new);
        }
        setChanged();
    }

    public void clearBlueprintData() {
        data = null;
        template = null;
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
        return template == null ? List.of() : template.getNeededItems();
    }

    @Override
    protected void saveAdditional(ValueOutput tag) {
        if (!hasLevel()) {
            throw new IllegalStateException("Saving with out level");
        }

        if (data != null) {
            data.save(tag, level.registryAccess());
        } else if (template instanceof ServerBlueprintPlan serverTemplate) {
            serverTemplate.save(tag);
        }
        saveMirror(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        TagValueOutput tag = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, pRegistries);
        if (data != null) {
            data.save(tag, pRegistries);
        }
        if (template != null) {
            template.saveItems(tag);
            template.saveSize(tag);
            template.saveHasStarted(tag);
        }
        tag.putBoolean(INDICATE_TAG_ON_CLIENT, true);
        return tag.buildResult();
    }


    @Override
    protected void loadAdditional(ValueInput tag) {
        super.loadAdditional(tag);
        loadMirror(tag);
        boolean client = tag.getBooleanOr(INDICATE_TAG_ON_CLIENT, false);

        if (level == null) {
            System.out.println("Loading with out level: " + (client ? "Client" : "Server") + " data: " + tag);
            return;
        }
        data = BlueprintData.load(tag, level.registryAccess());
        if (client) {
            template = new ClientBlueprintPlan(tag);
        } else {
            ServerBlueprintPlan.load(tag, level.registryAccess());
        }
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
        return template != null && template.hasStarted();
    }

    public boolean isFinished() {
        return template != null && template.isFinished();
    }

    @Nullable
    public BlueprintTemplateSettings getTemplateSettings() {
        if (template == null) {
            return null;
        }
        return new BlueprintTemplateSettings(template.getSize(), mirror, fromDirection(getFacing()));
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
        if (template == null || template.isFinished()) {
            return null;
        }
        var settings = getTemplateSettings();
        var request = ((ServerBlueprintPlan) template).getRequest((ServerLevel) level, getTemplateBase(settings), getTemplateSettings(), canReachDestination);
        if (request != null && !canReachDestination.test(request.getPos())) {
            request.cancel();
            return null;
        }
        return request;
    }

    private BlockPos getTemplateBase(BlueprintTemplateSettings settings) {
        return worldPosition.offset(
                BlockPos.ZERO.relative(getFacing().getOpposite(), 2).relative(getFacing().getClockWise(), settings.getDistanceToCenter())
        );
    }

    private boolean doBlueprintRequest(@Nullable BlueprintRequest request, ClayMobEntity placer) {
        if (request == null) {
            return false;
        }
        return tryPlacingSoldier(request.getItem().getDefaultInstance(), placer).isSuccess();
    }

    public BlueprintPlan.PlaceResult tryPlacingSoldier(ItemStack item, @Nullable LivingEntity placer) {
        if (template == null) {
            return BlueprintPlan.PlaceResult.NOT_NEEDED;
        }

        var settings = getTemplateSettings();
        var result = template.tryPlacing(level, item,
                getTemplateBase(settings),
                settings
        );

        if (result.isSuccess()) {
            if (!level.isClientSide()) {
                ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingBlockEntity(this, new BlueprintPlacePayload(worldPosition, item.getItem()));
                triggerAdvancement(placer, template.isFinished());
            }
            setChanged();
        }
        return result;
    }

    private void triggerAdvancement(@Nullable LivingEntity placer, boolean isFinished) {
        var key = level.registryAccess().lookupOrThrow(ModRegistries.BLUEPRINTS).getResourceKey(data).orElseThrow(() -> new IllegalStateException("Key for BlueprintData does not exist"));

        if (placer instanceof ServerPlayer serverPlayer) {
            ModCriterions.BLUEPRINT_COMPLETION_TRIGGER.get().trigger(serverPlayer, key, isFinished);
        } else if (placer instanceof ClayMobEntity clayMob && clayMob.getClayTeamOwner() instanceof ServerPlayer serverPlayer) {
            ModCriterions.BLUEPRINT_COMPLETION_TRIGGER.get().trigger(serverPlayer, key, isFinished);
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
}
