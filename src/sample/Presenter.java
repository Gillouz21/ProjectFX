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

    //if the route deck clicked, get route cards and return them
    @Override
    public ArrayList<Route> routeDeckClicked() {
        ArrayList<Route> r = model.getRouteCards();
        iview.showRouteCard(r);
        return r;
    }

    //path clicked on the board / bot picked a path
    public String pathClicked(int index){
        String str= "";
        model.occupyPath(model.getPlayer().getPlayerID(), index); //occupies the path and add points to the player
        iview.changeColor(index, model.getPlayerColor(model.getPlayer().getPlayerID()));

        //iterate over the route cards and check what route cards have been completed
        Iterator<Route> iterator = model.getPlayer().getRouteList().iterator();
        while (iterator.hasNext()) {
            Route route = iterator.next();
            if (model.isRouteCompleted(route)) {
                if(model.getPlayer().getPlayerID() == 1) //if bot's turn, promp what route cards has he has completed
                    str = str + String.format("The Bot has completed a path between %s and %s\n", getCityName(route.getCities()[0]), getCityName(route.getCities()[1]) );
                model.getPlayer().incPoints(route.getPoints());
                iterator.remove();
            }
        }

        //if is last turns
        if (model.isLastTurn(model.getPlayer().getPlayerID())) {
            iview.showLastTurn();
            model.setLastTurnCounter();
        }
        return str;
    }

    //passing the panes to the model for initializing purposes
    public void passPanes(ArrayList<Pane> panes){
        model.initPossibleConnections(panes);
    }

    public Model getModel() {
        return this.model;
    }

    //getting citiy name by code
    public String getCityName(int cityNum){
        return model.getCityName(cityNum);
    }

    //inserting route card to the route card list of the current player
    @Override
    public void insertRouteToPlayerRouteList(Route r) {
        model.insertRouteToPlayer(r);
    }

    //inserting the unpicked route cards to the route cards queue
    @Override
    public void insertRouteQueue(ArrayList<Route> optionalRouteCards) {
        model.insertQueue(optionalRouteCards);
    }

    //getting a wagon card
    @Override
    public int getWagonCard() {
        return model.wagonStack.pop();
    }

    //adding wagon card to a player
    @Override
    public void addWagonCard(int index) {
        model.getPlayer().AddCard(index);
    }

    //making the bots move, will pick one of 3 options: picking route cards, picking wagon cards or claiming path
    @Override
    public void botMove() {
        String moveMade = "";
        if (model.getPlayer().getRouteList().isEmpty() && !model.lastTurn) // if the bot doesn't have destination cards and not in his last turn
        {
            pickDestinationCards();
            moveMade = "The Bot has picked Destination Cards";
        }
        else {
            //pick best Route
            boolean pickedDestinationCard = false;
            List<List<Integer>> chosenPath = bestPath(model.getPlayer().getRouteList()); //picking the best path for completing as many route cards as possible with a fair tradeoff of wagons used
            List<List<Integer>> possibleConnections = null;
            if (chosenPath == null){ //if cannot complete any route card pick cards or try to complete more paths to get more points
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

            if(!pickedDestinationCard) { //if didn't pick route cards
                int connectionColorCode;
                if (possibleConnections.size() == 0) {
                    List<Integer> bestConnection;

                    //pick the best path to claim
                    if (chosenPath == null)
                        bestConnection = model.pickBestConnection(possibleConnections);
                    else
                        bestConnection = model.pickBestConnection(chosenPath);

                    int i = bestConnection.get(0);
                    int j = bestConnection.get(1);
                    connectionColorCode = model.getPathColorCode(model.getIndexByIAndJ(i, j));


                    int[] optionalOpenCards = iview.getOptionalWagonCards();
                    pickCards(connectionColorCode, optionalOpenCards);       //pick cards that will help claiming the path

                    //checks if needs shuffle
                    if (model.wagonStack.isEmpty() && model.cannotCompleteTurn(iview.getOptionalWagonCards())) {
                        model.insertUsedToWagonStack();
                        iview.initOptionalWagonCards();
                    }
                    moveMade = "The Bot has Picked Wagon Cards";

                } else {//if there are possible paths to claim, claim the best one
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
                        pathClicked(index);
                        }
                    else
                        pathClicked(model.getIndexByIAndJ(i, j));

                    moveMade = "The Bot has claimed a path";
                    iview.updateWagonsLeft();
                    iview.checkIfEnd();

                }

            }

        }

        iview.updateBotsMove(moveMade);
        iview.updatePointLabels();
        model.nextPlayer();

    }

    // pick destination cards
    private void pickDestinationCards() {
        ArrayList<Route> optionalRoutes = model.getRouteCards();// getting 3 optional route cards
        List<List<Integer>> chosenPath = bestPath(optionalRoutes); //pick the best set of connections

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

        if (model.firstTurn) { //if is the first turn mae sure that the bot picks at least 2 cards
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


        //theres a possibility that the bot ill pick a Route card that is already completed
        Iterator<Route> iterator = model.getPlayer().getRouteList().iterator();
        while (iterator.hasNext()) {
            Route route = iterator.next();
            if (model.isRouteCompleted(route)) {
                model.getPlayer().incPoints(route.getPoints());
                iterator.remove();
            }
        }

    }

    //returning a set of connections the player is able to claim
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

    //getting a list of vertices out of a set of connections {(1, 2), (2, 3)} ---> {1, 2, 3}
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

    //picking the best path for completing as many route cards as possible
    private List<List<Integer>> bestPath(ArrayList<Route> routeList) {
        List<List<Route>> routesSet = getAllRouteCombinations(routeList);   //getting every combination of route cards {A,B} --> {(A), (B), (A, B), (B, A)}
        Map<List<Route>, Map<List<List<Integer>>, Integer>> routeCombos2Paths = new HashMap<>();   //creating a map that will store every combination as key and the shortest paths for completing the set as value
        Map<List<List<Integer>>, Float> pathsScores = new HashMap<>();    //creating a map that will store every path and its corresponding score calculated by the calculateScore  function in model
        List<List<Integer>> chosenPath;

        //get every path for every combo
        for (List<Route> set: routesSet
        ) {
            routeCombos2Paths.put(set, model.pickRoutes((ArrayList<Route>) set, model.getPlayer().getNumOfWagons())); //insert the paths picked for every combo
            if (routeCombos2Paths.get(set).size() == 0) //if couldn't find path than delete the combination
                routeCombos2Paths.remove(set);
        }

        //calculate the score for every path
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

                pathsScores.put(path, model.calculateScore(cardsCompleted,cardsValue,pathWeight, pathConnections)); //insert the paths and scores
            }
        }

        //if couldn't find any path than return null;
        if (pathsScores.size() == 0)
            return null;
        return Collections.max(pathsScores.entrySet(),Map.Entry.comparingByValue()).getKey(); // return the path with the max score
    }

    //getting the value of all of the route cards in the set
    private int getRoutesValue(List<Route> set) {
        int sum = 0;
        for (Route r: set
             ) {
            sum += r.getPoints();
        }
        return sum;
    }

    //getting every combination of route cards {A,B} --> {(A), (B), (A, B), (B, A)}
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

    // an helper function for 'getAllRouteCombinations' that gets for every unique set every set in different order (A,B) --> {(A, B), (B, A)}
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
            remaining.addAll(set.subList(i+1, set.size()));

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

    // use wagon cards that some player holds
    public void useWagonCard(int index) {
        model.getPlayer().removeCard(index);
        model.usedWagonStack.push(index);
    }

    //picking cards that will help to claim the chosen connection
    public void pickCards(int connectionColorCode, int[] optionalOpenCards) {
        int chosenCards = 0;
        int i = 0;

        if (connectionColorCode == 8){
            for (int j = 0; j < 5; j++) {
                if (optionalOpenCards[j] == 8){
                    addWagonCard(8);
                    if (!model.wagonStack.isEmpty())
                        iview.updateOptionalWagon(j, getWagonCard());
                    else {
                        iview.removeOptionalWagon(j);
                        optionalOpenCards[j] = -1;
                    }
                    return;
                }
            }
        }


        //if there are cards of the color we want pick them
        while(i < 5 && chosenCards < 2 ) {
            if (optionalOpenCards[i] == connectionColorCode && chosenCards < 2){
                addWagonCard(optionalOpenCards[i]);
                if (!model.wagonStack.isEmpty())
                    iview.updateOptionalWagon(i, getWagonCard());
                else {
                    iview.removeOptionalWagon(i);
                    optionalOpenCards[i] = -1;
                }
                chosenCards++;
                i--;
            }
            i++;
        }

        //if founs only one in that color, pick one from the deck or, if empty, other random card
        if (chosenCards == 1){
            if (!model.wagonStack.isEmpty()) {
                addWagonCard(getWagonCard());
                chosenCards++;
            }
            else{
                for (int j = 0; j < 5 ; j++) {
                    if (optionalOpenCards[j] != 8 && optionalOpenCards[j] != -1 && chosenCards< 2) {
                        addWagonCard(optionalOpenCards[j]);
                        iview.removeOptionalWagon(j);
                        optionalOpenCards[j] = -1;
                        chosenCards++;
                    }
                }
            }
        }

        // if didn't find one of the chosen color, pick joker
        if (chosenCards == 0){
            for (int j = 0; j < 5 ; j++) {
                if (optionalOpenCards[j] == 8 && chosenCards < 2) {
                    addWagonCard(optionalOpenCards[j]);
                    if (!model.wagonStack.isEmpty())
                        iview.updateOptionalWagon(j, getWagonCard());
                    else {
                        iview.removeOptionalWagon(j);
                        optionalOpenCards[j] = -1;
                    }
                    chosenCards +=2;
                }
            }

            //if there is no joker try to draw 2 from the deck
            while (!model.wagonStack.isEmpty() && chosenCards < 2) {
                addWagonCard(getWagonCard());
                chosenCards++;
            }

            //if the deck is empty and already picked one
            if(chosenCards == 1){
                for (int j = 0; j < 5 ; j++) {
                    if ( optionalOpenCards[j] != -1 && chosenCards < 2) {
                        addWagonCard(optionalOpenCards[j]);
                        iview.removeOptionalWagon(j);
                        optionalOpenCards[j] = -1;
                        chosenCards++;
                    }
                }
            }

            //if there was no other option pick two random cards from the open ones
            else if (chosenCards == 0){
                i = 0;
                while(i < 5 && chosenCards < 2 ) {
                    if (optionalOpenCards[i] != -1 && chosenCards < 2) {
                        addWagonCard(optionalOpenCards[i]);
                        iview.removeOptionalWagon(i);
                        optionalOpenCards[i] = -1;
                        chosenCards++;
                        i--;
                    }
                    i++;
                }
            }
        }

        //update the optional open cards
        iview.setOptionalWagonCards(optionalOpenCards);
    }

    //has the player completed any destination/route cards
    public void hasCompletedDestinationCards() {
        Iterator<Route> iterator = model.getPlayer().getRouteList().iterator();
        while (iterator.hasNext()) {
            Route route = iterator.next();
            if (model.isRouteCompleted(route)) {
                model.getPlayer().incPoints(route.getPoints());
                iterator.remove();
            }
        }
    }
}
