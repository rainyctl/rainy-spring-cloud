package cc.rainyctl.services.order;

import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import java.util.List;

@SpringBootTest
public class DiscoveryTest {

    @Autowired
    private DiscoveryClient discoveryClient; // option 1

    @Autowired
    private NacosServiceDiscovery nacosServiceDiscovery; // option 2

    @Test
    public void testDiscoveryClient() {
        for (String serviceId : discoveryClient.getServices()) {
            System.out.println("serviceId: " + serviceId);
            List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
            for (ServiceInstance instance : instances) {
                System.out.println(instance.getServiceId() + "\t" + instance.getHost() + "\t" + instance.getPort());
            }
        }
    }

    @Test
    public void testNacosServiceDiscovery() throws NacosException {
        for (String serviceId : nacosServiceDiscovery.getServices()) {
            System.out.println("serviceId: " + serviceId);
            List<ServiceInstance> instances = nacosServiceDiscovery.getInstances(serviceId);
            for (ServiceInstance instance : instances) {
                System.out.println(instance.getServiceId() + "\t" + instance.getHost() + "\t" + instance.getPort());
            }
        }
    }
}
