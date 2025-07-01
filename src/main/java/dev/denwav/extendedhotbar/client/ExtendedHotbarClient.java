/*
 * This file is part of ExtendedHotbar, a FabricMC mod.
 * Copyright (C) 2023 Kyle Wood (DenWav)
 * Copyright (C) 2025 Katherine Brand (unilock)
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

import com.mojang.blaze3d.systems.RenderSystem;
import dev.denwav.extendedhotbar.Util;
import dev.denwav.extendedhotbar.mixin.InGameHudAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import net.minecraft.world.GameMode;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.*;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

import static org.lwjgl.glfw.GLFW.*;

@Mod(value = ExtendedHotbarClient.MOD_ID, dist = Dist.CLIENT)
public final class ExtendedHotbarClient {

	public static final String MOD_ID = "extendedhotbar";

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

	public ExtendedHotbarClient(IEventBus bus) {
		bus.addListener(RegisterKeyMappingsEvent.class, event -> {
			event.register(swapKeyBinding);
			event.register(toggleKeyBinding);
		});

		NeoForge.EVENT_BUS.addListener(ClientTickEvent.Post.class, event -> {
			if (toggleKeyBinding.wasPressed()) {
				Util.config.enabled.setValue(!Util.config.enabled.value());
				Util.config.save();
				return;
			}

			if (!Util.config.enabled.value()) {
				return;
			}

			final MinecraftClient client = MinecraftClient.getInstance();

			if (client.world == null || client.currentScreen != null || !MinecraftClient.isHudEnabled()) {
				return;
			}

			if (!swapKeyBinding.wasPressed()) {
				return;
			}

			boolean singleSwap;
			if (Util.config.enableModifier.value()) {
				final long window = client.getWindow().getHandle();
				singleSwap = glfwGetKey(window, GLFW_KEY_LEFT_SHIFT) != GLFW_PRESS && glfwGetKey(window, GLFW_KEY_RIGHT_SHIFT) != GLFW_PRESS;

				if (Util.config.invert.value()) {
					singleSwap = !singleSwap;
				}
			} else {
				singleSwap = !Util.config.invert.value();
			}

			Util.performSwap(client, singleSwap);
		});

		bus.addListener(RegisterGuiLayersEvent.class, event -> {
			event.registerAbove(VanillaGuiLayers.HOTBAR, Identifier.of(MOD_ID, "hotbar"), (context, tickCounter) -> {
				if (Util.isEnabled() && MinecraftClient.getInstance().interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
					context.getMatrices().translate(0, Util.DISTANCE, 0);

					RenderSystem.enableBlend();
					context.getMatrices().push();

					context.drawGuiTexture(InGameHudAccessor.getHOTBAR_TEXTURE(), context.getScaledWindowWidth() / 2 - 91, context.getScaledWindowHeight() - 22, 182, 22);

					final ClientPlayerEntity player = MinecraftClient.getInstance().player;
					int seed = 1;

					for (int slot = 0; slot < 9; slot++) {
						int x = context.getScaledWindowWidth() / 2 - 90 + slot * 20 + 2;
						int y = context.getScaledWindowHeight() - 16 - 3;
						((InGameHudAccessor) MinecraftClient.getInstance().inGameHud).callRenderHotbarItem(context, x, y, tickCounter, player, player.getInventory().main.get(slot + Util.SLOT_OFFSET), seed++);
					}

					context.getMatrices().pop();
					RenderSystem.disableBlend();
				}
			});
			event.registerBelow(VanillaGuiLayers.EFFECTS, Identifier.of(MOD_ID, "hotbar_post"), (context, tickCounter) -> {
				if (Util.isEnabled() && MinecraftClient.getInstance().interactionManager.getCurrentGameMode() != GameMode.SPECTATOR) {
					context.getMatrices().translate(0, -Util.DISTANCE, 0);
				}
			});
		});
	}
}
