package com.zurrtum.create.client.compat.rei.category;

import com.zurrtum.create.AllItems;
import com.zurrtum.create.client.compat.rei.CreateCategory;
import com.zurrtum.create.client.compat.rei.renderer.TwoIconRenderer;
import com.zurrtum.create.client.foundation.gui.AllGuiTextures;
import com.zurrtum.create.client.foundation.gui.render.BasinBlazeBurnerRenderer;
import com.zurrtum.create.client.foundation.gui.render.MixingBasinRenderer;
import com.zurrtum.create.client.foundation.utility.CreateLang;
import com.zurrtum.create.compat.rei.ReiCommonPlugin;
import com.zurrtum.create.compat.rei.display.PotionDisplay;
import com.zurrtum.create.content.processing.recipe.HeatCondition;
import me.shedaniel.math.Point;
import me.shedaniel.math.Rectangle;
import me.shedaniel.rei.api.client.gui.Renderer;
import me.shedaniel.rei.api.client.gui.widgets.Widget;
import me.shedaniel.rei.api.client.gui.widgets.Widgets;
import me.shedaniel.rei.api.common.category.CategoryIdentifier;
import me.shedaniel.rei.api.common.util.EntryIngredients;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.text.Text;

import java.util.List;

public class PotionCategory extends CreateCategory<PotionDisplay> {
    @Override
    public CategoryIdentifier<? extends PotionDisplay> getCategoryIdentifier() {
        return ReiCommonPlugin.AUTOMATIC_BREWING;
    }

    @Override
    public Text getTitle() {
        return CreateLang.translateDirect("recipe.automatic_brewing");
    }

    @Override
    public Renderer getIcon() {
        return new TwoIconRenderer(AllItems.MECHANICAL_MIXER, Items.BREWING_STAND);
    }

    @Override
    public void addWidgets(List<Widget> widgets, PotionDisplay display, Rectangle bounds) {
        Point input = new Point(bounds.x + 26, bounds.y + 56);
        Point fluid = new Point(bounds.x + 45, bounds.y + 56);
        Point output = new Point(bounds.x + 147, bounds.y + 56);
        HeatCondition requiredHeat = HeatCondition.HEATED;
        widgets.add(Widgets.createDrawableWidget((DrawContext graphics, int mouseX, int mouseY, float delta) -> {
            drawSlotBackground(graphics, input, fluid, output);
            AllGuiTextures.JEI_DOWN_ARROW.render(graphics, bounds.x + 141, bounds.y + 37);
            AllGuiTextures.JEI_HEAT_BAR.render(graphics, bounds.x + 9, bounds.y + 85);
            AllGuiTextures.JEI_LIGHT.render(graphics, bounds.x + 86, bounds.y + 93);
            BasinBlazeBurnerRenderer.render(graphics, bounds.x + 96, bounds.y + 74, requiredHeat.visualizeAsBlazeBurner());
            MixingBasinRenderer.render(graphics, bounds.x + 96, bounds.y);
            graphics.drawText(
                MinecraftClient.getInstance().textRenderer,
                CreateLang.translateDirect(requiredHeat.getTranslationKey()),
                bounds.x + 14,
                bounds.y + 91,
                requiredHeat.getColor(),
                false
            );
        }));
        widgets.add(createInputSlot(input).entries(display.input()));
        widgets.add(createInputSlot(fluid).entries(getRenderEntryStack(display.getInputEntries().getLast())));
        widgets.add(createOutputSlot(output).entries(getRenderEntryStack(display.getOutputEntries().getFirst())));
        widgets.add(createSlot(new Point(bounds.x + 139, bounds.y + 86)).entries(EntryIngredients.of(AllItems.BLAZE_BURNER)));
    }

    @Override
    public int getDisplayHeight() {
        return 113;
    }
}
