# 亮点

在线oJ，核心功能：
1、能够管理题目（保存很多的题目信息：题干+测试用例）
2、题目列表页：能够展示题目列表
3、题目详情页：能够展示某个题的详情信息+代码编辑框
4、提交并运行题目：详情页中有一个提交按钮，点击按钮网页就会把当前的代码给提交到服务器上，服务器就会执行代码，并且就会给一些是否通过用例的结果
5、查看运行结果：有另外一个结果页面，能展示上次提交的是否通过，以及错误的用例信息，这里也会提供一些提交记录就更好了

# 问答
- 项目的模块划分，每个模块都是干什么的

     - 编译运行模块: 通过多进程编程的方式, 调用 javac 和 java 命令, 针对代码进行编译运行. 
     - 题目管理模块: 基于 mysql , 对题库进行管理. 
     - 服务器模块: 基于 Servlet, 实现一组 HTTP 服务接口. 
     - 客户端模块: 基于 HTML/CSS/JS , 实现一组页面, 和用户交互. 

- 为什么使用多进程编程调用 javac/java? 是否可以使用多线程?

- **服务器进程和 javac/java 进程之间是如何通信的?**
      

  ```
  - 服务器把用户提交的请求中的代码, 写到一个 .java 文件中. 
  - 子进程 javac 针对这个 .java 文件进行编译. 得到 .class 文件
  - 如果编译出错, 则把错误信息写入到一个 compileError.txt 文本文件中. 
  - 子进程 java 针对 .class 文件进行执行. 把标准输出和标准错误写到 stdout.txt 和 stderr.txt 两个文本文件中. 
  - 服务器进程再读取 stdout.txt 和 stderr.txt 获取到运行结果. 
  ```

- 数据库是怎么设计的
- 设计的前后端交互的 API 有哪些?
- **如何避免用户提交了恶意代码(比如代码中执行了 rm -rf /)**

# 1.  从数据库中读取题目信息

1.1 创建题目数据库（sql）

1. 标题
2. 难度
3. 介绍
4. 给定的代码
5. 测试用户答案的代码
6. 题目序号（自增主键）

```sql

create database if not exists oj_database;

use oj_database;

drop table if exists oj_table;
create table oj_table (
    id int primary key auto_increment,
    title varchar(50),
    level varchar(50),
    description varchar(4096),
    templateCode varchar(4096),
    testCode varchar(4096)
);
```

1.2 创建题目 类（dao包下的problem）

```java
package dao;

public class Problem {
    private int id;
    private String title;
    private String level;
    private String description;
    private String templateCode;
    private String testCode;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    @Override
    public String toString() {
        return "Problem{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", level='" + level + '\'' +
                ", description='" + description + '\'' +
                ", templateCode='" + templateCode + '\'' +
                ", testCode='" + testCode + '\'' +
                '}';
    }
}
```
1.3 实现数据库连接类 （common包下的 DBUtil）

1. 通过datasource.getConnection();连接数据库

2. 再次之前需要知道

1）数据库的地址

2）用户名

3）密码

3. 由于可能多线程调用连接数据库类，所以用多线程的懒汉模式实现datasource赋值

4. 实现getconnection函数

5. 关闭资源函数（connection，statement，resultSet）

#### DBUtil
```java
package common;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;
import com.sun.xml.internal.ws.server.ServerRtException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DBUtil{
    // 需要封装和数据库之间的连接操作.
    private static final String URL = "jdbc:mysql://127.0.0.1:3306/oj_database?characterEncoding=utf8&useSSL=false";
    private static final String USERNAME = "root";
    // private static final String PASSWORD = "2222";
    private static final String PASSWORD = "123456";

    private static volatile DataSource dataSource = null;




    private static DataSource getDataSource(){
        if(dataSource == null){
            synchronized (DBUtil.class){
                if(dataSource == null){
                    MysqlDataSource mysqlDataSource = new MysqlDataSource();
                    mysqlDataSource.setURL(URL);
                    mysqlDataSource.setUser(USERNAME);
                    mysqlDataSource.setPassword(PASSWORD);
                    dataSource = mysqlDataSource;
                }
            }
        }
        return dataSource;
    }

    public static Connection getConnection() throws SQLException {
        return getDataSource().getConnection();
    }

    public static void close(Connection connection, PreparedStatement statement, ResultSet resultSet) {
        if (resultSet != null) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (statement != null) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
```
1.4 实现对数据库的题目 进行增删查改（dao包下的 problemDAO）

