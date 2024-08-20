package com.example.mq.study;

import com.example.mq.common.BinaryTool;
import com.example.mq.mqserver.core.MSGQueue;
import com.example.mq.mqserver.core.Message;
import com.sun.tools.jdeprscan.scan.Scan;

import java.io.*;
import java.util.Scanner;

public class Study {
    
    static public class Stat {
        public int totalCount;
        public int validCount;
    }
    
    public void init() {}
    
    public String getQueueDir(String queueName) {
        return "./data/" + queueName;
    }
    
    public String getQueueDataPath(String queueName) {
        return getQueueDir(queueName) + "/queue_data.txt";
    }
    
    public String getQueueStatPath(String queueName) {
        return getQueueDir(queueName) + "/queue_stat.txt";
    }
    
    private Stat readStat(String queueName) {
        Stat stat = new Stat();
        try (InputStream inputStream = new FileInputStream(getQueueDataPath(queueName))) {
            Scanner scanner = new Scanner(inputStream);
            stat.totalCount = scanner.nextInt();
            stat.validCount = scanner.nextInt();
            return stat;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    private void writeStat(String queueName, Stat stat) {
        try (OutputStream outputStream = new FileOutputStream(getQueueDataPath(queueName))) {
            PrintWriter printWriter = new PrintWriter(outputStream);
            printWriter.write(stat.totalCount + "\t" + stat.validCount);
            printWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    public void createQueueFile(String queueName) {
        File baseDir = new File(getQueueDir(queueName));
        if (!baseDir.exists()) {
            boolean ok = baseDir.mkdirs();
            if (!ok) {
                
            }
        }
        File queueDataFile = new File(getQueueDataPath(queueName));
        if (!queueDataFile.exists()) {
            
        }
        Stat stat = new Stat();
        stat.validCount = 0;
        stat.totalCount = 0;
        writeStat(queueName, stat);
    }

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

    public void sendMessage(MSGQueue queue, Message message) throws IOException {
        if (!checkFilesExits(queue.getName())) {
            
        }
        byte[] messageBinary = BinaryTool.toBytes(message);
        synchronized (queue) {
            File queueDataFile = new File(getQueueDataPath(queue.getName()));
            byte[] messageBinary = BinaryTool.toBytes(message);
            synchronized (queue) {
                File queueDataFile = new File(getQueueDataPath(queue.getName()));
                message.setOffsetEnd();
            }
        }
    }

    public boolean checkFilesExits(String queueName) {
        // 判定队列的数据文件和统计文件是否都存在!!
        File queueDataFile = new File(getQueueDataPath(queueName));
        if (!queueDataFile.exists()) {
            return false;
        }
        File queueStatFile = new File(getQueueStatPath(queueName));
        if (!queueStatFile.exists()) {
            return false;
        }
        return true;
    }
    
}
