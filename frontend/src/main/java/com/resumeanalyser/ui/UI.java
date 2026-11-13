package com.resumeanalyser.ui;

import javax.swing.*;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.regex.Pattern;

/** Theme (blue / white / light-grey) and small component factories shared by all screens. */
public final class UI {

    public static final Color PRIMARY = new Color(37, 99, 235);
    public static final Color PRIMARY_DARK = new Color(29, 78, 216);
    public static final Color PRIMARY_LIGHT = new Color(219, 234, 254);
    public static final Color SIDEBAR = new Color(30, 58, 138);
    public static final Color SIDEBAR_ACTIVE = new Color(59, 100, 220);
    public static final Color BG = new Color(243, 244, 246);
    public static final Color TEXT = new Color(17, 24, 39);
    public static final Color MUTED = new Color(107, 114, 128);
    public static final Color BORDER = new Color(229, 231, 235);
    public static final Color SUCCESS = new Color(22, 163, 74);
    public static final Color DANGER = new Color(220, 38, 38);

    public static final Font BASE = new Font("SansSerif", Font.PLAIN, 14);
    public static final Font BOLD = new Font("SansSerif", Font.BOLD, 14);
    public static final Font SMALL = new Font("SansSerif", Font.PLAIN, 12);
    public static final Font H1 = new Font("SansSerif", Font.BOLD, 26);
    public static final Font H2 = new Font("SansSerif", Font.BOLD, 17);
    public static final Font BIG = new Font("SansSerif", Font.BOLD, 34);

    private static final Pattern EMAIL = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    private UI() { }

    // ------------------------------------------------------------------ validation

    public static boolean isValidEmail(String s) {
        return s != null && EMAIL.matcher(s.trim()).matches();
    }

    /** @return an error message, or null when the password is acceptable. */
    public static String passwordProblem(String p) {
        if (p == null || p.length() < 8) return "Password must be at least 8 characters.";
        boolean up = false, low = false, digit = false;
        for (char c : p.toCharArray()) {
            if (Character.isUpperCase(c)) up = true;
            else if (Character.isLowerCase(c)) low = true;
            else if (Character.isDigit(c)) digit = true;
        }
        return (up && low && digit) ? null : "Password needs an uppercase letter, a lowercase letter and a digit.";
    }

    // ------------------------------------------------------------------ formatting

    public static String years(double y) {
        if (y <= 0) return "Fresher (0 years)";
        String n = (y == Math.rint(y)) ? String.valueOf((long) y) : String.valueOf(y);
        return n + (y == 1.0 ? " year" : " years");
    }

    public static String percent(double v) {
        return String.format("%.1f%%", v);
    }

    public static String time(String iso) {
        if (iso == null) return "";
        String s = iso.replace('T', ' ');
        return s.length() > 16 ? s.substring(0, 16) : s;
    }

    public static String html(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    // ------------------------------------------------------------------ components

    public static JLabel label(String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        return l;
    }

    public static JButton button(String text, boolean primary) {
        final JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());
        b.setFont(BOLD);
        b.setFocusPainted(false);
        b.setOpaque(true);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        final Color base = primary ? PRIMARY : Color.WHITE;
        final Color hover = primary ? PRIMARY_DARK : PRIMARY_LIGHT;
        b.setBackground(base);
        b.setForeground(primary ? Color.WHITE : PRIMARY);
        b.setBorder(primary
                ? new EmptyBorder(10, 22, 10, 22)
                : new CompoundBorder(new LineBorder(PRIMARY, 1, true), new EmptyBorder(9, 21, 9, 21)));
        b.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { if (b.isEnabled()) b.setBackground(hover); }
            @Override public void mouseExited(MouseEvent e) { b.setBackground(base); }
        });
        return b;
    }

    /** Text-only link style button. */
    public static JButton linkButton(String text) {
        JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());
        b.setFont(BOLD);
        b.setForeground(PRIMARY);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorder(new EmptyBorder(4, 4, 4, 4));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    public static JButton navButton(String text) {
        final JButton b = new JButton(text);
        b.setUI(new BasicButtonUI());
        b.setFont(new Font("SansSerif", Font.BOLD, 14));
        b.setForeground(Color.WHITE);
        b.setBackground(SIDEBAR);
        b.setOpaque(true);
        b.setFocusPainted(false);
        b.setHorizontalAlignment(SwingConstants.LEFT);
        b.setBorder(new EmptyBorder(12, 22, 12, 22));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setMaximumSize(new Dimension(Integer.MAX_VALUE, 46));
        b.setAlignmentX(Component.LEFT_ALIGNMENT);
        return b;
    }

    private static CompoundBorder fieldBorder() {
        return new CompoundBorder(new LineBorder(new Color(209, 213, 219), 1, true), new EmptyBorder(8, 10, 8, 10));
    }

    public static JTextField textField(int columns) {
        JTextField f = new JTextField(columns);
        f.setFont(BASE);
        f.setBorder(fieldBorder());
        return f;
    }

    public static JPasswordField passwordField(int columns) {
        JPasswordField f = new JPasswordField(columns);
        f.setFont(BASE);
        f.setBorder(fieldBorder());
        f.setEchoChar('\u2022');
        return f;
    }

    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(Color.WHITE);
        p.setBorder(new CompoundBorder(new LineBorder(BORDER, 1, true), new EmptyBorder(16, 18, 16, 18)));
        return p;
    }

    /** Caption above a field, stacked vertically. */
    public static JPanel field(String caption, JComponent input) {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        JLabel l = label(caption, SMALL, MUTED);
        l.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setAlignmentX(Component.LEFT_ALIGNMENT);
        input.setMaximumSize(new Dimension(Integer.MAX_VALUE, 44));
        p.add(l);
        p.add(Box.createVerticalStrut(4));
        p.add(input);
        p.setAlignmentX(Component.LEFT_ALIGNMENT);
        return p;
    }

    public static DefaultTableModel readOnlyModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
    }

    public static void styleTable(JTable t) {
        t.setFont(BASE);
        t.setRowHeight(30);
        t.setShowVerticalLines(false);
        t.setGridColor(BORDER);
        t.setSelectionBackground(PRIMARY_LIGHT);
        t.setSelectionForeground(TEXT);
        t.setFillsViewportHeight(true);
        t.setIntercellSpacing(new Dimension(0, 1));
        JTableHeader h = t.getTableHeader();
        h.setFont(BOLD);
        h.setBackground(PRIMARY_LIGHT);
        h.setForeground(PRIMARY_DARK);
        h.setReorderingAllowed(false);
        h.setPreferredSize(new Dimension(100, 34));
    }

    public static JScrollPane scroll(JComponent c) {
        JScrollPane sp = new JScrollPane(c);
        sp.setBorder(new LineBorder(BORDER));
        sp.getViewport().setBackground(Color.WHITE);
        return sp;
    }

    /** Colours "Shortlisted" green and anything else red. */
    public static class StatusRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean sel, boolean focus, int row, int col) {
            Component c = super.getTableCellRendererComponent(table, value, sel, focus, row, col);
            boolean ok = "Shortlisted".equals(String.valueOf(value));
            setForeground(ok ? SUCCESS : DANGER);
            setFont(BOLD);
            return c;
        }
    }

    public static void centerColumn(JTable t, int column) {
        DefaultTableCellRenderer r = new DefaultTableCellRenderer();
        r.setHorizontalAlignment(SwingConstants.CENTER);
        t.getColumnModel().getColumn(column).setCellRenderer(r);
    }

    public static void error(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    public static void info(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Resume Analyser", JOptionPane.INFORMATION_MESSAGE);
    }
}
