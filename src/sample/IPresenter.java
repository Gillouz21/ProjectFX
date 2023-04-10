package sample;

import javafx.scene.layout.Pane;

import java.util.ArrayList;

public interface IPresenter {
    void pathClicked(int index);
    public void passPanes(ArrayList<Pane> panes);
    public Model getModel();
    ArrayList<Route> routeDeckClicked();
    String getCityName(int cityNum);
    void insertRouteToPlayerRouteList(Route index);

    void insertRouteQueue(ArrayList<Route> optionalRouteCards);

    int getWagonCard();

    void addWagonCard(int index);
}
