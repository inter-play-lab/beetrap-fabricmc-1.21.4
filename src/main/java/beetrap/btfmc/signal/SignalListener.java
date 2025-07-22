package beetrap.btfmc.signal;

public interface SignalListener<T extends Signal> {

    void onSignalReceived(T signal);
}
