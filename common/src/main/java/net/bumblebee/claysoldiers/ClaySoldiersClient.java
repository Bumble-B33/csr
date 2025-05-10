package net.bumblebee.claysoldiers;

import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntityRenderer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntityRenderer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelModel;
import net.bumblebee.claysoldiers.datamap.horse.ClayHorseWearableProperties;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierCapeModel;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierShieldModel;
import net.bumblebee.claysoldiers.entity.client.boss.BossClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.boss.ClayBlockProjectileRenderer;
import net.bumblebee.claysoldiers.entity.client.boss.VampireBatRenderer;
import net.bumblebee.claysoldiers.entity.client.horse.*;
import net.bumblebee.claysoldiers.entity.client.undead.VampireClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.undead.ZombieClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.wraith.WraithModel;
import net.bumblebee.claysoldiers.entity.client.wraith.WraithRenderer;
import net.bumblebee.claysoldiers.init.*;
import net.bumblebee.claysoldiers.item.blueprint.tooltip.BlueprintTooltip;
import net.bumblebee.claysoldiers.item.blueprint.tooltip.ClientBlueprintTooltip;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.item.claypouch.ClayPouchContent;
import net.bumblebee.claysoldiers.item.claypouch.ClientClayPouchTooltip;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffModel;
import net.bumblebee.claysoldiers.platform.services.IClientHooks;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClaySoldiersClient {
    public static final IClientHooks CLIENT_HOOKS =  ClaySoldiersCommon.load(IClientHooks.class);
    @Nullable
    public static ClayStaffModel clayStaffModel;


    public static boolean hasPlayerClayGogglesEquipped() {
        return ClaySoldiersCommon.IS_WEARING_GOGGLES.test(Minecraft.getInstance().player);
    }

    public static void registerModalLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> event) {
        event.accept(ClaySoldierModel.LAYER_LOCATION, ClaySoldierModel::createLayer);

        event.accept(ClaySoldierCapeModel.LAYER_LOCATION, ClaySoldierCapeModel::createSoldierMesh);
        event.accept(ClaySoldierShieldModel.LAYER_LOCATION, ClaySoldierShieldModel::createShieldLayer);

        event.accept(ClayBlockProjectileRenderer.LAYER_LOCATION, ClayBlockProjectileRenderer::createClayBlockLayer);


        event.accept(WraithModel.LAYER_LOCATION, WraithModel::createBodyLayer);

        event.accept(ClayHorseModel.LAYER_LOCATION, ClayHorseModel::createLayerDefinition);
        event.accept(ClayHorseWingsModel.LAYER_LOCATION, ClayHorseWingsModel::createBodyLayer);
        event.accept(ClayHorseArmorLayer.LAYER_LOCATION, ClayHorseModel::createLayerArmorDefinition);

        event.accept(HamsterWheelBlockEntityRenderer.POWER_LAYER_LOCATION, HamsterWheelBlockEntityRenderer::createPowerLayer);
        event.accept(HamsterWheelBlockEntityRenderer.STAND_LAYER_LOCATION, HamsterWheelBlockEntityRenderer::createStandLayer);
        event.accept(HamsterWheelBlockEntityRenderer.BatteryType.LEFT.getLayerLocation(), HamsterWheelBlockEntityRenderer.BatteryType.LEFT::createLayerDefinition);
        event.accept(HamsterWheelBlockEntityRenderer.BatteryType.RIGHT.getLayerLocation(), HamsterWheelBlockEntityRenderer.BatteryType.RIGHT::createLayerDefinition);

        event.accept(HamsterWheelModel.LAYER_LOCATION, HamsterWheelModel::createWheelLayer);

        event.accept(EaselBlockEntityRenderer.STAND_LAYER_LOCATION, EaselBlockEntityRenderer::createStandLayer);
        event.accept(EaselBlockEntityRenderer.BLUEPRINT_LAYER_LOCATION, EaselBlockEntityRenderer::createBlueprintLayer);

        event.accept(ClayStaffModel.LAYER_LOCATION, ClayStaffModel::createStaffLayer);
        event.accept(ClayStaffModel.SOLDIER_LAYER_LOCATION, ClayStaffModel::createSoldierDollLayer);
    }

    public static void registerBlockRenderers(BlockEntityRendererFactory event) {
        event.registerBlockEntityRenderer(ModBlockEntities.HAMSTER_WHEEL_BLOCK_ENTITY.get(), HamsterWheelBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.EASEL_BLOCK_ENTITY.get(), EaselBlockEntityRenderer::new);
    }

    public static void registerEntityRenderers(EntityRendererFactory event) {
        event.registerEntityRenderer(ModEntityTypes.CLAY_SOLDIER_ENTITY.get(), ClaySoldierRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), ZombieClaySoldierRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get(), VampireClaySoldierRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.CLAY_HORSE_ENTITY.get(), ClayHorseRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLAY_PEGASUS_ENTITY.get(), ClayPegasusRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLAY_SOLDIER_THROWABLE_ITEM.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLAY_SOLDIER_POTION.get(), ThrownItemRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLAY_SOLDIER_SNOWBALL.get(), ThrownItemRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.CLAY_WRAITH.get(), WraithRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.VAMPIRE_BAT.get(), VampireBatRenderer::new);

        event.registerEntityRenderer(ModEntityTypes.BOSS_CLAY_SOLDIER_ENTITY.get(), BossClaySoldierRenderer::new);
        event.registerEntityRenderer(ModEntityTypes.CLAY_BLOCK_PROJECTILE.get(), ClayBlockProjectileRenderer::new);
    }

    public static void registerItemColorHandlers(final BiConsumer<ItemColor, ItemLike> event) {
        event.accept((pStack, pTintIndex) -> {
            /*int color = ClayMobTeamManger.getFromKeyOrError(pStack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()), Minecraft.getInstance().level.registryAccess()).getColor(Minecraft.getInstance().player, 0);
            if (color == -1) {
                return ColorHelper.DEFAULT_CLAY_COLOR;
            }
            return FastColor.ARGB32.opaque(color);*/
            return ClaySoldierSpawnItem.getColorFromTeam(pStack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()), Minecraft.getInstance().player);
        }, ModItems.CLAY_SOLDIER.get());
        event.accept((pStack, pTintIndex) -> {
            if (pTintIndex != 1) {
                return -1;
            }
            var content = pStack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
            int color = content == null ? -1 : content.getColor(Minecraft.getInstance().player);
            if (color == -1) {
                return ColorHelper.DEFAULT_CLAY_COLOR;
            }
            return FastColor.ARGB32.opaque(color);
        }, ModItems.CLAY_POUCH.get());
    }

    public static void registerItemInHandRenderers(Consumer<List<ItemLike>> items) {
        items.accept(List.of(ModItems.CLAY_STAFF, ModBlocks.EASEL_BLOCK, ModBlocks.HAMSTER_WHEEL_BLOCK));
    }

    public static void registerEntityInsideShader(BiConsumer<EntityType<?>, ResourceLocation> event) {
        event.accept(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get(), ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "shaders/post/vampire.json"));
    }

    public static void registerTooltipComponent(ClientTooltipFactory event) {
        event.register(BlueprintTooltip.class, tooltip -> new ClientBlueprintTooltip(tooltip.requirements()));
        event.register(ClayPouchContent.class, ClientClayPouchTooltip::new);

    }

    /**
     * @param player viewing the tooltip, might be null during start up
     */
    public static void tooltipEvent(@Nullable Player player, ItemStack stack, List<Component> tooltip) {
        if (player == null || (!Screen.hasAltDown() && !CLIENT_HOOKS.hasSoldierTabOpen())) {
            return;
        }

        ComponentFormating.addHoldableTooltip(ClaySoldiersCommon.DATA_MAP.getEffect(stack), tooltip);
        ComponentFormating.addPoiTooltip(ClaySoldiersCommon.DATA_MAP.getItemPoi(stack), tooltip);
        ComponentFormating.addPoiTooltip(ClaySoldiersCommon.DATA_MAP.getBlockPoi(stack), tooltip);
        addClayHorseTooltip(ClaySoldiersCommon.DATA_MAP.getHorseArmor(stack.getItem()), tooltip);
    }



    public static void addClayHorseTooltip(@Nullable ClayHorseWearableProperties effect, List<Component> tooltip) {
        ComponentFormating.formatClayHorseProperties(effect, tooltip);
    }

    public static void reloadClayStaffModel(ResourceManager resourceManager) {
        var instance = Minecraft.getInstance();
        if (instance != null) {
            ClaySoldiersClient.clayStaffModel = ClayStaffModel.create(instance.getEntityModels()::bakeLayer);
        }
    }

    public interface ClientTooltipFactory {
        <T extends TooltipComponent> void register(Class<T> type, Function<T, ? extends ClientTooltipComponent> factory);
    }

    public interface EntityRendererFactory {
        <T extends Entity> void registerEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider);
    }

    public interface BlockEntityRendererFactory {
        <T extends BlockEntity> void registerBlockEntityRenderer(BlockEntityType<? extends T> entityType, BlockEntityRendererProvider<T> entityRendererProvider);
    }
}
