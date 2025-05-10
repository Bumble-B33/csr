package net.bumblebee.claysoldiers.soldierproperties.customproperties;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayMobEntity;
import net.bumblebee.claysoldiers.soldierproperties.combined.ValueCombiner;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.Fireworks;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

public interface IEvacuationProperty {
    String EVACUATION_PROPERTY_PREFIX = ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG + ".evacuation.";
    Codec<IEvacuationProperty> CODEC = EvacuationProperty.CODEC.flatComapMap(e -> e, IEvacuationProperty::decode);
    StreamCodec<ByteBuf, IEvacuationProperty> STREAM_CODEC = new StreamCodec<>() {
        @Override
        public IEvacuationProperty decode(ByteBuf byteBuf) {
            return EvacuationProperty.values()[byteBuf.readByte()];
        }

        @Override
        public void encode(ByteBuf o, IEvacuationProperty iEvacuationProperty) {
            o.writeByte(Math.max(iEvacuationProperty.id(), 0));
        }
    };

    IEvacuationProperty NONE = EvacuationProperty.NONE;
    IEvacuationProperty FIREWORK = EvacuationProperty.FIREWORK;

    ValueCombiner<IEvacuationProperty> COMBINER = (first, second) -> {
      if (first instanceof CombinedEvacuationProperty combined1) {
          return combined1.append(second);
      } else if (second instanceof CombinedEvacuationProperty comnined2) {
          return comnined2.append(first);
      }
      return new CombinedEvacuationProperty(first, second);
    };
    BiFunction<String, IEvacuationProperty, List<Component>> DISPLAY_NAME_GETTER = ((key, property) -> {
        if (property instanceof CombinedEvacuationProperty) {
            return List.of(Component.translatable(key), property.getDisplayName());
        }
        return List.of(property.getDisplayName());
    });

    void evacuate(ServerLevel level, ClayMobEntity shooter);
    int id();
    Component getDisplayName();
    boolean isEmpty();
    int toInt();

    private static DataResult<EvacuationProperty> decode(IEvacuationProperty iEvacuationProperty) {
        if (iEvacuationProperty.id() < 0 || iEvacuationProperty.id() >= EvacuationProperty.values().length) {
            return DataResult.error(() -> "Cannot Decode EvacuationProperty with id " + iEvacuationProperty.id());
        }
        return DataResult.success(EvacuationProperty.values()[iEvacuationProperty.id()]);
    }

    enum EvacuationProperty implements StringRepresentable, IEvacuationProperty {
        NONE("none", (s, c) -> {}),
        FIREWORK("firework", EvacuationProperty::evacuateFirework);

        public static final Codec<EvacuationProperty> CODEC = StringRepresentable.fromEnum(EvacuationProperty::values);
        private final String serializedName;
        private final BiConsumer<ServerLevel, ClayMobEntity> evacuationFunction;

        EvacuationProperty(String serializedName, BiConsumer<ServerLevel, ClayMobEntity> evacuationFunction) {
            this.serializedName = serializedName;
            this.evacuationFunction = evacuationFunction;
        }

        @Override
        public void evacuate(ServerLevel level, ClayMobEntity entity) {
            evacuationFunction.accept(level, entity);
        }

        private static void evacuateFirework(ServerLevel level, ClayMobEntity shooter) {
            shooter.stopRiding();
            ItemStack stack = Items.FIREWORK_ROCKET.getDefaultInstance();
            stack.set(DataComponents.FIREWORKS, new Fireworks(1, List.of()));

            FireworkRocketEntity fireworkrocketentity = new FireworkRocketEntity(level, shooter, shooter.getX(), shooter.getY(), shooter.getZ(), stack);
            level.addFreshEntity(fireworkrocketentity);

            shooter.startRiding(fireworkrocketentity, true);
        }

        @Override
        public int id() {
            return ordinal();
        }

        @Override
        public int toInt() {
            return 1;
        }

        @Override
        public Component getDisplayName() {
            return Component.translatable(translatableKey());
        }

        public String translatableKey() {
            return EVACUATION_PROPERTY_PREFIX + serializedName;
        }

        @Override
        public String getSerializedName() {
            return serializedName;
        }

        @Override
        public boolean isEmpty() {
            return this == NONE;
        }
    }

    class CombinedEvacuationProperty implements IEvacuationProperty {
        private final List<IEvacuationProperty> evacs;

        public CombinedEvacuationProperty(IEvacuationProperty first, IEvacuationProperty second) {
            this.evacs = new ArrayList<>();
            evacs.add(first);
            evacs.add(second);
        }

        private IEvacuationProperty append(IEvacuationProperty property) {
            if (property instanceof CombinedEvacuationProperty combined) {
                evacs.addAll(combined.evacs);
            } else {
                evacs.add(property);
            }
            return this;
        }

        @Override
        public void evacuate(ServerLevel level, ClayMobEntity shooter) {
            evacs.getFirst().evacuate(level, shooter);
        }

        @Override
        public int id() {
            return -1;
        }

        @Override
        public Component getDisplayName() {
            var start = Component.literal("[");
            for (int i = 0;i < evacs.size(); i++) {
                if (i != 0) {
                    start.append(Component.literal(", "));
                }
                start.append(evacs.get(i).getDisplayName());
            }
            start.append(Component.literal("]"));
            return start;
        }

        @Override
        public int toInt() {
            return evacs.size();
        }

        @Override
        public boolean isEmpty() {
            return evacs.isEmpty();
        }
    }
}
