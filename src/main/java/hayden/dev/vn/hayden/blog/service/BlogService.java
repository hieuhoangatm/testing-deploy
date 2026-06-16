package hayden.dev.vn.hayden.blog.service;

import hayden.dev.vn.hayden.blog.model.BlogPost;
import hayden.dev.vn.hayden.blog.model.Project;
import hayden.dev.vn.hayden.blog.model.Skill;
import hayden.dev.vn.hayden.blog.model.TimelineEntry;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
public class BlogService {

    private static final String POST1_CONTENT = """
# Virtual Threads trong Java 21

Java 21 mang đến một trong những thay đổi lớn nhất trong lịch sử ngôn ngữ: **Virtual Threads** (Project Loom). Đây không chỉ là tính năng mới mà là cuộc cách mạng trong cách viết ứng dụng concurrent.

## Virtual Thread là gì?

Virtual Threads là lightweight threads được quản lý bởi JVM thay vì hệ điều hành. Mỗi OS thread có thể host hàng nghìn Virtual Threads, giúp scale lên hàng triệu concurrent tasks mà không tốn nhiều tài nguyên.

```java
// Cách cũ với Platform Thread
ExecutorService platform = Executors.newFixedThreadPool(200);

// Cách mới với Virtual Thread
ExecutorService virtual = Executors.newVirtualThreadPerTaskExecutor();

// Tạo 1 triệu virtual threads
try (var executor = Executors.newVirtualThreadPerTaskExecutor()) {
    IntStream.range(0, 1_000_000).forEach(i ->
        executor.submit(() -> {
            Thread.sleep(Duration.ofSeconds(1));
            return i;
        })
    );
}
```

## Tại sao Virtual Threads quan trọng?

### Vấn đề với Thread-per-Request Model

Trước đây, mỗi HTTP request cần 1 OS thread. OS thread nặng (~1MB stack), nên server chỉ handle được vài trăm concurrent requests.

### Giải pháp: Virtual Threads

Virtual Threads nhẹ hơn (~KB), JVM schedule chúng trên một pool nhỏ OS threads (carrier threads). Khi Virtual Thread block (I/O, sleep), nó được unmount khỏi carrier thread, cho phép carrier thread phục vụ VT khác.

## Benchmark thực tế

```java
@SpringBootTest
class VirtualThreadBenchmark {

    @Test
    void compareThreadTypes() throws Exception {
        int taskCount = 10_000;
        Duration ioDelay = Duration.ofMillis(100);

        // Platform threads
        long platformMs = benchmark(
            Executors.newCachedThreadPool(),
            taskCount, ioDelay
        );

        // Virtual threads
        long virtualMs = benchmark(
            Executors.newVirtualThreadPerTaskExecutor(),
            taskCount, ioDelay
        );

        System.out.printf("Platform: %dms, Virtual: %dms%n",
            platformMs, virtualMs);
    }
}
```

## Tích hợp với Spring Boot

Spring Boot 3.2+ hỗ trợ Virtual Threads chỉ bằng 1 config:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

## Khi nào dùng Virtual Threads?

✅ **Phù hợp:** I/O-bound workloads (DB queries, HTTP calls, file I/O)

❌ **Không phù hợp:** CPU-bound tasks (video encoding, mã hóa nặng)

## Kết luận

Virtual Threads không thay thế reactive programming nhưng làm cho imperative code trở nên scalable như reactive. Đây là lý do chính để upgrade lên Java 21.
""";

