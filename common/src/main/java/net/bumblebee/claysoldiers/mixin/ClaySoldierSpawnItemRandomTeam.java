package net.bumblebee.claysoldiers.mixin;

import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.minecraft.core.component.PatchedDataComponentMap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemStack.class)
public class ClaySoldierSpawnItemRandomTeam {

    @Inject(method = "<init>(Lnet/minecraft/world/level/ItemLike;ILnet/minecraft/core/component/PatchedDataComponentMap;)V", at = @At("RETURN"))
    private void updateComponentsOnCreation(ItemLike item, int count, PatchedDataComponentMap components, CallbackInfo ci) {
        if (item.asItem() instanceof ClaySoldierSpawnItem spawnItem) {
            spawnItem.verifyComponentsAfterLoad((ItemStack) (Object) this);
        }
    }
}
