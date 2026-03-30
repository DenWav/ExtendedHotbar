/*
 * This file is part of ExtendedHotbar, a FabricMC mod.
 * Copyright (C) 2023 Kyle Wood (DenWav)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, version 3 only.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <https://www.gnu.org/licenses/>.
 */

package dev.denwav.extendedhotbar.client;

import dev.denwav.extendedhotbar.ExtendedHotbarState;
import dev.denwav.extendedhotbar.ModConfig;
import dev.denwav.extendedhotbar.Util;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.CreativeInventoryScreen;
import net.minecraft.client.gui.screen.ingame.HorseScreen;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;

import static org.lwjgl.glfw.GLFW.*;

@Environment(EnvType.CLIENT)
public class ExtendedHotbarClient implements ClientModInitializer {

    private static final KeyBinding swapKeyBinding = new KeyBinding(
            "key.extendedhotbar.switch",
            InputUtil.Type.KEYSYM,
            GLFW_KEY_R,
            KeyBinding.Category.create(Identifier.of("category.extendedhotbar"))
    );

    private static final KeyBinding toggleKeyBinding = new KeyBinding(
            "key.extendedhotbar.toggle",
            InputUtil.Type.KEYSYM,
            GLFW_KEY_EQUAL,
            KeyBinding.Category.create(Identifier.of("key.extendedhotbar"))
    );

    // Track if we've swapped items for inventory display in fluent mode
    private static boolean fluentInventorySwapped = false;

    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(swapKeyBinding);
        KeyBindingHelper.registerKeyBinding(toggleKeyBinding);

        Util.configHolder = AutoConfig.register(ModConfig.class, Toml4jConfigSerializer::new);
        // Use "config" to hold state because it's simple
        Util.stateHolder = AutoConfig.register(ExtendedHotbarState.class, Toml4jConfigSerializer::new);

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ScreenEvents.BEFORE_INIT.register(this::onScreenOpen);
    }

    private void onTick(final MinecraftClient client) {
        final ModConfig config = Util.configHolder.getConfig();
        if (toggleKeyBinding.wasPressed()) {
            config.enabled = !config.enabled;
            Util.configHolder.save();
            return;
        }

        if (!config.enabled || config.fluent) {
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

    private void onScreenOpen(
            final MinecraftClient client,
            final Screen screen,
            final int scaledWidth,
            final int scaledHeight
    ) {
        if (!(screen instanceof InventoryScreen) && !(screen instanceof HorseScreen)) {
            return;
        }
        final ClientPlayerInteractionManager manager = client.interactionManager;
        if (manager != null && client.player.isInCreativeMode()) {
            if (!(screen instanceof CreativeInventoryScreen)) {
                return;
            }
        }

        // Handle fluent mode inventory opening
        if (Util.isFluent()) {
            // In fluent mode, we should NOT swap items when opening inventory
            // The inventory should show the actual state of the items
            // No swapping needed - just track that inventory is open
            Util.setFluentInventorySwapped(false);
        } else {
            // Handle non-fluent mode
            if (Util.isRenderSwapped()) {
                Util.resetRenderedPosition();
                Util.performSwap(client, true);
            }
        }

        ScreenEvents.remove(screen).register(this::onScreenClose);
    }

    private void onScreenClose(final Screen screen) {
        // Handle fluent mode inventory closing
        if (Util.isFluent()) {
            // In fluent mode, no swapping should happen when closing inventory
            // The items should remain in their actual positions
            // Just reset the flag
            Util.setFluentInventorySwapped(true);
        } else {
            // Handle non-fluent mode
            if (Util.isRenderSwapped()) {
                Util.resetRenderedPosition();
                Util.performSwap(MinecraftClient.getInstance(), true);
            }
        }
    }

}
