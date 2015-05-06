---
layout: doc
title:  Async RMI in 10 Minutes
date:   2014-11-01 15:40:56
categories: introduction
---

##Prerequisites

- Oracle [JDK8](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
- [maven](http://maven.apache.org/).

##Installing Async RMI with Maven dependency

```xml
    <dependency>
      <groupId>com.github.barakb</groupId>
      <artifactId>asyncrmi</artifactId>
      <version>1.0</version>
    </dependency>
```

##Installing from ZIP
Download the [zip](https://github.com/barakb/asyncrmi/archive/master.zip) or the [tar.gz](https://github.com/barakb/asyncrmi/archive/master.tar.gz) file
And open it where continence.

##Installing with GitHub
Clone the project `git clone git@github.com:barakb/asyncrmi.git`

##Compiling the sources.
From within the project dir type `mvn install`
This will install the Async RMI jar and its dependencies on your local machine.

##Creating your first Async RMI project.

- Create the project folder: `mkdir my-first-async-project` and cd to my-first-async-project dir.
- Create `pom.xml` file with the following content:

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
  xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
  <modelVersion>4.0.0</modelVersion>

  <groupId>org.my.app</groupId>
  <artifactId>my-app</artifactId>
  <version>1.0-SNAPSHOT</version>
  <packaging>jar</packaging>

  <name>My First Async RMI Project</name>
  <url>http://your-project-home-page</url>
  <properties>
       <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
       <version>0.1-SNAPSHOT</version>
       <slf4jVersion>1.7.7</slf4jVersion>
       <log4jVersion>1.2.17</log4jVersion>
  </properties>

  <dependencies>
        <dependency>
            <groupId>com.github.barakb</groupId>
            <artifactId>asyncrmi</artifactId>
            <version>1.0</version>
        </dependency>
        <dependency>
            <groupId>org.slf4j</groupId>
            <artifactId>slf4j-log4j12</artifactId>
            <version>${slf4jVersion}</version>
        </dependency>
        <dependency>
            <groupId>log4j</groupId>
            <artifactId>log4j</artifactId>
            <version>${log4jVersion}</version>
        </dependency>
  </dependencies>
</project>
```

- Create source directory `src/main/java` with those 3 files.

```java
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;

public interface Server extends Remote {
    CompletableFuture<Integer> asyncReadAfterDelay(long millis);
}
```

```java
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import org.async.rmi.Util;

public class ServerIml implements Server {
    @Override
    public CompletableFuture<Integer> asyncReadAfterDelay(long millis) {
        CompletableFuture<Integer> res = new CompletableFuture<>();
        new Thread(() -> res.complete(readAfterDelay(millis))).start();
        return res;
    }

   private Integer readAfterDelay(long millis) {
        try {
            Thread.sleep(millis);
        } catch (Exception ignored) {
        }
        return 1;
    }
    public static void main(String [] ags) throws Exception{
         Server server = new ServerIml();
         Util.writeToFile(server, new File("server.ser"));
         Thread.sleep(Long.MAX_VALUE);
    }
}
```


```java
import java.rmi.RemoteException;
import java.util.concurrent.CompletableFuture;
import org.async.rmi.Util;

public class Client{
    public static void main(String [] ags) throws Exception{
         Server server = (Server) Util.readFromFile(new File("server.ser"));
         server.asyncReadAfterDelay().get();
    }
}
```
- compile with mvn install.
- run from your idea.

## How to build the examples.
- After building Async RMI from the Async RMI dir type `(cd example; mvn install)`

