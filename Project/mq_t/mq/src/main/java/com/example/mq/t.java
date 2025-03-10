package com.example.mq;

import com.example.mq.mqserver.datacenter.MessageFileManager;

import java.io.*;
import java.util.Scanner;

public class t {
    
    static public class Stat {
        public int totalCount;
        public int validCount;
    }
    
    public void init(){}
    
    private String getQueueDir(String queueName) {
        return "./data/" + queueName;
    }
    
    private String getQueueDataPath(String queueName) {
        return getQueueDir(queueName) + "/queue_data.txt";
    }
    
    private String getQueueStatPath(String queueName) {
        return getQueueDir(queueName) + "/queue_stat.txt";
    }
    
    private Stat readStat(String queueName) {
        Stat stat = new Stat();
        try (InputStream inputStream = new FileInputStream(getQueueStatPath(queueName))){
            Scanner scanner = new Scanner(inputStream);
            stat.totalCount = scanner.nextInt();
            stat.validCount = scanner.nextInt();
            return stat;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void writeStat(String queueName, Stat stat) {
        try (OutputStream outputStream = new FileOutputStream(getQueueStatPath(queueName))){
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.write(stat.totalCount + "\t" + stat.validCount);
            printWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // 创建队列对应的文件和目录
    public void createQueueFiles(String queueName) throws IOException {
        // 1. 先创建队列对应的消息目录
        File baseDir = new File(getQueueDir(queueName));
        if (!baseDir.exists()) {
            // 不存在, 就创建这个目录
            boolean ok = baseDir.mkdirs();
            if (!ok) {
                throw new IOException("创建目录失败! baseDir=" + baseDir.getAbsolutePath());
            }
        }
        // 2. 创建队列数据文件
        File queueDataFile = new File(getQueueDataPath(queueName));
        if (!queueDataFile.exists()) {
            boolean ok = queueDataFile.createNewFile();
            if (!ok) {
                throw new IOException("创建文件失败! queueDataFile=" + queueDataFile.getAbsolutePath());
            }
        }
        // 3. 创建消息统计文件
        File queueStatFile = new File(getQueueStatPath(queueName));
        if (!queueStatFile.exists()) {
            boolean ok = queueStatFile.createNewFile();
            if (!ok) {
                throw new IOException("创建文件失败! queueStatFile=" + queueStatFile.getAbsolutePath());
            }
        }
        // 4. 给消息统计文件, 设定初始值. 0\t0
        MessageFileManager.Stat stat = new MessageFileManager.Stat();
        stat.totalCount = 0;
        stat.validCount = 0;
        writeStat(queueName, stat);
    }

    // 删除队列的目录和文件.
    // 队列也是可以被删除的. 当队列删除之后, 对应的消息文件啥的, 自然也要随之删除.
    public void destroyQueueFiles(String queueName) throws IOException {
        // 先删除里面的文件, 再删除目录.
        File queueDataFile = new File(getQueueDataPath(queueName));
        boolean ok1 = queueDataFile.delete();
        File queueStatFile = new File(getQueueStatPath(queueName));
        boolean ok2 = queueStatFile.delete();
        File baseDir = new File(getQueueDir(queueName));
        boolean ok3 = baseDir.delete();
        if (!ok1 || !ok2 || !ok3) {
            // 有任意一个删除失败, 都算整体删除失败.
            throw new IOException("删除队列目录和文件失败! baseDir=" + baseDir.getAbsolutePath());
        }
    }
    
    public boolean checkFileExits(String queueName) {
        File queueDataFile = new File(getQueueDataPath())
    }
    
    
}