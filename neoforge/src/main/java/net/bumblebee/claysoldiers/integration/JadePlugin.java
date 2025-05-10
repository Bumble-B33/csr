package net.bumblebee.claysoldiers.integration;

import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.integration.jade.*;
import net.bumblebee.claysoldiers.integration.jade.providers.ClayMobProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElement;
import snownee.jade.api.ui.IElementHelper;
import snownee.jade.impl.ui.CompoundElement;
import snownee.jade.impl.ui.HorizontalLineElement;
import snownee.jade.impl.ui.ItemStackElement;

@WailaPlugin
public class JadePlugin implements IWailaPlugin {
    @Override
    public void registerClient(IWailaClientRegistration registration) {
        JadeRegistry.getBlocks().forEach(jadeBlock -> registration.registerBlockComponent(new NeoForgeBlockProvider(jadeBlock), jadeBlock.getTargetClass()));
        JadeRegistry.getEntities().forEach(jadeEntity -> {
            var provider = new NeoForgeEntityProvider<>(jadeEntity);
            registration.registerEntityComponent(provider, jadeEntity.getTargetClass());

            if (jadeEntity == ClayMobProvider.INSTANCE) {
                registration.registerEntityIcon(provider, jadeEntity.getTargetClass());
            }
        });
    }

    @Override
    public void register(IWailaCommonRegistration registration) {
        JadeRegistry.getServerEntities().forEach(jadeEntityServer -> {
            registration.registerEntityDataProvider(new NeoForgeEntityDataProvider<>(jadeEntityServer), jadeEntityServer.getTargetClass());
        });
    }


    private record NeoForgeBlockProvider(CommonBlockProvider provider) implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip iTooltip, BlockAccessor blockAccessor, IPluginConfig iPluginConfig) {
            provider.appendTooltip(
                    new CommonBlockProvider.BlockData(blockAccessor.getBlockState(), blockAccessor.getBlockEntity(), blockAccessor.getServerData()),
                    new NeoForgeTooltipHelper(iTooltip), blockAccessor.showDetails()
            );
        }

        @Override
        public ResourceLocation getUid() {
            return provider.getUniqueId();
        }
    }

    private record NeoForgeEntityProvider<T extends Entity>(
            CommonEntityProvider<T> provider) implements IEntityComponentProvider {

        @Override
        public ResourceLocation getUid() {
            return provider.getUniqueId();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
            provider.appendTooltip((T) entityAccessor.getEntity(), new NeoForgeTooltipHelper(iTooltip), entityAccessor.showDetails(), provider.requiresServerData() ? entityAccessor.getServerData() : null);
        }

        @Override
        public @Nullable IElement getIcon(EntityAccessor accessor, IPluginConfig config, IElement currentIcon) {
            if (provider != ClayMobProvider.INSTANCE) {
                return IEntityComponentProvider.super.getIcon(accessor, config, currentIcon);
            }
            if (accessor.getPickedResult().isEmpty()) {
                return currentIcon;
            }
            IElementHelper helper = IElementHelper.get();
            IElement largeIcon = helper.item(accessor.getPickedResult());

            if (((ClayMobEntity) accessor.getEntity()).isWaxed()) {
                return new CompoundElement(largeIcon, helper.item(Items.HONEYCOMB.getDefaultInstance(), 0.5f));
            } else {
                return largeIcon;
            }
        }
    }

    private record NeoForgeEntityDataProvider<T extends Entity>(
            CommonEntityServerAppender<T> serverAppender) implements IServerDataProvider<EntityAccessor> {

        @Override
        @SuppressWarnings("unchecked")
        public void appendServerData(CompoundTag compoundTag, EntityAccessor accessor) {
            serverAppender.appendServerData(compoundTag, (T) accessor.getEntity());
        }

        @Override
        public ResourceLocation getUid() {
            return serverAppender.getUniqueId();
        }
    }

    private record NeoForgeTooltipHelper(ITooltip tooltip) implements CommonTooltipHelper {
        private static final HorizontalLineElement LINE_ELEMENT = new HorizontalLineElement();

        @Override
        public void add(Component component) {
            tooltip.add(component);
        }

        @Override
        public void append(Component component) {
            tooltip.append(component);
        }

        @Override
        public void addItemStack(ItemStack stack) {
            tooltip.add(ItemStackElement.of(stack));
        }

        @Override
        public void appendItemStack(ItemStack stack) {
            tooltip.append(ItemStackElement.of(stack));
        }

        @Override
        public void airBubbles(int breath, boolean bursting) {
            tooltip.add(new NeoForgeAirBubbleElement(breath, bursting));
        }

        @Override
        public void addCompoundItemStack(ItemStack large, ItemStack small) {
            tooltip.add(new CompoundElement(
                    ItemStackElement.of(large),
                    ItemStackElement.of(small, 0.5f)
            ));
        }

        @Override
        public void appendMultilineText(Component... lines) {
            tooltip.append(new NeoForgeMultiLineTextElement(lines));
        }

        @Override
        public void addHorizontalLine() {
            tooltip.add(LINE_ELEMENT);
        }
    }

}
