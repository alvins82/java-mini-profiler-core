# Java Mini Profiler

JMP is a mini-profiler for Java inspired by [mvc-mini-profile](http://miniprofiler.com/) (and Jeff Atwoods [blog post](http://www.codinghorror.com/blog/2011/06/performance-is-a-feature.html)\).

## Quick start

1. Add the following repo/dependencies to your maven pom -

```xml
	<dependency>
		<groupId>au.com.funkworks.jmp</groupId>
		<artifactId>java-mini-profiler-web</artifactId>
		<version>0.6</version>
	</dependency>	

	<repository>
    	<id>alvins-releases</id>
    	<url>https://github.com/alvins82/mvn-repo/raw/master/releases</url>
	</repository>
```

2. Add to your web.xml the servlet and filter.

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

3. Add somewhere in your jsp (if not using jsp/jstl - the 'mini_profile_includes' attribute is set on request object via setAttribute) 

```
	${mini_profile_includes}
```

4. Add some code to profile using the syntax below.

```java

	Step step = MiniProfiler.step("Some things happening");
	...	
	// do some work	
	...
	step.close();
```

5. Load your web-application and watch the profile in the top left corner as shown below. 

### Optional

*SQL Profiling*

*JSP Profiling*

*AOP Public Method Profiling*

## Features



## Dependencies