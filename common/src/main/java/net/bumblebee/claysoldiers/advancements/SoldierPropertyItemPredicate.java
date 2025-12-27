package net.bumblebee.claysoldiers.advancements;

import com.mojang.serialization.Codec;
import net.bumblebee.claysoldiers.ClaySoldiersCommon;
import net.bumblebee.claysoldiers.claysoldierpredicate.ClayPredicates;
import net.bumblebee.claysoldiers.soldierproperties.SoldierPropertyType;
import net.bumblebee.claysoldiers.soldierproperties.customproperties.AttackTypeProperty;
import net.minecraft.advancements.critereon.ItemSubPredicate;
import net.minecraft.world.item.ItemStack;

public class SoldierPropertyItemPredicate implements ItemSubPredicate {
    public static final Codec<SoldierPropertyItemPredicate> CODEC = ClayPredicates.SoldierPropertyPredicate.CODEC.xmap(SoldierPropertyItemPredicate::new, SoldierPropertyItemPredicate::getSoldierPropertyPredicate);
    private final ClayPredicates.SoldierPropertyPredicate soldierPropertyPredicate;

    public SoldierPropertyItemPredicate(ClayPredicates.SoldierPropertyPredicate soldierPropertyPredicate) {
        this.soldierPropertyPredicate = soldierPropertyPredicate;
    }

    public static <T> SoldierPropertyItemPredicate hasProperty(SoldierPropertyType<T> propertyType, T value) {
        return new SoldierPropertyItemPredicate(ClayPredicates.SoldierPropertyPredicate.isExactly(propertyType, value));
    }

    private ClayPredicates.SoldierPropertyPredicate getSoldierPropertyPredicate() {
        return soldierPropertyPredicate;
    }

    @Override
    public boolean matches(ItemStack itemStack) {
        var holdable = ClaySoldiersCommon.DATA_MAP.getEffect(itemStack);
        if (holdable == null) {
            return false;
        } else {
            return soldierPropertyPredicate.testMap(holdable.properties(), AttackTypeProperty.NORMAL);
        }
    }
}
