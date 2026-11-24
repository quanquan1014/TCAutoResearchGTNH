package com.emil.tcautoresearchgtnh.research;

import java.util.List;

import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.utils.HexUtils;

/**
 * 研究台操作抽象，由 {@link ResearchTableHelper} 委托 {@link ResearchTableAccess} 实现。
 */
public interface GuiResearchTableHelperInterface {

    ResearchNoteData note();

    AspectList availableAspects();

    void combine(Aspect first, Aspect second);

    void place(HexUtils.Hex hex, Aspect aspect);

    boolean hasInk();

    ItemStack researchNoteStack();

    ItemStack scribingToolsStack();

    int findIncompleteResearchNoteSlot();

    int findIncompleteResearchNoteSlot(String key);

    int countIncompleteResearchNotes();

    int countIncompleteResearchNotes(String key);

    int findCompletedResearchNoteSlot(String key);

    List<String> researchNoteKeys();

    int findUsableScribingToolsSlot();
}
