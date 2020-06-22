package to.epac.factorycraft.pas;

import android.Manifest;
import android.app.Activity;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import to.epac.factorycraft.pas.Handler.MessageListHandler;
import to.epac.factorycraft.pas.Handler.SendHandler;
import to.epac.factorycraft.pas.Handler.StopHandler;
import to.epac.factorycraft.pas.Handler.VolumeController;

import static to.epac.factorycraft.pas.Utils.getExtendedMemoryPath;

public class MainActivity extends AppCompatActivity {
    public static String selectedDb = "";

    public static Activity activity;

    // Raw txt file's text
    public static ArrayList<String> datas = new ArrayList<>();
    // All categories
    public static ArrayList<Category> categories = new ArrayList<>();
    public static int selectedCategory = 0;

    public static RecyclerView panel;
    public static PanelAdapter panelAdapter;

    public static Spinner MessageList;
    public static EditText Lang;
    public static SeekBar Volume;
    public static Button Send;
    public static Button Stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        activity = this;

        MessageList = findViewById(R.id.MessageList);
        Lang = findViewById(R.id.Lang);
        Volume = findViewById(R.id.Volume);
        Send = findViewById(R.id.Send);
        Stop = findViewById(R.id.Stop);
        panel = findViewById(R.id.panel);

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1001);
        }





    }

    /*private String[] getAssetFiles(String path) {
        ArrayList<String> items = new ArrayList<>();
        AssetManager assetManager = getApplicationContext().getAssets();
        try {
            for (String file : assetManager.list(path)) {
                if (file.endsWith(".txt"))
                    items.add(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        String[] file = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            file[i] = items.get(i);
        }

        return file;
    }*/

    private String[] getFiles(String path) {
        String path0 = getExtendedMemoryPath(this);
        File dir = new File(path0 + "/" + path);
        File[] directoryListing = dir.listFiles();

        ArrayList<String> items = new ArrayList<>();
        if (directoryListing != null) {
            for (File child : directoryListing) {
                if (child.getName().endsWith(".txt"))
                    items.add(child.getName());
            }
        }
        String[] file = new String[items.size()];
        for (int i = 0; i < items.size(); i++) {
            file[i] = items.get(i);
        }
        return file;
    }

    private void loadFile(String file) {

        BufferedReader reader = null;
        String path0 = getExtendedMemoryPath(this);
        File file0 = new File(path0 + "/" + file);
        try {
            //reader = new BufferedReader(new InputStreamReader(getAssets().open(file), "UTF-8"));
            reader = new BufferedReader(new FileReader(file0));

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\t", "    ");
                datas.add(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void classify() {
        for (int i = 0; i < datas.size(); i++) {
            String line = datas.get(i);
            if (line == null) break;

            // >01	Safety<
            if (Utils.isInteger(line.substring(0, line.indexOf(" ")))) {
                Category category = new Category();
                // >01<
                String id = line.substring(0, line.indexOf(" "));
                // >Safety<
                String name = line.substring(line.indexOf(" ") + 4);
                category.setId(id);
                category.setName(name);

                categories.add(category);
            }

            // >;#02     21E	KCR East Rail Extension<
            // >#02     22E	Ma On Shan Line<
            else if (line.startsWith("#") && Utils.isInteger(line.substring(1, line.indexOf(" "))) ||
                    (line.substring(0, 1).equals(";") &&
                            line.substring(1, 2).equals("#") &&
                            Utils.isInteger(line.substring(2, line.indexOf(" "))))) {

                // >;#02     21E	KCR East Rail Extension<
                boolean isDisabled = false;
                if (line.startsWith(";")) isDisabled = true;

                // Line itself but removed ";"
                // >#02     21E	KCR East Rail Extension<
                String theline = line.replaceFirst(";", "");

                // #02
                String prefix = theline.substring(0, theline.indexOf(" "));

                // >     21E	KCR East Rail Extension<
                theline = theline.substring(prefix.length());
                // >21E	KCR East Rail Extension<
                theline = theline.replaceFirst("\\s*", "");

                // >21E<
                String fullCode = theline.substring(0, theline.indexOf(" "));
                // >21<
                String id = fullCode.substring(0, fullCode.length() - 1);
                // >E<
                String lang = fullCode.substring(id.length(), id.length() + 1);

                // >    KCR East Rail Extension<
                String message = theline.substring(fullCode.length());
                // >KCR East Rail Extension<
                message = message.replaceFirst("\\s*", "");

                Content content = new Content();
                content.setId(id);
                content.setLang(lang);
                content.setMessage(message);

                // Create a new Category if it is a new one
                boolean isNewCategory = true;

                for (Category category : categories) {
                    if (category.getId().equals(prefix)) {
                        isNewCategory = false;
                        // If it belongs to an exist category, just simply add it here
                        category.addContent(content);
                    }
                }
                if (isNewCategory) {
                    Category category = new Category();
                    category.setId(prefix);
                    category.setName(prefix);
                    categories.add(category);
                    // If it belongs to a new category,  create a category first, then add it into the category
                    category.addContent(content);
                }
            }
            else {
                boolean isDisabled = false;
                if (line.startsWith(";")) isDisabled = true;

                // The line itself without ";"
                // >Appro   03.1E #04	Train for LW cross boundary with departure platform info (Train not yet come): The approaching train for Lo Wu will depart from<
                String theline = line.replaceFirst(";", "");

                // >Appro<
                String cat = theline.substring(0, 8);
                cat = cat.replaceAll("\\s*", "");

                // >   03.1E #04	Train for LW cross boundary with departure platform info (Train not yet come): The approaching train for Lo Wu will depart from<
                //theline = theline.substring(cat.length());
                theline = theline.substring(8);
                // >03.1E #04	Train for LW cross boundary with departure platform info (Train not yet come): The approaching train for Lo Wu will depart from<
                theline = theline.replaceFirst("\\s*", "");

                // >03.1E<
                String fullCode = theline.substring(0, theline.indexOf(" "));
                // >03.1<
                String fullid = fullCode.substring(0, fullCode.length() - 1);

                String id = fullid;
                String subid = "";
                if (fullid.contains(".")) {
                    // >03<
                    id = fullid.substring(0, fullid.indexOf("."));
                    // >1<
                    subid = fullid.substring(fullid.indexOf(".") + 1);
                }
                // >E<
                String lang = fullCode.substring(fullid.length(), fullid.length() + 1);

                // > #04	Train for LW cross boundary with departure platform info (Train not yet come): The approaching train for Lo Wu will depart from<
                theline = theline.substring(fullCode.length());
                // >#04	Train for LW cross boundary with departure platform info (Train not yet come): The approaching train for Lo Wu will depart from<
                theline = theline.replaceFirst("\\s*", "");

                String variable = "";
                if (theline.startsWith("#")) {
                    // >#04<
                    variable = theline.substring(0, theline.indexOf(" "));
                    theline = theline.substring(variable.length());
                }

                String fullMsg = theline.replaceFirst("\\s*", "");

                String title = "";
                String message = "";
                if (fullMsg.contains(":")) {
                    title = fullMsg.substring(0, fullMsg.indexOf(":"));
                    try {
                        message = fullMsg.substring(fullMsg.indexOf(":") + 2);
                    } catch (StringIndexOutOfBoundsException e) {
                        e.printStackTrace();
                    }
                }
                else {
                    title = fullMsg;
                }

                Content content = new Content();
                content.setDisabled(isDisabled);
                content.setId(id);
                content.setSubid(subid);
                content.setLang(lang);
                content.setTitle(title);
                content.setMessage(message);
                content.setVariable(variable);

                for (Category category : categories) {
                    if (category.getName().equals(cat))
                        category.addContent(content);
                }
            }
        }
    }

    private void showItems() {
        ScrollView debug = findViewById(R.id.debug);
        debug.setVisibility(View.VISIBLE);

        TextView tv = findViewById(R.id.tv);
        String msg = "";

        for (Category category : categories) {
            msg = msg + "Category Id: " + category.getId() + "    Name: " + category.getName() + "\n";

            for (Content content : category.getContents()) {
                if (content.getDisabled()) msg += ";";

                msg += "    Id: " + content.getId() + ":" + content.getSubid() + ":" + content.getLang() + "\n";
                msg += "    Title: " + content.getTitle() + "\n";
                msg += "    Msg: " + content.getMessage() + "\n";
                msg += "    Var: " + content.getVariable() + "\n\n";
            }
            msg += "\n----------End of Category----------\n\n";
        }
        tv.setText(msg);
        Log.d("tagg", msg);
    }
    private void showItems2() {
        ScrollView debug = findViewById(R.id.debug);
        debug.setVisibility(View.VISIBLE);

        TextView tv = findViewById(R.id.tv);
        String msg = "";

        for (Category category : categories) {
            if (category.getId().contains("#")) {
                msg = msg + "Category Id: " + category.getId() + "    Name: " + category.getName() + "\n";

                for (Content content : category.getContents()) {
                    if (content.getDisabled()) msg += ";";

                    msg += "    Id: " + content.getId() + ":" + content.getSubid() + ":" + content.getLang() + "\n";
                    msg += "    Title: " + content.getTitle() + "\n";
                    msg += "    Msg: " + content.getMessage() + "\n";
                    msg += "    Var: " + content.getVariable() + "\n\n";
                }
                msg += "\n----------End of Category----------\n\n";
            }
        }
        tv.setText(msg);
        Log.d("tagg", msg);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (requestCode == 1001) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Select a PAS database")
                        .setItems(getFiles("PAS"), new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                                MessageList.setOnItemSelectedListener(new MessageListHandler());
                                Volume.setOnSeekBarChangeListener(new VolumeController());
                                Send.setOnClickListener(new SendHandler());
                                Stop.setOnClickListener(new StopHandler());
                                panelAdapter = new PanelAdapter(getApplicationContext());
                                panel.setLayoutManager(new LinearLayoutManager(getApplicationContext())); panel.setAdapter(panelAdapter);

                                selectedDb = getFiles("PAS")[which];
                                //selectedDb = getAssetFiles("")[which];
                                loadFile("PAS/" + getFiles("PAS")[which]);
                                //loadFile(getAssetFiles("")[which]);
                                classify();
                                //showItems();
                                //showItems2();

                                CategoryLoader.applyCategoryToButton(activity);
                                CategoryLoader.updateMessageList(activity);

                            }
                        });
                builder.show();
            }
        }
    }
}
