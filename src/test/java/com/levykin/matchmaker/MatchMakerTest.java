package com.levykin.matchmaker;

import org.junit.Assert;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

public class MatchMakerTest {

    /**
     * Same rank, same enter time
     */
    @Test
    public void match1Test() {
        List<List<UserRank>> matches = new ArrayList<>();
        int matchSize = 3;

        MatchMaker maker = new MatchMaker(matchSize, matches::add);

        UserRank u1 = new UserRank() {{
            user = 1;
            rank = 1;
        }};

        UserRank u2 = new UserRank() {{
            user = 2;
            rank = 1;
        }};

        UserRank u3 = new UserRank() {{
            user = 3;
            rank = 1;
        }};

        maker.register(u1);
        maker.register(u2);

        maker.make(1000);

        Assert.assertEquals(0, matches.size());

        maker.register(u3);

        maker.make();

        Assert.assertEquals(1, matches.size());
        List<UserRank> match = matches.get(0);
        Assert.assertEquals(matchSize, match.size());
        Assert.assertTrue(match.contains(u1));
        Assert.assertTrue(match.contains(u2));
        Assert.assertTrue(match.contains(u3));
        Assert.assertEquals(0, maker.getQueueSize());
    }

    /**
     * Different rank, different enter time
     */
    @Test
    public void match2Test() {
        List<List<UserRank>> matches = new ArrayList<>();
        int matchSize = 3;

        MatchMaker maker = new MatchMaker(matchSize, matches::add);

        UserRank u1 = new UserRank() {{
            user = 1;
            rank = 1;
        }};

        UserRank u2 = new UserRank() {{
            user = 2;
            rank = 2;
        }};

        UserRank u3 = new UserRank() {{
            user = 3;
            rank = 3;
        }};

        maker.register(u1);
        maker.register(u2);
        maker.register(u3);

        maker.make(5000);

        Assert.assertEquals(1, matches.size());
        List<UserRank> match = matches.get(0);
        Assert.assertEquals(matchSize, match.size());
        Assert.assertTrue(match.contains(u1));
        Assert.assertTrue(match.contains(u2));
        Assert.assertTrue(match.contains(u3));
        Assert.assertEquals(0, maker.getQueueSize());
    }

    /**
     * Multiple matches
     */
    @Test
    public void match3Test() {
        List<List<UserRank>> matches = new ArrayList<>();
        int matchSize = 3;

        MatchMaker maker = new MatchMaker(matchSize, matches::add);

        UserRank u1 = new UserRank() {{
            user = 1;
            rank = 1;
        }};

        UserRank u2 = new UserRank() {{
            user = 2;
            rank = 1;
        }};

        UserRank u3 = new UserRank() {{
            user = 3;
            rank = 1;
        }};

        UserRank u4 = new UserRank() {{
            user = 4;
            rank = 30;
        }};

        UserRank u5 = new UserRank() {{
            user = 5;
            rank = 30;
        }};

        UserRank u6 = new UserRank() {{
            user = 5;
            rank = 30;
        }};

        UserRank u7 = new UserRank() {{
            user = 7;
            rank = 30;
        }};

        // Shuffle
        maker.register(u4);
        maker.register(u7);
        maker.register(u1);
        maker.register(u6);
        maker.register(u2);
        maker.register(u5);
        maker.register(u3);

        maker.make(0);

        Assert.assertEquals(2, matches.size());
        List<UserRank> match1 = matches.get(0);
        Assert.assertEquals(matchSize, match1.size());
        Assert.assertTrue(match1.get(0).rank == match1.get(1).rank);
        Assert.assertTrue(match1.get(1).rank == match1.get(2).rank);

        List<UserRank> match2 = matches.get(1);
        Assert.assertEquals(matchSize, match2.size());
        Assert.assertTrue(match2.get(0).rank == match2.get(1).rank);
        Assert.assertTrue(match2.get(1).rank == match2.get(2).rank);

        Assert.assertEquals(1, maker.getQueueSize());
    }

    @Test
    public void smallRateTest() throws IOException {
        Stat stat = getStat("users-queue-1K-5h.txt");
        Assert.assertTrue(stat.avgRankRange < 20);
        Assert.assertTrue(stat.avgWaiting < 55500);
    }

    @Test
    public void mediumRateTest() throws IOException {
        Stat stat = getStat("users-queue-10K-5h.txt");
        Assert.assertTrue(stat.avgRankRange < 7);
        Assert.assertTrue(stat.avgWaiting < 15500);
    }

    @Test
    public void bigRateTest() throws IOException {
        Stat stat = getStat("users-queue-100K-5h.txt");
        Assert.assertTrue(stat.avgRankRange < 2);
        Assert.assertTrue(stat.avgWaiting < 5000);
    }

    private static class Stat {
        double avgRankRange;
        int matches;
        long avgWaiting;
    }

    private Stat getStat(String fileName) throws IOException {
        int matchSize = 8;
        int[] matchCounter = new int[1];
        long[] currentTime = new long[1];
        long[] rankRangeSum = new long[1];
        List<Long> avgWaiting = new ArrayList<>(10000);

        MatchMaker maker = new MatchMaker(matchSize, usersRank -> {
            matchCounter[0]++;
            int minRank = MatchMaker.MAX_RANK;
            int maxRank = 1;

            long waitingSum = 0;
            for (UserRank userRank : usersRank) {
                if (userRank.rank > maxRank)
                    maxRank = userRank.rank;
                if (userRank.rank < minRank)
                    minRank = userRank.rank;
                waitingSum += currentTime[0] - userRank.enterTime;
            }
            avgWaiting.add(waitingSum / matchSize);
            rankRangeSum[0] += maxRank - minRank;
        });

        int users = simulateFromFile(maker, fileName, currentTime);

        Stat stat = new Stat();
        stat.matches = matchCounter[0];
        stat.avgRankRange = (double) rankRangeSum[0] / matchCounter[0];
        stat.avgWaiting = avgWaiting.stream().mapToLong(Long::longValue).sum() / matchCounter[0];

        DecimalFormat decimalFormat = new DecimalFormat(".##");
        System.out.println("Users: " + users);
        System.out.println("Matches: " + stat.matches);
        System.out.println("Average users per minute: " + decimalFormat.format(users / 300.0));
        System.out.println("Average rating range: " + decimalFormat.format(stat.avgRankRange));
        System.out.println("Average waiting: " + stat.avgWaiting);

        return stat;
    }

    private int simulateFromFile(MatchMaker maker, String fileName, long[] currentTime) throws IOException {
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(MatchMakerTest.class.getResourceAsStream(fileName)))) {
            int counter = 0;
            while (reader.ready()) {
                String row = reader.readLine();
                String[] split = row.split(":");
                UserRank userRank = new UserRank();
                userRank.user = ++counter;
                userRank.enterTime = Integer.parseInt(split[0]) * 1000;
                userRank.rank = Integer.parseInt(split[1]);
                maker.register(userRank);
                currentTime[0] = userRank.enterTime;
                maker.make(userRank.enterTime);
            }
            return counter;
        }
    }
}
