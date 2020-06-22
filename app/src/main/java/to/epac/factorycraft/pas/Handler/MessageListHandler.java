package to.epac.factorycraft.pas.Handler;

import android.view.View;
import android.widget.AdapterView;

import static to.epac.factorycraft.pas.MainActivity.panelAdapter;

public class MessageListHandler implements AdapterView.OnItemSelectedListener {
    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        panelAdapter.notifyDataSetChanged();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
}
