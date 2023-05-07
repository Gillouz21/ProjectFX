package sample;

import javafx.scene.layout.Pane;

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
        String moveMade = "";
        if (model.getPlayer().getRouteList().isEmpty()) // if the bot doesn't have destination cards
        {
            // pick destination cards
            ArrayList<Route> optionalRoutes = model.getRouteCards();
            List<List<Integer>> chosenPath = bestPath(optionalRoutes);

            List<Integer> cityList = pathToListOfVertices(chosenPath);

            //iterate over the possible destination cards and insert them if the bot has picked them
            Iterator<Route> iter = optionalRoutes.iterator();
            while (iter.hasNext()) {
                Route r = iter.next();
                if (cityList.contains(r.getCities()[0]) && cityList.contains(r.getCities()[1])) {
                    insertRouteToPlayerRouteList(r);
                    iter.remove();
                }
            }

            insertRouteQueue(optionalRoutes);   //insert the rest of the destination cards back to the stack
            moveMade = "The Bot has picked Destination Cards";
        }
        else {
            //pick best Route
            List<List<Integer>> chosenPath = bestPath(model.getPlayer().getRouteList()); //just for debugging im using the users cards

            //check if the bot has enough cards to claim the connection
            List<List<Integer>> possibleConnections = getOccupyableConnections(chosenPath);

            if (possibleConnections == null){
                List<Integer> bestConnection = model.pickWagonCards(chosenPath);


            }
            else{
                List<Integer> bestConnection = model.pickWagonCards(possibleConnections);
                int i = bestConnection.get(0);
                int j = bestConnection.get(1);
                int weight = model.getPathWeight(model.getIndexByIandJ(i,j));
                int connectionColorCode = model.getPathColorCode(model.getIndexByIandJ(i,j));
                while (weight > 0 ){
                    if (model.getPlayer().getCards()[connectionColorCode] > 0) //cards of the same color
                        model.getPlayer().getCards()[connectionColorCode]--;
                    else
                        model.getPlayer().getCards()[8]--;  // joker cards
                    weight--;
                }
                model.occupyPath(model.getPlayer().getPlayerID(),model.getIndexByIandJ(i, j));
                iview.changeColor(model.getIndexByIandJ(i, j), model.getPlayerColor(model.getPlayer().getPlayerID()));
                moveMade = "The Bot has claimed a path";
            }

        }
        iview.updateBotsMove(moveMade);
        model.nextPlayer();

    }

    private List<List<Integer>> getOccupyableConnections(List<List<Integer>> chosenPath) {
        List<List<Integer>> possibleConnections = new ArrayList<>();
        for (List<Integer> connection:
                chosenPath
             ) {
            int i = connection.get(0);
            int j = connection.get(1);
            int index = model.getIndexByIandJ(i,j);
            if (model.canOccupyPath(index)){
                possibleConnections.add(connection);
            }
        }
        return possibleConnections;
    }

    private List<Integer> pathToListOfVertices(List<List<Integer>> chosenPath) {
        List<Integer> vertices = new ArrayList<>();
        for (List<Integer> connection: chosenPath
             ) {
            if (!vertices.contains(connection.get(0)))
                vertices.add(connection.get(0));

            if (!vertices.contains(connection.get(1)))
                vertices.add(connection.get(1));
        }
        return vertices;
    }

    private List<List<Integer>> bestPath(ArrayList<Route> routeList) {
        List<List<Route>> routesSet = getAllRouteCombinations(routeList);
        Map<List<Route>, Map<List<List<Integer>>, Integer>> routeCombos2Paths = new HashMap<>();
        Map<List<List<Integer>>, Float> pathsScores = new HashMap<>();
        List<List<Integer>> chosenPath;

        //get every path for every combo
        for (List<Route> set: routesSet
        ) {
            routeCombos2Paths.put(set, model.pickRoutes((ArrayList<Route>) set, model.getPlayer().getNumOfWagons()));
            if (routeCombos2Paths.get(set).size() == 0)
                routeCombos2Paths.remove(set);
        }


        for (List<Route> set : routeCombos2Paths.keySet()) {
            int cardsCompleted = set.size();        //how many cards is this path completing?
            int cardsValue = getRoutesValue(set);       //how many points is this path worth?
            int pathWeight;
            int pathConnections;
            Map<List<List<Integer>>, Integer>  paths = routeCombos2Paths.get(set);      //all of the paths corresponding to the set of routes

            //for every path calculate its score
            for (List<List<Integer>> path: paths.keySet()
            ) {
                pathWeight = paths.get(path);
                pathConnections = path.size();

                pathsScores.put(path, model.calculateScore(cardsCompleted,cardsValue,pathWeight, pathConnections));
            }
        }

        return Collections.max(pathsScores.entrySet(),Map.Entry.comparingByValue()).getKey(); // pick the path with the max score
    }

    private int getRoutesValue(List<Route> set) {
        int sum = 0;
        for (Route r: set
             ) {
            sum += r.getPoints();
        }
        return sum;
    }

    private List<Integer> getNumOfRoutesCompletable(List<List<Route>> routesSet) {
        List<Integer> numOfRoutesCompleted = new ArrayList<>();
        for (List<Route> set: routesSet
        ) {
            numOfRoutesCompleted.add(set.size());
        }
        return numOfRoutesCompleted;
    }

    public static List<List<Route>> getAllRouteCombinations(List<Route> routes) {
        List<List<Route>> result = new ArrayList<>();
        List<List<Route>> finalResult = new ArrayList<>();
        result.add(new ArrayList<>()); // add empty set
        for (Route r : routes) {
            List<List<Route>> temp = new ArrayList<>();
            for (List<Route> l : result) {
                List<Route> newSet = new ArrayList<>(l);
                newSet.add(r);
                temp.add(newSet);
            }
            result.addAll(temp);
        }


        for (List<Route> set : result){
            if(set.size() > 1) {
                finalResult.addAll(getAllSetCombinations(set));
            }
            else if(set.size() != 0)
                finalResult.add(set);
        }

        //sort them by number of route cards
        Collections.sort(finalResult, new Comparator<List<Route>>() {
            @Override
            public int compare(List<Route> list1, List<Route> list2) {
                return Integer.compare(list1.size(), list2.size());
            }
        });

        return finalResult;
    }

    private static List<List<Route>> getAllSetCombinations(List<Route> set) {
        if(set.size() == 1){
            List<List<Route>> routeSet = new ArrayList<>(Collections.singletonList(set));
            return routeSet;
        }

        List<List<Route>> routeSet = new ArrayList<>();
        for (int i = 0; i < set.size(); i++) {
            Route current = set.get(i);
            List<Route> remaining = new ArrayList<>();
            remaining.addAll(set.subList(0, i));
            remaining.addAll(set.subList(i+1, set.size())); // check if works

            List<List<Route>> sub_combination = getAllSetCombinations(remaining);
            for (List<Route> set_combo : sub_combination
                 ) {
                List<Route> combined = new ArrayList<>();
                combined.addAll(Collections.singletonList(current));
                combined.addAll(set_combo);
                routeSet.add(combined);
            }
        }
        return routeSet;
    }

    public void useWagonCard(int index) {
        model.getPlayer().removeCard(index);
        model.usedWagonStack.push(index);
    }




}




