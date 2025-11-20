package com.dailybrief.ui;

public class AsciiArt {

    // 5 lines high for consistency
    public static final String SUN =
            "  \\   /  \n" +
            "   .-.   \n" +
            "― (   ) ―\n" +
            "   `-’   \n" +
            "  /   \\  ";

    public static final String CLOUD =
            "   .--.    \n" +
            " .-(   ).  \n" +
            "(___.__)_) \n" +
            "           \n" +
            "           ";

    public static final String RAIN =
            "  _  /     \n" +
            " / )( \\    \n" +
            " ' ' ' '   \n" +
            "  ' ' '    \n" +
            "           ";

    public static String getWeatherArt(String iconCode) {
        if (iconCode == null) {
            return CLOUD; // Default
        }
        // OpenWeatherMap icon codes: https://openweathermap.org/weather-conditions
        if (iconCode.startsWith("01")) {
            return SUN;
        } else if (iconCode.startsWith("09") || iconCode.startsWith("10") || iconCode.startsWith("11")) {
            return RAIN;
        } else {
            return CLOUD; // 02, 03, 04, 13, 50 etc.
        }
    }
}
