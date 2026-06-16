package net.bumblebee.claysoldiers.mixin;

import net.bumblebee.claysoldiers.menu.info.StatOverlay;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
public class RenderGuiOverlayMixin {

    @Inject(method = "extractCameraOverlays", at = @At("HEAD"))
    private void renderStatOverlay(GuiGraphicsExtractor guiGraphics, DeltaTracker deltaTracker, CallbackInfo ci) {
        StatOverlay.getInstance().render(guiGraphics, deltaTracker.getGameTimeDeltaTicks());
    }
}
