package beetrap.btfmc.agent;

import static beetrap.btfmc.Beetrapfabricmc.MOD_ID;

import beetrap.btfmc.agent.event.EventMessage;
import beetrap.btfmc.openai.OpenAiUtil;
import beetrap.btfmc.state.BeetrapStateManager;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.responses.Response;
import com.openai.models.responses.ResponseCreateParams;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.concurrent.CompletableFuture;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.network.packet.Packet;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Agent implements AutoCloseable {
    public static final int AGENT_LEVEL_NO_AGENT = 0;
    public static final int AGENT_LEVEL_CHAT_ONLY = 1;
    public static final int AGENT_LEVEL_CHAT_WITH_VOICE_TO_TEXT = 2;
    public static final int AGENT_LEVEL_PHYSICAL = 3;

    private static final Logger LOG = LogManager.getLogger(Agent.class);
    private static final ObjectMapper om;

    public static final String AGENT_NAME = "Bip Buzzley";

    protected final ServerWorld world;
    protected final String name;
    protected final OpenAIClient openAiClient;
    protected Response previousResponse;
    protected Deque<AgentCommand> agentCommandQueue;

    private final BeetrapStateManager beetrapStateManager;
    protected AgentState currentState;
    private String instructions;

    private void setCurrentAgentState(AgentState currentAgentState) {
        this.currentState = currentAgentState;
        this.currentState.onAttach(this);
    }

    public Agent(ServerWorld world, BeetrapStateManager beetrapStateManager, String name, AgentState initialAgentState) {
        this.world = world;
        this.beetrapStateManager = beetrapStateManager;
        this.name = name;
        this.setCurrentAgentState(initialAgentState);
        this.openAiClient = OpenAiUtil.getClient();
        this.agentCommandQueue = new ArrayDeque<>();
    }

    public Agent(ServerWorld world, BeetrapStateManager beetrapStateManager, AgentState initialState) {
        this(world, beetrapStateManager, AGENT_NAME, initialState);
    }

    public BeetrapStateManager getBeetrapStateManager() {
        return this.beetrapStateManager;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public String getInstructions() {
        return this.instructions;
    }

    public void onPlayerPollinate(ServerPlayerEntity serverPlayerEntity) {

    }

    public void tick() {
        if(this.currentState.hasNextState()) {
            this.setCurrentAgentState(this.currentState.getNextState());
        }

        this.tickCustom();
        this.currentState.tick();
    }

    protected void tickCustom() {

    }

    public void onChatMessageReceived(ServerPlayerEntity serverPlayerEntity, String message) {
        this.currentState.onChatMessageReceived(serverPlayerEntity, message);
    }

    public void onGameStart() {
        this.currentState.onGameStart();
    }

    public void close() {

    }

    public void sendGpt4oLatestResponseToInputToChatWithPresetInstructions(String input) {
        this.getGpt4oLatestResponseAsyncWithPresetInstructions(input).whenComplete(this::onGptResponseReceived);
    }

    public void sendGpt4oLatestResponseToInputToChatWithAdditionInstructionsToPreset(String additionalInstructions, String input) {
        this.getGpt4oLatestResponseAsyncWithInstructions(this.instructions + additionalInstructions, input).whenComplete(this::onGptResponseReceived);
    }

    public void onGptResponseReceived(Response response, Throwable throwable) {
        String string = response.output().getFirst().asMessage().content().getFirst().asOutputText().text();

        LOG.info("Raw response: {}", string);

        try {
            GptResponse gptResponse = om.readValue(string, GptResponse.class);

            for(AgentCommand agentCommand : gptResponse.getAgentCommands()) {
                this.agentCommandQueue.addLast(agentCommand);
            }
        } catch(JsonProcessingException e) {
            LOG.error(e);
        }

    }

    public void sendGptEventMessage(EventMessage eventMessage) {
        this.sendGpt4oLatestResponseToInputToChatWithPresetInstructions(eventMessage.toJsonString());
    }

    public void sendGptEventMessageWithAdditionalInstructions(String instructions, EventMessage eventMessage) {
        this.sendGpt4oLatestResponseToInputToChatWithAdditionInstructionsToPreset(instructions, eventMessage.toJsonString());
    }

    public CompletableFuture<Response> getGpt4oLatestResponseAsyncWithInstructions(String instructions, String input) {
        ResponseCreateParams params;

        if(previousResponse != null) {
            params = ResponseCreateParams.builder()
                    .instructions(instructions)
                    .input(input)
                    .model(ChatModel.CHATGPT_4O_LATEST)
                    .previousResponseId(previousResponse.id())
                    .build();
        } else {
            params = ResponseCreateParams.builder()
                    .instructions(instructions)
                    .input(input)
                    .model(ChatModel.CHATGPT_4O_LATEST)
                    .build();
        }

        CompletableFuture<Response> response = this.openAiClient.async().responses().create(params);
        return response.whenComplete((response1, throwable) -> {
            this.previousResponse = response1;
            if(throwable != null) {
                LOG.error(throwable);
            }
        });
    }

    public CompletableFuture<Response> getGpt4oLatestResponseAsyncWithPresetInstructions(String input) {
        ResponseCreateParams params;

        if(previousResponse != null) {
            params = ResponseCreateParams.builder()
                    .instructions(this.instructions)
                    .input(input)
                    .model(ChatModel.CHATGPT_4O_LATEST)
                    .previousResponseId(previousResponse.id())
                    .build();
        } else {
            params = ResponseCreateParams.builder()
                    .instructions(instructions)
                    .input(input)
                    .model(ChatModel.CHATGPT_4O_LATEST)
                    .build();
        }

        CompletableFuture<Response> response = this.openAiClient.async().responses().create(params);
        return response.whenComplete((response1, throwable) -> {
            this.previousResponse = response1;

            if(throwable != null) {
                LOG.error(throwable);
            }
        });
    }

    public void sendPacketToAllPlayers(Packet<?> packet) {
        for(ServerPlayerEntity player : this.world.getPlayers()) {
            player.networkHandler.sendPacket(packet);
        }
    }

    public void sendCustomPayloadToAllPlayers(CustomPayload payload) {
        for(ServerPlayerEntity player : this.world.getPlayers()) {
            ServerPlayNetworking.send(player, payload);
        }
    }

    public ServerWorld getWorld() {
        return this.world;
    }

    public String getName() {
        return this.name;
    }

    public void addCommand(AgentCommand agentCommand) {
        this.agentCommandQueue.addLast(agentCommand);
    }

    public AgentCommand nextCommand() {
        return this.agentCommandQueue.removeFirst();
    }

    public boolean hasNextCommand() {
        return !this.agentCommandQueue.isEmpty();
    }

    static {
        om = new ObjectMapper();
        SimpleModule sm = new SimpleModule();
        sm.addDeserializer(AgentCommand.class, new AgentCommandDeserializer());
        sm.addDeserializer(GptResponse.class, new GptResponseDeserializer());
        om.registerModule(sm);
    }
}
