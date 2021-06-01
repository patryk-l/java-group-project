package group.image_handlers;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ImageHandler {

    Integer threadCount;
    Integer completed = 0;
    ExecutorService executorService;

    public ImageHandler(Integer threadCount) {
        this.threadCount = threadCount;
        executorService = Executors.newFixedThreadPool(threadCount);
    }
}
