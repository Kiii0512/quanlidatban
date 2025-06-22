package test;

import javax.swing.*;
import javax.swing.event.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.*;

public class GhostText implements FocusListener, DocumentListener {
    private final JTextComponent comp;
    private final String ghost;
    private boolean showingGhost;

    public GhostText(JTextComponent comp, String ghost) {
        this.comp = comp;
        this.ghost = ghost;
        this.showingGhost = true;
        comp.addFocusListener(this);
        comp.getDocument().addDocumentListener(this);
        showGhost();
    }

    private void showGhost() {
        // Đưa vào invokeLater để không mutate ngay trong notification
        SwingUtilities.invokeLater(() -> {
            comp.setText(ghost);
            comp.setForeground(Color.GRAY);
            showingGhost = true;
        });
    }

    private void hideGhost() {
        SwingUtilities.invokeLater(() -> {
            comp.setText("");
            comp.setForeground(Color.BLACK);
            showingGhost = false;
        });
    }

    @Override
    public void focusGained(FocusEvent e) {
        if (showingGhost) hideGhost();
    }

    @Override
    public void focusLost(FocusEvent e) {
        if (comp.getText().isEmpty()) showGhost();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        // vẫn hideGhost khi vừa gõ ký tự đầu
        if (showingGhost) hideGhost();
    }
    @Override public void removeUpdate(DocumentEvent e) {}
    @Override public void changedUpdate(DocumentEvent e) {}
}