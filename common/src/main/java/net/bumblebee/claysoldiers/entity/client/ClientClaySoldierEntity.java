package net.bumblebee.claysoldiers.entity.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.WalkAnimationState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.entity.EntityInLevelCallback;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.util.Locale;
import java.util.Objects;

public class ClientClaySoldierEntity extends AbstractClaySoldierEntity {
    private static final Logger LOGGER = LogUtils.getLogger();
    @Nullable
    private final AbstractClaySoldierRenderer renderer;
    private final WalkAnimationState fakeWalkState;
    private ColorHelper offsetColor = ColorHelper.EMPTY;
    private Holder.Reference<ClayMobTeam> clayMobTeamId;
    private ItemStack cachedPickResult;
    private float scale = 1f;
    private boolean waxed = false;

    private ClientClaySoldierEntity(EntityType<? extends AbstractClaySoldierEntity> pEntityType, BlockPos pos, WalkAnimationState fakeWalkState, Holder.Reference<ClayMobTeam> clayMobTeamId) {
        super(pEntityType, Minecraft.getInstance().level, AttackTypeProperty.NORMAL);
        this.fakeWalkState = fakeWalkState;
        this.clayMobTeamId = clayMobTeamId;
        this.renderer = createRenderer(pEntityType, this);
        setLevelCallback(EntityInLevelCallback.NULL);
        setPosRaw(pos.getX(), pos.getY(), pos.getZ());
    }

    public static ClientClaySoldierEntity create(EntityType<? extends AbstractClaySoldierEntity> type, CompoundTag tag, BlockPos pos, WalkAnimationState state, Holder.Reference<ClayMobTeam> id, float size) {
        ClientClaySoldierEntity soldier = new ClientClaySoldierEntity(type, pos, state, id);
        soldier.waxed = tag.getBoolean(WAXED_TAG);
        soldier.readArmorSaveData(tag);
        soldier.offsetColor = ColorHelper.getFromTag(AbstractClaySoldierEntity.OFFSET_COLOR_TAG, tag);
        soldier.scale = size;
        return soldier;
    }

    public static ClientClaySoldierEntity createAsProjectile(WalkAnimationState state, Holder.Reference<ClayMobTeam> clayMobTeam) {
        return new ClientClaySoldierEntity(ModEntityTypes.CLAY_SOLDIER_ENTITY.get(), BlockPos.ZERO, state, clayMobTeam);
    }

    public void setUpCape() {
        moveCloak(0, 0, 0);
        moveCloak(0, 0, 10);
    }

    private static AbstractClaySoldierRenderer createRenderer(EntityType<? extends AbstractClaySoldierEntity> type, AbstractClaySoldierEntity soldier) {
        try {
            return Objects.requireNonNull((AbstractClaySoldierRenderer) Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(soldier));
        } catch (ClassCastException | NullPointerException e) {
            LOGGER.error("Could not create Renderer for Type {}, {}", type.getDescriptionId(), e.getMessage());
            return null;
        }
    }

    public void render(float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (renderer != null) {
            yBodyRotO = 0;
            yBodyRot = 0;
            yHeadRotO = 0;
            yHeadRot = 0;
            yRotO = 0;
            xRotO = 0;

            renderer.render(this, yaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    @Override
    public @NotNull String toString() {
        return String.format(
                Locale.ROOT,
                "%s['%s', r='%s' w='%b', s='%.2f' x=%.2f, y=%.2f, z=%.2f]",
                this.getClass().getSimpleName(),
                this.getName().getString(),
                this.renderer,
                this.waxed,
                this.scale,
                this.getX(),
                this.getY(),
                this.getZ());
    }

    @Override
    public float getScale() {
        return scale;
    }

    @Override
    public boolean isWaxed() {
        return waxed;
    }


    @Override
    public @NotNull ClayMobTeam getClayTeam() {
        if (ClayMobTeamManger.isValidTeam(clayMobTeamId.key().location(), registryAccess())) {
            return clayMobTeamId.value();
        }
        clayMobTeamId = ClayMobTeamManger.getDefault(registryAccess());
        return clayMobTeamId.value();
    }

    @Override
    public ItemStack getPickResult() {
        if (ClayMobTeamManger.isValidTeam(clayMobTeamId.key().location(), level().registryAccess())) {
            if (cachedPickResult == null) {
                this.cachedPickResult = ClayMobTeamManger.createStackForTeam(clayMobTeamId.key().location(), level().registryAccess());
            }
            return cachedPickResult;
        }

        return ItemStack.EMPTY;
    }

    @Override
    public ResourceLocation getClayTeamType() {
        return clayMobTeamId.key().location();
    }

    @Override
    public ColorHelper getOffsetColor() {
        return offsetColor;
    }

    @Override
    public WalkAnimationState getWalkAnimation() {
        return fakeWalkState;
    }

    @Override
    public @Nullable Component getWorkStatus() {
        return null;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
    }

    @Override
    public boolean isPassenger() {
        return false;
    }

    @Override
    public @Nullable Entity getVehicle() {
        return null;
    }

    @Override
    public boolean isCrouching() {
        return false;
    }

    @Override
    public void tick() {
    }


    @Override
    public float getYRot() {
        return 0;
    }

    @Override
    public void setYRot(float yRot) {
    }

    @Override
    public float getXRot() {
        return 0;
    }

    @Override
    public void setXRot(float xRot) {
    }

    @Override
    public boolean isInWater() {
        return false;
    }
}
