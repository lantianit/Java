package study;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class Study {
    public static int run(String cmd, String stdoutFile, String stderrFile) {
        try {
            Process process = Runtime.getRuntime().exec(cmd);
            if (stdoutFile != null) {
                InputStream stdoutFrom = process.getInputStream();
                FileOutputStream stdout = new FileOutputStream(stdoutFile);
                while (true) {
                    int ch = stdoutFrom.read();
                    if (ch == -1) {
                        break;
                    }
                    stdout.write(ch);
                }
                stdoutFrom.close();
                stdout.close();
            }
            // 3. 获取到标准错误, 并写入到指定文件中.
            if (stderrFile != null) {
                InputStream stderrFrom = process.getErrorStream();
                FileOutputStream stderrTo = new FileOutputStream(stderrFile);
                while (true) {
                    int ch = stderrFrom.read();
                    if (ch == -1) {
                        break;
                    }
                    stderrTo.write(ch);
                }
                stderrFrom.close();
                stderrTo.close();
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