增删查改的逻辑：

1. 参数有不同的参数

2. 建立connect

3. 预备statement存储操作命令

4. 连接connect

5. 处理命令

6. 执行命令

7. 判断执行的命令是否成功

8. 最后关闭资源

#### ProblemDAO
```java
package dao;

import common.DBUtil;
import javafx.scene.layout.Priority;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// 通过这个类封装了针对 Problem 的增删改查.
// 1. 新增题目
// 2. 删除题目
// 3. 查询题目列表
// 4. 查询题目详情
public class ProblemDAO {
    public void insert(Problem problem) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            // 1. 和数据库建立连接
            connection = DBUtil.getConnection();
            // 2. 构造 SQL 语句
            String sql = "insert into oj_table values(null, ?, ?, ?, ?, ?)";
            statement = connection.prepareStatement(sql);
            statement.setString(1, problem.getTitle());
            statement.setString(2, problem.getLevel());
            statement.setString(3, problem.getDescription());
            statement.setString(4, problem.getTemplateCode());
            statement.setString(5, problem.getTestCode());
            // 3. 执行 SQL
            int ret = statement.executeUpdate();
            if (ret != 1) {
                System.out.println("题目新增失败!");
            } else {
                System.out.println("题目新增成功!");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }

    public void delete(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        try {
            // 1. 和数据库建立连接
            connection = DBUtil.getConnection();
            // 2. 拼装 SQL 语句
            String sql = "delete from oj_table where id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            // 3. 执行 SQL
            int ret = statement.executeUpdate();
            if (ret != 1) {
                System.out.println("删除题目失败!");
            } else {
                System.out.println("删除题目成功!");
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, null);
        }
    }

    // 这个操作是把当前题目列表中的所有题都查出来了
    // 万一数据库中的题目特别多, 咋办? 只要实现 "分页查询" 即可. 后台实现分页查询, 非常容易.
    // 前端传过来一个当前的 "页码" , 根据页码算一下, 依据 sql limit offset 语句, 要算出来 offset 是 几
    // 但是前端这里实现一个分页器稍微麻烦一些(比后端要麻烦很多). 此处暂时不考虑分页功能.
    public List<Problem> selectAll() {
        List<Problem> problems = new ArrayList<>();
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            // 1. 和数据库建立连接
            connection = DBUtil.getConnection();
            // 2. 拼装 SQL
            String sql = "select id, title, level from oj_table";
            statement = connection.prepareStatement(sql);
            // 3. 执行 SQL
            resultSet = statement.executeQuery();
            // 4. 遍历 resultSet
            while (resultSet.next()) {
                // 每一行都是一个 Problem 对象
                Problem problem = new Problem();
                problem.setId(resultSet.getInt("id"));
                problem.setTitle(resultSet.getString("title"));
                problem.setLevel(resultSet.getString("level"));
                problems.add(problem);
            }
            return problems;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }

    public Problem selectOne(int id) {
        Connection connection = null;
        PreparedStatement statement = null;
        ResultSet resultSet = null;
        try {
            // 1. 和数据库建立连接
            connection = DBUtil.getConnection();
            // 2. 拼接 SQL 语句
            String sql = "select * from oj_table where id = ?";
            statement = connection.prepareStatement(sql);
            statement.setInt(1, id);
            // 3. 执行 SQL
            resultSet = statement.executeQuery();
            // 4. 遍历查询结果. (由于 id 是主键, 按照 id 查找的结果一定是唯一的)
            if (resultSet.next()) {
                Problem problem = new Problem();
                problem.setId(resultSet.getInt("id"));
                problem.setTitle(resultSet.getString("title"));
                problem.setLevel(resultSet.getString("level"));
                problem.setDescription(resultSet.getString("description"));
                problem.setTemplateCode(resultSet.getString("templateCode"));
                problem.setTestCode(resultSet.getString("testCode"));
                return problem;
            }
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        } finally {
            DBUtil.close(connection, statement, resultSet);
        }
        return null;
    }

    private static void testInsert() {
        ProblemDAO problemDAO = new ProblemDAO();
        Problem problem = new Problem();
        // problem.setId();
        problem.setTitle("两数之和");
        problem.setLevel("简单");
        problem.setDescription("给定一个整数数组 nums 和一个整数目标值 target，请你在该数组中找出 和为目标值 target  的那 两个 整数，并返回它们的数组下标。\n" +
                "\n" +
                "你可以假设每种输入只会对应一个答案。但是，数组中同一个元素在答案里不能重复出现。\n" +
                "\n" +
                "你可以按任意顺序返回答案。\n" +
                "\n" +
                " \n" +
                "\n" +
                "示例 1：\n" +
                "\n" +
                "输入：nums = [2,7,11,15], target = 9\n" +
                "输出：[0,1]\n" +
                "解释：因为 nums[0] + nums[1] == 9 ，返回 [0, 1] 。\n" +
                "示例 2：\n" +
                "\n" +
                "输入：nums = [3,2,4], target = 6\n" +
                "输出：[1,2]\n" +
                "示例 3：\n" +
                "\n" +
                "输入：nums = [3,3], target = 6\n" +
                "输出：[0,1]\n" +
                " \n" +
                "\n" +
                "提示：\n" +
                "\n" +
                "2 <= nums.length <= 104\n" +
                "-109 <= nums[i] <= 109\n" +
                "-109 <= target <= 109\n" +
                "只会存在一个有效答案\n" +
                "进阶：你可以想出一个时间复杂度小于 O(n2) 的算法吗？\n" +
                "\n" +
                "来源：力扣（LeetCode）\n" +
                "链接：https://leetcode-cn.com/problems/two-sum\n" +
                "著作权归领扣网络所有。商业转载请联系官方授权，非商业转载请注明出处。");
        problem.setTemplateCode("class Solution {\n" +
                "    public int[] twoSum(int[] nums, int target) {\n" +
                "\n" +
                "    }\n" +
                "}");
        problem.setTestCode("    public static void main(String[] args) {\n" +
                "        Solution solution = new Solution();\n" +
                "        // testcase1\n" +
                "        int[] nums = {2,7,11,15};\n" +
                "        int target = 9;\n" +
                "        int[] result = solution.twoSum(nums, target);\n" +
                "        if (result.length == 2 && result[0] == 0 && result[1] == 1) {\n" +
                "            System.out.println(\"testcase1 OK\");\n" +
                "        } else {\n" +
                "            System.out.println(\"testcase1 failed!\");\n" +
                "        }\n" +
                "\n" +
                "        // testcase2\n" +
                "        int[] nums2 = {3,2,4};\n" +
                "        int target2 = 6;\n" +
                "        int[] result2 = solution.twoSum(nums2, target2);\n" +
                "        if (result2.length == 2 && result[0] == 1 && result[1] == 2) {\n" +
                "            System.out.println(\"testcase2 OK\");\n" +
                "        } else {\n" +
                "            System.out.println(\"testcase2 failed!\");\n" +
                "        }\n" +
                "    }\n");
        problemDAO.insert(problem);
        System.out.println("插入成功!");
    }

    private static void testSelectAll() {
        ProblemDAO problemDAO = new ProblemDAO();
        List<Problem> problems = problemDAO.selectAll();
        System.out.println(problems);
    }

    private static void testSelectOne() {
        ProblemDAO problemDAO = new ProblemDAO();
        Problem problem = problemDAO.selectOne(1);
        System.out.println(problem);
    }

    private static void testDelete() {
        ProblemDAO problemDAO = new ProblemDAO();
        problemDAO.delete(1);
    }

    public static void main(String[] args) {
        testInsert();
        // testSelectAll();
        // testSelectOne();
        // testDelete();
    }
}

```
# 2. 对用户提交的代码进行编译和运行（利用多进程）

