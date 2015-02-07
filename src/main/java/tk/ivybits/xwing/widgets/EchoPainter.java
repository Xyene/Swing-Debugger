package tk.ivybits.xwing.widgets;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.TimerTask;
import java.util.Timer;

public class EchoPainter extends JPanel {
    private final Component what;
    private final Timer repaintTimer = new Timer();

    public EchoPainter(final Component what) {
        this.what = what;
        System.out.println("Echoing " + what);
        this.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                System.out.println(e);
                what.dispatchEvent(e);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                what.dispatchEvent(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                what.dispatchEvent(e);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                what.dispatchEvent(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                what.dispatchEvent(e);
            }

            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                what.dispatchEvent(e);
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                what.dispatchEvent(e);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                what.dispatchEvent(e);
            }
        });
    }

    @Override
    public void addNotify() {
        super.addNotify();
        TimerTask repaint = new TimerTask() {
            @Override
            public void run() {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        Dimension size = what.getSize();
                        setSize(size);
                        setPreferredSize(size);
                        revalidate();
                        repaint();
                    }
                });
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
                    ((JFrame) what).getRootPane().printAll(g);
                else what.printAll(g);
            } catch (Throwable ex) {
                ex.printStackTrace();
            }
        }
    }
}