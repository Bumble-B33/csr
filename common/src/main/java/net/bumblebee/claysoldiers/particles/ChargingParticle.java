package net.bumblebee.claysoldiers.particles;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SingleQuadParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.renderer.state.level.QuadParticleRenderState;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.ARGB;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Quaternionf;
import org.jspecify.annotations.Nullable;

public class ChargingParticle extends SingleQuadParticle {
    private static final float X_ROT_STRAIGHT = Mth.HALF_PI;
    private int delay;

    protected ChargingParticle(ClientLevel level, double x, double y, double z, int delay, int lifeTime, TextureAtlasSprite sprite) {
        super(level, x, y, z, 0, 0, 0, sprite);
        this.quadSize = 0.85F;
        this.delay = delay;
        this.lifetime = lifeTime;
        this.gravity = 0.0F;
        this.xd = 0.0;
        this.yd = 0.035;
        this.zd = 0.0;
    }

    @Override
    public float getQuadSize(float pScaleFactor) {
        return this.quadSize * 0.35f;
    }

    @Override
    public void extract(QuadParticleRenderState reusedState, Camera camera, float partialTick) {
        if (this.delay <= 0) {
            this.alpha = 1.0F - Mth.clamp((this.age + partialTick) / this.lifetime, 0.0F, 1.0F);
            Quaternionf quaternionf = new Quaternionf();
            quaternionf.rotationX(-X_ROT_STRAIGHT);
            this.extractRotatedQuad(reusedState, camera, quaternionf, partialTick);
            quaternionf.rotationYXZ((float) -Math.PI, X_ROT_STRAIGHT, 0.0F);
            this.extractRotatedQuad(reusedState, camera, quaternionf, partialTick);
        }
    }

    @Override
    protected int getLightCoords(float a) {
        return LightCoordsUtil.withBlock(super.getLightCoords(a), 15);
    }

    @Override
    protected Layer getLayer() {
        return Layer.TRANSLUCENT;
    }

    @Override
    public void tick() {
        if (this.delay > 0) {
            --this.delay;
        } else {
            super.tick();
        }
    }

    public static class Provider implements ParticleProvider<ChargingParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprite) {
            this.sprite = pSprite;
        }

        @Override
        public @Nullable Particle createParticle(
                ChargingParticleOption type,
                ClientLevel level,
                double x, double y, double z,
                double xSpeed, double ySpeed, double zSpeed,
                RandomSource random) {

            ChargingParticle chargingParticle = new ChargingParticle(level, x, y, z, type.delay(), (int) (type.height() * 10 + 10), this.sprite.get(random));
            chargingParticle.setAlpha(1.0F);
            chargingParticle.scale(type.height());
            int color = ClaySoldiersCommon.ENERGY_HELPER.getEnergyColor();
            chargingParticle.setColor(ARGB.red(color) / 255f, ARGB.green(color) / 255f, ARGB.blue(color) / 255f);
            return chargingParticle;
        }
    }
}
