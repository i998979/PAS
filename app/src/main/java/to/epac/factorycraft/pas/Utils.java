package to.epac.factorycraft.pas;

import android.content.Context;
import android.os.storage.StorageManager;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static to.epac.factorycraft.pas.MainActivity.categories;

public class Utils {
    /**
     * Check whether the entered String is Integer or not
     *
     * @param s Srting to check
     * @return true/false
     */
    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        return true;
    }

    /**
     * Get specified category's contents
     *
     * @param selected The selected category
     * @param allLang Return all language matches the category
     * @return Full list of the filtered content
     */
    public static ArrayList<Content> getCategoryContents(int selected, boolean allLang) {
        ArrayList<Content> contents = new ArrayList<>();

        for (Content content : categories.get(selected).getContents()) {
            if (content.getSubid().equals("") || content.getSubid().equals("1")) {
                if (allLang)
                    contents.add(content);
                else if (content.getLang().equals("E"))
                    contents.add(content);
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
                        if (item.getLang().equals("E"))
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
     * @param id The PA id
     * @param allLang Return all language matches the category
     * @return Full list of the filtered pa content
     */
    public static ArrayList<Content> getSelectedPaContents(int category, String id, boolean allLang) {
        ArrayList<Content> contents = new ArrayList<>();

        for (Content content : categories.get(category).getContents()) {
            if (content.getId().equals(id)) {
                if (allLang) {
                    contents.add(content);
                }
                else if (content.getLang().equals("E")) {
                    contents.add(content);
                }
            }
        }
        return contents;
    }

    public static String getExtendedMemoryPath(Context mContext) {

        StorageManager mStorageManager = (StorageManager) mContext.getSystemService(Context.STORAGE_SERVICE);
        Class<?> storageVolumeClazz = null;
        try {
            storageVolumeClazz = Class.forName("android.os.storage.StorageVolume");
            Method getVolumeList = mStorageManager.getClass().getMethod("getVolumeList");
            Method getPath = storageVolumeClazz.getMethod("getPath");
            Method isRemovable = storageVolumeClazz.getMethod("isRemovable");
            Object result = getVolumeList.invoke(mStorageManager);
            final int length = Array.getLength(result);
            for (int i = 0; i < length; i++) {
                Object storageVolumeElement = Array.get(result, i);
                String path = (String) getPath.invoke(storageVolumeElement);
                boolean removable = (Boolean) isRemovable.invoke(storageVolumeElement);
                if (removable) {
                    return path;
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }
}
