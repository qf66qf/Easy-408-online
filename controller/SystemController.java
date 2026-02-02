package com.easy408.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
@CrossOrigin(origins = "*")
public class SystemController {

    @Autowired
    private Environment environment;

    @GetMapping("/connect-info")
    public Map<String, String> getConnectInfo() {
        Map<String, String> info = new HashMap<>();
        info.put("ip", getLanIp());
        info.put("port", environment.getProperty("local.server.port"));
        return info;
    }

    private String getLanIp() {
        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (iface.isLoopback() || !iface.isUp() || iface.isVirtual()) continue;
                // 排除 Docker, VMware 等虚拟网卡通常的命名 (可选，视情况而定)
                if (iface.getDisplayName().toLowerCase().contains("docker") || 
                    iface.getDisplayName().toLowerCase().contains("veth")) continue;

                Enumeration<InetAddress> addresses = iface.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    InetAddress addr = addresses.nextElement();
                    // 寻找 192.168.x.x, 10.x.x.x, 172.16-31.x.x
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        if (addr.isSiteLocalAddress()) {
                            return addr.getHostAddress();
                        }
                    }
                }
            }
        } catch (Exception e) {
            return "127.0.0.1";
        }
        return "127.0.0.1";
    }
}