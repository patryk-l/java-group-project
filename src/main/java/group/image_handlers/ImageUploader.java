package group.image_handlers;

import group.DBConnect;
import group.ImageRow;
import javafx.concurrent.Task;

import java.io.File;
import java.util.List;
import java.util.Map;

public class ImageUploader extends Task<Void> {

    Map<File, List<Integer>> tagMap;

    public ImageUploader(Map<File, List<Integer>> map) {
        this.tagMap = map;
    }

    @Override
    protected Void call() throws Exception {
        int size = tagMap.keySet().size();
        int workDone = 0;
        for (Map.Entry<File, List<Integer>> entry : tagMap.entrySet()) {
            ImageRow imageRow = new ImageRow(entry.getKey());
            try {
                DBConnect.insertImage(imageRow);
                //System.out.println(imageRow.getId());
                if (entry.getValue() != null)
                    for (Integer tagId : entry.getValue())
                        DBConnect.insertImageTag(imageRow.getId(), tagId);
            } catch (Exception e) {
                e.printStackTrace();
            }
            workDone++;
            updateProgress(workDone, size);
            System.out.println(progressProperty());
        }
        return null;
    }
}
