package com.emil.tcautoresearchgtnh.client;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import com.emil.tcautoresearchgtnh.Config;

import cpw.mods.fml.client.config.GuiButtonExt;

/**
 * 工具条缩放与位置设置。上半部分可拖动工具条预览调整位置，下方调整缩放。
 */
public class GuiSettings extends GuiScreen {

    private static final int PREVIEW_W = 150;
    private static final int PREVIEW_H = 60;

    private final GuiScreen parent;
    private boolean dragging;
    private float dragOffsetX;
    private float dragOffsetY;

    public GuiSettings(GuiScreen parent) {
        this.parent = parent;
    }

    @Override
    public void initGui() {
        int center = width / 2;
        buttonList.clear();
        buttonList.add(new GuiButtonExt(0, center - 105, height - 82, 20, 20, "-"));
        buttonList.add(new GuiButtonExt(1, center + 85, height - 82, 20, 20, "+"));
        buttonList.add(new GuiButtonExt(3, center - 70, height - 56, 140, 20, "恢复默认位置"));
        buttonList.add(new GuiButtonExt(2, center - 50, height - 30, 100, 20, "完成"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (button.id == 0) {
            Config.setGuiScale(Config.guiScale - 0.1f);
        } else if (button.id == 1) {
            Config.setGuiScale(Config.guiScale + 0.1f);
        } else if (button.id == 3) {
            Config.setToolbarPosition(4.0f, 4.0f);
            Config.saveToolbarPosition();
        } else if (button.id == 2) {
            Config.saveToolbarPosition();
            mc.displayGuiScreen(parent);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        super.mouseClicked(mouseX, mouseY, mouseButton);
        if (mouseButton == 0 && isOverPreview(mouseX, mouseY)) {
            dragging = true;
            dragOffsetX = mouseX - Config.toolbarX;
            dragOffsetY = mouseY - Config.toolbarY;
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
        if (dragging) {
            Config.setToolbarPosition(mouseX - dragOffsetX, mouseY - dragOffsetY);
        }
    }

    @Override
    protected void mouseMovedOrUp(int mouseX, int mouseY, int which) {
        super.mouseMovedOrUp(mouseX, mouseY, which);
        if (which == 0 && dragging) {
            dragging = false;
            Config.saveToolbarPosition();
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        int center = width / 2;
        drawCenteredString(fontRendererObj, "拖动下方方框调整工具条位置（整体移动）", center, 12, 0xFFFFFF);
        drawCenteredString(fontRendererObj, "工具条缩放：" + String.format("%.1f", Config.guiScale) + "（0.5 ~ 2.0）",
                center, height - 100, 0xFFD070);
        drawToolbarPreview();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    private void drawToolbarPreview() {
        int x = (int) Config.toolbarX;
        int y = (int) Config.toolbarY;
        drawRect(x, y, x + PREVIEW_W, y + PREVIEW_H, dragging ? 0xD03A5A46 : 0xC0202028);
        drawRect(x, y, x + PREVIEW_W, y + 1, 0xFFE0B050);
        drawRect(x, y + PREVIEW_H - 1, x + PREVIEW_W, y + PREVIEW_H, 0xFFE0B050);
        drawRect(x, y, x + 1, y + PREVIEW_H, 0xFFE0B050);
        drawRect(x + PREVIEW_W - 1, y, x + PREVIEW_W, y + PREVIEW_H, 0xFFE0B050);
        drawCenteredString(fontRendererObj, "工具条",
                x + PREVIEW_W / 2, y + PREVIEW_H / 2 - 8, 0xE0E0E0);
        drawCenteredString(fontRendererObj, "按住拖动", x + PREVIEW_W / 2, y + PREVIEW_H / 2 + 2, 0x9FB4AA);
    }

    private boolean isOverPreview(int mouseX, int mouseY) {
        int x = (int) Config.toolbarX;
        int y = (int) Config.toolbarY;
        return mouseX >= x && mouseX < x + PREVIEW_W && mouseY >= y && mouseY < y + PREVIEW_H;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}
