import javax.swing.*;
import java.awt.*;
import java.util.Stack;

public class Navigator {
    /*
    class to handles opening and closing of panels
    it really works by putting panels into a stack. what's displayed is at the top of the stack
    got the idea from flutter, java < flutter for client side applications
     */
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
