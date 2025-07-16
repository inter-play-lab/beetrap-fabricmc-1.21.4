package beetrap.btfmc.agent;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

public class AgentCommandDeserializer extends StdDeserializer<AgentCommand> {

    public AgentCommandDeserializer() {
        super((Class<?>)null);
    }

    @Override
    public AgentCommand deserialize(JsonParser jsonParser,
            DeserializationContext deserializationContext) throws IOException {
        ObjectCodec codec = jsonParser.getCodec();
        JsonNode node = codec.readTree(jsonParser);
        JsonNode argsNode = node.get("args");
        int n = argsNode.size();
        String[] args = new String[n];

        for(int i = 0; i < n; ++i) {
            args[i] = argsNode.get(i).asText();
        }

        return new AgentCommand(node.get("type").asText(), args);
    }
}
