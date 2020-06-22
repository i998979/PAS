package to.epac.factorycraft.pas.Handler;

import android.media.audiofx.LoudnessEnhancer;
import android.widget.SeekBar;

import static to.epac.factorycraft.pas.PaPlayer.mp;

public class VolumeController implements SeekBar.OnSeekBarChangeListener {
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.KITKAT) {

            LoudnessEnhancer le = new LoudnessEnhancer(mp.getAudioSessionId());
            le.setEnabled(true);
            le.setTargetGain(progress * 1000);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }
}
