package g54.si26.planFormativeAction;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JOptionPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;
import javax.swing.ListSelectionModel;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Color;
import g54.si26.DTOs.TeacherDTO;

public class ViewPlanFormativeAction {

	private JFrame frame;

	// Section 1 – General Info
	private JTextField txtCourseName;
	private JTextArea txtObjectives;
	private JTextArea txtMainContents;

	private JTextField txtSpots;

	// Section 2 – Location and Schedule
	private JTextField txtStartDate;
	private JTextField txtEndDate;
	private JTextField txtLocation;
	private JSpinner spnDuration;
	private JCheckBox chkOnline;

	// Section 3 – Teacher
	private JComboBox<TeacherDTO> cbTeacher;
	private JTextField txtRemuneration;
	private JTable tblTeachers;
	private JButton btnAddTeacher;
	private JButton btnRemoveTeacher;

	// Section 4 – Finance
	private JTextField txtFee;
	private JCheckBox chkFreeCourse;

	// Section 5 – Enrolment Period
	private JTextField txtEnrolStart;
	private JTextField txtEnrolEnd;
	private JLabel lblEnrolWarning;

	// DEBUG button – remove entire block for review
	private JButton btnFillDebug;

	// Buttons
	private JButton btnBack;
	private JButton btnClear;
	private JButton btnSave;

	public ViewPlanFormativeAction() {
		initialize();
	}

