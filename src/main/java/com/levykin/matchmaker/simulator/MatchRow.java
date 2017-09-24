package com.levykin.matchmaker.simulator;

public class MatchRow {
    private Integer number;
    private String time;
    private String users;
    private Integer range;
    private String maxWaiting;
    private String averageWaiting;

    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getUsers() {
        return users;
    }

    public void setUsers(String users) {
        this.users = users;
    }

    public Integer getRange() {
        return range;
    }

    public void setRange(Integer range) {
        this.range = range;
    }

    public String getMaxWaiting() {
        return maxWaiting;
    }

    public void setMaxWaiting(String maxWaiting) {
        this.maxWaiting = maxWaiting;
    }

    public String getAverageWaiting() {
        return averageWaiting;
    }

    public void setAverageWaiting(String averageWaiting) {
        this.averageWaiting = averageWaiting;
    }
}
