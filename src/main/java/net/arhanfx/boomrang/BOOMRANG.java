
package net.arhanfx.boomrang;

import net.arhanfx.boomrang.entity.ModEntityTypes;
import net.arhanfx.boomrang.item.ModItems;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BOOMRANG implements ModInitializer {

	public static final String MOD_ID = "boomerang";
	public static final Logger LOGGER =
			LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Initializing BOOMERANG mod");

		ModItems.registerModItems();
		ModEntityTypes.registerModEntityTypes();
	}
}