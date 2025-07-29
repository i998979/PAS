package to.epac.factorycraft.pas;

import static to.epac.factorycraft.pas.MainActivity.audios;
import static to.epac.factorycraft.pas.MainActivity.player;

import android.content.Context;
import android.net.Uri;

import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.MediaItem;

import java.util.ArrayList;
import java.util.List;

public class PaPlayer {

    public static void play(Context context, ArrayList<SoundPath> paths) {
        if (paths == null || paths.isEmpty()) return;

        Uri folderUri = Uri.parse(MainActivity.selectedFolder);

        // Access the folder
        DocumentFile parentFolder = DocumentFile.fromTreeUri(context, folderUri);
        if (parentFolder == null || !parentFolder.isDirectory()) return;

        Uri dbUri = Uri.parse(MainActivity.selectedDB);
        DocumentFile dbFile = DocumentFile.fromSingleUri(context, dbUri);
        if (dbFile == null || !dbFile.exists()) return;


        // Initialize player
        player.stop();
        player.clearMediaItems();


        // Match and collect playable media items
        List<MediaItem> mediaItems = new ArrayList<>();
        for (SoundPath sound : paths) {
            String targetName = sound.getPath().toLowerCase();
            DocumentFile audioFile = audios.get(targetName);

            if (audioFile != null && audioFile.isFile()) {
                Uri fileUri = audioFile.getUri();
                mediaItems.add(MediaItem.fromUri(fileUri));
            }
        }

        for (MediaItem item : mediaItems) {
            player.addMediaItem(item);
        }

        player.prepare();
        player.play();
    }


    public static void stop() {
        player.stop();
        player.clearMediaItems();
    }
}
