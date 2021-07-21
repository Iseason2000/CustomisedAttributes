package top.iseason.customisedattributes.Util;

import top.iseason.customisedattributes.ConfigManager;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PercentageGetter {
    static Pattern area = Pattern.compile("[0-9.]+.*-.*[0-9.]+");
    static Pattern num = Pattern.compile("[0-9.]+");

    public static double formatString(String string) {
        if (string == null || string.isEmpty()) {
            return 0.0;
        }
        Matcher areaMatcher = area.matcher(string);
        if (areaMatcher.find()) {
            Matcher numMatcher = num.matcher(string);
            double min = 0, max = 0;
            if (numMatcher.find()) {
                min = Double.parseDouble(numMatcher.group());
            }
            if (numMatcher.find()) {
                max = Double.parseDouble(numMatcher.group());
            }
            int add = ConfigManager.random.nextInt((int) (max - min));
            return min + add;
        }
        Matcher matcher = num.matcher(string);
        if (matcher.find()) {
            return Double.parseDouble(matcher.group());
        }
        return 0.0;
    }
}
