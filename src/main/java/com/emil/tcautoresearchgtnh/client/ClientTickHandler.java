package com.emil.tcautoresearchgtnh.client;

import net.minecraft.client.Minecraft;

import com.emil.tcautoresearchgtnh.research.ResearchSession;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

/** 客户端心跳：驱动求解会话的分步执行与自动求解。 */
public final class ClientTickHandler {

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getMinecraft();
        if (mc == null || mc.thePlayer == null) {
            return;
        }
        ResearchSession.get().tick();
    }
}
