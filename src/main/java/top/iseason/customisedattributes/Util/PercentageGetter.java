package top.iseason.customisedattributes.Util;

import top.iseason.customisedattributes.ConfigManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PercentageGetter {
    private static final Pattern area = Pattern.compile("[0-9]+\\.?[0-9]*.*-.*[0-9]+\\.?[0-9]*");
    private static final Pattern num = Pattern.compile("[0-9]+\\.?[0-9]*");

    public static double formatString(String string) {
        if (string == null || string.isEmpty()) {
            return 0.0;
        }
        Matcher areaMatcher = area.matcher(string);
        if (areaMatcher.find()) {
            double min, max;
            String[] split = string.split("-");
            min = Double.parseDouble(split[0]);
            max = Double.parseDouble(split[1]);
            double add = ConfigManager.random.nextDouble() * (max - min);
            return min + add;
        }
        Matcher matcher = num.matcher(string);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }
        return 0.0;
    }
}
