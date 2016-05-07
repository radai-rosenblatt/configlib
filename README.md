# Confusion - An Object-first configuration library for java
### Briefly
Suppose your application has configurable bits. suppose also that you would like to express your configuration as an object:
```java
public class AppConfig {
    private int numThreads;
    private int listenOnPort;
    //getters, setters, constructors, the usual.
}
```
Confusion allows you to create a Configuration Service:
```java
ConfigurationService<AppConfig> srv = Confusion.create(...);
```
through which you could read the latest configuration (which updates when the underlying storage does):
```java
AppConfiguration latest = srv.getConfiguration();
```
or register to be notified of changes:
```java
srv.register(changeEvent -> {
    AppConfig prev = changeEvent.getOldConf();
    AppConfig curr = changeEvent.getNewConf();
    if (curr.numThreads != prev.numThreads) {
        //resize the thread pool
    }
});
```
If you're using Spring, your life could become even easier:
```java
@Named
public class SomeSpringBean {
    @Inject
    private ConfigurationService<AppConfig> confService;
    @Inject
    private AppConfig initialConf;

    @EventListener
    public void configChanged(ConfigurationChangeEvent<AppConfig> evt) {
        //do something
    }
}
```
Configuration can be stored as a local INI file:
```java
srv = Confusion.create(AppConfig.class, new PathStore("/some/file.ini"), new IniCodec());
```
or as XML pulled from consul under some key:
```java
srv = Confusion.create(AppConfig.class, new ConsulStore("localhost", 8500, "key"), new JaxbCodec());
```
or any other combination of Store and Codec

### More Detail

Currently supported Stores:
* In-mem
* Path
* Etcd
* Consul
* Kafka

Currently supported Codecs:
* Ini
* Jaxb
* Serializable

You could also provide your own implementation of a Store/Codec.

### How to use in your own project

you need the jitpack.io repository defined in your pom:
```xml
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
        <releases>
            <enabled>true</enabled>
        </releases>
        <snapshots>
            <enabled>true</enabled>
        </snapshots>
    </repository>
</repositories>
```
and then you could depend on just the bits and pieces you need:
```xml
<dependency>
    <groupId>com.github.radai-rosenblatt.confusion</groupId>
    <artifactId>confusion-core</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
...
<dependency>
    <groupId>com.github.radai-rosenblatt.confusion</groupId>
    <artifactId>confusion-ini</artifactId>
    <version>master-SNAPSHOT</version>
</dependency> 
```

### Notes on building locally

must use `mvn clean install` without skipping tests the 1st time you build, since child modules depend on the core module's test classes, and jitpack doesnt publish the tests artifact for some odd reason.