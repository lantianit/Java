package com.example.myblog.并发;

import java.io.IOException;

public class E01_ProcessExample {
    public static void main(String[] args) {
        try {
            // 创建一个 ProcessBuilder 对象，指定要执行的命令
            ProcessBuilder processBuilder = new ProcessBuilder("notepad.exe");
            // 启动进程
            Process process = processBuilder.start();
            // 获取进程的退出状态码
            int exitCode = process.waitFor();
            System.out.println("Process exited with code " + exitCode);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}