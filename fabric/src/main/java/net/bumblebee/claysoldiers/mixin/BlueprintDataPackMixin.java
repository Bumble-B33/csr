package net.bumblebee.claysoldiers.mixin;

import net.bumblebee.claysoldiers.BlueprintPackSource;
import net.bumblebee.claysoldiers.ClaySoldierFabric;
import net.minecraft.SharedConstants;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackSelectionConfig;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pack.class)
public abstract class BlueprintDataPackMixin {

    @Inject(method = "readMetaAndCreate", at = @At("RETURN"), cancellable = true)
    private static void redirect(PackLocationInfo location, Pack.ResourcesSupplier resources, PackType packType, PackSelectionConfig selectionConfig, CallbackInfoReturnable<Pack> cir) {
        if (location.id().contains(ClaySoldierFabric.BLUEPRINT_PACK_ID.toString())) {
            int i = SharedConstants.getCurrentVersion().getPackVersion(packType);
            PackLocationInfo info = new PackLocationInfo(location.id(), location.title(), BlueprintPackSource.INSTANCE, location.knownPackInfo());
            Pack.Metadata metadata = Pack.readPackMetadata(info, resources, i);
            if (metadata != null) {
                Pack pack = new Pack(info, resources, metadata, selectionConfig);
                cir.setReturnValue(pack);
            }
        }
    }
}
