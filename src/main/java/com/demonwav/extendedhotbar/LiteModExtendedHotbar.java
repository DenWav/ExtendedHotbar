package com.demonwav.extendedhotbar;

import com.demonwav.extendedhotbar.mixin.MixinGuiContainerCreative;
import com.google.gson.annotations.Expose;
import com.mumfrey.liteloader.PlayerClickListener;
import com.mumfrey.liteloader.PlayerInteractionListener.MouseButton;
import com.mumfrey.liteloader.Tickable;
import com.mumfrey.liteloader.core.LiteLoader;
import com.mumfrey.liteloader.modconfig.ConfigStrategy;
import com.mumfrey.liteloader.modconfig.ExposableOptions;
import java.io.File;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiContainerCreative;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import org.lwjgl.input.Keyboard;

@ExposableOptions(strategy = ConfigStrategy.Versioned, filename = "extendedhotbar.json")
public class LiteModExtendedHotbar implements Tickable, PlayerClickListener {

    private static final KeyBinding swapKeyBinding = new KeyBinding("key.hotbar.switch", Keyboard.KEY_R, "key.categories.litemods");
    private static final KeyBinding toggleKeyBinding = new KeyBinding("key.hotbar.toggle", Keyboard.KEY_P, "key.categories.litemods");

    private static final int INVENTORY_TAB_INDEX = CreativeTabs.INVENTORY.getTabIndex();

    private static final int LEFT_BOTTOM_ROW_SLOT_INDEX = 27;
    private static final int LEFT_HOTBAR_SLOT_INDEX = 36;
    private static final int BOTTOM_RIGHT_CRAFTING_SLOT_INDEX = 4;

    @Expose
    private boolean enabled = true;

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public String getName() {
        return "ExtendedHotbar";
    }

    @Override
    public String getVersion() {
        return "1.2.0";
    }

    @Override
    public void init(final File configPath) {
        LiteLoader.getInput().registerKeyBinding(swapKeyBinding);
        LiteLoader.getInput().registerKeyBinding(toggleKeyBinding);
    }

    @Override
    public void upgradeSettings(final String version, final File configPath, final File oldConfigPath) {
    }