### 2.1 创建一个接收用户提交代码的 类（complie包下面的 Question）
#### 1. Question包含String code信息
```java
package compile;

// 用这个类来表示一个 task 的输入内容
// 会包含要编译的代码
public class Question {
    private String code;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
```
### 2.2 进行编译和运行的 	类（compile包下 CommandUtil）

#### 1. run方法实现 执行cmd指令 并把结果输出到指定文件，并返回状态码
#### 2. `Process process = Runtime.getRuntime().exec(cmd);`创建进程
#### 3. 通过process.getInputStream();读取进程信息，FileOutputStream输出进程信息
#### 4. process.getErrorStream();FileOutputStream输出错误信息
#### 5. int exitCode = process.waitFor(); 返回子进程的状态码

```java
package compile;

import dao.Problem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class CommandUtil {
    // 1. 通过 Runtime 类得到 Runtime 实例, 执行 exec 方法
    // 2. 获取到标准输出, 并写入到指定文件中.
    // 3. 获取到标准错误, 并写入到指定文件中.
    // 4. 等待子进程结束, 拿到子进程的状态码, 并返回.
    public static int run(String cmd, String stdoutFile, String stderrFile) {
        try {
            // 1. 通过 Runtime 类得到 Runtime 实例, 执行 exec 方法
            Process process = Runtime.getRuntime().exec(cmd);
            // 2. 获取到标准输出, 并写入到指定文件中.
            if (stdoutFile != null) {
                InputStream stdoutFrom = process.getInputStream();
                FileOutputStream stdoutTo = new FileOutputStream(stdoutFile);
                while (true) {
                    int ch = stdoutFrom.read();
                    if (ch == -1) {
                        break;
                    }
                    stdoutTo.write(ch);
                }
                stdoutFrom.close();
                stdoutTo.close();
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
            // 4. 等待子进程结束, 拿到子进程的状态码, 并返回.
            int exitCode = process.waitFor();
            return exitCode;
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void main(String[] args) {
        CommandUtil.run("javac", "stdout.txt", "stderr.txt");
    }
}
```
### 2.3 实现编辑运行代码的 具体逻辑 （compile包下的 Task）

