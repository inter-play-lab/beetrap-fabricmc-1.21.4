package beetrap.btfmc.util;

public final class TicksUtil {

    private TicksUtil() {
        throw new AssertionError();
    }

    public static boolean inInterval(long ticks, long startInclusive, long endExclusive) {
        return startInclusive <= ticks && ticks < endExclusive;
    }
}
