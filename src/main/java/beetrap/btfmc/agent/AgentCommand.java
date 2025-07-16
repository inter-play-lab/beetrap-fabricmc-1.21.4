package beetrap.btfmc.agent;

import java.util.Arrays;

public record AgentCommand(String type, String[] args) {
    @Override
    public String toString() {
        return "AgentCommand{" +
                "type='" + this.type + '\'' +
                ", args=" + Arrays.toString(this.args) +
                '}';
    }
}
