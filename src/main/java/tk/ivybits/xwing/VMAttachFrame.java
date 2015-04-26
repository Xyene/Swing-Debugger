package tk.ivybits.xwing;

import sun.tools.jconsole.LocalVirtualMachine;
import tk.ivybits.agent.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VMAttachFrame extends JFrame {
    public void reload() {
        vmpid.clear();
        final DefaultListModel<String> n = new DefaultListModel<>();
        Map<Integer, LocalVirtualMachine> map = LocalVirtualMachine.getAllVirtualMachines();
        map.remove(Tools.getCurrentPID());
        for (Map.Entry<Integer, LocalVirtualMachine> machine : map.entrySet()) {
            Integer id = machine.getKey();
            vmpid.add(id);
            String base = String.valueOf(id);
            if (!machine.getValue().displayName().isEmpty()) {
                base += " (" + machine.getValue().displayName() + ")";
            }
            n.addElement(base);
        }
        final Object idx = vms.getSelectedValue();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                vms.setModel(n);
                vms.setSelectedValue(idx, true);
            }
        });
    }

    private final JList<String> vms = new JList<>(new DefaultListModel<String>());
    private final List<Integer> vmpid = new ArrayList<>();
    private final Thread poll;

    public VMAttachFrame() {
        super("Choose VM to attach to...");
        setLayout(new BorderLayout());
        poll = new Thread() {
            public void run() {
                while (true) {
                    reload();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        vms.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int id = vmpid.get(vms.getSelectedIndex());
                    System.out.println("Strapping on " + id);
                    BootstrapAgent.loadAgent(id);
                }
            }
        });
//        System.out.println(System.getProperty("java.io.tmpdir") + "hsperfdata_" + System.getProperty("user.name") + File.separatorChar);
//        File[] list = new File(System.getProperty("java.io.tmpdir") + "hsperfdata_" + System.getProperty("user.name") + File.separatorChar).listFiles();
//        for (File id : list) {
//            ((DefaultListModel) vms.getModel()).addElement(id.getName());
//        }
        add(new JScrollPane(vms), BorderLayout.CENTER);
        reload();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setPreferredSize(new Dimension(400, 200));
        pack();
    }

    @Override
    public void addNotify() {
        super.addNotify();
        poll.start();
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        try {
            poll.stop();
        } catch (Throwable ignored) {
        }
    }
}

