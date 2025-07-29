package to.epac.factorycraft.pas;

import android.app.Activity;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.media.audiofx.LoudnessEnhancer;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.documentfile.provider.DocumentFile;
import androidx.media3.common.Player;
import androidx.media3.common.util.UnstableApi;
import androidx.media3.exoplayer.ExoPlayer;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import to.epac.factorycraft.pas.components.Category;
import to.epac.factorycraft.pas.components.Component;
import to.epac.factorycraft.pas.components.Content;

public class MainActivity extends AppCompatActivity {
    // Raw txt file's text
    public static ArrayList<String> data = new ArrayList<>();
    // All categories
    public static ArrayList<Category> categories = new ArrayList<>();
    // All components
    public static ArrayList<Component> components = new ArrayList<>();

    public static String selectedDB = "";
    public static String selectedFolder = "";
    public static Map<String, DocumentFile> audios = new HashMap<>();
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


        ActivityResultLauncher<Intent> folderPicker = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> {
                    if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                        Uri folderUri = result.getData().getData();
                        if (folderUri != null) {
                            getContentResolver().takePersistableUriPermission(
                                    folderUri,
                                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                            );

                            selectedFolder = folderUri.toString();

                            Toast.makeText(this, "Loading folder...", Toast.LENGTH_SHORT).show();

                            CompletableFuture.runAsync(() -> {
                                DocumentFile folderDoc = DocumentFile.fromTreeUri(this, folderUri);
                                if (folderDoc != null && folderDoc.exists()) {
                                    DocumentFile databaseTxt = null;

                                    for (DocumentFile file : folderDoc.listFiles()) {
                                        String name = file.getName().toLowerCase();

                                        // Find first .txt file
                                        if (databaseTxt == null && file.isFile() && name.endsWith(".txt"))
                                            databaseTxt = file;
                                        else
                                            // It should be audio file, put it into cache
                                            audios.put(name, file);
                                    }

                                    if (databaseTxt != null) {
                                        selectedDB = databaseTxt.getUri().toString();

                                        runOnUiThread(() -> {
                                            loadData();
                                            categorize();
                                            applyCategoryToButton();
                                            updateMessageList();

                                            Toast.makeText(this, "Folder loaded successfully", Toast.LENGTH_SHORT).show();
                                        });
                                    } else {
                                        runOnUiThread(() -> Toast.makeText(this, "No .txt file found in folder", Toast.LENGTH_SHORT).show());
                                    }
                                }
                            });
                        }
                    }
                }
        );


        new AlertDialog.Builder(this)
                .setTitle("Select PAS folder with .txt and audios")
                .setPositiveButton("Select", (dialog, which) -> {
                    Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION |
                            Intent.FLAG_GRANT_WRITE_URI_PERMISSION |
                            Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                    folderPicker.launch(intent);
                })
                .show();


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


            PaPlayer.play(v.getContext(), sortedPath);
        });

        stop.setOnClickListener(v -> PaPlayer.stop());
    }

    private void loadData() {
        try {
            Uri fileUri = Uri.parse(selectedDB);
            InputStream inputStream = getContentResolver().openInputStream(fileUri);
            if (inputStream == null) {
                Toast.makeText(this, "Unable to open file", Toast.LENGTH_SHORT).show();
                return;
            }

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;

            while ((line = reader.readLine()) != null) {
                data.add(line);
            }

            reader.close();
            inputStream.close();

            Toast.makeText(this, "File read into lines: " + data.size(), Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
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
