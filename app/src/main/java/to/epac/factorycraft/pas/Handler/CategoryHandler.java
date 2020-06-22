package to.epac.factorycraft.pas.Handler;

import android.view.View;
import android.widget.Button;

import to.epac.factorycraft.pas.CategoryLoader;

import static to.epac.factorycraft.pas.MainActivity.panelAdapter;
import static to.epac.factorycraft.pas.MainActivity.selectedCategory;

public class CategoryHandler implements Button.OnClickListener {
    @Override
    public void onClick(View view) {
        selectedCategory = view.getId();

        CategoryLoader.updateMessageList(view.getContext());
        panelAdapter.notifyDataSetChanged();
    }
}
