package manujiaming.ui;

import javafx.scene.control.ComboBox;

public class MJComboBox<T extends Object> extends ComboBox<T> {

    public MJComboBox() {
        super();

        this.setBackground(MJConstants.DEFAULT_BACKGROUND);
        this.getEditor().setFont(MJFontManager.sansSerifBold);
    }
}
