package com.resumeanalyser.ui;

import javax.swing.*;
import java.awt.*;

public class LoginPanel extends JPanel {

    private final AppContext ctx;
    private final JTextField email = UI.textField(20);
    private final JPasswordField password = UI.passwordField(20);
    private final JCheckBox show = new JCheckBox("Show password");
    private final JLabel error = UI.label(" ", UI.SMALL, UI.DANGER);
    private final JButton loginBtn = UI.button("Sign In", true);

    public LoginPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new GridLayout(1, 2));
        add(brandPanel());
        add(formPanel());
    }

    private JPanel brandPanel() {
        JPanel p = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setPaint(new GradientPaint(0, 0, UI.PRIMARY_DARK, 0, getHeight(), UI.SIDEBAR));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        JPanel box = new JPanel();
        box.setOpaque(false);
        box.setLayout(new BoxLayout(box, BoxLayout.Y_AXIS));
        box.add(UI.label("Resume Analyser", new Font("SansSerif", Font.BOLD, 34), Color.WHITE));
        box.add(Box.createVerticalStrut(12));
        box.add(UI.label("Smart resume screening for HR teams", new Font("SansSerif", Font.PLAIN, 17), new Color(191, 219, 254)));
        box.add(Box.createVerticalStrut(30));
        for (String s : new String[]{"Upload PDF, DOCX or TXT resumes", "Extract skills, education and experience",
                "Match candidates against job roles", "Rank and shortlist the best fit"}) {
            box.add(UI.label("\u2022  " + s, UI.BASE, Color.WHITE));
            box.add(Box.createVerticalStrut(8));
        }
        p.add(box);
        return p;
    }

    private JPanel formPanel() {
        JPanel outer = new JPanel(new GridBagLayout());
        outer.setBackground(Color.WHITE);

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(380, 430));

        JLabel title = UI.label("Welcome back", UI.H1, UI.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UI.label("Sign in to your HR dashboard", UI.BASE, UI.MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(26));
        form.add(UI.field("Email", email));
        form.add(Box.createVerticalStrut(14));
        form.add(UI.field("Password", password));
        form.add(Box.createVerticalStrut(8));

        show.setOpaque(false);
        show.setFont(UI.SMALL);
        show.setAlignmentX(Component.LEFT_ALIGNMENT);
        final char echo = password.getEchoChar();
        show.addActionListener(e -> password.setEchoChar(show.isSelected() ? (char) 0 : echo));
        form.add(show);
        form.add(Box.createVerticalStrut(6));

        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(error);
        form.add(Box.createVerticalStrut(10));

        loginBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(loginBtn);
        form.add(Box.createVerticalStrut(18));

        JPanel reg = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        reg.setOpaque(false);
        reg.setAlignmentX(Component.LEFT_ALIGNMENT);
        reg.add(UI.label("New here?  ", UI.BASE, UI.MUTED));
        JButton register = UI.linkButton("Create an account");
        register.addActionListener(e -> ctx.frame.showRegister());
        reg.add(register);
        form.add(reg);

        loginBtn.addActionListener(e -> doLogin());
        password.addActionListener(e -> doLogin());
        email.addActionListener(e -> password.requestFocusInWindow());

        outer.add(form);
        return outer;
    }

    public void reset(String prefillEmail) {
        email.setText(prefillEmail == null ? "" : prefillEmail);
        password.setText("");
        show.setSelected(false);
        password.setEchoChar('\u2022');
        error.setText(" ");
        loginBtn.setEnabled(true);
    }

    private void doLogin() {
        String mail = email.getText().trim();
        String pass = new String(password.getPassword());
        if (!UI.isValidEmail(mail)) { error.setText("Please enter a valid email address."); return; }
        if (pass.isEmpty()) { error.setText("Please enter your password."); return; }
        error.setText(" ");
        loginBtn.setEnabled(false);
        Async.run(this, () -> ctx.api.login(mail, pass),
                user -> {
                    ctx.setUser(user);
                    reset(null);
                    ctx.frame.showApp();
                },
                ex -> {
                    loginBtn.setEnabled(true);
                    error.setText(ex.getMessage());
                });
    }
}
