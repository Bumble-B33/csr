package net.bumblebee.claysoldiers.init;

import com.mojang.serialization.MapCodec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.loot.SetRandomClayMobTeam;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.Supplier;

public class ModLootTables {
    // todo use
    public static final ResourceKey<LootTable> SMALL_HOUSE = ResourceKey.create(Registries.LOOT_TABLE, Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "chests/small_house"));

    public static final Supplier<MapCodec<SetRandomClayMobTeam>> RANDOM_CLAY_SOLDIER_TEAM = ClaySoldiersCommon.PLATFORM.registerLootItemFunction("random_clay_soldier_team",
            () -> SetRandomClayMobTeam.CODEC
    );

    private ModLootTables() {
    }

    public static void init() {
    }
}
