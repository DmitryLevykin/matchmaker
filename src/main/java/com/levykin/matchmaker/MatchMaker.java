package com.levykin.matchmaker;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Create users group with similar rank
 */
public class MatchMaker {

    private final Queue<UserRank> usersQueue = new ConcurrentLinkedQueue();

    private final static int TIME_CRITERION_MS = 5000;

    private int matchSize;

    private MatchHandler handler;

    private long currentTime;

    public MatchMaker(int matchSize, MatchHandler handler) {
        this.matchSize = matchSize;
        this.handler = handler;
    }

    protected long getWaitingTime(UserRank userRank) {
        return currentTime - userRank.enterTime;
    }

    protected boolean isMatched(UserRank userRank1, UserRank userRank2) {
        return Math.abs(userRank1.rank - userRank2.rank) * TIME_CRITERION_MS
                <= getWaitingTime(userRank1) + getWaitingTime(userRank2);
    }

    private boolean isMatched(List<UserRank> userRanks) {
        for (int i = 0; i < userRanks.size(); i++)
            for (int j = 0; j < userRanks.size(); j++)
                if (i != j && !isMatched(userRanks.get(i), userRanks.get(j)))
                    return false;

        return true;
    }

    public boolean make() {
        return make(System.currentTimeMillis());
    }

    public synchronized boolean make(long currentTime) {
        if (usersQueue.size() < matchSize)
            return false;

        this.currentTime = currentTime;
        List<UserRank> allUsers = new ArrayList<>(usersQueue);

        allUsers.sort(Comparator.comparingInt(o -> o.rank));

        // All matched users for user
        Map<UserRank, List<UserRank>> matchMap = new HashMap<>();

        for (int i = 0; i < allUsers.size(); i++) {
            List<UserRank> userList = new ArrayList<>(matchSize);
            UserRank userRankI = allUsers.get(i);
            matchMap.put(userRankI, userList);
            userList.add(userRankI);

            for (int j = 0; j < allUsers.size(); j++) {
                if (i == j)
                    continue;

                UserRank userRankJ = allUsers.get(j);

                if (isMatched(userRankI, userRankJ)) {
                    userList.add(userRankJ);
                    if (userList.size() == matchSize)
                        break;
                }
            }

            if (userList.size() < matchSize) // Skip too small matched list
                matchMap.remove(userRankI);
        }

        for (List<UserRank> userRanks : matchMap.values()) {
            if (!isMatched(userRanks))
                continue;

            // Make match
            usersQueue.removeAll(userRanks);
            handler.onMatchCreated(userRanks);
            make(currentTime);
            return true;
        }

        return false;
    }

    /**
     * Register user in queue
     */
    public void register(UserRank userRank) {
        usersQueue.add(userRank);
    }

    public int getQueueSize() {
        return usersQueue.size();
    }

    public List<UserRank> getQueueUsers() {
        return new ArrayList<>(usersQueue);
    }
}
