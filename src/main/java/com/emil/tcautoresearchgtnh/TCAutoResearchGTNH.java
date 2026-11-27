package com.emil.tcautoresearchgtnh;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.emil.tcautoresearchgtnh.proxy.CommonProxy;

import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;

@Mod(
    modid = TCAutoResearchGTNH.MODID,
    name = TCAutoResearchGTNH.NAME,
    version = TCAutoResearchGTNH.VERSION,
    acceptedMinecraftVersions = "[1.7.10]",
    dependencies = "required-after:Thaumcraft;after:ThaumcraftResearchTweaks")
public class TCAutoResearchGTNH {

    public static final String MODID = "tcautoresearchgtnh";
    public static final String NAME = "TC Auto Research GTNH";
    public static final String VERSION = "1.2.0";
    public static final Logger LOG = LogManager.getLogger(MODID);

    @SidedProxy(
        clientSide = "com.emil.tcautoresearchgtnh.proxy.ClientProxy",
        serverSide = "com.emil.tcautoresearchgtnh.proxy.CommonProxy")
    public static CommonProxy proxy;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        // 配置必须在 preInit 阶段加载：GTNH fork 的 FMLPostInitializationEvent 没有 getSuggestedConfigurationFile()。
        Config.load(event.getSuggestedConfigurationFile());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);
    }
}
