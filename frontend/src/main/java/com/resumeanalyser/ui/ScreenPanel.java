package com.resumeanalyser.ui;

import javax.swing.*;

/** Base class of the screens shown inside the application shell. */
public abstract class ScreenPanel extends JPanel {

    protected ScreenPanel() {
        setBackground(UI.BG);
    }

    /** Called every time the screen becomes visible: (re)load data from the backend here. */
    public abstract void onShow();
}
