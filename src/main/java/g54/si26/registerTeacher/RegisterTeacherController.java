package g54.si26.registerTeacher;

import g54.si26.DTOs.TeacherDTO;
import g54.si26.utils.SwingUtil;

public class RegisterTeacherController {

    private final RegisterTeacherModel model;
    private final RegisterTeacherView view;

    public RegisterTeacherController(RegisterTeacherModel model, RegisterTeacherView view) {
        this.model = model;
        this.view = view;
    }

    public void initController() {
        view.getBtnCancel().addActionListener(e -> view.getFrame().dispose());
        view.getBtnSave().addActionListener(e -> SwingUtil.exceptionWrapper(() -> saveTeacher()));
        view.getFrame().setVisible(true);
    }

    private void saveTeacher() {
        TeacherDTO teacher = new TeacherDTO();
        teacher.setName(view.getName());
        teacher.setFiscalId(view.getFiscalId());
        teacher.setEmail(view.getEmail());
        teacher.setPhone(view.getPhone());

        // model.registerTeacher throws ApplicationException with joined error messages if any
        model.registerTeacher(teacher);
        
        view.showMessage("Teacher registered successfully!");
        view.getFrame().dispose();
    }
}