#### 1. 工作目录，编译的类名，代码文件名，标准输出，错误输出
#### 2. 实现编译运行函数（传入参数Question）
##### 2.1 创建目录
##### 2.2 判断代码合理性
##### 2.3 把用户代码 写入要编译运行的 文件中
##### 2.4 创建编译命令，进行编译，判断是否正确，否则返回错误值
##### 2.5 创建运行指令，进行运行，判断是否正确

```java
package compile;


import common.FileUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

// 每次的 "编译+运行" 这个过程, 就称为是一个 compile.Task
public class Task {
    // 通过一组常量来约定临时文件的名字.
    // 之前这里的名字都是静态常量. 但是现在要实现针对每个请求都有不同的临时目录, 就不能使用静态常量了
    // 这个表示所有临时文件所在的目录
    private String WORK_DIR = null;
    // 约定代码的类名
    private String CLASS = null;
    // 约定要编译的代码文件名.
    private String CODE = null;
    // 约定存放编译错误信息的文件名
    private String COMPILE_ERROR = null;
    // 约定存放运行时的标准输出的文件名
    private String STDOUT = null;
    // 约定存放运行时的标准错误的文件名
    private String STDERR = null;

    public Task() {
        // 在 Java 中使用 UUID 这个类就能生成一个 UUID 了
        WORK_DIR = "./tmp/" + UUID.randomUUID().toString() + "/";
        CLASS = "Solution";
        CODE = WORK_DIR + "Solution.java";
        COMPILE_ERROR = WORK_DIR + "compileError.txt";
        STDOUT = WORK_DIR + "stdout.txt";
        STDERR = WORK_DIR + "stderr.txt";
    }

    // 这个 compile.Task 类提供的核心方法, 就叫做 compileAndRun, 编译+运行 的意思.
    // 参数: 要编译运行的 java 源代码.
    // 返回值: 表示编译运行的结果. 编译出错/运行出错/运行正确.....
    public Answer compileAndRun(Question question) {
        Answer answer = new Answer();
        // 0. 准备好用来存放临时文件的目录
        File workDir = new File(WORK_DIR);
        if (!workDir.exists()) {
            // 创建多级目录.
            workDir.mkdirs();
        }
        // 进行安全性判定
        if (!checkCodeSafe(question.getCode())) {
            System.out.println("用户提交了不安全的代码!");
            answer.setError(3);
            answer.setReason("您提交的代码可能会危害到服务器, 禁止运行!");
            return answer;
        }
        // 1. 把 question 中的 code 写入到一个 Solution.java 文件中.
        FileUtil.writeFile(CODE, question.getCode());
        // 2. 创建子进程, 调用 javac 进行编译. 注意! 编译的时候, 需要有一个 .java 文件.
        //       如果编译出错, javac 就会把错误信息给写入到 stderr 里. 就可以用一个专门的文件来保存. compileError.txt
        //    需要先把编译命令给构造出来.
        String compileCmd = String.format("javac -encoding utf8 %s -d %s", CODE, WORK_DIR);
        System.out.println("编译命令: " + compileCmd);
        CommandUtil.run(compileCmd, null, COMPILE_ERROR);
        // 如果编译出错了, 错误信息就被记录到 COMPILE_ERROR 这个文件中了. 如果没有编译出错, 这个文件是空文件.
        String compileError = FileUtil.readFile(COMPILE_ERROR);
        if (!compileError.equals("")) {
            // 编译出错!
            // 直接返回 compile.Answer, 让 compile.Answer 里面记录编译的错误信息.
            System.out.println("编译出错!");
            answer.setError(1);
            answer.setReason(compileError);
            return answer;
        }
        // 编译正确! 继续往下执行运行的逻辑
        // 3. 创建子进程, 调用 java 命令并执行
        //       运行程序的时候, 也会把 java 子进程的标准输出和标准错误获取到. stdout.txt, stderr.txt
        String runCmd = String.format("java -classpath %s %s", WORK_DIR, CLASS);
        System.out.println("运行命令: " + runCmd);
        CommandUtil.run(runCmd, STDOUT, STDERR);
        String runError = FileUtil.readFile(STDERR);
        if (!runError.equals("")) {
            System.out.println("运行出错!");
            answer.setError(2);
            answer.setReason(runError);
            return answer;
        }
        // 4. 父进程获取到刚才的编译执行的结果, 并打包成 compile.Answer 对象
        //       编译执行的结果, 就通过刚才约定的这几个文件来进行获取即可.
        answer.setError(0);
        answer.setStdout(FileUtil.readFile(STDOUT));
        return answer;
    }

    private boolean checkCodeSafe(String code) {
        List<String> blackList = new ArrayList<>();
        // 防止提交的代码运行恶意程序
        blackList.add("Runtime");
        blackList.add("exec");
        // 禁止提交的代码读写文件
        blackList.add("java.io");
        // 禁止提交的代码访问网络
        blackList.add("java.net");

        for (String target : blackList) {
            int pos = code.indexOf(target);
            if (pos >= 0) {
                // 找到任意的恶意代码特征, 返回 false 表示不安全
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Task task = new Task();
        Question question = new Question();
        question.setCode("public class Solution {\n" +
                "    public static void main(String[] args) {\n" +
                "        System.out.println(\"hello world\");\n" +
                "    }\n" +
                "}\n");
        Answer answer = task.compileAndRun(question);
        System.out.println(answer);
    }
}
```
### 2.4 由于编译运行需要返回答案，所以创建Answer类
#### 1. error错误值（0正常，1编译错误，2运行错误）
#### 2. reason（错误原因）
#### 3. stdout（标准输出）
#### 4. stderr（错误输出）
```java
package compile;

// 表示一个 compile.Task 的执行结果
public class Answer {
    // 错误码. 约定 error 为 0 表示编译运行都 ok, 为 1 表示编译出错, 为 2 表示运行出错(抛异常).
    private int error;
    // 出错的提示信息. 如果 error 为 1, 编译出错了, reason 中就放编译的错误信息, 如果 error 为 2, 运行异常了, reason 就放异常信息
    private String reason;
    // 运行程序得到的标准输出的结果.
    private String stdout;
    // 运行程序得到的标准错误的结果.
    private String stderr;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getStdout() {
        return stdout;
    }

    public void setStdout(String stdout) {
        this.stdout = stdout;
    }

    public String getStderr() {
        return stderr;
    }

    public void setStderr(String stderr) {
        this.stderr = stderr;
    }

    @Override
    public String toString() {
        return "compile.Answer{" +
                "error=" + error +
                ", reason='" + reason + '\'' +
                ", stdout='" + stdout + '\'' +
                ", stderr='" + stderr + '\'' +
                '}';
    }

}
```
# 3. 前后端交互

