package com.demonwav.extendedhotbar.client;

import com.demonwav.extendedhotbar.ModConfig;
import com.demonwav.extendedhotbar.Util;
import com.demonwav.extendedhotbar.mixin.MixinCreativeInventoryScreen;
import me.sargunvohra.mcmods.autoconfig1u.AutoConfig;
import me.sargunvohra.mcmods.autoconfig1u.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemGroup;
import net.minecraft.screen.slot.SlotActionType;

import static org.lwjgl.glfw.GLFW.*;

@Environment(EnvType.CLIENT)
public class ExtendedHotbarClient implements ClientModInitializer {

    private static final KeyBinding swapKeyBinding = new KeyBinding(
        "key.extendedhotbar.switch",
        InputUtil.Type.KEYSYM,
        GLFW_KEY_R,
        "key.extendedhotbar"
    );

    private static final KeyBinding toggleKeyBinding = new KeyBinding(
        "key.extendedhotbar.toggle",
        InputUtil.Type.KEYSYM,
        GLFW_KEY_EQUAL,
        "key.extendedhotbar"
    );

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(swapKeyBinding);
        KeyBindingHelper.registerKeyBinding(toggleKeyBinding);

        Util.configHolder = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(final MinecraftClient client) {
        final ModConfig config = Util.configHolder.getConfig();
        if (toggleKeyBinding.wasPressed()) {
            config.enabled = !config.enabled;
            Util.configHolder.save();
            return;
        }

        if (!config.enabled) {
            return;
        }

        if (client.world == null || client.currentScreen != null || !MinecraftClient.isHudEnabled()) {
            return;
        }

        if (!swapKeyBinding.wasPressed()) {
            return;
        }

        boolean singleSwap;
        if (config.enableModifier) {
            final long window = client.getWindow().getHandle();
            singleSwap = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) != GLFW_PRESS;

            if (config.invert) {
                singleSwap = !singleSwap;
            }
        } else {
            singleSwap = !config.invert;
        }

        Util.performSwap(client, singleSwap);
    }
}
