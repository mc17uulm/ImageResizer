package de.uulm.mc17.classes;

import de.uulm.mc17.main.Controller;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.TableView;
import org.imgscalr.AsyncScalr;
import org.imgscalr.Scalr;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

/**
 * Created by Marco on 20.12.2017.
 */
public class ResizeTask extends Task<Boolean> {
    
    private TableView<ImageFile> table;
    private ObservableList<ImageFile> list;
    private String selectedType;
    private File actualDir;
    private Controller controller;
    
    public ResizeTask(TableView<ImageFile> table, Controller controller, File actualDir, String selectedType){
        this.table = table;
        this.list = this.table.getItems();
        this.controller = controller;
        this.actualDir = actualDir;
        this.selectedType = selectedType;
    }

    @Override
    protected Boolean call() throws Exception {

        int i = 0;
        int count = this.list.size();
        for(ImageFile f : this.list){
            compressImage(f, this.selectedType, this.actualDir);
            i++;
            this.updateProgress(i, count);
        }
        
        return true;

    }

    private void compressImage(ImageFile file, String ext, File outputFolder){

        long start = System.currentTimeMillis();

        file.setStatus("in queue");

        try{
            BufferedImage image = ImageIO.read(file.getFile());

            int height = image.getHeight();
            int width = image.getWidth();

            int[] dim = computeDimension(height, width);

            this.controller.write("Compress Image " + file.getName());

            BufferedImage resizedImage = AsyncScalr.resize(image, Scalr.Method.ULTRA_QUALITY, dim[1], dim[0]).get();

            String outputFileName = getOutputName(outputFolder.getAbsolutePath(), file.getName(), ext);

            File outputFile = new File(outputFileName);

            this.controller.write("Image saved to: " + outputFileName);
            ImageIO.write(resizedImage, ext, outputFile);
            file.setStatus("finished");
            file.setResult("Resized from " + file.getSize() + " to " + file.getSizeValue(outputFile.length()));

        } catch (InterruptedException e) {
            e.printStackTrace();
            this.controller.write("Threads/Process Error: " + e.getMessage());
            file.setStatus("ERROR");
        } catch (ExecutionException e) {
            e.printStackTrace();
            this.controller.write("Compression Error: " + e.getMessage());
            file.setStatus("ERROR");
        } catch (IOException e) {
            e.printStackTrace();
            this.controller.write("File Error: " + e.getMessage());
            file.setStatus("ERROR");
        }

        long end = System.currentTimeMillis();
        long duration = end - start;
        this.controller.write("Calculation time for " + file.getName() + ": " + (duration/1000) + " sec.");
    }
    
    private int[] computeDimension(int height, int width){

        if((height < 2000) || (width < 2000)){

            return new int[]{height, width};

        } else{

            int newW = width/(width/2000);
            int newH = height/(height/2000);

            return new int[]{newH, newW};

        }

    }

    private String getOutputName(String outputFolder, String fileName, String outputFileType){
        return getOutputName(outputFolder, fileName, outputFileType, true);
    }

    private String getOutputName(String outputFolder, String fileName, String outputFileType, boolean rename){

        int point = fileName.lastIndexOf('.');
        String e = fileName.substring(point + 1);
        String prefix = "";
        if(rename){
            prefix = "_resized";
        }
        if((point == -1) || (!e.matches("\\w+"))){
            return outputFolder + File.separator + fileName + prefix + "." + outputFileType;
        } else{
            return outputFolder + File.separator + fileName.substring(0, point) + prefix + "." + outputFileType;
        }

    }

}
