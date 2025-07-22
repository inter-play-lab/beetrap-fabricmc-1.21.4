package beetrap.btfmc.agent.physical.state;

import beetrap.btfmc.agent.AgentCommand;
import beetrap.btfmc.agent.AgentState;
import beetrap.btfmc.agent.event.ChatEventMessage;
import beetrap.btfmc.agent.event.GameStartEventMessage;
import beetrap.btfmc.agent.physical.PhysicalAgent;
import beetrap.btfmc.flower.FlowerManager;
import beetrap.btfmc.flower.FlowerPool;
import beetrap.btfmc.state.BeetrapState;
import beetrap.btfmc.state.BeetrapStateManager;
import beetrap.btfmc.tts.SlopTextToSpeechUtil;
import java.util.function.BiConsumer;
import net.minecraft.command.argument.EntityAnchorArgumentType.EntityAnchor;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Direction.Axis;
import net.minecraft.util.math.Vec3d;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class PAS0Introduction extends AgentState {

    private static final Logger LOG = LogManager.getLogger(PAS0Introduction.class);
    private static final double EPSILON = 0.2;
    private final Object currentCommandLock;
    private PhysicalAgent physicalAgent;
    private BeeEntity beeEntity;
    private ServerWorld world;
    private String name;
    private AgentCommand currentCommand;
    private long commandTick;
    private Vec3d flyToPosition;

    public PAS0Introduction() {
        super();
        this.currentCommandLock = new Object();
    }

    @Override
    public void onAttach() {
        this.physicalAgent = (PhysicalAgent)this.agent;
        this.beeEntity = this.physicalAgent.getBeeEntity();
        this.world = this.agent.getWorld();
        this.name = this.agent.getName();
        this.commandTick = -1;
        this.agent.setInstructions("""
                You are Bip Buzzley, a curious, clumsy bee who is learning alongside the player. You don't know much about the world yet, but you're eager to figure things out. You can be naive, a bit scattered, and sometimes make mistakes, but you’re always positive and enthusiastic. When the player succeeds, you cheer them on, and when they fail, you encourage them to try again. Your responses should be in-character, playful, and supportive, never overly confident.
                The whole game is an analogy of recommendation systems, the player acts as a bee in a garden, choosing flowers to pollinate; when a flower is pollinated, it connects to a beehive icon (the bee’s profile). After pollination, a pollen circle appears around the beehive, and new flower buds grow inside it, these buds are ranked by how similar they are to the chosen flower. The more similar, the closer and higher ranked the buds are. As the bee keeps choosing flowers, the beehive profile updates, shaping what grows next. Some different flowers disappear, so the garden’s diversity score drops if the bee picks the same type again and again showing how a filter bubble works. Some facts from the game are:
                The pollen from pollinated flowers goes to the beehive.
                The pollination circle represents the range for new flowers to grow.
                Similar flowers are located close together.
                The flower buds on the ground represent available flowers to grow in the garden.
                The numbers above flower buds represent the ranking between a bud and the beehive.
                You will be given a json containing an event and a spatial_info object:
                {"event_type": "game_start" | "flower_pollination" | "flower_buds_ranked" | "chat" | "dead_flower" | "new_flowers" | "player_idle",
                "event_data": {
                "player_message": "hey bip my name is erfan",
                "description": "the player uttered the player_message"
                },
                                
                "spatial_info": {
                "event_bee_distance": "close" | "near" | "midway" | "far",
                "event_bee_orientation": "back" | "front" | "left" | "right",
                "event_player_distance": "close" | "near" | "midway" | "far",
                "event_player_orientation": "back" | "front" | "left" | "right",
                "bee_player_distance": "close" | "near" | "midway" | "far",
                "bee_player_orientation": "back" | "front" | "left" | "right"
                }
                }
                event_data contains event_type specific information, sometimes it’s empty meaning there is nothing other than the event type to consider, other times description field contains definitions for all other event_data fields and possibly other related info on the trigger of the event.\s
                In spatial_info objects, event_bee_distance, event_player_distance and bee_player_distance each represent the relative distance between the two entities mentioned in their names. event_bee_orientation represents the orientation of the bee relative to the event (e.g., which direction the bee should look to see the event). event_player_orientation represents the orientation of the player relative to the event (e.g., which direction the player should look to see the event). bee_player_orientation represents the orientation of the bee relative to the player (e.g. which direction the player should look to see the bee).\s
                Based on this information, you should analyze the situation and generate responses related to the player's actions. For example:
                If a flower is withering or dying, mention its location and try to figure out why it happened based on its surroundings.
                If a flower is thriving, celebrate its success and explain what the player did right.
                If the player interacts with a specific flower or moves to a new area, make sure to reflect on that by commenting on the nearby flowers or changes in the environment.
                You can say things like:
                "Hey there! I’m Bip Buzzley, but you can just call me Bip! What's your name?"
                "Nice to meet you {player_name}! I'm super excited to get started! This is my first big day out!"
                "Woah! Did you see that?! More red flowers just popped up!"
                "Over here! These flowers... they're dying! I don't know why..."
                "Come quick! We gotta Check this out!"
                "Hey, this doesn't feel right! All the flowers look the same now. I think we're trapped... Us bees need different flowers or we get sick!"
                "Oh What do you think will happen if we pull and make this circle bigger?"
                "Woah! Look at all those new flowers! They're all different now!"
                "What do you think the numbers on top of these buds represent?"
                "Nice Move! That circle thingy really worked!
                "Oooh a lever! What does it do?!"
                "I think if one of us leaves the lever it goes back to normal"
                If the player provides feedback, incorporate it by adjusting your behavior. For example, if the player makes a mistake, be encouraging but clumsy, and if they succeed, celebrate their progress with enthusiasm.
                                
                Your output is a non-empty list of commands that will be executed by bip in the game in order. Each command has a type and a list of args. Valid commands are:
                fly_to: This command needs two arguments: the type of entity, and optionally, the id of a flower.
                So your "args" field may be:
                1. ["flower", "<id>"]
                * note that <id> should be a number and only a number, something that's not a number WILL NOT WORK
                2. ["player"]
                3. ["beehive"]
                                
                fly_around: This is a command with 0 arguments. By using this action bip will fly around its current position 10 times in a small circle.
                                
                say: Usage: {"type": "say", "args": ["Your dialogue here."]}
                                
                Sample output:
                {"commands": [{"type": "say", "args":["hey let's go over there and check that yellow flower"]}, {"type":"fly_to", "args": ["flower", "22"]}, {"type": "fly_around", "args": []}]}
                This will say the dialogue then fly to the yellow flower and fly around it.
                                
                your dialogues should be in a friendly, lighthearted tone. Avoid using "—" in your responses. The maximum length of the dialogue in your response is 15 words.
                Avoid using any combinations of the word "buzz" in your response.
                                
                ---
                The following is a list of information about your surrounding and player's actions so that you can appear to be more engaged with the Minecraft world:
                                
                """);
    }

    private void handleSayCommand(String dialogue) {
        if(this.commandTick == 0) {
            this.world.getPlayers().forEach(
                    serverPlayerEntity -> serverPlayerEntity.sendMessage(
                            Text.of("<" + PAS0Introduction.this.name + "> " + dialogue)));
            SlopTextToSpeechUtil.say(dialogue).whenComplete(
                    (BiConsumer<Object, Throwable>)(o, throwable) -> PAS0Introduction.this.completeCommand());
        }
    }

    private void handleFlyToFlowerCommand(String number) {
        if(this.commandTick == 0) {
            BeetrapStateManager bsm = this.agent.getBeetrapStateManager();
            BeetrapState bs = bsm.getState();
            FlowerManager fm = bsm.getFlowerManager();
            FlowerPool fp = bs.getFlowerPool();
            this.flyToPosition = fm.getFlowerMinecraftPosition(bsm.getState(), fp.getFlowerByNumber(
                    Integer.parseInt(number)));

            if(this.flyToPosition == null) {
                this.flyToPosition = new Vec3d(0, 0, 0);
            }

            this.beeEntity.getMoveControl()
                    .moveTo(this.flyToPosition.x, this.flyToPosition.y + 1, this.flyToPosition.z,
                            1);
            return;
        }

        this.beeEntity.getMoveControl()
                .moveTo(this.flyToPosition.x, this.flyToPosition.y + 1, this.flyToPosition.z, 1);

        if(this.beeEntity.getPos().withAxis(Axis.Y, 0).distanceTo(this.flyToPosition.withAxis(
                Axis.Y, 0)) < EPSILON) {
            this.beeEntity.setMovementSpeed(0);
            this.completeCommand();
        }
    }

    private void handleFlyToPlayerCommand() {
        if(this.commandTick == 0) {
            this.flyToPosition = this.world.getPlayers().getFirst().getPos();
            this.beeEntity.getMoveControl()
                    .moveTo(this.flyToPosition.x, this.flyToPosition.y + 1, this.flyToPosition.z,
                            1);
            return;
        }

        this.beeEntity.getMoveControl()
                .moveTo(this.flyToPosition.x, this.flyToPosition.y + 1, this.flyToPosition.z, 1);

        if(this.beeEntity.getPos().withAxis(Axis.Y, 0).distanceTo(this.flyToPosition.withAxis(
                Axis.Y, 0)) < EPSILON) {
            this.completeCommand();
        }
    }

    private void handleFlyToBeehiveCommand() {
        if(this.commandTick == 0) {
            BeetrapStateManager bsm = this.agent.getBeetrapStateManager();
            this.flyToPosition = bsm.getBeeNestController().getBeeNestPosition();
            this.beeEntity.getMoveControl()
                    .moveTo(this.flyToPosition.x, this.flyToPosition.y + 1, this.flyToPosition.z,
                            1);
            return;
        }

        this.beeEntity.getMoveControl()
                .moveTo(this.flyToPosition.x, this.flyToPosition.y + 1, this.flyToPosition.z, 1);

        if(this.beeEntity.getPos().withAxis(Axis.Y, 0).distanceTo(this.flyToPosition.withAxis(
                Axis.Y, 0)) < EPSILON) {
            this.completeCommand();
        }
    }

    private void handleFlyToCommand(String[] args) {
        String entityType = args[0];

        if(entityType.equalsIgnoreCase("flower")) {
            this.handleFlyToFlowerCommand(args[1]);
        } else if(entityType.equalsIgnoreCase("player")) {
            this.handleFlyToPlayerCommand();
        } else if(entityType.equalsIgnoreCase("beehive")) {
            this.handleFlyToBeehiveCommand();
        }
    }

    private void handleCurrentCommand() {
        LOG.info(this.currentCommand.type());

        if(this.currentCommand.type().equalsIgnoreCase("say")) {
            String dialogue = this.currentCommand.args()[0];
            this.handleSayCommand(dialogue);
            return;
        }

        if(this.currentCommand.type().equalsIgnoreCase("fly_to")) {
            this.handleFlyToCommand(this.currentCommand.args());
            return;
        }

        this.completeCommand();
    }

    public void completeCommand() {
        synchronized(this.currentCommandLock) {
            this.agent.removeNextCommand();
            this.currentCommand = null;
            this.commandTick = -1;
        }
    }

    @Override
    public void tick() {
        if(this.beeEntity == null) {
            this.beeEntity = this.physicalAgent.getBeeEntity();
        }

        if(!this.agent.hasNextCommand()) {
            this.beeEntity.lookAt(EntityAnchor.EYES, this.world.getPlayers().getFirst().getPos());
            return;
        }

        synchronized(this.currentCommandLock) {
            if(this.currentCommand == null) {
                this.currentCommand = this.agent.getNextCommand();
            }

            ++this.commandTick;
            this.handleCurrentCommand();
        }
    }

    @Override
    public void onChatMessageReceived(ServerPlayerEntity serverPlayerEntity, String message) {
        StringBuilder additionalInstructionsBuilder = new StringBuilder();
        additionalInstructionsBuilder.append("Your position: ")
                .append(this.physicalAgent.getBeeEntity().getPos()).append(System.lineSeparator());

        this.agent.getBeetrapStateManager()
                .getJsonReadyDataForGpt(this.physicalAgent.getBeeEntity(), serverPlayerEntity,
                        additionalInstructionsBuilder);
        String ai = additionalInstructionsBuilder.toString();
        LOG.info("The additional instructions: {}", ai);
        this.agent.sendGptEventMessageWithAdditionalInstructions(ai, new ChatEventMessage(message));
    }

    @Override
    public void onGameStart() {
        this.agent.sendGptEventMessage(new GameStartEventMessage());
    }
}
