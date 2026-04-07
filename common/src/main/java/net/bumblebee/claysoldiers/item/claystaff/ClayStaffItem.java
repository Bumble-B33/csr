package net.bumblebee.claysoldiers.item.claystaff;

import net.bumblebee.claysoldiers.entity.common.boss.ClayBlockProjectileEntity;
import net.bumblebee.claysoldiers.init.ModDataComponents;
import net.bumblebee.claysoldiers.init.ModEnchantments;
import net.bumblebee.claysoldiers.init.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.function.Predicate;

public class ClayStaffItem extends Item {
    private static final int MIN_BLOCK_SIZE = 1;
    private static final int MAX_BLOCK_SIZE = 5;
    private static final Predicate<ItemStack> PROJECTILE_PREDICATE = stack -> stack.is(Items.CLAY_BALL);
    public static final Predicate<ItemStack> SOLDIER_PREDICATE = stack -> {
        if (ModItems.CLAY_SOLDIER.is(stack)) {
            return true;
        }
        var pouchContent = stack.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
        return pouchContent != null && pouchContent.getItem() == ModItems.CLAY_SOLDIER.get() && pouchContent.getCount() > 0;
    };

    public static final int MAX_HOLD_DURATION = 72000;

    public ClayStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.BOW;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, BlockState state, Level level, BlockPos pos, LivingEntity entity) {
        if (entity instanceof Player player) {
            return !player.isCreative();
        }
        return false;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MAX_HOLD_DURATION;
    }

    @Override
    public boolean releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeRemaining) {
        if (!(livingEntity instanceof Player player) || level.isClientSide()) {
            return false;
        }
        boolean infiniteMaterials = livingEntity.hasInfiniteMaterials();

        ItemStack ammo = null;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack ammoSlot = player.getInventory().getItem(i);
            Predicate<ItemStack> ammoPredicate = getAmmoPredicate(stack, level.registryAccess());
            if (ammoPredicate.test(ammoSlot)) {
                var content = ammoSlot.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
                if (content != null) {
                    ammo = content.createStack(level.registryAccess());
                } else {
                    ammo = ammoSlot;
                }
                if (!infiniteMaterials) {
                    if (content == null) {
                        player.getInventory().removeItem(i, 1);
                    } else {
                        ammoSlot.set(ModDataComponents.CLAY_POUCH_CONTENT.get(), content.shrink(1, content.getMaxCapacity()));
                    }
                }

                break;
            }
        }
        if (ammo == null && infiniteMaterials) {
            ammo = getEnchantmentLevel(stack, ModEnchantments.SOLDIER_PROJECTILE, level.registryAccess()) > 0 ? ModItems.CLAY_SOLDIER.get().getDefaultInstance() : Items.CLAY_BALL.getDefaultInstance();
        }

        if (ammo != null) {
            shootBlock(livingEntity, level, stack, timeRemaining, ammo.get(ModDataComponents.CLAY_MOB_TEAM_COMPONENT.get()));
            return true;
        }
        return false;
    }

    public static ItemStack getClayStaffAmmo(Predicate<ItemStack> ammoPredicate, Player player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack ammoSlot = player.getInventory().getItem(i);
            if (ammoPredicate.test(ammoSlot)) {
                var content = ammoSlot.get(ModDataComponents.CLAY_POUCH_CONTENT.get());
                ItemStack ammo;
                if (content != null) {
                    ammo = content.createStack(player.registryAccess());
                } else {
                    ammo = ammoSlot;
                }
                return ammo;
            }
        }
        return null;
    }

    protected void shootBlock(LivingEntity shooter, Level level, ItemStack firedFrom, int timeRemaining, @Nullable ResourceLocation clayTeam) {
        int pierce = getEnchantmentLevel(firedFrom, Enchantments.PIERCING, level.registryAccess());

        float blockSize = ((MAX_HOLD_DURATION - timeRemaining) * 3f) / getMaxPower(firedFrom, level.registryAccess());
        blockSize = Mth.clamp(blockSize + 1f, MIN_BLOCK_SIZE, MAX_BLOCK_SIZE);
        shootBlock(shooter, level, blockSize, 0f, pierce, clayTeam, firedFrom);
        if (getEnchantmentLevel(firedFrom, Enchantments.MULTISHOT, level.registryAccess()) > 0) {
            shootBlock(shooter, level, blockSize, -15.0f, pierce, clayTeam, firedFrom);
            shootBlock(shooter, level, blockSize, 15.0f, pierce, clayTeam, firedFrom);
        }
    }

    /**
     * Shoots a {@code ClayBlockProjectile}
     * @param blockSize Value in range [{@value MIN_BLOCK_SIZE} - {@value MAX_BLOCK_SIZE}]
     * @param yAngleOffset y angle offset in degree
     */
    protected void shootBlock(LivingEntity shooter, Level level, float blockSize, float yAngleOffset, int pierce, @Nullable ResourceLocation clayTeam, ItemStack firedFrom) {

        ClayBlockProjectileEntity clayBlock = new ClayBlockProjectileEntity(level, shooter, shooter.getEyeHeight());
        clayBlock.shootFromRotation(shooter, shooter.getXRot(), shooter.getYRot() + yAngleOffset, 0.0F, blockSize / 2f, 1.0F);
        clayBlock.setFiredFrom(firedFrom);

        if (clayTeam != null) {
            clayBlock.setClayTeam(clayTeam);
        } else {
            clayBlock.setBlockSize(blockSize);
        }
        clayBlock.setPierceCount(pierce);

        level.addFreshEntity(clayBlock);
    }

    public static int getMaxPower(ItemStack stack, RegistryAccess registryAccess) {
        return switch (getEnchantmentLevel(stack, Enchantments.QUICK_CHARGE, registryAccess)) {
            case 0 -> 40;
            case 1 -> 30;
            case 2 -> 20;
            default -> 10;
        };
    }

    public static int getEnchantmentLevel(ItemStack stack, ResourceKey<Enchantment> key, HolderLookup.Provider registries) {
        return registries.lookup(Registries.ENCHANTMENT)
                .map(r -> r.get(key)
                        .map(h -> stack.getEnchantments().getLevel(h))
                        .orElse(0))
                .orElse(0);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand usedHand) {
        ItemStack itemInHand = player.getItemInHand(usedHand);
        Predicate<ItemStack> ammoPredicate = getAmmoPredicate(itemInHand, player.registryAccess());
        if (player.hasInfiniteMaterials() || player.getInventory().contains(ammoPredicate)) {
            player.startUsingItem(usedHand);

            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }

    private static Predicate<ItemStack> getAmmoPredicate(ItemStack stack, RegistryAccess registries) {
        return getEnchantmentLevel(stack, ModEnchantments.SOLDIER_PROJECTILE, registries) > 0 ? SOLDIER_PREDICATE : PROJECTILE_PREDICATE;
    }
}