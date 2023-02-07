package sample;

import javafx.application.Application;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.Stage;


import java.util.ArrayList;

public class Main extends Application {

    private ArrayList<Pane> panes = new ArrayList<>();
    private static String ID_PREFIX = "#Route";
    ///testttt
    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("sample.fxml"));
        primaryStage.setTitle("Hello World");

        Scene s = new Scene(root);

        insertRoutePanes(s);
        setMouseClicksLister();



        primaryStage.setScene(s);
        primaryStage.show();
    }

    private void changeColor(Pane pane, Color color){

        for (int i = 0; i < pane.getChildren().size(); i++) {
            Rectangle r = (Rectangle) pane.getChildren().get(i);
            r.setFill(color );

        }
    }

    private void insertRoutePanes(Scene s) {
        String str, numPref ;
        Pane p = (Pane) s.lookup(ID_PREFIX+"00");
        panes.add(p);
        for (int i = 1; i < 25; i++) {
            numPref = "";
            if(i < 10)
                numPref = "0";
            str = ID_PREFIX + numPref + i;

            p = (Pane) s.lookup(str);
            panes.add(p);
        }
    }

    private void setMouseClicksLister() {
        for (Pane p:panes
             ) {
            p.setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent mouseEvent) {
                    Pane pane = (Pane)mouseEvent.getSource();

                    String Route = pane.getId();
                    System.out.println( Route + " clicked");

                    changeColor(pane, Color.RED);

                    //((Pane) mouseEvent.getSource()).setStyle("fx-background-color: #FF0000");
                }
            });
        }

    }




    public static void main(String[] args) {
        launch(args);
    }
}
