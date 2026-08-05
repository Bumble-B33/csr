package net.bumblebee.claysoldiers.datagen.tags;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.bumblebee.claysoldiers.integration.curios.ModCuriosDataProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagCopyingItemTagProvider;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends BlockTagCopyingItemTagProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags) {
        super(output, lookupProvider, blockTags, ClaySoldiersCommon.MOD_ID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModTags.Items.SOLDIER_HOLDABLE)
                .add(ModTagProvider.ForcedTagEntry.tag(ModTags.Items.SOLDIER_WEAPON))
                .add(ModTagProvider.ForcedTagEntry.tag(ModTags.Items.SOLDIER_ARMOR));
        this.tag(ModTags.Items.CLAY_FOOD).add(ModItems.CLAY_COOKIE.get());
        this.tag(ModTags.Items.CLAY_WAX).add(Items.HONEYCOMB);
        this.tag(ModTags.Items.SOLDIER_RGB_GLASSES).addTag(ModTags.Items.GLASS_PANES).remove(ModTags.Items.GLASS_PANES_COLORLESS);
        this.tag(ModTags.Items.GAME_MASTER_ITEM).add(
                Items.COMMAND_BLOCK,
                Items.CHAIN_COMMAND_BLOCK,
                Items.REPEATING_COMMAND_BLOCK,
                Items.COMMAND_BLOCK_MINECART,
                Items.STRUCTURE_BLOCK,
                Items.STRUCTURE_VOID,
                Items.JIGSAW,
                Items.BARRIER,
                Items.LIGHT,
                Items.DEBUG_STICK
        );
        this.tag(ModTags.Items.SOLDIER_BOSS_EQUIPPABLE).addTag(ModTags.Items.SOLDIER_HOLDABLE).remove(Items.COMMAND_BLOCK, Items.DEBUG_STICK);
        this.addItemToTags(ModItems.CLAY_STAFF.get(),
                ItemTags.CROSSBOW_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE, Tags.Items.RANGED_WEAPON_TOOLS, ModTags.Items.SOLDIER_SLINGSHOT_ENCHANTABLE);

        this.addItemToTags(ModItems.CLAY_DISRUPTOR.get(), ItemTags.DURABILITY_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE, Tags.Items.TOOLS);
        this.addItemToTags(ModItems.TERRACOTTA_DISRUPTOR.get(), ItemTags.DURABILITY_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE, Tags.Items.TOOLS);
        this.tag(ModTags.Items.STAT_ITEM).add(ModItems.STATOMETER.get());
        this.tag(ModTags.Items.CLAY_GOGGLES_ITEM).add(ModItems.CLAY_GOGGLES.get());


        this.tag(ModTags.Items.ACCESSORIES_FACE).add(ModItems.CLAY_GOGGLES.get());
        this.tag(ModTags.Items.ACCESSORIES_HAT).add(ModItems.CLAY_SOLDIER.get());
        this.tag(ModTags.Items.ACCESSORIES_BELT).add(ModItems.STATOMETER.get());

        ModCuriosDataProvider.addTags(this::tag);

        this.tag(Tags.Items.MELEE_WEAPON_TOOLS).add(ModItems.SHEAR_BLADE.get(), ModItems.SHARPENED_STICK.get());
        this.tag(ItemTags.SHARP_WEAPON_ENCHANTABLE).add(ModItems.SHEAR_BLADE.get(), ModItems.SHARPENED_STICK.get());
        this.tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.SHEAR_BLADE.get(), ModItems.SHARPENED_STICK.get());
        this.tag(ItemTags.FIRE_ASPECT_ENCHANTABLE).add(ModItems.SHEAR_BLADE.get());

        this.tag(ModTags.Items.CHIP).add(
                ModItems.BLANK_CHIP.get(),
                ModItems.COMBAT_CHIP.get(),
                ModItems.POI_CHIP.get(),
                ModItems.DIG_CHIP.get(),
                ModItems.BREAK_CROPS_CHIP.get(),
                ModItems.PLACE_SEEDS_CHIP.get(),
                ModItems.PICK_UP_ITEMS_CHIP.get(),
                ModItems.FISHING_CHIP.get(),
                ModItems.BLUEPRINT_CHIP.get(),
                ModItems.BEE_KEEPING_CHIP.get(),
                ModItems.ELECTRICIAN_CHIP.get()
        );
        this.tag(ModTags.Items.BATTERY).add(ModItems.SMALL_BATTERY.get(), ModItems.LARGE_BATTERY.get());
    }

    @SafeVarargs
    private void addItemToTags(Item item, TagKey<Item>... tags) {
        for (TagKey<Item> tag : tags) {
            this.tag(tag).add(item);
        }
    }
}
