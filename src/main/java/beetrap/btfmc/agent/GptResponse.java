package beetrap.btfmc.agent;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.Arrays;

public class GptResponse {

    @JsonAlias("commands")
    private final AgentCommand[] commands;

    public GptResponse(AgentCommand[] commands) {
        this.commands = commands;
    }

    public AgentCommand[] getAgentCommands() {
        return this.commands;
    }

    @Override
    public String toString() {
        return "GptResponse{" +
                "commands=" + Arrays.toString(commands) +
                '}';
    }
}
