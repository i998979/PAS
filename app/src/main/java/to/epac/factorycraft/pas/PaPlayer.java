package to.epac.factorycraft.pas;

import android.media.MediaPlayer;
import android.util.Log;
import android.view.View;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static to.epac.factorycraft.pas.Utils.getExtendedMemoryPath;

public class PaPlayer {
    public static MediaPlayer mp = new MediaPlayer();

    public static void play(final View view, final ArrayList<SoundPath> paths, String path) {
        Log.d("tagg", path);

        mp.reset();
        try {
            // **** Get files from asset **** //
            //AssetFileDescriptor afd = view.getContext().getAssets().openFd(path);
            //mp.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());

            // **** Get files from External Storage (Phone Storage) **** //
            //String path0 = Environment.getExternalStorageDirectory().getPath();

            // **** Get files from External Menory (SD Card) **** //
            String path0 = getExtendedMemoryPath(view.getContext());
            File file = new File(path0 + "/" + path);
            FileInputStream fin = new FileInputStream(file);
            mp.setDataSource(fin.getFD());

            fin.close();

            mp.prepareAsync();

        } catch (IOException e) {
            e.printStackTrace();
            paths.remove(0);
            if (paths.size() > 0)
                play(view, paths, paths.get(0).getPath());
            else {
                mp.reset();
                return;
            }
        }

        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                paths.remove(0);
                if (paths.size() > 0)
                    play(view, paths, paths.get(0).getPath());
            }
        });

        mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                mp.start();
            }
        });
    }
}
