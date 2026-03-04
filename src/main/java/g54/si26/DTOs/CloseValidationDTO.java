package g54.si26.DTOs;

import java.util.ArrayList;
import java.util.List;

public class CloseValidationDTO {
    
    private boolean canClose;
    private List<String> errors;
    private List<String> warnings;

    public CloseValidationDTO() {
        this.canClose = true;
        this.errors = new ArrayList<>();
        this.warnings = new ArrayList<>();
    }

    public boolean isCanClose() { 
        return canClose; 
    }
    
    public void setCanClose(boolean canClose) { 
        this.canClose = canClose; 
    }
    
    public List<String> getErrors() { 
        return errors; 
    }
    
    // Blocks automatically if there's any error.
    public void addError(String error) { 
        this.errors.add(error); 
        this.canClose = false; 
    }
    
    public List<String> getWarnings() { 
        return warnings; 
    }
    
    public void addWarning(String warning) { 
        this.warnings.add(warning); 
    }
}