package pl.michnam.app.util;

import java.util.ArrayList;
import java.util.List;

public class MathCalc {
    public static double sdFromList(ArrayList<Integer> data, double avg) {
        double tmp = 0;
        for (int i = 0; i < data.size(); i++) {
            int val = data.get(i);
            double sqrtDiff = Math.pow(val - avg, 2);
            tmp += sqrtDiff;
        }
        double avgOfDiffs = tmp / (double) (data.size());

        return Math.sqrt(avgOfDiffs);
    }

    public static double averageList(List<Integer> data) {
        return data.stream()
                .mapToInt(Integer::intValue)
                .average()
                .orElse(0.0);
    }

    public static double averageListDouble(List<Double> data) {
        return data.stream()
                .mapToDouble(Double::doubleValue)
                .average()
                .orElse(0.0);
    }
}
