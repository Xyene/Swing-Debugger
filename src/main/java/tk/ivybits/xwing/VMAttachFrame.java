package tk.ivybits.xwing;

import sun.tools.jconsole.LocalVirtualMachine;
import tk.ivybits.agent.Tools;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class VMAttachFrame extends JFrame {
    public void reload() {
        vmpid.clear();
        DefaultListModel n = new DefaultListModel();
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
        Object idx = vms.getSelectedValue();
        vms.setModel(n);

        vms.setSelectedValue(idx, true);
    }

    private JList vms = new JList(new DefaultListModel());
    private List<Integer> vmpid = new ArrayList<Integer>();

    public VMAttachFrame() {
        super("Choose VM to attach to...");
        setLayout(new BorderLayout());
        Thread poll = new Thread() {
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
        poll.start();

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
        pack();
    }
}
