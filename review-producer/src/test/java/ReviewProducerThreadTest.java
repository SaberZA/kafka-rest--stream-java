import com.stevenv.AppConfig;
import com.stevenv.Review;
import com.stevenv.runnable.ReviewProducerThread;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

public class ReviewProducerThreadTest {

    private final AppConfig appConfig;
    private final ArrayBlockingQueue<Review> reviewsQueue;
    private final CountDownLatch countDownLatch;

    public ReviewProducerThreadTest() {
        appConfig = new AppConfig(ConfigFactory.load());
        reviewsQueue = new ArrayBlockingQueue<>(appConfig.getQueueCapacity());
        countDownLatch = new CountDownLatch(2);
    }

    @Test
    public void Construct_ReviewProducerThread() {
        ReviewProducerThread reviewProducerThread = CreateReviewProducerThread();
        Assert.assertNotNull(reviewProducerThread);
    }

    @Test
    public void Run_ReviewProducerThread() {
        ReviewProducerThread reviewProducerThread = CreateReviewProducerThread();
        reviewProducerThread.run();
    }

    private ReviewProducerThread CreateReviewProducerThread() {
        return new ReviewProducerThread(appConfig, reviewsQueue, countDownLatch);
    }
}
