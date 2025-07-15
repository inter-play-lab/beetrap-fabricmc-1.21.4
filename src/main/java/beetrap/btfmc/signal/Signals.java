package beetrap.btfmc.signal;

public final class Signals {
    private static final SignalSystem ss = new SignalSystem();

    private Signals() {
        throw new AssertionError();
    }

    public static <T extends Signal> void register(Class<T> signalType) {
        ss.register(signalType);
    }

    public static <T extends Signal> void deregister(Class<T> signalType) {
        ss.deregister(signalType);
    }

    public static <T extends Signal> void subscribe(Class<T> signalType,
            SignalListener<T> signalListener) {
        ss.subscribe(signalType, signalListener);
    }

    public static <T extends Signal> void unsubscribeAll(Class<T> signalType) {
        ss.unsubscribeAll(signalType);
    }

    public static <T extends Signal> void publish(T signal) {
        ss.publish(signal);
    }
}
