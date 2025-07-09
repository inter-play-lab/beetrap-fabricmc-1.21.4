package beetrap.btfmc.agent.physical;

import beetrap.btfmc.agent.Agent;
import beetrap.btfmc.agent.GptJsonResponseDeserialized;
import beetrap.btfmc.agent.event.EventMessage;
import beetrap.btfmc.agent.physical.state.PAS0Introduction;
import beetrap.btfmc.event.BeetrapGameEvent;
import beetrap.btfmc.state.BeetrapStateManager;
import beetrap.btfmc.tts.SlopTextToSpeechUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.openai.models.responses.Response;
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import static beetrap.btfmc.agent.event.EventMessage.EVENT_MESSAGE_KEY_EVENT_TYPE_VALUE_DEAD_FLOWER;
import static beetrap.btfmc.event.BeetrapGameEvent.EventType.DEAD_FLOWER;
import static net.minecraft.entity.MovementType.SELF;

public class PhysicalAgent extends Agent {
    private static final ObjectMapper om = new ObjectMapper();

    private final BeeEntity beeEntity;
    private boolean isAnimating;
    private Vec3d flyDestination;

    public PhysicalAgent(ServerWorld world, BeetrapStateManager beetrapStateManager) {
        super(world, beetrapStateManager, new PAS0Introduction());

        this.beeEntity = new BeeEntity(EntityType.BEE, this.world);
        this.beeEntity.setInvulnerable(true);
        this.beeEntity.setAiDisabled(true);
        this.beeEntity.setPos(0.5, 1, 0.5);
        this.world.spawnEntity(this.beeEntity);
        this.isAnimating = false;
        this.flyDestination = null;
    }

    public BeeEntity getBeeEntity() {
        return this.beeEntity;
    }


    @Override
    public void tickCustom() {
        if (!isAnimating) {
            ServerPlayerEntity player = this.world.getPlayers().getFirst();
            this.beeEntity.lookAt(EntityAnchor.EYES, player.getPos());
        } else {
            if (this.beeEntity.squaredDistanceTo(this.flyDestination) < 1) {
                this.beeEntity.move(SELF, new Vec3d(0, 0, 0));
                this.isAnimating = false;
                this.flyDestination = null;
                this.sendGptEventMessage(new EventMessage(EVENT_MESSAGE_KEY_EVENT_TYPE_VALUE_DEAD_FLOWER));
            } else {
                this.beeEntity.move(SELF, new Vec3d(
                        (this.flyDestination.x - this.beeEntity.getX()) / 3,
                        0,
                        (this.flyDestination.z - this.beeEntity.getZ()) / 3));
            }
        }
    }

    @Override
    public void close() {
        this.beeEntity.kill(this.world);
    }

    @Override
    public void addGameEvent(BeetrapGameEvent bge) {
        super.addGameEvent(bge);
        if (bge.getType().equals(DEAD_FLOWER) && !isAnimating) {
            this.isAnimating = true;
            Vec3d flowerPos = (Vec3d) bge.getData();
            this.beeEntity.lookAt(EntityAnchor.EYES, flowerPos);
            this.flyDestination = new Vec3d(flowerPos.x, this.beeEntity.getY(), flowerPos.z);
        }
    }

    @Override
    public void onGptResponseReceived(Response response, Throwable throwable) {
        String s = response.output().getFirst().asMessage().content().getFirst().asOutputText().text();
        try {
            GptJsonResponseDeserialized gjrd = om.readValue(s, GptJsonResponseDeserialized.class);
            if (gjrd.action().equals("fly")) {
//                this.world
            }
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
}