    private static final String POST2_CONTENT = """
# Clean Architecture trong Spring Boot

Clean Architecture của Uncle Bob đã thay đổi cách tôi viết code. Sau khi áp dụng vào 3 dự án production, đây là những gì tôi học được.

## Vấn đề với code thông thường

Hầu hết Spring Boot projects có cấu trúc kiểu này:

```
src/
├── controller/
│   └── UserController.java
├── service/
│   └── UserService.java
├── repository/
│   └── UserRepository.java
└── entity/
    └── User.java
```

Cấu trúc theo layer này có vấn đề: **business logic bị phụ thuộc vào framework và database**.

## Clean Architecture là gì?

```
┌─────────────────────────────────────┐
│  Frameworks & Drivers               │ ← Spring, JPA, REST
│  ┌───────────────────────────────┐  │
│  │  Interface Adapters           │  │ ← Controllers, Repositories
│  │  ┌─────────────────────────┐  │  │
│  │  │  Application Business   │  │  │ ← Use Cases
│  │  │  ┌───────────────────┐  │  │  │
│  │  │  │  Enterprise Rules  │  │  │  │ ← Entities
│  │  │  └───────────────────┘  │  │  │
│  │  └─────────────────────────┘  │  │
│  └───────────────────────────────┘  │
└─────────────────────────────────────┘
```

**Dependency Rule:** Dependencies chỉ trỏ vào trong. Business rules không biết UI hay Database tồn tại.

## Ví dụ thực tế

### Domain Entity

```java
// Không có JPA annotation - thuần Java
public class User {
    private final UserId id;
    private String email;
    private String name;

    public void changeEmail(String newEmail) {
        if (!isValidEmail(newEmail)) {
            throw new InvalidEmailException(newEmail);
        }
        this.email = newEmail;
    }

    private boolean isValidEmail(String email) {
        return email != null && email.contains("@");
    }
}
```

### Use Case

```java
public class RegisterUserUseCase {
    private final UserRepository userRepository; // interface!
    private final EmailService emailService;     // interface!

    public User execute(RegisterUserCommand command) {
        if (userRepository.existsByEmail(command.email())) {
            throw new EmailAlreadyExistsException();
        }
        var user = new User(UserId.generate(), command.email(), command.name());
        userRepository.save(user);
        emailService.sendWelcome(user);
        return user;
    }
}
```

### Repository Interface (Domain layer)

```java
// Trong domain - không biết gì về JPA
public interface UserRepository {
    void save(User user);
    Optional<User> findById(UserId id);
    boolean existsByEmail(String email);
}
```

### Repository Implementation (Infrastructure layer)

```java
// Trong infrastructure - biết về JPA
@Repository
public class JpaUserRepository implements UserRepository {
    private final UserJpaRepository jpaRepository;

    @Override
    public void save(User user) {
        UserEntity entity = UserMapper.toEntity(user);
        jpaRepository.save(entity);
    }
}
```

## Kết quả sau 6 tháng áp dụng

- **Test coverage:** Từ 40% → 85% (business logic dễ test hơn)
- **Onboarding time:** Giảm 50% (cấu trúc rõ ràng hơn)
- **Bug rate:** Giảm 30% (separation of concerns)

## Kết luận

Clean Architecture có overhead ban đầu nhưng payoff về lâu dài. Hãy bắt đầu với một bounded context nhỏ trước khi áp dụng toàn project.
""";

    private static final String POST3_CONTENT = """
# Docker Best Practices cho Java Applications

Sau nhiều lần optimize Docker images cho Spring Boot, đây là những gì tôi đúc kết được.

## Sai lầm phổ biến

### Image quá lớn

```dockerfile
# ❌ Sai - image 600MB+
FROM openjdk:17
COPY target/app.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Cải thiện với Multi-stage Build

```dockerfile
# ✅ Stage 1: Build
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -q
COPY src ./src
RUN mvn package -DskipTests -q

# ✅ Stage 2: Extract layers
FROM eclipse-temurin:21-jre-alpine AS extract
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
RUN java -Djarmode=layertools -jar app.jar extract

# ✅ Stage 3: Runtime
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=extract /app/dependencies/ ./
COPY --from=extract /app/spring-boot-loader/ ./
COPY --from=extract /app/snapshot-dependencies/ ./
COPY --from=extract /app/application/ ./
EXPOSE 8080
ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]
```

**Kết quả:** Image size từ 600MB → 150MB, rebuild nhanh hơn 3x vì cache layers.

## JVM Tuning cho Container

```dockerfile
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseZGC \
               -Xss512k \
               -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
```

### Giải thích các flags:

| Flag | Mục đích |
|------|----------|
| `UseContainerSupport` | JVM đọc CPU/RAM limits từ cgroup |
| `MaxRAMPercentage=75` | Dùng 75% RAM của container |
| `UseZGC` | GC có latency thấp, phù hợp microservices |
| `Xss512k` | Giảm stack size per thread |

## Health Check

```dockerfile
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \\
  CMD curl -f http://localhost:8080/actuator/health || exit 1
```

## Docker Compose cho Development

```yaml
version: '3.8'
services:
  app:
    build:
      context: .
      target: build  # Chỉ build đến stage build khi dev
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
    volumes:
      - ./src:/app/src  # Hot reload
    depends_on:
      postgres:
        condition: service_healthy

  postgres:
    image: postgres:16-alpine
    environment:
      POSTGRES_DB: myapp
      POSTGRES_USER: dev
      POSTGRES_PASSWORD: dev123
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "dev"]
      interval: 5s
      timeout: 5s
      retries: 5
```

## Kết luận

3 điều quan trọng nhất: multi-stage build, JVM container flags, và layer ordering để tận dụng cache.
""";

