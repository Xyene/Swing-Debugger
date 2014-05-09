package tk.ivybits.agent;

import com.sun.tools.attach.AgentInitializationException;
import com.sun.tools.attach.AgentLoadException;
import com.sun.tools.attach.AttachNotSupportedException;
import com.sun.tools.attach.VirtualMachine;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

import static tk.ivybits.agent.Tools.getBytesFromStream;

/**
 * A utility class for loading Java agents.
 *
 */
public class AgentLoader {

    /**
     * Loads an agent into a JVM.
     *
     * @param agent     The main agent class.
     * @param resources Array of classes to be included with agent.
     * @param pid       The ID of the target JVM.
     * @throws java.io.IOException
     * @throws AttachNotSupportedException
     * @throws AgentLoadException
     * @throws AgentInitializationException
     */
    public static void attachAgentToJVM(int pid, Class agent, Class... resources)
            throws IOException, AttachNotSupportedException, AgentLoadException, AgentInitializationException {

        VirtualMachine vm = VirtualMachine.attach(String.valueOf(pid));
        vm.loadAgent(generateAgentJar(agent, resources).getAbsolutePath());
        vm.detach();
    }

    /**
     * Generates a temporary agent file to be loaded.
     *
     * @param agent     The main agent class.
     * @param resources Array of classes to be included with agent.
     * @return Returns a temporary jar file with the specified classes included.
     * @throws java.io.FileNotFoundException
     * @throws java.io.IOException
     */
    public static File generateAgentJar(Class agent, Class... resources) throws IOException {
        File jarFile = File.createTempFile("agent", ".jar");
        jarFile.deleteOnExit();

        Manifest manifest = new Manifest();
        Attributes mainAttributes = manifest.getMainAttributes();
        // Create manifest stating that agent is allowed to transform classes
        mainAttributes.put(Name.MANIFEST_VERSION, "1.0");
        mainAttributes.put(new Name("Agent-Class"), agent.getName());
        mainAttributes.put(new Name("Can-Retransform-Classes"), "true");
        mainAttributes.put(new Name("Can-Redefine-Classes"), "true");

        JarOutputStream jos = new JarOutputStream(new FileOutputStream(jarFile), manifest);

        add(agent, jos);

        for (Class clazz : resources) {
            try {
                add(clazz, jos);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }

        jos.close();
        return jarFile;
    }

    private static void add(Class clazz, JarOutputStream jos) throws IOException {
        String name = unqualify(clazz);
        System.out.println("Packed " + name);
        jos.putNextEntry(new JarEntry(name));
        jos.write(getBytesFromStream(clazz.getClassLoader().getResourceAsStream(name)));
        jos.closeEntry();
        for (Class child : clazz.getDeclaredClasses()) {
            add(child, jos);
        }
        for (int i = 1; ; i++) {
            try {
                Class child = Class.forName(clazz.getName() + "$" + i);
                System.out.println("\tDiscovered anonymous class $" + i);
                add(child, jos);
            } catch (Exception ex) {
                break;
            }
        }
    }

    private static String unqualify(Class clazz) {
        return clazz.getName().replace('.', '/') + ".class";
    }
}
