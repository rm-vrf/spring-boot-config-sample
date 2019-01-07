# spring-boot-config-sample

## 原则

软件需要在不同的环境中部署，代码是保持不变的，但是不同的运行环境存在差异，所以需要使用配置适应不同的环境。比如：

- 数据库，Redis，以及其他 后端服务 的配置；
- 第三方服务的证书，如 oAuth、支付接口 等；
- 每份部署特有的配置，如域名等。

配置的原则是：代码与配置要严格分离，不允许在代码中使用常量保存配置。

最常见的配置方式就是配置文件，按照配置文件的存储位置，可以分为内部配置和外部配置：

- 内部配置：部署文件是发布产物的一部分，存储在发布目录中，甚至打包在一起，比如 jar 包里面的 properties 文件。这些配置文件在不同的环境间不存在差异，其实他们可以看做是部署产物的一部分，比如 Spring 用来定义类创建和注入关系的 xml 文件。从运维的角度来看，尽管他们是配置文件，实际上和写死代码没有什么区别。反过来说，如果某个配置项在不同环境中是有差异的，就不应该放到内部；
- 外部配置：不在部署产物内，需要在部署环境上修改。需要注意不要把这样的文件提交到代码控制系统，总是有人不小心这样做。

## 配置漂移

构建好的程序被部署在服务器上后，为了解决故障、性能优化、适应新的需求，需要对服务器和应用的配置进行更改。如果直接登录服务器修改某个配置，随着时间的推移和管理的复杂化，就会引发配置漂移。

这种可以直接登录修改的服务器称为可变服务器。可变服务器会造成开发、测试、生产服务器不一致，生产环境中不同的节点也不一致，容易出现运行问题。

要防止配置漂移，服务器要禁止手动修改，只能通过自动化部署形式更改配置，这种服务器就叫不可变服务器。不可变服务器消除了不一致性，开发、测试环境中得到的程序包和最终到达服务器的程序包是完全相同的。这样就能防止配置漂移。

## 推荐方式

生产环境使用以下几配置种技术：

- 配置文件：使用外部配置，按照 Linux 的目录规范在 `/etc` 目录，或者其他合适的位置；
- 启动参数：在启动参数中注入配置数据，这是防止配置漂移的好办法；
- 环境变量：与启动参数相似，也是推荐的方式；
- 配置服务：使用某种集中配置平台，比如 etcd、Zookeeper、Spring Cloud Config. 这些平台使用不同的协议，在数据结构、存储一致性方面有不同的设计思想，可以选择一个合适的。配置服务可以支持动态修改，比如 Spring Cloud Config 提供了 `refresh` 端口，可以调动这个端口在不重启进程的情况下修改配置项。

## Spring Boot 配置方式

示例程序演示了 Spring Boot 配置方式，打包运行：

```shell
mvn package

export message=bonjour

java -Dmessage=hello \
    -jar spring-boot-config-sample-1.0.0-SNAPSHOT.jar \
    --message=hi \
    --spring.config.name=application,conf
```

`message` 配置出现在 4 个位置：

- 系统环境变量 `export message=bonjour`
- Java 属性 `-Dmessage=hello`
- 命令行参数 `--message=hi`
- 内部配置文件 `application.properties`

启动之后在 `env` 端口查看设置：

```shell
$ curl http://localhost:8080/actuator/env
{
    "propertySources":
    [
        {
            "name": "commandLineArgs",
            "properties":
            {
                "message":
                {
                    "value": "hi"
                }
            }
        },
        {
            "name": "systemProperties",
            "properties":
            {
                "message":
                {
                    "value": "hello"
                }
            }
        },
        {
            "name": "systemEnvironment",
            "properties":
            {
                "message":
                {
                    "value": "bonjour",
                    "origin": "System Environment Property \"message\""
                }
            }
        },
        {
            "name": "applicationConfig: [classpath:/application.properties]",
            "properties":
            {
                "message":
                {
                    "value": "nihao",
                    "origin": "class path resource [application.properties]:9:9"
                }
            }
        }
    ]
}
```

这里删掉了一些无关的内容，可以看到 `message` 设置是按照 `commandLineArgs`、`systemProperties`、`systemEnvironment`、`applicationConfig` 的顺序加载的，以最先出现的为准。

`spring.config.name` 启动参数定义了配置文件的名称。Spring Boot 默认的配置文件是 `application.properties`，这里添加了一个 `conf.properties`。

Spring Boot 按照特定的顺序加载配置项，位置和顺序如下：

1. `DevTool` 定义的配置项
2. `@TestPropertySource` 标签定义的配置项
3. `@SpringBootTest#properties` 标签定义的配置项
4. 启动参数
5. `SPRING_APPLICATION_JSON` 环境变量
6. `ServletConfig` 定义的 `init` 参数
7. `ServletContext` 定义的 `init` 参数
8. JNDI 属性
9. Java 系统属性，使用 `-Dkey=value` 定义，`System.getProperties()` 可以查看到
10. 操作系统环境变量
11. `RandomValuePropertySource` 定义的随机值
12. 带 `profile` 的外部配置文件
13. 带 `profile` 的内部配置文件
14. 不带 `profile` 的外部配置文件
15. 不带 `profile` 的内部配置文件
16. `@Configuration` 类型里面的 `@PropertySource` 标签定义的配置
17. `SpringApplication.setDefaultProperties` 方法设置的默认值

内外部配置文件的加载位置和顺序如下：

1. `config` 目录
2. `.` 当前目录
3. `classpath:/config`
4. `classpath:/`

## Docker

使用 `Dockerfile` 是在生产环境创建 Docker 镜像的唯一推荐方式，示例程序提供了 `Dockerfile` 样例。`Dockerfile` 将运行包和配置文件复制到镜像里：

```shell
RUN mkdir -p /opt/stack

COPY target/spring-boot-config-sample-1.0.0-SNAPSHOT.jar /opt/stack/
COPY ./conf.properties /opt/stack/
```

在环境变量和启动参数中注入配置项：

```shell
ENV message=bonjour

ENTRYPOINT ["java", \
    "-jar", \
    "spring-boot-config-sample-1.0.0-SNAPSHOT.jar", \
    "--spring.config.name=application,conf"]
```

定义了健康检查规则：

```shell
HEALTHCHECK --interval=30s --timeout=10s \
    CMD curl -f http://localhost:8080/actuator/health || exit 1
```

`Dockerfile` 里面还对镜像系统进行了设置，提高了打开文件句柄数。Docker 是实现不可变服务器的最佳方式。

```shell
RUN ulimit -n 65536
```

使用 `docker build` 命令制作镜像，第一次运行会下载一个 `primetoninc/jdk` 基础镜像，需要花一些时间：

```shell
docker build -t config-sample .
```

使用 `docker run` 命令运行程序：

```shell
docker run -it -p 8080:8080 config-sample
```

在实际环境上可以使用 Kubernetes、Mesos 这样的平台管理和运行 Docker 镜像，完全可以避免直接登录服务器操作。
