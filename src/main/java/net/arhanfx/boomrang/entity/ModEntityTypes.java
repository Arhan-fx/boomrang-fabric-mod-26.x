package net.arhanfx.boomrang.entity;

import net.arhanfx.boomrang.BOOMRANG;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntityTypes {

    private static final ResourceKey<EntityType<?>> SIMPLE_BOOMERANG_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(BOOMRANG.MOD_ID, "simple_boomerang_projectile"));
    public static final EntityType<BoomerangProjectile> SIMPLE_BOOMERANG_PROJECTILE = Registry.register(BuiltInRegistries.ENTITY_TYPE, SIMPLE_BOOMERANG_KEY, EntityType.Builder.<BoomerangProjectile>of(BoomerangProjectile::new, MobCategory.MISC).sized(0.25F, 0.25F).clientTrackingRange(4).updateInterval(1).build(SIMPLE_BOOMERANG_KEY));

    public static void registerModEntityTypes() {
        BOOMRANG.LOGGER.info("Registering projectile entities for " + BOOMRANG.MOD_ID);
    }
}