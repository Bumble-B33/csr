package net.bumblebee.claysoldiers.datagen.tags;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.init.ModItems;
import net.bumblebee.claysoldiers.init.ModTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

public class ModItemTagProvider extends ItemTagsProvider {
    public ModItemTagProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, CompletableFuture<TagLookup<Block>> blockTags, @Nullable ExistingFileHelper existingFileHelper) {
        super(output, lookupProvider, blockTags, ClaySoldiersCommon.MOD_ID, existingFileHelper);
    }


    @Override
    protected void addTags(HolderLookup.Provider provider) {
        this.tag(ModTags.Items.SOLDIER_HOLDABLE)
                .addOptionalTag(ModTags.Items.SOLDIER_WEAPON)
                .addOptionalTag(ModTags.Items.SOLDIER_ARMOR);
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
        this.tag(ModTags.Items.ACCESSORIES_FACE).add(ModItems.CLAY_GOGGLES.get());
        this.tag(ModTags.Items.CURIOS_HEAD).add(ModItems.CLAY_GOGGLES.get());
        this.tag(ModTags.Items.SOLDIER_BOSS_EQUIPABLE).addTag(ModTags.Items.SOLDIER_HOLDABLE).remove(Items.COMMAND_BLOCK, Items.DEBUG_STICK);
        this.addItemToTags(ModItems.CLAY_STAFF.get(),
                ItemTags.CROSSBOW_ENCHANTABLE, ItemTags.VANISHING_ENCHANTABLE, Tags.Items.RANGED_WEAPON_TOOLS, ModTags.Items.SOLDIER_SLINGSHOT_ENCHANTABLE);

    }

    @SafeVarargs
    private void addItemToTags(Item item, TagKey<Item>... tags) {
        for (TagKey<Item> tag : tags) {
            this.tag(tag).add(item);
        }
    }
}
