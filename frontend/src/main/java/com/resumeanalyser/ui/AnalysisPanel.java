package com.resumeanalyser.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.html.HTMLDocument;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AnalysisPanel extends ScreenPanel {

    private final AppContext ctx;
    private final JComboBox<Models.ResumeItem> resumeCombo = new JComboBox<>();
    private final DefaultTableModel tableModel = UI.readOnlyModel("Job Role", "Match %", "Status", "Analysed");
    private final JTable table = new JTable(tableModel);
    private final List<JsonNode> rows = new ArrayList<>();

    private final JLabel scoreLabel = UI.label("-", new Font("SansSerif", Font.BOLD, 46), UI.PRIMARY);
    private final JLabel statusLabel = UI.label(" ", new Font("SansSerif", Font.BOLD, 16), UI.MUTED);
    private final JProgressBar bar = new JProgressBar(0, 100);
    private final JEditorPane detail = new JEditorPane();

    private Long pendingResume;
    private Long pendingJob;
    private boolean loading;

    public AnalysisPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel t = UI.label("Analysis Results", UI.H1, UI.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = UI.label("Stored analyses for a resume. Select a row to see the full breakdown.", UI.BASE, UI.MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(t);
        head.add(Box.createVerticalStrut(2));
        head.add(s);

        JPanel pick = UI.card();
        pick.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));
        resumeCombo.setFont(UI.BASE);
        resumeCombo.setPreferredSize(new Dimension(420, 38));
        resumeCombo.addActionListener(e -> { if (!loading) loadResults(null); });
        JButton refresh = UI.button("Refresh", false);
        refresh.addActionListener(e -> onShow());
        pick.add(UI.label("Resume:", UI.BOLD, UI.TEXT));
        pick.add(resumeCombo);
        pick.add(refresh);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(pick, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // left: list of analyses
        UI.styleTable(table);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.getColumnModel().getColumn(2).setCellRenderer(new UI.StatusRenderer());
        UI.centerColumn(table, 1);
        table.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) showSelected();
        });
        JPanel left = UI.card();
        left.setLayout(new BorderLayout(0, 10));
        left.add(UI.label("Analyses of this resume", UI.H2, UI.TEXT), BorderLayout.NORTH);
        left.add(UI.scroll(table), BorderLayout.CENTER);
        left.setPreferredSize(new Dimension(470, 100));

        // right: breakdown
        bar.setStringPainted(true);
        bar.setForeground(UI.PRIMARY);
        bar.setBackground(UI.BORDER);
        bar.setPreferredSize(new Dimension(100, 22));
        detail.setContentType("text/html");
        detail.setEditable(false);
        detail.setBackground(Color.WHITE);
        ((HTMLDocument) detail.getDocument()).getStyleSheet().addRule("body { font-family: SansSerif; font-size: 13pt; }");

        JPanel scoreRow = new JPanel(new BorderLayout(16, 0));
        scoreRow.setOpaque(false);
        scoreRow.add(scoreLabel, BorderLayout.WEST);
        JPanel barBox = new JPanel();
        barBox.setOpaque(false);
        barBox.setLayout(new BoxLayout(barBox, BoxLayout.Y_AXIS));
        statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        bar.setAlignmentX(Component.LEFT_ALIGNMENT);
        barBox.add(Box.createVerticalStrut(8));
        barBox.add(statusLabel);
        barBox.add(Box.createVerticalStrut(6));
        barBox.add(bar);
        scoreRow.add(barBox, BorderLayout.CENTER);

        JPanel right = UI.card();
        right.setLayout(new BorderLayout(0, 12));
        right.add(scoreRow, BorderLayout.NORTH);
        right.add(UI.scroll(detail), BorderLayout.CENTER);

        JPanel center = new JPanel(new BorderLayout(16, 0));
        center.setOpaque(false);
        center.add(left, BorderLayout.WEST);
        center.add(right, BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
        clearDetail();
    }

    public void preselect(long resumeId, long jobId) {
        pendingResume = resumeId;
        pendingJob = jobId;
    }

    @Override
    public void onShow() {
        Async.run(this, () -> ctx.api.resumes(), nodes -> {
            loading = true;
            Models.ResumeItem previous = (Models.ResumeItem) resumeCombo.getSelectedItem();
            Long wanted = pendingResume != null ? pendingResume
                    : (previous != null ? Long.valueOf(previous.id()) : ctx.lastResumeId);
            resumeCombo.removeAllItems();
            List<Models.ResumeItem> resumes = Models.resumes(nodes);
            int select = 0;
            for (int i = 0; i < resumes.size(); i++) {
                resumeCombo.addItem(resumes.get(i));
                if (wanted != null && wanted == resumes.get(i).id()) select = i;
            }
            if (!resumes.isEmpty()) resumeCombo.setSelectedIndex(select);
            loading = false;
            Long job = pendingJob;
            pendingResume = null;
            pendingJob = null;
            loadResults(job);
        });
    }

    private void loadResults(Long selectJobId) {
        Models.ResumeItem resume = (Models.ResumeItem) resumeCombo.getSelectedItem();
        tableModel.setRowCount(0);
        rows.clear();
        if (resume == null) {
            clearDetail();
            return;
        }
        Async.run(this, () -> ctx.api.results(resume.id()), nodes -> {
            int select = 0;
            int i = 0;
            for (JsonNode n : nodes) {
                rows.add(n);
                tableModel.addRow(new Object[]{n.path("roleName").asText(), UI.percent(n.path("matchScore").asDouble()),
                        n.path("status").asText(), UI.time(n.path("analyzedAt").asText())});
                if (selectJobId != null && n.path("jobId").asLong() == selectJobId) select = i;
                i++;
            }
            if (rows.isEmpty()) {
                clearDetail();
            } else {
                table.setRowSelectionInterval(select, select);
            }
        });
    }

    private void showSelected() {
        int r = table.getSelectedRow();
        if (r < 0 || r >= rows.size()) return;
        JsonNode n = rows.get(r);
        double score = n.path("matchScore").asDouble();
        boolean shortlisted = n.path("shortlisted").asBoolean();
        scoreLabel.setText(UI.percent(score));
        statusLabel.setText(n.path("status").asText());
        statusLabel.setForeground(shortlisted ? UI.SUCCESS : UI.DANGER);
        bar.setValue((int) Math.round(score));
        bar.setString(UI.percent(score));
        bar.setForeground(shortlisted ? UI.SUCCESS : UI.PRIMARY);
        detail.setText(buildHtml(n));
        detail.setCaretPosition(0);
    }

    private void clearDetail() {
        scoreLabel.setText("-");
        statusLabel.setText(" ");
        bar.setValue(0);
        bar.setString("");
        detail.setText("<html><body><p style='color:#6b7280'>No analysis to show yet. Upload a resume and run an analysis "
                + "from 'Job Roles &amp; Analyse'.</p></body></html>");
    }

    private String buildHtml(JsonNode n) {
        List<String> matched = list(n.path("matchedSkills"));
        List<String> missing = list(n.path("missingSkills"));
        int total = n.path("requiredSkills").size();
        StringBuilder sb = new StringBuilder("<html><body>");
        sb.append("<h2 style='color:#1d4ed8'>").append(UI.html(n.path("candidateName").asText())).append("</h2>");
        sb.append("<table cellpadding='3'>");
        row(sb, "Email", n.path("email").asText("-"));
        row(sb, "Applied role", n.path("roleName").asText());
        row(sb, "Education", n.path("education").asText("-"));
        row(sb, "Experience", UI.years(n.path("experienceYears").asDouble()));
        row(sb, "Status", n.path("status").asText());
        sb.append("</table>");
        sb.append("<p><b>Matched Skills (").append(matched.size()).append(")</b><br>")
                .append(colored(matched, "#16a34a", "none")).append("</p>");
        sb.append("<p><b>Missing Skills (").append(missing.size()).append(")</b><br>")
                .append(colored(missing, "#dc2626", "none - all required skills found")).append("</p>");
        sb.append("<p><b>Required Skills</b><br>").append(UI.html(Models.join(n.path("requiredSkills"), ", "))).append("</p>");
        sb.append("<p style='color:#6b7280'>Match Score = (").append(matched.size()).append(" / ").append(total)
                .append(") &times; 100 = ").append(UI.percent(n.path("matchScore").asDouble())).append("</p>");
        sb.append("</body></html>");
        return sb.toString();
    }

    private static void row(StringBuilder sb, String k, String v) {
        sb.append("<tr><td><font color='#6b7280'>").append(UI.html(k)).append("</font></td><td>")
                .append(UI.html(v)).append("</td></tr>");
    }

    private static List<String> list(JsonNode array) {
        List<String> out = new ArrayList<>();
        for (JsonNode n : array) out.add(n.asText());
        return out;
    }

    private static String colored(List<String> items, String color, String empty) {
        if (items.isEmpty()) return "<font color='#6b7280'>" + UI.html(empty) + "</font>";
        return "<font color='" + color + "'>" + UI.html(String.join(", ", items)) + "</font>";
    }
}
