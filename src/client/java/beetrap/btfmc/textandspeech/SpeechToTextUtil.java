package beetrap.btfmc.textandspeech;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.entity.mime.MultipartEntityBuilder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.CloseableHttpResponse;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import static beetrap.btfmc.Beetrapfabricmc.MOD_REQUIRED_OPENAI_API_KEY;

public final class SpeechToTextUtil {
    private static final Logger LOG = LogManager.getLogger(SpeechToTextUtil.class);
    private static final CloseableHttpClient closeableHttpClient;
    private static final String OPENAI_API_URL = "https://api.openai.com/v1/audio/transcriptions";
    private static final String OPENAI_API_KEY = System.getProperty(MOD_REQUIRED_OPENAI_API_KEY); // Get API key from environment variable

    private SpeechToTextUtil() {
        throw new AssertionError();
    }

    /**
     * Transcribes audio data using OpenAI Whisper API.
     * This method is kept for backward compatibility.
     * It creates a temporary WAV file and then calls transcribeAudioFile.
     * 
     * @param audioData Raw audio data as byte array
     * @return Transcription text
     */
    public static String transcribeAudio(byte[] audioData) {
        LOG.info("Transcribing audio data ({} bytes)", audioData.length);
        try {
            // Use the existing WAV file from SimpleVoiceChatPlugin instead of creating a new one
            Path tempFile = Files.createTempFile("whisper_input", ".wav");
            try (FileOutputStream fos = new FileOutputStream(tempFile.toFile())) {
                // Write WAV header - we need to implement this since we're creating a temp file
                writeWavHeader(fos, audioData.length);
                // Write audio data
                fos.write(audioData);
            }

            LOG.info("Created temporary WAV file: {}", tempFile);
            return transcribeAudioFile(tempFile);
        } catch (Exception e) {
            LOG.error("Failed to transcribe audio", e);
            return "Transcription failed: " + e.getMessage();
        }
    }

    /**
     * Transcribes an audio file using OpenAI Whisper API.
     * 
     * @param audioFile Path to the WAV file
     * @return Transcription text
     */
    public static String transcribeAudioFile(Path audioFile) {
        LOG.info("Transcribing audio file: {}", audioFile);

        if (OPENAI_API_KEY == null || OPENAI_API_KEY.isEmpty()) {
            LOG.error("OpenAI API key not found. Please set the OPENAI_API_KEY environment variable.");
            return "Transcription failed: OpenAI API key not found";
        }

        try {
            HttpPost httpPost = new HttpPost(OPENAI_API_URL);
            httpPost.setHeader("Authorization", "Bearer " + OPENAI_API_KEY);

            // Create multipart request with the audio file
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody(
                "file", 
                new FileInputStream(audioFile.toFile()), 
                ContentType.create("audio/wav"), 
                audioFile.getFileName().toString()
            );
            builder.addTextBody("model", "whisper-1", ContentType.TEXT_PLAIN);

            HttpEntity multipart = builder.build();
            httpPost.setEntity(multipart);

            LOG.info("Sending request to OpenAI Whisper API...");
            try (CloseableHttpResponse response = closeableHttpClient.execute(httpPost)) {
                int statusCode = response.getCode();
                LOG.info("Received response with status code: {}", statusCode);

                HttpEntity responseEntity = response.getEntity();
                String responseBody = EntityUtils.toString(responseEntity);

                if (statusCode == 200) {
                    // Parse JSON response
                    JsonObject jsonResponse = new Gson().fromJson(responseBody, JsonObject.class);
                    String transcription = jsonResponse.get("text").getAsString();
                    LOG.info("Transcription successful: {}", transcription);
                    return transcription;
                } else {
                    LOG.error("API request failed with status code {}: {}", statusCode, responseBody);
                    return "Transcription failed: API request failed with status code " + statusCode;
                }
            }
        } catch (Exception e) {
            LOG.error("Failed to transcribe audio file", e);
            return "Transcription failed: " + e.getMessage();
        }
    }

    /**
     * Writes a WAV header to the output stream.
     * 
     * @param fos FileOutputStream to write to
     * @param audioDataLength Length of the audio data in bytes
     * @throws IOException If an I/O error occurs
     */
    private static void writeWavHeader(FileOutputStream fos, int audioDataLength) throws IOException {
        // WAV header format (simplified)
        int sampleRate = 48000; // VoiceChat uses 48kHz
        int channels = 1; // Mono
        int bitsPerSample = 16; // 16-bit PCM

        // RIFF header
        fos.write("RIFF".getBytes());
        fos.write(intToBytes(36 + audioDataLength)); // File size - 8
        fos.write("WAVE".getBytes());

        // fmt chunk
        fos.write("fmt ".getBytes());
        fos.write(intToBytes(16)); // Chunk size
        fos.write(shortToBytes((short) 1)); // Audio format (1 = PCM)
        fos.write(shortToBytes((short) channels)); // Channels
        fos.write(intToBytes(sampleRate)); // Sample rate
        fos.write(intToBytes(sampleRate * channels * bitsPerSample / 8)); // Byte rate
        fos.write(shortToBytes((short) (channels * bitsPerSample / 8))); // Block align
        fos.write(shortToBytes((short) bitsPerSample)); // Bits per sample

        // data chunk
        fos.write("data".getBytes());
        fos.write(intToBytes(audioDataLength)); // Chunk size
    }

    private static byte[] intToBytes(int value) {
        return new byte[] {
            (byte) (value & 0xff),
            (byte) ((value >> 8) & 0xff),
            (byte) ((value >> 16) & 0xff),
            (byte) ((value >> 24) & 0xff)
        };
    }

    private static byte[] shortToBytes(short value) {
        return new byte[] {
            (byte) (value & 0xff),
            (byte) ((value >> 8) & 0xff)
        };
    }

    static {
        closeableHttpClient = HttpClients.createDefault();
    }
}
