package org.example;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        if(args.length < 2)
        {
            System.out.println("Pls.");
            return;
        }

        int day = 0;
        if(args.length > 2)
        {
            day = Integer.parseInt(args[2]);
        }

        String fullArg0 = args[0] + "010000";
        String fullArg1 = args[1] + "010000";

        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");
        DateTimeFormatter formatArg = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

        LocalDateTime from = LocalDateTime.parse(fullArg0, formatArg);
        LocalDateTime to = LocalDateTime.parse(fullArg1, formatArg);

        InputStream input = Main.class.getResourceAsStream("/dataexport.csv");
        InputStreamReader reader = new InputStreamReader(input);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(reader);

        double totalMW = 0;

        ArrayList<Double> monthlyMW = new ArrayList<>();
        double currentMonth = 0;
        int monthCounter = 0;

        ArrayList<Double> dailyMW = new ArrayList<>();
        double currentDay = 0;

        for (CSVRecord record : records)
        {
            if(timestampCheck(record.get(0)))
            {
                LocalDateTime recordDate = LocalDateTime.parse(record.get(0), format);
                if(recordDate.isAfter(from) && recordDate.isBefore(to))
                {
                    totalMW += Double.parseDouble(record.get(1));
                    currentMonth += Double.parseDouble(record.get(1));
                    if(recordDate.getDayOfMonth() == recordDate.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth() && recordDate.getHour() == 23)
                    {
                        monthlyMW.add(currentMonth);
                        currentMonth = 0;
                        monthCounter++;
                    }
                    if(recordDate.getDayOfWeek().getValue() == day)
                    {
                        currentDay += Double.parseDouble(record.get(1));
                        if(recordDate.getHour() == 23)
                        {
                            dailyMW.add(currentDay);
                            currentDay = 0;
                        }
                    }
                }
            }
        }

        System.out.println("Celkovy vykon za dane obdobi: " + totalMW + " W/m2.");
        for(int i = 0; i < monthCounter; i++)
        {
            LocalDateTime mon = from.plusMonths(i);
            System.out.println("Vykon za " + mon.getYear() + "/" + mon.getMonth().getValue() + ": " + monthlyMW.get(i));
        }
        int dailyH = 0;
        double monthlyDailyMW = 0;
        int countOfDays = 0;
        for(LocalDateTime date = from; date.isBefore(to); date = date.plusDays(1))
        {
            if(date.getDayOfWeek().getValue() == day)
            {
                System.out.println("Vykon za " + date.getYear() + "/" + date.getMonth().getValue() + "/" + date.getDayOfMonth() + ": " + dailyMW.get(dailyH));
                monthlyDailyMW += dailyMW.get(dailyH);
                dailyH++;
                countOfDays++;
            }
            if(date.getDayOfMonth() == date.with(TemporalAdjusters.lastDayOfMonth()).getDayOfMonth())
            {
                System.out.println("Prumerny vykon zvoleneho dnu za " + date.getMonth() + " je:" + monthlyDailyMW / countOfDays);
                monthlyDailyMW = 0;
                countOfDays = 0;
            }
        }

    }

    public static boolean timestampCheck(String timestamp)
    {
        DateTimeFormatter format = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmm");
        try {
            LocalDateTime date = LocalDateTime.parse(timestamp, format);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}