package com.emil.tcautoresearchgtnh;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.minecraftforge.common.config.Configuration;

import com.emil.tcautoresearchgtnh.core.DefaultWeights;

import thaumcraft.api.aspects.Aspect;
import thaumcraft.api.aspects.AspectList;

/**
 * 配置。文件 {@code config/tcautoresearchgtnh.cfg}。
 */
public final class Config {

    private static Configuration config;

    /** 自动解锁当前笔记。 */
    public static boolean autoSolve = false;
    /** 每个合成/放置动作之间的间隔（tick）。 */
    public static int actionIntervalTicks = 4;
    /** 完成时是否显示通知。 */
    public static boolean notifyOnFinish = true;
    /** 执行前是否预览方案。 */
    public static boolean previewPlan = true;
    /** 完成报告中是否显示棋盘预览。 */
    public static boolean reportBoardPreview = true;
    /** 加权模式开关。 */
    public static boolean weightedMode = false;
    /** 库存感知：加权模式下对无库存的要素额外惩罚，优先使用已有要素。 */
    public static boolean inventoryAware = false;
    /** 求解超时（毫秒）。 */
    public static int solveTimeoutMs = 8000;
    /** 束搜索宽度。 */
    public static int beamWidth = 128;
    /** 工具条缩放系数（0.5~2.0）。 */
    public static float guiScale = 1.0f;
    /** 工具条左上角位置（未缩放的 GUI 坐标）。 */
    public static float toolbarX = 20.0f;
    public static float toolbarY = 20.0f;

    private static long revision = 0;

    /**
     * 默认权重数据版本。wiki 数据更新或修复历史错误默认值时递增；
     * 配置文件中记录的版本低于此值时，自动用新默认值覆盖 weighted 段。
     */
    private static final int WEIGHT_DATA_VERSION = 2;

    private static Map<String, Double> weights = new LinkedHashMap<>();
    /** 禁用要素集合（tag 原始大小写），不能作为中间放置，锚点不受限。 */
    private static final Set<String> disabledAspects = new LinkedHashSet<>();

    private Config() {
    }

    public static void load(File suggested) {
        config = new Configuration(suggested);
        config.load();
        sync();
    }

