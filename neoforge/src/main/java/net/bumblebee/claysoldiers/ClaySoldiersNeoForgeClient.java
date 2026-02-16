package net.bumblebee.claysoldiers;

import net.bumblebee.claysoldiers.init.ModMenuTypes;
import net.bumblebee.claysoldiers.init.ModParticles;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.accessories.ModAccessoryRenderers;
import net.bumblebee.claysoldiers.integration.curios.ModCuriosRenderers;
import net.bumblebee.claysoldiers.menu.escritoire.EscritoireScreen;
import net.bumblebee.claysoldiers.menu.horse.ClayHorseScreen;
import net.bumblebee.claysoldiers.menu.info.StatOverlay;
import net.bumblebee.claysoldiers.menu.soldier.ClaySoldierScreen;
import net.bumblebee.claysoldiers.particles.ScaledParticleProviderAdapter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;


@Mod(value = ClaySoldiersCommon.MOD_ID, dist = Dist.CLIENT)
public class ClaySoldiersNeoForgeClient {

    public ClaySoldiersNeoForgeClient(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modEventBus.addListener(this::clientSetUp);
        modEventBus.addListener(this::registerClientTooltipComponent);
        modEventBus.addListener(this::registerEntityInsideShader);
        modEventBus.addListener(this::registerParticles);
        modEventBus.addListener(this::registerMenuScreen);
        modEventBus.addListener(this::registerItemColorHandler);
        modEventBus.addListener(this::registerItemModelCondition);

        modEventBus.addListener(this::registerRenderers);
        modEventBus.addListener(this::registerModalLayers);

        modEventBus.addListener(this::registerSpecialModelRenderer);
        modEventBus.addListener(this::registerSpecialBlockRenderer);
        modEventBus.addListener(this::registerGuiOverlay);


        NeoForge.EVENT_BUS.addListener(this::itemTooltipEvent);

        ExternalMods.CURIOS.ifLoaded(() -> () -> new ModCuriosRenderers(modEventBus));
    }

    private void clientSetUp(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ExternalMods.ACCESSORIES.ifLoaded(() -> ModAccessoryRenderers::init);

            ClaySoldiersCommon.clientPlayer = () -> Minecraft.getInstance().player;
        });
    }

    private void itemTooltipEvent(final ItemTooltipEvent event) {
        ClaySoldiersClient.tooltipEvent(event.getEntity(), event.getItemStack(), event.getToolTip());
    }
    private void registerClientTooltipComponent(final RegisterClientTooltipComponentFactoriesEvent event) {
        ClaySoldiersClient.registerTooltipComponent(event::register);
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
    private void registerItemColorHandler(RegisterColorHandlersEvent.ItemTintSources event) {
        ClaySoldiersClient.registerItemColorHandlers(event::register);
    }

    private void registerItemModelCondition(RegisterRangeSelectItemModelPropertyEvent event) {
        ClaySoldiersClient.registerItemModelCondition(event::register);
    }

    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        ClaySoldiersClient.registerEntityRenderers(event::registerEntityRenderer);
        ClaySoldiersClient.registerBlockRenderers(event::registerBlockEntityRenderer);
    }
    private void registerModalLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        ClaySoldiersClient.registerModalLayers(event::registerLayerDefinition);
    }

    private void registerSpecialBlockRenderer(final RegisterSpecialBlockModelRendererEvent event) {
        ClaySoldiersClient.registerSpecialBlockModelRenderer(event::register);
    }

    private void registerSpecialModelRenderer(final RegisterSpecialModelRendererEvent event) {
        ClaySoldiersClient.registerSpecialItemModelRenderer(event::register);
    }

    private void registerGuiOverlay(final RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.CAMERA_OVERLAYS,
                ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "stats_overlay"),
                (guiGraphics, deltaTracker) -> StatOverlay.getInstance().render(guiGraphics, deltaTracker.getGameTimeDeltaTicks())
        );
    }
}
