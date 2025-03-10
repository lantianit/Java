package com.loadbalancer.gray;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.*;
import org.springframework.cloud.loadbalancer.core.*;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 定义灰度发布的负载均衡算法
 */
public class GrayLoadBalancer implements ReactorServiceInstanceLoadBalancer {
    private static final Log log = LogFactory.getLog(GrayLoadBalancer.class);
    private final String serviceId;
    private AtomicInteger position; // 位置，下标
    private ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

    public GrayLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider, String serviceId) {
        this.serviceId = serviceId;
        this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
        this.position = new AtomicInteger(new Random().nextInt(1000));
    }

    public Mono<Response<ServiceInstance>> choose(Request request) {
        // 提供备选的服务实例列表
        ServiceInstanceListSupplier supplier = (ServiceInstanceListSupplier) this.serviceInstanceListSupplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new);
        // 选择服务实例
        return supplier.get(request).next().map((serviceInstances) -> {
            return this.processInstanceResponse(supplier, serviceInstances, request);
        });
    }

    private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
                                                              List<ServiceInstance> serviceInstances,
                                                              Request request) {
        // 从备选的服务列表中选择一个具体的服务实例
        Response<ServiceInstance> serviceInstanceResponse = this.getInstanceResponse(serviceInstances,
                request);
        if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
            ((SelectedInstanceCallback) supplier).selectedServiceInstance((ServiceInstance) serviceInstanceResponse.getServer());
        }
        return serviceInstanceResponse;
    }

    private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances,
                                                          Request request) {
        // 实例为空
        if (instances.isEmpty()) {
            if (log.isWarnEnabled()) {
                log.warn("No servers available for service: " + this.serviceId);
            }
            return new EmptyResponse();
        } else { // 服务不为空
            // 灰度节点的业务实现
            // 0.得到 Request 对象[通过方法参数的传递得到此对象]
            // 1.从 Request 对象的 Header 中得到灰度标签
            RequestDataContext requestContext = (RequestDataContext) request.getContext();
            HttpHeaders headers = requestContext.getClientRequest().getHeaders();
            List<String> headersList = headers.get(GlobalVariable.GRAY_TAGE);
            if (headersList != null && headersList.size() > 0 &&
                    headersList.get(0).equals("true")) { // 灰度请求
                // 灰度列表
                List<ServiceInstance> grayList = instances.stream().
                        filter(i -> i.getMetadata().get(GlobalVariable.GRAY_TAGE) != null &&
                                i.getMetadata().get(GlobalVariable.GRAY_TAGE).equals("true")).
                        toList();
                if (grayList.size() > 0) { // 存在灰度服务节点
                    instances = grayList;
                }
            } else { // 正式节点
                // 2.将实例进行分组【正式服务列表|灰度服务列表】
                instances = instances.stream().
                        filter(i -> i.getMetadata().get(GlobalVariable.GRAY_TAGE) == null ||
                                !i.getMetadata().get(GlobalVariable.GRAY_TAGE).equals("true")).
                        toList();
            }
            // 3.使用负载均衡算法选择上一步列表中的某一个节点
            int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;
            ServiceInstance instance = (ServiceInstance)instances.get(pos % instances.size());
            return new DefaultResponse(instance);
        }
    }
}
