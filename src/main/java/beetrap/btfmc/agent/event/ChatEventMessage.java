package beetrap.btfmc.agent.event;

public class ChatEventMessage extends EventMessage {
    public ChatEventMessage(String message) {
        super(EventMessage.EventTypeValue.CHAT.toString());
        this.setMessage(message);
    }

    public String getMessage() {
        return super.get(Key.MESSAGE.toString()).toString();
    }

    public void setMessage(String message) {
        super.put(Key.MESSAGE.toString(), message);
    }

    public enum Key {
        MESSAGE
    }
}
