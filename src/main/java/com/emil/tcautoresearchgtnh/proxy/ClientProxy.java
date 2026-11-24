package com.emil.tcautoresearchgtnh.proxy;

import net.minecraftforge.common.MinecraftForge;

import com.emil.tcautoresearchgtnh.client.ClientEvents;
import com.emil.tcautoresearchgtnh.client.ClientTickHandler;
import com.emil.tcautoresearchgtnh.research.ClientResearchTickHandler;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

public class ClientProxy extends CommonProxy {

    @Override
    public void postInit(FMLPostInitializationEvent event) {
        MinecraftForge.EVENT_BUS.register(new ClientEvents());
        FMLCommonHandler.instance().bus().register(new ClientTickHandler());
        FMLCommonHandler.instance().bus().register(new ClientResearchTickHandler());
    }
}
