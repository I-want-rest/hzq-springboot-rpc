# rpc-core
## codec
### RpcFrameDecoder
粘包拆包编码器，使用固定长度的帧解码器，通过约定用定长字节表示接下来数据的长度 
### SharableRpcMessageCodec
```
--------------------------------------------------------------------
*  | 魔数 (4byte) | 版本号 (1byte)  | 序列化算法 (1byte) | 消息类型 (1byte) |
*  -------------------------------------------------------------------
*  |    状态类型 (1byte)  |    消息序列号 (4byte)   |    消息长度 (4byte)   |
*  --------------------------------------------------------------------
*  |                        消息内容 (不固定长度)                         |
-------------------------------------------------------------------
```
基于Netty框架实现的可共享（@Sharable）的RPC消息编解码器，负责将自定义协议格式的二进制数据与Java对象RpcMessage之间进行编码和解码
该编解码器作为Netty Pipeline中的关键组件，用于RPC框架的客户端与服务端通信，确保二进制数据与Java对象的高效转换，同时保障协议的完整性和安全性。
## common
### HeartbeatMessage
心跳检测信息格式
### RpcRequest
Rpc请求格式
### RpcResponse
Rpc响应格式
### ServiceInfo
请求服务信息
## config
### ThreadPoolConfig
线程池配置类
## constant
### ProtocolConstants
定义协议常量
## discovery
### ServiceDiscovery
定义发现类的同意接口，后续可以用nacos等进行拓展
### ZookeeperServiceDiscovery
Zookeeper实现发现类
## enums
### MessageStatus
消息状态枚举类
### MessageType
消息类型枚举类
### SerializationType
序列化枚举类
## exception
### RpcException
自定义Rpc异常
### SerializeException
序列化异常
## extension
### ExtensionLoader
实现了一个基于Java SPI（Service Provider Interface）机制的扩展加载器，类似于Dubbo的扩展点加载机制。它的核心功能是通过配置文件动态加载接口的实现类，并缓存实例以提高性能
SPI机制：通过META-INF/extensions/目录下的配置文件，定义接口与实现类的映射关系，实现插拔式扩展。
缓存优化：使用多级缓存（类加载器、实例对象）避免重复加载和反射开销。
线程安全：通过双重检查锁（Double-Checked Locking）和ConcurrentHashMap保证线程安全。
1. getExtensionLoader(Class<T> type)
   作用：获取指定接口的扩展加载器。
   校验逻辑：
   类型必须是接口。
   接口必须标注@SPI注解（Dubbo风格）。
   缓存机制：使用ConcurrentHashMap缓存ExtensionLoader实例，避免重复创建。
2. getExtension(String name)
   作用：根据名称获取扩展类实例。
   双重检查锁：
   通过Holder对象延迟初始化实例。
   使用synchronized保证线程安全，结合两次判空避免重复创建。
3. createExtension(String name)
   作用：反射创建扩展类实例。
   流程：
   根据名称从cachedClasses中获取实现类。
   从EXTENSION_INSTANCES缓存获取实例，若不存在则通过反射创建并缓存。
4. getExtensionClasses()
   作用：加载并缓存接口的所有扩展类。
   延迟加载：通过Holder和synchronized实现懒加载，仅在首次调用时解析配置文件。
5. loadDirectory() 和 loadResource()
   作用：从META-INF/extensions/目录加载配置文件。
   文件格式：每行格式为name=className，支持#注释。
   示例：
   myImpl=com.example.MyServiceImpl
anotherImpl=com.example.AnotherImpl
流程：
拼接文件路径（如META-INF/extensions/com.example.MyService）。
使用类加载器读取所有匹配的URL。
解析文件内容，加载类并存入extensionClasses。
### Holder
自定义容器
### SpiExtensionFactory
拓展工厂类，实际未使用
### SPI
SPI 注解，被标注的类表示为需要加载的扩展类接口
## factory
### SingletonFactory
线程安全的单例工厂类
原子性操作 使用 ConcurrentHashMap.computeIfAbsent() 替代 containsKey + put 组合操作，确保整个 get-or-create 过程原子化，彻底解决并发重复创建问题。
最终一致性 即使多线程同时触发创建，computeIfAbsent 保证最终只有一个实例被存入缓存。
### ThreadPoolFactory
线程池工厂类
## loadbalance
### LoadBalance
负载均衡接口类
### AbstractLoadBalance
抽象类
### impl
#### ConsistentHashLoadBalance
一致性哈希的负载均衡
一致性哈希实现细节 (ConsistentHashSelector)
虚拟节点生成
目的：解决节点分布不均的问题，提高负载均衡性。
步骤：
MD5摘要：对服务地址 + 序号进行MD5运算，得到16字节的摘要。
四次哈希计算：将摘要分成4段（每段4字节），生成4个不同的哈希值。
存入TreeMap：每个哈希值对应一个真实服务节点，形成哈希环。
哈希环查找 (selectForKey)
使用TreeMap.ceilingEntry(hash)找到第一个大于等于请求哈希的虚拟节点。
若未找到（如哈希值超出环尾），则取环首节点，形成逻辑环状结构。
#### RandomLoadBalance
随机负载均衡
#### RoundRobinLoadBalance
使用CAS（Compare-And-Swap）实现无锁线程安全：
获取当前值prev
计算下一个值next（处理Integer溢出）
通过compareAndSet原子更新，失败则重试
## protocol
### MessageHeader
协议头
### RpcMessage
协议
## registry
### ServiceRegistry
注册中心接口
### ZookeeperServiceRegistry
Zookeeper注册中心，注册相应服务
## serialization
### hessian
二进制协议，比JSON等文本协议更高效
跨语言支持（支持Java/Python/C++等）
相比JDK原生序列化：
体积减少50%以上
性能提升约4倍
避免Serializable接口强耦合
### jdk
jdk原生序列化方法
### json
json序列化
### kryo
通过Kryo高效实现了RPC对象的序列化，利用ThreadLocal解决线程安全问题
### protostuff
通过Protostuff库和缓冲区重用策略，提供了高效的序列化能力
### 序列化协议对比总表

| 维度            | Hessian          | JDK              | JSON             | Kryo             | Protostuff       |
|-----------------|------------------|------------------|------------------|------------------|------------------|
| **性能**        | 中               | 低               | 低               | **高**           | **高**           |
| **数据大小**    | 较小             | 大               | 大               | **最小**         | **小**           |
| **跨语言支持**  | **支持**         | 仅Java           | **支持**         | 仅JVM            | **支持**         |
| **易用性**      | 简单             | 简单             | **极简**         | 中等             | 中等             |
| **兼容性**      | 一般             | 差               | **强**           | 差               | 强               |
| **安全性**      | 安全             | **风险高**       | 安全             | 安全             | 安全             |
| **适用场景**    | 跨语言RPC        | 本地持久化       | REST API         | 高性能Java应用   | 跨语言高性能场景 |
### SerializationFactory
序列化工厂类
## util
### ServiceUtil
Service的工具类，包括命名、类型转换



