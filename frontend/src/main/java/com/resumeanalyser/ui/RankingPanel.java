package com.resumeanalyser.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class RankingPanel extends ScreenPanel {

    private final AppContext ctx;
    private final JComboBox<Models.JobItem> roleCombo = new JComboBox<>();
    private final DefaultTableModel liveModel = UI.readOnlyModel("Rank", "Candidate", "Match %", "Matched Skills", "Missing Skills", "Experience", "Education", "Status");
    private final JLabel liveNote = UI.label(" ", UI.SMALL, UI.MUTED);
    private Long pendingJob;
    private boolean loading;

    public RankingPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel t = UI.label("Candidate Ranking", UI.H1, UI.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = UI.label("Candidates in descending order of match score for the selected job role.", UI.BASE, UI.MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(t);
        head.add(Box.createVerticalStrut(2));
        head.add(s);

        JPanel pick = UI.card();
        pick.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));
        roleCombo.setFont(UI.BASE);
        roleCombo.setPreferredSize(new Dimension(260, 38));
        roleCombo.addActionListener(e -> { if (!loading) loadRanking(); });
        JButton refresh = UI.button("Refresh", false);
        refresh.addActionListener(e -> loadRanking());
        pick.add(UI.label("Job role:", UI.BOLD, UI.TEXT));
        pick.add(roleCombo);
        pick.add(refresh);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(pick, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UI.BOLD);
        tabs.addTab("Live Ranking (calculated)", liveTab());
        tabs.addTab("PPT Illustrative Sample", sampleTab());
        add(tabs, BorderLayout.CENTER);
    }

    private JPanel liveTab() {
        JTable table = new JTable(liveModel);
        UI.styleTable(table);
        table.getColumnModel().getColumn(7).setCellRenderer(new UI.StatusRenderer());
        UI.centerColumn(table, 0);
        UI.centerColumn(table, 2);
        table.getColumnModel().getColumn(0).setMaxWidth(60);
        table.getColumnModel().getColumn(2).setMaxWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(140);

        JPanel p = UI.card();
        p.setLayout(new BorderLayout(0, 8));
        p.add(UI.scroll(table), BorderLayout.CENTER);
        p.add(liveNote, BorderLayout.SOUTH);
        return p;
    }

    private JPanel sampleTab() {
        DefaultTableModel m = UI.readOnlyModel("Rank", "Candidate", "Profile", "Experience", "Education", "Match % (PPT)");
        m.addRow(new Object[]{1, "Candidate D", "Java, Hibernate, MySQL", "4 years", "M.Tech CSE", "91%"});
        m.addRow(new Object[]{2, "Candidate A", "Java, Spring Boot, SQL", "2 years", "B.Tech CSE", "82%"});
        m.addRow(new Object[]{3, "Candidate E", "C++, DSA, Git", "0 years", "B.Tech CSE", "56%"});
        m.addRow(new Object[]{4, "Candidate C", "HTML, CSS, JavaScript", "3 years", "B.Sc CS", "48%"});
        m.addRow(new Object[]{5, "Candidate B", "Python, ML, Pandas", "1 year", "B.Tech IT", "35%"});
        JTable table = new JTable(m);
        UI.styleTable(table);
        UI.centerColumn(table, 0);
        UI.centerColumn(table, 5);
        table.getColumnModel().getColumn(0).setMaxWidth(60);

        JPanel p = UI.card();
        p.setLayout(new BorderLayout(0, 8));
        p.add(UI.scroll(table), BorderLayout.CENTER);
        p.add(UI.label("Illustrative demonstration figures taken from the project PPT (not calculated). "
                + "Uploaded and seeded resumes are scored by the real skill-matching formula in the Live Ranking tab.",
                UI.SMALL, UI.MUTED), BorderLayout.SOUTH);
        return p;
    }

    public void preselectJob(long jobId) {
        pendingJob = jobId;
    }

    @Override
    public void onShow() {
        Async.run(this, () -> ctx.api.jobs(), nodes -> {
            loading = true;
            Models.JobItem previous = (Models.JobItem) roleCombo.getSelectedItem();
            Long wanted = pendingJob != null ? pendingJob : (previous != null ? Long.valueOf(previous.id()) : null);
            roleCombo.removeAllItems();
            List<Models.JobItem> jobs = Models.jobs(nodes);
            int select = 0;
            for (int i = 0; i < jobs.size(); i++) {
                roleCombo.addItem(jobs.get(i));
                if (wanted != null && wanted == jobs.get(i).id()) select = i;
            }
            if (!jobs.isEmpty()) roleCombo.setSelectedIndex(select);
            pendingJob = null;
            loading = false;
            loadRanking();
        });
    }

    private void loadRanking() {
        Models.JobItem job = (Models.JobItem) roleCombo.getSelectedItem();
        if (job == null) return;
        Async.run(this, () -> ctx.api.ranking(job.id()), nodes -> {
            liveModel.setRowCount(0);
            for (JsonNode n : nodes) {
                liveModel.addRow(new Object[]{n.path("rank").asInt(), n.path("candidateName").asText(),
                        UI.percent(n.path("matchScore").asDouble()), Models.join(n.path("matchedSkills"), ", "),
                        Models.join(n.path("missingSkills"), ", "), UI.years(n.path("experienceYears").asDouble()),
                        n.path("education").asText(""), n.path("status").asText()});
            }
            liveNote.setText(liveModel.getRowCount() == 0
                    ? "No resume has been analysed for " + job.name() + " yet."
                    : liveModel.getRowCount() + " candidate(s) ranked for " + job.name() + ". Shortlisted when the match score is at least the server threshold (default 50%).");
        });
    }
}
