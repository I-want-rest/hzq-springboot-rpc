# rpc-client
client模块中共有7个文件包，分别对应7个模块
## annotation
### RpcReference
注解属性（Attributes）
interfaceClass
作用: 指定远程服务的接口类型。
默认值: void.class（通常通过字段类型自动推断接口）。
interfaceName
作用: 指定接口的全限定类名（如 com.example.UserService）。
默认值: 空字符串，与 interfaceClass 二选一使用。
version
作用: 服务版本号，用于区分同一接口的不同实现（如灰度发布）。
默认值: "1.0"。
loadbalance
作用: 负载均衡策略，可选 random（随机）、roundrobin（轮询）、leastactive（最小活跃调用）。
默认值: 空字符串（使用全局配置的默认策略）。
mock
作用: 服务降级时的模拟实现类名。若未设置，默认使用接口名 + Mock（如 UserServiceMock）。
示例: 当远程服务不可用时，调用本地Mock类返回预设数据。
timeout
作用: 服务调用超时时间（单位通常为毫秒）。
默认值: 0（表示使用全局配置）
## common
### RequestMetadata
请求数据元格式
## config
### RpcClientAutoConfiguration
作用：根据配置属性动态创建 RPC 客户端所需的组件（如负载均衡、服务发现、传输协议等）。
核心注解：
@Configuration：标记为 Spring 配置类。
@EnableConfigurationProperties(RpcClientProperties.class)：启用属性绑定，将配置文件中的 rpc.client.* 属性映射到 RpcClientProperties 类。
使用 @ConditionalOnXxx 注解实现条件化 Bean 创建。
### RpcClientProperties
配置类，用于管理属性
## handler
### RpcResponseHandler
用于处理RPC客户端响应和心跳机制的Netty处理器
UNPROCESSED_RPC_RESPONSES:
维护一个全局的未完成RPC请求映射表，以sequenceId为键，存储对应的Promise对象。
原理：当客户端发起RPC调用时会生成sequenceId，并将Promise存入该Map，等待服务端响应时通过sequenceId匹配并完成异步通知。
流程：
通过sequenceId从Map中移除对应的Promise。
若Promise不存在（可能已超时或重复响应），记录警告。
根据响应内容决定Promise状态：异常则标记失败，否则传递成功结果。
## proxy
### ClientStubInvocationHandler
JDK动态代理
服务发现组件：用于定位服务实例地址
RPC客户端：实际发送网络请求
服务名称：标识目标服务（如UserService-1.0）
客户端配置：控制调用行为（超时、重试等）
Method对象：被调用的方法元数据
方法参数：调用时传入的实际参数
### ClientStubMethodInterceptor
作为 CGLIB 动态代理的拦截器，负责拦截本地方法调用并将其转发为远程 RPC 调用。
### ClientStubProxyFactory
RPC客户端中用于生成动态代理的核心工厂类，其主要职责是根据服务接口/类及版本创建对应的代理对象，并缓存这些代理以提高性能。
### RemoteMethodCall
实现远程方法调用
## spring
### RpcClientBeanPostProcessor
通过 Spring 的扩展机制实现了 RPC 客户端的自动化依赖注入
### RpcClientExitDisposableBean
客户端退出时的操作
## transport
### http
#### HttpRpcClient

### netty
#### ChannelProvider
ChannelProvider 具备以下特性：
安全可靠：自动清理无效连接，避免资源泄漏
高效管理：LRU 策略控制内存占用
易于维护：清晰的日志和原子操作
弹性扩展：预留连接池和心跳检测接入点
适用于需要高效管理长连接的 RPC 客户端场景，能够显著提升系统稳定性和资源利用率。
#### NettyRpcClient
基于 Netty 实现的 RPC 客户端，负责与 RPC 服务端建立连接、发送请求并处理响应
### socket
#### SocketRpcClient
通过Socket与RPC服务端建立连接，发送请求数据并同步接收响应结果

