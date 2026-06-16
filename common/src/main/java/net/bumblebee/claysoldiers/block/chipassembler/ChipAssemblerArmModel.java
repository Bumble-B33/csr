package net.bumblebee.claysoldiers.block.chipassembler;

import net.bumblebee.claysoldiers.recipe.chip.ChipAssemblyRecipe;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

public class ChipAssemblerArmModel extends Model<ChipAssemblerRenderState> {
    private static final String ARM_NAME = "arm";
    private static final String BEAM_NAME = "beam";
    private static final String SLIDER_NAME = "slider";
    private static final String CLAY_NAME = "pointer";

    private static final Target RESTING_POS = new Target(0, 0, -2.5f);
    private static final Target CENTER_POS = new Target(0, 1.5f, -5);
    private static final int TICKS_PER_PHASE = ChipAssemblyRecipe.BUILT_TIME_MULTIPLIER;


    private final ModelPart arm;
    private final ModelPart beam;
    private final ModelPart slider;
    private final ModelPart claw;

    public ChipAssemblerArmModel(ModelPart root) {
        super(root, RenderTypes::entityCutout);
        this.arm = root.getChild(ARM_NAME);
        this.beam = arm.getChild(BEAM_NAME);
        this.slider = beam.getChild(SLIDER_NAME);
        this.claw = slider.getChild(CLAY_NAME);
    }

    @Override
    public void setupAnim(ChipAssemblerRenderState state) {
        super.setupAnim(state);

        float progress = state.progress;
        int startProgress = state.startProgress;

        if (progress == -1) {
            setToTarget(RESTING_POS);
            return;
        }

        int elapsed = startProgress - (int) progress;
        int remaining = (int) progress;

        if (remaining < TICKS_PER_PHASE) {
            float t = easeInOut(1f - (remaining / (float) TICKS_PER_PHASE));
            Target current = getPositionAt(elapsed - 1, startProgress); // where we were just before this phase
            Target interpolated = lerp(current, RESTING_POS, t);
            setToTarget(interpolated);
            return;
        }

        int workingDuration = startProgress - TICKS_PER_PHASE;
        setToTarget(getPositionAt(elapsed, startProgress, workingDuration));
    }

    private Target getPositionAt(int elapsed, int startProgress) {
        int workingDuration = startProgress - TICKS_PER_PHASE;
        return getPositionAt(elapsed, startProgress, workingDuration);
    }

    private Target getPositionAt(int elapsed, int startProgress, int workingDuration) {
        int numRandomPos = getNumRandomPositions(startProgress);
        int totalPhases = 1 + numRandomPos * 2;
        Target[] keyframes = buildKeyframes(startProgress, numRandomPos);

        int phaseLength = workingDuration / totalPhases;
        int clampedElapsed = Math.min(elapsed, workingDuration - 1); // never overshoot
        int phase = Math.min(clampedElapsed / phaseLength, totalPhases - 1);
        float t = (clampedElapsed % phaseLength) / (float) phaseLength;

        Target from = keyframes[phase];
        Target to = keyframes[phase + 1];
        return lerp(from, to, easeInOut(t));
    }

    private Target[] buildKeyframes(int startProgress, int numRandomPos) {
        Target[] keyframes = new Target[2 + numRandomPos * 2];
        keyframes[0] = RESTING_POS;
        keyframes[1] = CENTER_POS;
        for (int i = 0; i < numRandomPos; i++) {
            keyframes[2 + i * 2] = getRandomTarget(startProgress, i);
            keyframes[3 + i * 2] = CENTER_POS;
        }
        return keyframes;
    }

    private int getNumRandomPositions(int startProgress) {
        return Math.max(1, ((startProgress - TICKS_PER_PHASE) / TICKS_PER_PHASE - 1) / 2);
    }

    /** Deterministic random target derived from the seed and which random stop it is. */
    private static Target getRandomTarget(int seed, int index) {
        int h = hash(seed * 31 + index);
        int hz = hash(seed * 31 + index + 13);
        float x = mapToRange(h,        -3f, 3f);
        float z = mapToRange(hz,  -8f, -2f);
        return new Target(x, 2, z);
    }

    private static int hash(int n) {
        n = ((n >> 16) ^ n) * 0x45d9f3b;
        n = ((n >> 16) ^ n) * 0x45d9f3b;
        n = (n >> 16) ^ n;
        return n;
    }

    private static float mapToRange(int h, float min, float max) {
        float t = (h & 0xFFFF) / (float) 0xFFFF;
        return min + t * (max - min);
    }

    private static Target lerp(Target a, Target b, float t) {
        return new Target(
                a.x() + (b.x() - a.x()) * t,
                a.y() + (b.y() - a.y()) * t,
                a.z() + (b.z() - a.z()) * t
        );
    }

    private static float easeInOut(float t) {
        return t * t * (3f - 2f * t);
    }

    private void setToTarget(Target target) {
        setToTarget(target.x(), target.y(), target.z());
    }

    private void setToTarget(float tx, float ty, float tz) {
        this.arm.yRot = (float) Math.atan(tx / tz);

        double planarDistance = Math.sqrt((tx * tx) + (tz * tz));
        this.beam.xRot = (float) Math.atan(ty / planarDistance);
        this.claw.xRot = -this.beam.xRot;

        double dis = planarDistance / Math.cos(this.beam.xRot);
        this.slider.z = (float) -dis;
    }

    private record Target(float x, float y, float z) {}

    public static LayerDefinition createLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition arm = partdefinition.addOrReplaceChild(ARM_NAME, CubeListBuilder.create()
                        .texOffs(56, 0).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 2.0F),
                PartPose.offsetAndRotation(8F, 5, 12F, 0, 0, Mth.PI));

        PartDefinition tool = arm.addOrReplaceChild(BEAM_NAME, CubeListBuilder.create()
                        .texOffs(48, 17).addBox(0.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F)
                        .texOffs(38, 4).addBox(0.5F, -0.5F, -10.0F, 1.0F, 1.0F, 12.0F),
                PartPose.offset(1.0F, -7.0F, 0.0F));

        PartDefinition con = tool.addOrReplaceChild(SLIDER_NAME, CubeListBuilder.create()
                        .texOffs(56, 17).addBox(0.0F, -1.0F, -1.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offset(0.0F, 0.0F, -0.0F));

        PartDefinition claw = con.addOrReplaceChild(CLAY_NAME, CubeListBuilder.create()
                        .texOffs(56, 21).addBox(-2.0F, -1.0F, -1.0F, 2.0F, 3.0F, 2.0F)
                        .texOffs(48, 21).addBox(-2.0F, 2.25F, -1.0F, 2.0F, 1.0F, 2.0F)
                        .texOffs(49, 24).addBox(-1.5F, 2.0F, -0.5F, 1.0F, 3.0F, 1.0F),
                PartPose.offset(0.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
}
