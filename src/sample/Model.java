package sample;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.PriorityQueue;
import java.util.Comparator;

import java.util.*;

public class Model {
    final int NUM_OF_WAGONS = 35;                                                         //number of wagons available for each player
    final int NUM_OF_CITIES = 16;                                                         //number of cities (verticies in the graph)
    final int NUM_OF_PLAYERS = 2;                                                         //number of players playing the game

    private Player[] PLAYER_ARR = new Player[NUM_OF_PLAYERS];                             //creating an array of player
    private int currentPlayer = 0;                                                        //current player, the user is playing first
    protected Connection[][] board= new Connection[NUM_OF_CITIES][NUM_OF_CITIES];         //creating the graph using adjacency matrix made of Connection type
    Stack<Integer> wagonStack = new Stack<>();                                            //creating the wagon cards deck
    Stack<Integer> usedWagonStack = new Stack<>();                                        //creating the used wagon cards deck
    Queue<Route> routeQueue= new LinkedList<>() ;                                         //creating the route cards deck
    Map<Integer, Integer[]> routeMap = new HashMap<>();                                   //maps route number to i,j in board graph
    Map< Integer, String> cityMap = new HashMap<>();                                      //maps vertex number to the city's name
    Map< Integer, Integer> wagonsToPoints = new HashMap<>();                              //maps the route length to it's points value
    Map< Integer, Integer> doubledPaths = new HashMap<>();                                //map for the doubled paths, holds route number and it's corresponding index in the Connection
    Map< Integer, Color> pathsIndexToColor = new HashMap<>();                             //maps the paths index to their color
    Map<Color, Integer> colorsAndNumbers = new HashMap<Color, Integer>();                 //maps the color code to its color: {WHITE, BLUE, YELLOW, PURPLE, ORANGE, BLACK, RED, GREEN, JOKER}
    Color[] playersColors = {Color.BLUE, Color.WHITE};                                    //players' colors
    List<Integer> botsCityList = new ArrayList<>();                                       //list of the cities the bot has connected
    int lastTurnCounter = NUM_OF_PLAYERS + 1;                                             //activated right after the players turn (and he needs to play another turn himself as well)
    boolean lastTurn = false;                                                             //boolean flag for ending the game
    boolean firstTurn = true;                                                             //boolean flag for the first turn
    List<Integer> criticalCities = new ArrayList<>();                                     //cities with 2 or less connections to other cities


    public Model() {
        initGame();
    }

    public void initGame() {
        //players initializer
        for (int i = 0; i <NUM_OF_PLAYERS; i++)
            PLAYER_ARR[i] = new Player(i);

        // initializing the colors to numbers map
        initColorsAndNumbers();

        //initializing the wagon card stack
        initWagonStack();

        //initializing the wagon card stack
        initRouteQueue();

        //initializing the routeMap (maps route number to i,j in board graph)
        initRouteMap();

        //initializing the cityMap (maps cities number to their name)
        initCityMap();

        //initializing the wagonsToPoints that maps the number of wagons in the path that has been placed to its value in points
        initWagonsToPoints();

        //initializing doubledPaths that maps which path fits the right index in the Connection
        initDoubledPaths();
    }

