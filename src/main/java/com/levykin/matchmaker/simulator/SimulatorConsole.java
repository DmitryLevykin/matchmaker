package com.levykin.matchmaker.simulator;

import com.levykin.matchmaker.MatchMaker;
import com.levykin.matchmaker.UserRank;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class SimulatorConsole {

    private final static String WELCOME_MESSAGE = "Type user rank (1-30) or type command: 'exit'. Example 5.";

    private final DateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");

    private final static int MATCH_SIZE = 8;

    private final static int MAX_RATING = 30;

    private int usersCounter = 0;

    private final MatchMaker matchMaker;

    private SimulatorConsole() throws IOException {

        System.out.println("MatchMaker console");
        System.out.println(WELCOME_MESSAGE);

        matchMaker = new MatchMaker(MATCH_SIZE, userRanks -> {
            List<String> collect = userRanks.stream().map(t -> "user" + t.user).collect(Collectors.toList());
            System.out.println(dateFormat.format(new Date()) + " " + String.join(", ", collect));
        });

        try (BufferedReader bufferRead = new BufferedReader(new InputStreamReader(System.in))) {
            while (true) {
                if (bufferRead.ready()) {
                    String command = bufferRead.readLine();

                    if ("exit".equalsIgnoreCase(command))
                        break;

                    Integer rank = null;
                    try {
                        rank = Integer.valueOf(command);
                    } catch (NumberFormatException ignored) {
                    }
                    if (rank != null && rank > 0 && rank <= MAX_RATING) {
                        UserRank userRank = new UserRank();
                        userRank.enterTime = System.currentTimeMillis();
                        userRank.rank = rank;
                        userRank.user = ++usersCounter;
                        matchMaker.register(userRank);
                        System.out.println(String.format("Register user%d with rank %d", userRank.user, rank));
                    } else
                        System.out.println(WELCOME_MESSAGE);
                }
                if (!matchMaker.make())
                    Thread.sleep(20);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public static void main(String[] args) throws IOException {
        new SimulatorConsole();
    }
}
