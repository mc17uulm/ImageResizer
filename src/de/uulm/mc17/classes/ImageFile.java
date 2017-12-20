package de.uulm.mc17.classes;

import javafx.beans.property.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import org.apache.commons.io.FilenameUtils;
import org.imgscalr.Scalr;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.*;

/**
 * Created by Marco on 19.12.2017.
 */
public class ImageFile implements Serializable{

    private StringProperty name = new SimpleStringProperty();
    private StringProperty type = new SimpleStringProperty();
    private LongProperty size = new SimpleLongProperty();
    private StringProperty status = new SimpleStringProperty();
    private StringProperty result = new SimpleStringProperty();
    private File file;
    private ImageView view;

    public ImageFile (File file){
        this.name.set(file.getName());
        this.type.set(FilenameUtils.getExtension(file.getAbsolutePath()));
        this.size.set(file.length());
        this.file = file;
        this.view = new ImageView();
        try {
            this.view.setImage(new Image(new FileInputStream(file)));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        this.status.set("uncompressed");
        this.result.set("");
    }

    public final StringProperty nameProperty() {
        return this.name;
    }

    public final String getName(){
        return this.nameProperty().get();
    }

    public final void setName(String name){
        this.nameProperty().set(name);
    }

    public final StringProperty typeProperty() {
        return this.type;
    }

    public final String getType() {
        return this.typeProperty().get();
    }

    public final void setType(String value){
        this.typeProperty().set(value);
    }

    public final StringProperty sizeProperty() {
        return new SimpleStringProperty(getSizeValue(this.size.get()));
    }

    public final String getSize() {
        return getSizeValue(this.size.get());
    }

    public String getSizeValue(long size){
        long s = size;
        if(s > 1024){
            if(s > (1024*1024)){
                if(s > (1024*1024*1024)){
                    return s/(1024*1024*1024) + " gb";
                } else{
                    return s/(1024*1024) + " mb";
                }
            } else{
                return s/1024 + " kb";
            }
        } else{
            return s + " bytes";
        }
    }

    public final StringProperty statusProperty() {
        return this.status;
    }

    public final String getStatus() { return this.statusProperty().get(); }

    public final void setStatus(String status){
        this.statusProperty().set(status);
    }

    public final StringProperty resultProperty() {
        return this.result;
    }

    public final String getResult() { return this.resultProperty().get(); }

    public final void setResult(String result){
        this.resultProperty().set(result);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public ImageView getView() {
        view.setFitHeight(75);
        view.setFitWidth(75);
        return view;
    }

    public void setView(ImageView view) {
        this.view = view;
    }

}
