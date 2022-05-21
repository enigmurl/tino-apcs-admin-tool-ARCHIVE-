package manujiaming.ui;

import com.sun.javafx.tk.FontLoader;
import com.sun.javafx.tk.Toolkit;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import manujiaming.MJSubmission;
import manujiaming.MJTool;

import javax.security.sasl.SaslServer;

public class MJHeader extends VBox {

    public MJHeader(int initialOffset, int[] widths, String[] headers) {
        super(0);
        //field for expanding submissions
        //the actual name and student ID
        //autograder comments
        this.setAlignment(Pos.CENTER);

        Line l1 = new Line(0,0,0,0);
        l1.endXProperty().bind(this.widthProperty());

        l1.setStroke(Color.WHITE);

        HBox hb = getMain(initialOffset, widths, headers);
        hb.setAlignment(Pos.CENTER_LEFT);

        this.getChildren().addAll(l1, hb);
        this.setBackground(new Background(new BackgroundFill(MJConstants.BLUE_GRAY, null, null)));
    }

    public HBox getMain(int initialOffset, int[] widths, String[] headers) {
        HBox b = new HBox();

        FontLoader fontLoader = Toolkit.getToolkit().getFontLoader();

        for (int i = 0; i < widths.length; i++) {
            MJLabel m = new MJLabel(headers[i], MJFontManager.sansSerifBold);
            m.setTextFill(Color.LIGHTYELLOW);
            int lOff = i == 0 ? initialOffset : 0;

            double width = fontLoader.computeStringWidth(m.getText(), m.getFont());

            HBox.setMargin(m, new Insets(2, (widths[i] - width) / 2, 2, (widths[i] - width) / 2 + lOff));
            b.getChildren().add(m);
        }

        return b;
    }
}
