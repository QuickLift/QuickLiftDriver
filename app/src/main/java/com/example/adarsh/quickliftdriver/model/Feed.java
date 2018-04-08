package com.example.adarsh.quickliftdriver.model;

/**
 * Created by amit on 1/4/18.
 */

public class Feed {
    int rejectedRideCount;
    int canceledRidesCount;
    int bookedRideCount;
    int totalEarning;

    public int getRejectedRideCount() {
        return rejectedRideCount;
    }

    public void setRejectedRideCount(int rejectedRideCount) {
        this.rejectedRideCount = rejectedRideCount;
    }

    public int getCanceledRidesCount() {
        return canceledRidesCount;
    }

    public void setCanceledRidesCount(int canceledRidesCount) {
        this.canceledRidesCount = canceledRidesCount;
    }

    public int getBookedRideCount() {
        return bookedRideCount;
    }

    public void setBookedRideCount(int bookedRideCount) {
        this.bookedRideCount = bookedRideCount;
    }

    public int getTotalEarning() {
        return totalEarning;
    }

    public void setTotalEarning(int totalEarning) {
        this.totalEarning = totalEarning;
    }
}
