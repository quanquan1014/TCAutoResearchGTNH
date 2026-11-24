package com.emil.tcautoresearchgtnh.research;

import net.minecraft.client.Minecraft;

import com.emil.tcautoresearchgtnh.Config;
import com.emil.tcautoresearchgtnh.TCAutoResearchGTNH;

import thaumcraft.client.lib.PlayerNotifications;
import thaumcraft.common.lib.research.ResearchNoteData;

/**
 * 研究台会话：统一委托 {@link ResearchSolveController} 求解与执行。
 */
public final class ResearchSession {

    private static final ResearchSession INSTANCE = new ResearchSession();

    private String message = "";
    private int messageTicks;
    private String lastSig = "";
    private boolean autoPaused;

    private ResearchSession() {
    }

    public static ResearchSession get() {
        return INSTANCE;
    }

    public String message() {
        return messageTicks > 0 ? message : "";
    }

    public void toggleAuto() {
        if (Config.autoSolve) {
            // 已开启：再点一次即关闭（停止）
            Config.setAuto(false);
            autoPaused = false;
            toast("自动完成已关闭");
            return;
        }
        // 开启，但默认暂停，需点「开始」才运行
        Config.setAuto(true);
        autoPaused = true;
        toast("自动完成已开启，当前暂停，点「开始」运行");
    }

    public boolean isAutoPaused() {
        return autoPaused;
    }

    public void toggleAutoPause() {
        autoPaused = !autoPaused;
        if (autoPaused) {
            ResearchSolveController.cancel();
        }
        toast(autoPaused ? "自动完成已暂停" : "自动完成已继续");
    }

    public void toggleWeighted() {
        Config.setWeighted(!Config.weightedMode);
        toast(Config.weightedMode ? "已切换到加权模式（维基成本优先）" : "已切换到最短模式（理论最少跳数）");
    }

    public void resetWeights() {
        Config.resetWeights();
        toast("权重已还原为维基默认值");
    }

    /** 手动求解：计算后预览（若开启），确认后执行。 */
    public void solve() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) {
            toast("玩家未就绪");
            return;
        }
        try {
            ResearchNoteData note = ResearchTableAccess.note();
            if (note == null) {
                toast("未打开研究台或没有笔记");
                return;
            }
            if (note.complete) {
                toast("这张笔记已经完成");
                return;
            }
            if (ResearchSolveController.isBusy()) {
                ResearchSolveController.cancel();
            }
            lastSig = signature(note);
            ResearchSolveController.request(ResearchTableHelper.INSTANCE, mc.thePlayer, mc, false);
        } catch (Throwable t) {
            TCAutoResearchGTNH.LOG.error("手动求解异常", t);
            toast("求解异常：" + t);
        }
    }

    /** 解锁所有缺失要素。 */
    public void unlockAll() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) {
            toast("玩家未就绪");
            return;
        }
        if (ResearchSolveController.isBusy()) {
            toast("请先停止当前求解或放置");
            return;
        }
        if (AspectSynthesisController.isRunning()) {
            AspectSynthesisController.cancel();
        }
        AspectSynthesisController.discoverAll(mc.thePlayer, null);
    }

    public void stop() {
        ResearchSolveController.cancel();
        if (AspectSynthesisController.isRunning()) {
            AspectSynthesisController.cancel();
        }
        toast("已停止");
    }

    /** 关闭研究台时静默清理。 */
    public void onLeave() {
        lastSig = "";
        ResearchSolveController.onResearchGuiClosed();
        if (AspectSynthesisController.isRunning()) {
            AspectSynthesisController.cancel();
        }
    }

    /** 每 tick 调用：消费消息、驱动自动完成。 */
    public void tick() {
        if (messageTicks > 0) {
            messageTicks--;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) {
            return;
        }
        if (!Config.autoSolve || autoPaused || ResearchSolveController.isBusy()
                || TargetResearchController.isRunning() || BatchResearchController.isRunning()) {
            return;
        }
        if (ResearchTableAccess.table() == null) {
            return;
        }
        ResearchNoteData note = ResearchTableAccess.note();
        if (note == null || note.complete || ResearchSolveController.isAutomaticSuppressed(note)) {
            return;
        }
        String sig = signature(note);
        if (!sig.equals(lastSig)) {
            lastSig = sig;
            ResearchSolveController.request(ResearchTableHelper.INSTANCE, mc.thePlayer, mc, true);
        }
    }

    private String signature(ResearchNoteData note) {
        if (note == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder(64);
        sb.append(note.key == null ? "?" : note.key).append('|');
        int hash = 0;
        if (note.hexes != null) {
            for (String path : note.hexes.keySet()) {
                hash += path.hashCode();
            }
        }
        // 已放置要素也算进签名，放置变化后能触发续解
        if (note.hexEntries != null) {
            for (Object value : note.hexEntries.values()) {
                thaumcraft.common.lib.research.ResearchManager.HexEntry he =
                        (thaumcraft.common.lib.research.ResearchManager.HexEntry) value;
                hash += he == null || he.aspect == null ? 0 : he.aspect.getTag().hashCode();
            }
        }
        return sb.append(hash).toString();
    }

    private void toast(String text) {
        message = text;
        messageTicks = 400;
        TCAutoResearchGTNH.LOG.info(text);
        try {
            PlayerNotifications.addNotification(text);
        } catch (Throwable ignored) {
            // 通知失败不影响功能
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.thePlayer != null) {
            mc.thePlayer.addChatMessage(new net.minecraft.util.ChatComponentText("[自动研究] " + text));
        }
    }
}
