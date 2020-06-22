package to.epac.factorycraft.pas.Handler;

import android.view.View;
import android.widget.Button;
import android.widget.Spinner;

import java.util.ArrayList;

import to.epac.factorycraft.pas.Component;
import to.epac.factorycraft.pas.Content;
import to.epac.factorycraft.pas.PaPlayer;
import to.epac.factorycraft.pas.SoundPath;
import to.epac.factorycraft.pas.Utils;

import static to.epac.factorycraft.pas.MainActivity.Lang;
import static to.epac.factorycraft.pas.MainActivity.MessageList;
import static to.epac.factorycraft.pas.MainActivity.categories;
import static to.epac.factorycraft.pas.MainActivity.selectedCategory;
import static to.epac.factorycraft.pas.MainActivity.selectedDb;
import static to.epac.factorycraft.pas.PanelAdapter.components;

public class SendHandler implements Button.OnClickListener {
    @Override
    public void onClick(View view) {

        ArrayList<SoundPath> paths = new ArrayList<>();

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

        // Get selected PA's all Contents
        // **** THIS TIME WE NEED ALL LANGUAGE **** //
        ArrayList<Content> selectedPAcontentList = Utils.getSelectedPaContents(selectedCategory, selectedId, true);

        String categoryName = categories.get(selectedCategory).getName();

        for (Content content : selectedPAcontentList) {

            String fullid = content.getId();
            if (!content.getSubid().equals("")) {
                if (selectedDb.equals("LMC_database.txt"))
                    fullid += "." + content.getSubid();
                else if (selectedDb.equals("Database.txt"))
                    fullid += content.getSubid();
            }

            String lang = content.getLang();

            if (selectedDb.equals("LMC_database.txt"))
                paths.add(new SoundPath(content.getLang(), "PAS/KCR/" + categoryName + "/" + fullid + lang + ".mp3"));
            else if (selectedDb.equals("Database.txt"))
                paths.add(new SoundPath(content.getLang(), "PAS/MTR/" + categoryName.split("(?<=\\G.{3})")[0].toUpperCase() + fullid + lang + ".mp3"));

            if (!content.getVariable().equals("")) {
                String vartype = content.getVariable();
                String title = content.getTitle();

                if (title.startsWith(">")) {
                    int warpto = Character.getNumericValue(title.charAt(1));

                    for (Component component : components) {
                        if (component.getId().equals(content.getId() + "." + warpto) && component.getComponent() instanceof Spinner) {

                            Spinner spinner = (Spinner) component.getComponent();
                            int selected = spinner.getSelectedItemPosition();

                            ArrayList<Content> VariableList = Utils.getCategoryContents(vartype, false);

                            Content selectedVar = VariableList.get(selected);

                            String id = selectedVar.getId();
                            if (!selectedVar.getSubid().equals("")) {
                                if (selectedDb.equals("LMC_database.txt"))
                                    id += "." + selectedVar.getSubid();
                                else if (selectedDb.equals("Database.txt"))
                                    id += selectedVar.getSubid();
                            }

                            if (selectedDb.equals("LMC_database.txt"))
                                paths.add(new SoundPath(content.getLang(), "PAS/KCR/" + vartype + "/" + id + lang + ".mp3"));
                            else if (selectedDb.equals("Database.txt"))
                                paths.add(new SoundPath(content.getLang(), "PAS/MTR/" + vartype.split("(?<=\\G.{3})")[0].toUpperCase() + id + lang + ".mp3"));
                            break;
                        }
                    }
                }
                else {
                    for (Component component : components) {
                        String compId = "";
                        if (selectedDb.equals("LMC_database.txt"))
                            compId = component.getId();
                        else if (selectedDb.equals("Database.txt"))
                            compId = component.getId().replace(".", "");

                        if (compId.equals(fullid) && component.getComponent() instanceof Spinner) {

                            Spinner spinner = (Spinner) component.getComponent();
                            int selected = spinner.getSelectedItemPosition();

                            ArrayList<Content> VariableList = Utils.getCategoryContents(vartype, false);

                            Content selectedVar = VariableList.get(selected);

                            String id = selectedVar.getId();
                            if (!selectedVar.getSubid().equals("")) {
                                if (selectedDb.equals("LMC_database.txt"))
                                    id += "." + selectedVar.getSubid();
                                else if (selectedDb.equals("Database.txt"))
                                    id += selectedVar.getSubid();
                            }

                            if (selectedDb.equals("LMC_database.txt"))
                                paths.add(new SoundPath(content.getLang(), "PAS/KCR/" + vartype + "/" + id + lang + ".mp3"));
                            else if (selectedDb.equals("Database.txt"))
                                paths.add(new SoundPath(content.getLang(), "PAS/MTR/" + vartype.split("(?<=\\G.{3})")[0].toUpperCase() + id + lang + ".mp3"));

                            break;
                        }
                    }
                }
            }
        }

        ArrayList<SoundPath> sortedPath = new ArrayList<>();
        if (Lang.getText().toString().equals(""))
            Lang.setText("CPE");

        char[] lang = Lang.getText().toString().toCharArray();
        for (char c : lang) {
            for (SoundPath soundPath : paths) {
                if (soundPath.getLang().charAt(0) == c) {
                    sortedPath.add(soundPath);
                }
            }
        }

        PaPlayer.play(view, sortedPath, sortedPath.get(0).getPath());
    }
}
