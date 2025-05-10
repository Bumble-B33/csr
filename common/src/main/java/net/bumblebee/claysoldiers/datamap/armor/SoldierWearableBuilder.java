package net.bumblebee.claysoldiers.datamap.armor;

import net.bumblebee.claysoldiers.util.color.ColorHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.armortrim.TrimMaterial;
import net.minecraft.world.item.armortrim.TrimPattern;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class SoldierWearableBuilder {
    private final Set<SoldierWearableEffect.SoldierArmorTrim> trims = new HashSet<>();
    private ColorHelper color = ColorHelper.EMPTY;
    @Nullable
    private final ArmorItem armorCopy;
    private boolean offsetColor = false;

    private SoldierWearableBuilder(@Nullable ArmorItem armorCopy) {
        this.armorCopy = armorCopy;
    }

    public static SoldierWearableBuilder armor(ArmorItem armorCopy) {
        return new SoldierWearableBuilder(armorCopy);
    }
    public static SoldierWearableBuilder empty() {
        return new SoldierWearableBuilder(null);
    }


    public SoldierWearableBuilder color(ColorHelper color) {
        this.color = color;
        return this;
    }
    public SoldierWearableBuilder color(int color) {
        this.color = ColorHelper.color(color);
        return this;
    }
    public SoldierWearableBuilder affectedOffsetColor() {
        this.offsetColor = true;
        return this;
    }
    public SoldierWearableBuilder addTrim(ResourceKey<TrimPattern> pattern, ResourceKey<TrimMaterial> material) {
        return addTrim(pattern, material, ColorHelper.EMPTY);
    }
    public SoldierWearableBuilder addTrim(ResourceKey<TrimPattern> pattern, ResourceKey<TrimMaterial> material, ColorHelper color) {
        this.trims.add(new SoldierWearableEffect.SoldierArmorTrim(pattern, material, color));
        return this;
    }
    public SoldierWearableEffect build() {
        return new SoldierWearableEffect(armorCopy, color, trims, offsetColor);
    }
}
