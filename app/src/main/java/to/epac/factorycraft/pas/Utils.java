package to.epac.factorycraft.pas;

import static to.epac.factorycraft.pas.MainActivity.categories;

import java.util.ArrayList;

import to.epac.factorycraft.pas.components.Category;
import to.epac.factorycraft.pas.components.Content;

public class Utils {
    /**
     * Check whether the entered String is Integer or not
     *
     * @param s String to check
     * @return true/false
     */
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * Get specified category's contents
     *
     * @param selected The selected category
     * @param allLang  Return all language matches the category
     * @return Full list of the filtered content
     */
    public static ArrayList<Content> getCategoryContents(int selected, boolean allLang) {
        ArrayList<Content> contents = new ArrayList<>();

        if (!categories.isEmpty()) {
            for (Content content : categories.get(selected).getContents()) {
                if (content.getSubId().isEmpty() || content.getSubId().equals("1")) {
                    if (allLang)
                        contents.add(content);
                    else if (content.getLang().equalsIgnoreCase("E"))
                        contents.add(content);
                }
            }
        }
        return contents;
    }

    /**
     * Get specified category's contents
     *
     * @param varType The selected Variable Type
     * @param allLang Return all language matches the category
     * @return Full list of the filtered content
     */
    public static ArrayList<Content> getCategoryContents(String varType, boolean allLang) {
        ArrayList<Content> contents = new ArrayList<>();

        for (Category category : categories) {
            if (category.getId().equals(varType)) {
                if (allLang)
                    contents.addAll(category.getContents());
                else {
                    for (Content item : category.getContents()) {
                        if (item.getLang().equalsIgnoreCase("E"))
                            contents.add(item);
                    }
                }
            }
        }
        return contents;
    }

    /**
     * Get specified category's specified pa's contents
     *
     * @param category The selected category
     * @param id       The PA id
     * @param allLang  Return all language matches the category
     * @return Full list of the filtered pa content
     */
    public static ArrayList<Content> getSelectedPaContents(int category, String id, boolean allLang) {
        ArrayList<Content> contents = new ArrayList<>();

        for (Content content : categories.get(category).getContents()) {
            if (content.getId().equals(id)) {
                if (allLang) {
                    contents.add(content);
                } else if (content.getLang().equalsIgnoreCase("E")) {
                    contents.add(content);
                }
            }
        }
        return contents;
    }
}
