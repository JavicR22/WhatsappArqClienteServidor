package org.example.view;

import javax.swing.*;
import java.awt.*;

public class UserListRenderer extends JPanel implements ListCellRenderer<UserListItem> {
    private final JLabel lblAvatar = new JLabel();
    private final JLabel lblName = new JLabel();

    public UserListRenderer() {
        setLayout(new BorderLayout(8, 0));
        add(lblAvatar, BorderLayout.WEST);
        add(lblName, BorderLayout.CENTER);
        setBorder(BorderFactory.createEmptyBorder(4,4,4,4));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends UserListItem> list, UserListItem value, int index, boolean isSelected, boolean cellHasFocus) {
        lblName.setText(value.getUsername());
        if (value.getAvatar() != null) {
            lblAvatar.setIcon(value.getAvatar());
        } else {
            lblAvatar.setIcon(ImageUtils.createPlaceholderIcon(40));
        }

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }
        return this;
    }
}
