package to.epac.factorycraft.pas;

import android.view.View;

public class Component {
    private String id;
    private String data;
    private View component;

    public Component (String id, String data, View component) {
        this.id = id;
        this.data = "";
        this.component = component;
    }
    public Component (String id, View component) {
        this(id, "", component);
    }

    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }

    public String getData() {
        return data;
    }
    public void setData(String data) {
        this.data = data;
    }

    public View getComponent() {
        return component;
    }
    public void setComponent(View component) {
        this.component = component;
    }
}
