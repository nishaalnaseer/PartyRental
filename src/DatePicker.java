import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Calendar;
import java.util.HashMap;

public class DatePicker {
    private final JPanel panel;
    private JComboBox<Integer> dayComboBox;
    private final JComboBox<String> monthComboBox;
    private final JComboBox<Integer> yearComboBox;
    private final JLabel label;
    final HashMap<String, Integer> monthDays;

    DatePicker(String text) {
        monthComboBox = new JComboBox<>(new String[]{
                "January", "February", "March", "April", "May", "June", "July",
                "August", "September", "October", "November", "December"
        });

        monthDays = new HashMap<>();
        monthDays.put("January", 31);
        monthDays.put("March", 31);
        monthDays.put("May", 31);
        monthDays.put("July", 31);
        monthDays.put("August", 31);
        monthDays.put("October", 31);
        monthDays.put("December", 31);
        monthDays.put("April", 30);
        monthDays.put("June", 30);
        monthDays.put("September", 30);
        monthDays.put("November", 30);
//
        yearComboBox = new JComboBox<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        for (int i = currentYear; i >= currentYear - 100; i--) {
            yearComboBox.addItem(i);
        }

        monthComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDays();
            }
        });
        yearComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setDays();
            }
        });
//
        label = new JLabel(text);
        panel = new JPanel();
        dayComboBox = new JComboBox<>();
        enterDays(31);
        panel.add(label);
        panel.add(dayComboBox);
        panel.add(monthComboBox);
        panel.add(yearComboBox);
    }
    void print(String text) {
        // development code delete on production
        System.out.println(text);
    }

    private void setDays() {
        String month = (String) monthComboBox.getSelectedItem();

        int year;
        try {
            year = (int) yearComboBox.getSelectedItem();
        } catch (NullPointerException ex) {
            return;
        }

        removeDays();
        if (month.equals("February") && (year % 4 == 0)) {
            enterDays(28);
        } else if (month.equals("February")) {
            enterDays(29);
        } else {
            int days = monthDays.get(month);
            enterDays(days);
        }
    }

    JPanel getPanel() {
        return panel;
    }

    private void enterDays(int days) {
        for (int i = 1; i <= days; i++) {
            dayComboBox.addItem(i);
        }
    }

    private void removeDays() {
        int itemCount = dayComboBox.getItemCount();
        for (int i = itemCount - 1; i >= 0; i--) {
            dayComboBox.removeItemAt(i);
        }
    }
}
