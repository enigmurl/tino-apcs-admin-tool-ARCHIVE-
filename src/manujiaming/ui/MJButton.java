package manujiaming.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.image.Image;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.io.File;

public class MJButton extends Button {

    public MJButton(String text) {
        super();

        this.setText(text);
        this.init();
    }

    public MJButton(String imageName, int prefSize) {
        this.init();

        ImageView image = new ImageView(new Image(new File("imgs/" + imageName).toURI().toString()));

        this.setGraphic(image);
        this.setPrefSize(prefSize, prefSize);
    }


    private void init() {
        this.setFont(MJFontManager.sansSerifBold);
        this.setAlignment(Pos.CENTER);

        this.setBackground(MJConstants.DEFAULT_BACKGROUND);

        this.setOnMouseEntered((event) -> {
            if (this.getBackground() == MJConstants.DEFAULT_BACKGROUND) {
                this.setBackground(MJConstants.HOVER_BACKGROUND);
            } else if (this.getBackground() == MJConstants.YELLOW_BACKGROUND) {
                this.setBackground(MJConstants.YELLOWHOVER_BACKGROUND);
            }
        });
        this.setOnMouseExited((event) -> {
            if (this.getBackground() == MJConstants.HOVER_BACKGROUND) {
                this.setBackground(MJConstants.DEFAULT_BACKGROUND);
            } else if (this.getBackground() == MJConstants.YELLOWHOVER_BACKGROUND) {
                this.setBackground(MJConstants.YELLOW_BACKGROUND);
            }
        });
    }
}
