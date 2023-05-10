package sample;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Arrays;

public class Player {
    private final  int playerID;
    private ArrayList<Route> routeList;
    private int cards[];
    private int points;
    private int numOfWagons ;


    public Player(int playerID) {
        this.playerID = playerID;
        this.routeList = new ArrayList<Route>();
        this.points = 0;
        this.numOfWagons = 35;
        this.cards = new int[9]; //{WHITE, BLUE, YELLOW, PURPLE, ORANGE, BLACK, RED, GREEN, JOKER}
        Arrays.fill(this.cards, 0);

    }

    public int getPlayerID() {
        return playerID;
    }


    public ArrayList<Route> getRouteList() {
        return routeList;
    }

    public int[] getCards() {
        return cards;
    }


    public int getPoints() {
        return points;
    }

    public void setPoints(int points) {
        this.points = points;
    }

    public void incPoints(int points){
        this.points+=points;

    }
    public void AddRouteList(Route r ){
        this.routeList.add(r);
    }

    public void AddCard(int card){
        this.cards[card]++;
    }

    public void removeCard(int card){
        this.cards[card]--;
    }

    public int getNumOfWagons() {
        return numOfWagons;
    }

    public void decreaseNumOfWagons(int n) {
        this.numOfWagons -= n;
    }

}
