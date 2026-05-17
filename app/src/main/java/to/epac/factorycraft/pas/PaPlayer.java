package to.epac.factorycraft.pas;

import static to.epac.factorycraft.pas.MainActivity.player;

import android.net.Uri;

import androidx.media3.common.MediaItem;
import androidx.media3.common.Player;

import java.io.File;
import java.util.ArrayList;

public class PaPlayer {

    private static Player.Listener cleanupListener;

    public static void play(ArrayList<String> localPaths) {
        if (localPaths == null || localPaths.isEmpty()) return;

        player.stop();
        player.clearMediaItems();

        if (cleanupListener != null) {
            player.removeListener(cleanupListener);
        }

        for (String path : localPaths) {
            player.addMediaItem(MediaItem.fromUri(Uri.fromFile(new File(path))));
        }

        cleanupListener = new Player.Listener() {
            @Override
            public void onPlaybackStateChanged(int playbackState) {
                if (playbackState == Player.STATE_ENDED || playbackState == Player.STATE_IDLE) {
                    for (String path : localPaths) {
                        File cacheFile = new File(path);
                        if (cacheFile.exists()) {
                            cacheFile.delete();
                        }
                    }
                }
            }
        };
        player.addListener(cleanupListener);

        player.prepare();
        player.play();
    }

    public static void stop() {
        player.stop();
        player.clearMediaItems();
    }
}