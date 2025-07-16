package beetrap.btfmc.agent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class GptResponseDeserializer extends StdDeserializer<GptResponse> {
    private final AgentCommandDeserializer agentCommandDeserializer;

    public GptResponseDeserializer() {
        super((Class<?>)null);
        this.agentCommandDeserializer = new AgentCommandDeserializer();
    }

    @Override
    public GptResponse deserialize(JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        JsonNode commandsNode = node.get("commands");
        int n = commandsNode.size();
        AgentCommand[] commands = new AgentCommand[n];

        for(int i = 0; i < n; ++i) {
            JsonNode agentCommandNode = commandsNode.get(i);
            commands[i] = this.agentCommandDeserializer.deserialize(agentCommandNode.traverse(codec), deserializationContext);
        }

        return new GptResponse(commands);
    }
}
