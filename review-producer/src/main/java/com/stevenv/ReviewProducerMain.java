package com.stevenv;

import com.stevenv.rest.ReviewRestApiClient;
import com.stevenv.runnable.ReviewFetcherThread;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.*;

public class ReviewProducerMain {

    private Logger log = LoggerFactory.getLogger(ReviewProducerMain.class.getSimpleName());

    private ExecutorService executor;
    private final CountDownLatch latch;
    private AppConfig appConfig;
    private final ReviewFetcherThread reviewFetcherThread;

    public static void main(String[] args) {
        ReviewProducerMain app = new ReviewProducerMain();
        app.start();
    }

    private ReviewProducerMain() {
        appConfig = new AppConfig(ConfigFactory.load());
        latch = new CountDownLatch(2);
        executor = Executors.newFixedThreadPool(2);
        ArrayBlockingQueue<Review> reviewsQueue = new ArrayBlockingQueue<>(appConfig.getQueueCapacity());

        reviewFetcherThread = new ReviewFetcherThread(appConfig, reviewsQueue, latch);
    }

    private void start() {

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (!executor.isShutdown()){
                log.info("Shutdown requested");
                shutdown();
            }
        }));

        log.info("application started.");

        executor.submit(reviewFetcherThread);

        log.info("Started processors.");
        try {
            log.info("Latch await.");
            latch.await();
            log.info("Threads completed");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            shutdown();
            log.info("Applications shutdown successfully.");
        }
    }

    private void shutdown() {
        if (!executor.isShutdown()) {
            log.info("Shutting down");
            executor.shutdownNow();
            try {
                if (!executor.awaitTermination(2000, TimeUnit.MILLISECONDS)) {
                    log.warn("Executor did not terminate in the specificed time.");
                    List<Runnable> droppedTasks = executor.shutdownNow();
                    log.warn("Executor was abruptly shutdown. " + droppedTasks.size() + " tasks will not be executed.");
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


}