### 1. 客户端向服务器请求所有题目 或者 单个题目

通过servlet进行前后端交互

前端：通过problem的URL地址访问（如果没有其它参数，则是查询所有题目，如果有id参数，就是查询具体题目）
后端：返回题目的具体详情
![在这里插入图片描述](C:\application\gitee\learning-notes\项目笔记\在线oj项目笔记.assets\ed6ba33e1f7c834d18afe1af29dec737.png)![在这里插入图片描述](C:\application\gitee\learning-notes\项目笔记\在线oj项目笔记.assets\148088c9d222c3be27608a44131d757e.png)
#### 前端
###### 获取所有题目

```html
        <script>
            // 在页面加载的时候, 尝试从服务器获取题目列表. 通过 ajax 的方式来进行获取
            function getProblems() {
                // 1. 先通过 ajax 从服务器获取到题目列表. 
                $.ajax({
                    url: "problem",
                    type: "GET",
                    success: function(data, status) {
                        // data 是响应的 body, status 是响应的状态码
                        // 2. 把得到的响应数据给构造成 HTML 片段
                        makeProblemTable(data);
                    }
                })
            }

            // 通过这个函数来把数据转换成 HTML 页面片段
            function makeProblemTable(data) {
                let problemTable = document.querySelector("#problemTable");
                for (let problem of data) {
                    let tr = document.createElement("tr");

                    let tdId = document.createElement("td");
                    tdId.innerHTML = problem.id;
                    tr.appendChild(tdId);

                    let tdTitle = document.createElement("td");
                    let a = document.createElement("a");
                    a.innerHTML = problem.title;
                    a.href = 'problemDetail.html?id=' + problem.id;
                    a.target = '_blank';
                    tdTitle.appendChild(a);
                    tr.appendChild(tdTitle);

                    let tdLevel = document.createElement("td");
                    tdLevel.innerHTML = problem.level;
                    tr.appendChild(tdLevel);

                    problemTable.appendChild(tr);
                }
            }

            getProblems();
        </script>
```

