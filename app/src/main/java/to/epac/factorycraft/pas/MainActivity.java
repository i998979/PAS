package to.epac.factorycraft.pas;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.audiofx.LoudnessEnhancer;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.oauth.DbxCredential;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.FileMetadata;
import com.dropbox.core.v2.files.FolderMetadata;
import com.dropbox.core.v2.files.ListFolderResult;
import com.dropbox.core.v2.files.Metadata;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;

import to.epac.factorycraft.pas.components.Category;
import to.epac.factorycraft.pas.components.Component;
import to.epac.factorycraft.pas.components.Content;

public class MainActivity extends AppCompatActivity {
    public static DbxClientV2 dbxClient;

    // Prevent scanning the folders every time the screen is locked/unlocked
    private boolean isConnectedAndScanned = false;
    private AlertDialog connectDialog;

    // Raw txt file's text
    public static ArrayList<String> data = new ArrayList<>();
    // All categories
    public static ArrayList<Category> categories = new ArrayList<>();
    // All components
    public static ArrayList<Component> components = new ArrayList<>();

    public static String selectedDbxFolder = "";

    public static int selectedCategory = 0;
    public static String languageOrder = "CPE";

    public static ExoPlayer player;
    public LoudnessEnhancer enhancer;

    public GridLayout buttonLayout;

    public LinearLayout panel;

    public Spinner messageList;
    public EditText langOrder;
    public SeekBar volume;
    public Button send;
    public Button stop;

    @OptIn(markerClass = UnstableApi.class)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        player = new ExoPlayer.Builder(this).build();
        player.addListener(new Player.Listener() {
            @Override
            public void onAudioSessionIdChanged(int audioSessionId) {
                enhancer = new LoudnessEnhancer(audioSessionId);
                enhancer.setEnabled(true);
            }
        });

        buttonLayout = findViewById(R.id.buttonLayout);

        messageList = findViewById(R.id.messageList);
        langOrder = findViewById(R.id.langOrder);
        volume = findViewById(R.id.volume);
        send = findViewById(R.id.send);
        stop = findViewById(R.id.stop);

        panel = findViewById(R.id.panel);

        messageList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                panel.removeAllViews();

                // All Contents in the selected Category
                ArrayList<Content> categoryContents = Utils.getCategoryContents(selectedCategory, false);

                // Get which PA selected
                String selectedId;
                if (messageList.getSelectedItemPosition() < categoryContents.size())
                    selectedId = categoryContents.get(messageList.getSelectedItemPosition()).getId();
                else
                    return;

                ArrayList<Content> selectedPAcontentList = Utils.getSelectedPaContents(selectedCategory, selectedId, false);

