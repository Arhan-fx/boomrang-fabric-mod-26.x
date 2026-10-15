package net.arhanfx.boomrang.datagen;

import net.arhanfx.boomrang.item.ModItems;
import net.fabricmc.fabric.api.client.datagen.v1.provider.FabricModelProvider;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ModelTemplates;

public final class ModModelProvider extends FabricModelProvider {

    public ModModelProvider(FabricPackOutput output) {
        super(output);
    }

    @Override
    public void generateBlockStateModels(BlockModelGenerators blockModelGenerators) {
    }

    @Override
    public void generateItemModels(ItemModelGenerators itemModelGenerators) {
        itemModelGenerators.generateFlatItem(
                ModItems.TNT_BOOMERANG,
                ModelTemplates.FLAT_ITEM
        );
        itemModelGenerators.generateFlatItem(
                ModItems.ENDER_BOOMERANG,
                ModelTemplates.FLAT_ITEM
        );
        itemModelGenerators.generateFlatItem(
                ModItems.FIRE_BOOMERANG,
                ModelTemplates.FLAT_ITEM
        );
        itemModelGenerators.generateFlatItem(
                ModItems.ICE_BOOMERANG,
                ModelTemplates.FLAT_ITEM
        );
    }

    @Override
    public String getName() {
        return "BOOMERANG Item Models";
    }
}
