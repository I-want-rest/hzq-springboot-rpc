Netty + Zookeeper + SpringBoot 实现的自定义 RPC 框架。通信协议包括 Http、Socket 等。
### 项目实现

- [x] 自定义创建的消息协议，编解码器
- [x] 基于 Zookeeper 的服务注册与发现，增加服务本地缓存与监听
- [x] 实现 Netty/Socket/Http 三种方式的网路通信
- [x] 实现 Netty 心跳机制，复用 Channel 连接
- [x] 多种序列化算法（JDK、JSON、HESSIAN、KRYO、PROTOSTUFF）
- [x] 多种负载均衡算法（RoundRobin、Random、ConsistentHash）
- [x] 两类动态代理（JDK、CGLIB）
- [x] 集成 Spring，自定义注解提供 RPC 组件扫描、服务注册、服务消费
- [x] 集成 SpringBoot，实现自动配置
- [x] 自定义的 SPI 机制
各个模块内有详细的模块readme介绍
