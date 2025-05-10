package net.bumblebee.claysoldiers.integration;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.kinetics.crank.HandCrankBlockEntity;
import net.bumblebee.claysoldiers.capability.AssignablePoiCapability;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.init.ModCapabilities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.jetbrains.annotations.Nullable;

public class CreateHandCrankPoi implements AssignablePoiCapability {
    private final HandCrankBlockEntity entity;
    private static int handIndex = 0;

    public CreateHandCrankPoi(HandCrankBlockEntity entity) {
        this.entity = entity;
    }

    @Override
    public boolean canUse(ClayMobEntity clayMob) {
        return true;
    }

    @Override
    public void use(ClayMobEntity clayMob) {
        if (entity.inUse < 3) {
            entity.turn(false);
            handIndex++;
            if (handIndex % 3 == 0 || handIndex % 7 == 0) {
                clayMob.swing(InteractionHand.MAIN_HAND);
            } else {
                clayMob.swing(InteractionHand.OFF_HAND);
            }
        }
    }

    @Override
    public boolean isOneTimeUse() {
        return false;
    }

    public static void register(RegisterCapabilitiesEvent event) {
        event.registerBlock(ModCapabilities.ASSIGNABLE_POI_CAP, new IBlockCapabilityProvider<>() {
                    @Override
                    public @Nullable AssignablePoiCapability getCapability(Level level, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, Void unused) {
                        if (blockEntity instanceof HandCrankBlockEntity handCrankBlockEntity) {
                            return new CreateHandCrankPoi(handCrankBlockEntity);
                        }
                        return null;
                    }
                },
                AllBlocks.HAND_CRANK.get()
        );
    }
}