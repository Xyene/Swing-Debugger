package tk.ivybits.xwing.widgets;

import javax.swing.*;
import java.awt.*;
import java.util.TimerTask;
import java.util.Timer;

public class EchoPainter extends JPanel {
    private final Component what;
    private final Timer repaintTimer = new Timer();

    public EchoPainter(final Component what) {
        this.what = what;
    }

    @Override
    public void addNotify() {
        super.addNotify();

        TimerTask repaint = new TimerTask() {
            @Override
            public void run() {
                Dimension size = what.getSize();
                setSize(size);
                setPreferredSize(size);
                revalidate();
                repaint();
            }
        };
        repaintTimer.schedule(repaint, 10, 10);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        repaintTimer.cancel();
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (what != null) {
            try {
                if (what instanceof JFrame)
                    ((JFrame) what).getRootPane().paint(g);
                else what.paint(g);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}