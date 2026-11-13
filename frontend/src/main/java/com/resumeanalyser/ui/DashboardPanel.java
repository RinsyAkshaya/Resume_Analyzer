package com.resumeanalyser.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.List;

public class DashboardPanel extends ScreenPanel {

    private record Snapshot(JsonNode stats, JsonNode recent, JsonNode jobs) { }

    private final AppContext ctx;
    private final JLabel welcome = UI.label("Welcome", UI.H1, UI.TEXT);
    private final JLabel vAnalysed = bigValue();
    private final JLabel vAverage = bigValue();
    private final JLabel vShortlisted = bigValue();
    private final JLabel vUploaded = bigValue();
    private final JLabel lastUpload = UI.label("No resume uploaded in this session yet.", UI.BASE, UI.MUTED);
    private final JComboBox<Models.JobItem> roleCombo = new JComboBox<>();
    private final JLabel rankTitle = UI.label("Candidate Ranking", UI.H2, UI.TEXT);
    private final DefaultTableModel recentModel = UI.readOnlyModel("Candidate", "Job Role", "Match %", "Status", "Analysed");
    private final DefaultTableModel rankModel = UI.readOnlyModel("Rank", "Candidate", "Match %", "Status");
    private boolean loadingRoles = false;

    public DashboardPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout());
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel header = new JPanel();
        header.setOpaque(false);
        header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
        welcome.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel sub = UI.label("HR screening overview", UI.BASE, UI.MUTED);
        sub.setAlignmentX(Component.LEFT_ALIGNMENT);
        header.add(welcome);
        header.add(Box.createVerticalStrut(2));
        header.add(sub);
        header.setBorder(new EmptyBorder(0, 0, 18, 0));
        add(header, BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.gridx = 0;
        gc.weightx = 1;
        gc.fill = GridBagConstraints.BOTH;
        gc.insets = new Insets(0, 0, 16, 0);

        // stat cards
        JPanel stats = new JPanel(new GridLayout(1, 4, 16, 0));
        stats.setOpaque(false);
        stats.add(statCard("Total Resumes Analysed", vAnalysed));
        stats.add(statCard("Average Match Score", vAverage));
        stats.add(statCard("Shortlisted Candidates", vShortlisted));
        stats.add(statCard("Resumes Uploaded", vUploaded));
        gc.gridy = 0;
        body.add(stats, gc);

        // quick actions
        JPanel quick = UI.card();
        quick.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 6));
        JButton uploadBtn = UI.button("Upload Resume", false);
        uploadBtn.addActionListener(e -> chooseAndUpload());
        roleCombo.setFont(UI.BASE);
        roleCombo.setPreferredSize(new Dimension(220, 38));
        JButton analyseBtn = UI.button("Analyse Resume", true);
        analyseBtn.addActionListener(e -> analyseLast());
        roleCombo.addActionListener(e -> { if (!loadingRoles) loadRanking(); });
        quick.add(uploadBtn);
        quick.add(lastUpload);
        quick.add(Box.createHorizontalStrut(16));
        quick.add(UI.label("Job role:", UI.BOLD, UI.TEXT));
        quick.add(roleCombo);
        quick.add(analyseBtn);
        gc.gridy = 1;
        body.add(quick, gc);

        // tables
        JPanel tables = new JPanel(new GridLayout(1, 2, 16, 0));
        tables.setOpaque(false);

        JTable recentTable = new JTable(recentModel);
        UI.styleTable(recentTable);
        recentTable.getColumnModel().getColumn(3).setCellRenderer(new UI.StatusRenderer());
        UI.centerColumn(recentTable, 2);
        JPanel recentCard = UI.card();
        recentCard.setLayout(new BorderLayout(0, 10));
        recentCard.add(UI.label("Recent Analysis Results", UI.H2, UI.TEXT), BorderLayout.NORTH);
        recentCard.add(UI.scroll(recentTable), BorderLayout.CENTER);

        JTable rankTable = new JTable(rankModel);
        UI.styleTable(rankTable);
        rankTable.getColumnModel().getColumn(3).setCellRenderer(new UI.StatusRenderer());
        UI.centerColumn(rankTable, 0);
        UI.centerColumn(rankTable, 2);
        rankTable.getColumnModel().getColumn(0).setMaxWidth(60);
        JPanel rankCard = UI.card();
        rankCard.setLayout(new BorderLayout(0, 10));
        JPanel rankHead = new JPanel(new BorderLayout());
        rankHead.setOpaque(false);
        rankHead.add(rankTitle, BorderLayout.WEST);
        JButton full = UI.linkButton("View full ranking");
        full.addActionListener(e -> {
            Models.JobItem job = (Models.JobItem) roleCombo.getSelectedItem();
            if (job != null) ctx.shell.openRanking(job.id());
        });
        rankHead.add(full, BorderLayout.EAST);
        rankCard.add(rankHead, BorderLayout.NORTH);
        rankCard.add(UI.scroll(rankTable), BorderLayout.CENTER);

        tables.add(recentCard);
        tables.add(rankCard);
        gc.gridy = 2;
        gc.weighty = 1;
        gc.insets = new Insets(0, 0, 0, 0);
        body.add(tables, gc);

        add(body, BorderLayout.CENTER);
    }

    private static JLabel bigValue() {
        return UI.label("-", UI.BIG, UI.PRIMARY);
    }

    private JPanel statCard(String title, JLabel value) {
        JPanel c = UI.card();
        c.setLayout(new BoxLayout(c, BoxLayout.Y_AXIS));
        JLabel t = UI.label(title, UI.SMALL, UI.MUTED);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        value.setAlignmentX(Component.LEFT_ALIGNMENT);
        c.add(t);
        c.add(Box.createVerticalStrut(6));
        c.add(value);
        return c;
    }

    private void chooseAndUpload() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a resume (PDF, DOCX or TXT)");
        chooser.setFileFilter(new FileNameExtensionFilter("Resumes (*.pdf, *.docx, *.txt)", "pdf", "docx", "txt"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File f = chooser.getSelectedFile();
            ctx.shell.openUpload(f);
        }
    }

    private void analyseLast() {
        if (ctx.lastResumeId == null) {
            UI.error(this, "Upload a resume first (Upload Resume), or open 'Job Roles & Analyse' to pick an existing one.");
            return;
        }
        Models.JobItem job = (Models.JobItem) roleCombo.getSelectedItem();
        if (job == null) {
            UI.error(this, "Please select a job role.");
            return;
        }
        final long resumeId = ctx.lastResumeId;
        final long jobId = job.id();
        Async.run(this, () -> ctx.api.analyse(resumeId, jobId), r -> ctx.shell.openResults(resumeId, jobId));
    }

    @Override
    public void onShow() {
        welcome.setText("Welcome back, " + ctx.fullName);
        lastUpload.setText(ctx.lastResumeId == null
                ? "No resume uploaded in this session yet."
                : "Last uploaded: " + ctx.lastResumeLabel);
        Async.run(this, () -> new Snapshot(ctx.api.stats(), ctx.api.recentResults(8), ctx.api.jobs()), snap -> {
            vAnalysed.setText(snap.stats().path("resumesAnalysed").asText("0"));
            vAverage.setText(UI.percent(snap.stats().path("averageMatchScore").asDouble()));
            vShortlisted.setText(snap.stats().path("shortlistedCandidates").asText("0"));
            vUploaded.setText(snap.stats().path("totalResumes").asText("0"));

            recentModel.setRowCount(0);
            for (JsonNode r : snap.recent()) {
                recentModel.addRow(new Object[]{r.path("candidateName").asText(), r.path("roleName").asText(),
                        UI.percent(r.path("matchScore").asDouble()), r.path("status").asText(),
                        UI.time(r.path("analyzedAt").asText())});
            }

            Models.JobItem previous = (Models.JobItem) roleCombo.getSelectedItem();
            List<Models.JobItem> jobs = Models.jobs(snap.jobs());
            loadingRoles = true;
            roleCombo.removeAllItems();
            int select = 0;
            for (int i = 0; i < jobs.size(); i++) {
                roleCombo.addItem(jobs.get(i));
                if (previous != null && previous.id() == jobs.get(i).id()) select = i;
            }
            if (!jobs.isEmpty()) roleCombo.setSelectedIndex(select);
            loadingRoles = false;
            loadRanking();
        });
    }

    private void loadRanking() {
        Models.JobItem job = (Models.JobItem) roleCombo.getSelectedItem();
        if (job == null) return;
        Async.run(this, () -> ctx.api.ranking(job.id()), nodes -> {
            rankTitle.setText("Candidate Ranking - " + job.name());
            rankModel.setRowCount(0);
            int shown = 0;
            for (JsonNode n : nodes) {
                if (shown++ >= 5) break;
                rankModel.addRow(new Object[]{n.path("rank").asInt(), n.path("candidateName").asText(),
                        UI.percent(n.path("matchScore").asDouble()), n.path("status").asText()});
            }
        });
    }
}
