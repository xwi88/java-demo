package com.im.utils;

import sun.jvmstat.monitor.MonitorException;
import sun.jvmstat.monitor.MonitoredHost;
import sun.jvmstat.monitor.MonitoredVm;
import sun.jvmstat.monitor.MonitoredVmUtil;
import sun.jvmstat.monitor.VmIdentifier;

import java.lang.management.ManagementFactory;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

public class ProcessUtil {
    public static int getProcess() {
        // get name representing the running Java virtual machine.
        // port@hostname
        String name = ManagementFactory.getRuntimeMXBean().getName();
        // get pid
        String pid = name.split("@")[0];
        return Integer.parseInt(pid);
    }

    // FIXME
    public static int getProcess(Class<?> cls) {
        if (cls == null) {
            return -1;
        }

        // monitor host
        MonitoredHost local = null;
        try {
            local = MonitoredHost.getMonitoredHost("localhost");
            // active vm sets
            Set<?> vmList = new HashSet<Object>(local.activeVms());
            // foreach process
            for (Object process : vmList) {
                MonitoredVm vm = local.getMonitoredVm(new VmIdentifier("//" + process));
                // 获取类名
                String processName = MonitoredVmUtil.mainClass(vm, true);
                if (cls.getName().equals(processName)) {
                    return (Integer) process;
                }
            }
        } catch (MonitorException | URISyntaxException e) {
            e.printStackTrace();
            return -1;
        }
        return -1;
    }
}

