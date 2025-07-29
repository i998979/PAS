package to.epac.factorycraft.pas;

public class SoundPath {
    private String lang;
    private String path;

    public SoundPath(String lang, String path) {
        this.lang = lang;
        this.path = path;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }


    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
