package com.emil.tcautoresearchgtnh.client;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

import org.lwjgl.opengl.GL11;

import com.emil.tcautoresearchgtnh.Config;
import com.emil.tcautoresearchgtnh.research.BatchResearchController;
import com.emil.tcautoresearchgtnh.research.CompletionReport;
import com.emil.tcautoresearchgtnh.research.ResearchCatalog;
import com.emil.tcautoresearchgtnh.research.ResearchSession;
import com.emil.tcautoresearchgtnh.research.ResearchSolveController;
import com.emil.tcautoresearchgtnh.research.ResearchTableHelper;

/**
 * 研究台左上角的工具条与状态行。
 */
public final class ResearchOverlay {

    private static final int COLS = 3;
    private static final int X = 20;
    private static final int Y = 20;
    private static final int W = 52;
    private static final int H = 14;
    private static final int GAP = 2;

    private final GuiScreen gui;
    private final List<ThemedButton> mine = new ArrayList<>();

    private final ThemedButton auto;
    private final ThemedButton solve;
    private final ThemedButton stop;
    private final ThemedButton weighted;
    private final ThemedButton unlock;
    private final ThemedButton reset;
    private final ThemedButton weights;
    private final ThemedButton batch;
    private final ThemedButton search;
    private final ThemedButton report;
    private final ThemedButton settings;
    private final ThemedButton pause;
    private final ThemedButton batchStop;
    private final ThemedButton autoPause;
    private int textTop;

