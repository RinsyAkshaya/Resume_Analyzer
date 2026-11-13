package com.resumeanalyser.ui;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

/** Sidebar + content area shown after login. */
public class AppShell extends JPanel {

    private final AppContext ctx;
    private final CardLayout cards = new CardLayout();
    private final JPanel content = new JPanel(cards);
    private final Map<String, ScreenPanel> screens = new LinkedHashMap<>();
    private final Map<String, JButton> navButtons = new LinkedHashMap<>();

    private final DashboardPanel dashboard;
    private final UploadPanel upload;
    private final JobRolePanel jobs;
    private final AnalysisPanel results;
    private final RankingPanel ranking;

    public AppShell(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout());

        dashboard = new DashboardPanel(ctx);
        upload = new UploadPanel(ctx);
        jobs = new JobRolePanel(ctx);
        results = new AnalysisPanel(ctx);
        ranking = new RankingPanel(ctx);

        screens.put("dashboard", dashboard);
        screens.put("upload", upload);
        screens.put("jobs", jobs);
        screens.put("results", results);
        screens.put("ranking", ranking);
        for (Map.Entry<String, ScreenPanel> e : screens.entrySet()) content.add(e.getValue(), e.getKey());

        add(buildSidebar(), BorderLayout.WEST);
        add(content, BorderLayout.CENTER);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(UI.SIDEBAR);
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setPreferredSize(new Dimension(240, 100));

        JPanel brand = new JPanel();
        brand.setOpaque(false);
        brand.setLayout(new BoxLayout(brand, BoxLayout.Y_AXIS));
        brand.setBorder(BorderFactory.createEmptyBorder(24, 22, 20, 22));
        brand.setAlignmentX(Component.LEFT_ALIGNMENT);
        brand.add(UI.label("Resume Analyser", new Font("SansSerif", Font.BOLD, 20), Color.WHITE));
        brand.add(Box.createVerticalStrut(10));
        brand.add(UI.label(ctx.fullName, UI.BOLD, new Color(191, 219, 254)));
        brand.add(UI.label(ctx.email, UI.SMALL, new Color(147, 197, 253)));
        side.add(brand);

        addNav(side, "dashboard", "Dashboard");
        addNav(side, "upload", "Upload Resume");
        addNav(side, "jobs", "Job Roles & Analyse");
        addNav(side, "results", "Analysis Results");
        addNav(side, "ranking", "Candidate Ranking");

        side.add(Box.createVerticalGlue());
        JButton logout = UI.navButton("Logout");
        logout.setBackground(new Color(153, 27, 27));
        logout.addActionListener(e -> {
            int ok = JOptionPane.showConfirmDialog(this, "Do you want to log out?", "Logout", JOptionPane.YES_NO_OPTION);
            if (ok == JOptionPane.YES_OPTION) ctx.frame.logout();
        });
        side.add(logout);
        return side;
    }

    private void addNav(JPanel side, String key, String label) {
        JButton b = UI.navButton(label);
        b.addActionListener(e -> show(key));
        navButtons.put(key, b);
        side.add(b);
    }

    public void show(String key) {
        for (Map.Entry<String, JButton> e : navButtons.entrySet()) {
            e.getValue().setBackground(e.getKey().equals(key) ? UI.SIDEBAR_ACTIVE : UI.SIDEBAR);
        }
        cards.show(content, key);
        screens.get(key).onShow();
    }

    // ---- navigation helpers used by the screens

    public void openUpload(File fileToUpload) {
        upload.preselect(fileToUpload);
        show("upload");
    }

    public void openJobs(long resumeId) {
        jobs.preselectResume(resumeId);
        show("jobs");
    }

    public void openResults(long resumeId, long jobId) {
        results.preselect(resumeId, jobId);
        show("results");
    }

    public void openRanking(long jobId) {
        ranking.preselectJob(jobId);
        show("ranking");
    }
}
