package beetrap.btfmc.stt;

import static beetrap.btfmc.BeetrapfabricmcClient.MOD_ID;

import de.maxhenkel.voicechat.api.VoicechatApi;
import de.maxhenkel.voicechat.api.VoicechatPlugin;
import de.maxhenkel.voicechat.api.events.ClientSoundEvent;
import de.maxhenkel.voicechat.api.events.EventRegistration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SimpleVoiceChatPlugin implements VoicechatPlugin {
    private static final Logger LOG = LogManager.getLogger(SimpleVoiceChatPlugin.class);

    @Override
    public void initialize(VoicechatApi api) {

    }

    @Override
    public String getPluginId() {
        return MOD_ID + "-SimpleVoiceChatPlugin";
    }

    public void onClientSoundEvent(ClientSoundEvent cse) {
//        LOG.info("{}", cse.getRawAudio().length);
    }

    @Override
    public void registerEvents(EventRegistration registration) {
        registration.registerEvent(ClientSoundEvent.class, this::onClientSoundEvent);
    }
}
