package beetrap.btfmc.agent.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;

public class EventMessage extends LinkedHashMap<String, Object> {
    private static final ObjectMapper om;

    public static final String EVENT_MESSAGE_KEY_EVENT_TYPE = "event_type";
    public static final String EVENT_MESSAGE_KEY_EVENT_TYPE_VALUE_CHAT_EVENT = "chat";
    public static final String CHAT_EVENT_MESSAGE_KEY_PLAYER_MESSAGE = "player_message";
    public static final String EVENT_MESSAGE_KEY_EVENT_TYPE_VALUE_GAME_START_EVENT = "game_start";

    public EventMessage(String eventType) {
        this.setEventType(eventType);
    }

    public final void setEventType(String eventType) {
        super.put(EVENT_MESSAGE_KEY_EVENT_TYPE, eventType);
    }

    public final String toJsonString() {
        try {
            return om.writeValueAsString(this);
        } catch(JsonProcessingException e) {
            throw new AssertionError(e);
        }
    }

    static {
        om = new ObjectMapper();
    }
}
