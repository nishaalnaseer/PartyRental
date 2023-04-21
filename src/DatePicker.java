import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class DatePicker {
    private final JPanel panel;
    private JComboBox<String> dayComboBox;
    private final JComboBox<String> monthComboBox;
    private final JComboBox<String> yearComboBox;
    private final JLabel label;
    final HashMap<String, Integer> monthDays;

    DatePicker(String text) {
        monthComboBox = new JComboBox<>(new String[]{ "Select Month",
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
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[101];
        years[0] = "Select Year";
        for (int i = 0; i < 100; i++) {
            int year = currentYear + i;
            years[i+1] = Integer.toString(year);
        }
        yearComboBox = new JComboBox<>(years);

        monthComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
//                if (!monthComboBox.getSelectedItem().equals("Select Month")) {
//                    JOptionPane.showMessageDialog(null, "Select a Month");
//                    return;
//                }

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
        String month;
        String yearText;
        try {
            yearText = (String) yearComboBox.getSelectedItem();
            month = (String) monthComboBox.getSelectedItem();
            assert month != null;
            if(month.equals("Select Month")) {
                return;
            }
        } catch (NullPointerException ex) {
            return;
        }

        int year;
        try {
            assert yearText != null;
            year = Integer.parseInt(yearText);
        } catch (NumberFormatException ex) {
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
        dayComboBox.addItem("Select Day");

        for (int i = 1; i <= days; i++) {
            dayComboBox.addItem(Integer.toString(i));
        }
    }

    private void removeDays() {
        int itemCount = dayComboBox.getItemCount();
        for (int i = itemCount - 1; i >= 0; i--) {
            dayComboBox.removeItemAt(i);
        }
    }

    String getDate() {
        String day = (String) dayComboBox.getSelectedItem();
        return "Hello";
    }
}
