import com.stevenv.AppConfig;
import com.stevenv.Review;
import com.stevenv.runnable.ReviewFetcherThread;
import com.typesafe.config.ConfigFactory;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;


public class ReviewFetcherThreadTest {

    private final AppConfig appConfig;
    private ArrayBlockingQueue<Review> reviewsQueue;
    private CountDownLatch countDownLatch;

    public ReviewFetcherThreadTest() {
        appConfig = new AppConfig(ConfigFactory.load());
        reviewsQueue = new ArrayBlockingQueue<>(appConfig.getQueueCapacity());
        countDownLatch = new CountDownLatch(2);
    }

    @Test
    public void Construct_ReviewFetcherThread() {
        ReviewFetcherThread thread = CreateReviewFetcherThread();
        Assert.assertNotNull(thread);
    }

    @Test
    public void Run_ReviewFetcherThread() {
        ReviewFetcherThread reviewFetcherThread = CreateReviewFetcherThread();
        reviewFetcherThread.run();
    }

    private ReviewFetcherThread CreateReviewFetcherThread() {
        return new ReviewFetcherThread(appConfig, reviewsQueue, countDownLatch);
    }
}