    private static final String POST4_CONTENT = """
# Xây dựng REST API chuẩn với Spring Boot

Sau khi review hàng chục REST APIs, đây là những pattern tôi luôn áp dụng.

## Response Structure thống nhất

```java
@Data
@Builder
public class ApiResponse<T> {
    private boolean success;
    private T data;
    private String message;
    private String errorCode;
    private Instant timestamp;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .timestamp(Instant.now())
                .build();
    }

    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .errorCode(errorCode)
                .timestamp(Instant.now())
                .build();
    }
}
```

## Global Exception Handler

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiResponse<Void> handleNotFound(ResourceNotFoundException ex) {
        return ApiResponse.error(ex.getMessage(), "RESOURCE_NOT_FOUND");
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiResponse<Map<String, String>> handleValidation(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        fe -> fe.getDefaultMessage() != null
                              ? fe.getDefaultMessage() : "Invalid"
                ));
        return ApiResponse.<Map<String, String>>builder()
                .success(false)
                .data(errors)
                .errorCode("VALIDATION_FAILED")
                .timestamp(Instant.now())
                .build();
    }
}
```

## Versioning

```java
// Trong RequestMapping
@RestController
@RequestMapping("/api/v1/users")
public class UserV1Controller { ... }

// Hoặc dùng Header versioning
@GetMapping(headers = "X-API-Version=2")
public ResponseEntity<UserV2Dto> getUserV2(@PathVariable Long id) { ... }
```

## Pagination chuẩn

```java
@GetMapping
public ApiResponse<Page<UserDto>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(defaultValue = "createdAt,desc") String sort) {

    String[] sortParts = sort.split(",");
    Sort.Direction direction = sortParts.length > 1
            ? Sort.Direction.fromString(sortParts[1])
            : Sort.Direction.ASC;

    Pageable pageable = PageRequest.of(page, size,
            Sort.by(direction, sortParts[0]));

    return ApiResponse.ok(userService.findAll(pageable).map(userMapper::toDto));
}
```

## Idempotency cho POST

```java
@PostMapping
public ResponseEntity<ApiResponse<OrderDto>> createOrder(
        @RequestHeader("Idempotency-Key") String idempotencyKey,
        @Valid @RequestBody CreateOrderRequest request) {

    if (idempotencyService.exists(idempotencyKey)) {
        return ResponseEntity.ok(ApiResponse.ok(
                idempotencyService.getResult(idempotencyKey)));
    }

    OrderDto order = orderService.create(request);
    idempotencyService.store(idempotencyKey, order);
    return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.ok(order));
}
```

## Kết luận

Consistent response structure và proper error handling là nền tảng của một API tốt. Đầu tư vào đây ngay từ đầu sẽ tiết kiệm rất nhiều thời gian sau này.
""";

    private static final String POST5_CONTENT = """
# Database Performance với Spring Data JPA

N+1 problem là kẻ thù số một của các Spring Boot apps. Đây là cách tôi giải quyết nó.

## N+1 Problem

```java
// ❌ N+1: 1 query lấy users + N queries lấy orders của từng user
List<User> users = userRepository.findAll();
users.forEach(user -> {
    System.out.println(user.getOrders().size()); // Lazy load → N queries
});
```

## Giải pháp 1: JOIN FETCH

```java
@Query("SELECT u FROM User u LEFT JOIN FETCH u.orders WHERE u.active = true")
List<User> findActiveUsersWithOrders();
```

## Giải pháp 2: @EntityGraph

```java
@EntityGraph(attributePaths = {"orders", "profile"})
List<User> findByActiveTrue();
```

## Giải pháp 3: DTO Projection (tốt nhất)

```java
public interface UserSummary {
    Long getId();
    String getName();
    int getOrderCount();
}

@Query("SELECT u.id as id, u.name as name, COUNT(o) as orderCount " +
       "FROM User u LEFT JOIN u.orders o GROUP BY u.id, u.name")
List<UserSummary> findUserSummaries();
```

## Batch Size cho Collections

```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 100
```

Với `batch_fetch_size`, Hibernate gom N queries thành 1 query với `IN (id1, id2, ...)`.

## Query Logging để Debug

```yaml
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql: TRACE

spring:
  jpa:
    properties:
      hibernate:
        format_sql: true
        generate_statistics: true
```

## Index Strategy

```java
@Entity
@Table(name = "orders", indexes = {
    @Index(name = "idx_orders_user_status", columnList = "user_id, status"),
    @Index(name = "idx_orders_created_at", columnList = "created_at DESC")
})
public class Order { ... }
```

## Kết luận

Luôn bật SQL logging khi development. Một N+1 không được phát hiện có thể làm chậm app 100x trong production.
""";

