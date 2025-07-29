package to.epac.factorycraft.pas.components;

/**
 * Content of the PA, including different segments of it
 * Appro   03.1C #04	.
 * Appro   03.1E #04	Train for LW cross boundary with departure platform info (Train not yet come): The approaching train for Lo Wu will depart from
 * Appro   03.1P #04	.
 */
public class Content {
    // >03<
    private String id;

    // >1<
    private String subId;

    // >C< >E< >P<
    private String lang;

    // Before semi-colon
    // >Train for LW cross boundary with departure platform info (Train not yet come)<
    private String title;

    // After semi-colon
    // >The approaching train for Lo Wu will depart from<
    private String message;

    // >#04<
    private String variable;

    // >;<
    private boolean disabled;

    public Content(String id, String subId, String lang,
                   String title, String message, String variable, boolean disabled) {
        this.id = id;
        this.subId = subId;
        this.lang = lang;
        this.title = title;
        this.message = message;
        this.variable = variable;
        this.disabled = disabled;
    }

    public Content(String id, String lang, String message, boolean disabled) {
        this(id, "", lang, "", message, "", disabled);
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getSubId() {
        return subId;
    }

    public void setSubId(String subId) {
        this.subId = subId;
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


    public boolean isDisabled() {
        return disabled;
    }

    public void setDisabled(boolean disabled) {
        this.disabled = disabled;
    }
}
