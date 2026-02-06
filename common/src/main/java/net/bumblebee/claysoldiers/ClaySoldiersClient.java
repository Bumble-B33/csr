package net.bumblebee.claysoldiers;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.block.SpecialItemRenderers;
import net.bumblebee.claysoldiers.block.blueprint.EaselBlockEntityRenderer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelBlockEntityRenderer;
import net.bumblebee.claysoldiers.block.hamsterwheel.HamsterWheelModel;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierModel;
import net.bumblebee.claysoldiers.entity.client.ClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierCapeModel;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierShieldModel;
import net.bumblebee.claysoldiers.entity.client.accesories.ClaySoldierSnorkelModel;
import net.bumblebee.claysoldiers.entity.client.boss.BossClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.boss.ClayBlockProjectileRenderer;
import net.bumblebee.claysoldiers.entity.client.boss.VampireBatRenderer;
import net.bumblebee.claysoldiers.entity.client.horse.*;
import net.bumblebee.claysoldiers.entity.client.undead.VampireClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.undead.ZombieClaySoldierRenderer;
import net.bumblebee.claysoldiers.entity.client.wraith.WraithModel;
import net.bumblebee.claysoldiers.entity.client.wraith.WraithRenderer;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModBlocks;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.item.blueprint.tooltip.BlueprintTooltip;
import net.bumblebee.claysoldiers.item.blueprint.tooltip.ClientBlueprintTooltip;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierOnHeadModel;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.bumblebee.claysoldiers.item.claypouch.ClayPouchContent;
import net.bumblebee.claysoldiers.item.claypouch.ClientClayPouchTooltip;
import net.bumblebee.claysoldiers.item.claystaff.ClayStaffModel;
import net.bumblebee.claysoldiers.platform.services.IClientHooks;
import net.bumblebee.claysoldiers.util.ComponentFormating;
import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.ARGB;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ClaySoldiersClient {
    public static final IClientHooks CLIENT_HOOKS =  ClaySoldiersCommon.load(IClientHooks.class);

    public static boolean hasPlayerClayGogglesEquipped() {
        return ClaySoldiersCommon.IS_WEARING_GOGGLES.test(Minecraft.getInstance().player);
    }

    public static void registerModalLayers(BiConsumer<ModelLayerLocation, Supplier<LayerDefinition>> event) {
        event.accept(ClaySoldierModel.LAYER_LOCATION, ClaySoldierModel::createSoldierLayer);
        event.accept(ClaySoldierOnHeadModel.LAYER_LOCATION, ClaySoldierOnHeadModel::createLayer);

        ArmorModelSet<LayerDefinition> set = HumanoidModel.createArmorMeshSet(new CubeDeformation(0.5f), new CubeDeformation(1f))
                .map(mesh -> LayerDefinition.create(mesh, 64, 32));
        event.accept(ClaySoldierModel.HELMET_LAYER_LOCATION, set::head);
        event.accept(ClaySoldierModel.CHESTPLATE_LAYER_LOCATION, set::chest);
        event.accept(ClaySoldierModel.LEGGINGS_LAYER_LOCATION, set::legs);
        event.accept(ClaySoldierModel.BOOTS_LAYER_LOCATION, set::feet);


        event.accept(ClaySoldierSnorkelModel.SNORKEL_LAYER_LOCATION, ClaySoldierSnorkelModel::createSnorkelLayer);
        event.accept(ClaySoldierCapeModel.LAYER_LOCATION, ClaySoldierCapeModel::createSoldierCapeLayer);
        event.accept(ClaySoldierShieldModel.LAYER_LOCATION, ClaySoldierShieldModel::createShieldLayer);

        event.accept(ClayBlockProjectileRenderer.LAYER_LOCATION, ClayBlockProjectileRenderer::createClayBlockLayer);


        event.accept(WraithModel.LAYER_LOCATION, WraithModel::createBodyLayer);

        event.accept(ClayHorseModel.LAYER_LOCATION, ClayHorseModel::createLayerDefinition);
        event.accept(ClayHorseModel.ARMOR_LAYER_LOCATION, ClayHorseModel::createLayerArmorDefinition);

        event.accept(ClayHorseWingsModel.LAYER_LOCATION, ClayHorseWingsModel::createBodyLayer);
        event.accept(ClayHorseHornModel.LAYER_LOCATION, ClayHorseHornModel::createHornLayer);


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

    public enum ClaySoldierItemTintSource implements ItemTintSource {
        INSTANCE;

        private static final MapCodec<ClaySoldierItemTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
            return ClaySoldierSpawnItem.getColorFromTeam(itemStack.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()), Minecraft.getInstance().player);
        }

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return MAP_CODEC;
        }
    }

    public enum ClayPouchItemTintSource implements ItemTintSource {
        INSTANCE;

        private static final MapCodec<ClayPouchItemTintSource> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public int calculate(ItemStack itemStack, @Nullable ClientLevel clientLevel, @Nullable LivingEntity livingEntity) {
            var content = itemStack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
            int color = content == null ? -1 : content.getColor(Minecraft.getInstance().player);
            if (color == -1) {
                return ColorHelper.DEFAULT_CLAY_COLOR;
            }
            return ARGB.opaque(color);}

        @Override
        public MapCodec<? extends ItemTintSource> type() {
            return MAP_CODEC;
        }
    }

    public enum ClayBrushConditionalProperty implements RangeSelectItemModelProperty {
        INSTANCE;

        private static final MapCodec<ClayBrushConditionalProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
            return Objects.requireNonNullElse(stack.get(ModDataComponents.CLAY_BRUSH_MODE.get()), ClayBrushItem.Mode.COMMAND).getOverrideProperty();
        }


        @Override
        public MapCodec<? extends RangeSelectItemModelProperty> type() {
            return MAP_CODEC;
        }
    }

    public enum BlueprintPageConditionalProperty implements RangeSelectItemModelProperty {
        INSTANCE;

        private static final BlueprintItem.BlueprintItemData DEFAULT_DATA = new BlueprintItem.BlueprintItemData(0);
        private static final MapCodec<BlueprintPageConditionalProperty> MAP_CODEC = MapCodec.unit(INSTANCE);

        @Override
        public float get(ItemStack stack, @Nullable ClientLevel clientLevel, @Nullable ItemOwner itemOwner, int i) {
            return stack.getOrDefault(ModDataComponents.BLUEPRINT_ITEM_DATA.get(), DEFAULT_DATA).marking();
        }

        @Override
        public MapCodec<? extends RangeSelectItemModelProperty> type() {
            return MAP_CODEC;
        }
    }

    public static void registerItemColorHandlers(final BiConsumer<ResourceLocation, MapCodec<? extends ItemTintSource>> event) {
        event.accept(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_soldier"), ClaySoldierItemTintSource.MAP_CODEC);
        event.accept(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_pouch"), ClayPouchItemTintSource.MAP_CODEC);
    }

    public static void registerItemModelCondition(final BiConsumer<ResourceLocation, MapCodec<? extends RangeSelectItemModelProperty>> event) {
        event.accept(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_brush"), ClayBrushConditionalProperty.MAP_CODEC);
        event.accept(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "blueprint_page"), BlueprintPageConditionalProperty.MAP_CODEC);
    }

    public static void registerSpecialItemModelRenderer(final BiConsumer<ResourceLocation, MapCodec<? extends SpecialModelRenderer.Unbaked>> event) {
        event.accept(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_staff_renderer"), SpecialItemRenderers.ClayStaffSpecialRenderer.Unbaked.MAP_CODEC);
        event.accept(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hamster_wheel_renderer"), SpecialItemRenderers.HamsterWheelSpecialRenderer.Unbaked.MAP_CODEC);
        event.accept(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "easel_renderer"), SpecialItemRenderers.EaselBlockSpecialRenderer.Unbaked.MAP_CODEC);
    }

    public static void registerSpecialBlockModelRenderer(final BiConsumer<Block, SpecialModelRenderer.Unbaked> event) {
        event.accept(ModBlocks.HAMSTER_WHEEL_BLOCK.get(), new SpecialItemRenderers.HamsterWheelSpecialRenderer.Unbaked());
        event.accept(ModBlocks.EASEL_BLOCK.get(), new SpecialItemRenderers.EaselBlockSpecialRenderer.Unbaked());

    }

    public static void registerEntityInsideShader(BiConsumer<EntityType<?>, ResourceLocation> event) {
        event.accept(ModEntityTypes.VAMPIRE_CLAY_SOLDIER_ENTITY.get(), ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "vampire"));
    }

    public static void registerTooltipComponent(ClientTooltipFactory event) {
        event.register(BlueprintTooltip.class, tooltip -> new ClientBlueprintTooltip(tooltip.requirements()));
        event.register(ClayPouchContent.class, ClientClayPouchTooltip::new);

    }

    /**
     * @param player viewing the tooltip, might be null during start up
     */
    public static void tooltipEvent(@Nullable Player player, ItemStack stack, List<Component> tooltip) {
        if (player == null || (Minecraft.getInstance().hasAltDown() && !CLIENT_HOOKS.hasSoldierTabOpen())) {
            return;
        }

        ComponentFormating.addHoldableTooltip(ClaySoldiersCommon.DATA_MAP.getEffect(stack), tooltip);
        ComponentFormating.addPoiTooltip(ClaySoldiersCommon.DATA_MAP.getItemPoi(stack), tooltip);
        ComponentFormating.addPoiTooltip(ClaySoldiersCommon.DATA_MAP.getBlockPoi(stack), tooltip);
        ComponentFormating.formatClayHorseProperties(ClaySoldiersCommon.DATA_MAP.getHorseArmor(stack.getItem()), tooltip);
    }

    public interface ClientTooltipFactory {
        <T extends TooltipComponent> void register(Class<T> type, Function<T, ? extends ClientTooltipComponent> factory);
    }

    public interface EntityRendererFactory {
        <T extends Entity> void registerEntityRenderer(EntityType<? extends T> entityType, EntityRendererProvider<T> entityRendererProvider);
    }

    public interface BlockEntityRendererFactory {
        <T extends BlockEntity, S extends BlockEntityRenderState> void registerBlockEntityRenderer(BlockEntityType<? extends T> entityType, BlockEntityRendererProvider<T, S> entityRendererProvider);
    }
}
