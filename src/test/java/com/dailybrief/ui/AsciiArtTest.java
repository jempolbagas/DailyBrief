package com.dailybrief.ui;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class AsciiArtTest {

    @Test
    public void testAsciiArtWidths() {
        verifyWidth(AsciiArt.SUN, "SUN");
        verifyWidth(AsciiArt.CLOUD, "CLOUD");
        verifyWidth(AsciiArt.RAIN, "RAIN");
    }

    private void verifyWidth(String art, String name) {
        String[] lines = art.split("\n");
        for (int i = 0; i < lines.length; i++) {
            assertEquals(AsciiArt.WIDTH, lines[i].length(),
                String.format("Line %d of %s should be exactly %d chars wide", i, name, AsciiArt.WIDTH));
        }
    }
}
