package net.bumblebee.claysoldiers;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleFactoryRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.*;
import net.fabricmc.fabric.api.resource.IdentifiableResourceReloadListener;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.bumblebee.claysoldiers.block.ModBlockEntityWithoutLevelRenderer;
import net.bumblebee.claysoldiers.init.ModMenuTypes;
import net.bumblebee.claysoldiers.init.ModParticles;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.accessories.ModAccessoryRenderers;
import net.bumblebee.claysoldiers.item.ClayBrushItem;
import net.bumblebee.claysoldiers.item.blueprint.BlueprintItem;
import net.bumblebee.claysoldiers.menu.escritoire.EscritoireScreen;
import net.bumblebee.claysoldiers.menu.horse.ClayHorseScreen;
import net.bumblebee.claysoldiers.menu.soldier.ClaySoldierScreen;
import net.bumblebee.claysoldiers.networking.ConfigSyncPayload;
import net.bumblebee.claysoldiers.networking.DataMapPayloadBuilder;
import net.bumblebee.claysoldiers.particles.ScaledParticleProviderAdapter;
import net.bumblebee.claysoldiers.platform.FabricNetworkManger;
import net.bumblebee.claysoldiers.platform.services.INetworkManger;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.particle.GlowParticle;
import net.minecraft.client.particle.HeartParticle;
import net.minecraft.client.particle.SuspendedTownParticle;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClaySoldierFabricClient implements ClientModInitializer {
    private final Map<Class<?>, Function<TooltipComponent, ? extends ClientTooltipComponent>> CLIENT_TOOLTIP_MAP = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> ClaySoldiersCommon.clientPlayer = () -> client.player);

        ClaySoldiersClient.registerBlockRenderers(BlockEntityRenderers::register);
        ClaySoldiersClient.registerEntityRenderers(EntityRendererRegistry::register);
        ClaySoldiersClient.registerModalLayers((modelLayerLocation, layerDefinitionSupplier) -> EntityModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get));

        ClayBrushItem.registerProperties(ItemProperties::register);
        BlueprintItem.registerProperties(ItemProperties::register);

        ParticleFactoryRegistry.getInstance().register(ModParticles.SMALL_HEART_PARTICLE.get(), pSprites -> new ScaledParticleProviderAdapter(new HeartParticle.Provider(pSprites), 0.35f));
        ParticleFactoryRegistry.getInstance().register(ModParticles.SMALL_ANGRY_PARTICLE.get(), pSprites -> new ScaledParticleProviderAdapter(new HeartParticle.AngryVillagerProvider(pSprites), 0.35f));
        ParticleFactoryRegistry.getInstance().register(ModParticles.SMALL_HAPPY_PARTICLE.get(), pSprites -> new ScaledParticleProviderAdapter(new SuspendedTownParticle.HappyVillagerProvider(pSprites), 1.1f));
        ParticleFactoryRegistry.getInstance().register(ModParticles.SMALL_WAXED_PARTICLE.get(), pSprites -> new ScaledParticleProviderAdapter(new GlowParticle.WaxOnProvider(pSprites), 0.5f));

        ClaySoldiersClient.registerItemColorHandlers(ColorProviderRegistry.ITEM::register);

        ResourceManagerHelper.get(PackType.CLIENT_RESOURCES).registerReloadListener(ClientReloadListener.CLAY_STAFF_MODEL);

        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipFlag, list) ->
                ClaySoldiersClient.tooltipEvent(Minecraft.getInstance().player, itemStack, list)
        );

        FabricNetworkManger.forEachClient(data -> ClientPlayNetworking.registerGlobalReceiver(data.id(),
                (payload, context) -> context.client().execute(
                        () -> data.clientHandler().accept(payload, new INetworkManger.PayloadContext(context.client(), context.player()))
                )));

        DataMapPayloadBuilder.registerAllReceiver();
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, ConfigSyncPayload::handleClient);

        MenuScreens.register(ModMenuTypes.CLAY_SOLDIER_MENU.get(), ClaySoldierScreen::new);
        MenuScreens.register(ModMenuTypes.CLAY_HORSE_MENU.get(), ClayHorseScreen::new);
        MenuScreens.register(ModMenuTypes.ESCRITOIRE_MENU.get(), EscritoireScreen::new);

        ClaySoldiersClient.registerItemInHandRenderers(items -> {
            items.forEach(item -> BuiltinItemRendererRegistry.INSTANCE.register(item, ModBlockEntityWithoutLevelRenderer.getOrCreateInstance()::renderByItem));
        });

        ClaySoldiersClient.registerTooltipComponent(new ClaySoldiersClient.ClientTooltipFactory() {
            @Override
            public <T extends TooltipComponent> void register(Class<T> type, Function<T, ? extends ClientTooltipComponent> factory) {
                CLIENT_TOOLTIP_MAP.put(type, tooltipComponent -> factory.apply(type.cast(tooltipComponent)));
            }
        });

        TooltipComponentCallback.EVENT.register(tooltipComponent -> {
            var factory = CLIENT_TOOLTIP_MAP.get(tooltipComponent.getClass());
            try {
                return factory != null ? factory.apply(tooltipComponent) : null;
            } catch (ClassCastException e) {
                ErrorHandler.INSTANCE.handle("Error Casting client tooltip", e);
                return null;
            }
        });


        ExternalMods.ACCESSORIES.ifLoaded(() -> ModAccessoryRenderers::init);
    }

    private enum ClientReloadListener implements IdentifiableResourceReloadListener, ResourceManagerReloadListener {
        CLAY_STAFF_MODEL(ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "clay_staff_reloader"), ClaySoldiersClient::reloadClayStaffModel);

        private final ResourceLocation id;
        private final Consumer<ResourceManager> effect;

        ClientReloadListener(ResourceLocation id, Consumer<ResourceManager> effect) {
            this.id = id;
            this.effect = effect;
        }

        @Override
        public ResourceLocation getFabricId() {
            return id;
        }

        @Override
        public void onResourceManagerReload(ResourceManager resourceManager) {
            effect.accept(resourceManager);
        }
    }
}