    @Override
    public void onTick(final Minecraft minecraft, final float partialTicks, final boolean inGame, final boolean clock) {
        if (toggleKeyBinding.isPressed()) {
            enabled = !enabled;
            LiteLoader.getInstance().writeConfig(this);
            return;
        }

        if (!enabled) {
            return;
        }

        if (!inGame || minecraft.currentScreen != null || !Minecraft.isGuiEnabled()) {
            return;
        }

        if (swapKeyBinding.isPressed()) {
            performSwap(!Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) && !Keyboard.isKeyDown(Keyboard.KEY_RSHIFT));
        }
    }

    private void performSwap(final boolean fullRow) {
        final Minecraft minecraft = Minecraft.getMinecraft();

        final GuiInventory inventory = new GuiInventory(minecraft.player);
        minecraft.displayGuiScreen(inventory);

        final GuiScreen currentScreen = minecraft.currentScreen;
        if (currentScreen == null) {
            return;
        }

        // For the switcheroo to work, we need to be in the inventory window
        final int index;
        if (currentScreen instanceof GuiContainerCreative) {
            index = ((GuiContainerCreative) currentScreen).getSelectedTabIndex();

            if (index != INVENTORY_TAB_INDEX) {
                ((MixinGuiContainerCreative) currentScreen).callSetCurrentCreativeTab(CreativeTabs.INVENTORY);
            }
        } else {
            index = -1;
        }

        final int windowId = inventory.inventorySlots.windowId;

        if (fullRow) {
            swapRows(minecraft, windowId);
        } else {
            final int currentItem = minecraft.player.inventory.currentItem;
            swapItem(minecraft, windowId, currentItem);
        }

        // If index == -1 then it's not a creative inventory, if it's INVENTORY_TAB_INDEX then there's no need to change it back to itself
        if (index != -1 && index != INVENTORY_TAB_INDEX) {
            ((MixinGuiContainerCreative) currentScreen).callSetCurrentCreativeTab(CreativeTabs.CREATIVE_TAB_ARRAY[index]);
        }

        minecraft.displayGuiScreen(null);
    }

    private void swapRows(final Minecraft minecraft, final int windowId) {
        for (int i = 0; i < 9; i++) {
            swapItem(minecraft, windowId, i);
        }
    }

    /**
     * Swaps two items in the hotbar and the bottom row of the player's inventory, 0 being the far left column, 8 being the far right.
     */
    private void swapItem(final Minecraft minecraft, final int windowId, final int slotId) {
        /*
         * Implementation note:
         * There are fancy click mechanisms to swap item stacks without using a temporary slot, but when swapping between two identical item
         * stacks, things can get messed up. Using a temporary slot that we know is guaranteed to be empty is the safest option.
         */

        // Move hotbar item to crafting slot
        minecraft.playerController.windowClick(windowId, slotId + LEFT_HOTBAR_SLOT_INDEX, 0, ClickType.PICKUP, minecraft.player);
        minecraft.playerController.windowClick(windowId, BOTTOM_RIGHT_CRAFTING_SLOT_INDEX, 0, ClickType.PICKUP, minecraft.player);
        // Move bottom row item to hotbar
        minecraft.playerController.windowClick(windowId, slotId + LEFT_BOTTOM_ROW_SLOT_INDEX, 0, ClickType.PICKUP, minecraft.player);
        minecraft.playerController.windowClick(windowId, slotId + LEFT_HOTBAR_SLOT_INDEX, 0, ClickType.PICKUP, minecraft.player);
        // Move crafting slot item to bottom row
        minecraft.playerController.windowClick(windowId, BOTTOM_RIGHT_CRAFTING_SLOT_INDEX, 0, ClickType.PICKUP, minecraft.player);
        minecraft.playerController.windowClick(windowId, slotId + LEFT_BOTTOM_ROW_SLOT_INDEX, 0, ClickType.PICKUP, minecraft.player);
    }

    @Override
    public boolean onMouseClicked(final EntityPlayerSP player, final MouseButton button) {
        if (button != MouseButton.MIDDLE) {
            return true;
        }

        final Minecraft minecraft = Minecraft.getMinecraft();

        if (minecraft.currentScreen != null) {
            return true;
        }

        final RayTraceResult result = minecraft.objectMouseOver;
        if (result.typeOfHit != RayTraceResult.Type.BLOCK) {
            return true;
        }

        final BlockPos blockPos = result.getBlockPos();
        final Block block = minecraft.world.getBlockState(blockPos).getBlock();

        // If the block is in the hotbar, we do nothing and let Minecraft do it's thing
        final InventoryPlayer inventory = player.inventory;
        for (int i = 0; i < 9; i++) {
            // While LEFT_HOTBAR_SLOT_INDEX is the base index for hotbar slots in the inventory gui, in InventoryPlayer,
            // the hotbar starts at 0
            final Item item = inventory.getStackInSlot(i).getItem();
            final Block blockFromItem = Block.getBlockFromItem(item);

            if (block == blockFromItem) {
                return true;
            }
        }

        // If the block is in the bottom row and not in the hotbar, we need to emulate Minecraft's default behavior
        // We do this by swapping the rows and then letting Minecraft go from there
        // It'll find the item in the hotbar and move the selection to that item accordingly
        for (int i = 0; i < 9; i++) {
            final Item item = inventory.getStackInSlot(i + LEFT_BOTTOM_ROW_SLOT_INDEX).getItem();
            final Block blockFromItem = Block.getBlockFromItem(item);

            if (block != blockFromItem) {
                continue;
            }

            performSwap(true);
            break;
        }

        return true;
    }

    @Override
    public boolean onMouseHeld(final EntityPlayerSP player, final MouseButton button) {
        return true;
    }
}
