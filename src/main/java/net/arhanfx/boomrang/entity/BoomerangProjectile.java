package net.arhanfx.boomrang.entity;

import java.util.concurrent.ThreadLocalRandom;

import net.arhanfx.boomrang.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class BoomerangProjectile extends ThrowableItemProjectile {

    private static final int MAX_OUTBOUND_TICKS = 20;
    private static final int MIN_RETURN_TICKS = 14;
    private static final int MAX_RETURN_TICKS = 40;

    private static final double OUTBOUND_CURVE = 0.012;
    private static final double ARRIVAL_DISTANCE = 0.8;

    private static final float SIMPLE_DAMAGE = 4.0F;
    private static final float ENDER_DAMAGE = 4.0F;
    private static final float FIRE_DAMAGE = 6.0F;
    private static final float ICE_DAMAGE = 4.0F;

    private static final float TNT_EXPLOSION_POWER = 4.0F;

    private double returnX;
    private double returnY;
    private double returnZ;

    private double startX;
    private double startY;
    private double startZ;

    private double control1X;
    private double control1Y;
    private double control1Z;

    private double control2X;
    private double control2Y;
    private double control2Z;

    private int outboundTicks;
    private int returnTicks;
    private int returnDuration = 24;

    private boolean returning;
    private boolean dropItemOnReturn = true;

    private double curveDirection =
            ThreadLocalRandom.current().nextBoolean() ? 1.0 : -1.0;

    public BoomerangProjectile(
            EntityType<? extends ThrowableItemProjectile> type,
            Level level
    ) {
        super(type, level);
        noPhysics = level.isClientSide();
        setNoGravity(level.isClientSide());
    }

    public BoomerangProjectile(
            Level level,
            LivingEntity owner,
            ItemStack itemStack
    ) {
        super(
                ModEntityTypes.SIMPLE_BOOMERANG_PROJECTILE,
                owner,
                level,
                itemStack
        );

        noPhysics = level.isClientSide();
        setNoGravity(level.isClientSide());

        BlockPos launchBlock = owner.blockPosition().below();

        returnX = launchBlock.getX() + 0.5;
        returnY = launchBlock.getY() + 1.05;
        returnZ = launchBlock.getZ() + 0.5;

        dropItemOnReturn =
                !(owner instanceof Player player)
                        || !player.getAbilities().instabuild;
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SIMPLE_BOOMERANG;
    }

    private boolean isItem(Item item) {
        return getItem().is(item);
    }

    private boolean isTntBoomerang() {
        return isItem(ModItems.TNT_BOOMERANG);
    }

    @Override
    public void tick() {
        super.tick();

        if (!(level() instanceof ServerLevel serverLevel)
                || isRemoved()) {
            return;
        }

        // Outbound flight.
        if (!returning) {
            outboundTicks++;
            applyOutboundCurve();

            if (outboundTicks >= MAX_OUTBOUND_TICKS) {
                beginReturning();
            }
        }

        // Curved return flight.
        if (returning) {
            returnTicks++;

            double progress = Math.min(
                    1.0,
                    (double) returnTicks / returnDuration
            );

            Vec3 point = getReturnCurvePoint(progress);
            setPos(point.x, point.y, point.z);

            if (returnTicks >= returnDuration) {
                finishReturning(serverLevel);
                return;
            }

            double nextProgress = Math.min(
                    1.0,
                    (double) (returnTicks + 1) / returnDuration
            );

            Vec3 nextPoint = getReturnCurvePoint(nextProgress);
            setDeltaMovement(nextPoint.subtract(point));
        }
    }

    private void applyOutboundCurve() {
        Vec3 velocity = getDeltaMovement();

        Vec3 horizontal = new Vec3(
                velocity.x,
                0.0,
                velocity.z
        );

        if (horizontal.lengthSqr() < 1.0E-8) {
            return;
        }

        Vec3 sideways = new Vec3(
                -horizontal.z,
                0.0,
                horizontal.x
        ).normalize().scale(OUTBOUND_CURVE * curveDirection);

        setDeltaMovement(velocity.add(sideways));
    }

    private void beginReturning() {
        if (returning) {
            return;
        }

        returning = true;
        noPhysics = true;
        setNoGravity(true);

        // Random landing destination in a 3x3 horizontal area.
        if (level() instanceof ServerLevel) {
            int centerX = (int) Math.floor(returnX);
            int centerZ = (int) Math.floor(returnZ);

            int offsetX = ThreadLocalRandom.current().nextInt(-1, 2);
            int offsetZ = ThreadLocalRandom.current().nextInt(-1, 2);

            returnX = centerX + offsetX + 0.5;
            returnZ = centerZ + offsetZ + 0.5;
        }

        Vec3 start = position();
        Vec3 target = new Vec3(returnX, returnY, returnZ);
        Vec3 toTarget = target.subtract(start);

        Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z);

        if (horizontal.lengthSqr() < 1.0E-8) {
            Vec3 velocity = getDeltaMovement();
            horizontal = new Vec3(velocity.x, 0.0, velocity.z);
        }

        if (horizontal.lengthSqr() < 1.0E-8) {
            horizontal = new Vec3(1.0, 0.0, 0.0);
        }

        Vec3 direction = horizontal.normalize();

        Vec3 sideways = new Vec3(
                -direction.z,
                0.0,
                direction.x
        ).scale(curveDirection);

        double horizontalDistance = horizontal.length();

        double sideAmount = Math.max(1.0, Math.min(6.0, horizontalDistance * 0.18));

        double forwardAmount = Math.min(
                5.0,
                horizontalDistance * 0.25
        );

        double arcHeight = Math.max(1.0, Math.min(4.0, horizontalDistance * 0.10));

        startX = start.x;
        startY = start.y;
        startZ = start.z;

        Vec3 control1 = start
                .add(direction.scale(forwardAmount))
                .add(sideways.scale(sideAmount))
                .add(0.0, arcHeight, 0.0);

        control1X = control1.x;
        control1Y = control1.y;
        control1Z = control1.z;

        Vec3 control2 = target
                .subtract(direction.scale(forwardAmount))
                .subtract(sideways.scale(sideAmount))
                .add(0.0, arcHeight, 0.0);

        control2X = control2.x;
        control2Y = control2.y;
        control2Z = control2.z;

        returnDuration = Math.max(MIN_RETURN_TICKS, Math.min(MAX_RETURN_TICKS, (int) Math.ceil(horizontalDistance * 0.7) + 12));

        returnTicks = 0;
    }

    private Vec3 getReturnCurvePoint(double t) {
        double inverse = 1.0 - t;

        Vec3 p0 = new Vec3(startX, startY, startZ);
        Vec3 p1 = new Vec3(control1X, control1Y, control1Z);
        Vec3 p2 = new Vec3(control2X, control2Y, control2Z);
        Vec3 p3 = new Vec3(returnX, returnY, returnZ);

        return p0.scale(inverse * inverse * inverse)
                .add(p1.scale(3.0 * inverse * inverse * t))
                .add(p2.scale(3.0 * inverse * t * t))
                .add(p3.scale(t * t * t));
    }

    @Override
    protected void onHitEntity(EntityHitResult hitResult) {
        super.onHitEntity(hitResult);

        if (returning) {
            return;
        }

        Entity target = hitResult.getEntity();

        if (level() instanceof ServerLevel serverLevel) {

            // TNT: explode on entity contact, then return.
            if (isTntBoomerang()) {
                explodeTnt(serverLevel);
                beginReturning();
                return;
            }

            if (target != getOwner()) {
                float damage = getImpactDamage();

                target.hurtServer(
                        serverLevel,
                        serverLevel.damageSources()
                                .thrown(this, getOwner()),
                        damage
                );

                // Ender: teleport the target upward so it can fall.
                if (isItem(ModItems.ENDER_BOOMERANG)) {
                    launchTargetIntoSky(serverLevel, target);
                }

                // Fire: ignite after dealing impact damage.
                if (isItem(ModItems.FIRE_BOOMERANG)) {
                    target.igniteForSeconds(5);
                }

                // Ice: freeze the target for an extended period.
                if (isItem(ModItems.ICE_BOOMERANG)) {
                    int frozenTicks =
                            target.getTicksRequiredToFreeze() + 60;

                    target.setTicksFrozen(
                            Math.max(target.getTicksFrozen(), frozenTicks)
                    );
                }
            }
        }

        beginReturning();
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);

        if (returning) {
            return;
        }

        // TNT: explode on surface contact, then return.
        if (isTntBoomerang()
                && level() instanceof ServerLevel serverLevel) {
            explodeTnt(serverLevel);
        }

        beginReturning();
    }

    private float getImpactDamage() {
        if (isItem(ModItems.FIRE_BOOMERANG)) {
            return FIRE_DAMAGE;
        }

        if (isItem(ModItems.ICE_BOOMERANG)) {
            return ICE_DAMAGE;
        }

        if (isItem(ModItems.ENDER_BOOMERANG)) {
            return ENDER_DAMAGE;
        }

        return SIMPLE_DAMAGE;
    }

    private void explodeTnt(ServerLevel serverLevel) {
        serverLevel.explode(
                this,
                getX(),
                getY(),
                getZ(),
                TNT_EXPLOSION_POWER,
                Level.ExplosionInteraction.TNT
        );
    }

    private void launchTargetIntoSky(
            ServerLevel serverLevel,
            Entity target
    ) {
        // Stay below the top of the world's build height.
        double skyY = Math.min(
                target.getY() + 100.0,
                serverLevel.getMaxY() - target.getBbHeight() - 1.0
        );

        if (skyY <= target.getY()) {
            return;
        }

        target.teleportTo(
                target.getX(),
                skyY,
                target.getZ()
        );

        // Start a fresh fall from the new height.
        target.resetFallDistance();
        target.setDeltaMovement(Vec3.ZERO);
        target.setOnGround(false);
    }

    private void finishReturning(ServerLevel serverLevel) {
        if (dropItemOnReturn) {
            ItemStack returnedStack = getItem().copy();

            if (!returnedStack.isEmpty()) {
                // Each completed throw costs one durability point.
                int maxDamage = returnedStack.getMaxDamage();

                if (maxDamage > 0) {
                    int nextDamage = returnedStack.getDamageValue() + 1;

                    // A broken boomerang is not dropped.
                    if (nextDamage >= maxDamage) {
                        discard();
                        return;
                    }

                    returnedStack.setDamageValue(nextDamage);
                }

                ItemEntity droppedItem = new ItemEntity(
                        serverLevel,
                        returnX,
                        returnY,
                        returnZ,
                        returnedStack
                );

                droppedItem.setDefaultPickUpDelay();
                serverLevel.addFreshEntity(droppedItem);
            }
        }

        discard();
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);

        output.putDouble("return_x", returnX);
        output.putDouble("return_y", returnY);
        output.putDouble("return_z", returnZ);

        output.putDouble("start_x", startX);
        output.putDouble("start_y", startY);
        output.putDouble("start_z", startZ);

        output.putDouble("control1_x", control1X);
        output.putDouble("control1_y", control1Y);
        output.putDouble("control1_z", control1Z);

        output.putDouble("control2_x", control2X);
        output.putDouble("control2_y", control2Y);
        output.putDouble("control2_z", control2Z);

        output.putDouble("curve_direction", curveDirection);

        output.putInt("outbound_ticks", outboundTicks);
        output.putInt("return_ticks", returnTicks);
        output.putInt("return_duration", returnDuration);

        output.putBoolean("returning", returning);
        output.putBoolean("drop_item_on_return", dropItemOnReturn);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);

        returnX = input.getDoubleOr("return_x", getX());
        returnY = input.getDoubleOr("return_y", getY());
        returnZ = input.getDoubleOr("return_z", getZ());

        startX = input.getDoubleOr("start_x", getX());
        startY = input.getDoubleOr("start_y", getY());
        startZ = input.getDoubleOr("start_z", getZ());

        control1X = input.getDoubleOr("control1_x", getX());
        control1Y = input.getDoubleOr("control1_y", getY());
        control1Z = input.getDoubleOr("control1_z", getZ());

        control2X = input.getDoubleOr("control2_x", getX());
        control2Y = input.getDoubleOr("control2_y", getY());
        control2Z = input.getDoubleOr("control2_z", getZ());

        curveDirection = input.getDoubleOr("curve_direction", 1.0);

        outboundTicks = input.getIntOr("outbound_ticks", 0);
        returnTicks = input.getIntOr("return_ticks", 0);

        returnDuration = Math.max(
                1,
                input.getIntOr("return_duration", 24)
        );

        returning = input.getBooleanOr("returning", false);
        dropItemOnReturn =
                input.getBooleanOr("drop_item_on_return", true);

        noPhysics = returning || level().isClientSide();
        setNoGravity(returning || level().isClientSide());
    }
}
