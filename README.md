# simple-netty-test

To run this sample:

1) `mvn clean install`
2) `cd child-module`
3) `mvn package -Pnative`

Java version from sdkman:
```
openjdk version "17.0.3" 2022-04-19
OpenJDK Runtime Environment GraalVM CE 22.1.0 (build 17.0.3+7-jvmci-22.1-b06)
OpenJDK 64-Bit Server VM GraalVM CE 22.1.0 (build 17.0.3+7-jvmci-22.1-b06, mixed mode, sharing)
```

---- Experiment with netty and slf4j-simple ------

Using just netty and slf4j-simple results in the following error:

```
Fatal error: com.oracle.graal.pointsto.util.AnalysisError$ParsingError: Error encountered while parsing io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel.<init>(io.grpc.netty.shaded.io.netty.channel.Channel, java.nio.channels.SelectableChannel, int) 
Parsing context:
   at io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioChannel.<init>(AbstractNioChannel.java:80)
   at io.grpc.netty.shaded.io.netty.channel.nio.AbstractNioMessageChannel.<init>(AbstractNioMessageChannel.java:42)

	at com.oracle.graal.pointsto.util.AnalysisError.parsingError(AnalysisError.java:141)
	at com.oracle.graal.pointsto.flow.MethodTypeFlow.createTypeFlow(MethodTypeFlow.java:315)
	at com.oracle.graal.pointsto.flow.MethodTypeFlow.ensureTypeFlowCreated(MethodTypeFlow.java:286)
	at com.oracle.graal.pointsto.flow.MethodTypeFlow.addContext(MethodTypeFlow.java:107)
	at com.oracle.graal.pointsto.DefaultAnalysisPolicy$DefaultSpecialInvokeTypeFlow.onObservedUpdate(DefaultAnalysisPolicy.java:364)
	at com.oracle.graal.pointsto.flow.TypeFlow.notifyObservers(TypeFlow.java:487)
	at com.oracle.graal.pointsto.flow.TypeFlow.update(TypeFlow.java:556)
	at com.oracle.graal.pointsto.PointsToAnalysis$2.run(PointsToAnalysis.java:598)
	at com.oracle.graal.pointsto.util.CompletionExecutor.executeCommand(CompletionExecutor.java:195)
	at com.oracle.graal.pointsto.util.CompletionExecutor.lambda$executeService$0(CompletionExecutor.java:179)
	at java.base/java.util.concurrent.ForkJoinTask$RunnableExecuteAction.exec(ForkJoinTask.java:1395)
	at java.base/java.util.concurrent.ForkJoinTask.doExec(ForkJoinTask.java:373)
	at java.base/java.util.concurrent.ForkJoinPool$WorkQueue.topLevelExec(ForkJoinPool.java:1182)
	at java.base/java.util.concurrent.ForkJoinPool.scan(ForkJoinPool.java:1655)
	at java.base/java.util.concurrent.ForkJoinPool.runWorker(ForkJoinPool.java:1622)
	at java.base/java.util.concurrent.ForkJoinWorkerThread.run(ForkJoinWorkerThread.java:165)
Caused by: com.oracle.graal.pointsto.constraints.UnsupportedFeatureException: No instances of org.slf4j.impl.SimpleLogger are allowed in the image heap as this class should be initialized at image runtime. To see how this object got instantiated use --trace-object-instantiation=org.slf4j.impl.SimpleLogger.
	at com.oracle.svm.hosted.classinitialization.ClassInitializationFeature.checkImageHeapInstance(ClassInitializationFeature.java:133)
	at com.oracle.graal.pointsto.meta.AnalysisUniverse.replaceObject(AnalysisUniverse.java:575)
	at com.oracle.svm.hosted.ameta.AnalysisConstantReflectionProvider.replaceObject(AnalysisConstantReflectionProvider.java:217)
	at com.oracle.svm.hosted.ameta.AnalysisConstantReflectionProvider.interceptValue(AnalysisConstantReflectionProvider.java:188)
	at com.oracle.svm.hosted.ameta.AnalysisConstantReflectionProvider.readValue(AnalysisConstantReflectionProvider.java:102)
```

**Note** The slf4j dependency requires some configurations to be compatible with netty. Specifically `--initialize-at-build-time=org.slf4j.impl.SimpleLogger,org.slf4j.impl.StaticLoggerBinder,org.slf4j.LoggerFactory`. However, adding these configurations results in a conflict with Slf4LoggerFactory:

```
Error: Classes that should be initialized at run time got initialized during image building:
 io.grpc.netty.shaded.io.netty.util.internal.logging.Slf4JLoggerFactory the class was requested to be initialized at run time 
```

------ Experiment with removing  ----------

Using a SNAPSHOT version of gax-grpc which doesn't initialize Slf4jLoggerFactory and Log4JLogger at runtime results in a successful build with some additional slf4j configurations. 


SOLUTION: initializing these logging classes at run-time is incompatible when slf4j is present on the classpath, hence, is not a good solution to the resolving the following warning: `Warning: class initialization of class io.grpc.netty.shaded.io.netty.util.internal.logging.Slf4JLoggerFactory failed with exception java.lang.NoClassDefFoundError: Could not initialize class io.grpc.netty.shaded.io.netty.util.internal.logging.Slf4JLoggerFactory. This class will be initialized at run time because option --allow-incomplete-classpath is used for image building. Use the option --initialize-at-run-time=io.grpc.netty.shaded.io.netty.util.internal.logging.Slf4JLoggerFactory to explicitly request delayed initialization of this class.
`

