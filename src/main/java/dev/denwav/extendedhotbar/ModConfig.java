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

package dev.denwav.extendedhotbar;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class ModConfig extends ReflectiveConfig {

	@Comment("Toggle Extended Hotbar on and off")
	public final TrackedValue<Boolean> enabled = value(true);

	@Comment("Invert full row / single item swap")
	public final TrackedValue<Boolean> invert = value(false);

	@Comment("Enable double-tapping a hotbar slot key to swap only that hotbar slot")
	public final TrackedValue<Boolean> enableDoubleTap = value(false);

	@Comment("Enable holding SHIFT to change item swap behavior")
	public final TrackedValue<Boolean> enableModifier = value(false);
}
