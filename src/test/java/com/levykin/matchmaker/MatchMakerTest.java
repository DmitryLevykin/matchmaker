package com.levykin.matchmaker;

import org.junit.Assert;
import org.junit.Test;

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
}
