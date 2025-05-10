package net.bumblebee.claysoldiers.mixin;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.Dynamic2CommandExceptionType;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import net.bumblebee.claysoldiers.ClaySoldierFabric;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackRepository;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataPackCommand.class)
public abstract class DataPackCommandMixin {
    @Final @Shadow private static DynamicCommandExceptionType ERROR_CANNOT_DISABLE_FEATURE;

    @Shadow @Final private static Dynamic2CommandExceptionType ERROR_PACK_FEATURES_NOT_ENABLED;

    @Shadow @Final private static DynamicCommandExceptionType ERROR_PACK_ALREADY_ENABLED;

    @Shadow @Final private static DynamicCommandExceptionType ERROR_PACK_ALREADY_DISABLED;

    @Inject(method = "getPack", at = @At("HEAD"))
    private static void preventDisablingBlueprintPack(CommandContext<CommandSourceStack> context, String name, boolean enabling, CallbackInfoReturnable<Pack> cir) throws CommandSyntaxException {
        String s = StringArgumentType.getString(context, name);
        PackRepository packrepository = context.getSource().getServer().getPackRepository();
        Pack pack = packrepository.getPack(s);
        if (pack != null && pack.getId().contains(ClaySoldierFabric.BLUEPRINT_PACK_ID.toString())) {
            boolean isSelected = packrepository.getSelectedPacks().contains(pack);
            if (enabling && isSelected) {
                throw ERROR_PACK_ALREADY_ENABLED.create(s);
            } else if (!enabling && !isSelected) {
                throw ERROR_PACK_ALREADY_DISABLED.create(s);
            } else if (enabling) {
                throw ERROR_PACK_FEATURES_NOT_ENABLED.create(s, ClaySoldierFabric.BLUEPRINT_PACK_ID.toString());
            } else {
                throw ERROR_CANNOT_DISABLE_FEATURE.create(s);
            }
        }
    }
}