    private static final String POST6_CONTENT = """
# Kafka Event-Driven Architecture trong Microservices

Event-driven architecture đã giúp team tôi giải quyết vấn đề coupling giữa các services. Đây là cách chúng tôi implement.

## Vấn đề với Synchronous Communication

```
Service A → HTTP → Service B → HTTP → Service C
```

Khi Service C down, cả chain bị fail. Coupling cao, latency tích lũy.

## Event-Driven Pattern

```
Service A → Kafka Topic → Service B (async)
                       → Service C (async)
                       → Service D (async)
```

## Event Design

```java
// Event contract - shared library
public record OrderCreatedEvent(
    String eventId,
    String eventType,
    Instant occurredAt,
    String orderId,
    String customerId,
    BigDecimal totalAmount
) {
    public static OrderCreatedEvent of(String orderId,
                                       String customerId,
                                       BigDecimal amount) {
        return new OrderCreatedEvent(
            UUID.randomUUID().toString(),
            "ORDER_CREATED",
            Instant.now(),
            orderId, customerId, amount
        );
    }
}
```

## Producer

```java
@Service
@Slf4j
public class OrderEventProducer {
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public void publish(OrderCreatedEvent event) {
        kafkaTemplate.send("orders.created", event.orderId(), event)
            .thenAccept(result ->
                log.info("Published event {} to partition {}",
                    event.eventId(),
                    result.getRecordMetadata().partition()))
            .exceptionally(ex -> {
                log.error("Failed to publish event {}", event.eventId(), ex);
                return null;
            });
    }
}
```

## Consumer với Idempotency

```java
@Service
@Slf4j
public class NotificationConsumer {
    private final ProcessedEventRepository processedEvents;
    private final NotificationService notificationService;

    @KafkaListener(topics = "orders.created", groupId = "notification-service")
    @Transactional
    public void handle(OrderCreatedEvent event) {
        // Idempotency check
        if (processedEvents.exists(event.eventId())) {
            log.debug("Skipping duplicate event {}", event.eventId());
            return;
        }
        notificationService.sendOrderConfirmation(event.customerId(), event.orderId());
        processedEvents.markProcessed(event.eventId());
    }
}
```

## Dead Letter Queue

```java
@Bean
public ConcurrentKafkaListenerContainerFactory<String, Object> kafkaListenerContainerFactory() {
    var factory = new ConcurrentKafkaListenerContainerFactory<String, Object>();
    factory.setCommonErrorHandler(
        new DefaultErrorHandler(
            new DeadLetterPublishingRecoverer(kafkaTemplate),
            new FixedBackOff(1000L, 3)  // Retry 3 lần, mỗi lần 1 giây
        )
    );
    return factory;
}
```

## Kết luận

Event-driven architecture giải quyết coupling nhưng tăng complexity (eventual consistency, ordering, idempotency). Chỉ áp dụng khi lợi ích vượt qua chi phí.
""";

