package tk.ivybits.xwing;

import javax.swing.*;

public class SwingDebugger {
    public static void main(String[] argv) throws Exception {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        VMAttachFrame frame = new VMAttachFrame();
        frame.reload();
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
    }
}
