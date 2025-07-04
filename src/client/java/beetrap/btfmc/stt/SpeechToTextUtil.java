package beetrap.btfmc.stt;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;

public final class SpeechToTextUtil {
    private static final String TYPECAST_API_KEY;
    private static CloseableHttpClient closeableHttpClient;
    private static ExecutorService executorService;

    private SpeechToTextUtil() {
        throw new AssertionError();
    }

    public static void say(String message) {

    }

    static {
        TYPECAST_API_KEY = System.getenv("TYPECAST_API_KEY");
        closeableHttpClient = HttpClients.createDefault();
        executorService = Executors.newSingleThreadExecutor();
    }
}