                for (Content content : selectedPAcontentList) {
                    String fullId = content.getId();
                    if (!content.getSubId().isEmpty()) fullId += "." + content.getSubId();

                    /* ##### TextView ##### */
                    TextView tv = new TextView(MainActivity.this);

                    if (content.getMessage().isEmpty())
                        tv.setText(content.getTitle());
                    else
                        tv.setText(content.getMessage());

                    components.add(new Component(fullId, tv));
                    panel.addView(tv);

                    /* ##### Spinner ##### */
                    Spinner varSpinner = new Spinner(MainActivity.this);

                    // If the content has variable to load
                    if (!content.getVariable().isEmpty()) {
                        String varType = content.getVariable();

                        ArrayList<Content> varList = Utils.getCategoryContents(varType, false);

                        ArrayList<String> msgPreviewList = new ArrayList<>();
                        for (Content item : varList) {
                            msgPreviewList.add(item.getId() + " " + item.getMessage());
                        }

                        ArrayAdapter<String> adapter = new ArrayAdapter<>(MainActivity.this, android.R.layout.simple_spinner_dropdown_item, msgPreviewList);
                        varSpinner.setAdapter(adapter);

                        components.add(new Component(fullId, varSpinner));
                        panel.addView(varSpinner);
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        langOrder.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                languageOrder = s.toString();
            }
        });

        volume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                enhancer.setTargetGain(progress * 1000);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        send.setOnClickListener(v -> {
            ArrayList<SoundPath> paths = new ArrayList<>();

            // All Contents in the selected Category
            ArrayList<Content> categoryContents = Utils.getCategoryContents(selectedCategory, false);

            // Get which PA selected
            String selectedMessageId;
            if (messageList.getSelectedItemPosition() < categoryContents.size())
                selectedMessageId = categoryContents.get(messageList.getSelectedItemPosition()).getId();
            else
                return;


            // Get selected PA's all Contents
            // **** THIS TIME WE NEED ALL LANGUAGE **** //
            ArrayList<Content> selectedPAcontentList = Utils.getSelectedPaContents(selectedCategory, selectedMessageId, true);

            String categoryName = categories.get(selectedCategory).getName();

            for (Content content : selectedPAcontentList) {
                String fullId = content.getId() + content.getSubId();

                String lang = content.getLang();

                paths.add(new SoundPath(content.getLang(), categoryName.split("(?<=\\G.{3})")[0].toUpperCase() + fullId + lang + ".mp3"));

                if (!content.getVariable().isEmpty()) {
                    String varType = content.getVariable();
                    String title = content.getTitle();

                    if (title.startsWith(">")) {
                        int warpTo = Character.getNumericValue(title.charAt(1));

                        for (Component component : components) {
                            if (component.getId().equals(content.getId() + "." + warpTo) && component.getComponent() instanceof Spinner) {

                                Spinner spinner = (Spinner) component.getComponent();
                                int selected = spinner.getSelectedItemPosition();

                                ArrayList<Content> variableList = Utils.getCategoryContents(varType, false);

                                Content selectedVar = variableList.get(selected);

                                String id = selectedVar.getId();
                                if (!selectedVar.getSubId().isEmpty())
                                    id += selectedVar.getSubId();

                                paths.add(new SoundPath(content.getLang(), varType.split("(?<=\\G.{3})")[0].toUpperCase() + id + lang + ".mp3"));
                                break;
                            }
                        }
                    } else {
                        for (Component component : components) {
                            String compId = component.getId().replace(".", "");

                            if (compId.equals(fullId) && component.getComponent() instanceof Spinner) {

                                Spinner spinner = (Spinner) component.getComponent();
                                int selected = spinner.getSelectedItemPosition();

                                ArrayList<Content> VariableList = Utils.getCategoryContents(varType, false);

                                Content selectedVar = VariableList.get(selected);

                                String id = selectedVar.getId();
                                if (selectedVar.getSubId().isEmpty())
                                    id += selectedVar.getSubId();

                                paths.add(new SoundPath(content.getLang(), varType.split("(?<=\\G.{3})")[0].toUpperCase() + id + lang + ".mp3"));

                                break;
                            }
                        }
                    }
                }
            }


            ArrayList<SoundPath> sortedPath = new ArrayList<>();
            for (char lang : languageOrder.toCharArray()) {
                for (SoundPath soundPath : paths) {
                    if (soundPath.getLang().charAt(0) == lang) {
                        sortedPath.add(soundPath);
                    }
                }
            }

            if (dbxClient == null) return;
            Toast.makeText(MainActivity.this, "Downloading tracks...", Toast.LENGTH_SHORT).show();
            send.setEnabled(false);

            new Thread(() -> {
                ArrayList<String> localCachePaths = new ArrayList<>();
                try {
                    String folderPath = selectedDbxFolder;
                    if (folderPath.equals("/")) {
                        folderPath = "";
                    }

                    ListFolderResult folderResult = dbxClient.files().listFolder(folderPath);
                    ArrayList<Metadata> cloudFiles = new ArrayList<>(folderResult.getEntries());

                    for (SoundPath soundPath : sortedPath) {
                        String targetFileName = soundPath.getPath();

                        String targetBaseName = targetFileName;
                        if (targetFileName.contains(".")) {
                            targetBaseName = targetFileName.substring(0, targetFileName.lastIndexOf("."));
                        }

                        String actualDbxPath = null;
                        String actualCloudFileName = null;

                        for (Metadata metadata : cloudFiles) {
                            String cloudName = metadata.getName();
                            String cloudBaseName = cloudName;
                            if (cloudName.contains(".")) {
                                cloudBaseName = cloudName.substring(0, cloudName.lastIndexOf("."));
                            }

                            if (cloudBaseName.equalsIgnoreCase(targetBaseName)) {
                                actualDbxPath = metadata.getPathLower();
                                actualCloudFileName = cloudName;
                                break;
                            }
                        }

                        if (actualDbxPath != null) {
                            File cacheFile = new File(getCacheDir(), actualCloudFileName);
                            try (FileOutputStream out = new FileOutputStream(cacheFile)) {
                                dbxClient.files().download(actualDbxPath).download(out);
                                localCachePaths.add(cacheFile.getAbsolutePath());
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e("PAS_DEBUG", "Track not found: " + targetBaseName);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                runOnUiThread(() -> {
                    send.setEnabled(true);
                    if (!localCachePaths.isEmpty()) {
                        PaPlayer.play(localCachePaths);
                    } else {
                        Toast.makeText(MainActivity.this, "No playable tracks downloaded.", Toast.LENGTH_SHORT).show();
                    }
                });
            }).start();
        });

        stop.setOnClickListener(v -> PaPlayer.stop());
    }

    @Override
    protected void onResume() {
        super.onResume();

        String appKey = BuildConfig.DROPBOX_APP_KEY;

        SharedPreferences prefs = getSharedPreferences("dropbox-prefs", MODE_PRIVATE);
        String savedAccessToken = prefs.getString("access-token", null);
        String savedRefreshToken = prefs.getString("refresh-token", null);
        long expiresAt = prefs.getLong("expires-at", -1);

        // 2. Fetch returned credentials from Auth flow
        DbxCredential credential = null;
        String legacyToken = null;
        try {
            credential = Auth.getDbxCredential(); // Modern short-lived token
            Log.d("tagg", credential.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (credential == null) {
            legacyToken = Auth.getOAuth2Token(); // Fallback for older configurations
        }

        if (credential != null) {
            // Save newly fetched short-lived token
            prefs.edit()
                    .putString("access-token", credential.getAccessToken())
                    .putString("refresh-token", credential.getRefreshToken())
                    .putLong("expires-at", credential.getExpiresAt() != null ? credential.getExpiresAt() : -1)
                    .apply();
            initDropboxClient(credential);

        } else if (legacyToken != null) {
            // Save newly fetched long-lived token
            prefs.edit().putString("access-token", legacyToken).apply();
            initDropboxClient(new DbxCredential(legacyToken, null, null, appKey));

        } else if (savedAccessToken != null) {
            // Re-use saved token from previous sessions
            DbxCredential cred;
            if (savedRefreshToken != null) {
                cred = new DbxCredential(savedAccessToken, expiresAt == -1 ? null : expiresAt, savedRefreshToken, appKey);
            } else {
                cred = new DbxCredential(savedAccessToken, null, null, appKey);
            }
            initDropboxClient(cred);

        } else {
            // Ask user to connect (Only if completely unauthorized)
            if (connectDialog == null || !connectDialog.isShowing()) {
                connectDialog = new AlertDialog.Builder(this)
                        .setTitle("Connect to Dropbox")
                        .setMessage("Authorization required to fetch database (.txt) files.")
                        .setPositiveButton("Connect", (dialog, which) -> {
                            // 3. Start PKCE flow which correctly supports Modern Short-Lived tokens
                            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/pas").build();
                            Auth.startOAuth2PKCE(MainActivity.this, appKey, config);
                        })
                        .setCancelable(false)
                        .show();
            }
        }
    }

    private void initDropboxClient(DbxCredential credential) {
        if (dbxClient == null) {
            DbxRequestConfig config = DbxRequestConfig.newBuilder("dropbox/pas").build();
            dbxClient = new DbxClientV2(config, credential);
        }

        // Make sure we ONLY scan once, preventing overlapping/resetting the app UI
        if (!isConnectedAndScanned) {
            isConnectedAndScanned = true;
            scanDropboxFolders();
        }
    }

    private void scanDropboxFolders() {
        Toast.makeText(this, "Scanning Dropbox folders...", Toast.LENGTH_SHORT).show();
        new Thread(() -> {
            try {
                ArrayList<String> txtFiles = new ArrayList<>();

                Queue<String> folderQueue = new LinkedList<>();
                folderQueue.add("");

                while (!folderQueue.isEmpty()) {
                    String currentFolder = folderQueue.poll();
                    ListFolderResult result = dbxClient.files().listFolder(currentFolder);

                    for (Metadata metadata : result.getEntries()) {
                        if (metadata instanceof FolderMetadata) {
                            folderQueue.add(metadata.getPathLower());
                        } else if (metadata instanceof FileMetadata) {
                            if (metadata.getName().toLowerCase().endsWith(".txt")) {
                                txtFiles.add(metadata.getPathDisplay());
                            }
                        }
                    }
                }

                runOnUiThread(() -> {
                    if (txtFiles.isEmpty()) {
                        Toast.makeText(this, "No .txt file found in any folder", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String[] items = txtFiles.toArray(new String[0]);
                    new AlertDialog.Builder(MainActivity.this)
                            .setTitle("Select Database (.txt)")
                            .setItems(items, (d, w) -> {
                                String selectedPath = items[w];

                                int lastSlash = selectedPath.lastIndexOf("/");
                                selectedDbxFolder = lastSlash > 0 ? selectedPath.substring(0, lastSlash) : "";

                                new Thread(() -> {
                                    try {
                                        InputStream in = dbxClient.files().download(selectedPath).getInputStream();
                                        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                                        String line;
                                        data.clear();
                                        categories.clear();
                                        components.clear();

                                        while ((line = reader.readLine()) != null) {
                                            data.add(line);
                                        }
                                        reader.close();
                                        in.close();

                                        runOnUiThread(() -> {
                                            categorize();
                                            applyCategoryToButton();
                                            updateMessageList();
                                            Toast.makeText(MainActivity.this, "Database Loaded from: " + selectedPath, Toast.LENGTH_LONG).show();
                                        });
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                        runOnUiThread(() -> Toast.makeText(MainActivity.this, "Error reading file", Toast.LENGTH_SHORT).show());
                                    }
                                }).start();
                            })
                            .setCancelable(false)
                            .show();
                });
            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(this, "Dropbox scan failed. Try restarting app.", Toast.LENGTH_SHORT).show();
                    // Optional: isConnectedAndScanned = false; // if you want them to be able to try scanning again
                });
            }
        }).start();
    }

    private void categorize() {
        for (String line : data) {
            if (line.isEmpty()) continue;

            // Normalize spacing: convert any sequence of spaces or tabs into a single tab
            line = line.trim().replaceAll("\t", " ");

            // Category
            // >01	Safety<
            if (Utils.isInteger(line.substring(0, line.indexOf(" ")))) {
                // >01<
                String id = line.substring(0, line.indexOf(" "));
                // >Safety<
                String name = line.substring(line.indexOf(" ") + 1).trim();

                categories.add(new Category(id, name));
                continue;
            }


            // Variables, category may not be declared on its first run
            // >;#02     21E	KCR East Rail Extension<
            // >#02     22E	Ma On Shan Line<
            if ((line.startsWith("#") && Utils.isInteger(line.substring(1, line.indexOf(" ")))) ||
                    (line.startsWith(";#") && Utils.isInteger(line.substring(2, line.indexOf(" "))))) {
                // >;#02     21E	KCR East Rail Extension<
                boolean disabled = line.startsWith(";");

                // >#02     21E	KCR East Rail Extension<
                String varCatLine = line.replaceFirst(";", "");
                // >#02<
                String varCat = varCatLine.substring(0, varCatLine.indexOf(" "));


                // >     21E KCR East Rail Extension<
                // >21E	KCR East Rail Extension<
                varCatLine = varCatLine.substring(varCat.length()).trim();


                // >21E<
                String varCode = varCatLine.substring(0, varCatLine.indexOf(" "));
                // >21<
                String varId = varCode.substring(0, varCode.length() - 1);
                // >E<
                String varLang = varCode.substring(varId.length(), varId.length() + 1);

                // >KCR East Rail Extension<
                String varMsg = varCatLine.substring(varCode.length()).trim();


                Content content = new Content(varId, varLang, varMsg, disabled);

                // Create a new Category if it is a new one
                Category category = categories.stream()
                        .filter(cat -> cat.getId().equals(varCat)).findFirst().orElse(null);
                if (category == null) {
                    // ID: #01        Name: #01
                    category = new Category(varCat, varCat);
                    categories.add(category);
                }

                category.getContents().add(content);
            }

            // PA lines, category should be declared already
            // Appro   03.1C #04	.
            // Appro   03.1E #04	Train for LW cross boundary with departure platform info (Train not yet come): The approaching train for Lo Wu will depart from
            // Appro   03.1P #04	.
            else {
                boolean disabled = line.startsWith(";");

                // The line itself without ";"
                // >Appro   03.1E #04	Train for LW cross boundary with departure platform info (Train not yet come): The approaching train for Lo Wu will depart from<
                String contentLine = line.replaceFirst(";", "");

                // >Appro   <
                // >Appro<
                String cat = contentLine.substring(0, 8).trim();


                // >   03.1E #04	Train for ...<
                // >03.1E #04	Train for ...<
                contentLine = contentLine.substring(cat.length()).trim();


                // >03.1E<
                String fullCode = contentLine;
                if (contentLine.contains(" "))
                    fullCode = contentLine.substring(0, contentLine.indexOf(" "));
                // >03.1<
                String fullId = fullCode.substring(0, fullCode.length() - 1);

                String id = fullId;
                String subId = "";
                String lang = fullCode.substring(id.length());
                if (fullId.contains(".")) {
                    // >03<
                    id = fullId.substring(0, fullId.indexOf("."));
                    // >1<
                    subId = fullId.substring(id.length() + 1);
                    // >03.1E<
                    // >E<
                    lang = fullCode.substring(id.length() + 1 + subId.length());
                }

                // > #04	Train for ...<      >	Train for LW cross boundary (Train not yet come): Your attention please. The approaching train is going to Lo Wu.<
                // >#04	Train for ...<          >Train for LW cross boundary (Train not yet come): Your attention please. The approaching train is going to Lo Wu.<
                contentLine = contentLine.substring(fullCode.length()).trim();


                // (Optional)
                String variable = "";
                if (contentLine.startsWith("#")) {
                    // >#04<
                    variable = contentLine.substring(0, contentLine.indexOf(" "));

                    // >	Train for ...<
                    // >Train for ...<
                    contentLine = contentLine.substring(variable.length()).trim();
                }


                String title;
                String message = "";

                // >Train for LW cross boundary (Train not yet come): Your attention please. The approaching train is going to Lo Wu.<
                if (contentLine.contains(":")) {
                    // Train for LW cross boundary (Train not yet come)<
                    title = contentLine.substring(0, contentLine.indexOf(":"));
                    // >Your attention please. The approaching train is going to Lo Wu.<
                    message = contentLine.substring(title.length() + 1).trim();
                } else {
                    title = contentLine;
                }

                Content content = new Content(id, subId, lang, title, message, variable, disabled);
                for (Category category : categories) {
                    if (category.getName().equals(cat))
                        category.getContents().add(content);
                }
            }
        }
    }

    private void applyCategoryToButton() {
        for (int i = 0; i < 14; i++) {
            Button button = new Button(this);
            button.setId(i);
            button.setAllCaps(false);
            button.setTextColor(Color.parseColor("#000000"));
            button.setBackgroundTintList(ColorStateList.valueOf(Color.parseColor("#c0c0c0")));

            if (i < categories.size() && !categories.get(i).getName().startsWith("#")) {
                String name = categories.get(i).getName();
                button.setText(name);
                button.setEnabled(true);

                button.setOnClickListener(v -> {
                    selectedCategory = v.getId();
                    updateMessageList();
                });
            } else {
                button.setText("");
                button.setEnabled(false);
            }

            buttonLayout.addView(button);
        }
    }

    private void updateMessageList() {
        panel.removeAllViews();

        ArrayList<String> spinnerList = new ArrayList<>();

        for (Content content : Utils.getCategoryContents(selectedCategory, false)) {
            spinnerList.add(categories.get(selectedCategory).getName() + " " + content.getId() + " " + content.getTitle());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, spinnerList);
        messageList.setAdapter(adapter);
    }
}
