package mjtool.ui;


import javafx.scene.text.Font;

public class MJFontManager {
    public static Font sansSerif;
    public static Font sansSerifBold;
    public static Font serif;


    static {
        sansSerif = Font.loadFont("file:fonts/Montserrat-Light.ttf", 12);
        sansSerifBold = Font.loadFont("file:fonts/Montserrat-Bold.ttf", 13);
        serif = Font.loadFont("file:fonts/cmu.serif-roman.ttf", 45);
    }
}
