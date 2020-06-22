package to.epac.factorycraft.pas.Handler;

import android.view.View;
import android.widget.Button;

import to.epac.factorycraft.pas.PaPlayer;

public class StopHandler implements Button.OnClickListener {
    @Override
    public void onClick(View view) {
        PaPlayer.mp.reset();
    }
}