    public void initWagonStack() {
        List<Integer> values = new ArrayList<>();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 12; j++)
                values.add(i);
        }
        //2 additional joker cards
        values.add(8);
        values.add(8);

        Collections.shuffle(values);

        // Push the shuffled values onto the stack
        for (int value : values)
            wagonStack.push(value);
    }

    public void initRouteQueue(){
        List<Route> values = new ArrayList<>();
        values.add(new Route(13,15, 12));
        values.add(new Route(1,5, 8));
        values.add(new Route(2,12, 17));
        values.add(new Route(0,5, 11));
        values.add(new Route(8,10, 7));
        values.add(new Route(4,8, 9));
        values.add(new Route(2,15, 20));
        values.add(new Route(6,13, 14));
        values.add(new Route(3,14, 15));
        values.add(new Route(12,15, 6));
        values.add(new Route(7,10, 13));
        values.add(new Route(9,12, 6));
        values.add(new Route(0,11, 18));
        values.add(new Route(2,8, 12));
        values.add(new Route(1,10, 13));
        Collections.shuffle(values);

        // Push the shuffled values into the Queue
        for (Route value : values)
            routeQueue.add(value);
    }

    public void initWagonsToPoints(){
        wagonsToPoints.put(1,1);
        wagonsToPoints.put(2,2);
        wagonsToPoints.put(3,4);
        wagonsToPoints.put(4,7);
        wagonsToPoints.put(5,10);
        wagonsToPoints.put(6,15);
    }

    public void initDoubledPaths(){
        doubledPaths.put(2,0);
        doubledPaths.put(25,1);
    }

    public void initColorsAndNumbers(){ //{WHITE, BLUE, YELLOW, PURPLE, ORANGE, BLACK, RED, GREEN, JOKER}
         colorsAndNumbers.put(Color.WHITE, 0);
         colorsAndNumbers.put(Color.BLUE, 1);
         colorsAndNumbers.put(Color.YELLOW, 2);
         colorsAndNumbers.put(Color.PURPLE, 3);
         colorsAndNumbers.put(Color.ORANGE, 4);
         colorsAndNumbers.put(Color.BLACK, 5);
         colorsAndNumbers.put(Color.RED, 6);
         colorsAndNumbers.put(Color.GREEN, 7);
    }

    public void initRouteMap(){
        //routeIndex --> i,j of connection routeMap
        routeMap.put(0, new Integer[] {9,10});
        routeMap.put(1, new Integer[] {12,13});
        routeMap.put(2, new Integer[] {11,12});
        routeMap.put(3, new Integer[] {8,11});
        routeMap.put(4, new Integer[] {12,14});
        routeMap.put(5, new Integer[] {14,15});
        routeMap.put(6, new Integer[] {8,14});
        routeMap.put(7, new Integer[] {6,8});
        routeMap.put(8, new Integer[] {6,9});
        routeMap.put(9, new Integer[] {9,11});
        routeMap.put(10, new Integer[] {6,7});
        routeMap.put(11, new Integer[] {3,7});
        routeMap.put(12, new Integer[] {1,7});
        routeMap.put(13, new Integer[] {1,3});
        routeMap.put(14, new Integer[] {0,1});
        routeMap.put(15, new Integer[] {5,10});
        routeMap.put(16, new Integer[] {7,8});
        routeMap.put(17, new Integer[] {3,6});
        routeMap.put(18, new Integer[] {5,6});
        routeMap.put(19, new Integer[] {3,5});
        routeMap.put(20, new Integer[] {4,5});
        routeMap.put(21, new Integer[] {2,3});
        routeMap.put(22, new Integer[] {0,2});
        routeMap.put(23, new Integer[] {2,4});
        routeMap.put(24, new Integer[] {10,12});
        routeMap.put(25, new Integer[] {11,12});
    }

    public void initCityMap(){
        cityMap.put(0, "Portland");
        cityMap.put(1, "Helena");
        cityMap.put(2, "San Francisco");
        cityMap.put(3, "Salt Lake");
        cityMap.put(4, "Los Angeles");
        cityMap.put(5, "Phoenix");
        cityMap.put(6, "Denver");
        cityMap.put(7, "Minneapolis");
        cityMap.put(8, "Chicago");
        cityMap.put(9, "Lubbock");
        cityMap.put(10, "Houston");
        cityMap.put(11, "Oklahoma City");
        cityMap.put(12, "Atlanta");
        cityMap.put(13, "Miami");
        cityMap.put(14, "Charleston");
        cityMap.put(15, "New York");
    }

    public void initCriticalCities(){
        for (int i = 0; i < NUM_OF_CITIES; i++) {
            int counter = 0;
            for (int j = 0; j < NUM_OF_CITIES; j++) {
                if (board[i][j] != null)
                    counter++;
            }
            if (counter <= 2){              //if has 2 or less connections
                criticalCities.add(i);
            }
        }
    }

    //inset to connection board the data
    public void initPossibleConnections(ArrayList<Pane> panes){
        int i =0;
        for (Integer[] array : routeMap.values()){

            // Get the first Rectangle from the Pane (assuming there is at least one)
            Rectangle rect = (Rectangle) panes.get(i).getChildren().get(0);

            // Get the fill color of the Rectangle
            Color fillColor = (Color) rect.getFill();

            Color[] temp = new Color[] { fillColor };


            if(board[array[0]][array[1]] == null) //one road
            {
                board[array[0]][array[1]] = new Connection(panes.get(i).getChildren().size(), 1);
            }
            else // two roads
            {
                temp = new Color[] {board[array[0]][array[1]].getColor()[0], fillColor };
                board[array[0]][array[1]] = new Connection(board[array[0]][array[1]].getWeight(), 2);
            }
            board[array[0]][array[1]].setColor(temp);
            board[array[1]][array[0]] = board[array[0]][array[1]]; //for symmetrical purposes

            int index = Integer.valueOf(panes.get(i).getId().split("e")[1]);
            pathsIndexToColor.put(index,fillColor);
            i++;
        }

        //initializing criticalCities that contains the cities with 2 or less connection
        initCriticalCities();
    }

    //returning the current player
    public Player getPlayer() {
        return PLAYER_ARR[currentPlayer];
    }

    //returning the players color
    public Color getPlayerColor(int player){
        return playersColors[player];
    }

    //returns if last turn for each player or not
    public boolean isLastTurn(int player) {
        return (this.PLAYER_ARR[player].getNumOfWagons() <= 2 );
    }

    //passing the turn to the next player
    public void nextPlayer() {
        this.currentPlayer = (this.currentPlayer + 1) % NUM_OF_PLAYERS;
    }

    //returning 3 route cards
    public ArrayList<Route> getRouteCards() {
        int i = 0;
        ArrayList<Route> r = new ArrayList<Route>();
        while(!routeQueue.isEmpty() && i < 3 ){
            r.add(routeQueue.remove());
            i++;
        }
        return r;
    }

    //returning city name by cityNum
    public String getCityName(int cityNum) {
        return cityMap.get(cityNum);
    }

    //inserting the unpicked route cards back to the deck
    public void insertQueue(ArrayList<Route> optionalRouteCards) {
        while(!optionalRouteCards.isEmpty()) {
            Route r = optionalRouteCards.remove(0);
            if (r!= null)
                routeQueue.add(r);
        }
    }

    //inserting new route card to players' route list
    public void insertRouteToPlayer(Route r) {
        Player currentPlayer = getPlayer();
        currentPlayer.AddRouteList(r);

    }

    //returning the player_arr
    public Player[] getPLAYER_ARR() {
        return PLAYER_ARR;
    }

    //returns true if the current player can occupy the path (checks if he has enough cards and wagons)
    public boolean canOccupyPath(int index) {
        int i = routeMap.get(index)[0];
        int j = routeMap.get(index)[1];
        int w = board[i][j].getWeight();
        Color c;
        int sum;

        if(board[i][j].getNumOfRoads() == 2){
            c = board[i][j].getColor()[doubledPaths.get(index)];
        }
        else{
            c = board[i][j].getColor()[0];
        }
        sum = PLAYER_ARR[currentPlayer].getCards()[colorsAndNumbers.get(c)] + PLAYER_ARR[currentPlayer].getCards()[8] ;
        return sum >= w && PLAYER_ARR[currentPlayer].getNumOfWagons() >= w;
    }

    //occupying the path, increasing points and decreasing num of wagons
    public void occupyPath(int player, int index) {
        int i  = this.routeMap.get(index)[0];
        int j  = this.routeMap.get(index)[1];

        if(board[i][j].getNumOfRoads() == 2)
            board[i][j].placePath(player, playersColors[player], doubledPaths.get(index));
        else
            board[i][j].placePath(player, playersColors[player] );

        PLAYER_ARR[player].incPoints(wagonsToPoints.get(board[i][j].getWeight()));
        PLAYER_ARR[player].decreaseNumOfWagons(board[i][j].getWeight());
    }

    //checking if a route card was completed
    public boolean isRouteCompleted(Route route){ //DFS
        int source = route.getCities()[0], destination = route.getCities()[1] ;
        boolean[] visited = new boolean[NUM_OF_CITIES];
        return dfs( visited, source, destination);

    }

    //Depth First Search, used for checking if there is aconnection between source and destination
    private boolean dfs(boolean[] visited, int source, int destination) {
        visited[source] = true;

        //if got to the destination return true
        if(source == destination) {
            return true;
        }

        //else, recursively check for every neighbour vertex
        for(int i = 0; i < NUM_OF_CITIES; i++) {
            if(isConnectionExists( source,  i,  currentPlayer)  && !visited[i]) {
                boolean isConnected = dfs(visited, i, destination);
                if(isConnected) {
                    return true;
                }
            }
        }

        return false;
    }

    //get the path color code by its index in the gui
    public int getPathColorCode(int index) {
        int i = routeMap.get(index)[0];
        int j = routeMap.get(index)[1];
        Color c;
        if(board[i][j].getNumOfRoads() == 2)
            c = board[i][j].getColor()[doubledPaths.get(index)];
        else
            c = board[i][j].getColor()[0];
        return colorsAndNumbers.get(c);
    }

    //returns path weight
    public int getPathWeight(int index) {
        int i = routeMap.get(index)[0];
        int j = routeMap.get(index)[1];
        return board[i][j].getWeight();
    }

    // inserting the used wagons back to the wagon stack
    public void insertUsedToWagonStack() {
        Collections.shuffle(usedWagonStack);
        while(!usedWagonStack.isEmpty()){
            wagonStack.push(usedWagonStack.pop());
        }
    }

    //returns true if there is a players connection between i and j vertices
    public boolean isConnectionExists(int i, int j, int playerID){
        if(board[i][j] != null){
            if(board[i][j].getNumOfRoads() == 2)
                return board[i][j].getPlayerID()[0] == playerID || board[i][j].getPlayerID()[1] == playerID;
            else
                return board[i][j].getPlayerID()[0] == playerID;
        }
        return false;
    }

    //returns true if there is another players connection between i and j vertices
    public boolean isTaken(int i, int j, int playerID) {
        if(board[i][j] != null && board[i][j].isComplete()){
            if(board[i][j].getNumOfRoads() == 2)
                return board[i][j].getPlayerID()[0] != playerID && board[i][j].getPlayerID()[1] != playerID;
            else
                return board[i][j].getPlayerID()[0] != playerID;
        }
        return false;
    }

    //function that returns all of the possible shortest paths between two vertices on the graph
    public List<List<List<Integer>>> getAllShortestPaths(int source, int dest, int playerID, List<List<Integer>> explored)
    {
            int n = NUM_OF_CITIES;
            int[] dist = new int[n];
            boolean[] visited = new boolean[n];
            List<List<List<Integer>>> paths = new ArrayList<>();
            PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt((int[] a) -> a[1]));
            for (int i = 0; i < n; i++) {
                dist[i] = Integer.MAX_VALUE;
            }
            dist[source] = 0;

            pq.offer(new int[] {source, 0});// inserting the first vertex (source) and its distance from the source (0)
            while (!pq.isEmpty()) {
                int[] curr = pq.poll();
                int u = curr[0];
                if (u == dest) {
                    // Reached destination, build all paths
                    List<Integer> path = new ArrayList<>();
                    buildPaths(paths, path, source, dest, dist, playerID, explored, new HashSet<Integer>());
                    return paths;
                }
                if (visited[u]) { //if visited, no need to check
                    continue;
                }
                visited[u] = true;

                for (int v = 0; v < n; v++) {
                    if (board[u][v] != null) {
                        int alt = dist[u] + board[u][v].getWeight(); //calculating the distance from the source

                        //check if the route is taken by someone else, if so than not possible to get from there
                        if(isTaken(u,v,playerID)) {
                            alt = Integer.MAX_VALUE;
                        }

                        // Check if a connection already exists between u and v
                        else if( isConnectionExists( u,  v, playerID) || (explored!= null && explored.contains(List.of(u, v)))) {
                            alt = dist[u] + 0;
                        }

                        if (alt < dist[v]) {
                            dist[v] = alt;
                            pq.offer(new int[] {v, alt}); //insert the next vertex
                        }
                    }
                }
            }
            return paths; // No paths found
    }

    //helper function for building paths and returning them back to getAllShortestPaths function
    public void buildPaths( List<List<List<Integer>>> paths, List<Integer> path, int u, int dest, int[] dist, int playerID, List<List<Integer>> explored, Set<Integer> visited) {
        path.add(u);
        visited.add(u);
        if (u == dest) {
            List<List<Integer>> edgesPath = verticesToEdges(path);
            paths.add(new ArrayList<>(edgesPath));
        } else {
            for (int v = 0; v < NUM_OF_CITIES; v++) {
                if (board[u][v] != null) {
                    int x = dist[u] + board[u][v].getWeight();

                    //check if the route is taken by someone else, if so than not possible to get from there
                    if(isTaken(u,v,playerID)) {
                        x = Integer.MAX_VALUE;
                    }

                    // Check if a connection already exists between u and v
                    if( isConnectionExists( u,  v, playerID) || (explored!= null && explored.contains(List.of(u, v)))) {
                        x = dist[u] + 0;
                    }

                    if(dist[v] == x && !visited.contains(v))
                        buildPaths( paths, path, v, dest, dist, playerID, explored, visited);
                }
            }
        }
        path.remove(path.size() - 1);
        visited.remove(u);

    }

    //returns a list of connections i and j's out of a list of vertices
    public List<List<Integer>> verticesToEdges(List<Integer> path) {
        List<List<Integer>> explored = new ArrayList<>();
        for (int i = 0; i < path.size() - 1; i++) {
            explored.add(List.of(path.get(i), path.get(i + 1)));
        }
        return explored;
    }

    //returns true if there are common connections between the paths
    public boolean mergeable(List<List<Integer>> list1 , List<List<Integer>> list2) {
        for (List<Integer> sublist1 : list1) {
            for (List<Integer> sublist2 : list2) {
                if (sublist1.equals(sublist2)) {
                    return true;
                }
            }
        }
        return false;
    }

    //merge the paths into one list of connections
    public List<List<Integer>> mergePaths(List<List<Integer>> list1 , List<List<Integer>> list2) {
        List<List<Integer>> mergedPath = list1;

        for (List<Integer> pair : list2) {
            if (!mergedPath.contains(pair))
                mergedPath.add(pair);
        }

        return mergedPath;
    }

    //a function to pick the shortes paths that connect every route card in the "routes" list
    public Map<List<List<Integer>>, Integer> pickRoutes(ArrayList<Route> routes, int numOfWagons){
        Queue<List<Integer>> routeCards = new LinkedList<>();

        //getting the cities numbers out of thw route cards
        for (Route route: routes
        ) {
            int city1 = route.getCities()[0];
            int city2 = route.getCities()[1];
            routeCards.add(List.of(city1, city2));
        }

        List<List<List<Integer>>> paths;
        List<List<List<Integer>>> nextPaths;

        //getting the first route card shortest path
        List<Integer> pair = routeCards.remove();
        paths = getAllShortestPaths(pair.get(0) ,pair.get(1), getPlayer().getPlayerID(), null);

        while (!routeCards.isEmpty()){
            pair = routeCards.remove();
            int tempSize = paths.size();
            for (int i = 0; i < tempSize; i++) {
                List<List<Integer>> path = paths.get(i);

                //getting the next shortest path while considering we already claimed the previous connections
                nextPaths = getAllShortestPaths(pair.get(0) ,pair.get(1), getPlayer().getPlayerID(), path);

                //checking if the paths are able to be merged
                for (List<List<Integer>> nextPath: nextPaths
                ) {
                    if (mergeable(nextPath, path)) {
                        paths.add(mergePaths(nextPath, path));
                    }
                }
            }
            paths = paths.subList(tempSize, paths.size()); //leaving only the merged paths (if exist) and removing the first ones
        }

        Map<List<List<Integer>>, Integer> weightedPaths = calculatePathsCost(paths, numOfWagons); //calculating every path weight
        return weightedPaths;
    }

    //calculating the path cost and removing the ones with higer cost
    public Map<List<List<Integer>>, Integer> calculatePathsCost(List<List<List<Integer>>> paths, int numOfWagons) {
        int minWeight = Integer.MAX_VALUE;
        Map<List<List<Integer>>, Integer> weightedPaths = new HashMap<>();
        for (List<List<Integer>> path : paths
        ) {
            int weight = getRouteWeight(path);
            if (weight <= numOfWagons) // only possible if there are enough train cars
            {
                weightedPaths.put(path, weight);
                if (weight < minWeight)
                    minWeight = weight;  //finding the minimum path
            }
        }

        for (Map.Entry<List<List<Integer>>, Integer> entry : weightedPaths.entrySet()
        ) {
            if (entry.getValue() != minWeight ){
                weightedPaths.remove(entry);  //removing every path that has a cost higher then the minimum
            }
        }
        return weightedPaths;
    }

    //getting the path weight
    private int getRouteWeight(List<List<Integer>> path) {
        int sum = 0;
        for (List<Integer> connection: path
             ) {
            if(!isConnectionExists(connection.get(0), connection.get(1), getPlayer().getPlayerID()))
                sum += board[connection.get(0)][connection.get(1)].getWeight();
        }
        return sum;
    }

    //claculating the score for a path considering how many cards will it complete, cardsValue, pathWeight and number of different connections that needs to be places
    public float calculateScore(int cardsCompleted, int cardsValue, int pathWeight, int pathConnections) {
        return ((float) cardsCompleted * cardsValue) / ((float) pathConnections * pathWeight);

    }

    //gets route gui index by i and j
    public Integer getIndexByIAndJ(int i, int j) { //
        for (Map.Entry<Integer, Integer[]> entry : routeMap.entrySet()) {
            Integer[] value = entry.getValue();
            if ((value[0].equals(i) && value[1].equals(j)) || (value[0].equals(j) && value[1].equals(i))) {
                return entry.getKey();
            }
        }
        return null;
    }

    //gets route gui index by i and j for the doubled paths
    public Integer getIndexByIAndJ(int i, int j, Color c) {
        for (Map.Entry<Integer, Integer[]> entry : routeMap.entrySet()) {
            Integer[] value = entry.getValue();
            if ((value[0].equals(i) && value[1].equals(j)) || (value[0].equals(j) && value[1].equals(i))  ) {
                if (pathsIndexToColor.get((entry.getKey())) == c)
                return entry.getKey();
            }
        }
        return null;
    }

    //from the path, pick the best connection possible using scoring system
    public List<Integer> pickBestConnection(List<List<Integer>> chosenPath) {
        int score;
        int maxScore = -1;
        List<Integer> chosenConnection = null;
        for (List<Integer> connection : chosenPath
        ) {
                int i = connection.get(0);
                int j = connection.get(1);

            if(!isConnectionExists(i,j,currentPlayer)){
                score = 0;
                Connection c = board[i][j];

                //bette score for shorter connections
                if (c.getWeight() == 2)
                    score += 50;
                else if (c.getWeight() == 3)
                    score += 30;
                else if (c.getWeight() == 4)
                    score += 20;

                //close to other bots connections
                if (isNearOtherConnections(connection, currentPlayer))
                    score += 60;

                //close to opponents
                if (isNearOtherConnections(connection, 0))
                    score += 30;

                //if the bot hasn't connected that city yet
                if (!(botsCityList.contains(i) && botsCityList.contains(j)))
                    score += 50;
                else if (!botsCityList.contains(i) || !botsCityList.contains(j))
                    score += 20;

                //if one of the cities is in the critical cities list.
                if (criticalCities.contains(i) || criticalCities.contains(j))
                    score += 80;

                if (score > maxScore) {
                    maxScore = score;
                    chosenConnection = connection;
                }
            }
        }
        return chosenConnection;
    }

    //returns true if is near other connections
    private boolean isNearOtherConnections(List<Integer> connection, int playerID) {
        int i = connection.get(0);
        int j = connection.get(1);

        for (int k = 0; k < NUM_OF_CITIES; k++) {
            if ((k != j && isConnectionExists(i,k,playerID)) || (k != i && isConnectionExists(j,k,playerID))){
                return true;
            }
        }
        return false;
    }

    //returns true if the player or the bot aren't able to pick cards (one left for example)
    public boolean cannotCompleteTurn(int[] openCards){
        List<Integer> temp = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            if (openCards[i] != -1)
                temp.add(openCards[i]);
            }

        if (temp.size() == 2)
        {
            return (temp.get(0) == 8 && temp.get(1) !=8) || (temp.get(1) == 8 && temp.get(0) !=8);
        }
        else if (temp.size() == 1){
            return temp.get(0) != 8;
        }

        if (wagonStack.isEmpty() && temp.size()==0)
            return true;

        return false;
    }

    //if the game is about to end
    public boolean isEndGame(){
        for (int i = 0; i <NUM_OF_PLAYERS; i++) {
            if(PLAYER_ARR[i].getPoints() > 80 || PLAYER_ARR[i].getNumOfWagons() < 8)
                return true;
        }
        return false;
    }

    //returns every open connection that hasn't been claimed yet
    public List<List<Integer>> getAllFreeConnection() {
        List<List<Integer>> allFreeConnections = new ArrayList<>();
        for (int i = 0; i < NUM_OF_CITIES ; i++) {
            for (int j = 0; j < NUM_OF_CITIES; j++) {
                if (board[i][j] != null &&!board[i][j].isComplete()){
                    allFreeConnections.add(new ArrayList<>(Arrays.asList(i, j)));
                }
            }
        }
        return allFreeConnections;
    }

    //setting lastTurn flag as true
    public void setLastTurnCounter() {
        lastTurn = true;
    }

    //calculates the final points and returns who won
    public String whoWon() {
        int user = PLAYER_ARR[0].getPoints() - decreasePointsByRoute(PLAYER_ARR[0].getRouteList());
        int bot = PLAYER_ARR[1].getPoints() - decreasePointsByRoute(PLAYER_ARR[1].getRouteList());
        if (user > bot)
            return String.format("Game Over!\nYou won with %d points!", user);
        else if (user == bot)
            return String.format("Game Over!\nIt's a Tie! both of you finished the game with %d points!", user);
        else
            return String.format("Game Over!\nYou lost :(,the bot won with %d points!", bot);
    }

    //decreasing points for every route card a player has in hand at the end game
    private int decreasePointsByRoute(ArrayList<Route> routeList) {
        int sum = 0;
        for (Route r: routeList
             ) {
            sum += r.getPoints();
        }
        return sum;
    }

    //returns the color of a doubled path
    public Color get2PathColor(int i, int j) {
        Connection temp = board[i][j];
        if (temp.getPlayerID()[0] == -1 && temp.getPlayerID()[1] == -1)
            return temp.getColor()[0];//pick the first
        else{
            if (temp.getPlayerID()[0] != -1 && temp.getPlayerID()[1] == -1) //if first is taken take the second
                return temp.getColor()[1];
            else
                return temp.getColor()[0];
        }
    }

    //finds which player has the longest continuous path (using dfs)
    public List<Integer> findLongestPath() {
        int longestPath = 0;
        List<Integer> maxPlayers = new ArrayList<>();
        Map<Integer, Integer> playersLongestPaths = new HashMap<>();
        for (int i = 0; i <NUM_OF_PLAYERS ; i++) {
            boolean[] visited = new boolean[NUM_OF_CITIES];
            for (int j = 0; j < NUM_OF_CITIES; j++) {
                if (!visited[j]) {
                    int currentPathLength = dfsL(visited, j, i);
                    if (currentPathLength > longestPath) {
                        longestPath = currentPathLength;
                    }
                }
            }
            playersLongestPaths.put(i, longestPath);
            longestPath = 0;
        }
        int maxLength = Integer.MIN_VALUE;

        //for every player's longest path check who has the maximum path length and add them to the returned list
        for (Map.Entry<Integer, Integer> entry : playersLongestPaths.entrySet()) {
            int playerId = entry.getKey();
            int value = entry.getValue();
            if (value > maxLength){
                maxPlayers.clear();
                maxPlayers.add(playerId);
                maxLength = value;

            } else if (value == maxLength) {
                maxPlayers.add(playerId);
            }
        }
        return maxPlayers;
    }

    //modified version of dfs, returning the longest path of a specific player
    private int dfsL(boolean[] visited, int source, int player) {
        visited[source] = true;
        int maxLength = 0;

        //going ob=ver every vertex
        for (int i = 0; i < NUM_OF_CITIES; i++) {
            if (isConnectionExists(source, i, player) && !visited[i]) {
                int pathLength = board[source][i].getWeight() + dfsL(visited, i, player);
                if (pathLength > maxLength) {
                    maxLength = pathLength;
                }
            }
        }

        visited[source] = false;
        return maxLength;
    }
}
