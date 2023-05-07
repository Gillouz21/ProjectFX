package sample;

import javafx.scene.paint.Color;

import java.util.ArrayList;

public interface IView {
    void changeColor(int index, Color color);
    void showRouteCard(ArrayList<Route> r);
    void updateBotsMove(String s);
    int[] getOptionalWagonCards();
    void updatePossibleWagon(int pI, int key);
    void removePossibleWagon(int paneIndex);
    }
