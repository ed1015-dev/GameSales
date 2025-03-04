package com.gamesales;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

@SpringBootApplication
@EnableScheduling
public class GameSalesApplication {

    public static void main(String[] args) {
        SpringApplication.run(GameSalesApplication.class, args);
        csvGenerate();
    }

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final BigDecimal TAX_RATE = new BigDecimal("0.09");


    public static void csvGenerate () {
        String filePath = "game_sales_data.csv";
        int numRecords = 1_000_000;

        try (FileWriter writer = new FileWriter(filePath)) {
            // Write header
            writer.append("id,game_no,game_name,game_code,type,cost_price,tax,sale_price,date_of_sale\n");

            // Generate and write records
            for (int i = 1; i <= numRecords; i++) {
                // Generate random data according to task requirements
                int gameNo = ThreadLocalRandom.current().nextInt(1, 101);
                String gameName = "Game" + gameNo + "-" + ThreadLocalRandom.current().nextInt(1000);

                String gameCode = "G" + gameNo;
                if (gameCode.length() > 5) {
                    gameCode = gameCode.substring(0, 5);
                }

                int type = ThreadLocalRandom.current().nextInt(1, 3); // 1 or 2

                // Cost price up to 100
                BigDecimal costPrice = BigDecimal.valueOf(ThreadLocalRandom.current().nextDouble(1, 100))
                        .setScale(2, RoundingMode.HALF_UP);

                // Tax at 9%
                BigDecimal tax = costPrice.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);

                // Sale price is cost price + tax
                BigDecimal salePrice = costPrice.add(tax).setScale(2, RoundingMode.HALF_UP);

                // Random date between April 1 and April 30, 2024
                int day = ThreadLocalRandom.current().nextInt(1, 31);
                int hour = ThreadLocalRandom.current().nextInt(0, 24);
                int minute = ThreadLocalRandom.current().nextInt(0, 60);
                int second = ThreadLocalRandom.current().nextInt(0, 60);

                LocalDateTime dateOfSale = LocalDateTime.of(2024, 4, day, hour, minute, second);
                String formattedDate = dateOfSale.format(DATE_FORMATTER);

                // Build CSV line
                String line = i + "," +
                        gameNo + "," +
                        gameName + "," +
                        gameCode + "," +
                        type + "," +
                        costPrice + "," +
                        tax + "," +
                        salePrice + "," +
                        formattedDate + "\n";

                writer.append(line);

                // Progress reporting
                if (i % 100000 == 0) {
                    System.out.println("Generated " + i + " records");
                }
            }

            System.out.println("CSV file generated successfully: " + filePath);

        } catch (IOException e) {
            System.err.println("Error generating CSV file: " + e.getMessage());
        }
    }
}