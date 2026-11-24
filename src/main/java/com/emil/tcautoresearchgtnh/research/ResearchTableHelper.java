package com.emil.tcautoresearchgtnh.research;

import java.util.List;

import net.minecraft.item.ItemStack;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;
import thaumcraft.common.lib.research.ResearchNoteData;
import thaumcraft.common.lib.utils.HexUtils;

/** 把 {@link ResearchTableAccess} 静态方法包装成 helper 接口，供控制器层复用。 */
public final class ResearchTableHelper implements GuiResearchTableHelperInterface {

    public static final ResearchTableHelper INSTANCE = new ResearchTableHelper();

    private ResearchTableHelper() {
    }

    @Override
    public ResearchNoteData note() {
        return ResearchTableAccess.note();
    }

    @Override
    public AspectList availableAspects() {
        return ResearchTableAccess.availableAspects();
    }

    @Override
    public void combine(Aspect first, Aspect second) {
        ResearchTableAccess.combine(first, second);
    }

    @Override
    public void place(HexUtils.Hex hex, Aspect aspect) {
        ResearchTableAccess.place(hex, aspect);
    }

    @Override
    public boolean hasInk() {
        return ResearchTableAccess.hasInk();
    }

    @Override
    public ItemStack researchNoteStack() {
        return ResearchTableAccess.researchNoteStack();
    }

    @Override
    public ItemStack scribingToolsStack() {
        return ResearchTableAccess.scribingToolsStack();
    }

    @Override
    public int findIncompleteResearchNoteSlot() {
        return ResearchTableAccess.findIncompleteResearchNoteSlot();
    }

    @Override
    public int findIncompleteResearchNoteSlot(String key) {
        return ResearchTableAccess.findIncompleteResearchNoteSlot(key);
    }

    @Override
    public int countIncompleteResearchNotes() {
        return ResearchTableAccess.countIncompleteResearchNotes();
    }

    @Override
    public int countIncompleteResearchNotes(String key) {
        return ResearchTableAccess.countIncompleteResearchNotes(key);
    }

    @Override
    public int findCompletedResearchNoteSlot(String key) {
        return ResearchTableAccess.findCompletedResearchNoteSlot(key);
    }

    @Override
    public List<String> researchNoteKeys() {
        return ResearchTableAccess.researchNoteKeys();
    }

    @Override
    public int findUsableScribingToolsSlot() {
        return ResearchTableAccess.findUsableScribingToolsSlot();
    }
}
