package sample;

import javafx.scene.layout.Pane;

import javax.management.ObjectName;
import java.util.*;

public class Presenter implements IPresenter {
    private IView iview;
    private Model model;


    public Presenter(IView iview){
        this.iview = iview;
        this.model = new Model();

    }

    @Override
    public ArrayList<Route> routeDeckClicked() {
        ArrayList<Route> r = model.getRouteCards();
        iview.showRouteCard(r);
        return r;
    }

    //FIRST OPTION OF USER TURN
    public void pathClicked(int index){

        model.occupyPath(0, index); //occupies the path and add points to the player
        iview.changeColor(index, model.getPlayerColor(0));

        for (Route route: model.getPlayer().getRouteList()) {
            if (!route.isComplete() && model.isRouteCompleted(route)){
                route.setComplete(true);
                model.getPlayer().incPoints(route.getPoints());
            }
        }
        if (model.isWin(0)) {
            //end game}
        }

    }

    public void passPanes(ArrayList<Pane> panes){
        model.initPossibleConnections(panes);

    }

    public Model getModel() {
        return this.model;
    }

    public String getCityName(int cityNum){
        return model.getCityName(cityNum);
    }

    @Override
    public void insertRouteToPlayerRouteList(Route r) {
        model.insertRouteToPlayer(r);
    }

    @Override
    public void insertRouteQueue(ArrayList<Route> optionalRouteCards) {
        model.insertQueue(optionalRouteCards);
    }

    @Override
    public int getWagonCard() {
        return model.wagonStack.pop();
    }

    @Override
    public void addWagonCard(int index) {
        model.getPLAYER_ARR()[0].AddCard(index);
    }

    @Override
    public void botMove() {
        ArrayList<Route> routes = model.getPLAYER_ARR()[0].getRouteList();
        Map<List<List<Integer>>, Integer> bestRoutes =  model.pickRoutes(routes);


        model.nextPlayer();

    }




    @Override
    public void useWagonCard(int index) {
        model.getPlayer().removeCard(index);
        model.usedWagonStack.push(index);
    }



}




