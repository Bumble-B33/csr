package net.bumblebee.claysoldiers.soldierproperties.customproperties.revive;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.entity.ClayWraithEntity;
import net.bumblebee.claysoldiers.entity.soldier.AbstractClaySoldierEntity;
import net.bumblebee.claysoldiers.init.ModEntityTypes;
import net.bumblebee.claysoldiers.soldierproperties.translation.KeyableTranslatableProperty;
import net.bumblebee.claysoldiers.util.codec.CodecUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;

public enum ReviveType implements StringRepresentable, KeyableTranslatableProperty {
    NONE("none", (level, toRevive, reviver) -> ReviveResult.FAIL, (toRevive, reviver) -> false, null),
    NECROTIC("necrotic", ReviveType::necroticRevive, (toRevive, reviver) -> !toRevive.isIgnited(), ChatFormatting.DARK_GREEN),
    DARK_NECROTIC("dark_necrotic", ReviveType::darkNecrotic, (toRevive, reviver) -> true, ChatFormatting.BLACK),
    MEDIC("medic", ReviveType::medicRevive, ReviveType::medicPredicate, ChatFormatting.GREEN),
    ANGEL("angel", ReviveType::medicRevive, ReviveType::sameTeam, ChatFormatting.YELLOW),
    WRAITH("spiritual", ReviveType::wraithConversion, ReviveType::sameTeam, ChatFormatting.DARK_GREEN) {
        @Override
        public Style getStyle() {
            return Style.EMPTY.withColor(0x03FCDF);
        }
    };


    public static final Codec<ReviveType> CODEC = StringRepresentable.fromEnum(ReviveType::values);
    public static final StreamCodec<FriendlyByteBuf, ReviveType> STREAM_CODEC = CodecUtils.createEnumStreamCodec(ReviveType.class);

    public static final StreamCodec<ByteBuf, List<Integer>> INT_LIST_STREAM_CODEC = ByteBufCodecs.INT.apply(ByteBufCodecs.list(ReviveType.values().length));


    private final String serializedName;
    @Nullable
    private final ChatFormatting chatFormatting;
    private final ReviveFunction effect;
    private final BiPredicate<AbstractClaySoldierEntity, AbstractClaySoldierEntity> reviveCondition;

    ReviveType(String serializedName, ReviveFunction effect, BiPredicate<AbstractClaySoldierEntity, AbstractClaySoldierEntity> reviveCondition, @Nullable ChatFormatting chatFormatting) {
        this.serializedName = serializedName;
        this.reviveCondition = reviveCondition;
        this.effect = effect;
        this.chatFormatting = chatFormatting;
    }

    @Override
    @NotNull
    public String getSerializedName() {
        return serializedName;
    }

    @Nullable
    public Component getDisplayPrefix() {
        if (getStyle() == null) {
            return null;
        }
        return Component.translatable(translatablePrefixKey()).withStyle(getStyle());
    }

    @Override
    public String translatableKey() {
        return ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG + "revive_type." + serializedName;
    }

    public String translatablePrefixKey() {
        return ClaySoldiersCommon.CLAY_SOLDIER_PROPERTY_LANG + "revive_type." + serializedName + ".prefix";
    }

    @Override
    public ChatFormatting getFormat() {
        return chatFormatting;
    }

    public ReviveResult revive(ServerLevel level, AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
        if (!reviveCondition.test(toRevive, reviver)) {
            return ReviveResult.FAIL;
        }
        return effect.revive(level, toRevive, reviver);
    }

    public static Optional<ReviveType> getFromString(String name) {
        for (ReviveType type : values()) {
            if (type.serializedName.equals(name)) {
                return Optional.of(type);
            }
        }
        return Optional.empty();
    }

    private static ReviveResult necroticRevive(ServerLevel level, AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
        return zombieRevive(toRevive, reviver, true);
    }
    private static ReviveResult darkNecrotic(ServerLevel level, AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
        return zombieRevive(toRevive, reviver, false);
    }
    private static ReviveResult zombieRevive(AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver, boolean curable) {
        return ReviveResult.zombieOrFail(toRevive.convertToSoldier(ModEntityTypes.ZOMBIE_CLAY_SOLDIER_ENTITY.get(), (convertedTo) -> {
            convertedTo.setClayTeamType(reviver.getClayTeamType());
            convertedTo.setPreviousTeam(toRevive.getClayTeamType());
            convertedTo.setCurable(curable);
        }));
    }

    private static ReviveResult medicRevive(ServerLevel level, AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
        return ReviveResult.soldierOrFail(toRevive.convertToSoldier(ModEntityTypes.CLAY_SOLDIER_ENTITY.get(), (convertedTo) -> {
            convertedTo.setClayTeamType(reviver.getClayTeamType());
            convertedTo.setHealth(convertedTo.getMaxHealth() / 2);
        }));
    }

    private static ReviveResult wraithConversion(ServerLevel level, AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
        return ReviveResult.wraithOrFail(ClayWraithEntity.spawnWraith(level, toRevive, 10));
    }

    private static boolean medicPredicate(AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
        return sameTeam(toRevive, reviver) && toRevive.isOnFire() && !toRevive.isIgnited();
    }

    private static boolean sameTeam(AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver) {
        return toRevive.sameTeamAs(reviver);
    }

    @FunctionalInterface
    public interface ReviveFunction {
        ReviveResult revive(ServerLevel level, AbstractClaySoldierEntity toRevive, AbstractClaySoldierEntity reviver);
    }
}
