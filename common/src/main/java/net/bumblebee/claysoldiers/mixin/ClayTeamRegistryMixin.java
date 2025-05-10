package net.bumblebee.claysoldiers.mixin;

import net.bumblebee.claysoldiers.init.ModRegistries;
import net.bumblebee.claysoldiers.team.ClayMobTeam;
import net.bumblebee.claysoldiers.team.ClayMobTeamManger;
import net.bumblebee.claysoldiers.util.ErrorHandler;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(MappedRegistry.class)
public abstract class ClayTeamRegistryMixin<T> implements Registry<T> {

    @SuppressWarnings("unchecked")
    @Inject(method = "freeze", at = @At("HEAD"))
    private void beforeFreeze(CallbackInfoReturnable<Registry<T>> cir) {
        if (this.key().equals(ModRegistries.CLAY_MOB_TEAMS)) {
            try {
                ClayMobTeamManger.registerDefault((Registry<ClayMobTeam>) this);
            } catch (RuntimeException e) {
                ErrorHandler.INSTANCE.handle("Error mixin ClayMobTeamRegistry" , e);
            }
        }
    }
}
