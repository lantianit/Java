## 模块划分

![在这里插入图片描述](https://gitee.com/zdawdaw/img/raw/master/202408252317430.png)

## 数据存储：核心类的创建和存储

1. Exchange

2. MSGQueue

3. Binding

4. Message

### sqlite存储Exchange, MSGQueue, Binding

对于 Exchange, MSGQueue, Binding, 我们使⽤数据库进⾏持久化保存
此处我们使⽤的数据库是 SQLite, 是⼀个更轻量的数据库
SQLite 只是⼀个动态库(当然, 官⽅也提供了可执⾏程序 exe), 我们在 Java 中直接引⼊ SQLite 依赖, 即可直接使⽤, 不必安装其他的软件

#### 1、配置 sqlite★

引⼊ pom.xml 依赖

```xml
<dependency>
	<groupId>org.xerial</groupId>
	<artifactId>sqlite-jdbc</artifactId>
<version>3.41.0.1</version></dependency>
```
配置数据源 application.yml

```yml
spring:
	datasource:
	url: jdbc:sqlite:./data/meta.db
	username:
	password:
	driver-class-name: org.sqlite.JDBC
mybatis:
	mapper-locations:classpath:mapper/**Mapper.xml
```
此处我们约定, 把数据库⽂件放到 ./data/meta.db 中.
SQLite 只是把数据单纯的存储到⼀个⽂件中. ⾮常简单⽅便



### 文件存储消息

设计思路

#### 1. 为什么要用文件存储

>  消息类：①属性（messageId、routingKey、deliverMode）②正文 byte[] body

消息需要在硬盘上存储. 但是并不直接放到数据库中, ⽽是直接使⽤⽂件存储.
原因如下:

1. 对于消息的操作并不需要复杂的 增删改查 .
2. 对于⽂件的操作效率⽐数据库会⾼很多

>主流 MQ 的实现(包括 RabbitMQ), 都是把消息存储在⽂件中, ⽽不是数据库中.
>文件存储结构

我们给每个队列分配⼀个⽬录. ⽬录的名字为 data + 队列名. 形如 ./data/testQueue
该⽬录中包含两个固定名字的⽂件
==• queue_data.txt 消息数据⽂件, ⽤来保存消息内容.==
==• queue_stat.txt 消息统计⽂件, ⽤来保存消息统计信息.==

#### 2. queue_data.txt ⽂件格式 ★

使⽤⼆进制⽅式存储.
每个消息分成两个部分:
• 前四个字节, 表⽰ Message 对象的⻓度(字节数)
• 后⾯若⼲字节, 表⽰ Message 内容
• 消息和消息之间⾸尾相连
每个 Message 基于 Java 标准库的ObjectInputStream/ ObjectOutputStream 序列化.
![在这里插入图片描述](https://gitee.com/zdawdaw/img/raw/master/202408261131568.png)
Message 对象中的 offsetBeg 和 offsetEnd 正是⽤来描述每个消息体所在的位置

#### 3. queue_stat.txt ⽂件格式:★

使⽤⽂本⽅式存储.
⽂件中只包含⼀⾏, ⾥⾯包含两列(都是整数), 使⽤ \t 分割.
第⼀列表⽰当前总的消息数⽬. 第⼆列表⽰有效消息数⽬.
形如:

> 2000\t1500

#### 4. MessageFileManager

创建 mqserver.database.MessageFileManager

> 1. stat类 + 读写stat（InputStream-Scanner  OutputStreadm-PrintWriter）
> 2. 获取数据目录方法；创建队列数据目录文件方法，并初始化stat文件数据 0 0。以及删除文件目录方法
> 4. sendMessage，把消息写入队列中的方法：①拿到队列目录 ②上锁 ③序列化 ④message初始位置和起始位置 ⑤写入
> 5. 删除message：①上锁 ②找到队列 ③randomaccessfile 读取文件 ④设置无效 ⑤写回文件

## 整合数据存储操作

### 创建 DiskDataCenter

```java
    // 这个实例用来管理数据库中的数据
    private DataBaseManager dataBaseManager = new DataBaseManager();
    // 这个实例用来管理数据文件中的数据
    private MessageFileManager messageFileManager = new MessageFileManager();
```

## 内存 存储

### MemoryDataCenter

> 1.交换机 2.队列 3.绑定 4.消息 5.待确认消息 6.队列所对应的消息

```java
public class MemoryDataCenter {
    // key 是 exchangeName, value 是 Exchange 对象
    private ConcurrentHashMap<String, Exchange> exchangeMap = new ConcurrentHashMap<>();
    // key 是 queueName, value 是 MSGQueue 对象
    private ConcurrentHashMap<String, MSGQueue> queueMap = new ConcurrentHashMap<>();
    // 第一个 key 是 exchangeName, 第二个 key 是 queueName
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Binding>> bindingsMap = new ConcurrentHashMap<>();
    // key 是 messageId, value 是 Message 对象
    private ConcurrentHashMap<String, Message> messageMap = new ConcurrentHashMap<>();
    // key 是 queueName, value 是一个 Message 的链表
    private ConcurrentHashMap<String, LinkedList<Message>> queueMessageMap = new ConcurrentHashMap<>();
    // 第一个 key 是 queueName, 第二个 key 是 messageId
    private ConcurrentHashMap<String, ConcurrentHashMap<String, Message>> queueMessageWaitAckMap = new ConcurrentHashMap<>();
```

## VirtualHost虚拟机 

**虚拟机的作用：** 

1. 对数据CURD进行封装
2. 实现不同机器的隔离
3. 对业务逻辑进行完善

### Router

作用：1. 检查bindingKey是否合规 2. 检查routingKey是否合规 3. 判定该消息是否可以转发给这个绑定对应的队列

### Consumer回调方法

收到消息之后要处理消息时调用的方法.

```java
/*
 * 只是一个单纯的函数式接口(回调函数). 收到消息之后要处理消息时调用的方法.
 */
@FunctionalInterface
public interface Consumer {
    // Delivery 的意思是 "投递", 这个方法预期是在每次服务器收到消息之后, 来调用.
    // 通过这个方法把消息推送给对应的消费者.
    // (注意! 这里的方法名和参数, 也都是参考 RabbitMQ 展开的)
    void handleDelivery(String consumerTag, BasicProperties basicProperties, byte[] body) throws MqException, IOException;
}
```

### ConsumerEnv消费者

1、consumerTag 消费者身份标识 2、队列名称 queueName 3、是否自动应答 autoAck 4、回调函数 consumer

### ConsumerManager 消费管理

属性：

1. VirtualHost parent
2. ExecutorService workerPool线程池 执行回调函数
3. BlockingQueue\<String> tokenQueue存放有消息的队列名称
4. Thread scannerThread 扫描线程

方法：

1. 初始化：1、虚拟机赋值 2、设置扫描线程：一直去拿需要消费的队列，消费队列
2. notifyConsume：当有消息发送到了队列，则调用找个方法，把队列放到待消费的queue中
3. addConsumer：1、在内存找到要存入消息的队列 2、创建消费者 3、消费队列
4. consumeMessage：1、找到消费者 2、找到队列 3、调用回调方法

## 网络通信协议设计

### 1. 定义 Request / Response

>属性：1、类型 2、长度 3、正文

```java
/*
 * 表示一个网络通信中的请求对象. 按照自定义协议的格式来展开的
 */
public class Request {
    private int type;
    private int length;
    private byte[] payload;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}

```

```java
/*
 * 这个对象表示一个响应. 也是根据自定义应用层协议来的
 */
public class Response {
    private int type;
    private int length;
    private byte[] payload;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public byte[] getPayload() {
        return payload;
    }

    public void setPayload(byte[] payload) {
        this.payload = payload;
    }
}

```
### 2. 定义payload参数⽗类

> 属性：1.身份表示 2.通道的身份表示

构造⼀个类表⽰⽅法的参数, 作为 Request 的 payload.
不同的⽅法中, 参数形态各异, 但是有些信息是通⽤的, 使⽤⼀个⽗类表⽰出来. 具体每个⽅法的参数再通过继承的⽅式体现

```java
/*
 * 使用这个类表示方法的公共参数/辅助的字段.
 * 后续每个方法又会有一些不同的参数, 不同的参数再分别使用不同的子类来表示.
 */
public class BasicArguments implements Serializable {
    // 表示一次请求/响应 的身份标识. 可以把请求和响应对上.
    protected String rid;
    // 这次通信使用的 channel 的身份标识.
    protected String channelId;

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}

```
此处的 rid 和 channelId 都是基于 UUID 来⽣成的. rid ⽤来标识⼀个请求-响应. 这⼀点在请求响应⽐较多的时候⾮常重要

### 3. 定义返回值⽗类

```java
public class BasicReturns implements Serializable {
    // 用来标识唯一的请求和响应.
    protected String rid;
    // 用来标识一个 channel
    protected String channelId;
    // 表示当前这个远程调用方法的返回值
    protected boolean ok;

    public String getRid() {
        return rid;
    }

    public void setRid(String rid) {
        this.rid = rid;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }
}

```
### 4. 定义其他参数类

针对每个 VirtualHost 提供的⽅法, 都需要有⼀个类表⽰对应的参数

#### 1) ExchangeDeclareArguments

```java
package com.example.mq.common;

import com.example.mq.mqserver.core.ExchangeType;

import java.io.Serializable;
import java.util.Map;

public class ExchangeDeclareArguments extends BasicArguments implements Serializable {
    private String exchangeName;
    private ExchangeType exchangeType;
    private boolean durable;
    private boolean autoDelete;
    private Map<String, Object> arguments;

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}

```
⼀个创建交换机的请求, 形如:
• 可以把 ExchangeDeclareArguments 转成 byte[], 就得到了下列图⽚的结构.
• 按照 length ⻓度读取出 payload, 就可以把读到的⼆进制数据转换成ExchangeDeclareArguments 对象

#### 2) ExchangeDeleteArguments

```java
public class ExchangeDeleteArguments extends BasicArguments implements Serializable {
    private String exchangeName;

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }
}

```
#### 3) QueueDeclareArguments

```java
public class QueueDeclareArguments extends BasicArguments implements Serializable {
    private String queueName;
    private boolean durable;
    private boolean exclusive;
    private boolean autoDelete;
    private Map<String, Object> arguments;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public boolean isDurable() {
        return durable;
    }

    public void setDurable(boolean durable) {
        this.durable = durable;
    }

    public boolean isExclusive() {
        return exclusive;
    }

    public void setExclusive(boolean exclusive) {
        this.exclusive = exclusive;
    }

    public boolean isAutoDelete() {
        return autoDelete;
    }

    public void setAutoDelete(boolean autoDelete) {
        this.autoDelete = autoDelete;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public void setArguments(Map<String, Object> arguments) {
        this.arguments = arguments;
    }
}

```
#### 4) QueueDeleteArguments

```java
public class QueueDeleteArguments extends BasicArguments implements Serializable {
    private String queueName;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }
}

```
#### 5) QueueBindArguments

```java
public class QueueBindArguments extends BasicArguments implements Serializable {
    private String queueName;
    private String exchangeName;
    private String bindingKey;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getBindingKey() {
        return bindingKey;
    }

    public void setBindingKey(String bindingKey) {
        this.bindingKey = bindingKey;
    }
}

```
#### 6) QueueUnbindArguments

```java
public class QueueUnbindArguments extends BasicArguments implements Serializable {
    private String queueName;
    private String exchangeName;

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }
}

```
#### 7) BasicPublishArguments

```java
public class BasicPublishArguments extends BasicArguments implements Serializable {
    private String exchangeName;
    private String routingKey;
    private BasicProperties basicProperties;
    private byte[] body;

    public String getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(String exchangeName) {
        this.exchangeName = exchangeName;
    }

    public String getRoutingKey() {
        return routingKey;
    }

    public void setRoutingKey(String routingKey) {
        this.routingKey = routingKey;
    }

    public BasicProperties getBasicProperties() {
        return basicProperties;
    }

    public void setBasicProperties(BasicProperties basicProperties) {
        this.basicProperties = basicProperties;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}

```
#### 8) BasicConsumeArguments

```java
public class BasicConsumeArguments extends BasicArguments implements Serializable {
    private String consumerTag;
    private String queueName;
    private boolean autoAck;
    // 这个类对应的 basicConsume 方法中, 还有一个参数, 是回调函数. (如何来处理消息)
    // 这个回调函数, 是不能通过网络传输的.
    // 站在 broker server 这边, 针对消息的处理回调, 其实是统一的. (把消息返回给客户端)
    // 客户端这边收到消息之后, 再在客户端自己这边执行一个用户自定义的回调就行了.
    // 此时, 客户端也就不需要把自身的回调告诉给服务器了.
    // 这个类就不需要 consumer 成员了.


    public String getConsumerTag() {
        return consumerTag;
    }

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public boolean isAutoAck() {
        return autoAck;
    }

    public void setAutoAck(boolean autoAck) {
        this.autoAck = autoAck;
    }
}

```
#### 9) SubScribeReturns

```java
public class SubScribeReturns extends BasicReturns implements Serializable {
    private String consumerTag;
    private BasicProperties basicProperties;
    private byte[] body;

    public String getConsumerTag() {
        return consumerTag;
    }

    public void setConsumerTag(String consumerTag) {
        this.consumerTag = consumerTag;
    }

    public BasicProperties getBasicProperties() {
        return basicProperties;
    }

    public void setBasicProperties(BasicProperties basicProperties) {
        this.basicProperties = basicProperties;
    }

    public byte[] getBody() {
        return body;
    }

    public void setBody(byte[] body) {
        this.body = body;
    }
}

```
## 实现 BrokerServer

### 1. 创建 BrokerServer 类

> 属性：①ServerSocket ②虚拟机 ③会话哈希表 ④线程池处理多个请求 ⑤是否继续运行标记
>
> 方法：
>
> 1. 初始化：给serverSocket赋值
>
> 2. start方法：创建线程池、获取tcp、处理请求
>
> 3. close：标记false、线程池销毁、关闭socket
>
> 4. processConnection：1.readRequest(dataInputStream)读取请求【InputStream inputStream = clientSocket.getInputStream() DataInputStream dataInputStream = new DataInputStream(inputStream)】 2.处理请求
>
> 5. 处理请求：1.获取请求正文 2.根据请求type决定接下来的操作，调用virtualHost进行处理
>
>    > 请求类型：
>    >
>    > 1. 创建channel：sessions.put(basicArguments.getChannelId(), clientSocket);
>    >
>    > 2. 销毁channel：sessions.remove(basicArguments.getChannelId());
>    >
>    > 3. 创建交换机：ok = virtualHost.exchangeDeclare(arguments.getExchangeName(), arguments.getExchangeType(),        arguments.isDurable(), arguments.isAutoDelete(), arguments.getArguments());
>    >
>    > 4. 删除交换机：virtualHost.exchangeDelete(arguments.getExchangeName());
>    >
>    > 5. 创建队列：virtualHost.queueDeclare(arguments.getQueueName(), arguments.isDurable(),        arguments.isExclusive(), arguments.isAutoDelete(), arguments.getArguments());
>    >
>    > 6. 删除队列：virtualHost.queueDelete((arguments.getQueueName()));
>    >
>    > 7. 绑定：virtualHost.queueBind(arguments.getQueueName(), arguments.getExchangeName(), arguments.getBindingKey());
>    >
>    > 8. 删除绑定：virtualHost.basicPublish(arguments.getExchangeName(), arguments.getRoutingKey(),        arguments.getBasicProperties(), arguments.getBody());
>    >
>    > 9. 消费者订阅队列：参数 1.消费者tag 2.队列名称 3.是否自动应答 4.消费函数具体操作
>    >
>    >    消费函数具体操作：1.拿到socket 2.构造响应数据 3.返回消息给消费者
>    >
>    >    ```java
>    >    virtualHost.basicConsume(arguments.getConsumerTag(), arguments.getQueueName(), arguments.isAutoAck(),
>    >            new Consumer() {
>    >                // 这个回调函数要做的工作, 就是把服务器收到的消息可以直接推送回对应的消费者客户端
>    >                @Override
>    >                public void handleDelivery(String consumerTag, BasicProperties basicProperties, byte[] body) throws MqException, IOException {
>    >                    // 先知道当前这个收到的消息, 要发给哪个客户端.
>    >                    // 此处 consumerTag 其实是 channelId. 根据 channelId 去 sessions 中查询, 就可以得到对应的
>    >                    // socket 对象了, 从而可以往里面发送数据了
>    >                    // 1. 根据 channelId 找到 socket 对象
>    >                    Socket clientSocket = sessions.get(consumerTag);
>    >                    if (clientSocket == null || clientSocket.isClosed()) {
>    >                        throw new MqException("[BrokerServer] 订阅消息的客户端已经关闭!");
>    >                    }
>    >                    // 2. 构造响应数据
>    >                    SubScribeReturns subScribeReturns = new SubScribeReturns();
>    >                    subScribeReturns.setChannelId(consumerTag);
>    >                    subScribeReturns.setRid(""); // 由于这里只有响应, 没有请求, 不需要去对应. rid 暂时不需要.
>    >                    subScribeReturns.setOk(true);
>    >                    subScribeReturns.setConsumerTag(consumerTag);
>    >                    subScribeReturns.setBasicProperties(basicProperties);
>    >                    subScribeReturns.setBody(body);
>    >                    byte[] payload = BinaryTool.toBytes(subScribeReturns);
>    >                    Response response = new Response();
>    >                    // 0xc 表示服务器给消费者客户端推送的消息数据.
>    >                    response.setType(0xc);
>    >                    // response 的 payload 就是一个 SubScribeReturns
>    >                    response.setLength(payload.length);
>    >                    response.setPayload(payload);
>    >                    // 3. 把数据写回给客户端.
>    >                    //    注意! 此处的 dataOutputStream 这个对象不能 close !!!
>    >                    //    如果 把 dataOutputStream 关闭, 就会直接把 clientSocket 里的 outputStream 也关了.
>    >                    //    此时就无法继续往 socket 中写入后续数据了.
>    >                    DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
>    >                    writeResponse(dataOutputStream, response);
>    >                }
>    >            });
>    >    ```
>    >
>    >    10. 确认消息：BasicAckArguments arguments = (BasicAckArguments) basicArguments;ok = virtualHost.basicAck(arguments.getQueueName(), arguments.getMessageId());

• virtualHost 表⽰服务器持有的虚拟主机. 队列, 交换机, 绑定, 消息都是通过虚拟主机管理.
• sessions ⽤来管理所有的客⼾端的连接. 记录每个客⼾端的 socket.
• serverSocket 是服务器⾃⾝的 socket
• executorService 这个线程池⽤来处理响应.
• runnable 这个标志位⽤来控制服务器的运⾏停⽌.

```java

/*
 * 这个 BrokerServer 就是咱们 消息队列 本体服务器.
 * 本质上就是一个 TCP 的服务器.
 */
public class BrokerServer {
    private ServerSocket serverSocket = null;

    // 当前考虑一个 BrokerServer 上只有一个 虚拟主机
    private VirtualHost virtualHost = new VirtualHost("default");
    // 使用这个 哈希表 表示当前的所有会话(也就是说有哪些客户端正在和咱们的服务器进行通信)
    // 此处的 key 是 channelId, value 为对应的 Socket 对象
    private ConcurrentHashMap<String, Socket> sessions = new ConcurrentHashMap<String, Socket>();
    // 引入一个线程池, 来处理多个客户端的请求.
    private ExecutorService executorService = null;
    // 引入一个 boolean 变量控制服务器是否继续运行
    private volatile boolean runnable = true;

    public BrokerServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
    }

    public void start() throws IOException {
        System.out.println("[BrokerServer] 启动!");
        executorService = Executors.newCachedThreadPool();
        try {
            while (runnable) {
                Socket clientSocket = serverSocket.accept();
                // 把处理连接的逻辑丢给这个线程池.
                executorService.submit(() -> {
                    processConnection(clientSocket);
                });
            }
        } catch (SocketException e) {
            System.out.println("[BrokerServer] 服务器停止运行!");
            // e.printStackTrace();
        }
    }

    // 一般来说停止服务器, 就是直接 kill 掉对应进程就行了.
    // 此处还是搞一个单独的停止方法. 主要是用于后续的单元测试.
    public void stop() throws IOException {
        runnable = false;
        // 把线程池中的任务都放弃了. 让线程都销毁.
        executorService.shutdownNow();
        serverSocket.close();
    }

    // 通过这个方法, 来处理一个客户端的连接.
    // 在这一个连接中, 可能会涉及到多个请求和响应.
    private void processConnection(Socket clientSocket) {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {
            // 这里需要按照特定格式来读取并解析. 此时就需要用到 DataInputStream 和 DataOutputStream
            try (DataInputStream dataInputStream = new DataInputStream(inputStream);
                 DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
                while (true) {
                    // 1. 读取请求并解析.
                    Request request = readRequest(dataInputStream);
                    // 2. 根据请求计算响应
                    Response response = process(request, clientSocket);
                    // 3. 把响应写回给客户端
                    writeResponse(dataOutputStream, response);
                }
            }
        } catch (EOFException | SocketException e) {
            // 对于这个代码, DataInputStream 如果读到 EOF , 就会抛出一个 EOFException 异常.
            // 需要借助这个异常来结束循环
            System.out.println("[BrokerServer] connection 关闭! 客户端的地址: " + clientSocket.getInetAddress().toString()
                    + ":" + clientSocket.getPort());
        } catch (IOException | ClassNotFoundException | MqException e) {
            System.out.println("[BrokerServer] connection 出现异常!");
            e.printStackTrace();
        } finally {
            try {
                // 当连接处理完了, 就需要记得关闭 socket
                clientSocket.close();
                // 一个 TCP 连接中, 可能包含多个 channel. 需要把当前这个 socket 对应的所有 channel 也顺便清理掉.
                clearClosedSession(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Request readRequest(DataInputStream dataInputStream) throws IOException {
        Request request = new Request();
        request.setType(dataInputStream.readInt());
        request.setLength(dataInputStream.readInt());
        byte[] payload = new byte[request.getLength()];
        int n = dataInputStream.read(payload);
        if (n != request.getLength()) {
            throw new IOException("读取请求格式出错!");
        }
        request.setPayload(payload);
        return request;
    }

    private void writeResponse(DataOutputStream dataOutputStream, Response response) throws IOException {
        dataOutputStream.writeInt(response.getType());
        dataOutputStream.writeInt(response.getLength());
        dataOutputStream.write(response.getPayload());
        // 这个刷新缓冲区也是重要的操作!!
        dataOutputStream.flush();
    }

    private Response process(Request request, Socket clientSocket) throws IOException, ClassNotFoundException, MqException {
        // 1. 把 request 中的 payload 做一个初步的解析.
        BasicArguments basicArguments = (BasicArguments) BinaryTool.fromBytes(request.getPayload());
        System.out.println("[Request] rid=" + basicArguments.getRid() + ", channelId=" + basicArguments.getChannelId()
            + ", type=" + request.getType() + ", length=" + request.getLength());
        // 2. 根据 type 的值, 来进一步区分接下来这次请求要干啥.
        boolean ok = true;
        if (request.getType() == 0x1) {
            // 创建 channel
            sessions.put(basicArguments.getChannelId(), clientSocket);
            System.out.println("[BrokerServer] 创建 channel 完成! channelId=" + basicArguments.getChannelId());
        } else if (request.getType() == 0x2) {
            // 销毁 channel
            sessions.remove(basicArguments.getChannelId());
            System.out.println("[BrokerServer] 销毁 channel 完成! channelId=" + basicArguments.getChannelId());
        } else if (request.getType() == 0x3) {
            // 创建交换机. 此时 payload 就是 ExchangeDeclareArguments 对象了.
            ExchangeDeclareArguments arguments = (ExchangeDeclareArguments) basicArguments;
            ok = virtualHost.exchangeDeclare(arguments.getExchangeName(), arguments.getExchangeType(),
                    arguments.isDurable(), arguments.isAutoDelete(), arguments.getArguments());
        } else if (request.getType() == 0x4) {
            ExchangeDeleteArguments arguments = (ExchangeDeleteArguments) basicArguments;
            ok = virtualHost.exchangeDelete(arguments.getExchangeName());
        } else if (request.getType() == 0x5) {
            QueueDeclareArguments arguments = (QueueDeclareArguments) basicArguments;
            ok = virtualHost.queueDeclare(arguments.getQueueName(), arguments.isDurable(),
                    arguments.isExclusive(), arguments.isAutoDelete(), arguments.getArguments());
        } else if (request.getType() == 0x6) {
            QueueDeleteArguments arguments = (QueueDeleteArguments) basicArguments;
            ok = virtualHost.queueDelete((arguments.getQueueName()));
        } else if (request.getType() == 0x7) {
            QueueBindArguments arguments = (QueueBindArguments) basicArguments;
            ok = virtualHost.queueBind(arguments.getQueueName(), arguments.getExchangeName(), arguments.getBindingKey());
        } else if (request.getType() == 0x8) {
            QueueUnbindArguments arguments = (QueueUnbindArguments) basicArguments;
            ok = virtualHost.queueUnbind(arguments.getQueueName(), arguments.getExchangeName());
        } else if (request.getType() == 0x9) {
            BasicPublishArguments arguments = (BasicPublishArguments) basicArguments;
            ok = virtualHost.basicPublish(arguments.getExchangeName(), arguments.getRoutingKey(),
                    arguments.getBasicProperties(), arguments.getBody());
        } else if (request.getType() == 0xa) {
            BasicConsumeArguments arguments = (BasicConsumeArguments) basicArguments;
            ok = virtualHost.basicConsume(arguments.getConsumerTag(), arguments.getQueueName(), arguments.isAutoAck(),
                    new Consumer() {
                        // 这个回调函数要做的工作, 就是把服务器收到的消息可以直接推送回对应的消费者客户端
                        @Override
                        public void handleDelivery(String consumerTag, BasicProperties basicProperties, byte[] body) throws MqException, IOException {
                            // 先知道当前这个收到的消息, 要发给哪个客户端.
                            // 此处 consumerTag 其实是 channelId. 根据 channelId 去 sessions 中查询, 就可以得到对应的
                            // socket 对象了, 从而可以往里面发送数据了
                            // 1. 根据 channelId 找到 socket 对象
                            Socket clientSocket = sessions.get(consumerTag);
                            if (clientSocket == null || clientSocket.isClosed()) {
                                throw new MqException("[BrokerServer] 订阅消息的客户端已经关闭!");
                            }
                            // 2. 构造响应数据
                            SubScribeReturns subScribeReturns = new SubScribeReturns();
                            subScribeReturns.setChannelId(consumerTag);
                            subScribeReturns.setRid(""); // 由于这里只有响应, 没有请求, 不需要去对应. rid 暂时不需要.
                            subScribeReturns.setOk(true);
                            subScribeReturns.setConsumerTag(consumerTag);
                            subScribeReturns.setBasicProperties(basicProperties);
                            subScribeReturns.setBody(body);
                            byte[] payload = BinaryTool.toBytes(subScribeReturns);
                            Response response = new Response();
                            // 0xc 表示服务器给消费者客户端推送的消息数据.
                            response.setType(0xc);
                            // response 的 payload 就是一个 SubScribeReturns
                            response.setLength(payload.length);
                            response.setPayload(payload);
                            // 3. 把数据写回给客户端.
                            //    注意! 此处的 dataOutputStream 这个对象不能 close !!!
                            //    如果 把 dataOutputStream 关闭, 就会直接把 clientSocket 里的 outputStream 也关了.
                            //    此时就无法继续往 socket 中写入后续数据了.
                            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                            writeResponse(dataOutputStream, response);
                        }
                    });
        } else if (request.getType() == 0xb) {
            // 调用 basicAck 确认消息.
            BasicAckArguments arguments = (BasicAckArguments) basicArguments;
            ok = virtualHost.basicAck(arguments.getQueueName(), arguments.getMessageId());
        } else {
            // 当前的 type 是非法的.
            throw new MqException("[BrokerServer] 未知的 type! type=" + request.getType());
        }
        // 3. 构造响应
        BasicReturns basicReturns = new BasicReturns();
        basicReturns.setChannelId(basicArguments.getChannelId());
        basicReturns.setRid(basicArguments.getRid());
        basicReturns.setOk(ok);
        byte[] payload = BinaryTool.toBytes(basicReturns);
        Response response = new Response();
        response.setType(request.getType());
        response.setLength(payload.length);
        response.setPayload(payload);
        System.out.println("[Response] rid=" + basicReturns.getRid() + ", channelId=" + basicReturns.getChannelId()
            + ", type=" + response.getType() + ", length=" + response.getLength());
        return response;
    }

    private void clearClosedSession(Socket clientSocket) {
        // 这里要做的事情, 主要就是遍历上述 sessions hash 表, 把该被关闭的 socket 对应的键值对, 统统删掉.
        List<String> toDeleteChannelId = new ArrayList<>();
        for (Map.Entry<String, Socket> entry : sessions.entrySet()) {
            if (entry.getValue() == clientSocket) {
                // 不能在这里直接删除!!!
                // 这属于使用集合类的一个大忌!!! 一边遍历, 一边删除!!!
                // sessions.remove(entry.getKey());
                toDeleteChannelId.add(entry.getKey());
            }
        }
        for (String channelId : toDeleteChannelId) {
            sessions.remove(channelId);
        }
        System.out.println("[BrokerServer] 清理 session 完成! 被清理的 channelId=" + toDeleteChannelId);
    }

}

```
### 2. 启动/停⽌服务器
• 这⾥就是⼀个单纯的 TCP 服务器, 没啥特别的.
• 实现停⽌操作, 主要是为了⽅便后续开展单元测试

```java
    public void start() throws IOException {
        System.out.println("[BrokerServer] 启动!");
        executorService = Executors.newCachedThreadPool();
        try {
            while (runnable) {
                Socket clientSocket = serverSocket.accept();
                // 把处理连接的逻辑丢给这个线程池.
                executorService.submit(() -> {
                    processConnection(clientSocket);
                });
            }
        } catch (SocketException e) {
            System.out.println("[BrokerServer] 服务器停止运行!");
            // e.printStackTrace();
        }
    }

    // 一般来说停止服务器, 就是直接 kill 掉对应进程就行了.
    // 此处还是搞一个单独的停止方法. 主要是用于后续的单元测试.
    public void stop() throws IOException {
        runnable = false;
        // 把线程池中的任务都放弃了. 让线程都销毁.
        executorService.shutdownNow();
        serverSocket.close();
    }
```
### 3. 实现处理连接
• 对于 EOFException 和 SocketException , 我们视为客⼾端正常断开连接.
◦ 如果是客⼾端先 close, 后调⽤ DataInputStream 的 read, 则抛出 EOFException
◦ 如果是先调⽤ DataInputStream 的 read, 后客⼾端调⽤ close, 则抛出 SocketException

```java
    // 通过这个方法, 来处理一个客户端的连接.
    // 在这一个连接中, 可能会涉及到多个请求和响应.
    private void processConnection(Socket clientSocket) {
        try (InputStream inputStream = clientSocket.getInputStream();
             OutputStream outputStream = clientSocket.getOutputStream()) {
            // 这里需要按照特定格式来读取并解析. 此时就需要用到 DataInputStream 和 DataOutputStream
            try (DataInputStream dataInputStream = new DataInputStream(inputStream);
                 DataOutputStream dataOutputStream = new DataOutputStream(outputStream)) {
                while (true) {
                    // 1. 读取请求并解析.
                    Request request = readRequest(dataInputStream);
                    // 2. 根据请求计算响应
                    Response response = process(request, clientSocket);
                    // 3. 把响应写回给客户端
                    writeResponse(dataOutputStream, response);
                }
            }
        } catch (EOFException | SocketException e) {
            // 对于这个代码, DataInputStream 如果读到 EOF , 就会抛出一个 EOFException 异常.
            // 需要借助这个异常来结束循环
            System.out.println("[BrokerServer] connection 关闭! 客户端的地址: " + clientSocket.getInetAddress().toString()
                    + ":" + clientSocket.getPort());
        } catch (IOException | ClassNotFoundException | MqException e) {
            System.out.println("[BrokerServer] connection 出现异常!");
            e.printStackTrace();
        } finally {
            try {
                // 当连接处理完了, 就需要记得关闭 socket
                clientSocket.close();
                // 一个 TCP 连接中, 可能包含多个 channel. 需要把当前这个 socket 对应的所有 channel 也顺便清理掉.
                clearClosedSession(clientSocket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
```
### 4. 实现 readRequest

```java
    private Request readRequest(DataInputStream dataInputStream) throws IOException {
        Request request = new Request();
        request.setType(dataInputStream.readInt());
        request.setLength(dataInputStream.readInt());
        byte[] payload = new byte[request.getLength()];
        int n = dataInputStream.read(payload);
        if (n != request.getLength()) {
            throw new IOException("读取请求格式出错!");
        }
        request.setPayload(payload);
        return request;
    }
```
### 5. 实现 writeResponse

```java
    private void writeResponse(DataOutputStream dataOutputStream, Response response) throws IOException {
        dataOutputStream.writeInt(response.getType());
        dataOutputStream.writeInt(response.getLength());
        dataOutputStream.write(response.getPayload());
        // 这个刷新缓冲区也是重要的操作!!
        dataOutputStream.flush();
    }
```
### 6. 实现处理请求



• 先把请求转换成 BaseArguments , 获取到其中的 channelId 和 rid
• 再根据不同的 type, 分别处理不同的逻辑. (主要是调⽤ virtualHost 中不同的⽅法).
• 针对消息订阅操作, 则需要在存在消息的时候通过回调, 把响应结果写回给对应的客⼾端.
• 最后构造成统⼀的响应

```java
    private Response process(Request request, Socket clientSocket) throws IOException, ClassNotFoundException, MqException {
        // 1. 把 request 中的 payload 做一个初步的解析.
        BasicArguments basicArguments = (BasicArguments) BinaryTool.fromBytes(request.getPayload());
        System.out.println("[Request] rid=" + basicArguments.getRid() + ", channelId=" + basicArguments.getChannelId()
            + ", type=" + request.getType() + ", length=" + request.getLength());
        // 2. 根据 type 的值, 来进一步区分接下来这次请求要干啥.
        boolean ok = true;
        if (request.getType() == 0x1) {
            // 创建 channel
            sessions.put(basicArguments.getChannelId(), clientSocket);
            System.out.println("[BrokerServer] 创建 channel 完成! channelId=" + basicArguments.getChannelId());
        } else if (request.getType() == 0x2) {
            // 销毁 channel
            sessions.remove(basicArguments.getChannelId());
            System.out.println("[BrokerServer] 销毁 channel 完成! channelId=" + basicArguments.getChannelId());
        } else if (request.getType() == 0x3) {
            // 创建交换机. 此时 payload 就是 ExchangeDeclareArguments 对象了.
            ExchangeDeclareArguments arguments = (ExchangeDeclareArguments) basicArguments;
            ok = virtualHost.exchangeDeclare(arguments.getExchangeName(), arguments.getExchangeType(),
                    arguments.isDurable(), arguments.isAutoDelete(), arguments.getArguments());
        } else if (request.getType() == 0x4) {
            ExchangeDeleteArguments arguments = (ExchangeDeleteArguments) basicArguments;
            ok = virtualHost.exchangeDelete(arguments.getExchangeName());
        } else if (request.getType() == 0x5) {
            QueueDeclareArguments arguments = (QueueDeclareArguments) basicArguments;
            ok = virtualHost.queueDeclare(arguments.getQueueName(), arguments.isDurable(),
                    arguments.isExclusive(), arguments.isAutoDelete(), arguments.getArguments());
        } else if (request.getType() == 0x6) {
            QueueDeleteArguments arguments = (QueueDeleteArguments) basicArguments;
            ok = virtualHost.queueDelete((arguments.getQueueName()));
        } else if (request.getType() == 0x7) {
            QueueBindArguments arguments = (QueueBindArguments) basicArguments;
            ok = virtualHost.queueBind(arguments.getQueueName(), arguments.getExchangeName(), arguments.getBindingKey());
        } else if (request.getType() == 0x8) {
            QueueUnbindArguments arguments = (QueueUnbindArguments) basicArguments;
            ok = virtualHost.queueUnbind(arguments.getQueueName(), arguments.getExchangeName());
        } else if (request.getType() == 0x9) {
            BasicPublishArguments arguments = (BasicPublishArguments) basicArguments;
            ok = virtualHost.basicPublish(arguments.getExchangeName(), arguments.getRoutingKey(),
                    arguments.getBasicProperties(), arguments.getBody());
        } else if (request.getType() == 0xa) {
            BasicConsumeArguments arguments = (BasicConsumeArguments) basicArguments;
            ok = virtualHost.basicConsume(arguments.getConsumerTag(), arguments.getQueueName(), arguments.isAutoAck(),
                    new Consumer() {
                        // 这个回调函数要做的工作, 就是把服务器收到的消息可以直接推送回对应的消费者客户端
                        @Override
                        public void handleDelivery(String consumerTag, BasicProperties basicProperties, byte[] body) throws MqException, IOException {
                            // 先知道当前这个收到的消息, 要发给哪个客户端.
                            // 此处 consumerTag 其实是 channelId. 根据 channelId 去 sessions 中查询, 就可以得到对应的
                            // socket 对象了, 从而可以往里面发送数据了
                            // 1. 根据 channelId 找到 socket 对象
                            Socket clientSocket = sessions.get(consumerTag);
                            if (clientSocket == null || clientSocket.isClosed()) {
                                throw new MqException("[BrokerServer] 订阅消息的客户端已经关闭!");
                            }
                            // 2. 构造响应数据
                            SubScribeReturns subScribeReturns = new SubScribeReturns();
                            subScribeReturns.setChannelId(consumerTag);
                            subScribeReturns.setRid(""); // 由于这里只有响应, 没有请求, 不需要去对应. rid 暂时不需要.
                            subScribeReturns.setOk(true);
                            subScribeReturns.setConsumerTag(consumerTag);
                            subScribeReturns.setBasicProperties(basicProperties);
                            subScribeReturns.setBody(body);
                            byte[] payload = BinaryTool.toBytes(subScribeReturns);
                            Response response = new Response();
                            // 0xc 表示服务器给消费者客户端推送的消息数据.
                            response.setType(0xc);
                            // response 的 payload 就是一个 SubScribeReturns
                            response.setLength(payload.length);
                            response.setPayload(payload);
                            // 3. 把数据写回给客户端.
                            //    注意! 此处的 dataOutputStream 这个对象不能 close !!!
                            //    如果 把 dataOutputStream 关闭, 就会直接把 clientSocket 里的 outputStream 也关了.
                            //    此时就无法继续往 socket 中写入后续数据了.
                            DataOutputStream dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                            writeResponse(dataOutputStream, response);
                        }
                    });
        } else if (request.getType() == 0xb) {
            // 调用 basicAck 确认消息.
            BasicAckArguments arguments = (BasicAckArguments) basicArguments;
            ok = virtualHost.basicAck(arguments.getQueueName(), arguments.getMessageId());
        } else {
            // 当前的 type 是非法的.
            throw new MqException("[BrokerServer] 未知的 type! type=" + request.getType());
        }
        // 3. 构造响应
        BasicReturns basicReturns = new BasicReturns();
        basicReturns.setChannelId(basicArguments.getChannelId());
        basicReturns.setRid(basicArguments.getRid());
        basicReturns.setOk(ok);
        byte[] payload = BinaryTool.toBytes(basicReturns);
        Response response = new Response();
        response.setType(request.getType());
        response.setLength(payload.length);
        response.setPayload(payload);
        System.out.println("[Response] rid=" + basicReturns.getRid() + ", channelId=" + basicReturns.getChannelId()
            + ", type=" + response.getType() + ", length=" + response.getLength());
        return response;
    }
```
### 7. 实现 clearClosedSession
• 如果客⼾端只关闭了 Connection, 没关闭 Connection 中包含的 Channel, 也没关系, 在这⾥统⼀进⾏清理.
• 注意迭代器失效问题.

```java
    private void clearClosedSession(Socket clientSocket) {
        // 这里要做的事情, 主要就是遍历上述 sessions hash 表, 把该被关闭的 socket 对应的键值对, 统统删掉.
        List<String> toDeleteChannelId = new ArrayList<>();
        for (Map.Entry<String, Socket> entry : sessions.entrySet()) {
            if (entry.getValue() == clientSocket) {
                // 不能在这里直接删除!!!
                // 这属于使用集合类的一个大忌!!! 一边遍历, 一边删除!!!
                // sessions.remove(entry.getKey());
                toDeleteChannelId.add(entry.getKey());
            }
        }
        for (String channelId : toDeleteChannelId) {
            sessions.remove(channelId);
        }
        System.out.println("[BrokerServer] 清理 session 完成! 被清理的 channelId=" + toDeleteChannelId);
    }
```
## ⼗三. 实现客⼾端

### 1. 创建 ConnectionFactory

> 属性：host port
>
> 方法：①创建一个connection方法 ②

⽤来创建连接的⼯⼚类.
• 当前没有实现⽤⼾认证和多虚拟主机, ⽤⼾名密码可以暂时先不要

```java
package com.example.mq.mqclient;

import java.io.IOException;

public class ConnectionFactory {
    // broker server 的 ip 地址
    private String host;
    // broker server 的端口号
    private int port;

    // 访问 broker server 的哪个虚拟主机.
    // 下列几个属性暂时先都不搞了.
//    private String virtualHostName;
//    private String username;
//    private String password;

    public Connection newConnection() throws IOException {
        Connection connection = new Connection(host, port);
        return connection;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}

```
Connection 和 Channel 的定义

⼀个客⼾端可以创建多个 Connection.
⼀个 Connection 对应⼀个 socket, ⼀个 TCP 连接.
⼀个 Connection 可以包含多个 Channel

### 2. Connection

> 属性：①socket ②channelMap ③callbackPool
>
> 方法：①初始化：线程不断读取请求 分开给channel

```java
public class Connection {
    private Socket socket = null;
    // 需要管理多个 channel. 使用一个 hash 表把若干个 channel 组织起来.
    private ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    private InputStream inputStream;
    private OutputStream outputStream;
    private DataInputStream dataInputStream;
    private DataOutputStream dataOutputStream;

    private ExecutorService callbackPool = null;

    public Connection(String host, int port) throws IOException {
        socket = new Socket(host, port);
        inputStream = socket.getInputStream();
        outputStream = socket.getOutputStream();
        dataInputStream = new DataInputStream(inputStream);
        dataOutputStream = new DataOutputStream(outputStream);

        callbackPool = Executors.newFixedThreadPool(4);

        // 创建一个扫描线程, 由这个线程负责不停的从 socket 中读取响应数据. 把这个响应数据再交给对应的 channel 负责处理.
        Thread t = new Thread(() -> {
            try {
                while (!socket.isClosed()) {
                    Response response = readResponse();
                    dispatchResponse(response);
                }
            } catch (SocketException e) {
                // 连接正常断开的. 此时这个异常直接忽略.
                System.out.println("[Connection] 连接正常断开!");
            } catch (IOException | ClassNotFoundException | MqException e) {
                System.out.println("[Connection] 连接异常断开!");
                e.printStackTrace();
            }
        });
        t.start();
    }

    public void close() {
        // 关闭 Connection 释放上述资源
        try {
            callbackPool.shutdownNow();
            channelMap.clear();
            inputStream.close();
            outputStream.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 使用这个方法来分别处理, 当前的响应是一个针对控制请求的响应, 还是服务器推送的消息.
    private void dispatchResponse(Response response) throws IOException, ClassNotFoundException, MqException {
        if (response.getType() == 0xc) {
            // 服务器推送来的消息数据
            SubScribeReturns subScribeReturns = (SubScribeReturns) BinaryTool.fromBytes(response.getPayload());
            // 根据 channelId 找到对应的 channel 对象
            Channel channel = channelMap.get(subScribeReturns.getChannelId());
            if (channel == null) {
                throw new MqException("[Connection] 该消息对应的 channel 在客户端中不存在! channelId=" + channel.getChannelId());
            }
            // 执行该 channel 对象内部的回调.
            callbackPool.submit(() -> {
                try {
                    channel.getConsumer().handleDelivery(subScribeReturns.getConsumerTag(), subScribeReturns.getBasicProperties(),
                            subScribeReturns.getBody());
                } catch (MqException | IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            // 当前响应是针对刚才的控制请求的响应
            BasicReturns basicReturns = (BasicReturns) BinaryTool.fromBytes(response.getPayload());
            // 把这个结果放到对应的 channel 的 hash 表中.
            Channel channel = channelMap.get(basicReturns.getChannelId());
            if (channel == null) {
                throw new MqException("[Connection] 该消息对应的 channel 在客户端中不存在! channelId=" + channel.getChannelId());
            }
            channel.putReturns(basicReturns);
        }
    }

    // 发送请求
    public void writeRequest(Request request) throws IOException {
        dataOutputStream.writeInt(request.getType());
        dataOutputStream.writeInt(request.getLength());
        dataOutputStream.write(request.getPayload());
        dataOutputStream.flush();
        System.out.println("[Connection] 发送请求! type=" + request.getType() + ", length=" + request.getLength());
    }

    // 读取响应
    public Response readResponse() throws IOException {
        Response response = new Response();
        response.setType(dataInputStream.readInt());
        response.setLength(dataInputStream.readInt());
        byte[] payload = new byte[response.getLength()];
        int n = dataInputStream.read(payload);
        if (n != response.getLength()) {
            throw new IOException("读取的响应数据不完整!");
        }
        response.setPayload(payload);
        System.out.println("[Connection] 收到响应! type=" + response.getType() + ", length=" + response.getLength());
        return response;
    }

    // 通过这个方法, 在 Connection 中能够创建出一个 Channel
    public Channel createChannel() throws IOException {
        String channelId = "C-" + UUID.randomUUID().toString();
        Channel channel = new Channel(channelId, this);
        // 把这个 channel 对象放到 Connection 管理 channel 的 哈希表 中.
        channelMap.put(channelId, channel);
        // 同时也需要把 "创建 channel" 的这个消息也告诉服务器.
        boolean ok = channel.createChannel();
        if (!ok) {
            // 服务器这里创建失败了!! 整个这次创建 channel 操作不顺利!!
            // 把刚才已经加入 hash 表的键值对, 再删了.
            channelMap.remove(channelId);
            return null;
        }
        return channel;
    }
}

```

#### 封装请求响应读写操作

在 Connection 中, 实现下列⽅法

```java
    // 发送请求
    public void writeRequest(Request request) throws IOException {
        dataOutputStream.writeInt(request.getType());
        dataOutputStream.writeInt(request.getLength());
        dataOutputStream.write(request.getPayload());
        dataOutputStream.flush();
        System.out.println("[Connection] 发送请求! type=" + request.getType() + ", length=" + request.getLength());
    }

    // 读取响应
    public Response readResponse() throws IOException {
        Response response = new Response();
        response.setType(dataInputStream.readInt());
        response.setLength(dataInputStream.readInt());
        byte[] payload = new byte[response.getLength()];
        int n = dataInputStream.read(payload);
        if (n != response.getLength()) {
            throw new IOException("读取的响应数据不完整!");
        }
        response.setPayload(payload);
        System.out.println("[Connection] 收到响应! type=" + response.getType() + ", length=" + response.getLength());
        return response;
    }

```
#### 创建 channel

在 Connection 中, 定义下列⽅法来创建⼀个 channel

```java
    // 通过这个方法, 在 Connection 中能够创建出一个 Channel
    public Channel createChannel() throws IOException {
        String channelId = "C-" + UUID.randomUUID().toString();
        Channel channel = new Channel(channelId, this);
        // 把这个 channel 对象放到 Connection 管理 channel 的 哈希表 中.
        channelMap.put(channelId, channel);
        // 同时也需要把 "创建 channel" 的这个消息也告诉服务器.
        boolean ok = channel.createChannel();
        if (!ok) {
            // 服务器这里创建失败了!! 整个这次创建 channel 操作不顺利!!
            // 把刚才已经加入 hash 表的键值对, 再删了.
            channelMap.remove(channelId);
            return null;
        }
        return channel;
    }
```
### 3. Channel 

```java
package com.example.mq.mqclient;

import com.example.mq.common.*;
import com.example.mq.mqserver.core.BasicProperties;
import com.example.mq.mqserver.core.ExchangeType;

import java.io.IOException;
import java.lang.ref.ReferenceQueue;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class Channel {
    private String channelId;
    // 当前这个 channel 属于哪个连接.
    private Connection connection;
    // 用来存储后续客户端收到的服务器的响应.
    private ConcurrentHashMap<String, BasicReturns> basicReturnsMap = new ConcurrentHashMap<>();
    // 如果当前 Channel 订阅了某个队列, 就需要在此处记录下对应回调是啥. 当该队列的消息返回回来的时候, 调用回调.
    // 此处约定一个 Channel 中只能有一个回调.
    private Consumer consumer = null;

    public Channel(String channelId, Connection connection) {
        this.channelId = channelId;
        this.connection = connection;
    }

    // 在这个方法中, 和服务器进行交互, 告知服务器, 此处客户端创建了新的 channel 了.
    public boolean createChannel() throws IOException {
        // 对于创建 Channel 操作来说, payload 就是一个 basicArguments 对象
        BasicArguments basicArguments = new BasicArguments();
        basicArguments.setChannelId(channelId);
        basicArguments.setRid(generateRid());
        byte[] payload = BinaryTool.toBytes(basicArguments);

        Request request = new Request();
        request.setType(0x1);
        request.setLength(payload.length);
        request.setPayload(payload);

        // 构造出完整请求之后, 就可以发送这个请求了.
        connection.writeRequest(request);
        // 等待服务器的响应
        BasicReturns basicReturns = waitResult(basicArguments.getRid());
        return basicReturns.isOk();
    }

    // 期望使用这个方法来阻塞等待服务器的响应.
    private BasicReturns waitResult(String rid) {
        BasicReturns basicReturns = null;
        while ((basicReturns = basicReturnsMap.get(rid)) == null) {
            // 如果查询结果为 null, 说明包裹还没回来.
            // 此时就需要阻塞等待.
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // 读取成功之后, 还需要把这个消息从哈希表中删除掉.
        basicReturnsMap.remove(rid);
        return basicReturns;
    }

    public void putReturns(BasicReturns basicReturns) {
        basicReturnsMap.put(basicReturns.getRid(), basicReturns);
        synchronized (this) {
            // 当前也不知道有多少个线程在等待上述的这个响应.
            // 把所有的等待的线程都唤醒.
            notifyAll();
        }
    }

    private String generateRid() {
        return "R-" + UUID.randomUUID().toString();
    }

    // 关闭 channel, 给服务器发送一个 type = 0x2 的请求
    public boolean close() throws IOException {
        BasicArguments basicArguments = new BasicArguments();
        basicArguments.setRid(generateRid());
        basicArguments.setChannelId(channelId);
        byte[] payload = BinaryTool.toBytes(basicArguments);

        Request request = new Request();
        request.setType(0x2);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(basicArguments.getRid());
        return basicReturns.isOk();
    }

    // 创建交换机
    public boolean exchangeDeclare(String exchangeName, ExchangeType exchangeType, boolean durable, boolean autoDelete,
                                   Map<String, Object> arguments) throws IOException {
        ExchangeDeclareArguments exchangeDeclareArguments = new ExchangeDeclareArguments();
        exchangeDeclareArguments.setRid(generateRid());
        exchangeDeclareArguments.setChannelId(channelId);
        exchangeDeclareArguments.setExchangeName(exchangeName);
        exchangeDeclareArguments.setExchangeType(exchangeType);
        exchangeDeclareArguments.setDurable(durable);
        exchangeDeclareArguments.setAutoDelete(autoDelete);
        exchangeDeclareArguments.setArguments(arguments);
        byte[] payload = BinaryTool.toBytes(exchangeDeclareArguments);

        Request request = new Request();
        request.setType(0x3);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(exchangeDeclareArguments.getRid());
        return basicReturns.isOk();
    }

    // 删除交换机
    public boolean exchangeDelete(String exchangeName) throws IOException {
        ExchangeDeleteArguments arguments = new ExchangeDeleteArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setExchangeName(exchangeName);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x4);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 创建队列
    public boolean queueDeclare(String queueName, boolean durable, boolean exclusive, boolean autoDelete,
                                Map<String, Object> arguments) throws IOException {
        QueueDeclareArguments queueDeclareArguments = new QueueDeclareArguments();
        queueDeclareArguments.setRid(generateRid());
        queueDeclareArguments.setChannelId(channelId);
        queueDeclareArguments.setQueueName(queueName);
        queueDeclareArguments.setDurable(durable);
        queueDeclareArguments.setExclusive(exclusive);
        queueDeclareArguments.setAutoDelete(autoDelete);
        queueDeclareArguments.setArguments(arguments);
        byte[] payload = BinaryTool.toBytes(queueDeclareArguments);

        Request request = new Request();
        request.setType(0x5);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(queueDeclareArguments.getRid());
        return basicReturns.isOk();
    }

    // 删除队列
    public boolean queueDelete(String queueName) throws IOException {
        QueueDeleteArguments arguments = new QueueDeleteArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x6);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 创建绑定
    public boolean queueBind(String queueName, String exchangeName, String bindingKey) throws IOException {
        QueueBindArguments arguments = new QueueBindArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setExchangeName(exchangeName);
        arguments.setBindingKey(bindingKey);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x7);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 解除绑定
    public boolean queueUnbind(String queueName, String exchangeName) throws IOException {
        QueueUnbindArguments arguments = new QueueUnbindArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setExchangeName(exchangeName);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x8);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 发送消息
    public boolean basicPublish(String exchangeName, String routingKey, BasicProperties basicProperties, byte[] body) throws IOException {
        BasicPublishArguments arguments = new BasicPublishArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setExchangeName(exchangeName);
        arguments.setRoutingKey(routingKey);
        arguments.setBasicProperties(basicProperties);
        arguments.setBody(body);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x9);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 订阅消息
    public boolean basicConsume(String queueName, boolean autoAck, Consumer consumer) throws MqException, IOException {
        // 先设置回调.
        if (this.consumer != null) {
            throw new MqException("该 channel 已经设置过消费消息的回调了, 不能重复设置!");
        }
        this.consumer = consumer;

        BasicConsumeArguments arguments = new BasicConsumeArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setConsumerTag(channelId);  // 此处 consumerTag 也使用 channelId 来表示了.
        arguments.setQueueName(queueName);
        arguments.setAutoAck(autoAck);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0xa);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    // 确认消息
    public boolean basicAck(String queueName, String messageId) throws IOException {
        BasicAckArguments arguments = new BasicAckArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setMessageId(messageId);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0xb);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }

    public ConcurrentHashMap<String, BasicReturns> getBasicReturnsMap() {
        return basicReturnsMap;
    }

    public void setBasicReturnsMap(ConcurrentHashMap<String, BasicReturns> basicReturnsMap) {
        this.basicReturnsMap = basicReturnsMap;
    }

    public Consumer getConsumer() {
        return consumer;
    }

    public void setConsumer(Consumer consumer) {
        this.consumer = consumer;
    }

}

```

发送请求

通过 Channel 提供请求的发送操作.

#### 1) 创建 channel

```java
    // 在这个方法中, 和服务器进行交互, 告知服务器, 此处客户端创建了新的 channel 了.
    public boolean createChannel() throws IOException {
        // 对于创建 Channel 操作来说, payload 就是一个 basicArguments 对象
        BasicArguments basicArguments = new BasicArguments();
        basicArguments.setChannelId(channelId);
        basicArguments.setRid(generateRid());
        byte[] payload = BinaryTool.toBytes(basicArguments);

        Request request = new Request();
        request.setType(0x1);
        request.setLength(payload.length);
        request.setPayload(payload);

        // 构造出完整请求之后, 就可以发送这个请求了.
        connection.writeRequest(request);
        // 等待服务器的响应
        BasicReturns basicReturns = waitResult(basicArguments.getRid());
        return basicReturns.isOk();
    }
```
generateRid 的实现

```java
    private String generateRid() {
        return "R-" + UUID.randomUUID().toString();
    }
```
waitResult 的实现
• 由于服务器的响应是异步的. 此处通过 waitResult 实现同步等待的效果

```java

    // 期望使用这个方法来阻塞等待服务器的响应.
    private BasicReturns waitResult(String rid) {
        BasicReturns basicReturns = null;
        while ((basicReturns = basicReturnsMap.get(rid)) == null) {
            // 如果查询结果为 null, 说明包裹还没回来.
            // 此时就需要阻塞等待.
            synchronized (this) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        // 读取成功之后, 还需要把这个消息从哈希表中删除掉.
        basicReturnsMap.remove(rid);
        return basicReturns;
    }
```
#### 2) 关闭 channel

```java
    // 关闭 channel, 给服务器发送一个 type = 0x2 的请求
    public boolean close() throws IOException {
        BasicArguments basicArguments = new BasicArguments();
        basicArguments.setRid(generateRid());
        basicArguments.setChannelId(channelId);
        byte[] payload = BinaryTool.toBytes(basicArguments);

        Request request = new Request();
        request.setType(0x2);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(basicArguments.getRid());
        return basicReturns.isOk();
    }
```
#### 3) 创建交换机

```java
    // 创建交换机
    public boolean exchangeDeclare(String exchangeName, ExchangeType exchangeType, boolean durable, boolean autoDelete,
                                   Map<String, Object> arguments) throws IOException {
        ExchangeDeclareArguments exchangeDeclareArguments = new ExchangeDeclareArguments();
        exchangeDeclareArguments.setRid(generateRid());
        exchangeDeclareArguments.setChannelId(channelId);
        exchangeDeclareArguments.setExchangeName(exchangeName);
        exchangeDeclareArguments.setExchangeType(exchangeType);
        exchangeDeclareArguments.setDurable(durable);
        exchangeDeclareArguments.setAutoDelete(autoDelete);
        exchangeDeclareArguments.setArguments(arguments);
        byte[] payload = BinaryTool.toBytes(exchangeDeclareArguments);

        Request request = new Request();
        request.setType(0x3);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(exchangeDeclareArguments.getRid());
        return basicReturns.isOk();
    }
```
#### 4) 删除交换机

```java
    // 删除交换机
    public boolean exchangeDelete(String exchangeName) throws IOException {
        ExchangeDeleteArguments arguments = new ExchangeDeleteArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setExchangeName(exchangeName);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x4);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }
```
#### 5) 创建队列

```java
   // 创建队列
    public boolean queueDeclare(String queueName, boolean durable, boolean exclusive, boolean autoDelete,
                                Map<String, Object> arguments) throws IOException {
        QueueDeclareArguments queueDeclareArguments = new QueueDeclareArguments();
        queueDeclareArguments.setRid(generateRid());
        queueDeclareArguments.setChannelId(channelId);
        queueDeclareArguments.setQueueName(queueName);
        queueDeclareArguments.setDurable(durable);
        queueDeclareArguments.setExclusive(exclusive);
        queueDeclareArguments.setAutoDelete(autoDelete);
        queueDeclareArguments.setArguments(arguments);
        byte[] payload = BinaryTool.toBytes(queueDeclareArguments);

        Request request = new Request();
        request.setType(0x5);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(queueDeclareArguments.getRid());
        return basicReturns.isOk();
    }
```
#### 6) 删除队列

```java
    // 删除队列
    public boolean queueDelete(String queueName) throws IOException {
        QueueDeleteArguments arguments = new QueueDeleteArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x6);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }
```
#### 7) 创建绑定

```java
    // 创建绑定
    public boolean queueBind(String queueName, String exchangeName, String bindingKey) throws IOException {
        QueueBindArguments arguments = new QueueBindArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setExchangeName(exchangeName);
        arguments.setBindingKey(bindingKey);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x7);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }
```
#### 8) 删除绑定

```java
    // 解除绑定
    public boolean queueUnbind(String queueName, String exchangeName) throws IOException {
        QueueUnbindArguments arguments = new QueueUnbindArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setExchangeName(exchangeName);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x8);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }
```
#### 9) 发送消息

```java
    // 发送消息
    public boolean basicPublish(String exchangeName, String routingKey, BasicProperties basicProperties, byte[] body) throws IOException {
        BasicPublishArguments arguments = new BasicPublishArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setExchangeName(exchangeName);
        arguments.setRoutingKey(routingKey);
        arguments.setBasicProperties(basicProperties);
        arguments.setBody(body);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0x9);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }
```

#### 10) 订阅消息

```java

    // 订阅消息
    public boolean basicConsume(String queueName, boolean autoAck, Consumer consumer) throws MqException, IOException {
        // 先设置回调.
        if (this.consumer != null) {
            throw new MqException("该 channel 已经设置过消费消息的回调了, 不能重复设置!");
        }
        this.consumer = consumer;

        BasicConsumeArguments arguments = new BasicConsumeArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setConsumerTag(channelId);  // 此处 consumerTag 也使用 channelId 来表示了.
        arguments.setQueueName(queueName);
        arguments.setAutoAck(autoAck);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0xa);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }
```

#### 11) 确认消息

```java
    // 确认消息
    public boolean basicAck(String queueName, String messageId) throws IOException {
        BasicAckArguments arguments = new BasicAckArguments();
        arguments.setRid(generateRid());
        arguments.setChannelId(channelId);
        arguments.setQueueName(queueName);
        arguments.setMessageId(messageId);
        byte[] payload = BinaryTool.toBytes(arguments);

        Request request = new Request();
        request.setType(0xb);
        request.setLength(payload.length);
        request.setPayload(payload);

        connection.writeRequest(request);
        BasicReturns basicReturns = waitResult(arguments.getRid());
        return basicReturns.isOk();
    }
```

⼩结

上述发送请求的操作, 逻辑基本⼀致. 构造参数 + 构造请求 + 发送 + 等待结果

## 十四、测试客⼾端-服务器

```java
public class MqClientTests {
    private BrokerServer brokerServer = null;
    private ConnectionFactory factory = null;
    private Thread t = null;

    @BeforeEach
    public void setUp() throws IOException {
        // 1. 先启动服务器
        MqApplication.context = SpringApplication.run(MqApplication.class);
        brokerServer = new BrokerServer(9090);
        t = new Thread(() -> {
            // 这个 start 方法会进入一个死循环. 使用一个新的线程来运行 start 即可!
            try {
                brokerServer.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        t.start();

        // 2. 配置 ConnectionFactory
        factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(9090);
    }

    @AfterEach
    public void tearDown() throws IOException {
        // 停止服务器
        brokerServer.stop();
        // t.join();
        MqApplication.context.close();

        // 删除必要的文件
        File file = new File("./data");
        FileUtils.deleteDirectory(file);

        factory = null;
    }

    @Test
    public void testConnection() throws IOException {
        Connection connection = factory.newConnection();
        Assertions.assertNotNull(connection);
    }

    @Test
    public void testChannel() throws IOException {
        Connection connection = factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel = connection.createChannel();
        Assertions.assertNotNull(channel);
    }

    @Test
    public void testExchange() throws IOException {
        Connection connection = factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel = connection.createChannel();
        Assertions.assertNotNull(channel);

        boolean ok = channel.exchangeDeclare("testExchange", ExchangeType.DIRECT, true, false, null);
        Assertions.assertTrue(ok);

        ok = channel.exchangeDelete("testExchange");
        Assertions.assertTrue(ok);

        // 此处稳妥起见, 把改关闭的要进行关闭.
        channel.close();
        connection.close();
    }

    @Test
    public void testQueue() throws IOException {
        Connection connection = factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel = connection.createChannel();
        Assertions.assertNotNull(channel);

        boolean ok = channel.queueDeclare("testQueue", true, false, false, null);
        Assertions.assertTrue(ok);

        ok = channel.queueDelete("testQueue");
        Assertions.assertTrue(ok);

        channel.close();
        connection.close();
    }

    @Test
    public void testBinding() throws IOException {
        Connection connection = factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel = connection.createChannel();
        Assertions.assertNotNull(channel);

        boolean ok = channel.exchangeDeclare("testExchange", ExchangeType.DIRECT, true, false, null);
        Assertions.assertTrue(ok);
        ok = channel.queueDeclare("testQueue", true, false, false, null);
        Assertions.assertTrue(ok);

        ok = channel.queueBind("testQueue", "testExchange", "testBindingKey");
        Assertions.assertTrue(ok);

        ok = channel.queueUnbind("testQueue", "testExchange");
        Assertions.assertTrue(ok);

        channel.close();
        connection.close();
    }

    @Test
    public void testMessage() throws IOException, MqException, InterruptedException {
        Connection connection = factory.newConnection();
        Assertions.assertNotNull(connection);
        Channel channel = connection.createChannel();
        Assertions.assertNotNull(channel);

        boolean ok = channel.exchangeDeclare("testExchange", ExchangeType.DIRECT, true, false, null);
        Assertions.assertTrue(ok);
        ok = channel.queueDeclare("testQueue", true, false, false, null);
        Assertions.assertTrue(ok);

        byte[] requestBody = "hello".getBytes();
        ok = channel.basicPublish("testExchange", "testQueue", null, requestBody);
        Assertions.assertTrue(ok);

        ok = channel.basicConsume("testQueue", true, new Consumer() {
            @Override
            public void handleDelivery(String consumerTag, BasicProperties basicProperties, byte[] body) throws MqException, IOException {
                System.out.println("[消费数据] 开始!");
                System.out.println("consumerTag=" + consumerTag);
                System.out.println("basicProperties=" + basicProperties);
                Assertions.assertArrayEquals(requestBody, body);
                System.out.println("[消费数据] 结束!");
            }
        });
        Assertions.assertTrue(ok);

        Thread.sleep(500);

        channel.close();
        connection.close();
    }
}

```
## ⼗五. 案例: 基于 MQ 的⽣产者消费者模型

```java
/*
 * 这个类表示一个消费者.
 * 通常这个类也应该是在一个独立的服务器中被执行
 */
public class DemoConsumer {
    public static void main(String[] args) throws IOException, MqException, InterruptedException {
        System.out.println("启动消费者!");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(9090);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        channel.exchangeDeclare("testExchange", ExchangeType.DIRECT, true, false, null);
        channel.queueDeclare("testQueue", true, false, false, null);

        channel.basicConsume("testQueue", true, new Consumer() {
            @Override
            public void handleDelivery(String consumerTag, BasicProperties basicProperties, byte[] body) throws MqException, IOException {
                System.out.println("[消费数据] 开始!");
                System.out.println("consumerTag=" + consumerTag);
                System.out.println("basicProperties=" + basicProperties);
                String bodyString = new String(body, 0, body.length);
                System.out.println("body=" + bodyString);
                System.out.println("[消费数据] 结束!");
            }
        });

        // 由于消费者也不知道生产者要生产多少, 就在这里通过这个循环模拟一直等待消费.
        while (true) {
            Thread.sleep(500);
        }
    }
}

```

```java
/*
 * 这个类用来表示一个生产者.
 * 通常这是一个单独的服务器程序.
 */
public class DemoProducer {
    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("启动生产者");
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("127.0.0.1");
        factory.setPort(9090);

        Connection connection = factory.newConnection();
        Channel channel = connection.createChannel();

        // 创建交换机和队列
        channel.exchangeDeclare("testExchange", ExchangeType.DIRECT, true, false, null);
        channel.queueDeclare("testQueue", true, false, false, null);

        // 创建一个消息并发送
        byte[] body = "hello".getBytes();
        boolean ok = channel.basicPublish("testExchange", "testQueue", null, body);
        System.out.println("消息投递完成! ok=" + ok);

        Thread.sleep(500);
        channel.close();
        connection.close();
    }
}

```
## 十六、扩展功能

• 虚拟主机管理
• ⽤⼾管理/⽤⼾认证
• 交换机/队列 的独占模式和⾃动删除.
• 发送⽅确认(broker 给⽣产者的确认应答)
• 拒绝应答 (nack)
• 死信队列
• 管理接⼝
• 管理⻚⾯