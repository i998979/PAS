package to.epac.factorycraft.pas;

import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridLayout;

import java.util.ArrayList;

import to.epac.factorycraft.pas.Handler.CategoryHandler;

import static to.epac.factorycraft.pas.MainActivity.MessageList;
import static to.epac.factorycraft.pas.MainActivity.activity;
import static to.epac.factorycraft.pas.MainActivity.categories;
import static to.epac.factorycraft.pas.MainActivity.selectedCategory;

public class CategoryLoader {

    public static void applyCategoryToButton(Context context) {
        GridLayout buttonLayout = activity.findViewById(R.id.buttonLayout);

        // TODO - Add Category Button according to their ID but not affect clicking on them
        /*for (int i = 0; i < 14; i++) {
            Button button = new Button(context);
            button.setAllCaps(false);
            button.setId(i);
            button.setText("");

            for (Category category : categories) {
                if (!category.getId().startsWith("#")) {
                    if (Integer.valueOf(category.getId()) == i) {
                        button.setText(category.getName());
                        button.setOnClickListener(new CategoryHandler());
                    }
                }
            }
            buttonLayout.addView(button);
        }*/

        for (int i = 0; i < 14; i++) {
            if (!categories.get(i).getName().startsWith("#")) {
                Button button = new Button(context);
                button.setAllCaps(false);
                button.setText(categories.get(i).getName());
                button.setId(i);
                button.setOnClickListener(new CategoryHandler());
                buttonLayout.addView(button);
            }
        }
    }

    public static void updateMessageList(Context context) {
        ArrayList<String> SpinnerList = new ArrayList<>();

        for (Content content : Utils.getCategoryContents(selectedCategory, false)) {
            String name = categories.get(selectedCategory).getName();
            String id = content.getId();
            String title = content.getTitle();
            SpinnerList.add(name + " " + id + " " + title);
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item, SpinnerList);

        MessageList.setAdapter(adapter);
    }
}
