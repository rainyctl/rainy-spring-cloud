# Troubleshooting

## Common Issues

### MyBatis-Plus Dependency
**Issue**: `UnsatisfiedDependencyException` with Spring Boot 3.x.
**Solution**: Use `mybatis-plus-spring-boot3-starter`.

### Mapper Not Found
**Issue**: `NoSuchBeanDefinitionException`.
**Solution**: Add `@Mapper` to interfaces or use `@MapperScan`.

### Nacos Config Data ID
**Issue**: Config not loading.
**Solution**: Ensure Data ID includes extension (e.g., `service-order.properties`, not just `service-order`).

## Future Explorations

### Nacos with MySQL
Switch from embedded Derby to MySQL for persistence.
Requires `nacos-mysql.sql` and `custom.properties`.

### Nacos Cluster Mode
Deploy 3+ nodes behind Nginx for High Availability.
