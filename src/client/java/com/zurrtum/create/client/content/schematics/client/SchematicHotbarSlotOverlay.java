package com.zurrtum.create.client.content.schematics.client;

import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.Window;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;

public class SchematicHotbarSlotOverlay {

    public void renderOn(MinecraftClient mc, DrawContext graphics, int slot, float tickProgress) {
        Window mainWindow = mc.getWindow();
        int x = (mainWindow.getScaledWidth() / 2 - 88) + 20 * slot;
        int y = mainWindow.getScaledHeight() - 19;
        AllGuiTextures.SCHEMATIC_SLOT.render(graphics, x, y);
        ItemStack stack = mc.player.getInventory().getStack(slot);
        float f = stack.getBobbingAnimationTime() - tickProgress;
        MatrixStack ms = graphics.getMatrices();
        if (f > 0.0F) {
            float g = 1.0F + f / 5.0F;
            ms.push();
            ms.translate(x + 8, y + 12, 0);
            ms.scale(1.0F / g, (g + 1.0F) / 2.0F, 1);
            ms.translate(-(x + 8), -(y + 12), 0);
        }
        graphics.drawItem(mc.player, stack, x, y, slot + 1);
        if (f > 0.0F) {
            ms.pop();
        }
        graphics.drawItemInSlot(mc.textRenderer, stack, x, y);
    }

}
