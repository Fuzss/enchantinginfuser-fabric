package fuzs.enchantinginfuser;

import fuzs.enchantinginfuser.config.ServerConfig;
import fuzs.enchantinginfuser.network.client.message.C2SAddEnchantLevelMessage;
import fuzs.enchantinginfuser.network.message.S2CCompatibleEnchantsMessage;
import fuzs.enchantinginfuser.registry.ModRegistry;
import fuzs.puzzleslib.config.AbstractConfig;
import fuzs.puzzleslib.config.ConfigHolder;
import fuzs.puzzleslib.config.ConfigHolderImpl;
import fuzs.puzzleslib.network.MessageDirection;
import fuzs.puzzleslib.network.NetworkHandler;
import net.fabricmc.api.ModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class EnchantingInfuser implements ModInitializer {
    public static final String MOD_ID = "enchantinginfuser";
    public static final String MOD_NAME = "Enchanting Infuser";
    public static final Logger LOGGER = LogManager.getLogger(MOD_NAME);

    public static final NetworkHandler NETWORK = NetworkHandler.of(MOD_ID);
    @SuppressWarnings("Convert2MethodRef")
    public static final ConfigHolder<AbstractConfig, ServerConfig> CONFIG = ConfigHolder.server(() -> new ServerConfig());

    @Override
    public void onInitialize() {
        onConstructMod();
    }

    public static void onConstructMod() {
        ((ConfigHolderImpl<?, ?>) CONFIG).addConfigs(MOD_ID);
        registerMessages();
        ModRegistry.touch();
    }

    private static void registerMessages() {
        NETWORK.register(S2CCompatibleEnchantsMessage.class, S2CCompatibleEnchantsMessage::new, MessageDirection.TO_CLIENT);
        NETWORK.register(C2SAddEnchantLevelMessage.class, C2SAddEnchantLevelMessage::new, MessageDirection.TO_SERVER);
    }
}
