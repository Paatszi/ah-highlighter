package com.example.mixin;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Mixin(HandledScreen.class)
public abstract class AhHighlightMixin {

    // Maksymalna cena do podświetlenia (zmień wedle uznania):
    private static final double MAX_PRICE = 500.0;

    // Szuka linijki "CENA: 210$"
    private static final Pattern PRICE_PATTERN = Pattern.compile("(?i)cena:\\s*([0-9]+(?:[.,][0-9]+)*)\\s*\\$?");

    @Inject(method = "drawSlot", at = @At("TAIL"))
    private void highlightSlot(DrawContext context, Slot slot, CallbackInfo ci) {
        HandledScreen<?> screen = (HandledScreen<?>) (Object) this;

        // Sprawdza, czy tytuł okna to RYNEK
        if (!screen.getTitle().getString().equalsIgnoreCase("RYNEK")) {
            return;
        }

        ItemStack stack = slot.getStack();
        if (stack.isEmpty()) return;

        double price = getPrice(stack);

        if (price > 0 && price <= MAX_PRICE) {
            context.fill(slot.x, slot.y, slot.x + 16, slot.y + 16, 0x6600FF00);
        }
    }

    private double getPrice(ItemStack stack) {
        LoreComponent lore = stack.get(DataComponentTypes.LORE);
        if (lore == null) return -1;

        for (Text line : lore.lines()) {
            Matcher m = PRICE_PATTERN.matcher(line.getString());
            if (m.find()) {
                try {
                    return Double.parseDouble(m.group(1).replace(",", ""));
                } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }
}