###### 获取一个题目

```html
<script>
            // 通过 ajax 从服务器获取到题目的详情
            function getProblem() {
                // 1. 通过 ajax 给服务器发送一个请求
                $.ajax({
                    url: "problem" + location.search,
                    type: "GET",
                    success: function (data, status) {
                        makeProblemDetail(data);
                    }
                })
            }

            function makeProblemDetail(problem) {
                // 1. 获取到 problemDesc, 把题目详情填写进去
                let problemDesc = document.querySelector("#problemDesc");

                let h3 = document.createElement("h3");
                h3.innerHTML = problem.id + "." + problem.title + "_" + problem.level
                problemDesc.appendChild(h3);

                let pre = document.createElement("pre");
                let p = document.createElement("p");
                p.innerHTML = problem.description;
                pre.appendChild(p);
                problemDesc.appendChild(pre);

                // 2. 把代码的模板填写到编辑框中. 
                // let codeEditor = document.querySelector("#codeEditor");
                // codeEditor.innerHTML = problem.templateCode;
                editor.setValue(problem.templateCode)
</script>
```

#### 后端
##### 1. objectMapper把要返回的题目列表的String 转成body类型
##### 2. 返回值的状态值设定和类型设定
##### 3. 创建数据库的题目操作类，进行查询

