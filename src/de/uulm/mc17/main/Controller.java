package de.uulm.mc17.main;

import de.uulm.mc17.classes.ImageFile;
import de.uulm.mc17.classes.LoadTask;
import de.uulm.mc17.classes.ResizeTask;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Callback;
import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class Controller implements Initializable {

    public VBox vBox;
    public TableView<ImageFile> imageTable;
    public Button addBtn;
    public Button removeBtn;
    public Button compressBtn;
    public TextArea console;
    public ProgressBar progressBar;
    public TableColumn<ImageFile, String> nameCol;
    public TableColumn<ImageFile, String> sizeCol;
    public TableColumn<ImageFile, String> typeCol;
    public TableColumn<ImageFile, ImageView> imageCol;
    public ToggleGroup group = new ToggleGroup();
    public RadioButton jpgToggle;
    public RadioButton pngToggle;
    public String selectedType = "jpg";
    public Button selectBtn;
    public TextField folderURL;
    public TableColumn<ImageFile, String> statusCol;
    public TableColumn<ImageFile, String> resultCol;
    public ProgressIndicator progress;

    private ResizeTask resizeTask;
    private LoadTask loadTask;

    private File dir = new File(System.getProperty("user.home") + System.getProperty("file.separator") + "Desktop");
    private File actualDir;
    private ObservableList<ImageFile> selected = null;
    public Stage stage;
    private boolean setOutput = false;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        actualDir = dir;

        jpgToggle.setToggleGroup(group);
        jpgToggle.setUserData("jpg");
        jpgToggle.setSelected(true);
        pngToggle.setToggleGroup(group);
        pngToggle.setUserData("png");

        vBox.sceneProperty().addListener((observableScene, oldScene, newScene) -> {
            if (oldScene == null && newScene != null) {
                // scene is set for the first time. Now its the time to listen stage changes.
                newScene.windowProperty().addListener((observableWindow, oldWindow, newWindow) -> {
                    if (oldWindow == null && newWindow != null) {
                        // stage is set. now is the right time to do whatever we need to the stage in the controller.
                        this.stage = (Stage) newWindow;
                        ((Stage) newWindow).maximizedProperty().addListener((a, b, c) -> {
                            if (c) {
                                System.out.println("I am maximized!");
                            }
                        });
                    }
                });
            }
        });

        imageTable.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        imageTable.getSelectionModel().getSelectedItems().addListener((ListChangeListener.Change<? extends ImageFile> change) ->
                selected = imageTable.getSelectionModel().getSelectedItems()
        );

        group.selectedToggleProperty().addListener((ObservableValue<?extends Toggle> ov, Toggle oldTogg, Toggle newTogg) -> {
            if(group.getSelectedToggle() != null){
                selectedType = group.getSelectedToggle().getUserData().toString();
                write("Output type changed to: " + selectedType);
            }
        });
    }

    public void write(String in){
        console.appendText(in + "\r\n");
    }

    public void write(String in, boolean append){
        if(append){
            console.appendText(in + "\r\n");
        } else{
            console.setText(in + "\r\n");
        }
    }


    public void add(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        FileChooser.ExtensionFilter filter = new FileChooser.ExtensionFilter("Images", "*.png", "*.PNG", "*.jpg", "*.JPG", "*.jpeg", "*.JPEG");

        fileChooser.setTitle("Select Images");
        fileChooser.setInitialDirectory(dir);
        fileChooser.getExtensionFilters().add(filter);

        List<File> out = fileChooser.showOpenMultipleDialog(this.stage);

        //progress.setProgress(0);
        progress.setVisible(true);

        loadTask = new LoadTask(out, this, this.stage);

        progress.progressProperty().unbind();
        progress.progressProperty().bind(loadTask.progressProperty());

        loadTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {

                //List<File> input = loadTask.getValue();

                List<ImageFile> data = loadTask.getValue();

                ObservableList<ImageFile> list = FXCollections.observableList(data);

                imageCol.setCellValueFactory(new PropertyValueFactory<ImageFile, ImageView>("view"));
                nameCol.setCellValueFactory(new PropertyValueFactory<ImageFile, String>("name"));
                sizeCol.setCellValueFactory(new PropertyValueFactory<ImageFile, String>("size"));
                typeCol.setCellValueFactory(new PropertyValueFactory<ImageFile, String>("type"));
                statusCol.setCellValueFactory(new PropertyValueFactory<ImageFile, String>("status"));

                statusCol.setCellFactory(new Callback<TableColumn<ImageFile, String>, TableCell<ImageFile, String>>() {
                    public TableCell<ImageFile, String> call(TableColumn<ImageFile, String> param){
                        return new TableCell<ImageFile, String>() {

                            @Override
                            public void updateItem(String item, boolean empty){
                                if(!empty){
                                    int index = indexProperty().getValue() < 0 ? 0 : indexProperty().getValue();
                                    String status = param.getTableView().getItems().get(index).getStatus();
                                    if(status.equals("uncompressed")){
                                        setTextFill(javafx.scene.paint.Color.RED);
                                        setText(status);
                                    } else if(status.equals("in queue")){
                                        setTextFill(javafx.scene.paint.Color.BLUE);
                                        setText(status);
                                    } else if(status.equals("finished")){
                                        setTextFill(javafx.scene.paint.Color.GREEN);
                                        setText(status);
                                    } else if(status.equals("ERROR")){
                                        setTextFill(javafx.scene.paint.Color.RED);
                                        setText(status);
                                    }
                                }
                                System.out.println("END OF UPDATE");
                            }

                        };
                    }
                });

                imageTable.setItems(list);

                if(imageTable.getItems().size() > 0){
                    removeBtn.setDisable(false);
                    jpgToggle.setDisable(false);
                    pngToggle.setDisable(false);
                    if(setOutput){
                        compressBtn.setDisable(false);
                    }
                }

                actualDir = new File(imageTable.getItems().get(imageTable.getItems().size()-1).getFile().getParentFile().getAbsolutePath());

                progress.progressProperty().unbind();
                //progress.setProgress(0);
                progress.setVisible(false);
            }
        });

        new Thread(loadTask).start();

    }

    public void remove(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Remove Files");
        alert.setHeaderText("Remove Files");
        String msg = "Do you really want to remove this files form list:\r\n";
        for(ImageFile f : selected){
            msg += f.getName() + "\r\n";
        }
        alert.setContentText(msg);

        Optional<ButtonType> result = alert.showAndWait();
        if(result.get() == ButtonType.OK){

            for(ImageFile f: selected){
                imageTable.getItems().remove(f);
                write("Removed file " + f.getName() + " from list");
            }
        }

        if(imageTable.getItems().size() <= 0){
            removeBtn.setDisable(true);
            compressBtn.setDisable(true);
            jpgToggle.setDisable(true);
            pngToggle.setDisable(true);
        }
    }

    public void compress(ActionEvent actionEvent) {

        write("Compress files:");
        long start = System.currentTimeMillis();
        int size = imageTable.getItems().size();

        progressBar.setProgress(0);

        resizeTask = new ResizeTask(imageTable, this, actualDir, selectedType);

        progressBar.progressProperty().unbind();
        progressBar.progressProperty().bind(resizeTask.progressProperty());

        resizeTask.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, new EventHandler<WorkerStateEvent>() {
            @Override
            public void handle(WorkerStateEvent event) {
                long end = System.currentTimeMillis();
                long duration = end - start;
                write("Complete Compression Time: " + (duration/1000) + " sec.");
            }
        });

        new Thread(resizeTask).start();

    }

    public void selectFolder(ActionEvent actionEvent) {

        DirectoryChooser dirChooser = new DirectoryChooser();

        dirChooser.setTitle("Select Output Folder");
        dirChooser.setInitialDirectory(actualDir);

        try{
            File outputDir = dirChooser.showDialog(this.stage);

            folderURL.setText(outputDir.getAbsolutePath());
            write("Output Folder set to: " + outputDir.getAbsolutePath());

            actualDir = outputDir;
            setOutput = true;

            if(imageTable.getItems().size() > 0){
                compressBtn.setDisable(false);
            }

        } catch (NullPointerException e){
            e.printStackTrace();
        }
        
    }
}
