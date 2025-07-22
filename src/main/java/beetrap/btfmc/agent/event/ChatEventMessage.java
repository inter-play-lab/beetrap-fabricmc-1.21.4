package beetrap.btfmc.agent.event;

public class ChatEventMessage extends EventMessage {

    public ChatEventMessage(String message) {
        super(EVENT_MESSAGE_KEY_EVENT_TYPE_VALUE_CHAT_EVENT);
        this.setMessage(message);
    }

    public String getMessage() {
        return super.get(CHAT_EVENT_MESSAGE_KEY_PLAYER_MESSAGE).toString();
    }

    public void setMessage(String message) {
        super.put(CHAT_EVENT_MESSAGE_KEY_PLAYER_MESSAGE, message);
    }
}
