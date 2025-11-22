package com.dailybrief;

import com.dailybrief.commands.RootCommand;
import picocli.CommandLine;

public class Responsi1_PPBO_L0124042_SC {
    public static void main(String[] args) {
        int exitCode = new CommandLine(new RootCommand()).execute(args);
        System.exit(exitCode);
    }
}