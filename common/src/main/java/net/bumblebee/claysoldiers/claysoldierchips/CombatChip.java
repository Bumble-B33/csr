package net.bumblebee.claysoldiers.claysoldierchips;

import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddon;
import net.bumblebee.claysoldiers.claysoldierchips.addon.ClaySoldierChipAddons;
import net.bumblebee.claysoldiers.entity.common.ClayMobEntity;
import net.bumblebee.claysoldiers.entity.common.programmable.ProgrammableClaySoldierEntity;
import net.bumblebee.claysoldiers.entity.goal.ClaySoldierMeleeAttackGoal;
import net.bumblebee.claysoldiers.entity.goal.ClaySoldierRangedAttackGoal;
import net.bumblebee.claysoldiers.entity.goal.target.ClayMobOwnerHurtByTarget;
import net.bumblebee.claysoldiers.entity.goal.target.ClayMobOwnerTarget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.Monster;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.OptionalInt;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class CombatChip extends ClaySoldierChip {
    private static final Identifier ASSET_OWNER_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "combat_owner");
    private static final Identifier ASSET_MONSTER_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "combat_monster");
    private static final Identifier ASSET_ANIMAL_ID = Identifier.fromNamespaceAndPath(ClaySoldiersCommon.MOD_ID, "combat_animal");
    private static final CombatChip NO_ADDON = new CombatChip(List.of());
    private static final int DEFAULT_SEARCH_RANGE = 10;

    public static final String COMBAT_DATA_ANIMAL_LANG = LANG_PREFIX + "combat.animal";
    public static final String COMBAT_DATA_MONSTER_LANG = LANG_PREFIX + "combat.monster";
    public static final String COMBAT_DATA_IGNORE_BABIES_LANG = LANG_PREFIX + "combat.ignore_babies";
    public static final String COMBAT_DATA_OWNER_LANG = LANG_PREFIX + "combat.owner";

    public static final AddonInfo ADDON_INFO = new AddonInfo() {
        @Override
        public boolean canBeApplied(List<ClaySoldierChipAddon> presentAddons, ClaySoldierChipAddon addon) {
            if (presentAddons.contains(addon)) {
                return false;
            }
            if (addon == ClaySoldierChipAddons.TARGET_MONSTER_ADDON) {
                return !presentAddons.contains(ClaySoldierChipAddons.TARGET_ANIMALS_ADDON);
            }

            if (addon == ClaySoldierChipAddons.TARGET_ANIMALS_ADDON) {
                return !presentAddons.contains(ClaySoldierChipAddons.TARGET_MONSTER_ADDON);
            }

            if (addon == ClaySoldierChipAddons.TARGET_IGNORE_BABIES_ADDON) {
                return presentAddons.contains(ClaySoldierChipAddons.TARGET_ANIMALS_ADDON);
            }

            return false;
        }

        @Override
        public int getAllowedAddonsCount() {
            return 2;
        }
    };

    private final boolean ignoreBabies;
    @NonNull
    private final Target target;


    public CombatChip(List<ClaySoldierChipAddon> addons) {
        super(addons);
        this.ignoreBabies = hasAddon(ClaySoldierChipAddons.TARGET_IGNORE_BABIES_ADDON);

        if (hasAddon(ClaySoldierChipAddons.TARGET_ANIMALS_ADDON)) {
            this.target = Target.ANIMAL;
        } else if (hasAddon(ClaySoldierChipAddons.TARGET_MONSTER_ADDON)) {
            this.target = Target.MONSTER;
        } else {
            this.target = Target.OWNER;
        }
    }

    public static CombatChip create(List<ClaySoldierChipAddon> addons) {
        if (addons.isEmpty()) {
            return NO_ADDON;
        }
        return new CombatChip(addons);
    }

    public static CombatChip create() {
        return NO_ADDON;
    }


    @Override
    public ClaySoldierChip withSoldier(ProgrammableClaySoldierEntity soldier) {
        return this;
    }

    @Override
    public void addSpecialGoals(ServerLevel level, ProgrammableClaySoldierEntity soldier, BiConsumer<Integer, Goal> goalAdder, BiConsumer<Integer, Goal> targetAdder) {
        goalAdder.accept(1, new ClaySoldierRangedAttackGoal(soldier, 1, DEFAULT_SEARCH_RANGE));
        goalAdder.accept(2, new ClaySoldierMeleeAttackGoal(soldier, 1, false));

        if (target == Target.OWNER) {
            targetAdder.accept(1, new ClayMobOwnerHurtByTarget(soldier));
            targetAdder.accept(2, new ClayMobOwnerTarget(soldier));
        } else {
            targetAdder.accept(0, new NearestAttackableTargetGoal<>(soldier, target.targetedClass, false, this::targetSelector));
        }
    }

    public boolean targetSelector(LivingEntity livingEntity, ServerLevel level) {
        return !livingEntity.isBaby() || !ignoreBabies;
    }

    @Override
    public Component getWorkStatusDisplayName(@NonNull ClayMobEntity soldier) {
        MutableComponent targetLang = switch (target) {
            case OWNER -> Component.translatable(COMBAT_DATA_OWNER_LANG);
            case ANIMAL -> Component.translatable(COMBAT_DATA_ANIMAL_LANG);
            case MONSTER -> Component.translatable(COMBAT_DATA_MONSTER_LANG);
        };

        if (ignoreBabies) {
            return targetLang.append(" (").append(COMBAT_DATA_IGNORE_BABIES_LANG).append(")");
        }
        return targetLang;
    }

    @Override
    public Type getType() {
        return ClaySoldierChips.COMBAT_TYPE.get();
    }

    @Override
    public Identifier assetId() {
        return switch (target) {
            case OWNER -> ASSET_OWNER_ID;
            case ANIMAL -> ASSET_ANIMAL_ID;
            case MONSTER -> ASSET_MONSTER_ID;
        };
    }

    @Override
    public OptionalInt getItemColorForLayer(ItemLayer layer) {
        return switch (layer) {
            case BASE -> OptionalInt.of(0xFF0707);
            case CONNECTION -> switch (target) {
                case OWNER -> OptionalInt.of(0xF9F9F9);
                case MONSTER -> OptionalInt.of(0x130E0E);
                case ANIMAL -> OptionalInt.of(0xFFA61B);
            };
        };
    }

    @Override
    public void appendDebugInfo(Consumer<String> appender) {
        super.appendDebugInfo(appender);
    }

    private enum Target {
        OWNER(null),
        ANIMAL(Animal.class),
        MONSTER(Monster.class);

        @Nullable
        private final Class<? extends LivingEntity> targetedClass;

        Target(@Nullable Class<? extends LivingEntity> target) {
            this.targetedClass = target;
        }
    }
}
