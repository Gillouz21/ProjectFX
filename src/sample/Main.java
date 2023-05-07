package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Main extends Application implements IView {
    private IPresenter presenter;
    private ArrayList<Pane> routePanes = new ArrayList<>();
    private ArrayList<Pane> routeCardPanes = new ArrayList<>();
    private ArrayList<Pane> wagonCardPanes = new ArrayList<>();
    private int[] optionalWagonCards = new int[5];
    private ArrayList<Pane> playersWagonCardPanes = new ArrayList<>();
    private Pane pointsPane;
    private ArrayList<Pane> generalPanes = new ArrayList<>();
    private static String ID_PREFIX = "#Route";
    boolean isDeckClicked = false;
    boolean firstChoice = false;
    int pathIndex = -1;
    int firstTurnChoices = 0;
    int numOfChosenCards = 0;
    ArrayList<Route> optionalRouteCards = new ArrayList<Route>();
    private Map<Integer, Image> wagonsImages = new HashMap<Integer ,Image>(); //{WHITE, BLUE, YELLOW, PURPLE, ORANGE, BLACK, RED, GREEN, JOKER}


    @Override
    public void start(Stage primaryStage) throws Exception{
        this.presenter = new Presenter(this);

        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");

        Scene s = new Scene(root);

        insertRoutePanes(s);

        initWagonImages();
        insertCardPanes(s);

        initOptionalWagonCards();


        //passing the routes to the model so it could insert them to the board
        presenter.passPanes(routePanes);



        setMouseClicksLister();

        primaryStage.setScene(s);



        primaryStage.show();
    }

    private void initWagonImages() { //{WHITE, BLUE, YELLOW, PURPLE, ORANGE, BLACK, RED, GREEN, JOKER}
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

    private void initOptionalWagonCards() {
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

    private void insertCardPanes(Scene s){
        Pane p = (Pane) s.lookup("#WagonDeck");
        generalPanes.add(p);
        p = (Pane) s.lookup("#RouteDeck");
        generalPanes.add(p);
        pointsPane = (Pane) s.lookup("#PlayersPoints");

        for (int i = 0; i < presenter.getModel().NUM_OF_PLAYERS; i++) {
            Label l = (Label)  pointsPane.getChildren().get(i * 2);
            Label points = (Label)  pointsPane.getChildren().get(i * 2 + 1);
            l.setVisible(true);
            points.setVisible(true);
            points.setText("0");
        }

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

    private void setMouseClicksLister() {
        for (Pane p:generalPanes
             ) {
            p.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    Pane pane = (Pane)mouseEvent.getSource();

                    if(pane.getId().matches("^Route\\d{2}$") && !isDeckClicked && !firstChoice && pathIndex == -1 && firstTurnChoices >=2 ) //if Route was clicked
                    {
                        int index = Integer.valueOf(pane.getId().split("e")[1]);

                        if (presenter.getModel().canOccupyPath(index)) 
                            pathIndex = index;
                        
                    }
                    else if (pane.getId().matches("RouteDeck") && !isDeckClicked && !firstChoice && pathIndex == -1 && presenter.getModel().routeQueue.size() > 0)
                    {
                        optionalRouteCards = presenter.routeDeckClicked();
                        isDeckClicked = true;
                    }
                    else if(pane.getId().matches("RouteCard\\d+") && !firstChoice)
                    {
                        int index = Integer.valueOf(pane.getId().split("d")[1]);

                        // pass index to presenter...
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
                    }
                    else if(pane.getId().matches("RouteCardEnd")  && firstTurnChoices >=2 ){
                        presenter.insertRouteQueue(optionalRouteCards);
                        hideRouteCards(routeCardPanes);
                        isDeckClicked = false;
                        presenter.getModel().nextPlayer();
                        presenter.botMove();
                    }
                    else if (pane.getId().matches("WagonDeck") && !isDeckClicked && pathIndex == -1 && !presenter.getModel().wagonStack.isEmpty()&& firstTurnChoices >=2 ) {
                        int index = presenter.getWagonCard();
                        presenter.addWagonCard(index);
                        updateWagonLabel(index);

                        if (firstChoice) {
                            presenter.getModel().nextPlayer();
                            presenter.botMove();
                        }
                        firstChoice = !firstChoice;
                    }
                    else if(pane.getId().matches("Wagon\\d+") && !isDeckClicked && pathIndex == -1 && firstTurnChoices >=2  ){
                        int paneIndex = Integer.valueOf(pane.getId().split("n")[1]);
                        int index = optionalWagonCards[paneIndex];

                        if (index == 8)                                 // multicolored
                        {
                          if (!firstChoice){                                        //if multicolored and the player didn't make the first choice
                              presenter.addWagonCard(index);
                              updateWagonLabel(index);
                              if (presenter.getModel().wagonStack.isEmpty())
                                  removePossibleWagon(paneIndex);
                              else
                                  updatePossibleWagon(paneIndex, presenter.getWagonCard());
                              presenter.getModel().nextPlayer();
                              presenter.botMove();


                          }
                        }
                        else{
                            presenter.addWagonCard(index);
                            updateWagonLabel(index);
                            if (presenter.getModel().wagonStack.isEmpty())
                                removePossibleWagon(paneIndex);
                            else
                                updatePossibleWagon(paneIndex, presenter.getWagonCard());
                            if (firstChoice) {
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
                    else if(pane.getId().matches("PlayersWagon\\d+") && pathIndex != -1){
                        int playerWagonCardID = Integer.valueOf(pane.getId().split("n")[1]);
                        if((playerWagonCardID == presenter.getModel().getPathColorCode(pathIndex) || playerWagonCardID == 8)  && presenter.getModel().getPlayer().getCards()[playerWagonCardID] > 0){
                            presenter.useWagonCard(playerWagonCardID);
                            updateWagonLabel(playerWagonCardID);
                            numOfChosenCards++;
                        }

                        if(numOfChosenCards == presenter.getModel().getPathWeight(pathIndex)){
                            presenter.pathClicked(pathIndex);
                            updatePointLabels();
                            pathIndex = -1;
                            numOfChosenCards = 0;

                            presenter.getModel().nextPlayer();
                            presenter.botMove();
                        }
                    }

                }
            });
        }
    }

    private void updatePossibleWagon(int pI, int key) {
        ImageView img = (ImageView) wagonCardPanes.get(pI).getChildren().get(0);
        img.setImage(wagonsImages.get(key));
        optionalWagonCards[pI] = key;
    }

    private void removePossibleWagon(int paneIndex){
        Pane p = wagonCardPanes.get(paneIndex);
        p.setVisible(false);
    }

    public boolean isWagonStackFinished(){
        Boolean allDone = true;
        for (Pane pane : wagonCardPanes) {
            if(pane.isVisible()){
                allDone = false;
            }
        }
        return allDone;
    }

    private void updateWagonLabel(int index) {
        Pane p = playersWagonCardPanes.get(index);
        Label l = (Label)  p.getChildren().get(1);
        l.setText(Integer.toString(presenter.getModel().getPLAYER_ARR()[0].getCards()[index]));
    }


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

    public void showRouteEndOption(){
        Pane p = routeCardPanes.get(3);
        p.setVisible(true);
    }

    private void hideRouteCards(ArrayList<Pane> routeCardPanes) {
        for (Pane p : routeCardPanes)
        {
            p.setVisible(false);
        }
    }

    public void updatePointLabels(){
        Player currP = presenter.getModel().getPlayer();
        int points = currP.getPoints();
        int pID = currP.getPlayerID();
        Label l = (Label) pointsPane.getChildren().get(pID * 2 + 1);
        l.setText(Integer.toString(points));

    }

}
