package com.zurrtum.create.client.compat.rei.renderer;

import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.client.util.math.MatrixStack;

public record TwoIconRenderer(ItemStack icon, ItemStack subIcon) implements Renderer {
    public TwoIconRenderer(Item icon, Item subIcon) {
        this(new ItemStack(icon), new ItemStack(subIcon));
    }

    @Override
    public void render(DrawContext graphics, Rectangle bounds, int mouseX, int mouseY, float delta) {
        MatrixStack matrices = graphics.getMatrices();
        matrices.push();
        matrices.translate(bounds.x, bounds.y, 0);
        matrices.scale(bounds.getWidth() / 16f, bounds.getHeight() / 16f, 1);
        graphics.drawItem(icon, 0, 0);
        matrices.translate(9, 9, 0);
        matrices.scale(0.5f, 0.5f, 1);
        graphics.drawItem(subIcon, 0, 0);
        matrices.pop();
    }
}
