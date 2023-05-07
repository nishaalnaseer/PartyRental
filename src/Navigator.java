import javax.swing.*;
import java.awt.*;
import java.util.Stack;

public class Navigator {
    private final Stack<JPanel> stack = new Stack<>();
    private final JFrame root;
    private final CardLayout layout;

    Navigator(JFrame root, CardLayout layout) {
        this.root = root;
        this.layout = layout;
    }

    void open(JPanel panel, String desc) {
        root.getContentPane().add(panel, desc);
        layout.show(root.getContentPane(), desc);

        stack.push(panel);
    }

    void close() {
        JPanel panel = stack.pop();
        layout.previous(root.getContentPane());
        this.root.remove(panel);
        panel = null;  // manual garbage collection
        System.gc();  // manual garbage collection
    }
}
