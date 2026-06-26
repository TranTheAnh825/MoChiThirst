package org.moChiThirst.utils;

import net.md_5.bungee.api.ChatColor;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Color {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([a-fA-F0-9]{6})");
    private static final Pattern GRADIENT_PATTERN = Pattern.compile(
            "&(#([a-fA-F0-9]{6})(?:-#([a-fA-F0-9]{6}))+)((?:&[k-oK-OrR])+)?(.*?)(?=(&#[a-fA-F0-9]{6}|&[a-fA-Fk-oK-OrR0-9])|$)");

    private Color() {}

    public static String translate(String input) {
        if (input == null || input.isEmpty()) return input;

        // Xử lý gradient trước
        Matcher gradientMatch = GRADIENT_PATTERN.matcher(input);
        StringBuffer sb = new StringBuffer();
        while (gradientMatch.find()) {
            String[] hexGroup = gradientMatch.group(1).split("-");
            String style = gradientMatch.group(4);
            String text  = gradientMatch.group(5);
            gradientMatch.appendReplacement(sb, Matcher.quoteReplacement(applyGradient(text, style, hexGroup)));
        }
        gradientMatch.appendTail(sb);
        input = sb.toString();

        // Xử lý hex đơn
        Matcher hexMatch = HEX_PATTERN.matcher(input);
        StringBuffer sbHex = new StringBuffer();
        while (hexMatch.find()) {
            hexMatch.appendReplacement(sbHex, ChatColor.of("#" + hexMatch.group(1)).toString());
        }
        hexMatch.appendTail(sbHex);
        input = sbHex.toString();

        return ChatColor.translateAlternateColorCodes('&', input);
    }

    private static String applyGradient(String text, String style, String[] hexColors) {
        if (text == null || text.isEmpty()) return "";

        int length   = text.length();
        int numHex   = hexColors.length;
        String formattedStyle = style != null ? ChatColor.translateAlternateColorCodes('&', style) : "";
        StringBuilder result = new StringBuilder();

        if (length <= numHex) {
            for (int i = 0; i < length; i++) {
                result.append(ChatColor.of(hexColors[i])).append(formattedStyle).append(text.charAt(i));
            }
            return result.toString();
        }

        float step = (float) (numHex - 1) / (length - 1);

        for (int i = 0; i < length; i++) {
            float pos      = i * step;
            int   idx      = (int) Math.floor(pos);
            int   nextIdx  = Math.min(idx + 1, numHex - 1);
            float ratio    = pos - idx;

            int r1 = Integer.parseInt(hexColors[idx].substring(1, 3), 16);
            int g1 = Integer.parseInt(hexColors[idx].substring(3, 5), 16);
            int b1 = Integer.parseInt(hexColors[idx].substring(5, 7), 16);

            int r2 = Integer.parseInt(hexColors[nextIdx].substring(1, 3), 16);
            int g2 = Integer.parseInt(hexColors[nextIdx].substring(3, 5), 16);
            int b2 = Integer.parseInt(hexColors[nextIdx].substring(5, 7), 16);

            int r = (int) (r1 + ratio * (r2 - r1));
            int g = (int) (g1 + ratio * (g2 - g1));
            int b = (int) (b1 + ratio * (b2 - b1));

            result.append(ChatColor.of(String.format("#%02x%02x%02x", r, g, b)))
                  .append(formattedStyle)
                  .append(text.charAt(i));
        }

        return result.toString();
    }
}
