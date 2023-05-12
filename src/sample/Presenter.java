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
    public String pathClicked(int index){
        String str= "";
        model.occupyPath(model.getPlayer().getPlayerID(), index); //occupies the path and add points to the player
        iview.changeColor(index, model.getPlayerColor(model.getPlayer().getPlayerID()));

        Iterator<Route> iterator = model.getPlayer().getRouteList().iterator();
        while (iterator.hasNext()) {
            Route route = iterator.next();
            if (model.isRouteCompleted(route)) {
                if(model.getPlayer().getPlayerID() == 1)
                    str = str + String.format("The Bot has completed a path between %s and %s\n", model.cityMap.get(route.getCities()[0]), model.cityMap.get(route.getCities()[1]) );
                model.getPlayer().incPoints(route.getPoints());
                iterator.remove();
            }
        }


        if (model.isLastTurn(model.getPlayer().getPlayerID())) {
            iview.showLastTurn();
            model.setLastTurnCounter();
        }


        return str;

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
        model.getPlayer().AddCard(index);
    }
//FOR TESTING
//    public void botMove(){
//        Route r1  =new Route(0,6, 12);
//        ArrayList<Route> optionalRoutes = new ArrayList<>();
//        optionalRoutes.add(r1);
//        List<List<Integer>> chosenPath = bestPath(optionalRoutes);
//        model.nextPlayer();
//    }
//

    @Override
    public void botMove() {
        String moveMade = "";
        if (model.getPlayer().getRouteList().isEmpty() && !model.lastTurn) // if the bot doesn't have destination cards
        {
            pickDestinationCards();
            moveMade = "The Bot has picked Destination Cards";
        }
        else {
            //pick best Route
            boolean pickedDestinationCard = false;
            List<List<Integer>> chosenPath = bestPath(model.getPlayer().getRouteList()); //could be null
            List<List<Integer>> possibleConnections = null;
            if (chosenPath == null){
                if(!model.isEndGame() ){
                    pickDestinationCards();
                    pickedDestinationCard = true;
                    moveMade = "The Bot has picked Destination Cards";
                }
                else{
                    //complete as many paths possible
                    possibleConnections = model.getAllFreeConnection();
                }
            }
            else {
                //check if the bot has enough cards to claim the connection
                possibleConnections = getOccupyableConnections(chosenPath);

            }

            if(!pickedDestinationCard) {
                int connectionColorCode;
                if (possibleConnections != null && possibleConnections.size() == 0) {
                    List<Integer> bestConnection = model.pickBestConnection(chosenPath);
                    int i = bestConnection.get(0);
                    int j = bestConnection.get(1);
                    connectionColorCode = model.getPathColorCode(model.getIndexByIAndJ(i, j));


                    int[] optionalOpenCards = iview.getOptionalWagonCards();
                    pickCards(connectionColorCode, optionalOpenCards);       // can be improved by considering multiple connections of different colors

                    //checks if needs shuffle
                    if (model.wagonStack.isEmpty() && model.cannotCompleteTurn(iview.getOptionalWagonCards())) {
                        model.insertUsedToWagonStack();
                        iview.initOptionalWagonCards();
                    }

                    moveMade = "The Bot has Picked Wagon Cards";
                } else {
                    List<Integer> bestConnection = model.pickBestConnection(possibleConnections);
                    int i = bestConnection.get(0);
                    int j = bestConnection.get(1);
                    int weight = model.getPathWeight(model.getIndexByIAndJ(i, j));

                    if (model.board[i][j].getNumOfRoads() == 2)
                        connectionColorCode = model.colorsAndNumbers.get(model.get2PathColor(i,j));
                    else
                        connectionColorCode = model.getPathColorCode(model.getIndexByIAndJ(i, j));
                    while (weight > 0) {
                        if (model.getPlayer().getCards()[connectionColorCode] > 0) //cards of the same color
                            model.getPlayer().getCards()[connectionColorCode]--;
                        else
                            model.getPlayer().getCards()[8]--;  // joker cards
                        weight--;
                    }
                    if (model.board[i][j].getNumOfRoads() == 2) {
                        int index = model.getIndexByIAndJ(i, j, model.get2PathColor(i, j));
                        //CONTINUE FROM HERE
                        //pass the correct index to the path clicked somehow
                        // pathClicked(model.getIndexByIAndJ(i, j, model.get2PathColor(i,j)));
                        }
                    else
                        pathClicked(model.getIndexByIAndJ(i, j));

                    moveMade = "The Bot has claimed a path";
                    iview.updateWagonsLeft();
                    if(getModel().lastTurn) {
                        getModel().lastTurnCounter--;
                        if (model.lastTurnCounter == 0)
                            iview.showEndingScreen();
                    }

                }

            }

        }

        iview.updateBotsMove(moveMade);
        iview.updatePointLabels();
        model.nextPlayer();

    }

    private void pickDestinationCards() { // ERRORS
        // pick destination cards
        ArrayList<Route> optionalRoutes = model.getRouteCards();
        List<List<Integer>> chosenPath = bestPath(optionalRoutes);

        if (chosenPath == null){ //if cannot complete any route pick the one with the minimum points;
            Route minRoute = null;
            int minPoints = Integer.MAX_VALUE;

            for (Route route : optionalRoutes) {
                int points = route.getPoints();
                if (points < minPoints) {
                    minPoints = points;
                    minRoute = route;
                }
            }
            insertRouteToPlayerRouteList(minRoute);
            optionalRoutes.remove(minRoute);

        }
        else{
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
        }

        if (model.firstTurn) {
            while (model.getPlayer().getRouteList().size() < 2) {
                Route minRoute = null;
                int minPoints = Integer.MAX_VALUE;
                Iterator<Route> iterator = optionalRoutes.iterator();

                while (iterator.hasNext()) {
                    Route route = iterator.next();
                    int points = route.getPoints();
                    if (points < minPoints) {
                        minPoints = points;
                        minRoute = route;
                    }
                }

                insertRouteToPlayerRouteList(minRoute);
                iterator.remove();
            }
            model.firstTurn = false;
        }


        insertRouteQueue(optionalRoutes);   //insert the rest of the destination cards back to the stack
    }

    private List<List<Integer>> getOccupyableConnections(List<List<Integer>> chosenPath) {
        List<List<Integer>> possibleConnections = new ArrayList<>();
        for (List<Integer> connection:
                chosenPath
             ) {
            int i = connection.get(0);
            int j = connection.get(1);
            int index = model.getIndexByIAndJ(i,j);
            if (model.canOccupyPath(index) && !model.isConnectionExists(i,j,model.getPlayer().getPlayerID()) && !model.isTaken(i,j,model.getPlayer().getPlayerID())){
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
            routeCombos2Paths.put(set, model.pickRoutes((ArrayList<Route>) set, model.getPlayer().getNumOfWagons())); //THERES AN ERROR HERE
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
        if (pathsScores.size() == 0)
            return null;
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


    public void pickCards(int connectionColorCode, int[] optionalOpenCards) { //need to continue, updating the cardPanes NNED TO CHECK IF THE PICKS THAT ARE MADE CHANGE THE MAIN OPTIONAL CARDS AS WELL
        int chosenCards = 0;
        int i = 0;

        if (connectionColorCode == 8){
            for (int j = 0; j < 5; j++) {
                if (optionalOpenCards[j] == 8){
                    addWagonCard(8);
                    if (!model.wagonStack.isEmpty())
                        iview.updatePossibleWagon(j, getWagonCard());
                    else {
                        iview.removePossibleWagon(j);
                        optionalOpenCards[j] = -1;
                    }
                    return;
                }
            }
        }



        while(i < 5 && chosenCards < 2 ) {
            if (optionalOpenCards[i] == connectionColorCode && chosenCards < 2){
                addWagonCard(optionalOpenCards[i]);
                if (!model.wagonStack.isEmpty())
                    iview.updatePossibleWagon(i, getWagonCard());
                else {
                    iview.removePossibleWagon(i);
                    optionalOpenCards[i] = -1;
                }
                chosenCards++;
                i--;
            }
            i++;
        }

        if (chosenCards == 1){
            if (!model.wagonStack.isEmpty()) {
                addWagonCard(getWagonCard());
                chosenCards++;
            }
            else{
                for (int j = 0; j < 5 ; j++) {
                    if (optionalOpenCards[j] != 8 && optionalOpenCards[j] != -1 && chosenCards< 2) {
                        addWagonCard(optionalOpenCards[j]);
                        iview.removePossibleWagon(j);
                        optionalOpenCards[j] = -1;
                        chosenCards++;
                    }
                }
            }
        }

        if (chosenCards == 0){
            for (int j = 0; j < 5 ; j++) {
                if (optionalOpenCards[j] == 8 && chosenCards < 2) {
                    addWagonCard(optionalOpenCards[j]);
                    if (!model.wagonStack.isEmpty())
                        iview.updatePossibleWagon(j, getWagonCard());
                    else {
                        iview.removePossibleWagon(j);
                        optionalOpenCards[j] = -1;
                    }
                    chosenCards +=2;
                }
            }


            while (!model.wagonStack.isEmpty() && chosenCards < 2) {
                addWagonCard(getWagonCard());
                chosenCards++;
            }

            if(chosenCards == 1){
                for (int j = 0; j < 5 ; j++) {
                    if ( optionalOpenCards[j] != -1 && chosenCards < 2) {
                        addWagonCard(optionalOpenCards[j]);
                        iview.removePossibleWagon(j);
                        optionalOpenCards[j] = -1;
                        chosenCards++;
                    }
                }
            }

            else if (chosenCards == 0){
                i = 0;
                while(i < 5 && chosenCards < 2 ) {
                    if (optionalOpenCards[i] != -1 && chosenCards < 2) {
                        addWagonCard(optionalOpenCards[i]);
                        iview.removePossibleWagon(i);
                        optionalOpenCards[i] = -1;
                        chosenCards++;
                        i--;
                    }
                    i++;
                }
            }
        }

        iview.setOptionalWagonCards(optionalOpenCards);

    }
}




