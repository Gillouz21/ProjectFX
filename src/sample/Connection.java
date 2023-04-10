package sample;

import javafx.scene.paint.Color;

import java.util.Arrays;

public class Connection {
    private int weight;
    private int numOfRoads;
    private int[] playerID;
    private Color[] color;
    private boolean complete;

    public Connection(int weight, int numOfRoads) {
        this.weight = weight;
        this.numOfRoads = numOfRoads;
        this.complete = false;

        this.color = new Color[numOfRoads];
        this.playerID = new int[numOfRoads];

        Arrays.fill(this.playerID, -1);
    }

    public int getWeight() {
        return weight;
    }

    public int getNumOfRoads() {
        return numOfRoads;
    }

    public int[] getPlayerID() {
        return playerID;
    }

    public Color[] getColor() {
        return color;
    }

    public void setColor(Color[] color) {
        this.color = color;
    }



    public boolean isComplete() {
        return complete;
    }

    //one road
    public void placeRoute(int playerID, Color color){
        this.playerID[0] = playerID;
        this.color[0] = color;
        this.complete = true;
    }

    //two roads
    public void placeRoute(int playerID, Color color, int r ) { //r = 0: road a, 1: road b //ASK ALON if needed
        if (r == 0){
            this.playerID[0] = playerID;
            this.color[0] = color;
            if (this.playerID[1] != -1)
                this.complete = true;
        }

        else{
            this.playerID[1] = playerID;
            this.color[1] = color;
            if (this.playerID[0] != -1)
                this.complete = true;
        }
















    }
}
