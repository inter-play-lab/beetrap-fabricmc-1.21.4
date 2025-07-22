package beetrap.btfmc.tts;

import static beetrap.btfmc.Beetrapfabricmc.MOD_REQUIRED_TYPECAST_API_KEY;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;
import org.apache.hc.client5.http.impl.classic.AbstractHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ClassicHttpRequest;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.support.ClassicRequestBuilder;

public final class SlopTextToSpeechUtil {

    private static final String TYPECAST_API_KEY = System.getProperty(
            MOD_REQUIRED_TYPECAST_API_KEY);
    private static final CloseableHttpClient httpClient;
    private static final String requestBody;
    private static final ExecutorService es;

    static {
        httpClient = HttpClients.createDefault();
        requestBody = """
                {
                    "model": "ssfm-v21",
                    "text": "{}",
                    "voice_id": "tc_660e5c11eef728e75f95f520"
                }
                """;

        es = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        });
    }

    private SlopTextToSpeechUtil() {
        throw new AssertionError();
    }

    private static void playAudioFile(File f) {
        try {
            // Get audio input stream from the file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(f);

            // Get the audio format
            AudioFormat format = audioInputStream.getFormat();
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

            // Get and open the audio line
            SourceDataLine line = (SourceDataLine)AudioSystem.getLine(info);
            line.open(format);
            line.start();

            // Create buffer for reading
            int bufferSize = 4096;
            byte[] buffer = new byte[bufferSize];
            int bytesRead;

            // Read and play the audio file
            while((bytesRead = audioInputStream.read(buffer, 0, buffer.length)) != -1) {
                line.write(buffer, 0, bytesRead);
            }

            // Clean up
            line.drain();
            line.stop();
            line.close();
            audioInputStream.close();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static void handleEntity(HttpEntity entity)
            throws IOException, UnsupportedAudioFileException, LineUnavailableException {
        File f = new File("imagine" + System.currentTimeMillis() + ".wav");
        BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(f));
        bos.write(entity.getContent().readAllBytes());
        bos.close();
        playAudioFile(f);
    }

    // TODO: Add proximity thingies
    public static CompletableFuture<?> say(String message) {
        return CompletableFuture.runAsync(() -> {
            String i = requestBody.replace("{}",
                    message.replaceAll("[\\n\\t]", " ").replaceAll("[^a-zA-Z0-9 ]", ""));
            ClassicHttpRequest tts = ClassicRequestBuilder.post(
                            "https://api.typecast.ai/v1/text-to-speech")
                    .addHeader("X-API-KEY", TYPECAST_API_KEY)
                    .addHeader("Content-Type", "application/json")
                    .setEntity(i)
                    .build();
            try {
                httpClient.execute(tts, new AbstractHttpClientResponseHandler<Void>() {
                    @Override
                    public Void handleEntity(HttpEntity entity) throws IOException {
                        try {
                            SlopTextToSpeechUtil.handleEntity(entity);
                        } catch(IOException | UnsupportedAudioFileException |
                                LineUnavailableException e) {
                            throw new IOException(e);
                        }
                        return null;
                    }
                });
            } catch(IOException e) {
                throw new RuntimeException(e);
            }
        }, es);
    }
}
