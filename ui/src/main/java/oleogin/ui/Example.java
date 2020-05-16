package oleogin.ui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

public class Example extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Parent root = FXMLLoader.load(getClass().getResource("/morda.fxml"));

        Scene scene = new Scene(root);
        scene.getStylesheets().add(getClass().getResource("/stylesV2.css").toExternalForm());

        stage.setTitle("Perfect prediction");
        stage.setScene(scene);

        stage.setWidth(Screen.getPrimary().getVisualBounds().getWidth());
        stage.setHeight(Screen.getPrimary().getVisualBounds().getHeight());


        stage.show();
    }


}
