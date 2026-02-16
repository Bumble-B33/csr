package net.bumblebee.claysoldiers.datamap.armor.accessories.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryData;
import net.bumblebee.claysoldiers.datamap.armor.accessories.SoldierAccessoryKey;
import net.bumblebee.claysoldiers.datamap.armor.accessories.client.custom.*;
import net.minecraft.client.renderer.SubmitNodeCollector;

import java.util.HashMap;
import java.util.Map;

public class RenderableAccessoryMap {
    private static final Map<SoldierAccessoryKey<?>, RenderableAccessory<?>> MAP = new HashMap<>();
    static {
        registerRenderer(SoldierAccessoryKey.CAPE, new CapeRenderable());
        registerRenderer(SoldierAccessoryKey.GLIDER, new GliderRenderable());
        registerRenderer(SoldierAccessoryKey.SHIELD, new ShieldRenderable());
        registerRenderer(SoldierAccessoryKey.SKULL, new SkullRenderable());
        registerRenderer(SoldierAccessoryKey.SNORKEL, new SnorkelRenderable());
        registerRenderer(SoldierAccessoryKey.STRING, new StringRenderable());
    }

    @SuppressWarnings("unchecked")
    public static <T extends SoldierAccessoryData> void submit(SoldierAccessoryKey<T> key, SoldierAccessoryData data, IAccessoryRenderLayer renderedFrom, PoseStack pPoseStack, SubmitNodeCollector nodeCollector, int pPackedLight, AccessoryRenderState claySoldier) {
        getRenderable(key).submit(renderedFrom, (T) data, pPoseStack, nodeCollector, pPackedLight, claySoldier);
    }

    @SuppressWarnings("unchecked")
    private static <T extends SoldierAccessoryData> RenderableAccessory<T> getRenderable(SoldierAccessoryKey<T> key) {
        return (RenderableAccessory<T>) MAP.get(key);
    }

    public static <T extends SoldierAccessoryData> void registerRenderer(SoldierAccessoryKey<T> key, RenderableAccessory<T> renderer) {
        MAP.put(key, renderer);
    }
}
