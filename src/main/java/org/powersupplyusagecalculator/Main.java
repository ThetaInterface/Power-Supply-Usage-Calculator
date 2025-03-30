package org.powersupplyusagecalculator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Scanner;
import java.lang.ProcessBuilder;
import java.lang.Process;
import java.lang.Runnable;

final public class Main {
    final private static Scanner userInputScan = new Scanner(System.in);

    private static float maxPowerSupplyUsage = 0;
    private static boolean isEnded = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.print("Plug off your charging things before start (press enter to continue)!");
        userInputScan.nextLine();

        Runnable task = () -> {
            userInputScan.nextLine();
            isEnded = true;
        };

        ArrayList<Float> powerHistory = new ArrayList<>();

        Thread thread = new Thread(task);
        thread.start();

        ProcessBuilder bProcess = new ProcessBuilder("cat", "/sys/class/power_supply/BAT0/uevent");

        while (!isEnded) {
            clearConsole();
            System.out.println("Press Enter to kill program\n");

            Process process = bProcess.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                StringBuilder output = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null)
                    output.append(line).append("\n");

                String outputData = output.toString();
                String powerString = foundEntryInStringArray(outputData.split("\n"), "POWER_SUPPLY_POWER_NOW");
                powerString = powerString.split("=")[1];

                float powerValue = Float.parseFloat(powerString) / 1000000f;

                if (powerValue > maxPowerSupplyUsage)
                    maxPowerSupplyUsage = powerValue;

                System.out.println("Current power usage: " +  powerValue + " Watt");
                System.out.println("Max power usage: " + maxPowerSupplyUsage + " Watt");

                powerHistory.add(powerValue);

                Thread.sleep(1000);
            }
        }

        float averagePowerUsage = 0;
        for (float var : powerHistory.toArray(new Float[0]))
            averagePowerUsage += var;

        averagePowerUsage /= powerHistory.size();

        System.out.println("Average power usage: " + averagePowerUsage + " Watt");
        System.out.println("Max power usage: " + maxPowerSupplyUsage + " Watt");
    }

    private static void clearConsole() throws IOException, InterruptedException {
        new ProcessBuilder("clear").inheritIO().start().waitFor();
    }

    private static String foundEntryInStringArray(String[] origin, String entry) {
        for (int i = 0; i < origin.length; i++)
            if (origin[i].contains(entry))
                return origin[i];

        return "null";
    }
}