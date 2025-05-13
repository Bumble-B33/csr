package net.bumblebee.claysoldiers.mixin;

import com.mojang.brigadier.context.CommandContext;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.commands.ClaySoldierCommands;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.commands.DataPackCommand;
import net.minecraft.server.packs.repository.Pack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(DataPackCommand.class)
public abstract class DefaultDataPackCommandMixin {
    @Inject(method = "getPack", at = @At("TAIL"))
    private static void infoGettingPack(CommandContext<CommandSourceStack> context, String name, boolean enabling, CallbackInfoReturnable<Pack> cir) {
        try {
            if (cir.getReturnValue().getId().contains(ClaySoldiersCommon.CSR_DEFAULT_DATA_PACK_PATH)) {
                if (enabling) {
                    context.getSource().sendSuccess(() -> Component.translatable(ClaySoldierCommands.ENABLING_DATAPACK).withStyle(ChatFormatting.YELLOW), false);
                } else {
                    context.getSource().sendSuccess(() -> Component.translatable(ClaySoldierCommands.DISABLING_DATAPACK).withStyle(ChatFormatting.YELLOW), false);
                }
            }
        } catch (RuntimeException e) {
            ClaySoldiersCommon.LOGGER.error("Error Mixin Datapack Command: ", e);
        }
    }
}
