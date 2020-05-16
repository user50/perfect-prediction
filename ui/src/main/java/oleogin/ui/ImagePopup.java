package oleogin.ui;

import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class ImagePopup extends StackPane {


    private Pane context;
    private List<Node> nodes;

    public ImagePopup(Pane stackpane, ImageView imageView) {
        context = stackpane;
        this.nodes = new ArrayList<>(stackpane.getChildren());

        VBox box = new VBox(imageView);

        box.setFillWidth(true);
        box.setAlignment(Pos.CENTER);
//            box.setSpacing(40);
        box.setStyle("-fx-background-color: White; -fx-max-width: 50; -fx-max-height: 50;");
        getChildren().add(box);
        this.setAlignment(Pos.CENTER);
        this.setStyle("-fx-background-color: #88888855;");

        this.setOnMouseClicked(event -> {
            if (event.getClickCount() == 1)
                context.getChildren().remove(this);
                context.getChildren().addAll(nodes);
        });

    }

    public void show() {
        context.getChildren().clear();
        context.getChildren().add(this);
    }
}
