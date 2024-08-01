package de.tum.cit.ase.ares.api.jdi;

import com.sun.jdi.*;
import com.sun.jdi.connect.AttachingConnector;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.event.*;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.MethodEntryRequest;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

public class JDIStackTraceMonitor {

    public static void main(String[] args) throws IOException, IllegalConnectorArgumentsException, InterruptedException, IncompatibleThreadStateException, TimeoutException, ExecutionException {
        String host = "localhost";
        String port = "5005"; // Replace with the port your application is listening on

        // Start student application via ProcessBuilder
        ProcessBuilder processBuilder = new ProcessBuilder("java", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:5005",  "C:\\Users\\sarps\\IdeaProjects\\Ares2\\src\\main\\java\\de\\tum\\cit\\ase\\ares\\api\\jdi\\StudentExample.java");
        processBuilder.redirectErrorStream(true);
        Process process = processBuilder.start();

        // Create VirtualMachine
        VirtualMachine vm = attachToVMWithTimeout(host, port, 15, TimeUnit.SECONDS);
        EventRequestManager erm = vm.eventRequestManager();

        // Create method entry and exit requests
        MethodEntryRequest methodEntryRequest = erm.createMethodEntryRequest();
        methodEntryRequest.addClassFilter("sun.nio.fs.WindowsFileSystemProvider");
        methodEntryRequest.addThreadFilter(findMainThread(vm));
        methodEntryRequest.setSuspendPolicy(EventRequest.SUSPEND_EVENT_THREAD);
        methodEntryRequest.enable();

        EventQueue queue = vm.eventQueue();
        while (true) {
            EventSet eventSet = queue.remove();
            EventIterator eventIterator = eventSet.eventIterator();
            while (eventIterator.hasNext()) {
                Event event = eventIterator.nextEvent();
                if (event instanceof MethodEntryEvent methodEntryEvent) {
                    List<String> locations = methodEntryEvent.thread().frames().stream().map(frame -> frame.location().toString())
                            .filter(location -> location.contains("StudentExample"))
                            .toList();
                    if (!locations.isEmpty()) {
                        throw new SecurityException("Access to sun.nio.fs.UnixFileSystemProvider is not allowed" + locations);
                    }
                } else if (event instanceof VMDeathEvent || event instanceof VMDisconnectEvent) {
                    return;
                }
            }
            eventSet.resume();
        }
    }

    private static ThreadReference findMainThread(VirtualMachine vm) {
        for (ThreadReference thread : vm.allThreads()) {
            if (thread.name().equals("main")) {
                return thread;
            }
        }
        throw new IllegalStateException("Main thread not found");
    }

    private static VirtualMachine attachToVMWithTimeout(String host, String port, long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        Future<VirtualMachine> future = executor.schedule(() -> attachToVM(host, port), timeout, unit);

        try {
            return future.get();
        } finally {
            executor.shutdown();
        }
    }

    private static VirtualMachine attachToVM(String host, String port) throws IOException, IllegalConnectorArgumentsException {
        AttachingConnector connector = null;
        for (AttachingConnector ac : Bootstrap.virtualMachineManager().attachingConnectors()) {
            if (ac.name().equals("com.sun.jdi.SocketAttach")) {
                connector = ac;
                break;
            }
        }
        if (connector == null) {
            throw new IllegalStateException("Cannot find SocketAttach connector");
        }

        Map<String, Connector.Argument> arguments = connector.defaultArguments();
        arguments.get("hostname").setValue(host);
        arguments.get("port").setValue(port);

        return connector.attach(arguments);
    }
}
