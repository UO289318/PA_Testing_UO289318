package g54.si26.cancelFormativeActions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JOptionPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import g54.si26.DTOs.FormativeActionDTO;
import g54.si26.utils.Util;
import java.util.Date;

public class ControllerCancelFormativeAction {
    private ModelCancelFormativeAction model;
    private ViewCancelFormativeAction view;
    private String simulatedDateStr;

    public ControllerCancelFormativeAction(ModelCancelFormativeAction model, ViewCancelFormativeAction view) {
        this.model = model;
        this.view = view;
    }

    public void setSimulatedDate(String simulatedDate) {
        this.simulatedDateStr = simulatedDate;
    }

    public void initController() {
        view.getTableCourses().getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) updateTeachersTable();
        });

        // Sync teacher completion with general completion by default
        view.getTxtCourseCompletion().getDocument().addDocumentListener(new DocumentListener() {
            public void changedUpdate(DocumentEvent e) { sync(); }
            public void removeUpdate(DocumentEvent e) { sync(); }
            public void insertUpdate(DocumentEvent e) { sync(); }
            private void sync() {
                String val = view.getTxtCourseCompletion().getText();
                DefaultTableModel m = (DefaultTableModel) view.getTableTeachers().getModel();
                for (int i = 0; i < m.getRowCount(); i++) {
                    m.setValueAt(val, i, 3);
                }
            }
        });

        view.getBtnCancel().addActionListener(e -> cancelAction());

        updateCoursesTable();
        view.getFrame().setVisible(true);
    }

    private void updateCoursesTable() {
        Date simulatedDate = Util.isoStringToDate(simulatedDateStr);
        List<FormativeActionDTO> list = model.getCancelableActions(simulatedDate);
        String[] columns = {"ID", "Name", "Start Date", "End Date", "Status"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        for (FormativeActionDTO fa : list) {
            tableModel.addRow(new Object[]{fa.getActionId(), fa.getName(), fa.getStartDate(), fa.getEndDate(), fa.getStatus()});
        }
        view.getTableCourses().setModel(tableModel);
        view.getTableTeachers().setModel(new DefaultTableModel());
    }

    private void updateTeachersTable() {
        int row = view.getTableCourses().getSelectedRow();
        if (row == -1) {
            view.getTableTeachers().setModel(new DefaultTableModel());
            return;
        }

        int actionId = (int) view.getTableCourses().getValueAt(row, 0);
        List<Object[]> list = model.getTeachersWithRemuneration(actionId);
        String[] columns = {"Teacher ID", "Name", "Current Remuneration", "Completion (%)"};
        DefaultTableModel tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return column == 3; }
        };
        String defaultPct = view.getTxtCourseCompletion().getText();
        for (Object[] t : list) {
            tableModel.addRow(new Object[]{t[0], t[1], t[2], defaultPct});
        }
        view.getTableTeachers().setModel(tableModel);
    }

    private void cancelAction() {
        int row = view.getTableCourses().getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(view.getFrame(), "Please select a formative action to cancel.");
            return;
        }

        try {
            int actionId = (int) view.getTableCourses().getValueAt(row, 0);
            
            double courseCompletionPct;
            try {
                courseCompletionPct = Double.parseDouble(view.getTxtCourseCompletion().getText());
                if (courseCompletionPct < 0 || courseCompletionPct > 100) {
                    JOptionPane.showMessageDialog(view.getFrame(), "Incorrect percentage");
                    return;
                }
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(view.getFrame(), "Incorrect percentage");
                return;
            }

            Map<Integer, Double> teacherCompletionPcts = new HashMap<>();
            DefaultTableModel teacherModel = (DefaultTableModel) view.getTableTeachers().getModel();
            for (int i = 0; i < teacherModel.getRowCount(); i++) {
                int teacherId = (int) teacherModel.getValueAt(i, 0);
                double teacherPct;
                try {
                    teacherPct = Double.parseDouble(teacherModel.getValueAt(i, 3).toString());
                    if (teacherPct < 0 || teacherPct > 100) {
                        JOptionPane.showMessageDialog(view.getFrame(), "Incorrect percentage");
                        return;
                    }
                } catch (NumberFormatException e) {
                    JOptionPane.showMessageDialog(view.getFrame(), "Incorrect percentage");
                    return;
                }
                teacherCompletionPcts.put(teacherId, teacherPct);
            }

            String msg = "Confirm cancellation?\nThis will set the status to CANCELLED and update financials.";
            if ("CANCELLED".equals(view.getTableCourses().getValueAt(row, 4))) {
                msg = "WARNING: This course is ALREADY cancelled.\nRe-cancelling will apply percentages relative to CURRENT remuneration.\nProceed?";
            }

            int confirm = JOptionPane.showConfirmDialog(view.getFrame(), msg, "Confirm", JOptionPane.YES_NO_OPTION);
            
            if (confirm == JOptionPane.YES_OPTION) {
                Date simDate = Util.isoStringToDate(simulatedDateStr);
                model.cancelAction(actionId, courseCompletionPct, teacherCompletionPcts, simDate);
                JOptionPane.showMessageDialog(view.getFrame(), "Action updated successfully.");
                updateCoursesTable();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view.getFrame(), "Error: " + e.getMessage());
        }
    }
}
