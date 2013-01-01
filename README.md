# Java Mini Profiler

JMP is a mini-profiler for Java inspired by [mvc-mini-profile](http://miniprofiler.com/) (and Jeff Atwood's [blog post](http://www.codinghorror.com/blog/2011/06/performance-is-a-feature.html)\).

![ScreenShot](https://raw.github.com/alvins82/java-mini-profiler-core/master/screenshot1.png)

## Quick start

1\. Add the following repo/dependencies to your maven pom -

```xml
	<dependency>
		<groupId>au.com.funkworks.jmp</groupId>
		<artifactId>java-mini-profiler-web</artifactId>
		<version>0.7</version>
	</dependency>	

	<repository>
    	<id>alvins-releases</id>
    	<url>https://github.com/alvins82/mvn-repo/raw/master/releases</url>
	</repository>
```

2\. Add to your web.xml the servlet and filter.

```xml
	<servlet>
		<servlet-name>miniprofiler</servlet-name>
		<servlet-class>au.com.funkworks.jmp.MiniProfilerServlet</servlet-class>		
	</servlet>
	<servlet-mapping>
		<servlet-name>miniprofiler</servlet-name>
		<url-pattern>/java_mini_profile/*</url-pattern>
	</servlet-mapping>
	
	<filter>
		<filter-name>miniprofiler-filter</filter-name>		 
		<filter-class>au.com.funkworks.jmp.MiniProfilerFilter</filter-class>				
	</filter>
	<filter-mapping>
		<filter-name>miniprofiler-filter</filter-name>
		<url-pattern>/*</url-pattern>
	</filter-mapping>
```

3\. Add somewhere in your jsp. 

```
	${mini_profile_includes}
```

4\. Add some code to profile using the syntax below.

```java

	Step step = MiniProfiler.step("Some things happening");
	...	
	// do some work	
	...
	step.close();
```

5\. Load your web-application and watch the profile in the top left corner as shown below. 

### Optional

**SQL Profiling**

This allows you to see how long your queries are taking and the actual query itself. This is implemented using a JDBC data-source proxy.

1\. Include the following in your maven to include the sql dependency.

```xml
	<dependency>
		<groupId>au.com.funkworks.jmp</groupId>
		<artifactId>java-mini-profiler-sql</artifactId>
		<version>0.7</version>
	</dependency>	
```

2\. Configure the original dataSource so it is wrapped by au.com.funkworks.jmp.SqlRecordDataSource. Example shown below. 

```xml
	<jee:jndi-lookup id="dataSourceActual" jndi-name="java:comp/env/jdbc/dataSource" />

   	<bean id="dataSourceProxy" class="au.com.funkworks.jmp.SqlRecordDataSource">
    	<constructor-arg ref="dataSourceActual" />
  	</bean>
  	 
	<bean id="dataSource" class="org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy">
        <property name="targetDataSource">
            <ref bean="dataSourceProxy" />
        </property>
    </bean>
```

**JSP Profiling**

It is often beneficial to profile around jsp statements. A good example is when you are using Spring's Open Session In View (OSIV) pattern.

1\. Include the following taglib in your JSP.

```xml
	<%@ taglib prefix="profiler" uri="http://www.funkworks.com.au/tags/jmp" %>
```

2\. Use the jmp tag to profile around bits of JSP code.

```xml
<profiler:jmp description="Profiling some jsp code">

jsp stuff here...

</profiler:jmp>
```

**AOP Public Method Profiling**

The real power of JMP comes alive with the use of aspect oriented programming. To enable AOP profiling ensure aop is enabled.

There are two options available.

1\. The first options is to profile around all public methods of your project - this is the most powerful option and gives the most benefit.

To use it - create an aspect class in your project - an example is shown below. 

```java
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import au.com.funkworks.jmp.MiniProfiler;
import au.com.funkworks.jmp.Step;

@Aspect
@Component
public class JMPFullAspect {

	@Pointcut(value="execution(public * *(..))")
	public void anyPublicMethod() {
	}
	
	@Pointcut("within(your.package..*)")
    private void inYourPackage() {}
 
	@Around("anyPublicMethod() && inYourPackage()")
	public Object profile(ProceedingJoinPoint pjp) throws Throwable {
		String desc = pjp.getSignature().toShortString();
		if (desc.endsWith("()")) {
			desc = desc.substring(0, desc.length()-2);
		} else if (desc.endsWith("(..)")) {
			desc = desc.substring(0, desc.length()-4);
		}
		
		String packageName = pjp.getSignature().toLongString().split(" ")[2];
		String tag = null;
		
		// optional tag for roll-up of timings e.g. around dao layer
		if (packageName.startsWith("your.package.dao")) {
			tag = "DAO Layer";
		}
		
		try (Step step = MiniProfiler.step(desc, tag);) {			
			return pjp.proceed();
		}
	}
}
```

You also need to enable AOP if not already enabled.

```xml
	<aop:aspectj-autoproxy proxy-target-class="true"/>
```
 
2\. Another option is to use the @Profiler annotation - simply annotate your method as below.

```java
	@Profiler
	public void yourMethod() {
		// do some things
	}
```

You also need to enable this aspect by including a spring context in your web.xml.

```xml
	<context-param>
		<param-name>contextConfigLocation</param-name>
		<param-value>
			...other contexts
			classpath:au/com/funkworks/jmp/applicationContext-jmp.xml							
		</param-value>
	</context-param>
```

## Notes

- If **not** using jsp/jstl - the 'mini_profile_includes' attribute is set on request object via setAttribute)
- jQuery is required to be loaded before including '${mini_profile_includes}' in your JSP.