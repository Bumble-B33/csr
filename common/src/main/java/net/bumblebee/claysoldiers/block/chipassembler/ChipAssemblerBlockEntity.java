package net.bumblebee.claysoldiers.block.chipassembler;

import com.google.common.primitives.Ints;
import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.common.StatInfoDisplay;
import net.bumblebee.claysoldiers.init.ModBlockEntities;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.bumblebee.claysoldiers.networking.ChipAssemblyEnergyPayload;
import net.bumblebee.claysoldiers.recipe.chip.ChipAssemblyRecipe;
import net.bumblebee.claysoldiers.recipe.chip.ChipInput;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

public class ChipAssemblerBlockEntity extends BlockEntity implements StatInfoDisplay {
    private static final String ENERGY_TAG = "energy";
    private static final String PROGRESS_TAG = "progress";
    private static final String PROGRESS_START_TAG = "progress_start";
    public static final int CHIP_RECIPE_EXTRA_REQUIRED_ENERGY = 20;

    private static final String LAST_RECIPE_TAG = "last_recipe";
    private static final Codec<ResourceKey<Recipe<?>>> LAST_RECIPE_CODEC = ResourceKey.codec(Registries.RECIPE);

    private final ChipAssemblerInventory inventory;
    private final ChipEnergyStorage energyStorage;
    private final RecipeManager.CachedCheck<ChipInput, ChipAssemblyRecipe> quickCheck;

    @Nullable
    private ResourceKey<Recipe<?>> lastRecipe = null;
    private int progress = -1;
    private int progressStart = -1;

    private long lastEnergyStored;

