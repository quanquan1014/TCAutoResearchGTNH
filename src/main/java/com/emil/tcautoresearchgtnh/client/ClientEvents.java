package com.emil.tcautoresearchgtnh.client;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import net.minecraftforge.client.event.GuiScreenEvent;

import com.emil.tcautoresearchgtnh.TCAutoResearchGTNH;
import com.emil.tcautoresearchgtnh.research.BatchResearchController;
import com.emil.tcautoresearchgtnh.research.ResearchSession;
import com.emil.tcautoresearchgtnh.research.ResearchTableAccess;
import com.emil.tcautoresearchgtnh.research.ResearchTableHelper;
import com.emil.tcautoresearchgtnh.research.TargetResearchController;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;

/**
 * 监听研究台 GUI 的打开与关闭，挂载工具条并驱动各控制器。
 */
public final class ClientEvents {

    private static ResearchOverlay overlay;
    private static int drawFailures;

    @SubscribeEvent
    public void onInitGui(GuiScreenEvent.InitGuiEvent.Post event) {
        try {
            GuiScreen gui = event.gui;
            if (!ResearchTableAccess.isResearchTableScreen(gui)) {
                if (overlay != null) {
                    overlay = null;
                    ResearchSession.get().onLeave();
                    ResearchTableHelper helper = ResearchTableHelper.INSTANCE;
                    BatchResearchController.onResearchTableClosed(helper);
                    TargetResearchController.onResearchTableClosed(helper);
                }
                return;
            }
            overlay = new ResearchOverlay(gui, event.buttonList);
            Minecraft mc = Minecraft.getMinecraft();
            ResearchTableHelper helper = ResearchTableHelper.INSTANCE;
            TargetResearchController.attach(helper, mc.thePlayer, mc);
            BatchResearchController.attach(helper, mc.thePlayer, mc);
        } catch (Throwable t) {
            overlay = null;
            TCAutoResearchGTNH.LOG.error("研究台工具条初始化失败", t);
        }
    }

    @SubscribeEvent
    public void onDrawPost(GuiScreenEvent.DrawScreenEvent.Post event) {
        ResearchOverlay current = overlay;
        if (current == null || current.gui() != event.gui) {
            return;
        }
        try {
            current.draw(Minecraft.getMinecraft(), event.mouseX, event.mouseY);
            drawFailures = 0;
        } catch (Throwable t) {
            // 绘制失败只记录并限流
            if (++drawFailures <= 10 || drawFailures % 50 == 0) {
                TCAutoResearchGTNH.LOG.error("研究台工具条绘制失败（第 " + drawFailures + " 次）", t);
            }
            if (drawFailures > 50) {
                overlay = null;
                drawFailures = 0;
            }
        }
    }

    /** 由 {@code GuiScreenMixin} 调用：命中工具条按钮则触发动作并返回 true。 */
    public static boolean handleMouseClick(GuiScreen gui, int mouseX, int mouseY, int button) {
        ResearchOverlay current = overlay;
        if (current == null || current.gui() != gui) {
            return false;
        }
        try {
            return current.handleMouseClick(mouseX, mouseY, button);
        } catch (Throwable t) {
            TCAutoResearchGTNH.LOG.error("研究台工具条点击处理失败", t);
            return false;
        }
    }
}
