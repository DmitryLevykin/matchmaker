package com.levykin.matchmaker.simulator;

import com.levykin.matchmaker.MatchMaker;
import com.levykin.matchmaker.UserRank;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * MathMaker GUI simulator
 */
public class SimulatorGUI extends Application {

    final static int MATCH_SIZE = 8;

    private final DateFormat smallDateFormat = new SimpleDateFormat("mm:ss.SSS");
    private final DateFormat bigDateFormat = new SimpleDateFormat("HH:mm:ss");

    private Thread makerThread;
    private MatchMaker matchMaker;
    private Long lastAdded;
    private SimulatorGUIController controller;

    @Override
    public void start(Stage stage) throws Exception {

        FXMLLoader loader = new FXMLLoader(SimulatorGUI.class.getResource("SimulatorGUI.fxml"));
        Parent root = loader.load();
        controller = loader.getController();
        controller.setResetAction(() -> {
            if (makerThread != null)
                makerThread.interrupt();
            start();
        });

        stage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        stage.setTitle("MatchMaker Simulator");
        stage.setScene(new Scene(root, 800, 800));
        stage.show();

        start();
    }

    private void start() {
        lastAdded = null;
        controller.resetState();

        makerThread = new Thread(() -> {
            long start = System.currentTimeMillis();

            AtomicInteger usersCounter = new AtomicInteger();
            AtomicInteger matchesCounter = new AtomicInteger();

            try {
                matchMaker = new MatchMaker(MATCH_SIZE, usersRank -> addMatchTableRow(matchesCounter, usersRank));

                while (!Thread.currentThread().isInterrupted()) {
                    // Update counters and histogram
                    Platform.runLater(() -> {
                        long uptime = System.currentTimeMillis() - start - TimeZone.getDefault().getRawOffset();
                        List<UserRank> queueUsers = matchMaker.getQueueUsers();
                        int[] ratingByUsers = new int[MatchMaker.MAX_RANK];
                        for (UserRank userRank : queueUsers)
                            ratingByUsers[userRank.rank - 1]++;
                        controller.updateState(bigDateFormat.format(new Date(uptime)),
                                String.valueOf(matchMaker.getQueueSize()), ratingByUsers);
                    });

                    // Register random user
                    int rate = controller.spinner.getValue();
                    if (rate != 0 && (lastAdded == null || System.currentTimeMillis() - lastAdded > 60000 / rate)) {
                        lastAdded = System.currentTimeMillis();
                        UserRank userRank = new UserRank();
                        userRank.user = usersCounter.incrementAndGet();
                        userRank.enterTime = System.currentTimeMillis();
                        userRank.rank = ThreadLocalRandom.current().nextInt(1, MatchMaker.MAX_RANK + 1);
                        matchMaker.register(userRank);
                    }

                    if (!matchMaker.make())
                        Thread.sleep(1000);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        makerThread.start();
    }

    private void addMatchTableRow(AtomicInteger matchesCounter, List<UserRank> usersRank) {
        long currentTime = System.currentTimeMillis();
        usersRank.sort(Comparator.comparingLong(o -> o.enterTime));
        List<String> users = new ArrayList<>(MATCH_SIZE);
        int minRank = MatchMaker.MAX_RANK;
        int maxRank = 1;
        long minEnter = currentTime;
        int sumWait = 0;
        for (UserRank userRank : usersRank) {
            long waiting = currentTime - userRank.enterTime - TimeZone.getDefault().getRawOffset();
            users.add(String.format("User%d rank %d, waiting %s",
                    userRank.user,
                    userRank.rank,
                    smallDateFormat.format(waiting)));
            if (userRank.rank > maxRank)
                maxRank = userRank.rank;
            if (userRank.rank < minRank)
                minRank = userRank.rank;
            if (userRank.enterTime < minEnter)
                minEnter = userRank.enterTime;
            sumWait += currentTime - userRank.enterTime;
        }

        long maxWaiting = currentTime - minEnter - TimeZone.getDefault().getRawOffset();
        int avgWaiting = sumWait / MATCH_SIZE - TimeZone.getDefault().getRawOffset();

        MatchRow row = new MatchRow();
        row.setNumber(matchesCounter.incrementAndGet());
        row.setTime(bigDateFormat.format(new Date()));
        row.setUsers(String.join("\n", users));
        row.setRange(maxRank - minRank);
        row.setMaxWaiting(smallDateFormat.format(maxWaiting));
        row.setAverageWaiting(smallDateFormat.format(avgWaiting));

        Platform.runLater(() -> {
            controller.table.getItems().add(row);
            controller.matchesL.setText(matchesCounter.toString());
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
