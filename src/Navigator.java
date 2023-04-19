import javax.swing.*;
import java.util.Stack;
import java.awt.*;

public class Navigator {
    private final Stack<JPanel> stack = new Stack<>();
    private final JFrame root;

    Navigator(JFrame root) {
        this.root = root;
    }

    void open(JPanel panel) {
        this.root.add(panel);
        stack.push(panel);
    }

    void close() {
        JPanel panel = stack.pop();
        this.root.remove(panel);
    }
}
