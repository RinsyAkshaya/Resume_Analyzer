package com.resumeanalyser.ui;

import com.fasterxml.jackson.databind.JsonNode;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class UploadPanel extends ScreenPanel {

    private final AppContext ctx;
    private File chosen;
    private Long uploadedResumeId;

    private final JTextField filePath = UI.textField(36);
    private final JButton uploadBtn = UI.button("Upload & Extract", true);
    private final JButton analyseBtn = UI.button("Select Job Role & Analyse", true);

    private final JLabel vName = value(), vEmail = value(), vPhone = value(), vExperience = value(), vEducation = value();
    private final JTextArea skillsArea = area();
    private final JTextArea technicalArea = area();

    private final DefaultTableModel listModel = UI.readOnlyModel("ID", "Candidate", "Email", "Experience", "Education", "Skills");
    private final List<Long> listIds = new ArrayList<>();
    private JTable listTable;

    public UploadPanel(AppContext ctx) {
        this.ctx = ctx;
        setLayout(new BorderLayout(0, 16));
        setBorder(new EmptyBorder(24, 28, 24, 28));

        JPanel head = new JPanel();
        head.setOpaque(false);
        head.setLayout(new BoxLayout(head, BoxLayout.Y_AXIS));
        JLabel t = UI.label("Upload Resume", UI.H1, UI.TEXT);
        t.setAlignmentX(Component.LEFT_ALIGNMENT);
        JLabel s = UI.label("Supported formats: PDF, DOCX, TXT. Extracted details are shown before analysis.", UI.BASE, UI.MUTED);
        s.setAlignmentX(Component.LEFT_ALIGNMENT);
        head.add(t);
        head.add(Box.createVerticalStrut(2));
        head.add(s);

        // picker row
        JPanel picker = UI.card();
        picker.setLayout(new FlowLayout(FlowLayout.LEFT, 12, 4));
        filePath.setEditable(false);
        JButton browse = UI.button("Browse...", false);
        browse.addActionListener(e -> browse());
        uploadBtn.addActionListener(e -> upload());
        picker.add(browse);
        picker.add(filePath);
        picker.add(uploadBtn);

        JPanel top = new JPanel(new BorderLayout(0, 14));
        top.setOpaque(false);
        top.add(head, BorderLayout.NORTH);
        top.add(picker, BorderLayout.CENTER);
        add(top, BorderLayout.NORTH);

        // extracted info
        JPanel info = UI.card();
        info.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 14);
        gc.anchor = GridBagConstraints.NORTHWEST;
        int row = 0;
        info.add(UI.label("Extracted Information", UI.H2, UI.TEXT), at(gc, 0, row++, 2));
        addRow(info, gc, row++, "Candidate Name", vName);
        addRow(info, gc, row++, "Email", vEmail);
        addRow(info, gc, row++, "Phone", vPhone);
        addRow(info, gc, row++, "Experience", vExperience);
        addRow(info, gc, row++, "Education", vEducation);
        gc.gridx = 0; gc.gridy = row++; gc.gridwidth = 1; gc.weightx = 0;
        info.add(UI.label("Skills", UI.BOLD, UI.MUTED), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        info.add(skillsArea, gc);
        gc.gridx = 0; gc.gridy = row++; gc.weightx = 0; gc.fill = GridBagConstraints.NONE;
        info.add(UI.label("Technical Skills", UI.BOLD, UI.MUTED), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        info.add(technicalArea, gc);
        gc.gridx = 1; gc.gridy = row; gc.weighty = 1; gc.fill = GridBagConstraints.NONE;
        analyseBtn.setEnabled(false);
        analyseBtn.addActionListener(e -> {
            if (uploadedResumeId != null) ctx.shell.openJobs(uploadedResumeId);
        });
        info.add(analyseBtn, gc);

        // all resumes
        listTable = new JTable(listModel);
        UI.styleTable(listTable);
        listTable.getColumnModel().getColumn(0).setMaxWidth(50);
        JPanel listCard = UI.card();
        listCard.setLayout(new BorderLayout(0, 10));
        JPanel listHead = new JPanel(new BorderLayout());
        listHead.setOpaque(false);
        listHead.add(UI.label("All Uploaded Resumes", UI.H2, UI.TEXT), BorderLayout.WEST);
        JButton analyseSel = UI.button("Analyse Selected", false);
        analyseSel.addActionListener(e -> {
            int r = listTable.getSelectedRow();
            if (r < 0) { UI.error(this, "Select a resume in the table first."); return; }
            ctx.shell.openJobs(listIds.get(listTable.convertRowIndexToModel(r)));
        });
        listHead.add(analyseSel, BorderLayout.EAST);
        listCard.add(listHead, BorderLayout.NORTH);
        listCard.add(UI.scroll(listTable), BorderLayout.CENTER);

        JPanel center = new JPanel(new GridLayout(1, 2, 16, 0));
        center.setOpaque(false);
        center.add(info);
        center.add(listCard);
        add(center, BorderLayout.CENTER);
    }

    private static GridBagConstraints at(GridBagConstraints gc, int x, int y, int w) {
        gc.gridx = x; gc.gridy = y; gc.gridwidth = w; gc.weightx = 0; gc.weighty = 0; gc.fill = GridBagConstraints.NONE;
        return gc;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int row, String caption, JLabel val) {
        at(gc, 0, row, 1);
        p.add(UI.label(caption, UI.BOLD, UI.MUTED), gc);
        gc.gridx = 1; gc.weightx = 1; gc.fill = GridBagConstraints.HORIZONTAL;
        p.add(val, gc);
    }

    private static JLabel value() {
        return UI.label("-", UI.BASE, UI.TEXT);
    }

    private static JTextArea area() {
        JTextArea a = new JTextArea(3, 24);
        a.setFont(UI.BASE);
        a.setEditable(false);
        a.setLineWrap(true);
        a.setWrapStyleWord(true);
        a.setBackground(new Color(249, 250, 251));
        a.setBorder(new EmptyBorder(6, 8, 6, 8));
        return a;
    }

    /** Called by the dashboard: pre-fills the file and uploads immediately. */
    public void preselect(File file) {
        if (file == null) return;
        chosen = file;
        filePath.setText(file.getAbsolutePath());
        SwingUtilities.invokeLater(this::upload);
    }

    private void browse() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Select a resume (PDF, DOCX or TXT)");
        chooser.setFileFilter(new FileNameExtensionFilter("Resumes (*.pdf, *.docx, *.txt)", "pdf", "docx", "txt"));
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            chosen = chooser.getSelectedFile();
            filePath.setText(chosen.getAbsolutePath());
        }
    }

    private void upload() {
        if (chosen == null) {
            UI.error(this, "Please choose a resume file first.");
            return;
        }
        final File file = chosen;
        uploadBtn.setEnabled(false);
        Async.run(this, () -> ctx.api.uploadResume(file, ctx.userId), resume -> {
            uploadBtn.setEnabled(true);
            show(resume);
            ctx.lastResumeId = resume.path("resumeId").asLong();
            ctx.lastResumeLabel = resume.path("candidateName").asText() + " (" + resume.path("uploadedFileName").asText() + ")";
            uploadedResumeId = ctx.lastResumeId;
            analyseBtn.setEnabled(true);
            loadList();
        }, ex -> {
            uploadBtn.setEnabled(true);
            UI.error(this, ex.getMessage());
        });
    }

    private void show(JsonNode r) {
        vName.setText(r.path("candidateName").asText("-"));
        vEmail.setText(r.path("email").asText("Not found"));
        vPhone.setText(r.path("phone").asText("Not found"));
        vExperience.setText(UI.years(r.path("experienceYears").asDouble()));
        vEducation.setText("<html>" + UI.html(r.path("education").asText("Not specified")) + "</html>");
        String skills = Models.join(r.path("skills"), ", ");
        skillsArea.setText(skills.isEmpty() ? "No known skills detected" : skills);

        StringBuilder sb = new StringBuilder();
        JsonNode tech = r.path("technicalSkills");
        tech.fields().forEachRemaining(en -> sb.append(en.getKey()).append(": ").append(Models.join(en.getValue(), ", ")).append("\n"));
        technicalArea.setText(sb.length() == 0 ? "No technical skills detected" : sb.toString().trim());
    }

    private void loadList() {
        Async.run(this, () -> ctx.api.resumes(), nodes -> {
            listModel.setRowCount(0);
            listIds.clear();
            for (Models.ResumeItem r : Models.resumes(nodes)) {
                listIds.add(r.id());
                listModel.addRow(new Object[]{r.id(), r.name(), r.email(), UI.years(r.years()), r.education(), r.skills()});
            }
        });
    }

    @Override
    public void onShow() {
        loadList();
    }
}
