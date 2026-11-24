package com.emil.tcautoresearchgtnh.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 默认权重表。数据来源：GTNH 中文维基
 * {@code Data:神秘时代研究助手.tabx}（知识共享 CC0），
 * 覆盖 GTNH 2.9.0 神秘时代 fork 的 69 个要素。
 *
 * <p>{@code defaultCost} 越高，该要素越难出现在路径里；
 * 缺失或非有限值时回退到 {@link #DEFAULT_COST}。</p>
 */
public final class DefaultWeights {

    public static final int DEFAULT_COST = 16;

    private static final Map<String, Double> WEIGHTS = new LinkedHashMap<>();

    static {
        // 6 个元始要素
        put("Aer", 4);
        put("Terra", 4);
        put("Ignis", 4);
        put("Aqua", 2);
        put("Ordo", 1);
        put("Perditio", 2);

        put("Vacuos", 8);
        put("Lux", 8);
        put("Tempestas", 64);
        put("Motus", 4);
        put("Gelum", 64);
        put("Vitreus", 2);

        put("Victus", 8);
        put("Venenum", 64);
        put("Potentia", 2);
        put("Permutatio", 16);
        put("Metallum", 1);
        put("Mortuus", 64);

        put("Volatus", 8);
        put("Tenebrae", 16);
        put("Spiritus", 64);
        put("Sano", 64);
        put("Iter", 4);
        put("Alienis", 8);

        put("Praecantatio", 8);
        put("Auram", 64);
        put("Vitium", 64);
        put("Limus", 16);
        put("Herba", 4);
        put("Arbor", 4);

        put("Bestia", 64);
        put("Corpus", 16);
        put("Exanimis", 16);
        put("Cognitio", 4);
        put("Sensus", 4);
        put("Humanus", 64);

        put("Messis", 16);
        put("Perfodio", 64);
        put("Instrumentum", 1);
        put("Meto", 64);
        put("Telum", 16);
        put("Tutamen", 4);

        put("Fames", 64);
        put("Lucrum", 4);
        put("Fabrico", 1);
        put("Pannus", 8);
        put("Machina", 1);
        put("Vinculum", 16);

        put("Terminus", 512);
        put("Luxuria", 512);
        put("Infernus", 64);
        put("Superbia", 64);
        put("Gula", 16);
        put("Invidia", 512);

        put("Desidia", 64);
        put("Ira", 64);
        put("Tempus", 64);
        put("Strontio", 64);
        put("Nebrisum", 64);
        put("Electrum", 1);

        put("Magneto", 16);
        put("Radio", 64);
        put("Aequalitas", 512);
        put("Vesania", 512);
        put("Primordium", 512);
        put("Astrum", 512);

        put("Gloria", 512);
        put("Caelum", 512);
        put("Tabernus", 64);
    }

    private static void put(String tag, double cost) {
        // Thaumcraft 的 Aspect.getTag() 为小写，统一按小写存储
        WEIGHTS.put(tag.toLowerCase(Locale.ROOT), cost);
    }

    private DefaultWeights() {
    }

    /** 返回一份可修改的默认权重副本（tag -> cost）。 */
    public static Map<String, Double> defaults() {
        return new LinkedHashMap<>(WEIGHTS);
    }

    /** 取单个要素成本（大小写不敏感）；未知 tag 回退 {@link #DEFAULT_COST}。 */
    public static double cost(String tag) {
        if (tag == null) {
            return DEFAULT_COST;
        }
        Double c = WEIGHTS.get(tag);
        if (c == null) {
            c = WEIGHTS.get(tag.toLowerCase(Locale.ROOT));
        }
        if (c == null || !Double.isFinite(c) || c < 0) {
            return DEFAULT_COST;
        }
        return c;
    }

    /**
     * 按库存量把要素映射为 2 的正整数次幂权重，库存越稀缺权重越高，无库存返回 512。
     */
    public static Map<String, Double> inventoryCosts(Map<String, Integer> stockByTag) {
        List<Integer> positiveStock = new ArrayList<>();
        for (Integer amount : stockByTag.values()) {
            if (amount != null && amount > 0) {
                positiveStock.add(amount);
            }
        }
        Collections.sort(positiveStock);

        Map<String, Double> result = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : stockByTag.entrySet()) {
            int amount = entry.getValue() == null ? 0 : entry.getValue();
            result.put(entry.getKey(), percentileCost(amount, positiveStock));
        }
        return result;
    }

    /** 按库存量在正库存中的分位，折算为 [1, 512] 区间内 2 的幂。 */
    private static double percentileCost(int amount, List<Integer> positiveStock) {
        if (amount <= 0 || positiveStock.isEmpty()) {
            return 512.0;
        }
        int lower = lowerBound(positiveStock, amount);
        int upper = upperBound(positiveStock, amount);
        double percentile = (lower + (upper - lower) / 2.0D) / positiveStock.size();
        int exponent = Math.max(0, Math.min(8, (int) Math.floor(9.0D * (1.0D - percentile))));
        return (double) (1 << exponent);
    }

    private static int lowerBound(List<Integer> values, int target) {
        int low = 0;
        int high = values.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (values.get(middle) < target) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }

    private static int upperBound(List<Integer> values, int target) {
        int low = 0;
        int high = values.size();
        while (low < high) {
            int middle = (low + high) >>> 1;
            if (values.get(middle) <= target) {
                low = middle + 1;
            } else {
                high = middle;
            }
        }
        return low;
    }
}
