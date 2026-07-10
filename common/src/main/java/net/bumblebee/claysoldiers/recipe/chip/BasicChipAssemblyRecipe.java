package net.bumblebee.claysoldiers.recipe.chip;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.block.chipassembler.ChipAssemblerBlockEntity;
import net.bumblebee.claysoldiers.block.chipassembler.ChipEnergyStorage;
import net.bumblebee.claysoldiers.init.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.PlacementInfo;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class BasicChipAssemblyRecipe extends ChipAssemblyRecipe {
    private static final MapCodec<BasicChipAssemblyRecipe> UNVALIDATED_CODEC = RecordCodecBuilder.mapCodec(in -> in.group(
            CommonInfo.MAP_CODEC.forGetter(s -> s.info),
            ChipAssemblyInfo.MAP_CODEC.forGetter(s -> s.recipeInfo),
            ItemStackTemplate.MAP_CODEC.forGetter(s -> s.result),
            Ingredient.CODEC.optionalFieldOf("chip").forGetter(BasicChipAssemblyRecipe::chip),
            Ingredient.CODEC.listOf().fieldOf("inputs").forGetter(s -> s.inputs),
            ExtraCodecs.POSITIVE_INT.fieldOf("building_steps").forGetter(BasicChipAssemblyRecipe::builtSteps),
            ExtraCodecs.POSITIVE_INT.fieldOf("energy_cost").forGetter(BasicChipAssemblyRecipe::energyCost)
    ).apply(in, BasicChipAssemblyRecipe::new));
    public static final MapCodec<BasicChipAssemblyRecipe> CODEC = UNVALIDATED_CODEC.validate(BasicChipAssemblyRecipe::validate);
    public static final StreamCodec<RegistryFriendlyByteBuf, BasicChipAssemblyRecipe> STREAM_CODEC = StreamCodec.composite(
            CommonInfo.STREAM_CODEC, s -> s.info,
            ChipAssemblyInfo.STREAM_CODEC, s -> s.recipeInfo,
            ItemStackTemplate.STREAM_CODEC, s -> s.result,
            ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), BasicChipAssemblyRecipe::chip,
            Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.list()), s -> s.inputs,
            ByteBufCodecs.VAR_INT, ChipAssemblyRecipe::builtSteps,
            ByteBufCodecs.VAR_INT, ChipAssemblyRecipe::energyCost,
            BasicChipAssemblyRecipe::new
    );
    private final ItemStackTemplate result;
    private final Ingredient chip;
    private final List<Ingredient> inputs;
    @Nullable
    private PlacementInfo placementInfo = null;

    public BasicChipAssemblyRecipe(CommonInfo info, ChipAssemblyInfo recipeInfo, ItemStackTemplate result, Optional<Ingredient> chip, List<Ingredient> inputs, int buildingTime, int energyCost) {
        super(info, recipeInfo, buildingTime, energyCost);
        this.result = result;
        this.chip = chip.orElse(null);
        this.inputs = inputs;
    }

    @Override
    public boolean matches(ChipInput chipInput, Level level) {
        if (!chip().map(i -> i.test(chipInput.getChip()))
                .orElse(chipInput.getChip().isEmpty())) {
            return false;
        }


        return sameContents(chipInput);
    }

    public Optional<Ingredient> chip() {
        return Optional.ofNullable(chip);
    }

    public List<Ingredient> getInputs() {
        return inputs;
    }

    public ItemStack createResult() {
        return result.create();
    }

    private boolean sameContents(ChipInput input) {
        if (input.ingredientCount() != this.inputs.size()) {
            return false;
        } else {
            boolean size1 = input.size() == 1 && this.inputs.size() == 1;
            return size1 ? (this.inputs.getFirst()).test(input.getItem(0)) : input.stackedContents().canCraft(this, null);
        }
    }

    @Override
    public ItemStack assemble(ChipInput chipInput) {
        return createResult();
    }

    @Override
    public RecipeSerializer<? extends Recipe<ChipInput>> getSerializer() {
        return ModRecipes.CHIP_ASSEMBLY_SERIALIZER.get();
    }

    @Override
    public PlacementInfo placementInfo() {
        if (this.placementInfo == null) {
            this.placementInfo = PlacementInfo.create(inputs);
        }
        return this.placementInfo;
    }

    private static DataResult<BasicChipAssemblyRecipe> validate(BasicChipAssemblyRecipe recipe) {
        int totalEnergyStorage = ChipEnergyStorage.MAX_CAPACITY + ChipAssemblerBlockEntity.CHIP_RECIPE_EXTRA_REQUIRED_ENERGY;
        int totalRequiredEnergy = recipe.totalEnergyCost();
        if (totalEnergyStorage >= totalRequiredEnergy) {
            return DataResult.success(recipe);
        }
        return DataResult.error(() ->
                "Total required Energy (%s) of the Recipe is greater than to Total Energy storage (%s) in the Basic Chip Assembler"
                        .formatted(totalRequiredEnergy, totalEnergyStorage)
        );
    }

    @Override
    public String toString() {
        return "BasicChipAssemblyRecipe{" + result.item().value() + '}';
    }
}
