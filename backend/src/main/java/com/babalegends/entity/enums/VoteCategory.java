package com.babalegends.entity.enums;

public enum VoteCategory {
    OVERALL,
    PACE,
    SHOOTING,
    PASSING,
    DRIBBLING,
    DEFENDING,
    PHYSICALITY;

    public boolean isAttribute() {
        return this != OVERALL;
    }
}
