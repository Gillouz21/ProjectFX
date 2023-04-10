package sample;

import javafx.scene.layout.Pane;

import java.util.ArrayList;

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
        int player = model.getPlayer().getPlayerID();
        model.occupyRoute(player,index); //occupies the route and add points to the player
        if (model.isWin(player))
        {
            //end game}
        }
        else{
            iview.changeColor(index, model.getPlayerColor(player));
        }

        model.nextPlayer();


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
        model.nextPlayer();
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
        model.getPlayer().AddCard(index); //NEEDS to be changed so that will ony show first players card.
    }


}