    ResearchOverlay(GuiScreen gui, List<GuiButton> buttonList) {
        this.gui = gui;
        int id = 1000;
        auto = add(buttonList, id++, "自动完成", "开启后：放入笔记自动求解并直接放置，无需确认",
                new Runnable() { public void run() { ResearchSession.get().toggleAuto(); } });
        solve = add(buttonList, id++, "手动求解", "计算当前笔记，预览确认后放置",
                new Runnable() { public void run() { ResearchSession.get().solve(); } });
        stop = add(buttonList, id++, "停止", "停止计算或放置",
                new Runnable() { public void run() { ResearchSession.get().stop(); } });
        weighted = add(buttonList, id++, "加权", "切换求解模式：加权（维基成本优先）/ 最短（理论最少跳数）",
                new Runnable() { public void run() { ResearchSession.get().toggleWeighted(); } });
        unlock = add(buttonList, id++, "解锁要素", "通过研究台依次合成并解锁所有缺失要素",
                new Runnable() { public void run() { ResearchSession.get().unlockAll(); } });
        reset = add(buttonList, id++, "还原权重", "恢复为默认权重",
                new Runnable() { public void run() { ResearchSession.get().resetWeights(); } });
        weights = add(buttonList, id++, "权重设置", "编辑要素权重与禁用",
                new Runnable() {
                    public void run() {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiAspectWeights(gui));
                    }
                });
        batch = add(buttonList, id++, "批量研究", "自动处理背包中全部未完成笔记",
                new Runnable() {
                    public void run() {
                        Minecraft mc = Minecraft.getMinecraft();
                        BatchResearchController.start(ResearchTableHelper.INSTANCE, mc.thePlayer, mc);
                    }
                });
        search = add(buttonList, id++, "搜索研究", "按标签/目录搜索研究项目",
                new Runnable() {
                    public void run() {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiResearchQueue(gui, ResearchCatalog.currentCategory()));
                    }
                });
        report = add(buttonList, id++, "完成报告", "查看最近一次求解的完成报告",
                new Runnable() {
                    public void run() {
                        CompletionReport r = ResearchSolveController.getLastReport();
                        if (r != null) {
                            Minecraft.getMinecraft().displayGuiScreen(new GuiCompletionReport(gui, r));
                        }
                    }
                });
        settings = add(buttonList, id++, "设置", "调整工具条缩放与位置",
                new Runnable() {
                    public void run() {
                        Minecraft.getMinecraft().displayGuiScreen(new GuiSettings(gui));
                    }
                });
        pause = add(buttonList, id++, "暂停", "暂停/继续批量研究",
                new Runnable() {
                    public void run() {
                        if (BatchResearchController.isPaused()) {
                            BatchResearchController.resume();
                        } else {
                            BatchResearchController.pause();
                        }
                    }
                });
        batchStop = add(buttonList, id++, "结束批量", "结束批量研究",
                new Runnable() { public void run() { BatchResearchController.cancel(); } });
        autoPause = add(buttonList, id++, "开始", "开始/暂停自动完成",
                new Runnable() { public void run() { ResearchSession.get().toggleAutoPause(); } });
        stop.visible = false;
        pause.visible = false;
        batchStop.visible = false;
        autoPause.visible = false;
    }

    private ThemedButton add(List<GuiButton> list, int id, String label, String tooltip, Runnable action) {
        ThemedButton b = new ThemedButton(id, X, Y, W, H, label, tooltip, action);
        mine.add(b);
        list.add(b);
        return b;
    }

    public GuiScreen gui() {
        return gui;
    }

    /** 自行处理鼠标点击：命中按钮则触发对应动作并返回 true。 */
    public boolean handleMouseClick(int mouseX, int mouseY, int button) {
        if (button != 0) {
            return false;
        }
        for (ThemedButton b : mine) {
            if (b.visible && b.enabled
                    && mouseX >= b.xPosition && mouseX < b.xPosition + b.width
                    && mouseY >= b.yPosition && mouseY < b.yPosition + b.height) {
                b.press(mc0(), mouseX, mouseY);
                return true;
            }
        }
        return false;
    }

    private Minecraft mc0() {
        return Minecraft.getMinecraft();
    }

    public void draw(Minecraft mc, int mouseX, int mouseY) {
        ResearchSession s = ResearchSession.get();

        auto.displayString = Config.autoSolve ? "自动完成*" : "自动完成";
        auto.setAccent(Config.autoSolve);
        weighted.displayString = Config.weightedMode ? "加权*" : "最短*";
        weighted.setAccent(true);

        boolean solving = ResearchSolveController.isSolving();
        boolean executing = ResearchSolveController.isExecuting();
        stop.visible = solving || executing;
        solve.setAccent(solving || executing);
        if (solving) {
            solve.displayString = "计算中";
            stop.displayString = "停止";
        } else if (executing) {
            solve.displayString = "放置中";
            stop.displayString = "停止";
        } else {
            solve.displayString = "手动求解";
        }

        boolean batchRunning = BatchResearchController.isRunning();
        pause.visible = batchRunning;
        batchStop.visible = batchRunning;
        pause.displayString = BatchResearchController.isPaused() ? "继续" : "暂停";
        batch.displayString = batchRunning ? "批量中" : "批量";
        autoPause.visible = Config.autoSolve && !batchRunning;
        autoPause.displayString = s.isAutoPaused() ? "开始" : "暂停";

        layoutVisible();

        String status;
        if (batchRunning) {
            status = BatchResearchController.isPaused() ? "批量已暂停" : BatchResearchController.buttonText();
        } else if (solving) {
            status = "正在计算方案…";
        } else if (executing) {
            status = "正在放置要素…";
        } else {
            status = s.message();
            if (status.isEmpty()) {
                status = Config.autoSolve ? "自动求解中" : "空闲";
            }
        }

        GL11.glPushMatrix();
        GL11.glScalef(Config.guiScale, Config.guiScale, 1.0f);
        mc.fontRenderer.drawString(status, (int) Config.toolbarX, textTop, 0xFFFFFF);
        GL11.glPopMatrix();

        drawTooltip(mc, mouseX, mouseY);
    }

    private void layoutVisible() {
        float scale = Config.guiScale;
        int x0 = (int) (Config.toolbarX * scale);
        int y0 = (int) (Config.toolbarY * scale);
        int w = (int) (W * scale);
        int h = (int) (H * scale);
        int dx = (int) ((W + GAP) * scale);
        int dy = (int) ((H + GAP) * scale);

        int col = 0;
        int y = y0;
        int fixedCount = 0;
        for (ThemedButton b : mine) {
            if (isDynamic(b) || !b.visible) {
                continue;
            }
            b.xPosition = x0 + col * dx;
            b.yPosition = y;
            b.width = w;
            b.height = h;
            col++;
            fixedCount++;
            if (col == COLS) {
                col = 0;
                y += dy;
            }
        }
        int fixedRows = (fixedCount + COLS - 1) / COLS;

        int dynY = y0 + fixedRows * dy + (int) (GAP * scale);
        int dynamicCount = 0;
        for (ThemedButton b : mine) {
            if (!isDynamic(b) || !b.visible) {
                continue;
            }
            b.xPosition = x0 + (dynamicCount % COLS) * dx;
            b.yPosition = dynY + (dynamicCount / COLS) * dy;
            b.width = w;
            b.height = h;
            dynamicCount++;
        }
        int dynamicRows = (dynamicCount + COLS - 1) / COLS;

        textTop = (int) Config.toolbarY + (fixedRows + dynamicRows) * (H + GAP) + 2;
    }

    private boolean isDynamic(ThemedButton b) {
        return b == stop || b == pause || b == batchStop || b == autoPause;
    }

    private void drawTooltip(Minecraft mc, int mouseX, int mouseY) {
        for (ThemedButton b : mine) {
            if (b.visible && b.tooltip != null
                    && mouseX >= b.xPosition && mouseX < b.xPosition + b.width
                    && mouseY >= b.yPosition && mouseY < b.yPosition + b.height) {
                int textWidth = mc.fontRenderer.getStringWidth(b.tooltip);
                int tx = mouseX + 10;
                int ty = mouseY - 12;
                GuiThemeRenderer.rect(tx - 2, ty - 2, tx + textWidth + 2, ty + 10, 0xC0101010);
                mc.fontRenderer.drawString(b.tooltip, tx, ty, 0xFFFFFF);
                return;
            }
        }
    }

    private static final class ThemedButton extends GuiButton {

        private final Runnable action;
        private final String tooltip;
        private boolean accent;

        ThemedButton(int id, int x, int y, int w, int h, String label, String tooltip, Runnable action) {
            super(id, x, y, w, h, label);
            this.tooltip = tooltip;
            this.action = action;
        }

        void setAccent(boolean accent) {
            this.accent = accent;
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY) {
            if (!visible) {
                return;
            }
            FontRenderer fr = mc.fontRenderer;
            boolean hover = mouseX >= xPosition && mouseX < xPosition + width
                    && mouseY >= yPosition && mouseY < yPosition + height;
            final int bg = accent ? (hover ? 0xE0906A18 : 0xD06B4E0C) : (hover ? 0xE0383846 : 0xD01C1C22);
            final int border = accent ? 0xFFE0B050 : (hover ? 0xFF8A8AA0 : 0xFF484855);
            final int left = xPosition;
            final int top = yPosition;
            final int right = xPosition + width;
            final int bottom = yPosition + height;
            GuiThemeRenderer.isolated(new Runnable() {
                public void run() {
                    GuiThemeRenderer.rect(left, top, right, bottom, bg);
                    GuiThemeRenderer.rect(left, top, right, top + 1, border);
                    GuiThemeRenderer.rect(left, bottom - 1, right, bottom, border);
                    GuiThemeRenderer.rect(left, top, left + 1, bottom, border);
                    GuiThemeRenderer.rect(right - 1, top, right, bottom, border);
                }
            });
            int text = accent ? 0xFFE6C060 : hover ? 0xFFFFFF : 0xC8C8D0;
            fr.drawString(displayString, xPosition + (width - fr.getStringWidth(displayString)) / 2,
                    yPosition + (height - 8) / 2, text);
        }

        @Override
        public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
            if (super.mousePressed(mc, mouseX, mouseY)) {
                press(mc, mouseX, mouseY);
                return true;
            }
            return false;
        }

        void press(Minecraft mc, int mouseX, int mouseY) {
            action.run();
        }
    }
}
