package beetrap.btfmc.agent.physical;

import beetrap.btfmc.agent.Agent;
import beetrap.btfmc.agent.GptJsonResponseDeserialized;
import beetrap.btfmc.agent.physical.state.PAS0Introduction;
import beetrap.btfmc.handler.SignalHandler;
import beetrap.btfmc.state.BeetrapStateManager;
import beetrap.btfmc.tts.SlopTextToSpeechUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.models.responses.Response;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

public class PhysicalAgent extends Agent {
    private static final ObjectMapper om = new ObjectMapper();

    private final BeeEntity beeEntity;

    public PhysicalAgent(ServerWorld world, BeetrapStateManager beetrapStateManager) {
        super(world, beetrapStateManager, new PAS0Introduction());

        this.beeEntity = new BeeEntity(EntityType.BEE, this.world);
        this.beeEntity.setInvulnerable(true);
        this.beeEntity.setAiDisabled(true);
        this.beeEntity.setPos(0.5, 1, 0.5);
        this.world.spawnEntity(this.beeEntity);
    }

    public BeeEntity getBeeEntity() {
        return this.beeEntity;
    }


    @Override
    public void tickCustom() {

    }

    @Override
    public void onGptResponseReceived(Response response, Throwable throwable) {
        String s = response.output().getFirst().asMessage().content().getFirst().asOutputText().text();
        try {
            GptJsonResponseDeserialized gjrd = om.readValue(s, GptJsonResponseDeserialized.class);
            this.world.getPlayers().forEach(
                    serverPlayerEntity -> serverPlayerEntity.sendMessage(Text.of("<" + super.name + "> " + gjrd.dialogue())));
            SlopTextToSpeechUtil.say(gjrd.dialogue());
        } catch(JsonProcessingException e) {
            for(ServerPlayerEntity player : this.world.getPlayers()) {
                player.sendMessage(Text.of(e.getMessage()));
            }

            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        this.beeEntity.kill(this.world);
    }
}
