package sample;

import java.util.ArrayList;

public class Route {

    private int city1;
    private int city2;
    private boolean complete;
    private int points;

    public Route(int city1, int city2, int points) {
        this.city1 = city1;
        this.city2 = city2;
        this.complete = false;
        this.points = points;
    }

    public void setComplete(boolean complete) {
        this.complete = complete;
    }

    public boolean isComplete() {
        return complete;
    }

    public int[] getCities() {
        int result[] ={city1, city2};
        return result;
    }

    public int getPoints() {
        return points;
    }

}


