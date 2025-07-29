package to.epac.factorycraft.pas.components;

import java.util.ArrayList;

/**
 * Categories that will be applied as button tooltip
 * 01	Safety
 * 02	Appro
 * 03	Problem
 */
public class Category {
    // >01<
    // >#02<
    private String id;

    // >Safety<
    // >#02<
    private String name;

    private ArrayList<Content> contents = new ArrayList<>();


    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public ArrayList<Content> getContents() {
        return contents;
    }

    public void setContents(ArrayList<Content> contents) {
        this.contents = contents;
    }
}
