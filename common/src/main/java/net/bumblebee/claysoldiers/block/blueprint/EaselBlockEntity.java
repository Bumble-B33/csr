package net.bumblebee.claysoldiers.block.blueprint;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.blueprint.BlueprintData;
import net.bumblebee.claysoldiers.blueprint.BlueprintManger;
import net.bumblebee.claysoldiers.blueprint.BlueprintRequest;
import net.bumblebee.claysoldiers.blueprint.BlueprintTemplateSettings;
import net.bumblebee.claysoldiers.blueprint.templates.BlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.templates.ClientBlueprintPlan;
import net.bumblebee.claysoldiers.blueprint.templates.ServerBlueprintPlan;
import net.bumblebee.claysoldiers.capability.BlueprintRequestHandler;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.networking.BlueprintPlacePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
        public boolean doRequest(@Nullable BlueprintRequest request) {
            return doBlueprintRequest(request);
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
        return BlueprintManger.createBlueprintItem(data, level.registryAccess());
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
    protected void saveAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.saveAdditional(pTag, pRegistries);
        if (data != null) {
            data.save(pTag, pRegistries);
        }
        if (pTag.contains(INDICATE_TAG_ON_CLIENT)) {
            if (template != null) {
                template.saveItems(pTag);
                template.saveSize(pTag);
                template.saveHasStarted(pTag);
            } else {
                pTag.remove(INDICATE_TAG_ON_CLIENT);
            }
        } else if (template instanceof ServerBlueprintPlan serverTemplate) {
            CompoundTag templateTag = new CompoundTag();
            pTag.put(TEMPLATE_TAG, serverTemplate.save(templateTag));
        }
        saveMirror(pTag);
    }


    @Override
    protected void loadAdditional(CompoundTag pTag, HolderLookup.Provider pRegistries) {
        super.loadAdditional(pTag, pRegistries);
        data = BlueprintData.load(pTag, pRegistries);
        if (pTag.contains(INDICATE_TAG_ON_CLIENT)) {
            template = new ClientBlueprintPlan(pTag);
        } else {
            if (pTag.contains(TEMPLATE_TAG)) {
                template = ServerBlueprintPlan.load(pTag.getCompound(TEMPLATE_TAG), pRegistries);
            }
        }
        loadMirror(pTag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean(INDICATE_TAG_ON_CLIENT, true);
        saveAdditional(tag, pRegistries);
        return tag;
    }

    @Override
    public @Nullable Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void saveMirror(CompoundTag tag) {
        tag.putString(MIRROR_TAG, this.mirror.toString());
    }

    private void loadMirror(CompoundTag tag) {
        try {
            this.mirror = Mirror.valueOf(tag.getString(MIRROR_TAG));
        } catch (IllegalArgumentException ignored) {
            this.mirror = Mirror.NONE;
        }
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
            default -> throw new IllegalStateException("Easel Block Entity should never have a BlockState Facing of:" + direction);
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

    private boolean doBlueprintRequest(BlueprintRequest request) {
        if (request == null) {
            return false;
        }
        return tryPlacingSoldier(request.getItem().getDefaultInstance()).isSuccess();
    }

    public BlueprintPlan.PlaceResult tryPlacingSoldier(ItemStack item) {
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
            }
            setChanged();
        }
        return result;
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
