package manujiaming.ui;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;


public class MJLabel extends Label {


    public MJLabel(String text, Font font) {
        super();

        this.setText(text);
        this.setFont(font);
        this.setTextFill(Color.WHITE);

        this.setAlignment(Pos.CENTER);

        this.setWrapText(true);
    }

    public MJLabel(String text) {
        super();

        this.setText(text);
        this.setTextFill(Color.WHITE);
        this.setAlignment(Pos.CENTER);

        this.setWrapText(true);
    }


    public void setWidth(int pixels) {
        this.setMinWidth(pixels);
        this.setMaxWidth(pixels);
        this.setPrefWidth(pixels);
    }
}