    private static void sync() {
        autoSolve = config.getBoolean("AutoResearch", "general", autoSolve,
                "Automatically solve the note in the research table.");
        actionIntervalTicks = config.getInt("ActionIntervalTicks", "general", actionIntervalTicks, 1, 40,
                "Ticks between two combine/place packets. Higher is safer on laggy servers.");
        notifyOnFinish = config.getBoolean("NotifyOnFinish", "general", notifyOnFinish,
                "Show a notification when a note is solved.");
        previewPlan = config.getBoolean("PreviewBeforeSolving", "general", previewPlan,
                "Show the computed routes before applying them.");
        reportBoardPreview = config.getBoolean("ReportBoardPreview", "general", reportBoardPreview,
                "Show a board preview in the completion report.");
        guiScale = (float) config.get("general", "GuiScale", guiScale,
                "Overlay toolbar scale (0.5 - 2.0).", 0.5, 2.0).getDouble();
        toolbarX = (float) config.get("general", "ToolbarX", toolbarX,
                "Overlay toolbar X position.", 0.0, 2000.0).getDouble();
        toolbarY = (float) config.get("general", "ToolbarY", toolbarY,
                "Overlay toolbar Y position.", 0.0, 2000.0).getDouble();
        weightedMode = config.getBoolean("WeightedMode", "general", weightedMode,
                "Use the weighted aspect costs (from the GTNH wiki) instead of hop count when solving.");
        inventoryAware = config.getBoolean("InventoryAware", "general", inventoryAware,
                "In weighted mode, add an extra penalty for aspects the player does not currently hold.");

        disabledAspects.clear();
        for (String tag : config.getStringList("DisabledAspects", "general", new String[0],
                "Aspect tags excluded from generated intermediate placements (anchors are unaffected).")) {
            if (tag != null && !tag.trim().isEmpty()) {
                disabledAspects.add(tag.trim());
            }
        }

        Map<String, Double> defaults = DefaultWeights.defaults();
        // 旧配置权重可能被写坏（如全为 16），检测到则迁移为默认权重
        int storedVersion = config.get("general", "WeightDataVersion", 0,
                "Internal default-weights data version. Do not edit.").getInt();
        Map<String, Double> storedWeights = new LinkedHashMap<>();
        boolean allSixteen = true;
        for (Map.Entry<String, Double> e : defaults.entrySet()) {
            double stored = config.get("weighted", e.getKey(), e.getValue(),
                    "Cost for aspect " + e.getKey() + ". Higher = less likely to appear in the path.",
                    0.0, 100000.0).getDouble();
            storedWeights.put(e.getKey(), stored);
            if (Math.round(stored) != DefaultWeights.DEFAULT_COST) {
                allSixteen = false;
            }
        }
        // 维基默认中有 10 个要素本身就是 16，因此只有「全部 16」才视为坏数据
        boolean migrateWeights = storedVersion < WEIGHT_DATA_VERSION
                || (allSixteen && !defaults.isEmpty());
        weights = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : defaults.entrySet()) {
            String tag = e.getKey();
            String comment = "Cost for aspect " + tag + ". Higher = less likely to appear in the path.";
            double v = migrateWeights ? e.getValue() : storedWeights.get(tag);
            if (!Double.isFinite(v) || v < 0) {
                v = DefaultWeights.DEFAULT_COST;
            }
            if (migrateWeights) {
                config.get("weighted", tag, e.getValue(), comment, 0.0, 100000.0).set(v);
            }
            weights.put(tag, v);
        }
        if (migrateWeights) {
            config.get("general", "WeightDataVersion", WEIGHT_DATA_VERSION,
                    "Internal default-weights data version. Do not edit.").set(WEIGHT_DATA_VERSION);
            TCAutoResearchGTNH.LOG.info("检测到旧权重配置，已自动迁移为维基默认权重");
        }

