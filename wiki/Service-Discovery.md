# Service Discovery

## What & Why
**Concept**: The ability for one service to look up the "phonebook" (Registry) to find the location of another service.
**Importance**: Enables decoupling. `Service A` doesn't need to know where `Service B` is, it just asks the Registry.

## How to Use
Spring Cloud provides the `DiscoveryClient` abstraction.

### Code Example

```java
@Autowired
private DiscoveryClient discoveryClient;

@Test
public void testDiscovery() {
    // Get all known service names
    for (String serviceId : discoveryClient.getServices()) {
        System.out.println("Found Service: " + serviceId);
        
        // Get specific instances for a service
        List<ServiceInstance> instances = discoveryClient.getInstances(serviceId);
        for (ServiceInstance instance : instances) {
            System.out.println(" - " + instance.getHost() + ":" + instance.getPort());
        }
    }
}
```

## Pro Tip: Simulating Clusters in IntelliJ IDEA
You can easily simulate a cluster (multiple instances of the same service) locally:
1.  Open **Run/Debug Configurations**.
2.  Select a service (e.g., `OrderMainApplication`).
3.  Click **Copy Configuration**.
4.  Add to **Program arguments**: `--server.port=8002`.
5.  Run both.
6.  Check Nacos Console: you will see `service-order` with **2 instances**.
