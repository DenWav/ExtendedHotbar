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

import dev.denwav.extendedhotbar.ModConfig;
import dev.denwav.extendedhotbar.Util;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.Toml4jConfigSerializer;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;

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
