package to.epac.factorycraft.pas;

import androidx.annotation.Nullable;

public class Content {
    // >30<
    private String id;

    // >1<
    @Nullable
    private String subid;

    // >C< >E< >P<
    private String lang;

    // Before semi-colon
    // >Train for LMC cross boundary (Train not yet come)<
    private String title;

    // After semi-colon
    // >The approaching train is going to Lok Ma Chau.<
    @Nullable
    private String message;

    // >#03<
    @Nullable
    private String variable;

    // true / false
    private boolean isDisabled;

    public Content() {
        this("", "", "", "", "", "", false);
    }
    public Content(String id, String subid, String lang, String title, String message, String variable, boolean isDisabled) {
        this.id = id;
        this.subid = subid;
        this.lang = lang;
        this.title = title;
        this.message = message;
        this.variable = variable;
        this.isDisabled = isDisabled;
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getSubid() {
        return subid;
    }
    public void setSubid(String subid) {
        this.subid = subid;
    }

    public String getLang() {
        return lang;
    }
    public void setLang(String lang) {
        this.lang = lang;
    }

    public String getTitle() {
        return title;
    }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }

    public String getVariable() {
        return variable;
    }
    public void setVariable(String variable) {
        this.variable = variable;
    }

    public boolean getDisabled() {
        return isDisabled;
    }
    public void setDisabled(boolean disabled) {
        isDisabled = disabled;
    }
}
