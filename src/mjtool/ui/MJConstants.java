package mjtool.ui;

import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.paint.Color;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class MJConstants {
    public static final Color BLUE_GRAY = new Color(0.141, 0.180, 0.251, 1);
    public static final Color BLUE = new Color(0.087, 0.498, 0.640, 1);
    public static final Color YELLOW = new Color(1, 1, 0, 1);


    public static final Background DEFAULT_BACKGROUND = new Background(new BackgroundFill(Color.LIGHTGRAY, new CornerRadii(8), null));
    public static final Background HOVER_BACKGROUND = new Background(new BackgroundFill(Color.GRAY, new CornerRadii(8), null));
    public static final Background YELLOW_BACKGROUND = new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(8), null));
    public static final Background YELLOWHOVER_BACKGROUND = new Background(new BackgroundFill(Color.YELLOW, new CornerRadii(8), null));

    public static final ZoneOffset UTC_OFFSET = OffsetDateTime.now().getOffset();
    public static final DateTimeFormatter STD_FORMATTER = DateTimeFormatter.ofPattern("MMM dd, yyyy", Locale.getDefault());
}
