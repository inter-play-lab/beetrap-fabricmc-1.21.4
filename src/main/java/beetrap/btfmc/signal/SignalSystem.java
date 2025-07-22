package beetrap.btfmc.signal;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SignalSystem {

    private final Map<Class<? extends Signal>, List<SignalListener<? extends Signal>>> signalTypeToSignalListenersMap;

    public SignalSystem() {
        this.signalTypeToSignalListenersMap = new LinkedHashMap<>();
    }

    public <T extends Signal> void register(Class<T> signalType) {
        this.signalTypeToSignalListenersMap.put(signalType, new ArrayList<>());
    }

    public <T extends Signal> void deregister(Class<T> signalType) {
        this.signalTypeToSignalListenersMap.remove(signalType);
    }

    public <T extends Signal> void subscribe(Class<T> signalType,
            SignalListener<T> signalListener) {
        if(!this.signalTypeToSignalListenersMap.containsKey(signalType)) {
            throw new IllegalStateException("Signal type: " + signalType + " not registered.");
        }

        this.signalTypeToSignalListenersMap.get(signalType).add(signalListener);
    }

    public <T extends Signal> void unsubscribeAll(Class<T> signalType) {
        this.signalTypeToSignalListenersMap.get(signalType).clear();
    }

    private <T extends Signal> Iterable<SignalListener<T>> getListeners(
            Class<? extends Signal> signalType) {
        return () -> {
            Iterator<SignalListener<? extends Signal>> iterator = signalTypeToSignalListenersMap.get(
                    signalType).iterator();

            return new Iterator<>() {
                @Override
                public boolean hasNext() {
                    return iterator.hasNext();
                }

                @SuppressWarnings("unchecked")
                @Override
                public SignalListener<T> next() {
                    return (SignalListener<T>)iterator.next();
                }
            };
        };
    }

    public <T extends Signal> void publish(T signal) {
        for(SignalListener<Signal> sl : this.getListeners(signal.getClass())) {
            sl.onSignalReceived(signal);
        }
    }
}
