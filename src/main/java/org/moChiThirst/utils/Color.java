package org.moChiThirst.utils;

import net.md_5.bungee.api.ChatColor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {
    private static final Pattern hexPattern = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final Pattern gradientPattern = Pattern.compile(
            "&(#([a-fA-F0-9]{6})(?:-#([a-fA-F0-9]{6}))+)((?:&[k-oK-OrR])+)?(.*?)(?=(&#[a-fA-F0-9]{6}|&[a-fA-Fk-oK-OrR0-9])|$)");

    public static String translate(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        Matcher gradientMatch = gradientPattern.matcher(input);
        StringBuffer sbGradient = new StringBuffer();
        while (gradientMatch.find()) {
            String[] hexGroup = gradientMatch.group(1).split("-");
            String style = gradientMatch.group(4);
            String text = gradientMatch.group(5);

            gradientMatch.appendReplacement(sbGradient, Matcher.quoteReplacement(gradientTranslate(text, style, hexGroup)));
        }
        gradientMatch.appendTail(sbGradient);
        input = sbGradient.toString();

        Matcher hexMatch = hexPattern.matcher(input);
        StringBuffer sbHex = new StringBuffer();
        while (hexMatch.find()) {
            String hexColor = "#" + hexMatch.group(1);
            hexMatch.appendReplacement(sbHex, ChatColor.of(hexColor).toString());
        }
        hexMatch.appendTail(sbHex);
        input = sbHex.toString();

        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private static String gradientTranslate(String input, String style, String[] hexGroup) {
        if (input == null || input.isEmpty()) return "";

        int textLength = input.length();
        int hexCount = hexGroup.length;
        StringBuilder gradientString = new StringBuilder();

        String formattedStyle = style != null ? ChatColor.translateAlternateColorCodes('&', style) : "";

        if (textLength <= hexCount) {
            for (int i = 0; i < textLength; i++) {
                gradientString.append(ChatColor.of(hexGroup[i]))
                        .append(formattedStyle)
                        .append(input.charAt(i));
            }
            return gradientString.toString();
        }

        float step = (float) (hexCount - 1) / (textLength - 1);

        for (int i = 0; i < textLength; i++) {
            float interval = i * step;
            int index = (int) Math.floor(interval);
            int nextIndex = Math.min(index + 1, hexCount - 1);
            float ratio = interval - index;

            int r1 = Integer.parseInt(hexGroup[index].substring(1, 3), 16);
            int g1 = Integer.parseInt(hexGroup[index].substring(3, 5), 16);
            int b1 = Integer.parseInt(hexGroup[index].substring(5, 7), 16);

            int r2 = Integer.parseInt(hexGroup[nextIndex].substring(1, 3), 16);
            int g2 = Integer.parseInt(hexGroup[nextIndex].substring(3, 5), 16);
            int b2 = Integer.parseInt(hexGroup[nextIndex].substring(5, 7), 16);

            int r = (int) (r1 + ratio * (r2 - r1));
            int g = (int) (g1 + ratio * (g2 - g1));
            int b = (int) (b1 + ratio * (b2 - b1));

            String hexColor = String.format("#%02x%02x%02x", r, g, b);
            gradientString.append(ChatColor.of(hexColor))
                    .append(formattedStyle)
                    .append(input.charAt(i));
        }

        return gradientString.toString();
    }
}
