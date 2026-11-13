package com.resumeanalyser.ui;

import javax.swing.*;
import java.awt.*;

public class RegisterPanel extends JPanel {

    private final AppContext ctx;
    private final JTextField fullName = UI.textField(20);
    private final JTextField email = UI.textField(20);
    private final JPasswordField password = UI.passwordField(20);
    private final JPasswordField confirm = UI.passwordField(20);
    private final JCheckBox show = new JCheckBox("Show passwords");
    private final JLabel error = UI.label(" ", UI.SMALL, UI.DANGER);
    private final JButton registerBtn = UI.button("Create Account", true);

    public RegisterPanel(AppContext ctx) {
        this.ctx = ctx;
        setBackground(Color.WHITE);
        setLayout(new GridBagLayout());

        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));
        form.setPreferredSize(new Dimension(420, 600));

        JLabel title = UI.label("Create your account", UI.H1, UI.TEXT);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UI.label("Register to start screening resumes", UI.BASE, UI.MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(title);
        form.add(Box.createVerticalStrut(4));
        form.add(sub);
        form.add(Box.createVerticalStrut(22));
        form.add(UI.field("Full Name", fullName));
        form.add(Box.createVerticalStrut(12));
        form.add(UI.field("Email", email));
        form.add(Box.createVerticalStrut(12));
        form.add(UI.field("Password (min 8 chars, upper + lower case + digit)", password));
        form.add(Box.createVerticalStrut(12));
        form.add(UI.field("Confirm Password", confirm));
        form.add(Box.createVerticalStrut(8));

        show.setOpaque(false);
        show.setFont(UI.SMALL);
        show.setAlignmentX(Component.LEFT_ALIGNMENT);
        final char echo = password.getEchoChar();
        show.addActionListener(e -> {
            char c = show.isSelected() ? (char) 0 : echo;
            password.setEchoChar(c);
            confirm.setEchoChar(c);
        });
        form.add(show);
        form.add(Box.createVerticalStrut(6));
        error.setAlignmentX(Component.LEFT_ALIGNMENT);
        form.add(error);
        form.add(Box.createVerticalStrut(10));

        registerBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        registerBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        form.add(registerBtn);
        form.add(Box.createVerticalStrut(16));

        JPanel back = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        back.setOpaque(false);
        back.setAlignmentX(Component.LEFT_ALIGNMENT);
        back.add(UI.label("Already registered?  ", UI.BASE, UI.MUTED));
        JButton login = UI.linkButton("Back to sign in");
        login.addActionListener(e -> ctx.frame.showLogin(null));
        back.add(login);
        form.add(back);

        registerBtn.addActionListener(e -> doRegister());
        confirm.addActionListener(e -> doRegister());
        add(form);
    }

    public void reset() {
        fullName.setText("");
        email.setText("");
        password.setText("");
        confirm.setText("");
        show.setSelected(false);
        password.setEchoChar('\u2022');
        confirm.setEchoChar('\u2022');
        error.setText(" ");
        registerBtn.setEnabled(true);
    }

    private void doRegister() {
        String name = fullName.getText().trim();
        String mail = email.getText().trim();
        String pass = new String(password.getPassword());
        String conf = new String(confirm.getPassword());

        if (name.length() < 2) { error.setText("Please enter your full name."); return; }
        if (!UI.isValidEmail(mail)) { error.setText("Please enter a valid email address."); return; }
        String problem = UI.passwordProblem(pass);
        if (problem != null) { error.setText(problem); return; }
        if (!pass.equals(conf)) { error.setText("Password and confirm password do not match."); return; }

        error.setText(" ");
        registerBtn.setEnabled(false);
        Async.run(this, () -> ctx.api.register(name, mail, pass, conf),
                user -> {
                    UI.info(this, "Account created successfully. Please sign in.");
                    ctx.frame.showLogin(mail);
                },
                ex -> {
                    registerBtn.setEnabled(true);
                    error.setText(ex.getMessage());
                });
    }
}
