package net.bumblebee.claysoldiers.loot;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.item.claymobspawn.ClaySoldierSpawnItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import java.util.List;

public class SetRandomClayMobTeam extends LootItemConditionalFunction {
    public static final MapCodec<SetRandomClayMobTeam> CODEC = RecordCodecBuilder.mapCodec(
            (in) -> commonFields(in).apply(in, SetRandomClayMobTeam::new));

    private SetRandomClayMobTeam(List<LootItemCondition> predicates) {
        super(predicates);
    }

    @Override
    public MapCodec<? extends LootItemConditionalFunction> codec() {
        return CODEC;
    }

    public ItemStack run(ItemStack stack, LootContext context) {
        ClaySoldierSpawnItem.setRandomTeam(stack, context.getLevel().registryAccess(), context.getRandom());
        return stack;
    }

    public static Builder<?> of() {
        return simpleBuilder(SetRandomClayMobTeam::new);
    }
}