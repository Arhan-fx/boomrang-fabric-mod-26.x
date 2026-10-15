
package net.arhanfx.boomrang;

import net.arhanfx.boomrang.entity.ModEntityTypes;
import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.renderer.entity.EntityRenderers;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;

public class BOOMRANGClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        BOOMRANG.LOGGER.info(
                "Registering Simple Boomerang projectile renderer"
        );

        EntityRenderers.register( ModEntityTypes.SIMPLE_BOOMERANG_PROJECTILE,ThrownItemRenderer::new );
    }
}