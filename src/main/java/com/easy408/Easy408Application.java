package com.easy408;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext;
import org.springframework.context.ApplicationContext;
import org.springframework.context.event.EventListener;

import java.awt.Desktop;
import java.net.ServerSocket;
import java.net.URI;

@SpringBootApplication
public class Easy408Application {
    
    // 默认端口设为 5408
    private static final int DEFAULT_PORT = 5408;

    public static void main(String[] args) {
        // 允许 Headless 模式
        System.setProperty("java.awt.headless", "false");

        // 端口冲突检测策略
        if (!isPortAvailable(DEFAULT_PORT)) {
            System.setProperty("server.port", "0");
        }

        SpringApplication.run(Easy408Application.class, args);
    }

    /**
     * 检测指定端口是否可用
     */
    private static boolean isPortAvailable(int port) {
        try (ServerSocket socket = new ServerSocket(port)) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @EventListener(ApplicationReadyEvent.class)
    public void onStartup(ApplicationReadyEvent event) {
        ApplicationContext context = event.getApplicationContext();
        int port = DEFAULT_PORT;
        if (context instanceof ServletWebServerApplicationContext) {
            port = ((ServletWebServerApplicationContext) context).getWebServer().getPort();
        }

        String localUrl = "http://localhost:" + port;

        System.out.println("\n----------------------------------------------------------");
        System.out.println("\tEasy 408 启动成功!");
        System.out.println("\t访问地址: \t" + localUrl);
        System.out.println("----------------------------------------------------------\n");

        // 自动打开浏览器逻辑
        openBrowser(localUrl);
    }

    private void openBrowser(String url) {
        String os = System.getProperty("os.name").toLowerCase();
        // Windows 尝试 App 模式
        if (os.contains("win")) {
            try {
                new ProcessBuilder("cmd", "/c", "start", "msedge", "--app=" + url, "--start-maximized").start();
                return;
            } catch (Exception e) {}
            try {
                new ProcessBuilder("cmd", "/c", "start", "chrome", "--app=" + url, "--start-maximized").start();
                return;
            } catch (Exception e) {}
        }
        // 通用打开
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(new URI(url));
            } else {
                Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
            }
        } catch (Exception e) {
            // ignore
        }
    }
}