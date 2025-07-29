package to.epac.factorycraft.pas.components;

import android.view.View;

public class Component {
    private String id;
    private View component;

    public Component(String id, View component) {
        this.id = id;
        this.component = component;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public View getComponent() {
        return component;
    }

    public void setComponent(View component) {
        this.component = component;
    }
}
