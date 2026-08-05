package net.bumblebee.claysoldiers;

import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.integration.ExternalMods;
import net.bumblebee.claysoldiers.integration.accessories.ModAccessoryRenderers;
import net.bumblebee.claysoldiers.networking.ConfigSyncPayload;
import net.bumblebee.claysoldiers.networking.DataMapPayloadBuilder;
import net.bumblebee.claysoldiers.platform.services.NetworkManger;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.item.v1.ItemTooltipCallback;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.particle.v1.ParticleProviderRegistry;
import net.fabricmc.fabric.api.client.recipe.v1.sync.ClientRecipeSynchronizedEvent;
import net.fabricmc.fabric.api.client.rendering.v1.BlockColorRegistry;
import net.fabricmc.fabric.api.client.rendering.v1.ClientTooltipComponentCallback;
import net.fabricmc.fabric.api.client.rendering.v1.ModelLayerRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemTintSources;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperties;
import net.minecraft.client.renderer.special.SpecialModelRenderers;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.tooltip.TooltipComponent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

public class ClaySoldierFabricClient implements ClientModInitializer {
    private final Map<Class<?>, Function<TooltipComponent, ? extends ClientTooltipComponent>> CLIENT_TOOLTIP_MAP = new HashMap<>();

    @Override
    public void onInitializeClient() {
        ClientLifecycleEvents.CLIENT_STARTED.register(client -> ClaySoldiersCommon.clientPlayer = () -> client.player);

        ClientRecipeSynchronizedEvent.EVENT.register((_, recipes) -> {
            ClaySoldiersCommon.setClientRecipes(recipes.getAllOfType(ModRecipes.CHIP_ASSEMBLY_TYPE));
        });

        ClaySoldiersClient.registerBlockRenderers(BlockEntityRenderers::register);
        ClaySoldiersClient.registerEntityRenderers(EntityRenderers::register);
        ClaySoldiersClient.registerModalLayers((modelLayerLocation, layerDefinitionSupplier) -> ModelLayerRegistry.registerModelLayer(modelLayerLocation, layerDefinitionSupplier::get));

        ClaySoldiersClient.registerParticles(new ClaySoldiersClient.ParticleRegistration() {
            public <T extends ParticleOptions> void registerSpriteSet(ParticleType<T> type, Function<SpriteSet, ParticleProvider<T>> engine) {
                ParticleProviderRegistry.getInstance().register(type, engine::apply);
            }
        });

        ClaySoldiersClient.registerItemColorHandlers(ItemTintSources.ID_MAPPER::put);
        ClaySoldiersClient.registerBlockColorHandlers(BlockColorRegistry::register);
        ClaySoldiersClient.registerItemModelCondition(RangeSelectItemModelProperties.ID_MAPPER::put);
        ClaySoldiersClient.registerSpecialItemModelRenderer(SpecialModelRenderers.ID_MAPPER::put);

        ClaySoldiersClient.registerTooltipComponent(new ClaySoldiersClient.ClientTooltipFactory() {
            @Override
            public <T extends TooltipComponent> void register(Class<T> type, Function<T, ? extends ClientTooltipComponent> factory) {
                CLIENT_TOOLTIP_MAP.put(type, tooltipComponent -> factory.apply(type.cast(tooltipComponent)));
            }
        });

        ItemTooltipCallback.EVENT.register((itemStack, tooltipContext, tooltipFlag, list) ->
                ClaySoldiersClient.tooltipEvent(Minecraft.getInstance().player, itemStack, list)
        );

        ClaySoldiersCommon.NETWORK_MANGER.forEach(data -> ClientPlayNetworking.registerGlobalReceiver(data.id(),
                (payload, context) -> context.client().execute(
                        () -> payload.handleClient(new NetworkManger.PayloadContext(context.client(), context.player()))
                )));

        DataMapPayloadBuilder.registerAllReceiver();
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.ID, ConfigSyncPayload::handleClient);

        ClaySoldiersClient.registerMenuScreenEvent(new ClaySoldiersClient.RegisterMenuEvent() {
            public <M extends AbstractContainerMenu, U extends Screen & MenuAccess<M>> void register(MenuType<? extends M> type, ClaySoldiersClient.ScreenConstructor<M, U> factory) {
                MenuScreens.register(type, factory::create);
            }
        });

        ClientTooltipComponentCallback.EVENT.register(tooltipComponent -> {
            var factory = CLIENT_TOOLTIP_MAP.get(tooltipComponent.getClass());
            try {
                return factory != null ? factory.apply(tooltipComponent) : null;
            } catch (ClassCastException e) {
                ClaySoldiersCommon.ERROR_HANDLER.error("Error Casting client tooltip", e);
                return null;
            }
        });

        ExternalMods.ACCESSORIES.ifLoaded(() -> ModAccessoryRenderers::init);


    }
}
