package com.levykin.matchmaker;

import java.util.List;

public interface MatchHandler {
    void onMatchCreated(List<UserRank> usersRank);
}
