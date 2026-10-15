package net.arhanfx.boomrang.item;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.arhanfx.boomrang.entity.BoomerangProjectile;
public class BoomerangItem extends Item {
    public BoomerangItem(Item.Properties properties) {
        super(properties);
    }
    @Override
    public InteractionResult use(
            Level level,
            Player player,
            InteractionHand hand
    ) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel serverLevel) {
            Projectile.spawnProjectileFromRotation(
                    BoomerangProjectile::new,
                    serverLevel,
                    stack,
                    player,
                    0.0F,
                    1.8F,
                    0.0F
            );
            if (!player.getAbilities().instabuild) {
                stack.consume(1, player);
            }
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        return InteractionResult.SUCCESS;
    }
}