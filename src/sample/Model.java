package sample;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.*;

import static java.awt.Color.*;
import static javafx.scene.paint.Color.PURPLE;

public class Model {
    final int NUM_OF_ROUTES = 26;
    final int NUM_OF_CITIES = 16;
    final int NUM_OF_PLAYERS = 4;

    private Player[] PLAYER_ARR = new Player[NUM_OF_PLAYERS];
    private int currentPlayer = 0;
    private Connection[][] board= new Connection[NUM_OF_CITIES][NUM_OF_CITIES];
    Stack<Integer> wagonStack = new Stack<>();
    Queue<Route> routeQueue= new LinkedList<>() ;
    Map<Integer, Integer[]> routeMap = new HashMap<>();
    Map< Integer, String> cityMap = new HashMap<>();
    Color[] playersColors = {Color.BLUE, Color.WHITE, Color.GREEN, Color.ORANGE};

    public Model() {
        initGame();
    }

    public void initGame() {
        //players initializer
        for (int i = 0; i <NUM_OF_PLAYERS; i++)
            PLAYER_ARR[i] = new Player(i);

        //initializing the wagon card stack
        initWagonStack();

        //initializing the wagon card stack
        initRouteQueue();

        //initializing the routeMap (maps route number to i,j in board graph)
        initRouteMap();

        //initializing the cityMap (maps cities number to their name)
        initCityMap();
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
        cityMap.put(1, "Hellena");
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
    }//so far, no need to use these. keeping the function just to remember the cities and their number

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
            i++;
        }

    }

    public Player getPlayer() {
        return PLAYER_ARR[currentPlayer];
    }

    public Color getPlayerColor(int player){
        return playersColors[player];

    }


    public void occupyRoute(int player, int index) {
       int i  = this.routeMap.get(index)[0];
       int j  = this.routeMap.get(index)[1];

       if(board[i][j].getNumOfRoads() == 2){
           //ASK ALON
       }
       else{
           board[i][j].placeRoute(player, playersColors[player] );
       }
        PLAYER_ARR[player].incPoints(board[i][j].getWeight());


    }

    public boolean isWin(int player) {
        return (this.PLAYER_ARR[player].getPoints() >= 100);
    }

    public void nextPlayer() {
        this.currentPlayer = (this.currentPlayer + 1) % NUM_OF_PLAYERS;
    }

    public ArrayList<Route> getRouteCards() {
        int i = 0;
        ArrayList<Route> r = new ArrayList<Route>();
        while(!routeQueue.isEmpty() && i < 3 ){
            r.add(routeQueue.remove());
            i++;
        }

        return r;
    }


    public String getCityName(int cityNum) {
        return cityMap.get(cityNum);
    }

    public void insertQueue(ArrayList<Route> optionalRouteCards) {
        while(!optionalRouteCards.isEmpty()) {
            routeQueue.add(optionalRouteCards.remove(0));
        }
    }


    public void insertRouteToPlayer(Route r) {
        Player currentPlayer = getPlayer();
        currentPlayer.AddRouteList(r);

    }
}