    private final List<BlogPost> posts = List.of(
            BlogPost.builder()
                    .id("1").title("Virtual Threads trong Java 21 - Thay đổi game cho Backend Developer")
                    .slug("virtual-threads-java-21")
                    .excerpt("Khám phá Virtual Threads trong Java 21 và cách nó cách mạng hóa việc xây dựng ứng dụng backend hiệu suất cao với hàng triệu concurrent tasks.")
                    .coverImage("https://images.unsplash.com/photo-1555099962-4199c345e5dd?w=800&q=80")
                    .category("Java").tags(List.of("Java", "Concurrency", "Performance", "Java 21"))
                    .publishedAt(LocalDate.of(2025, 5, 20)).readTimeMinutes(8).featured(true)
                    .content(POST1_CONTENT).build(),

            BlogPost.builder()
                    .id("2").title("Clean Architecture trong Spring Boot: Xây dựng ứng dụng có thể mở rộng")
                    .slug("clean-architecture-spring-boot")
                    .excerpt("Áp dụng Clean Architecture vào Spring Boot project giúp code dễ test, dễ maintain và tách biệt business logic khỏi framework.")
                    .coverImage("https://images.unsplash.com/photo-1507238691740-187a5b1d37b8?w=800&q=80")
                    .category("Architecture").tags(List.of("Architecture", "Spring Boot", "Clean Code", "Design Patterns"))
                    .publishedAt(LocalDate.of(2025, 4, 15)).readTimeMinutes(10).featured(true)
                    .content(POST2_CONTENT).build(),

            BlogPost.builder()
                    .id("3").title("Docker Best Practices cho Java Applications")
                    .slug("docker-best-practices-java")
                    .excerpt("Tối ưu Docker image cho Spring Boot: multi-stage build, JVM tuning cho container, layer caching và health checks.")
                    .coverImage("https://images.unsplash.com/photo-1605745341112-85968b19335b?w=800&q=80")
                    .category("DevOps").tags(List.of("Docker", "Java", "DevOps", "Performance"))
                    .publishedAt(LocalDate.of(2025, 3, 10)).readTimeMinutes(7).featured(false)
                    .content(POST3_CONTENT).build(),

            BlogPost.builder()
                    .id("4").title("Xây dựng REST API chuẩn với Spring Boot")
                    .slug("restful-api-spring-boot")
                    .excerpt("Những pattern tôi luôn dùng khi xây dựng REST API: response structure nhất quán, global exception handling, versioning và idempotency.")
                    .coverImage("https://images.unsplash.com/photo-1558494949-ef010cbdcc31?w=800&q=80")
                    .category("Spring Boot").tags(List.of("Spring Boot", "REST API", "Java", "Best Practices"))
                    .publishedAt(LocalDate.of(2025, 2, 28)).readTimeMinutes(9).featured(false)
                    .content(POST4_CONTENT).build(),

            BlogPost.builder()
                    .id("5").title("Database Performance với Spring Data JPA")
                    .slug("database-performance-spring-data-jpa")
                    .excerpt("Giải quyết N+1 problem, tối ưu queries với DTO projections, batch fetching và index strategy trong Spring Data JPA.")
                    .coverImage("https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800&q=80")
                    .category("Database").tags(List.of("JPA", "Hibernate", "Performance", "Database"))
                    .publishedAt(LocalDate.of(2025, 1, 20)).readTimeMinutes(11).featured(false)
                    .content(POST5_CONTENT).build(),

            BlogPost.builder()
                    .id("6").title("Kafka Event-Driven Architecture trong Microservices")
                    .slug("kafka-event-driven-microservices")
                    .excerpt("Thiết kế event-driven system với Kafka: event contracts, idempotent consumers, dead letter queues và xử lý lỗi trong production.")
                    .coverImage("https://images.unsplash.com/photo-1451187580459-43490279c0fa?w=800&q=80")
                    .category("Microservices").tags(List.of("Kafka", "Microservices", "Event-Driven", "Spring Boot"))
                    .publishedAt(LocalDate.of(2024, 12, 5)).readTimeMinutes(12).featured(false)
                    .content(POST6_CONTENT).build()
    );

    private final List<Project> projects = List.of(
            Project.builder()
                    .id("1").title("Hayden Blog")
                    .description("Personal blog về Java và Spring Boot với tính năng Markdown rendering, dark mode, full-text search và SEO optimization.")
                    .image("https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=800&q=80")
                    .technologies(List.of("Spring Boot", "Thymeleaf", "Java 21", "Docker"))
                    .githubUrl("https://github.com/hieuhd/hayden").demoUrl("https://hayden.dev.vn").featured(true).build(),

            Project.builder()
                    .id("2").title("Order Management System")
                    .description("Hệ thống quản lý đơn hàng microservices với event-driven architecture, CQRS và event sourcing. Xử lý 10,000+ orders/giây.")
                    .image("https://images.unsplash.com/photo-1563986768609-322da13575f3?w=800&q=80")
                    .technologies(List.of("Spring Boot", "Kafka", "PostgreSQL", "Redis", "Kubernetes"))
                    .githubUrl("https://github.com/hieuhd/order-system").demoUrl("").featured(true).build(),

            Project.builder()
                    .id("3").title("Dev Utils CLI")
                    .description("Command-line tool cho developers: generate boilerplate code, manage env files, convert data formats và API testing.")
                    .image("https://images.unsplash.com/photo-1629654297299-c8506221ca97?w=800&q=80")
                    .technologies(List.of("Java", "Spring Shell", "GraalVM Native"))
                    .githubUrl("https://github.com/hieuhd/dev-utils").demoUrl("").featured(false).build(),

            Project.builder()
                    .id("4").title("Real-time Chat API")
                    .description("WebSocket-based chat API với Spring Boot, hỗ trợ rooms, direct messages, file sharing và message history với pagination.")
                    .image("https://images.unsplash.com/photo-1611746872915-64382b5c76da?w=800&q=80")
                    .technologies(List.of("Spring WebSocket", "Redis Pub/Sub", "MongoDB", "JWT"))
                    .githubUrl("https://github.com/hieuhd/chat-api").demoUrl("").featured(false).build()
    );

