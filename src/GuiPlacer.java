import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

public class GuiPlacer {

    private final JPanel container = new JPanel(new GridBagLayout());
    private final GridBagConstraints gbc = new GridBagConstraints();

    GuiPlacer(int width, int height) {
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 1;
        gbc.weighty = 0;
        container.setBorder(BorderFactory.createLineBorder(Color.BLACK));
        container.setPreferredSize(new Dimension(width, height));
        gbc.fill = GridBagConstraints.HORIZONTAL;
    }

    void verticalPlacer(Component[] items) {
        for(int x = 0; x < items.length; x++) {
            gbc.gridy++;
            container.add(items[x], gbc);
        }
    }

    JPanel getContainer() { return container; }
}
