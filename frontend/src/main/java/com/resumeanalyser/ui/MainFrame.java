package com.resumeanalyser.ui;

import javax.swing.*;
import java.awt.*;

/** Top-level window: Login / Register / Application shell. */
public class MainFrame extends JFrame {

    private final AppContext ctx;
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private final LoginPanel loginPanel;
    private final RegisterPanel registerPanel;
    private AppShell appShell;

    public MainFrame(AppContext ctx) {
        super("Resume Analyser - HR Screening");
        this.ctx = ctx;
        ctx.frame = this;

        loginPanel = new LoginPanel(ctx);
        registerPanel = new RegisterPanel(ctx);
        root.add(loginPanel, "LOGIN");
        root.add(registerPanel, "REGISTER");

        setContentPane(root);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1100, 720));
        setSize(1240, 780);
        setLocationRelativeTo(null);
    }

    public void showLogin(String prefillEmail) {
        loginPanel.reset(prefillEmail);
        cards.show(root, "LOGIN");
    }

    public void showRegister() {
        registerPanel.reset();
        cards.show(root, "REGISTER");
    }

    /** Called after a successful login. A fresh shell is created so no data of a previous user remains. */
    public void showApp() {
        if (appShell != null) root.remove(appShell);
        appShell = new AppShell(ctx);
        ctx.shell = appShell;
        root.add(appShell, "APP");
        cards.show(root, "APP");
        appShell.show("dashboard");
    }

    public void logout() {
        ctx.clear();
        ctx.shell = null;
        if (appShell != null) {
            root.remove(appShell);
            appShell = null;
        }
        showLogin(null);
    }
}
