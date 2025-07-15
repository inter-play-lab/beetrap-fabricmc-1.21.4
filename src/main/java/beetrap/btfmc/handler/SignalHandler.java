package beetrap.btfmc.handler;

import beetrap.btfmc.signal.Signals;
import beetrap.btfmc.signal_type.PlayerTimeTravelSignal;

public final class SignalHandler {
    private SignalHandler() {
        throw new AssertionError();
    }

    public static void registerSignalTypes() {
        Signals.register(PlayerTimeTravelSignal.class);
    }

    public static void deregisterSignalTypes() {
        Signals.deregister(PlayerTimeTravelSignal.class);
    }
}
