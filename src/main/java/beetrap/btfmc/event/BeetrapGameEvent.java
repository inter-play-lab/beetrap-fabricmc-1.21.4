package beetrap.btfmc.event;

public class BeetrapGameEvent<T> {
    private final EventType type;
    private final T data;

    public enum EventType {
        ENTITY_TARGETED,
        DEAD_FLOWER,
        PLAYER_POLLINATE,
        TIME_TRAVEL,
        SUB_ACTIVITY_BEGIN,
        SCREEN_SHOWN
    }

    public BeetrapGameEvent(EventType type, T data) {
        this.type = type;
        this.data = data;
    }

    public EventType getType() {
        return type;
    }

    public T getData() {
        return data;
    }

    @Override
    public String toString() {
        return "BeetrapGameEvent{" +
                "type=" + type +
                ", data=" + data +
                '}';
    }
}
