package net.bumblebee.claysoldiers.blueprint;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;

public record BlueprintTemplateSettings(Vec3i structureSize, Mirror mirror, Rotation rotation) {
    private static final float DEG_90 = Mth.PI / 2f;
    private static final float DEG_270 = -Mth.PI / 2f;

    public Vec3i getOutlineOffset() {
        return new BlockPos(-structureSize.getX() / 2, 0, 2);
    }

    public float getOutlineRotation() {
        return switch (rotation) {
            case NONE -> 0;
            case CLOCKWISE_90 -> DEG_270;
            case CLOCKWISE_180 -> Mth.PI;
            case COUNTERCLOCKWISE_90 -> DEG_90;
        };
    }

    public int getDistanceToCenter() {
        return structureSize.getX() / 2 * (mirror != Mirror.NONE ? 1 : -1);
    }

    public StructurePlaceSettings convertTo() {
        var settings = new StructurePlaceSettings();
        settings.setMirror(mirror);
        settings.setRotation(rotation);
        return settings;
    }
}

