package net.bumblebee.claysoldiers.soldierproperties.customproperties.revive;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.bumblebee.claysoldiers.soldierproperties.translation.ITranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.ToIntFunction;

public class ReviveProperty implements ITranslatableProperty {
    public static final Codec<ReviveProperty> CODEC = RecordCodecBuilder.create(in -> in.group(
            ReviveType.CODEC.fieldOf("type").forGetter(s -> s.reviveType),
            Codec.INT.fieldOf("priority").forGetter(s -> s.priority),
            CodecUtils.CHANCE_CODEC.optionalFieldOf("chance", 1f).forGetter(s -> s.reviveChance),
            CodecUtils.TIME_CODEC.optionalFieldOf("cooldown", 0).forGetter(s -> s.cooldown)
    ).apply(in, ReviveProperty::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReviveProperty> STREAM_CODEC = StreamCodec.composite(
            ReviveType.STREAM_CODEC, s -> s.reviveType,
            ByteBufCodecs.VAR_INT, s -> s.priority,
            ByteBufCodecs.FLOAT, s -> s.reviveChance,
            ByteBufCodecs.VAR_INT, s -> s.cooldown,
            ReviveProperty::new
    );
    public static final ReviveProperty EMPTY = new ReviveProperty(ReviveType.NONE, 0) {
        @Override
        public ReviveProperty append(ReviveProperty property) {
            return property;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }
    };
    public static final ToIntFunction<ReviveProperty> TO_INT = (r) -> r.equals(EMPTY) ? 0 : 1;
    public static final ValueCombiner<ReviveProperty> COMBINER = ReviveProperty::append;

    private final int priority;
    private final ReviveType reviveType;
    private final float reviveChance;
    private final int cooldown;

    public ReviveProperty(ReviveType reviveType, int priority) {
        this(reviveType, priority, 1f, 0);
    }
    public ReviveProperty(ReviveType reviveType, int priority, float chance, int cooldown) {
        this.priority = priority;
        this.reviveType = reviveType;
        this.reviveChance = chance;
        this.cooldown = cooldown;
    }

    public ReviveResult reviveSoldier(ServerLevel level, AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
        if (!reviver.isReviveTypeOffCooldown(reviveType)) {
            return ReviveResult.FAIL;
        }
        if (reviveChance <= 0 || level.getRandom().nextFloat() > reviveChance) {
                return ReviveResult.FAIL;
        }

        ReviveResult result = reviveType.revive(level, toRevive, reviver);
        if (result.success() && cooldown > 0) {
            reviver.setReviveOnCooldown(reviveType, cooldown);
        }
        return result;
    }

    public boolean canRevive() {
        return !isEmpty();
    }

    @Override
    public @Nullable Component getDisplayName() {
        return reviveType.getDisplayName();
    }

    @Nullable
    public Component getDisplayPrefix() {
        var prefix = reviveType.getDisplayPrefix();
        if (prefix == null) {
            return CommonComponents.EMPTY;
        }
        return prefix;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ReviveProperty that = (ReviveProperty) o;
        return priority == that.priority && reviveType == that.reviveType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(priority, reviveType);
    }
    public boolean isEmpty() {
        return false;
    }

    public ReviveProperty append(ReviveProperty property) {
        if (property.isEmpty()) {
            return this;
        }
        if (property instanceof CombinedReviveProperty combined) {
            combined.append(this);
            return combined;
        }
        return new CombinedReviveProperty(this, property);
    }

    protected static class CombinedReviveProperty extends ReviveProperty {
        private final List<ReviveProperty> reviveProperties;

        public CombinedReviveProperty(ReviveProperty property1, ReviveProperty property2) {
            super(ReviveType.NONE, 0, 1f, 0);
            this.reviveProperties = new ArrayList<>(2);
            if (property1.priority >= property2.priority) {
                this.reviveProperties.add(property1);
                this.reviveProperties.add(property2);
            } else {
                this.reviveProperties.add(property2);
                this.reviveProperties.add(property1);
            }


        }
        @Override
        public ReviveProperty append(ReviveProperty property) {
            if (property.isEmpty()) {
                return this;
            }

            if (property instanceof CombinedReviveProperty combined) {
                for (var pr : combined.reviveProperties) {
                    append(pr);
                }

                return this;
            }

            for (int i = 0; i < reviveProperties.size();i++) {
                var previousProperty = reviveProperties.get(i);
                if (previousProperty.priority <= property.priority) {
                    reviveProperties.add(i, property);
                    return this;
                }
            }
            reviveProperties.add(property);
            return this;
        }

        @Override
        public ReviveResult reviveSoldier(ServerLevel level, AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
            for (ReviveProperty revives : reviveProperties) {
                ReviveResult result = revives.reviveSoldier(level, toRevive, reviver);
                if (result.success()) {
                    return result;
                }
            }
            return ReviveResult.FAIL;
        }

        @Override
        public @Nullable Component getDisplayName() {
            final int propertyCount = reviveProperties.size();
            int index = 0;
            Component end = null;
            while (index < propertyCount){
                end = reviveProperties.get(index).getDisplayName();
                index++;
                if (end != null) {
                    break;
                }
            }
            if (end == null) {
                return null;
            }

            MutableComponent name = null;
            for (int i = propertyCount - 1; i >= index; i--) {
                var prefix = reviveProperties.get(i).getDisplayPrefix();
                if (prefix == null) {
                    continue;
                }
                if (name == null) {
                    name = prefix.copy();
                } else {
                    name.append(prefix);
                }
                name.append(CommonComponents.SPACE);
            }
            return name != null ? name.append(end) : end;
        }
    }
}

