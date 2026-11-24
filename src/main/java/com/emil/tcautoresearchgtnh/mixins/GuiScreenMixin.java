package com.emil.tcautoresearchgtnh.mixins;

import net.minecraft.client.gui.GuiScreen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.emil.tcautoresearchgtnh.client.ClientEvents;

/**
 * 研究台工具条的鼠标点击处理入口。
 */
@Mixin(GuiScreen.class)
public abstract class GuiScreenMixin {

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void tcAutoResearchGTNH$handleOverlayClick(int mouseX, int mouseY, int button, CallbackInfo ci) {
        if (ClientEvents.handleMouseClick((GuiScreen) (Object) this, mouseX, mouseY, button)) {
            ci.cancel();
        }
    }
}
