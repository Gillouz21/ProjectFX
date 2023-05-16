package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main extends Application implements IView {
    private IPresenter presenter;
    private ArrayList<Pane> routePanes = new ArrayList<>();                           //panes to interact with the routes on the board
    private ArrayList<Pane> routeCardPanes = new ArrayList<>();                       //panes to interact with the route cards
    private ArrayList<Pane> wagonCardPanes = new ArrayList<>();                       //panes to interact with the wagon cards
    private ArrayList<Pane> playersWagonCardPanes = new ArrayList<>();                //panes to interact with the players wagon cards
    private ArrayList<Pane> generalPanes = new ArrayList<>();                         //list of all of the panes
    private Pane pointsPane;                                                          //pane of the points presented on the screen
    private Pane BotsMovePane;                                                        //pane that shows the bots moves
    private Pane wagonsLeft;                                                          //pane of the wagons left presented on the screen
    private int[] optionalWagonCards = new int[5];                                    //color code of the open wagon cards
    private Label lastTurnLabel;                                                      //pane of the last turn message
    private ListView<String> playersRouteCards;                                       //list view of the destination cards the user holds
    private static String ID_PREFIX = "#Route";                                       //prefix of all of the route panes
    boolean isDeckClicked = false;                                                    //boolean flag
    boolean firstChoice = false;                                                      //boolean flag
    int pathIndex = -1;                                                               //index of the route pane that is clicked by the user
    int firstTurnChoices = 0;                                                         //destination cards picked counter for the first turn
    int numOfChosenCards = 0;                                                         //wagon cards picked counter
    ArrayList<Route> optionalRouteCards = new ArrayList<Route>();                     //the optional route cards corresponding to the RouteCardPanes
    private Map<Integer, Image> wagonsImages = new HashMap<Integer ,Image>();         //images corresponding to the color code: {WHITE, BLUE, YELLOW, PURPLE, ORANGE, BLACK, RED, GREEN, JOKER}
    Stage primaryStage;                                                               //the primary window stage

    //initializing every part of the system and starting the main screen
    @Override
    public void start(Stage primaryStage) throws Exception{
        this.presenter = new Presenter(this);
        this.primaryStage = primaryStage;

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Ticket To Ride");

        Scene s = new Scene(root);
        insertRoutePanes(s);
        initWagonImages();
        insertTextPanes(s);
        insertCardPanes(s);
        initOptionalWagonCards();

        //passing the routes to the model so it could insert them to the board
        presenter.passPanes(routePanes);

        setMouseClicksLister();
        primaryStage.setScene(s);
        primaryStage.show();
    }

    //{WHITE, BLUE, YELLOW, PURPLE, ORANGE, BLACK, RED, GREEN, JOKER}
    private void initWagonImages(){
        wagonsImages.put(0, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\WHITE.jpg"));
        wagonsImages.put(1, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\BLUE.jpg"));
        wagonsImages.put(2, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\YELLOW.jpg"));
        wagonsImages.put(3, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\PURPLE.jpg"));
        wagonsImages.put(4, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\ORANGE.jpg"));
        wagonsImages.put(5, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\BLACK.jpg"));
        wagonsImages.put(6, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\RED.jpg"));
        wagonsImages.put(7, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\GREEN.jpg"));
        wagonsImages.put(8, new Image("C:\\Users\\User\\IdeaProjects\\ProjectFX\\src\\sample\\JOKER.jpg"));
    }

    //presenting the optional wagon cards
    public void initOptionalWagonCards() {
        int key, i = 0;
        for (Pane p : wagonCardPanes
             ) {
            p.setVisible(true);
            ImageView img = (ImageView) p.getChildren().get(0);
            key = presenter.getWagonCard();
            img.setImage(wagonsImages.get(key));
            optionalWagonCards[i] = key;
            i++;

        }
    }

    //inserting every route to the array list of the route panes
    private void insertRoutePanes(Scene s) {
        String str, numPref ;
        Pane p = (Pane) s.lookup(ID_PREFIX+"00");
        routePanes.add(p);
        generalPanes.add(p);
        for (int i = 1; i < 26; i++) {
            numPref = "";
            if(i < 10)
                numPref = "0";
            str = ID_PREFIX + numPref + i;

            p = (Pane) s.lookup(str);
            routePanes.add(p);
            generalPanes.add(p);
        }
    }

    //inserting the text related panes and card decks
    private void insertTextPanes(Scene s){
        Pane p = (Pane) s.lookup("#WagonDeck");
        generalPanes.add(p);
        p = (Pane) s.lookup("#RouteDeck");
        generalPanes.add(p);

        BotsMovePane = (Pane) s.lookup("#BotsMove");
        playersRouteCards = (ListView) s.lookup("#PlayersRouteCards");
        lastTurnLabel = (Label) s.lookup("#LastTurn");
        wagonsLeft = (Pane) s.lookup("#WagonsLeft");

        Label templ = (Label)  BotsMovePane.getChildren().get(0);
        templ.setText("Your Turn!");

        pointsPane = (Pane) s.lookup("#PlayersPoints");

        for (int i = 0; i < presenter.getModel().NUM_OF_PLAYERS; i++) {
            Label l = (Label)  pointsPane.getChildren().get(i * 2);
            Label points = (Label)  pointsPane.getChildren().get(i * 2 + 1);
            l.setVisible(true);
            points.setVisible(true);
            points.setText("0");

            //for the wagons Left:
            Label temp = (Label) wagonsLeft.getChildren().get(i);
            temp.setText(String.format("Player %d: %d Wagons Left", i, presenter.getModel().NUM_OF_WAGONS));
        }

    }

    //inserting every cards to the array lists
    private void insertCardPanes(Scene s){
        Pane p;
        for (int i = 0; i < 3; i++) {
            p = (Pane) s.lookup("#RouteCard" + i);
            routeCardPanes.add(p);
            generalPanes.add(p);
        }
        p = (Pane) s.lookup("#RouteCardEnd");
        routeCardPanes.add(p);
        generalPanes.add(p);

        for (int i = 0; i < 5; i++) {
            p = (Pane) s.lookup("#Wagon" + i);
            wagonCardPanes.add(p);
            generalPanes.add(p);
        }

        for (int i = 0; i < 9; i++) {
            p = (Pane) s.lookup("#PlayersWagon" + i);
            playersWagonCardPanes.add(p);
            generalPanes.add(p);
            ImageView img = (ImageView) p.getChildren().get(0);
            img.setImage(wagonsImages.get(i));
            Label l = (Label)  p.getChildren().get(1);
            l.setText("0");
        }
    }

    //changing the color and shape of an occupied route
    public void changeColor(int index, Color color){
        Pane parent = routePanes.get(index);

        for (int i = 0; i < parent.getChildren().size(); i++) {
            Rectangle rectangle = (Rectangle) parent.getChildren().get(i);
            Circle circle = new Circle(); // create a circle
            circle.setCenterX(rectangle.getLayoutX() + rectangle.getWidth() / 2);
            circle.setCenterY(rectangle.getLayoutY() + rectangle.getHeight() / 2);
            circle.setRadius(Math.min(rectangle.getWidth(), rectangle.getHeight())/1.3 );
            circle.setFill(color);
            circle.setOpacity(1);
            parent.getChildren().remove(rectangle);
            parent.getChildren().add(i,circle);
        }
    }

    //the function that handles every users click
    private void setMouseClicksLister() {
        for (Pane p:generalPanes
             ) {
            p.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    Pane pane = (Pane)mouseEvent.getSource();

                    //if Route clicked
                    if(pane.getId().matches("^Route\\d{2}$") && !isDeckClicked && !firstChoice && pathIndex == -1 && firstTurnChoices >=2 )
                    {
                        int index = Integer.valueOf(pane.getId().split("e")[1]);

                        if (presenter.getModel().canOccupyPath(index))
                            pathIndex = index;

                    }

                    //if route deck clicked
                    else if (pane.getId().matches("RouteDeck") && !isDeckClicked && !firstChoice && pathIndex == -1 && presenter.getModel().routeQueue.size() > 0)
                    {
                        optionalRouteCards = presenter.routeDeckClicked();
                        isDeckClicked = true;

                    }

                    //if route card clicked
                    else if(pane.getId().matches("RouteCard\\d+") && !firstChoice)
                    {
                        int index = Integer.valueOf(pane.getId().split("d")[1]);

                        presenter.insertRouteToPlayerRouteList(optionalRouteCards.get(index));
                        optionalRouteCards.set(index, null);
                        routeCardPanes.get(index).setVisible(false);

                        if (firstTurnChoices < 1)
                            firstTurnChoices++;
                        else if (firstTurnChoices < 2) {
                            showRouteEndOption();
                            firstTurnChoices++;
                        }
                        else
                            showRouteEndOption();

                        updatePlayersListView();
                    }

                    //if finished picking route cards
                    else if(pane.getId().matches("RouteCardEnd")  && firstTurnChoices >=2 )
                    {
                        presenter.insertRouteQueue(optionalRouteCards);
                        hideRouteCards(routeCardPanes);
                        isDeckClicked = false;

                        presenter.hasCompletedDestinationCards();
                        updatePlayersListView();
                        updatePointLabels();

                        checkIfEnd();


                        presenter.getModel().nextPlayer();
                        presenter.botMove();



                    }

                    //if wagon deck clicked
                    else if (pane.getId().matches("WagonDeck") && !isDeckClicked && pathIndex == -1 && !presenter.getModel().wagonStack.isEmpty()&& firstTurnChoices >=2 )
                    {
                        int index = presenter.getWagonCard();
                        presenter.addWagonCard(index);
                        updateWagonLabel(index);

                        if (firstChoice) {

                            checkIfEnd();

                            presenter.getModel().nextPlayer();
                            presenter.botMove();
                        }
                        firstChoice = !firstChoice;
                    }

                    //if optional wagon card clicked
                    else if(pane.getId().matches("Wagon\\d+") && !isDeckClicked && pathIndex == -1 && firstTurnChoices >=2  )
                    {
                        int paneIndex = Integer.valueOf(pane.getId().split("n")[1]);
                        int index = optionalWagonCards[paneIndex];

                        if (index == 8)                                 // multicolored
                        {
                          if (!firstChoice){                                        //if multicolored and the player didn't make the first choice
                              presenter.addWagonCard(index);
                              updateWagonLabel(index);
                              if (presenter.getModel().wagonStack.isEmpty()){
                                  removeOptionalWagon(paneIndex);
                                  optionalWagonCards[paneIndex] = -1;
                              }
                              else
                                  updateOptionalWagon(paneIndex, presenter.getWagonCard());


                              checkIfEnd();

                              presenter.getModel().nextPlayer();
                              presenter.botMove();


                          }
                        }
                        else{
                            presenter.addWagonCard(index);
                            updateWagonLabel(index);
                            if (presenter.getModel().wagonStack.isEmpty()) {
                                removeOptionalWagon(paneIndex);
                                optionalWagonCards[paneIndex] = -1;
                            }
                            else
                                updateOptionalWagon(paneIndex, presenter.getWagonCard());
                            if (firstChoice) {
                                checkIfEnd();
                                presenter.getModel().nextPlayer();
                                presenter.botMove();

                            }
                            firstChoice = !firstChoice;                                // made his first choice

                        }

                        if(isWagonStackFinished()){
                            presenter.getModel().insertUsedToWagonStack();
                            initOptionalWagonCards();
                        }

                    }

                    //if players wagon card clicked
                    else if(pane.getId().matches("PlayersWagon\\d+") && pathIndex != -1)
                    {
                        int playerWagonCardID = Integer.valueOf(pane.getId().split("n")[1]);
                        if((playerWagonCardID == presenter.getModel().getPathColorCode(pathIndex) || playerWagonCardID == 8)  && presenter.getModel().getPlayer().getCards()[playerWagonCardID] > 0){
                            presenter.useWagonCard(playerWagonCardID);
                            updateWagonLabel(playerWagonCardID);
                            numOfChosenCards++;
                        }

                        if(numOfChosenCards == presenter.getModel().getPathWeight(pathIndex)){
                            presenter.pathClicked(pathIndex);
                            pathIndex = -1;
                            numOfChosenCards = 0;
                            updatePlayersListView();
                            updatePointLabels();
                            updateWagonsLeft();


                            checkIfEnd();

                            presenter.getModel().nextPlayer();
                            presenter.botMove();
                        }
                    }
                }
            });
        }
    }

    //updating the optional wagon card
    public void updateOptionalWagon(int pI, int key) {
        ImageView img = (ImageView) wagonCardPanes.get(pI).getChildren().get(0);
        img.setImage(wagonsImages.get(key));
        optionalWagonCards[pI] = key;
    }

    //removing the optional wagon card (if the cad deck is empty)
    public void removeOptionalWagon(int paneIndex){
        Pane p = wagonCardPanes.get(paneIndex);
        p.setVisible(false);
    }

    //is the wagon deck empty
    public boolean isWagonStackFinished(){
        Boolean allDone = true;
        for (Pane pane : wagonCardPanes) {
            if(pane.isVisible()){
                allDone = false;
            }
        }
        return allDone;
    }

    //updating the amount of a specific wagon card a player has
    private void updateWagonLabel(int index) {
        Pane p = playersWagonCardPanes.get(index);
        Label l = (Label)  p.getChildren().get(1);
        l.setText(Integer.toString(presenter.getModel().getPLAYER_ARR()[0].getCards()[index]));
    }

    //presenting the optional route cards
    public void showRouteCard(ArrayList<Route> r){
        Pane p;
        for (int i = 0; i < r.size(); i++)
         {
             p = routeCardPanes.get(i);
            p.setVisible(true);
            Rectangle rec = (Rectangle) p.getChildren().get(0);
            Label city1 = (Label) p.getChildren().get(1);
            Label city2 = (Label) p.getChildren().get(2);
            Label points = (Label) p.getChildren().get(3);

            city1.setText(presenter.getCityName(r.get(i).getCities()[0]));
            city2.setText(presenter.getCityName(r.get(i).getCities()[1]));
            points.setText(Integer.toString(r.get(i).getPoints()));


            rec.setVisible(true);
            city1.setVisible(true);
            city2.setVisible(true);
            points.setVisible(true);

        }
    }

    //presenting the end button to end the route cards picking
    public void showRouteEndOption(){
        Pane p = routeCardPanes.get(3);
        p.setVisible(true);
    }

    //hiding the route cards
    private void hideRouteCards(ArrayList<Pane> routeCardPanes) {
        for (Pane p : routeCardPanes)
        {
            p.setVisible(false);
        }
    }

    //update the point labels
    public void updatePointLabels(){
        Player currP = presenter.getModel().getPlayer();
        int points = currP.getPoints();
        int pID = currP.getPlayerID();
        Label l = (Label) pointsPane.getChildren().get(pID * 2 + 1);
        l.setText(Integer.toString(points));

    }

    //updating the bots label (presenting bot moves)
    public void updateBotsMove(String s){
        Label l = (Label) BotsMovePane.getChildren().get(0);
        l.setText(s);
    }

    //getting the optional wagon cards currently presented on the screen
    public int[] getOptionalWagonCards(){
        return optionalWagonCards;
    }

    //updating the optional wagon cards (logical)
    public void setOptionalWagonCards(int[] optionalWagonCards){
        this.optionalWagonCards = optionalWagonCards;
    }

    //updating the list of route cards the player has
    public void updatePlayersListView(){
        playersRouteCards.getItems().clear();
        for (Route r: presenter.getModel().getPLAYER_ARR()[0].getRouteList()
             ) {
            String city1 = presenter.getModel().cityMap.get(r.getCities()[0]);
            String city2 = presenter.getModel().cityMap.get(r.getCities()[1]);
            int points = r.getPoints();
            playersRouteCards.getItems().add(String.format("%-20s %-20s %d", city1, city2, points));
        }

    }

    //showing the ending screen with the results of the game
    public void showEndingScreen( ) {
        String message = presenter.getModel().whoWon();
        Stage endingStage = new Stage();
        VBox root = new VBox();
        root.setAlignment(Pos.CENTER);
        root.setSpacing(20);

        Text messageText = new Text(message);
        messageText.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Button closeButton = new Button("Close");
        closeButton.setOnAction(e -> {
            endingStage.close();
            primaryStage.close();
        });

        root.getChildren().addAll(messageText, closeButton);
        Scene scene = new Scene(root, 600, 500);
        endingStage.setScene(scene);
        endingStage.show();

        primaryStage.hide();
    }

    //presenting a message for a last turn for every player
    public void showLastTurn(){
        lastTurnLabel.setText("Last Turn for every player!");
    }

    //updating the wagons left counter for each player
    public void updateWagonsLeft(){
        for (int i = 0; i < presenter.getModel().NUM_OF_PLAYERS; i++) {
            Label temp = (Label) wagonsLeft.getChildren().get(i);
            temp.setText(String.format("Player %d: %d Wagons Left", i, presenter.getModel().getPLAYER_ARR()[i].getNumOfWagons()));
        }
    }

    //has the game ended or not
    public void checkIfEnd(){
        if(presenter.getModel().lastTurn) {
            presenter.getModel().lastTurnCounter--;
            if (presenter.getModel().lastTurnCounter == 0) {
                List<Integer> longestPath = presenter.getModel().findLongestPath();
                for (Integer i: longestPath
                     ) {
                    presenter.getModel().getPLAYER_ARR()[i].incPoints(10); //increase player points for the lonest path;
                }
                showEndingScreen();
            }
        }
    }
}
