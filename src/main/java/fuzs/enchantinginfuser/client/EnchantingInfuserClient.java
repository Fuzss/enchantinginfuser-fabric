package fuzs.enchantinginfuser.client;

import fuzs.enchantinginfuser.client.gui.screens.inventory.InfuserScreen;
import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserItemRenderer;
import fuzs.enchantinginfuser.client.renderer.blockentity.InfuserRenderer;
import fuzs.enchantinginfuser.registry.ModRegistry;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.rendering.v1.BlockEntityRendererRegistry;
import net.fabricmc.fabric.api.client.screenhandler.v1.ScreenRegistry;
import net.fabricmc.fabric.api.event.client.ClientSpriteRegistryCallback;
import net.minecraft.client.renderer.texture.TextureAtlas;

public class EnchantingInfuserClient implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        onClientSetup();
    }

    public static void onClientSetup() {
        ScreenRegistry.register(ModRegistry.INFUSING_MENU_TYPE, InfuserScreen::new);
        BlockEntityRendererRegistry.register(ModRegistry.INFUSER_BLOCK_ENTITY_TYPE, InfuserItemRenderer::new);
        ClientSpriteRegistryCallback.event(InfuserRenderer.BOOK_LOCATION.atlasLocation()).register((TextureAtlas atlasTexture, ClientSpriteRegistryCallback.Registry registry) -> {
            registry.register(InfuserRenderer.BOOK_LOCATION.texture());
        });
    }
}
