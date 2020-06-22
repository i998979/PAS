package to.epac.factorycraft.pas;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import static to.epac.factorycraft.pas.MainActivity.MessageList;
import static to.epac.factorycraft.pas.MainActivity.selectedCategory;

public class PanelAdapter extends RecyclerView.Adapter<PanelAdapter.ViewHolder> {

    private Context context;

    public static ArrayList<Component> components = new ArrayList<>();

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public LinearLayout panelLayout;

        public ViewHolder(View itemView) {
            super(itemView);
            panelLayout = itemView.findViewById(R.id.Panel);
        }
    }

    public PanelAdapter(Context context) {
        this.context = context;
    }

    // TODO - panelLayout data will duplicate after OnBackPress and return

    @Override
    public PanelAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.panel, parent, false);

        ViewHolder holder = new ViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.panelLayout.removeAllViews();
        components.clear();

        // All Contents in the selected Category
        ArrayList<Content> ContentList = Utils.getCategoryContents(selectedCategory, false);

        // Get which PA selected
        String selectedId;
        try {
            selectedId = ContentList.get(MessageList.getSelectedItemPosition()).getId();
        } catch (ArrayIndexOutOfBoundsException e) {
            e.printStackTrace();
            return;
        }

        ArrayList<Content> selectedPAcontentList = Utils.getSelectedPaContents(selectedCategory, selectedId, false);

        for (Content content : selectedPAcontentList) {

            String fullid = content.getId();
            if (!content.getSubid().equals("")) fullid += "." + content.getSubid();

            /* ##### TextView ##### */
            TextView tv = new TextView(context);

            if (content.getMessage().equals(""))
                tv.setText(content.getTitle());
            else
                tv.setText(content.getMessage());

            components.add(new Component(fullid, tv));
            holder.panelLayout.addView(tv);

            /* ##### Spinner ##### */
            Spinner varSpinner = new Spinner(context);

            // If the content has variable to load
            if (!content.getVariable().equals("")) {
                String varType = content.getVariable();

                ArrayList<Content> varList = Utils.getCategoryContents(varType, false);

                ArrayList<String> msgPreviewList = new ArrayList<>();
                for (Content item : varList) {
                    msgPreviewList.add(item.getId() + " " + item.getMessage());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(context,android.R.layout.simple_spinner_dropdown_item, msgPreviewList);
                varSpinner.setAdapter(adapter);

                components.add(new Component(fullid, varType, varSpinner));
                holder.panelLayout.addView(varSpinner);

            }
        }
    }

    @Override
    public int getItemCount() {
        return 1;
    }
}
