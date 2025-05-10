package net.bumblebee.claysoldiers;

import net.bumblebee.claysoldiers.block.ModBlockEntityWithoutLevelRenderer;
import net.bumblebee.claysoldiers.init.ModMenuTypes;
import net.bumblebee.claysoldiers.init.ModParticles;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.accessories.ModAccessoryRenderers;
import net.bumblebee.claysoldiers.integration.curios.ModCuriosRenderers;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.menu.escritoire.EscritoireScreen;
import net.bumblebee.claysoldiers.menu.horse.ClayHorseScreen;
import net.bumblebee.claysoldiers.menu.soldier.ClaySoldierScreen;
import net.bumblebee.claysoldiers.particles.ScaledParticleProviderAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;


@Mod(value = ClaySoldiersCommon.MOD_ID, dist = Dist.CLIENT)
public class ClaySoldiersNeoForgeClient {

    public ClaySoldiersNeoForgeClient(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::clientSetUp);
        modEventBus.addListener(this::registerClientExtensions);
        modEventBus.addListener(this::registerClientTooltipComponent);
        modEventBus.addListener(this::registerEntityInsideShader);
        modEventBus.addListener(this::registerParticles);
        modEventBus.addListener(this::registerMenuScreen);
        modEventBus.addListener(this::registerItemColorHandler);
        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerModalLayers);
        modEventBus.addListener(this::registerResourceReloadListeners);

        NeoForge.EVENT_BUS.addListener(this::itemTooltipEvent);
    }

    private void clientSetUp(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ClayBrushItem.registerProperties(ItemProperties::register);
            BlueprintItem.registerProperties(ItemProperties::register);
            ExternalMods.ACCESSORIES.ifLoaded(() -> ModAccessoryRenderers::init);
            ExternalMods.CURIOS.ifLoaded(() -> ModCuriosRenderers::init);
            ClaySoldiersCommon.clientPlayer = () -> Minecraft.getInstance().player;

        });
    }

    private void itemTooltipEvent(final ItemTooltipEvent event) {
        ClaySoldiersClient.tooltipEvent(event.getEntity(), event.getItemStack(), event.getToolTip());
    }
    private void registerClientTooltipComponent(final RegisterClientTooltipComponentFactoriesEvent event) {
        ClaySoldiersClient.registerTooltipComponent(event::register);
    }
    private void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions extension = new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return ModBlockEntityWithoutLevelRenderer.getOrCreateInstance();
            }
        };
        ClaySoldiersClient.registerItemInHandRenderers(items -> event.registerItem(extension, items.stream().map(ItemLike::asItem).toArray(Item[]::new)));
    }
    private void registerEntityInsideShader(RegisterEntitySpectatorShadersEvent event) {
        ClaySoldiersClient.registerEntityInsideShader(event::register);
    }
    private void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticles.SMALL_HEART_PARTICLE.get(), pSprites -> new ScaledParticleProviderAdapter(new HeartParticle.Provider(pSprites), 0.35f));
        event.registerSpriteSet(ModParticles.SMALL_ANGRY_PARTICLE.get(), pSprites -> new ScaledParticleProviderAdapter(new HeartParticle.AngryVillagerProvider(pSprites), 0.35f));
        event.registerSpriteSet(ModParticles.SMALL_HAPPY_PARTICLE.get(), pSprites -> new ScaledParticleProviderAdapter(new SuspendedTownParticle.HappyVillagerProvider(pSprites), 1.1f));
        event.registerSpriteSet(ModParticles.SMALL_WAXED_PARTICLE.get(), pSprites -> new ScaledParticleProviderAdapter(new GlowParticle.WaxOnProvider(pSprites), 0.5f));
    }
    private void registerMenuScreen(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.CLAY_SOLDIER_MENU.get(), ClaySoldierScreen::new);
        event.register(ModMenuTypes.CLAY_HORSE_MENU.get(), ClayHorseScreen::new);
        event.register(ModMenuTypes.ESCRITOIRE_MENU.get(), EscritoireScreen::new);
    }
    private void registerItemColorHandler(RegisterColorHandlersEvent.Item event) {
        ClaySoldiersClient.registerItemColorHandlers(event::register);
    }
    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClaySoldiersClient.registerEntityRenderers(event::registerEntityRenderer);
        ClaySoldiersClient.registerBlockRenderers(event::registerBlockEntityRenderer);
    }
    private void registerModalLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ClaySoldiersClient.registerModalLayers(event::registerLayerDefinition);
        ExternalMods.CURIOS.ifLoaded(() -> () -> ModCuriosRenderers.registerLayerEvent(event));
    }

    private void registerResourceReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) ClaySoldiersClient::reloadClayStaffModel);
    }
}
