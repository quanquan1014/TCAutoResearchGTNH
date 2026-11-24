package com.emil.tcautoresearchgtnh.research;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.client.gui.GuiResearchTable;
import thaumcraft.common.Thaumcraft;
import thaumcraft.common.lib.network.PacketHandler;
import thaumcraft.common.lib.network.playerdata.PacketAspectCombinationToServer;
import thaumcraft.common.lib.network.playerdata.PacketAspectPlaceToServer;
import thaumcraft.common.lib.research.ResearchManager;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.utils.HexUtils;
import thaumcraft.common.tiles.TileResearchTable;

/**
 * 研究台数据 / 发包的唯一访问点。
 *
 * <p>兼容两条路径：</p>
 * <ol>
 *   <li>原版 {@link GuiResearchTable} + {@link ContainerResearchTable}（其他整合包）；</li>
 *   <li>GTNH {@code thaumcraft-research-tweaks} 的 {@code ComposableContainerGui} +
 *       {@code SpecializedContainer}——tile 藏在 slot 的 inventory 里，笔记从 tile 的
 *       笔记槽位（下标 1）读取。</li>
 * </ol>
 */
public final class ResearchTableAccess {

    private ResearchTableAccess() {
    }

    public static EntityPlayer player() {
        Minecraft mc = Minecraft.getMinecraft();
        return mc == null ? null : mc.thePlayer;
    }

    /**
     * 当前打开的研究台方块；未打开研究台时返回 null。
     * 统一通过「容器槽位的 inventory 是 TileResearchTable」反查，对原版 ContainerResearchTable
     * 与 GTNH research-tweaks 的 SpecializedContainer 都成立。
     */
    public static TileResearchTable table() {
        EntityPlayer p = player();
        if (p == null || p.openContainer == null) {
            return null;
        }
        Container c = p.openContainer;
        for (Object o : c.inventorySlots) {
            Slot s = (Slot) o;
            if (s.inventory instanceof TileResearchTable) {
                return (TileResearchTable) s.inventory;
            }
        }
        return null;
    }

    /** 当前笔记。GTNH 路径下从 tile 的笔记槽位读（下标 1 = RESEARCH_NOTES_SLOT_INDEX）。 */
    public static ResearchNoteData note() {
        Minecraft mc = Minecraft.getMinecraft();
        if (mc != null && mc.currentScreen instanceof GuiResearchTable) {
            ResearchNoteData n = ((GuiResearchTable) mc.currentScreen).note;
            if (n != null) {
                return n;
            }
        }
        TileResearchTable t = table();
        if (t == null) {
            return null;
        }
        net.minecraft.item.ItemStack notes = t.getStackInSlot(1);
        return notes == null ? null : ResearchManager.getData(notes);
    }

    /** 当前屏幕是否为研究台（含 GTNH 变体）。 */
    public static boolean isResearchTableScreen(net.minecraft.client.gui.GuiScreen gui) {
        if (gui instanceof GuiResearchTable) {
            return true;
        }
        return gui instanceof GuiContainer && table() != null;
    }

    public static AspectList knowledge(EntityPlayer p) {
        if (p == null) {
            return new AspectList();
        }
        AspectList list = Thaumcraft.proxy.getPlayerKnowledge().getAspectsDiscovered(p.getCommandSenderName());
        return list == null ? new AspectList() : list;
    }

    public static void combine(TileResearchTable table, Aspect first, Aspect second) {
        EntityPlayer p = player();
        if (p == null || table == null) {
            return;
        }
        boolean bonusA = table.bonusAspects != null && table.bonusAspects.getAmount(first) > 0;
        boolean bonusB = table.bonusAspects != null && table.bonusAspects.getAmount(second) > 0;
        PacketHandler.INSTANCE.sendToServer(new PacketAspectCombinationToServer(
                p, table.xCoord, table.yCoord, table.zCoord, first, second, bonusA, bonusB, true));
    }

    public static void place(TileResearchTable table, HexUtils.Hex hex, Aspect aspect) {
        EntityPlayer p = player();
        if (p == null || table == null || hex == null || aspect == null) {
            return;
        }
        PacketHandler.INSTANCE.sendToServer(new PacketAspectPlaceToServer(
                p, (byte) hex.q, (byte) hex.r, table.xCoord, table.yCoord, table.zCoord, aspect));
    }

    // ===== 供批量/目标/合成控制器使用的助手方法 =====

    public static void combine(Aspect first, Aspect second) {
        combine(table(), first, second);
    }

    public static void place(HexUtils.Hex hex, Aspect aspect) {
        place(table(), hex, aspect);
    }

