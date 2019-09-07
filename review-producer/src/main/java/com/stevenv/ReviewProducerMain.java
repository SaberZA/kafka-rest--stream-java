package com.stevenv;

import com.stevenv.rest.ReviewRestApiClient;
import com.typesafe.config.ConfigFactory;
import org.apache.http.HttpException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ReviewProducerMain {
    private Logger log = LoggerFactory.getLogger(ReviewProducerMain.class.getSimpleName());

    public static void main(String[] args) {
        ReviewProducerMain app = new ReviewProducerMain();
        app.start();
    }

    private void start() {
        log.info("application started.");
        AppConfig appConfig = new AppConfig(ConfigFactory.load());

        ReviewRestApiClient reviewRestApiClient = new ReviewRestApiClient(appConfig.getCourseId(), appConfig.getUdemyPageSize());

        List<Review> reviews;

        try {
            reviews = reviewRestApiClient.getNextReviews();
            log.info("Fetched " + reviews.size() + " reviews");
        } catch (HttpException e) {
            e.printStackTrace();
        } finally {
            log.info("Closing");
            reviewRestApiClient.close();
            log.info("Closed");
        }
    }

    private ReviewProducerMain() {

    }

}
