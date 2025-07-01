package beetrap.btfmc.state;

public class TimeTravelableBeetrapState extends ObserveFlowersOnlyState {
    public TimeTravelableBeetrapState(BeetrapState representedState) {
        super(representedState);
    }

    @Override
    public boolean timeTravelAvailable() {
        return true;
    }
}
