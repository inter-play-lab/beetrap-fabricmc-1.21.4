package beetrap.btfmc.agent.physical.state;

import beetrap.btfmc.agent.AgentCommand;
import beetrap.btfmc.agent.AgentState;
import beetrap.btfmc.agent.InstructionBuilder;
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

public class PhysicalAgentState extends AgentState {
    private static final double EPSILON = 0.25;
    protected PhysicalAgent physicalAgent;
    protected BeeEntity beeEntity;
    protected ServerWorld world;
    protected String name;
    protected final Object currentCommandLock;
    protected AgentCommand currentCommand;
    protected long commandTick;
    protected Vec3d flyToPosition;
    protected boolean hasNextState;
    protected PhysicalAgentState nextState;

    public PhysicalAgentState() {
        super();
        this.currentCommandLock = new Object();
    }

    public PhysicalAgentState(PhysicalAgentState state) {
        this.physicalAgent = state.physicalAgent;
        this.beeEntity = state.beeEntity;
        this.world = state.world;
        this.name = state.name;
        this.currentCommandLock = state.currentCommandLock;
        this.commandTick = state.commandTick;
        this.flyToPosition = state.flyToPosition;
        this.hasNextState = false;
        this.nextState = null;
    }

    @Override
    public void onAttach() {
        super.onAttach();
        this.physicalAgent = (PhysicalAgent)this.agent;
        this.beeEntity = this.physicalAgent.getBeeEntity();
        this.world = this.agent.getWorld();
        this.name = this.agent.getName();
        this.commandTick = -1;
    }

    private void handleSayCommand(String dialogue) {
        if(this.commandTick == 0) {
            this.world.getPlayers().forEach(
                    serverPlayerEntity -> serverPlayerEntity.sendMessage(
                            Text.of("<" + this.name + "> " + dialogue)));
            SlopTextToSpeechUtil.say(dialogue).whenComplete(
                    (BiConsumer<Object, Throwable>)(o, throwable) -> this.completeCommand());
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
        } else {
            synchronized(this.currentCommandLock) {
                if(this.currentCommand == null) {
                    this.currentCommand = this.agent.getNextCommand();
                }

                ++this.commandTick;
                this.handleCurrentCommand();
            }
        }

        if(!(this instanceof PAS1EndGame)) {
            if(this.agent.getBeetrapStateManager().isActivityEnded()) {
                this.hasNextState = true;
                this.nextState = new PAS1EndGame(this);
            }
        }
    }

    public void updateStateInstruction(InstructionBuilder ib) {
        ib.resetStateInstructionBuilder();
    }

    private void updateContextInstruction(InstructionBuilder ib, ServerPlayerEntity serverPlayerEntity) {
        ib.resetContextInstructionBuilder();
        StringBuilder contextInstructionBuilder = ib.contextInstructionBuilder();

        contextInstructionBuilder.append("Your position: ")
                .append(this.physicalAgent.getBeeEntity().getPos()).append(System.lineSeparator());

        this.agent.getBeetrapStateManager()
                .getJsonReadyDataForGpt(this.physicalAgent.getBeeEntity(), serverPlayerEntity,
                        contextInstructionBuilder);
    }

    public void updateInstructions(ServerPlayerEntity serverPlayerEntity) {
        InstructionBuilder ib = this.agent.getInstructionBuilder();
        this.updateStateInstruction(ib);
        this.updateContextInstruction(ib, serverPlayerEntity);
    }

    @Override
    public void onChatMessageReceived(ServerPlayerEntity serverPlayerEntity, String message) {
        this.updateInstructions(serverPlayerEntity);
        this.agent.sendGptEventMessage(new ChatEventMessage(message));
    }

    @Override
    public void onGameStart() {
        this.updateInstructions(this.world.getPlayers().getFirst());
        this.agent.sendGptEventMessage(new GameStartEventMessage());
    }

    @Override
    public boolean hasNextState() {
        return this.hasNextState;
    }

    @Override
    public AgentState getNextState() {
        return this.nextState;
    }
}
