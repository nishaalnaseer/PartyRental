import javax.swing.*;
import java.util.Calendar;
import java.util.HashMap;

public class DatePicker {
    /*
    this is a class containing a gui element that is a compabination of
    three JComboBox to create a single date picker
     */
    private final JPanel panel;
    private final JComboBox<String> dayComboBox;
    private final JComboBox<String> monthComboBox;
    private final JComboBox<String> yearComboBox;
    final HashMap<String, Integer> monthDays;

    DatePicker(String text, Object[] selectedDate) {
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

        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        String[] years = new String[3];
        years[0] = "Select Year";
        for (int i = 0; i < 2; i++) {
            int year = currentYear + i;
            years[i+1] = Integer.toString(year);
        }
        yearComboBox = new JComboBox<>(years);

        monthComboBox.addActionListener(e -> setDays());
        yearComboBox.addActionListener(e -> setDays());

        JLabel label = new JLabel(text);
        panel = new JPanel();
        dayComboBox = new JComboBox<>();
        enterDays(31);

        yearComboBox.setSelectedItem(selectedDate[2]);
        monthComboBox.setSelectedItem(selectedDate[1]);
        dayComboBox.setSelectedItem(selectedDate[0]);

        panel.add(label);
        panel.add(dayComboBox);
        panel.add(monthComboBox);
        panel.add(yearComboBox);
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
        return dayComboBox.getSelectedItem() + "-" +
                     monthComboBox.getSelectedItem() + "-" +
                     yearComboBox.getSelectedItem();
    }

    Object[] getDateRaw(){
        return new Object[]{dayComboBox.getSelectedItem(), monthComboBox.getSelectedItem(), yearComboBox.getSelectedItem()};
    }
}
