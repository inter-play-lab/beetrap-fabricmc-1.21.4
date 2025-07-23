package beetrap.btfmc.agent.event;

public class GameStartEventMessage extends EventMessage {

    public GameStartEventMessage() {
        super(EventTypeValue.GAME_START.toString());
    }
}
