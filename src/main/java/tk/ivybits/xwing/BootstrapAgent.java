package tk.ivybits.xwing;

import tk.ivybits.agent.AgentLoader;
import tk.ivybits.agent.Tools;
import tk.ivybits.xwing.widgets.EchoPainter;
import tk.ivybits.xwing.widgets.MemoryMonitorBar;

import javax.swing.*;
import java.awt.*;
import java.lang.instrument.Instrumentation;

public class BootstrapAgent {
    public static void loadAgent(int vmPid) {
        try {
            Tools.loadAgentLibrary();
            AgentLoader.attachAgentToJVM(vmPid,
                    BootstrapAgent.class,
                    Tools.class,
                    AgentLoader.class,
                    EchoPainter.class,
                    SwingDebugger.class,
                    SwingDebuggerFrame.class,
                    VMAttachFrame.class,
                    MemoryMonitorBar.class);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void agentmain(String string, Instrumentation instrument) {
        System.out.println("Agent loaded!");
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SwingDebuggerFrame frame = new SwingDebuggerFrame();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
                frame.setSize(new Dimension(780, 640));
            }
        });
    }
}
