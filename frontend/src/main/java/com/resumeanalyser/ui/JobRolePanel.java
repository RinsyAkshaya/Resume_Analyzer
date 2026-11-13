package com.resumeanalyser.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class JobRolePanel extends ScreenPanel {

    private record Data(JsonNode jobs, JsonNode resumes) { }

    private final AppContext ctx;
    private final JComboBox<Models.ResumeItem> resumeCombo = new JComboBox<>();
    private final DefaultListModel<Models.JobItem> jobModel = new DefaultListModel<>();
    private final JList<Models.JobItem> jobList = new JList<>(jobModel);
    private final JEditorPane roleInfo = new JEditorPane();
    private final JLabel candidateInfo = UI.label(" ", UI.BASE, UI.MUTED);
    private final JButton analyseBtn = UI.button("Analyse Resume", true);
    private Long pendingResumeId;
    private boolean loading;

    public JobRolePanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel t = UI.label("Job Role Selection", UI.H1, UI.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = UI.label("Choose a resume and the role to match it against.", UI.BASE, UI.MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(t);
        head.add(Box.createVerticalStrut(2));
        head.add(s);

        JPanel pick = UI.card();
        pick.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));
        resumeCombo.setFont(UI.BASE);
        resumeCombo.setPreferredSize(new Dimension(420, 38));
        resumeCombo.addActionListener(e -> { if (!loading) updateCandidateInfo(); });
        pick.add(UI.label("Resume:", UI.BOLD, UI.TEXT));
        pick.add(resumeCombo);
        pick.add(candidateInfo);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(pick, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // roles list + required skills
        jobList.setFont(new Font("SansSerif", Font.BOLD, 15));
        jobList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jobList.setFixedCellHeight(44);
        jobList.setSelectionBackground(UI.PRIMARY_LIGHT);
        jobList.setSelectionForeground(UI.PRIMARY_DARK);
        jobList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean sel, boolean focus) {
                JLabel l = (JLabel) super.getListCellRendererComponent(list, value, index, sel, focus);
                l.setBorder(new EmptyBorder(0, 14, 0, 14));
                return l;
            }
        });
        jobList.addListSelectionListener(e -> { if (!e.getValueIsAdjusting()) showRole(); });

        JPanel left = UI.card();
        left.setLayout(new BorderLayout(0, 10));
        left.add(UI.label("Job Roles", UI.H2, UI.TEXT), BorderLayout.NORTH);
        left.add(UI.scroll(jobList), BorderLayout.CENTER);

        roleInfo.setContentType("text/html");
        roleInfo.setEditable(false);
        roleInfo.setBackground(Color.WHITE);
        JPanel right = UI.card();
        right.setLayout(new BorderLayout(0, 10));
        right.add(UI.label("Required Skills", UI.H2, UI.TEXT), BorderLayout.NORTH);
        right.add(UI.scroll(roleInfo), BorderLayout.CENTER);
        analyseBtn.addActionListener(e -> analyse());
        JPanel btnRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        btnRow.setOpaque(false);
        btnRow.add(analyseBtn);
        right.add(btnRow, BorderLayout.SOUTH);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);
        center.add(left);
        center.add(right);
        add(center, BorderLayout.CENTER);
    }

    public void preselectResume(long resumeId) {
        pendingResumeId = resumeId;
    }

    @Override
    public void onShow() {
        Async.run(this, () -> new Data(ctx.api.jobs(), ctx.api.resumes()), data -> {
            loading = true;
            Models.ResumeItem previous = (Models.ResumeItem) resumeCombo.getSelectedItem();
            Models.JobItem previousJob = jobList.getSelectedValue();

            resumeCombo.removeAllItems();
            List<Models.ResumeItem> resumes = Models.resumes(data.resumes());
            Long wanted = pendingResumeId != null ? pendingResumeId
                    : (previous != null ? Long.valueOf(previous.id()) : ctx.lastResumeId);
            int select = 0;
            for (int i = 0; i < resumes.size(); i++) {
                resumeCombo.addItem(resumes.get(i));
                if (wanted != null && wanted == resumes.get(i).id()) select = i;
            }
            if (!resumes.isEmpty()) resumeCombo.setSelectedIndex(select);
            pendingResumeId = null;

            jobModel.clear();
            int jobSelect = 0;
            List<Models.JobItem> jobs = Models.jobs(data.jobs());
            for (int i = 0; i < jobs.size(); i++) {
                jobModel.addElement(jobs.get(i));
                if (previousJob != null && previousJob.id() == jobs.get(i).id()) jobSelect = i;
            }
            loading = false;
            if (!jobs.isEmpty()) jobList.setSelectedIndex(jobSelect);
            updateCandidateInfo();
            showRole();
        });
    }

    private void updateCandidateInfo() {
        Models.ResumeItem r = (Models.ResumeItem) resumeCombo.getSelectedItem();
        candidateInfo.setText(r == null ? "No resumes uploaded yet" : "Skills: " + (r.skills().isEmpty() ? "none detected" : r.skills()));
    }

    private void showRole() {
        Models.JobItem job = jobList.getSelectedValue();
        if (job == null) {
            roleInfo.setText("<html><body></body></html>");
            return;
        }
        StringBuilder sb = new StringBuilder("<html><body style='font-family:SansSerif;font-size:13pt'>");
        sb.append("<h2 style='color:#1d4ed8'>").append(UI.html(job.name())).append("</h2><ul>");
        for (String skill : job.skills().split(",")) {
            if (!skill.isBlank()) sb.append("<li>").append(UI.html(skill.trim())).append("</li>");
        }
        sb.append("</ul><p style='color:#6b7280'>Match Score = (Matched Skills / Total Required Skills) &times; 100</p>");
        sb.append("</body></html>");
        roleInfo.setText(sb.toString());
        roleInfo.setCaretPosition(0);
    }

    private void analyse() {
        Models.ResumeItem resume = (Models.ResumeItem) resumeCombo.getSelectedItem();
        Models.JobItem job = jobList.getSelectedValue();
        if (resume == null) { UI.error(this, "Please select a resume (upload one first)."); return; }
        if (job == null) { UI.error(this, "Please select a job role."); return; }
        final long resumeId = resume.id();
        final long jobId = job.id();
        analyseBtn.setEnabled(false);
        Async.run(this, () -> ctx.api.analyse(resumeId, jobId),
                r -> {
                    analyseBtn.setEnabled(true);
                    ctx.shell.openResults(resumeId, jobId);
                },
                ex -> {
                    analyseBtn.setEnabled(true);
                    UI.error(this, ex.getMessage());
                });
    }
}
