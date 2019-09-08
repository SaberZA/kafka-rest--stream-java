package com.stevenv.runnable;

import com.stevenv.AppConfig;
import com.stevenv.Review;
import com.stevenv.rest.ReviewRestApiClient;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ReviewFetcherThread implements Runnable {

    private final ReviewRestApiClient reviewRestApiClient;
    private Logger log = LoggerFactory.getLogger(ReviewFetcherThread.class.getSimpleName());

    private final AppConfig appConfig;
    private final ArrayBlockingQueue<Review> reviewsQueue;
    private final CountDownLatch latch;

    public ReviewFetcherThread(AppConfig appConfig, ArrayBlockingQueue<Review> reviewsQueue, CountDownLatch latch) {
        this.appConfig = appConfig;
        this.reviewsQueue = reviewsQueue;
        this.latch = latch;
        this.reviewRestApiClient = new ReviewRestApiClient(appConfig.getCourseId(), appConfig.getUdemyPageSize());
    }

    @Override
    public void run() {
        try {
            Boolean keepOnRunning = true;
            while (keepOnRunning) {
                List<Review> reviews;
                try {
                    reviews = reviewRestApiClient.getNextReviews();
                    log.info("Fetched " + reviews.size() + " reviews");

                    if (reviews.size() == 0) {
                        keepOnRunning = false;
                    } else {
                        // This is a flow control block
                        log.info("Queue size: " + reviewsQueue.size());
                        for (Review review :
                                reviews) {
                            reviewsQueue.put(review);
                        }
                    }
                } catch (HttpException e) {
                    e.printStackTrace();
                } finally {
                    Thread.sleep(50);
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            this.close();
        }
    }

    private void close() {
        log.info("Closing");
        reviewRestApiClient.close();
        latch.countDown();
        log.info("Closed");
    }
}
