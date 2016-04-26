# Confusion - An Object-first configuration library for java
### Briefly
Suppose your application has configurable bits. suppose also that you would like to express your configuration as an object:
```java
public class AppConfiguration {
    private int numThreads;
    private int listenOnPort;
    //getters, setters, constructors, the usual.
}
```
Confusion allows you to create a Configuration Service:
```java
ConfigurationService<AppConfiguration> srv = Confusion.create(...);
```
through which you you could read the latest configuration (which updates when the underlying storage does):
```java
AppConfiguration latest = srv.getConfiguration();
```
or register to be notified of changes:
```java
srv.register(changeEvent -> {
    AppConfiguration prev = changeEvent.getOldConf();
    AppConfiguration curr = changeEvent.getNewConf();
    if (curr.numThreads != prev.numThreads) {
        //resize the thread pool
    }
});
```
If youre using Spring, your life could become even easier:
```java
@Named
public class SomeSpringBean {
    @Inject
    private ConfigurationService<AppConfiguration> confService;
    @Inject
    private AppConfiguration initialConf;

    @EventListener
    public void configChanged(ConfigurationChangeEvent<AppConfiguration> event) {
        //do something
    }
}
```
Configuration can be stored as a local INI file:
```java
srv = Confusion.create(AppConfiguration.class, new PathStore("/some/file.ini"), new IniCodec());
```
or as XML pulled from consul under some key:
```java
srv = Confusion.create(AppConfiguration.class, new ConsulStore("localhost", 8500, "someKey"), new JaxbCodec());
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