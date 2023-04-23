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
            container.add(items[x], gbc);
            gbc.gridy++;
        }
    }

    void horizontalPlacer(Component[] items) {
        for(int x = 0; x < items.length; x++) {
            gbc.gridx = x;
            container.add(items[x], gbc);
        }
        gbc.gridy++;
    }

    void vhPlacer(Component[][] items) {
        for (int y = 0; y < items.length; y++) {
            Component[] row = items[y];
            gbc.gridx = 0;
            for (int x = 0; x < row.length; x++) {
                container.add(row[x], gbc);
                gbc.gridx++;
            }
            gbc.gridy++;
        }
    }

    JPanel getContainer() { return container; }
}

//    JLabel durationLabel = new JLabel("#days");
//    JLabel duration = new JLabel("%d%M%YYYY");
//    JLabel subTotal = new JLabel("Subtotal");
//    JLabel gstLabel = new JLabel("GST");
//    JLabel subTotalAmount = new JLabel("amount");
//    JLabel gstLabelAmount = new JLabel("amount");
//    JLabel gstRate = new JLabel("gstRate");
//    JLabel total = new JLabel("Total");
//    JLabel totalAmount = new JLabel("Total");
//    JLabel alreadyPaidLabel = new JLabel("Initially Paid");
//    JLabel alreadyPaidAmount = new JLabel(String.valueOf(rese
