package com.emil.tcautoresearchgtnh.research;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/**
 * 各研究控制器的心跳驱动。
 */
public final class ClientResearchTickHandler {

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ResearchNoteGenerationController.clientTick();
        AspectSynthesisController.clientTick();
        ResearchSolveController.clientTick();
        BatchResearchController.clientTick();
        TargetResearchController.clientTick();
    }
}
