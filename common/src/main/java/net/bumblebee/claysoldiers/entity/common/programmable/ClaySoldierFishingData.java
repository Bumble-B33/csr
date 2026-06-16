package net.bumblebee.claysoldiers.entity.common.programmable;

import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.phys.Vec3;

import java.text.DecimalFormat;

public class ClaySoldierFishingData {
    private static final DecimalFormat POS_FORMAT = new DecimalFormat("#.#");
    public static final ClaySoldierFishingData EMPTY = new ClaySoldierFishingData(false, Vec3.ZERO);
    public static final StreamCodec<ByteBuf, ClaySoldierFishingData> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public ClaySoldierFishingData decode(ByteBuf byteBuf) {
            if (!byteBuf.readBoolean()) {
                return EMPTY;
            }
            return new ClaySoldierFishingData(
                    true,
                    byteBuf.readFloat(),
                    byteBuf.readFloat(),
                    byteBuf.readFloat()
            );
        }

        @Override
        public void encode(ByteBuf byteBuf, ClaySoldierFishingData pos) {
            byteBuf.writeBoolean(pos.isFishing());
            if (pos.isFishing()) {
                byteBuf.writeFloat((float) pos.getPos().x());
                byteBuf.writeFloat((float) pos.getPos().y());
                byteBuf.writeFloat((float) pos.getPos().z());
            }
        }
    };
    public static final String EMPTY_LANG = "clay_soldier_fishing_pos.%s.empty".formatted(ClaySoldiersCommon.MOD_ID);
    public static final String FISHING_LANG = "clay_soldier_fishing_pos.%s.fishing".formatted(ClaySoldiersCommon.MOD_ID);
    public static final String ANKER_LANG = "clay_soldier_fishing_pos.%s.anker".formatted(ClaySoldiersCommon.MOD_ID);

    private final boolean isFishing;
    private final Vec3 pos;
    private final String posAsString;

    private ClaySoldierFishingData(boolean isFishing, Vec3 pos) {
        this.isFishing = isFishing;
        this.pos = pos;
        this.posAsString = "(%s, %s, %s)".formatted(
                POS_FORMAT.format(pos.x()),
                POS_FORMAT.format(pos.y()),
                POS_FORMAT.format(pos.z())
        );
    }

    private ClaySoldierFishingData(boolean isFishing, float x, float y, float z) {
        this(isFishing, new Vec3(x, y, z));
    }

    public boolean isFishing() {
        return isFishing;
    }

    public Vec3 getPos() {
        if (!isFishing) {
            ClaySoldiersCommon.ERROR_HANDLER.warn("Tried to get the Pos of an empty fishing pos");
        }
        return pos;
    }

    public Component displayName(boolean isAnker) {
        if (!isAnker) {
            return Component.translatable(EMPTY_LANG);
        }

        if (!isFishing) {
            return Component.translatable(ANKER_LANG);
        }

        return Component.translatable(FISHING_LANG, posAsString);
    }

    public static ClaySoldierFishingData at(Vec3 vec3) {
        return new ClaySoldierFishingData(true, vec3);
    }

    @Override
    public String toString() {
        return "FishingData{" +
                 (isFishing ? "Fishing" : "Not Fishing") +
                ", At:" + posAsString +
                '}';
    }
}
