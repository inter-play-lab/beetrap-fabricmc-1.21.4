package beetrap.btfmc.agent.event;

public class GameEndEventMessage extends EventMessage {
    public GameEndEventMessage() {
        super(EventMessage.EventTypeValue.GAME_END.toString());
    }
}
