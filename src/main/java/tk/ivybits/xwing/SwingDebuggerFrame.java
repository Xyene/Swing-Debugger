package tk.ivybits.xwing;

import tk.ivybits.xwing.widgets.EchoPainter;
import tk.ivybits.xwing.widgets.MemoryMonitorBar;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import java.awt.*;
import java.awt.event.AWTEventListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class SwingDebuggerFrame extends JFrame {
    public class ComponentTreeNode<T extends Component> extends DefaultMutableTreeNode {
        public T getComponent() {
            return component;
        }

        private final T component;

        public ComponentTreeNode(T component) {
            super("", true);
            String title = component.getClass().getSimpleName();
//            if (component instanceof Frame)
//                title += "(" + ((Frame) component).getTitle() + ")";
            this.setUserObject(title);
            this.component = component;
        }
    }

    protected final List<Window> tracked = new ArrayList<>();
    protected final JTree componentTree = new JTree();
    protected final JScrollPane echoPane = new JScrollPane();

    JScrollPane scrollable(Component c) {
        JScrollPane jsp = new JScrollPane(c);
        jsp.setBorder(BorderFactory.createEmptyBorder());
        return jsp;
    }

    public SwingDebuggerFrame() {
        super("Xwing Debugger");
        setLayout(new BorderLayout());

        JSplitPane pane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        pane.setLeftComponent(scrollable(componentTree));

        pane.setRightComponent(echoPane);
        pane.setContinuousLayout(true);
        pane.setResizeWeight(0.2);

        add(pane, BorderLayout.CENTER);

        JPanel statusBar = new JPanel(new BorderLayout());
        statusBar.add(new MemoryMonitorBar(), BorderLayout.EAST);
        add(statusBar, BorderLayout.SOUTH);

        componentTree.setRootVisible(false);
        final DefaultMutableTreeNode root;
        componentTree.addTreeSelectionListener(new TreeSelectionListener() {
            @Override
            public void valueChanged(TreeSelectionEvent e) {
                echoPane.setViewportView(new EchoPainter(((ComponentTreeNode) e.getPath().getLastPathComponent()).getComponent()));
                echoPane.revalidate();
            }
        });
        ((DefaultTreeModel) componentTree.getModel()).setRoot(root = new DefaultMutableTreeNode(""));

        Toolkit.getDefaultToolkit().addAWTEventListener(new MyAWTEventListener(root), AWTEvent.WINDOW_EVENT_MASK);
        for (Window w : Window.getWindows()) {
            if (w == SwingDebuggerFrame.this) continue;
            if (tracked.contains(w)) continue;
            tracked.add(w);
            trackComponent((TreeNode) componentTree.getModel().getRoot(), w);
        }
        ((DefaultTreeModel) componentTree.getModel()).reload();
        pack();
    }

    protected <T extends Component> void trackComponent(TreeNode parent, T c) {
        DefaultMutableTreeNode child = new ComponentTreeNode<>(c);
        ((DefaultMutableTreeNode) parent).add(child);
        if (c instanceof Container) {
            for (Component ch : ((Container) c).getComponents()) {
                trackComponent(child, ch);
            }
        }
    }

    private class MyAWTEventListener implements AWTEventListener {
        private final DefaultMutableTreeNode root;

        public MyAWTEventListener(DefaultMutableTreeNode root) {
            this.root = root;
        }

        @Override
        public void eventDispatched(AWTEvent _evt) {
            try {
                if (_evt instanceof WindowEvent) {
                    WindowEvent evt = (WindowEvent) _evt;
                    Window window = evt.getWindow();
                    if (evt.getWindow() == SwingDebuggerFrame.this) return;
                    int i = evt.getID();
                    if (i == WindowEvent.WINDOW_OPENED) {
                        trackComponent((TreeNode) componentTree.getModel().getRoot(), window);
                        tracked.add(window);
                        ((DefaultTreeModel) componentTree.getModel()).reload();
                    } else if (i == WindowEvent.WINDOW_CLOSING) {
                        tracked.remove(window);
                        Enumeration children = root.children();
                        while (children.hasMoreElements()) {
                            ComponentTreeNode child = (ComponentTreeNode) children.nextElement();
                            System.out.println(child);
                            if (child.getComponent() == window) {
                                root.remove(child);
                                break;
                            }
                        }
                        ((DefaultTreeModel) componentTree.getModel()).reload();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
