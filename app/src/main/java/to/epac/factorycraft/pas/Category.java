package to.epac.factorycraft.pas;

import androidx.annotation.Nullable;

import java.util.ArrayList;

public class Category {
    // >01<
    // >#02<
    private String id;

    // >Safety<
    // >#02<
    @Nullable
    private String name;

    private ArrayList<Content> contents = new ArrayList<>();

    public Category(String id, String name) {
        this.id = id;
        this.name = name;
    }
    public Category(String id) {
        this(id, "");
    }
    public Category() {
        this("", "");
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

    public void addContent(Content content) {
        contents.add(content);
    }
    public Content getContent(int index) {
        return contents.get(index);
    }
    public ArrayList<Content> getContents() {
        return contents;
    }
}
