package net.arhanfx.boomrang;

import net.arhanfx.boomrang.datagen.ModModelProvider;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
public final class BOOMRANGDataGenerator
		implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(
			FabricDataGenerator dataGenerator
	) {
		FabricDataGenerator.Pack pack = dataGenerator.createPack();
		pack.addProvider(ModModelProvider::new);
	}
}
