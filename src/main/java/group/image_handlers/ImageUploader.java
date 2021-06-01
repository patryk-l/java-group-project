package group.image_handlers;

import java.nio.file.Path;
import java.util.concurrent.Callable;

public class ImageUploader implements Callable<Integer> {
    Path path;
    String[] tags;

    public ImageUploader(Path path, String[] tags) {
        this.path = path;
        this.tags = tags;
    }

    @Override
    public Integer call() throws Exception {

        return 1;
    }
}
