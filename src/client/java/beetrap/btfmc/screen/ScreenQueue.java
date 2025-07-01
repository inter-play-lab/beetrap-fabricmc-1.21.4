package beetrap.btfmc.screen;

import java.util.LinkedList;
import java.util.List;
import net.minecraft.client.gui.screen.Screen;

public class ScreenQueue {
    private final List<Screen> stack;
    private boolean active;

    public ScreenQueue() {
        this.stack = new LinkedList<>();
    }

    public boolean shouldShowNext() {
        return !this.stack.isEmpty() && !this.active;
    }

    public void push(Screen ts) {
        this.stack.add(ts);
    }

    public Screen pop() {
        return this.stack.removeFirst();
    }

    void setActive(boolean f) {
        this.active = f;
    }
}
