package adudecalledleo.dontdropit;

import adudecalledleo.dontdropit.config.ModConfig;
import adudecalledleo.dontdropit.util.FavoritesUtil;
import adudecalledleo.lionutils.LoggerUtil;
import adudecalledleo.lionutils.config.ConfigHolder;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

public class DontDropItMod implements ClientModInitializer {
    public static final String MOD_ID = "dontdropit";
    public static final String MOD_NAME = "Don't Drop It!";

    public static Logger LOGGER = LoggerUtil.getLogger(MOD_NAME);
    public static final ConfigHolder<ModConfig> CONFIG_HOLDER = ConfigHolder.builder(MOD_ID + ".json",
            ModConfig.class, ModConfig::new).setLogger(LOGGER).build();

    public static final KeyBinding keyDropStack = new KeyBinding("key.dontdropit.dropStack",
            GLFW.GLFW_KEY_LEFT_CONTROL, "key.categories.dontdropit");
    public static final KeyBinding keyForceDrop = new KeyBinding("key.dontdropit.forceDrop",
            GLFW.GLFW_KEY_LEFT_ALT, "key.categories.dontdropit");
    public static final KeyBinding keyToggleDropDelay = new KeyBinding("key.dontdropit.toggleDropDelay",
            GLFW.GLFW_KEY_UNKNOWN, "key.categories.dontdropit");

    @Override
    public void onInitializeClient() {
        FavoritesUtil.addConfigListener();
        CONFIG_HOLDER.load();
        KeyBindingHelper.registerKeyBinding(keyDropStack);
        KeyBindingHelper.registerKeyBinding(keyForceDrop);
        KeyBindingHelper.registerKeyBinding(keyToggleDropDelay);
        ClientTickEvents.END_CLIENT_TICK.register(DropHandler::onClientTick);
        LOGGER.info("Don't drop that Diamond Pickaxe!");
    }
}