```java
package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import dao.Problem;
import dao.ProblemDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/problem")
public class ProblemServlet extends HttpServlet {
    //ObjectMapper类(com.fasterxml.jackson.databind.ObjectMapper)是Jackson的主要类，它可以帮助我们快速的进行各个类型和Json类型的相互转换。
    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        //设置返回的状态码 200表示成功
        resp.setStatus(200);
        //返回的数据类型
        resp.setContentType("application/json;charset=utf8");

        ProblemDAO problemDAO = new ProblemDAO();
        // 尝试获取 id 参数. 如果能获取到, 说明是获取题目详情; 如果不能获取到, 说明是获取题目列表.
        String idString = req.getParameter("id");
        if (idString == null || "".equals(idString)) {
            // 没有获取到 id 字段. 查询题目列表
            List<Problem> problems = problemDAO.selectAll();
            String respString = objectMapper.writeValueAsString(problems);
            resp.getWriter().write(respString);
        } else {
            // 获取到了题目 id. 查询题目详情
            Problem problem = problemDAO.selectOne(Integer.parseInt(idString));
            String respString = objectMapper.writeValueAsString(problem);
            resp.getWriter().write(respString);
        }

    }
}
```
### 2. 后端读取前端提交的代码，进行编译运行，返回结果
![在这里插入图片描述](C:\application\gitee\learning-notes\项目笔记\在线oj项目笔记.assets\d2f755b535f8fdc064bea8ac18d6aab0.png)
#### 前端提交代码

```html
<script>
                // 3. 给提交按钮注册一个点击事件
                let submitButton = document.querySelector("#submitButton");
                submitButton.onclick = function () {
                    // 点击这个按钮, 就要进行提交. (把编辑框的内容给提交到服务器上)
                    let body = {
                        id: problem.id,
                        // code: codeEditor.value,
                        code: editor.getValue(),
                    };
                    $.ajax({
                        type: "POST",
                        url: "compile",
                        data: JSON.stringify(body),
                        success: function (data, status) {
                            let problemResult = document.querySelector("#problemResult");
                            if (data.error == 0) {
                                // 编译运行没有问题, 把 stdout 显示到页面中
                                problemResult.innerHTML = data.stdout;
                            } else {
                                // 编译运行没有问题, 把 reason 显示到页面中
                                problemResult.innerHTML = data.reason;
                            }
                        }
                    });
                }
            }

</script>
```

