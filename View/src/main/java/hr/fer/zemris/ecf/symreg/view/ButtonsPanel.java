package hr.fer.zemris.ecf.symreg.view;

import javax.swing.*;

/**
 * Created by Domagoj on 09/06/15.
 */
public class ButtonsPanel extends JPanel {

    private JButton runBtn;
    private JButton resBtn;
    private boolean resBtnAdded = false;

    public ButtonsPanel(JButton runBtn, JButton resBtn) {
        super();
        this.runBtn = runBtn;
        this.resBtn = resBtn;

        setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
        add(runBtn);
    }

    public void addResBtn() {
        if (!resBtnAdded) {
            add(resBtn);
            resBtnAdded = true;
        }
    }

    public JButton getRunBtn() {
        return runBtn;
    }

    public JButton getResBtn() {
        return resBtn;
    }
}