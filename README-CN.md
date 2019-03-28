# Jugg
![](https://img.shields.io/crates/l/e.svg) ![](https://img.shields.io/badge/build-pass-brightgreen.svg) ![](https://img.shields.io/badge/language-java-yellowgreen.svg)

![Juggernaut](./docs/Juggernaut.jpg)

`jugg` 是一个 C/S 架构的 `OGNL` 解析器，你可以使用 `jugg` 体验到和 `Python REPL` 类似的 `Java REPL` 体验。

## 添加依赖

```
<dependency>
    <groupId>com.xhinliang</groupId>
    <artifactId>jugg</artifactId>
    <version>2.0.7</version>
    <exclusions>
        <exclusion>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-jdk14</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```

## 核心概念

下面以一个简单的 `Spring Boot` 工程来介绍 `Jugg` 的核心概念。

### 核心流程
![core](docs/jugg.png "ognl core sequence")

### IBeanLoader

`IBeanLoader` 接口定义了加载一个类（我们后面称为 `Class` ）或者一个对象（我们后面称为 `Bean` ）的方法。

通过 `IBeanLoader`，我们可以自由控制 `OGNL` 解析器的类加载和对象加载的逻辑，从而获得更好的操作体验。

我们提供了一个 `IBeanLoader` 的半实现 `FlexibleBeanLoader`，在 `FlexibleBeanLoader` 中，默认加载了当前 `ClassPath` 内的所有类。通过使用 `FlexibleBeanLoader`，在大部分情况下你可以节省键入类全名的时间。

示例：
``` java
public static void main(String[] args) {
    ConfigurableApplicationContext configurableApplicationContext = new SpringApplication(MainApp.class).run(args);

    // jugg threads.
    IBeanLoader beanLoader = new FlexibleBeanLoader() {

        @Override
        protected Object getActualBean(String name) {
            return configurableApplicationContext.getBean(name);
        }

        @Override
        public Object getBeanByClass(@Nonnull Class<?> clazz) {
            return configurableApplicationContext.getBean(clazz);
        }
    };

    JuggEvalKiller evalKiller = new JuggEvalKiller(beanLoader);

    List<IJuggInterceptor> handlers = Lists.newArrayList(//
            new JuggLoginHandler((username, password) -> password.equals(USER_PASSWORD.get(username))), //
            new JuggAliasHandler(beanLoader), //
            new JuggCheckHandler(commandContext -> true), //
            new JuggEvalHandler(evalKiller) //
    );

    JuggWebSocketServer webSocketServer = new JuggWebSocketServer(JUGG_PORT, handlers, MainApp::getResourceAsFile);
    webSocketServer.startOnNewThread();
}
```

### JuggCommandContext
`JuggCommandContext` 是一次请求的抽象，包含用户信息，`command` 内容（`Client` 发送过来的文本）

### IJuggEvalKiller

`IJuggEvalKiller` 决定如何将一个 `JuggCommandContext` 执行。

``` java
public interface IJuggEvalKiller {

    /**
     * Eval a command and get the value.
     * @param commandContext command & context from client.
     * @return eval result.
     */
    Object eval(CommandContext commandContext);
}
```

### JuggWebSocketServer

`JuggWebSocketServer` 是核心的 `WebSocket` 服务器。

``` java
JuggWebSocketServer webSocketServer = new JuggWebSocketServer(JUGG_PORT, handlers);
webSocketServer.startOnNewThread();
```

### OGNL

OGNL 语法总体来说跟正常的 Java 语法类似，但是有一些不一样的地方，具体可以参考一下官方文档。


[OGNL语法指南](https://commons.apache.org/proper/commons-ognl/language-guide.html)

### More OGNL

`jugg` 使用 `IBeanLoader` 扩展了 `OGNL`，你可以更方便地使用它。

#### 自动加载 Bean

你可以通过 `IBeanLoader` 自定义 Bean 的加载方法，从而让 `jugg` 能自动加载 Bean。
例如，你可以结合 `Spring` 来定义加载逻辑。
``` java
ConfigurableApplicationContext configurableApplicationContext = new SpringApplication(MainApp.class).run(args);

IBeanLoader beanLoader = new IBeanLoader() {

    @Nullable
    @Override
    public Object getBeanByName(String name) {
        try {
            return configurableApplicationContext.getBean(name);
        } catch (NoSuchBeanDefinitionException catchE) {
            return null;
        }
    }
    @Nullable
    @Override
    public Object getBeanByClass(@Nonnull Class<?> clazz) {
        try {
            return configurableApplicationContext.getBean(clazz);
        } catch (NoSuchBeanDefinitionException e) {
            return null;
        }
    }
    // 省略...
};
```

``` bash
# userService 是一个 Spring bean
# 你可以直接使用它，因为 Jugg 会委托 Spring 将它初始化
> userService.getById(123L)
{
   "id": 123,
   "name": test
}
```

#### 使用 SimpleClassName 调用静态方法

`OGNL` 调用静态方法时需要制定类全名，这里可以通过 `IBeanLoader` 来使用 SimpleClassName 替代
``` java
// 加载所有 ClassLoader 下的 Class 到 Map 中，存储格式 map(simpleClassName -> fullyQualifiedClassName)
Map<String, String> clazzMap = new HashMap<>();
Configuration configuration = new ConfigurationBuilder() //
        .setUrls(Stream.of(ClasspathHelper.forPackage("com"), ClasspathHelper.forPackage("org"), ClasspathHelper.forPackage("net")) //
                .flatMap(Collection::stream) //
                .collect(Collectors.toSet())) //
        .setScanners(new SubTypesScanner(false));

Reflections reflections = new Reflections(configuration);
reflections.getAllTypes().forEach(s -> {
    String simpleClassName = s.substring(s.lastIndexOf(".") + 1);
    clazzMap.putIfAbsent(simpleClassName, s);
});

IBeanLoader beanLoader = new IBeanLoader() {

    @Nonnull
    @Override
    public Class<?> getClassByName(String name) throws ClassNotFoundException {
        try {
            return Class.forName(name);
        } catch (ClassNotFoundException classNotFound) {
            String retryClassName = clazzMap.getOrDefault(name, name);
            return Class.forName(retryClassName);
        }
    }
    // 省略
};
```

``` bash
> @Maps@newHashMap()
{ }
```

#### 别名

通过 `alias` 命令，你可以自定义 Class 或者 Bean 的别名：

``` bash
> alias target M Maps
done

> @M@newHashMap()
{ }
```

### 关于客户端

`jugg` 通过 `WebSocket` 协议，使用了纯文本 REPL 的方式，所以理论上任何的 `WebSocket` 客户端都能直接使用，甚至可以直接使用 `wsdump.py`（请自行Google）。

现在实现了两套客户端
- `web` React 实现的 Web 客户端，通过浏览器访问指定的端口就能使用。
- `node-client` Node.js 实现的命令行客户端。

### License

```
MIT License

Copyright (c) 2019 XhinLiang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.

```
