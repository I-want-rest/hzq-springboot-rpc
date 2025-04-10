package com.hzq.rpc.server.spring;

import com.hzq.rpc.server.annotation.RpcComponentScan;
import com.hzq.rpc.server.annotation.RpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * RPC 服务 BeanDefinition 注册器
 * 用于扫描指定包路径下标记 {@link RpcService} 注解的类并注册到 Spring 容器
 */
@Slf4j
public class RpcBeanDefinitionRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    private ResourceLoader resourceLoader;

    @Override
    public void setResourceLoader(@NonNull ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void registerBeanDefinitions(
            @NonNull AnnotationMetadata annotationMetadata,
            @NonNull BeanDefinitionRegistry registry
    ) {
        // 1. 解析 RpcComponentScan 注解属性
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
                annotationMetadata.getAnnotationAttributes(RpcComponentScan.class.getName())
        );

        if (attributes == null) {
            log.warn("No @RpcComponentScan annotation found");
            return;
        }

        // 2. 获取扫描包路径（支持空值处理和默认包路径）
        String[] basePackages = resolveBasePackages(annotationMetadata, attributes);

        // 3. 创建并配置自定义扫描器
        ClassPathBeanDefinitionScanner scanner = createRpcServiceScanner(registry);

        // 4. 执行扫描并记录结果
        int beanCount = scanner.scan(basePackages);
        log.info("Registered {} RPC service beans from packages: {}", beanCount, Arrays.toString(basePackages));
    }

    /**
     * 解析需要扫描的包路径
     */
    private String[] resolveBasePackages(AnnotationMetadata metadata, AnnotationAttributes attributes) {
        // 优先获取显式配置的包路径
        String[] basePackages = attributes.getStringArray("basePackages");

        // 若未配置，则使用启动类所在包作为默认路径
        if (basePackages.length == 0) {
            String defaultPackage = ClassUtils.getPackageName(metadata.getClassName());
            log.debug("No basePackages specified, using default package: {}", defaultPackage);
            return new String[]{defaultPackage};
        }

        // 过滤空字符串
        return Arrays.stream(basePackages)
                .filter(StringUtils::hasText)
                .toArray(String[]::new);
    }

    /**
     * 创建 RPC 服务扫描器
     */
    private ClassPathBeanDefinitionScanner createRpcServiceScanner(BeanDefinitionRegistry registry) {
        RpcClassPathBeanDefinitionScanner scanner = new RpcClassPathBeanDefinitionScanner(registry, RpcService.class);

        // 可选：配置过滤规则（例如排除接口、抽象类等）
        scanner.resetFilters(false);
        scanner.addIncludeFilter((metadataReader, factory) -> true);

        // 注入资源加载器（确保不为空）
        Optional.ofNullable(resourceLoader).ifPresent(scanner::setResourceLoader);

        return scanner;
    }
}
