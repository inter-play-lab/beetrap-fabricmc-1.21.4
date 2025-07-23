package beetrap.btfmc.agent.physical.state;

import beetrap.btfmc.agent.InstructionBuilder;
import beetrap.btfmc.agent.event.GameEndEventMessage;

public class PAS1EndGame extends PhysicalAgentState {
    private final String stateInstruction;

    public PAS1EndGame() {
        this.stateInstruction = """
        The game has ended! That means the user has reached a diversity so low that it is below a threshold set by the game.
        Stop giving instructions that encourages players to interact with the garden - the garden now is only observable, not modifiable.
        """;
    }

    @Override
    public void updateStateInstruction(InstructionBuilder ib) {
        ib.resetStateInstructionBuilder();
        ib.stateInstructionBuilder().append(this.stateInstruction);
    }

    @Override
    public void onAttach() {
        super.onAttach();
        this.updateInstructions(this.world.getPlayers().getFirst());
        this.agent.sendGptEventMessage(new GameEndEventMessage());
    }
}