    /** 玩家知识池 + 研究台 bonus 的可用要素（用于合成决策）。 */
    public static AspectList availableAspects() {
        EntityPlayer p = player();
        TileResearchTable t = table();
        if (p == null) {
            return new AspectList();
        }
        AspectList available = new AspectList();
        String username = p.getCommandSenderName();
        for (Object value : Aspect.aspects.values()) {
            Aspect aspect = (Aspect) value;
            int amount = Thaumcraft.proxy.getPlayerKnowledge().getAspectPoolFor(username, aspect)
                    + (t != null && t.bonusAspects != null ? t.bonusAspects.getAmount(aspect) : 0);
            if (amount > 0) {
                available.add(aspect, amount);
            }
        }
        return available;
    }

    /** 研究台笔墨是否可用。 */
    public static boolean hasInk() {
        TileResearchTable t = table();
        return t != null && ResearchManager.consumeInkFromTable(t.getStackInSlot(0), false);
    }

    public static ItemStack researchNoteStack() {
        TileResearchTable t = table();
        return t == null ? null : t.getStackInSlot(1);
    }

    public static ItemStack scribingToolsStack() {
        TileResearchTable t = table();
        return t == null ? null : t.getStackInSlot(0);
    }

    public static int findIncompleteResearchNoteSlot() {
        return findIncompleteResearchNoteSlot(null);
    }

    public static int findIncompleteResearchNoteSlot(String key) {
        Container c = openContainer();
        if (c == null) {
            return -1;
        }
        int end = Math.min(38, c.inventorySlots.size());
        for (int slot = 2; slot < end; slot++) {
            ItemStack stack = c.getSlot(slot).getStack();
            if (ResearchNoteItems.isIncomplete(stack) && (key == null || ResearchNoteItems.hasKey(stack, key))) {
                return slot;
            }
        }
        return -1;
    }

    public static int countIncompleteResearchNotes() {
        return countIncompleteResearchNotes(null);
    }

    public static int countIncompleteResearchNotes(String key) {
        ItemStack tableStack = researchNoteStack();
        int count = ResearchNoteItems.isIncomplete(tableStack)
                && (key == null || ResearchNoteItems.hasKey(tableStack, key)) ? 1 : 0;
        Container c = openContainer();
        if (c == null) {
            return count;
        }
        int end = Math.min(38, c.inventorySlots.size());
        for (int slot = 2; slot < end; slot++) {
            ItemStack stack = c.getSlot(slot).getStack();
            if (ResearchNoteItems.isIncomplete(stack) && (key == null || ResearchNoteItems.hasKey(stack, key))) {
                count++;
            }
        }
        return count;
    }

    public static int findCompletedResearchNoteSlot(String key) {
        Container c = openContainer();
        if (c == null) {
            return -1;
        }
        int end = Math.min(38, c.inventorySlots.size());
        for (int slot = 1; slot < end; slot++) {
            ItemStack stack = c.getSlot(slot).getStack();
            if (ResearchNoteItems.isComplete(stack) && ResearchNoteItems.hasKey(stack, key)) {
                return slot;
            }
        }
        return -1;
    }

    public static List<String> researchNoteKeys() {
        List<String> completedKeys = new ArrayList<>();
        List<String> incompleteKeys = new ArrayList<>();
        Container c = openContainer();
        if (c == null) {
            return completedKeys;
        }
        int end = Math.min(38, c.inventorySlots.size());
        for (int slot = 1; slot < end; slot++) {
            ItemStack stack = c.getSlot(slot).getStack();
            ResearchNoteData data = ResearchNoteItems.data(stack);
            if (data == null || completedKeys.contains(data.key) || incompleteKeys.contains(data.key)) {
                continue;
            }
            (ResearchNoteItems.isComplete(stack) ? completedKeys : incompleteKeys).add(data.key);
        }
        List<String> keys = new ArrayList<>(completedKeys.size() + incompleteKeys.size());
        keys.addAll(completedKeys);
        keys.addAll(incompleteKeys);
        return keys;
    }

    public static int findUsableScribingToolsSlot() {
        Container c = openContainer();
        if (c == null) {
            return -1;
        }
        int end = Math.min(38, c.inventorySlots.size());
        for (int slot = 2; slot < end; slot++) {
            if (ResearchManager.consumeInkFromTable(c.getSlot(slot).getStack(), false)) {
                return slot;
            }
        }
        return -1;
    }

    private static Container openContainer() {
        EntityPlayer p = player();
        return p == null ? null : p.openContainer;
    }
}
