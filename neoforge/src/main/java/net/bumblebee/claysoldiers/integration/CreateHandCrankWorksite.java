package net.bumblebee.claysoldiers.integration;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.capability.AssignableWorksiteCapability;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.HashMap;

public class CreateHandCrankWorksite implements AssignableWorksiteCapability {
    public static final ResourceLocation WORKSITE_ID = ResourceLocation.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "hand_crank");
    /*private final HandCrankBlockEntity entity;*/
    private static int handIndex = 0;
    private final HashMap<ClayMobEntity, TimeUsedData> timesUse = new HashMap<>();

    /*public CreateHandCrankPoi(HandCrankBlockEntity entity) {
        this.entity = entity;
    }*/

    @Override
    public boolean canUse(ClayMobEntity clayMob) {
        return true;
    }

    @Override
    public int onUse(ClayMobEntity clayMob) {
        //if (entity.inUse < 3) {
        //    entity.turn(false);
            handIndex++;

            long gameTime = clayMob.level().getGameTime();

            timesUse.compute(clayMob, (k, v) -> v == null ? new TimeUsedData(1, gameTime) : v.update(gameTime));
            timesUse.entrySet().removeIf(e -> !e.getValue().isStillValid(gameTime));

            if (handIndex % 3 == 0 || handIndex % 7 == 0) {
                clayMob.swing(InteractionHand.MAIN_HAND);
            } else {
                clayMob.swing(InteractionHand.OFF_HAND);
            }
        //}
        return 1;
    }

    @Override
    public boolean isOneTimeUse() {
        return false;
    }

    @Override
    public ResourceLocation descriptionId() {
        return WORKSITE_ID;
    }

    public static void register(RegisterCapabilitiesEvent event) {
        /*event.registerBlock(ModCapabilities.ASSIGNABLE_POI_CAP, new IBlockCapabilityProvider<>() {
                    @Override
                    public @Nullable AssignablePoiCapability getCapability(Level level, BlockPos blockPos, BlockState blockState, @Nullable BlockEntity blockEntity, Void unused) {
                        if (blockEntity instanceof HandCrankBlockEntity handCrankBlockEntity) {
                            return new CreateHandCrankPoi(handCrankBlockEntity);
                        }
                        return null;
                    }
                },
                AllBlocks.HAND_CRANK.get()
        );*/
    }

    private static final class TimeUsedData {
        private int used;
        private long lastUsed;

        private TimeUsedData(int used, long dameTime) {
            this.used = used;
            this.lastUsed = dameTime;
        }

        public TimeUsedData update(long gameTime) {
            used += 1;
            this.lastUsed = gameTime;
            return this;
        }

        public boolean isStillValid(long gameTime) {
            return this.lastUsed + 20 >= gameTime;
        }

        public int getUsed() {
            return used;
        }
    }
}