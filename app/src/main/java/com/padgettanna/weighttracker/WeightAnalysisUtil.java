package com.padgettanna.weighttracker;

import com.padgettanna.weighttracker.model.WeightEntry;

import java.util.ArrayList;
import java.util.List;

public final class WeightAnalysisUtil {

    private WeightAnalysisUtil() {}

    public enum Trend {
        UPWARD,     // weight going up
        DOWNWARD,   // weight going down
        STABLE
    }

    /**
     * Calculates a rolling average (simple moving average) over the entries.
     */
    public static List<Double> rollingAverage(List<WeightEntry> entries, int windowSize) {
        List<Double> result = new ArrayList<>();
        if (entries == null || entries.isEmpty() || windowSize <= 0) return result;

        for (int i = 0; i < entries.size(); i++) {
            int start = Math.max(0, i - windowSize + 1);

            double sum = 0;
            for (int j = start; j <= i; j++) {
                sum += entries.get(j).getWeight();
            }

            result.add(sum / (i - start + 1));
        }

        return result;
    }

    /**
     * Detects overall trend based on the change between the first and last rolling average.
     * threshold: how much change counts as "real" (example: 0.5 lbs)
     */
    public static Trend detectTrend(List<Double> rollingAverages, double threshold) {
        if (rollingAverages == null || rollingAverages.size() < 2) return Trend.STABLE;

        double recent = rollingAverages.get(rollingAverages.size() - 1);
        double previous = rollingAverages.get(rollingAverages.size() - 2);
        double delta = recent - previous;

        if (delta > threshold) return Trend.UPWARD;
        if (delta < -threshold) return Trend.DOWNWARD;
        return Trend.STABLE;
    }
}

