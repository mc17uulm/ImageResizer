package de.uulm.mc17.main;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        Parent root = FXMLLoader.load(getClass().getResource("style.fxml"));

        try{
            primaryStage.getIcons().add(new Image("file:resources/icon.png"));
        } catch (IllegalArgumentException e){
            e.printStackTrace();
        }

        primaryStage.setTitle("ImageResizer 0.1");
        Scene scene = new Scene(root, 655, 635);
        primaryStage.setScene(scene);

        primaryStage.setOnCloseRequest(e -> Platform.exit());

        primaryStage.show();
    }


    public static void main(String[] args) {
        //System.out.print(com.sun.javafx.runtime.VersionInfo.getRuntimeVersion());
        launch(args);
    }
}