    public ChipAssemblerBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntities.CHIP_ASSEMBLER_BLOCK_ENTITY.get(), worldPosition, blockState);
        this.inventory = new ChipAssemblerInventory();
        this.energyStorage = ClaySoldiersCommon.CAPABILITY_MANGER.createEnergyChipStorage();
        this.quickCheck = RecipeManager.createCheck(ModRecipes.CHIP_ASSEMBLY_TYPE);
        this.lastEnergyStored = energyStorage.getEnergyStored();
    }

    public Optional<ItemStack> insert(ItemStack stack, BlockHitResult hitResult) {
        var res = inventory.insert(stack, hitResult, getBlockState().getValue(ChipAssemblerBlock.FACING));
        if (res.isPresent()) {
            markChanged();
        }
        return res;
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        inventory.forEachNonEmpty(stack ->
                Containers.dropItemStack(level, this.worldPosition.getX(), this.worldPosition.getY(), this.worldPosition.getZ(), stack)
        );
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    private void markChanged() {
        this.setChanged();
        this.getLevel().sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        inventory.load(input);
        energyStorage.set(Ints.saturatedCast(input.getLongOr(ENERGY_TAG, 0)));
        progress = input.getIntOr(PROGRESS_TAG, -1);
        progressStart = input.getIntOr(PROGRESS_START_TAG, -1);
        lastRecipe = input.read(LAST_RECIPE_TAG, LAST_RECIPE_CODEC).orElse(null);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        inventory.save(output);
        output.putLong(ENERGY_TAG, energyStorage.getEnergyStored());
        output.putInt(PROGRESS_TAG, progress);
        output.putInt(PROGRESS_START_TAG, progressStart);
        if (lastRecipe != null) {
            output.store(LAST_RECIPE_TAG, LAST_RECIPE_CODEC, lastRecipe);
        }
    }

    public ChipAssemblerInventory getInventory() {
        return inventory;
    }

    public ChipEnergyStorage getEnergyStorage(@Nullable Direction direction) {
        if (direction == null) {
            return energyStorage;
        }
        return getBlockState().getValue(ChipAssemblerBlock.FACING).getOpposite() == direction ? energyStorage : null;
    }


    public void serverTick() {
        if (level instanceof ServerLevel serverLevel) {
            ChipInput chipInput = new ChipInput(inventory.withoutEmpty());
            RecipeHolder<ChipAssemblyRecipe> recipeHolder = getRecipe(chipInput, level);

            if (energyRequiresUpdate(energyStorage.getEnergyStored(), lastEnergyStored) || (recipeHolder != null && recipeHolder.id() != lastRecipe)) {
                ClaySoldiersCommon.NETWORK_MANGER.sendToPlayersTrackingBlockEntity(this, new ChipAssemblyEnergyPayload(worldPosition, energyStorage.getEnergyStored()));
                lastEnergyStored = energyStorage.getEnergyStored();
            }

            int energy = tickRecipe(recipeHolder, chipInput, serverLevel, energyStorage.getEnergyStored());
            if (energy > 0) {
                energyStorage.remove(energy);
            }
        }
    }

    public void clientTick() {
        if (level != null) {
            ChipInput chipInput = new ChipInput(inventory.withoutEmpty());
            RecipeHolder<ChipAssemblyRecipe> recipeHolder = getRecipe(chipInput, level);
            tickRecipe(recipeHolder, chipInput, level, energyStorage.getEnergyStored());
        }
    }

    /**
     * @return the energy consumed by this recipe
     */
    private int tickRecipe(@Nullable RecipeHolder<ChipAssemblyRecipe> recipeHolder, ChipInput chipInput, @NotNull Level level, long storedEnergy) {
        if (recipeHolder == null) {
            lastRecipe = null;
            progress = -1;
            progressStart = -1;
            return 0;
        }
        if (progress <= -1 && recipeHolder.value().totalEnergyCost() + CHIP_RECIPE_EXTRA_REQUIRED_ENERGY > storedEnergy) {
            return 0;
        }

        int energyNeed = recipeHolder.value().energyCost();
        if (energyNeed > storedEnergy) {
            return 0;
        }

        if (recipeHolder.id() != lastRecipe) {
            lastRecipe = recipeHolder.id();
            progress = recipeHolder.value().adjustedBuiltTime();
            progressStart = progress;
            return energyNeed;
        }

        if (progress == 0) {
            if (level instanceof ServerLevel serverLevel) {
                ItemStack recipeResult = recipeHolder.value().assemble(chipInput);
                inventory.reduceByOne();

                if (inventory.get(ChipAssemblerInventory.Slot.CENTER).isEmpty()) {
                    inventory.put(ChipAssemblerInventory.Slot.CENTER, recipeResult);
                } else {
                    ItemEntity entity = new ItemEntity(serverLevel, worldPosition.getX() + 0.5f, worldPosition.getY() + 0.4f, worldPosition.getZ() + 0.5f, recipeResult);
                    serverLevel.addFreshEntity(entity);
                }
                markChanged();
                awardRecipeUse(serverLevel, recipeHolder, chipInput);
            }
            lastRecipe = null;
            progress = -1;
            progressStart = -1;

            return energyNeed;
        }

        progress--;
        return energyNeed;
    }

    private void awardRecipeUse(ServerLevel serverLevel, RecipeHolder<?> recipeHolder, ChipInput input) {
        for (ServerPlayer player : serverLevel.getEntitiesOfClass(ServerPlayer.class, AABB.ofSize(Vec3.atCenterOf(worldPosition), 17.0, 17.0, 17.0))) {
            player.triggerRecipeCrafted(recipeHolder, input.getNonCenterInputs());
        }
    }

    @Nullable
    private RecipeHolder<ChipAssemblyRecipe> getRecipe(ChipInput input, Level level) {
        if (level instanceof ServerLevel serverLevel) {
            return quickCheck.getRecipeFor(input, serverLevel).orElse(null);
        }
        return ClaySoldiersCommon.CLIENT_RECIPE_ACCESS.getChipAssemblyRecipe(input, level).findAny().orElse(null);
    }

    public int getProgress() {
        return progress;
    }

    public int getProgressStart() {
        return progressStart;
    }

    public void setEnergy(long energy) {
        this.energyStorage.set(Ints.saturatedCast(energy));
        if (level instanceof ServerLevel) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Setting Energy on the Server");
        }
    }

    private boolean energyRequiresUpdate(long energy, long lastEnergy) {
        if (energy == lastEnergy) {
            return false;
        }
        if (progress >= 0) {
            return true;
        }
        if (energy == 0) {
            return true;
        }
        if (Math.abs(energy - lastEnergy) > 3) {
            return true;
        }
        return energy >= energyStorage.getMaxCapacity();
    }

    @Override
    public void getStatDisplay(List<Component> list, LivingEntity viewer) {
        String energyUnit = ClaySoldiersCommon.PLATFORM.getEnergyUnitName();
        list.add(getBlockState().getBlock().getName());

        list.add(CommonComponents.space().append(Component.translatable(StatInfoDisplay.ENERGY, energyStorage.getEnergyStored() + energyUnit, energyStorage.getMaxCapacity() + energyUnit).withStyle(ChatFormatting.GRAY)));
        if (progress >= 0) {
            list.add(CommonComponents.space().append(Component.translatable(StatInfoDisplay.PROGRESS, (progress / 20) + 1)).withStyle(ChatFormatting.GRAY));

        }
    }

    public void addInfo(List<String> info) {
        info.add("Progress: " + progress);
        info.add("Energy: " + energyStorage);
    }

    @Override
    public String toString() {
        return "ChipAssemblerBlockEntity{%s, %s}".formatted(worldPosition, level);
    }
}
