package sample;

import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;

public interface IView {
    void changeColor(int index, Color color);
    void showRouteCard(ArrayList<Route> r);
    void updateBotsMove(String s);
    int[] getOptionalWagonCards();
    void updateOptionalWagon(int pI, int key);
    void removeOptionalWagon(int paneIndex);
    void setOptionalWagonCards(int[] optionalWagonCards);
    void updatePointLabels();
    void initOptionalWagonCards();
    void showEndingScreen();
    void showLastTurn();
    void updateWagonsLeft();
    void checkIfEnd();

}