    private final List<Skill> skills = List.of(
            Skill.builder().name("Java").category("Backend").level(5).build(),
            Skill.builder().name("Spring Boot").category("Backend").level(5).build(),
            Skill.builder().name("Spring Security").category("Backend").level(4).build(),
            Skill.builder().name("Spring Data JPA").category("Backend").level(4).build(),
            Skill.builder().name("Apache Kafka").category("Backend").level(4).build(),
            Skill.builder().name("REST API Design").category("Backend").level(5).build(),
            Skill.builder().name("PostgreSQL").category("Database").level(4).build(),
            Skill.builder().name("Redis").category("Database").level(3).build(),
            Skill.builder().name("MongoDB").category("Database").level(3).build(),
            Skill.builder().name("Docker").category("DevOps").level(4).build(),
            Skill.builder().name("Kubernetes").category("DevOps").level(3).build(),
            Skill.builder().name("GitHub Actions").category("DevOps").level(4).build(),
            Skill.builder().name("AWS").category("DevOps").level(3).build(),
            Skill.builder().name("React").category("Frontend").level(3).build(),
            Skill.builder().name("TypeScript").category("Frontend").level(3).build(),
            Skill.builder().name("Thymeleaf").category("Frontend").level(4).build()
    );

    private final List<TimelineEntry> timeline = List.of(
            TimelineEntry.builder()
                    .period("2023 - Nay").title("Backend Engineer")
                    .organization("Kaopiz Software")
                    .description("Phát triển và maintain các hệ thống backend với Java Spring Boot. Thiết kế microservices architecture, tích hợp Kafka cho event-driven systems. Cải thiện performance 40% cho hệ thống xử lý đơn hàng.")
                    .type("work").build(),
            TimelineEntry.builder()
                    .period("2022 - 2023").title("Junior Backend Developer")
                    .organization("Startup ABC")
                    .description("Xây dựng REST APIs với Spring Boot, tích hợp payment gateway (VNPay, Momo). Làm việc với PostgreSQL, Redis caching. Viết unit tests và integration tests.")
                    .type("work").build(),
            TimelineEntry.builder()
                    .period("2018 - 2022").title("Cử nhân Công nghệ Thông tin")
                    .organization("Đại học Bách Khoa Hà Nội")
                    .description("Chuyên ngành Kỹ thuật phần mềm. Tốt nghiệp loại Khá. Đề tài tốt nghiệp: Xây dựng hệ thống gợi ý sản phẩm sử dụng Machine Learning với độ chính xác 87%.")
                    .type("education").build()
    );

    public List<BlogPost> getAllPosts() {
        return posts;
    }

    public List<BlogPost> getRecentPosts(int count) {
        return posts.stream()
                .sorted((a, b) -> b.getPublishedAt().compareTo(a.getPublishedAt()))
                .limit(count)
                .toList();
    }

    public Optional<BlogPost> findBySlug(String slug) {
        return posts.stream().filter(p -> p.getSlug().equals(slug)).findFirst();
    }

    public List<BlogPost> getRelatedPosts(BlogPost current, int count) {
        return posts.stream()
                .filter(p -> !p.getId().equals(current.getId()))
                .filter(p -> p.getCategory().equals(current.getCategory())
                        || p.getTags().stream().anyMatch(current.getTags()::contains))
                .limit(count)
                .toList();
    }

    public List<BlogPost> searchPosts(String query) {
        if (query == null || query.isBlank()) return posts;
        String q = query.toLowerCase();
        return posts.stream()
                .filter(p -> p.getTitle().toLowerCase().contains(q)
                        || p.getExcerpt().toLowerCase().contains(q)
                        || p.getCategory().toLowerCase().contains(q))
                .toList();
    }

    public List<Project> getProjects() {
        return projects;
    }

    public List<Skill> getSkills() {
        return skills;
    }

    public List<TimelineEntry> getTimeline() {
        return timeline;
    }
}
