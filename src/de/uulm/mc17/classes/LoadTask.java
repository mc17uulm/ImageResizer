package de.uulm.mc17.classes;

import de.uulm.mc17.main.Controller;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Marco on 20.12.2017.
 */
public class LoadTask extends Task<List<ImageFile>> {

    private List<File> file;
    private Controller controller;
    private Stage stage;

    public LoadTask(List<File> file, Controller controller, Stage stage){
        this.file = file;
        this.controller = controller;
        this.stage = stage;
    }

    @Override
    protected List<ImageFile> call() throws Exception {

        try {

            System.out.println("Load");

            List<ImageFile> data = new LinkedList<ImageFile>();

            for (File tmp : this.file) {
                controller.write("Read in File: " + tmp.getAbsolutePath());
                data.add(new ImageFile(tmp));
            }

            return data;

        } catch (NullPointerException e){
            e.printStackTrace();
            return null;
        }

    }

}