        if (config.hasChanged()) {
            config.save();
        }
    }

    /** 当前要素成本表（已合并默认值与用户配置）。 */
    public static Map<String, Double> weights() {
        return weights;
    }

    /** 取某个要素的成本（大小写不敏感）。 */
    public static double weight(String tag) {
        if (tag == null) {
            return DefaultWeights.DEFAULT_COST;
        }
        Double v = weights.get(tag);
        if (v == null) {
            v = weights.get(tag.toLowerCase(java.util.Locale.ROOT));
        }
        return v == null ? DefaultWeights.cost(tag) : v;
    }

    /** 取某个要素的成本（按 Aspect 对象）。 */
    public static double weight(Aspect aspect) {
        return aspect == null ? DefaultWeights.DEFAULT_COST : weight(aspect.getTag());
    }

    /** 当前禁用要素集合（tag 原始大小写）。 */
    public static Set<String> disabledAspectTags() {
        return disabledAspects;
    }

    /** 某要素是否被禁用（禁用要素不能作为中间放置）。 */
    public static boolean isAspectDisabled(String tag) {
        return tag != null && disabledAspects.contains(tag);
    }

    public static void setAuto(boolean value) {
        autoSolve = value;
        if (config != null) {
            config.get("general", "AutoResearch", false).set(value);
            config.save();
        }
    }

    public static boolean autoResearch() {
        return autoSolve;
    }

    public static void setWeighted(boolean value) {
        weightedMode = value;
        if (config != null) {
            config.get("general", "WeightedMode", false).set(value);
            config.save();
        }
    }

    /** 把所有权重重置为默认值。 */
    public static void resetWeights() {
        Map<String, Double> defaults = DefaultWeights.defaults();
        weights = new LinkedHashMap<>(defaults);
        if (config != null) {
            for (Map.Entry<String, Double> e : defaults.entrySet()) {
                config.get("weighted", e.getKey(), e.getValue()).set(e.getValue());
            }
            config.save();
        }
    }

    // ===== 权重 GUI 支持 =====

    public enum SolveMode { NORMAL, WEIGHTED }

    public static SolveMode solveMode() {
        return weightedMode ? SolveMode.WEIGHTED : SolveMode.NORMAL;
    }

    public static void setSolveMode(SolveMode mode) {
        setWeighted(mode == SolveMode.WEIGHTED);
    }

    public static boolean previewResult() {
        return previewPlan;
    }

    public static void setPreviewResult(boolean value) {
        previewPlan = value;
        if (config != null) {
            config.get("general", "PreviewBeforeSolving", true).set(value);
            config.save();
        }
    }

    public static boolean reportBoardPreview() {
        return reportBoardPreview;
    }

    public static void setReportBoardPreview(boolean value) {
        reportBoardPreview = value;
        if (config != null) {
            config.get("general", "ReportBoardPreview", true).set(value);
            config.save();
        }
    }

    public static void setGuiScale(float value) {
        guiScale = Math.max(0.5f, Math.min(2.0f, value));
        if (config != null) {
            config.get("general", "GuiScale", 1.0f).set(guiScale);
            config.save();
        }
    }

    public static void setToolbarPosition(float x, float y) {
        toolbarX = Math.max(0.0f, Math.min(2000.0f, x));
        toolbarY = Math.max(0.0f, Math.min(2000.0f, y));
    }

    public static void saveToolbarPosition() {
        if (config != null) {
            config.get("general", "ToolbarX", 4.0f).set(toolbarX);
            config.get("general", "ToolbarY", 4.0f).set(toolbarY);
            config.save();
        }
    }

    public static boolean inventoryAware() {
        return inventoryAware;
    }

    public static void setInventoryAware(boolean value) {
        inventoryAware = value;
        if (config != null) {
            config.get("general", "InventoryAware", false).set(value);
            config.save();
        }
    }

    /** 单个要素成本（GUI 用整数语义）。 */
    public static int getAspectCost(String tag) {
        return (int) Math.round(weight(tag));
    }

    public static void setAspectCost(String tag, int value) {
        if (tag == null) {
            return;
        }
        weights.put(tag, (double) clampCost(value));
        markSolverDirty();
        persistWeights();
        saveSolverConfiguration();
    }

    public static void setAspectDisabled(String tag, boolean disabled) {
        if (tag == null) {
            return;
        }
        if (disabled) {
            disabledAspects.add(tag);
        } else {
            disabledAspects.remove(tag);
        }
        markSolverDirty();
        persistDisabled();
        saveSolverConfiguration();
    }

    /** 把所有权重与禁用状态恢复为维基默认。 */
    public static void applyWikiDefaults() {
        weights.clear();
        weights.putAll(DefaultWeights.defaults());
        disabledAspects.clear();
        markSolverDirty();
        persistWeights();
        persistDisabled();
        saveSolverConfiguration();
    }

    /** 按当前要素库存分配权重（percentile -&gt; 2 的幂）。 */
    public static void applyInventoryWeights(AspectList inventory) {
        Map<String, Integer> stock = new LinkedHashMap<>();
        if (inventory != null) {
            for (Aspect a : Aspect.aspects.values()) {
                stock.put(a.getTag(), inventory.getAmount(a));
            }
        }
        weights.clear();
        weights.putAll(DefaultWeights.inventoryCosts(stock));
        markSolverDirty();
        persistWeights();
        saveSolverConfiguration();
    }

    public static void saveSolverConfiguration() {
        if (config != null) {
            config.save();
        }
    }

    public static File configDirectory() {
        return config == null ? new File("config") : config.getConfigFile().getParentFile();
    }

    /** 权重方案快照（用于保存/加载）。 */
    public static final class WeightProfile {
        public final String name;
        public final Map<String, Integer> costs;
        public final Set<String> disabled;

        public WeightProfile(String name, Map<String, Integer> costs, Set<String> disabled) {
            this.name = name;
            this.costs = costs;
            this.disabled = disabled;
        }
    }

    public static WeightProfile snapshotProfile(String name) {
        Map<String, Integer> costs = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            costs.put(e.getKey(), (int) Math.round(e.getValue()));
        }
        return new WeightProfile(name, costs, new LinkedHashSet<>(disabledAspects));
    }

    public static void applyProfile(WeightProfile profile) {
        if (profile == null) {
            return;
        }
        weights.clear();
        if (profile.costs != null) {
            for (Map.Entry<String, Integer> e : profile.costs.entrySet()) {
                int v = e.getValue() == null ? DefaultWeights.DEFAULT_COST : e.getValue();
                weights.put(e.getKey(), (double) clampCost(v));
            }
        }
        disabledAspects.clear();
        if (profile.disabled != null) {
            disabledAspects.addAll(profile.disabled);
        }
        markSolverDirty();
        persistWeights();
        persistDisabled();
        saveSolverConfiguration();
    }

    /** 生成求解器配置快照（供求解线程使用）。 */
    public static synchronized SolverSettings snapshot() {
        return new SolverSettings(
                solveMode(),
                previewPlan,
                inventoryAware,
                solveTimeoutMs,
                beamWidth,
                revision,
                new LinkedHashMap<>(weights),
                new LinkedHashSet<>(disabledAspects));
    }

    private static void markSolverDirty() {
        revision++;
    }

    private static List<String> sortedKeys(Map<String, ?> map) {
        List<String> keys = new ArrayList<>(map.keySet());
        Collections.sort(keys);
        return keys;
    }

    private static int clampCost(int value) {
        return Math.max(0, Math.min(100000, value));
    }

    private static void persistWeights() {
        if (config == null) {
            return;
        }
        for (Map.Entry<String, Double> e : weights.entrySet()) {
            config.get("weighted", e.getKey(), e.getValue()).set(e.getValue());
        }
    }

    private static void persistDisabled() {
        if (config == null) {
            return;
        }
        List<String> tags = new ArrayList<>(disabledAspects);
        Collections.sort(tags);
        config.get("general", "DisabledAspects", new String[0]).set(tags.toArray(new String[0]));
    }

    /** 求解器只读配置快照。 */
    public static final class SolverSettings {

        public final SolveMode mode;
        public final boolean preview;
        public final boolean inventoryAware;
        public final int timeoutMs;
        public final int beamWidth;
        public final long revision;
        private final Map<String, Double> costs;
        private final Set<String> disabled;

        private SolverSettings(SolveMode mode, boolean preview, boolean inventoryAware, int timeoutMs, int beamWidth,
                long revision, Map<String, Double> costs, Set<String> disabled) {
            this.mode = mode;
            this.preview = preview;
            this.inventoryAware = inventoryAware;
            this.timeoutMs = timeoutMs;
            this.beamWidth = beamWidth;
            this.revision = revision;
            this.costs = Collections.unmodifiableMap(new LinkedHashMap<>(costs));
            this.disabled = Collections.unmodifiableSet(new LinkedHashSet<>(disabled));
        }

        public int cost(String tag) {
            Double value = costs.get(tag);
            if (value == null) {
                value = costs.get(tag.toLowerCase(java.util.Locale.ROOT));
            }
            return (int) Math.round(value == null ? DefaultWeights.cost(tag) : value);
        }

        public boolean disabled(String tag) {
            return disabled.contains(tag);
        }

        public String fingerprint() {
            StringBuilder result = new StringBuilder();
            result.append(mode)
                    .append('|')
                    .append(inventoryAware)
                    .append('|')
                    .append(timeoutMs)
                    .append('|')
                    .append(beamWidth)
                    .append('|');
            for (String tag : sortedKeys(costs)) {
                result.append(tag).append('=').append(costs.get(tag)).append(';');
            }
            List<String> disabledTags = new ArrayList<>(disabled);
            Collections.sort(disabledTags);
            for (String tag : disabledTags) {
                result.append('!').append(tag).append(';');
            }
            return result.toString();
        }

        SolverSettings withBeamWidth(int replacement) {
            return new SolverSettings(mode, preview, inventoryAware, timeoutMs, replacement, revision,
                    new LinkedHashMap<>(costs), new LinkedHashSet<>(disabled));
        }
    }
}