	private void initialize() {
		frame = new JFrame();
		frame.setTitle("Plan New Formative Action");
		frame.setName("ViewPlanFormativeAction");
		frame.setBounds(100, 100, 820, 780);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		frame.getContentPane().setLayout(new BorderLayout(0, 0));

		// BckButtn
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 5, 5));
		frame.getContentPane().add(topPanel, BorderLayout.NORTH);

		btnBack = new JButton("Back");
		btnBack.setName("btnBack");
		btnBack.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		btnBack.setBackground(new Color(230, 230, 230));
		btnBack.setForeground(Color.BLACK);
		topPanel.add(btnBack);

		// Main Pannel
		JPanel mainPanel = new JPanel();
		mainPanel.setLayout(null);
		mainPanel.setPreferredSize(new java.awt.Dimension(780, 760));

		JScrollPane mainScroll = new JScrollPane(mainPanel);
		mainScroll.getVerticalScrollBar().setUnitIncrement(16);
		mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		frame.getContentPane().add(mainScroll, BorderLayout.CENTER);

		// General Info (Section 1)
		JPanel panelGeneral = new JPanel();
		panelGeneral.setBorder(new TitledBorder(null, "1. General Info", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Segoe UI", Font.BOLD, 12), new Color(0, 120, 215)));
		panelGeneral.setLayout(null);
		panelGeneral.setBounds(10, 10, 775, 205);
		mainPanel.add(panelGeneral);

		JLabel lblCourseName = new JLabel("Course Name:");
		lblCourseName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblCourseName.setBounds(10, 25, 140, 20);
		panelGeneral.add(lblCourseName);

		txtCourseName = new JTextField();
		txtCourseName.setName("txtCourseName");
		txtCourseName.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtCourseName.setBounds(155, 25, 600, 22);
		panelGeneral.add(txtCourseName);

		JLabel lblObjectives = new JLabel("Objectives:");
		lblObjectives.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblObjectives.setBounds(10, 57, 140, 20);
		panelGeneral.add(lblObjectives);

		txtObjectives = new JTextArea();
		txtObjectives.setName("txtObjectives");
		txtObjectives.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtObjectives.setLineWrap(true);
		txtObjectives.setWrapStyleWord(true);
		JScrollPane scrollObj = new JScrollPane(txtObjectives);
		scrollObj.setBounds(155, 55, 600, 45);
		panelGeneral.add(scrollObj);

		JLabel lblMainContents = new JLabel("Main Contents:");
		lblMainContents.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblMainContents.setBounds(10, 110, 140, 20);
		panelGeneral.add(lblMainContents);

		txtMainContents = new JTextArea();
		txtMainContents.setName("txtMainContents");
		txtMainContents.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtMainContents.setLineWrap(true);
		txtMainContents.setWrapStyleWord(true);
		JScrollPane scrollMC = new JScrollPane(txtMainContents);
		scrollMC.setBounds(155, 108, 600, 45);
		panelGeneral.add(scrollMC);

		JLabel lblSpots = new JLabel("Places:");
		lblSpots.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblSpots.setBounds(10, 163, 140, 20);
		panelGeneral.add(lblSpots);

		txtSpots = new JTextField();
		txtSpots.setName("txtSpots");
		txtSpots.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtSpots.setBounds(155, 163, 100, 22);
		panelGeneral.add(txtSpots);

		// Section 2: Location and Schdule
		JPanel panelSchedule = new JPanel();
		panelSchedule.setBorder(new TitledBorder(null, "2. Location and Schedule", TitledBorder.LEADING,
				TitledBorder.TOP, new Font("Segoe UI", Font.BOLD, 12), new Color(0, 120, 215)));
		panelSchedule.setLayout(null);
		panelSchedule.setBounds(10, 225, 775, 155);
		mainPanel.add(panelSchedule);

		JLabel lblStartDate = new JLabel("Start Date (yyyy-MM-dd):");
		lblStartDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblStartDate.setBounds(10, 25, 175, 20);
		panelSchedule.add(lblStartDate);

		txtStartDate = new JTextField();
		txtStartDate.setName("txtStartDate");
		txtStartDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtStartDate.setBounds(190, 25, 150, 22);
		panelSchedule.add(txtStartDate);

		JLabel lblEndDate = new JLabel("End Date (yyyy-MM-dd):");
		lblEndDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblEndDate.setBounds(360, 25, 165, 20);
		panelSchedule.add(lblEndDate);

		txtEndDate = new JTextField();
		txtEndDate.setName("txtEndDate");
		txtEndDate.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtEndDate.setBounds(530, 25, 150, 22);
		panelSchedule.add(txtEndDate);

		JLabel lblDuration = new JLabel("Duration (Hours):");
		lblDuration.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblDuration.setBounds(10, 60, 140, 20);
		panelSchedule.add(lblDuration);

		spnDuration = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
		spnDuration.setName("spnDuration");
		spnDuration.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		spnDuration.setBounds(155, 60, 70, 22);
		panelSchedule.add(spnDuration);

		JLabel lblLocation = new JLabel("Location:");
		lblLocation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblLocation.setBounds(10, 95, 140, 20);
		panelSchedule.add(lblLocation);

		txtLocation = new JTextField();
		txtLocation.setName("txtLocation");
		txtLocation.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtLocation.setBounds(155, 95, 600, 22);
		panelSchedule.add(txtLocation);

		chkOnline = new JCheckBox("Held Online");
		chkOnline.setName("chkOnline");
		chkOnline.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		chkOnline.setBounds(10, 125, 200, 22);
		panelSchedule.add(chkOnline);

		//Section 3: Teacher
		// Left side: combo + remuneration + buttons. Right side: selected teachers grid.
		JPanel panelTeacher = new JPanel();
		panelTeacher.setBorder(new TitledBorder(null, "3. Teacher", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Segoe UI", Font.BOLD, 12), new Color(0, 120, 215)));
		panelTeacher.setLayout(null);
		panelTeacher.setBounds(10, 390, 775, 160);
		mainPanel.add(panelTeacher);

		JLabel lblTeacher = new JLabel("Teacher:");
		lblTeacher.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblTeacher.setBounds(10, 25, 100, 20);
		panelTeacher.add(lblTeacher);

		cbTeacher = new JComboBox<>();
		cbTeacher.setName("cbTeacher");
		cbTeacher.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		cbTeacher.setBackground(Color.WHITE);
		cbTeacher.setBounds(115, 25, 280, 22);
		panelTeacher.add(cbTeacher);

		JLabel lblRemuneration = new JLabel("Remuneration (€):");
		lblRemuneration.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblRemuneration.setBounds(10, 60, 140, 20);
		panelTeacher.add(lblRemuneration);

		txtRemuneration = new JTextField();
		txtRemuneration.setName("txtRemuneration");
		txtRemuneration.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtRemuneration.setBounds(155, 60, 120, 22);
		panelTeacher.add(txtRemuneration);

		btnAddTeacher = new JButton("Add →");
		btnAddTeacher.setName("btnAddTeacher");
		btnAddTeacher.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		btnAddTeacher.setBounds(10, 95, 90, 24);
		panelTeacher.add(btnAddTeacher);

		btnRemoveTeacher = new JButton("Remove");
		btnRemoveTeacher.setName("btnRemoveTeacher");
		btnRemoveTeacher.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		btnRemoveTeacher.setBounds(110, 95, 90, 24);
		panelTeacher.add(btnRemoveTeacher);

		// Selected teachers grid (right side)
		JLabel lblSelectedTeachers = new JLabel("Selected Teachers:");
		lblSelectedTeachers.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblSelectedTeachers.setBounds(415, 25, 150, 20);
		panelTeacher.add(lblSelectedTeachers);

		tblTeachers = new JTable(new DefaultTableModel(new String[] { "ID", "Name", "Remuneration (€)" }, 0));
		tblTeachers.setName("tblTeachers");
		tblTeachers.setFont(new Font("Segoe UI", Font.PLAIN, 11));
		tblTeachers.setRowHeight(20);
		tblTeachers.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		tblTeachers.setDefaultEditor(Object.class, null);
		tblTeachers.getColumnModel().getColumn(0).setPreferredWidth(30);
		tblTeachers.getColumnModel().getColumn(1).setPreferredWidth(150);
		tblTeachers.getColumnModel().getColumn(2).setPreferredWidth(110);
		JScrollPane scrollTeachers = new JScrollPane(tblTeachers);
		scrollTeachers.setBounds(415, 45, 345, 105);
		panelTeacher.add(scrollTeachers);

		//Section 4: Finance
		// Left side: fee field + free checkbox + buttons. Right side: fees grid.
		JPanel panelFinance = new JPanel();
		panelFinance.setBorder(new TitledBorder(null, "4. Finance", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Segoe UI", Font.BOLD, 12), new Color(0, 120, 215)));
		panelFinance.setLayout(null);
		panelFinance.setBounds(10, 560, 775, 65);
		mainPanel.add(panelFinance);

		JLabel lblFee = new JLabel("Course Fee (€):");
		lblFee.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblFee.setBounds(10, 25, 130, 20);
		panelFinance.add(lblFee);

		txtFee = new JTextField();
		txtFee.setName("txtFee");
		txtFee.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtFee.setBounds(145, 25, 120, 22);
		panelFinance.add(txtFee);

		chkFreeCourse = new JCheckBox("Free Course");
		chkFreeCourse.setName("chkFreeCourse");
		chkFreeCourse.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		chkFreeCourse.setBounds(280, 25, 120, 22);
		panelFinance.add(chkFreeCourse);

		//Section 5: Enrolmnt Period
		JPanel panelEnrolment = new JPanel();
		panelEnrolment.setBorder(new TitledBorder(null, "5. Enrolment Period", TitledBorder.LEADING, TitledBorder.TOP,
				new Font("Segoe UI", Font.BOLD, 12), new Color(0, 120, 215)));
		panelEnrolment.setLayout(null);
		panelEnrolment.setBounds(10, 635, 775, 110);
		mainPanel.add(panelEnrolment);

		JLabel lblEnrolStart = new JLabel("Start Date (yyyy-MM-dd):");
		lblEnrolStart.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblEnrolStart.setBounds(10, 25, 180, 20);
		panelEnrolment.add(lblEnrolStart);

		txtEnrolStart = new JTextField();
		txtEnrolStart.setName("txtEnrolStart");
		txtEnrolStart.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtEnrolStart.setBounds(195, 25, 180, 22);
		panelEnrolment.add(txtEnrolStart);

		JLabel lblEnrolEnd = new JLabel("End Date (yyyy-MM-dd):");
		lblEnrolEnd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		lblEnrolEnd.setBounds(10, 58, 180, 20);
		panelEnrolment.add(lblEnrolEnd);

		txtEnrolEnd = new JTextField();
		txtEnrolEnd.setName("txtEnrolEnd");
		txtEnrolEnd.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		txtEnrolEnd.setBounds(195, 58, 180, 22);
		panelEnrolment.add(txtEnrolEnd);

		lblEnrolWarning = new JLabel("\u26a0  Warning: enrolment should start at least 3 weeks before the session date.");
		lblEnrolWarning.setName("lblEnrolWarning");
		lblEnrolWarning.setFont(new Font("Segoe UI", Font.BOLD, 11));
		lblEnrolWarning.setForeground(new Color(200, 100, 0));
		lblEnrolWarning.setVisible(false);
		lblEnrolWarning.setBounds(10, 85, 740, 18);
		panelEnrolment.add(lblEnrolWarning);

		// Bottom: Clear + Save (+ DEBUG Fill up Data) ───────────────────────
		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 5, 5));
		frame.getContentPane().add(bottomPanel, BorderLayout.SOUTH);

		// DEBUG – remove button block for review
		btnFillDebug = new JButton("Fill up Data");
		btnFillDebug.setName("btnFillDebug");
		btnFillDebug.setFont(new Font("Segoe UI", Font.ITALIC, 12));
		btnFillDebug.setBackground(new Color(255, 200, 0));
		btnFillDebug.setForeground(Color.DARK_GRAY);
		bottomPanel.add(btnFillDebug);
		// END DEBUG

		btnClear = new JButton("Clear");
		btnClear.setName("btnClear");
		btnClear.setFont(new Font("Segoe UI", Font.PLAIN, 12));
		btnClear.setBackground(new Color(230, 230, 230));
		btnClear.setForeground(Color.BLACK);
		bottomPanel.add(btnClear);

		btnSave = new JButton("Save Formative Action");
		btnSave.setName("btnSave");
		btnSave.setFont(new Font("Segoe UI", Font.BOLD, 13));
		btnSave.setBackground(new Color(0, 120, 215));
		btnSave.setForeground(Color.WHITE);
		bottomPanel.add(btnSave);
	}


	public JFrame getFrame() {
		return frame;
	}

	public void setFrame(JFrame frame) {
		this.frame = frame;
	}

	public JTextField getTxtCourseNameField() {
		return txtCourseName;
	}

	public void setTxtCourseNameField(JTextField txtCourseName) {
		this.txtCourseName = txtCourseName;
	}

	public JTextArea getTxtObjectivesField() {
		return txtObjectives;
	}

	public void setTxtObjectivesField(JTextArea txtObjectives) {
		this.txtObjectives = txtObjectives;
	}

	public JTextArea getTxtMainContentsField() {
		return txtMainContents;
	}

	public void setTxtMainContentsField(JTextArea txtMainContents) {
		this.txtMainContents = txtMainContents;
	}

	public JTextField getTxtSpotsField() {
		return txtSpots;
	}

	public void setTxtSpotsField(JTextField txtSpots) {
		this.txtSpots = txtSpots;
	}

	public JTextField getTxtStartDateField() {
		return txtStartDate;
	}

	public void setTxtStartDateField(JTextField txtStartDate) {
		this.txtStartDate = txtStartDate;
	}

	public JTextField getTxtEndDateField() {
		return txtEndDate;
	}

	public void setTxtEndDateField(JTextField txtEndDate) {
		this.txtEndDate = txtEndDate;
	}

	public JTextField getTxtLocationField() {
		return txtLocation;
	}

	public void setTxtLocationField(JTextField txtLocation) {
		this.txtLocation = txtLocation;
	}

	public JSpinner getSpnDuration() {
		return spnDuration;
	}

	public void setSpnDuration(JSpinner spnDuration) {
		this.spnDuration = spnDuration;
	}

	public JCheckBox getChkOnline() {
		return chkOnline;
	}

	public void setChkOnline(JCheckBox chkOnline) {
		this.chkOnline = chkOnline;
	}

	public JComboBox<TeacherDTO> getCbTeacher() {
		return cbTeacher;
	}

	public void setCbTeacher(JComboBox<TeacherDTO> cbTeacher) {
		this.cbTeacher = cbTeacher;
	}

	public JTextField getTxtRemunerationField() {
		return txtRemuneration;
	}

	public void setTxtRemunerationField(JTextField txtRemuneration) {
		this.txtRemuneration = txtRemuneration;
	}

	public JTable getTblTeachers() {
		return tblTeachers;
	}

	public void setTblTeachers(JTable tblTeachers) {
		this.tblTeachers = tblTeachers;
	}

	public JButton getBtnAddTeacher() {
		return btnAddTeacher;
	}

	public void setBtnAddTeacher(JButton btnAddTeacher) {
		this.btnAddTeacher = btnAddTeacher;
	}

	public JButton getBtnRemoveTeacher() {
		return btnRemoveTeacher;
	}

	public void setBtnRemoveTeacher(JButton btnRemoveTeacher) {
		this.btnRemoveTeacher = btnRemoveTeacher;
	}

	public JTextField getTxtFeeField() {
		return txtFee;
	}

	public void setTxtFeeField(JTextField txtFee) {
		this.txtFee = txtFee;
	}

	public JCheckBox getChkFreeCourse() {
		return chkFreeCourse;
	}

	public void setChkFreeCourse(JCheckBox chkFreeCourse) {
		this.chkFreeCourse = chkFreeCourse;
	}

	public JTextField getTxtEnrolStartField() {
		return txtEnrolStart;
	}

	public void setTxtEnrolStartField(JTextField txtEnrolStart) {
		this.txtEnrolStart = txtEnrolStart;
	}

	public JTextField getTxtEnrolEndField() {
		return txtEnrolEnd;
	}

	public void setTxtEnrolEndField(JTextField txtEnrolEnd) {
		this.txtEnrolEnd = txtEnrolEnd;
	}

	public JLabel getLblEnrolWarning() {
		return lblEnrolWarning;
	}

	public void setLblEnrolWarning(JLabel lblEnrolWarning) {
		this.lblEnrolWarning = lblEnrolWarning;
	}

	public JButton getBtnFillDebug() {
		return btnFillDebug;
	}

	public void setBtnFillDebug(JButton btnFillDebug) {
		this.btnFillDebug = btnFillDebug;
	}

	public JButton getBtnBack() {
		return btnBack;
	}

	public void setBtnBack(JButton btnBack) {
		this.btnBack = btnBack;
	}

	public JButton getBtnClear() {
		return btnClear;
	}

	public void setBtnClear(JButton btnClear) {
		this.btnClear = btnClear;
	}

	public JButton getBtnSave() {
		return btnSave;
	}

	public void setBtnSave(JButton btnSave) {
		this.btnSave = btnSave;
	}

	public String getTxtCourseName() {
		return txtCourseName.getText().trim();
	}

	public String getTxtObjectives() {
		return txtObjectives.getText().trim();
	}

	public String getTxtMainContents() {
		return txtMainContents.getText().trim();
	}

	public String getTxtStartDate() {
		return txtStartDate.getText().trim();
	}

	public String getTxtEndDate() {
		return txtEndDate.getText().trim();
	}

	public String getTxtLocation() {
		return txtLocation.getText().trim();
	}

	public String getTxtRemuneration() {
		return txtRemuneration.getText().trim();
	}

	public String getTxtFee() {
		return txtFee.getText().trim();
	}

	public String getTxtEnrolStart() {
		return txtEnrolStart.getText().trim();
	}

	public String getTxtEnrolEnd() {
		return txtEnrolEnd.getText().trim();
	}

	public String getTxtSpots() {
		return txtSpots.getText().trim();
	}

	public void setTxtCourseName(String v) {
		txtCourseName.setText(v);
	}

	public void setTxtObjectives(String v) {
		txtObjectives.setText(v);
	}

	public void setTxtMainContents(String v) {
		txtMainContents.setText(v);
	}

	public void setTxtStartDate(String v) {
		txtStartDate.setText(v);
	}

	public void setTxtEndDate(String v) {
		txtEndDate.setText(v);
	}

	public void setTxtLocation(String v) {
		txtLocation.setText(v);
	}

	public void setTxtRemuneration(String v) {
		txtRemuneration.setText(v);
	}

	public void setTxtFee(String v) {
		txtFee.setText(v);
	}

	public void setTxtEnrolStart(String v) {
		txtEnrolStart.setText(v);
	}

	public void setTxtEnrolEnd(String v) {
		txtEnrolEnd.setText(v);
	}

	public void setTxtSpots(String v) {
		txtSpots.setText(v);
	}

	public void showSuccess(String courseName) {
		JOptionPane.showMessageDialog(frame, "Formative action \"" + courseName + "\" has been planned successfully.",
				"Success", JOptionPane.INFORMATION_MESSAGE);
	}

	public void showError(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Error", JOptionPane.ERROR_MESSAGE);
	}

	public void showWarningDialog(String msg) {
		JOptionPane.showMessageDialog(frame, msg, "Warning", JOptionPane.WARNING_MESSAGE);
	}

	
	//Shows a summary dialog listing ALL validation issues (errors + warnings).

	public void showValidationSummary(java.util.List<String> errors, java.util.List<String> warnings) {
		StringBuilder sb = new StringBuilder();
		sb.append("The Formative Action could not be saved.\n");
		sb.append("Please review the following issues:\n\n");
		if (!errors.isEmpty()){
			sb.append("BLOCKING ERRORS\n");
			errors.forEach(e -> sb.append("  -  ").append(e).append("\n"));
		}
		if (!warnings.isEmpty()){
			sb.append("\nWARNINGS\n");
			warnings.forEach(w -> sb.append("  -  ").append(w).append("\n"));
		}
		JOptionPane.showMessageDialog(frame, sb.toString(), "Validation Summary", JOptionPane.ERROR_MESSAGE);
	}
}