import javax.swing.*;
import java.awt.*;
import java.util.Stack;

public class Navigator {
    private final Stack<JPanel> stack = new Stack<>();
    private final JFrame root;

    Navigator(JFrame root) {
        this.root = root;
    }

    void open(JPanel panel, CardLayout layout, String desc) {
        root.getContentPane().add(panel, desc);
        layout.show(root.getContentPane(), desc);

        stack.push(panel);
    }

    void close(CardLayout layout) {
//        JPanel panel = stack.pop();
//        this.root.remove(panel);
        layout.previous(root.getContentPane());
    }
}
