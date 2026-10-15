package net.arhanfx.boomrang.item;

import java.util.function.Function;

import net.arhanfx.boomrang.BOOMRANG;
import net.fabricmc.fabric.api.creativetab.v1.CreativeModeTabEvents;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final Item SIMPLE_BOOMERANG =
            registerBoomerang("simple_boomerang", 59);
    public static final Item TNT_BOOMERANG =
            registerBoomerang("tnt_boomerang", 131);
    public static final Item ENDER_BOOMERANG =
            registerBoomerang("ender_boomerang", 1561);
    public static final Item FIRE_BOOMERANG =
            registerBoomerang("fire_boomerang", 250);
    public static final Item ICE_BOOMERANG =
            registerBoomerang("ice_boomerang", 32);
    public static final Item RANGE_ENCHANTMENT_BOOK =
            registerPlainItem("range_enchantment_book");
    private static Item registerBoomerang(String name, int durability) {
        return registerItem(
                name,
                BoomerangItem::new,
                new Item.Properties().durability(durability)
        );
    }

    private static Item registerPlainItem(String name) {
        return registerItem(
                name,
                Item::new,
                new Item.Properties()
        );
    }

    private static Item registerItem(
            String name,
            Function<Item.Properties, Item> itemFactory,
            Item.Properties properties
    ) {
        Identifier id = Identifier.fromNamespaceAndPath(BOOMRANG.MOD_ID, name);
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, id);

        Item item = itemFactory.apply(properties.setId(itemKey));

        return Registry.register(BuiltInRegistries.ITEM, id, item);
    }

    public static void registerModItems() {
        BOOMRANG.LOGGER.info(
                "Registering Mod Items for " + BOOMRANG.MOD_ID
        );

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.COMBAT)
                .register(output -> {
                    output.accept(SIMPLE_BOOMERANG);
                    output.accept(TNT_BOOMERANG);
                    output.accept(ENDER_BOOMERANG);
                    output.accept(FIRE_BOOMERANG);
                    output.accept(ICE_BOOMERANG);
                });

        CreativeModeTabEvents.modifyOutputEvent(CreativeModeTabs.INGREDIENTS)
                .register(output ->
                        output.accept(RANGE_ENCHANTMENT_BOOK)
                );
    }
}
