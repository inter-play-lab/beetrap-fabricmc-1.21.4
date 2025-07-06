package beetrap.btfmc.tts;

import com.mojang.text2speech.Narrator;
import com.mojang.text2speech.Narrator.InitializeException;
import com.mojang.text2speech.NarratorLinux;
import com.mojang.text2speech.NarratorMac;
import com.mojang.text2speech.NarratorWindows;
import com.mojang.text2speech.OperatingSystem;

public final class CrappyTextToSpeechUtil {
    private static final Narrator narrator;

    private CrappyTextToSpeechUtil() {

    }

    public static boolean textToSpeechSupported() {
        return narrator != null && narrator.active();
    }

    public static void say(String s) {
        narrator.say(s, false);
    }

    static {
        try {
            narrator = switch(OperatingSystem.get()) {
                case WINDOWS -> new NarratorWindows();
                case MAC_OS -> new NarratorMac();
                case LINUX -> new NarratorLinux();
                default -> null;
            };
        } catch(InitializeException e) {
            throw new RuntimeException(e);
        }
    }
}