#### 后端处理
##### 1. （首先清楚前端传过来的是什么——编译的代码和题目序号）把前端传来的body读取成string，然后按着数据类型转成相应的类
##### 2. 创建数据库操作函数，查询要编译的题目
##### 3. 处理代码，进行编译和运行
```java
package api;

import com.fasterxml.jackson.databind.ObjectMapper;
import common.CodeInValidException;
import common.ProblemNotFoundException;
import compile.Answer;
import compile.Question;
import compile.Task;
import dao.Problem;
import dao.ProblemDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;

@WebServlet("/compile")
public class CompileServlet extends HttpServlet {
    static class CompileRequest {
        public int id;
        public String code;
    }

    static class CompileResponse {
        // 约定 error 为 0 表示编译运行 ok, error 为 1 表示编译出错, error 为 2 表示运行异常(用户提交的代码异常了), 3 表示其他错误
        public int error;
        public String reason;
        public String stdout;
    }

    private ObjectMapper objectMapper = new ObjectMapper();


//    {
//        "id": 2,
//        "code": "class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        int[] a = {0, 1};\n        return a;\n    }\n}    "
//    }
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws 35812ServletException, IOException {
        // 临时加一下这个代码, 来获取到 SmartTomcat 的工作目录
        System.out.println("用户的当前工作目录: "+System.getProperty("user.dir"));

        CompileRequest compileRequest = null;
        CompileResponse compileResponse = new CompileResponse();
        try {
            resp.setStatus(200);
            resp.setContentType("application/json;charset=utf8");
            // 1. 先读取请求的正文. 别按照 JSON 格式进行解析
            String body = readBody(req);
            compileRequest = objectMapper.readValue(body, CompileRequest.class);
            // 2. 根据 id 从数据库中查找到题目的详情 => 得到测试用例代码
            ProblemDAO problemDAO = new ProblemDAO();
            Problem problem = problemDAO.selectOne(compileRequest.id);
            if (problem == null) {
                // 为了统一处理错误, 在这个地方抛出一个异常.
                throw new ProblemNotFoundException();
            }
            // testCode 是测试用例的代码
            String testCode = problem.getTestCode();
            // requestCode 是用户提交的代码
            String requestCode = compileRequest.code;
            // 3. 把用户提交的代码和测试用例代码, 给拼接成一个完整的代码.
            String finalCode = mergeCode(requestCode, testCode);
            if (finalCode == null) {
                throw new CodeInValidException();
            }
            // System.out.println(finalCode);
            // 4. 创建一个 Task 实例, 调用里面的 compileAndRun 来进行编译运行.
            Task task = new Task();
            Question question = new Question();
            question.setCode(finalCode);
            Answer answer = task.compileAndRun(question);
            // 5. 根据 Task 运行的结果, 包装成一个 HTTP 响应
            compileResponse.error = answer.getError();
            compileResponse.reason = answer.getReason();
            compileResponse.stdout = answer.getStdout();
        } catch (ProblemNotFoundException e) {
            // 处理题目没有找到的异常
            compileResponse.error = 3;
            compileResponse.reason = "没有找到指定的题目! id=" + compileRequest.id;
        } catch (CodeInValidException e) {
            compileResponse.error = 3;
            compileResponse.reason = "提交的代码不符合要求!";
        } finally {
            String respString = objectMapper.writeValueAsString(compileResponse);
            resp.getWriter().write(respString);
        }
    }

    private static String mergeCode(String requestCode, String testCode) {
        // 1. 查找 requestCode 中的最后一个 }
        int pos = requestCode.lastIndexOf("}");
        if (pos == -1) {
            // 说明提交的代码完全没有 } , 显然是非法的代码.
            return null;
        }
        // 2. 根据这个位置进行字符串截取
        String subStr = requestCode.substring(0, pos);
        // 3. 进行拼接
        return subStr + testCode + "\n}";
    }

    private static String readBody(HttpServletRequest req) throws UnsupportedEncodingException {
        // 1. 先根据 请求头 里面的 ContentLength 获取到 body 的长度(单位是字节)
        int contentLength = req.getContentLength();
        // 2. 按照这个长度准备好一个 byte[] .
        byte[] buffer = new byte[contentLength];
        // 3. 通过 req 里面的 getInputStream 方法, 获取到 body 的流对象.
        try (InputStream inputStream = req.getInputStream()) {
            // 4. 基于这个流对象, 读取内容, 然后把内容放到 byte[] 数组中即可.
            inputStream.read(buffer);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // 5. 把这个 byte[] 的内容构造成一个 String
        return new String(buffer, "UTF8");
    }
}

```
## 4. 前端页面
根据上述逻辑
自己设计即可