package com.emil.tcautoresearchgtnh.research;

import net.minecraft.util.StatCollector;

import com.emil.tcautoresearchgtnh.Config;

public final class SolverLocalization {

    private SolverLocalization() {
    }

    /** 把求解失败原因映射为本地化文案。 */
    public static String failure(String reason) {
        String key = "tcautores.failure." + (reason == null || reason.isEmpty() ? "unknown" : reason);
        String localized = StatCollector.translateToLocal(key);
        return localized.equals(key) ? StatCollector.translateToLocal("tcautores.failure.unknown") : localized;
    }

    /** 求解模式本地化文案。 */
    public static String mode(Config.SolveMode mode) {
        return StatCollector.translateToLocal(mode == Config.SolveMode.WEIGHTED
                ? "tcautores.mode_weighted_value"
                : "tcautores.mode_normal_value");
    }
}
