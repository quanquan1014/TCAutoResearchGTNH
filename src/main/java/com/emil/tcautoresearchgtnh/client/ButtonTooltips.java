package com.emil.tcautoresearchgtnh.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.gui.GuiButton;

/** 按钮悬浮提示：按按钮 id 记录文案，绘制时检测悬停。 */
final class ButtonTooltips {

    private final Map<Integer, String> texts = new HashMap<>();

    void put(int id, String text) {
        texts.put(id, text);
    }

    String text(int id) {
        return texts.get(id);
    }

    /** 返回鼠标悬停且配置了提示的按钮，否则 null。 */
    GuiButton hovered(Iterable<GuiButton> buttons, int mouseX, int mouseY) {
        for (GuiButton b : buttons) {
            if (b.visible && texts.containsKey(b.id)
                    && mouseX >= b.xPosition && mouseX < b.xPosition + b.getButtonWidth()
                    && mouseY >= b.yPosition && mouseY < b.yPosition + b.height) {
                return b;
            }
        }
        return null;
    }
}
