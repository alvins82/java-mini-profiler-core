package au.com.funkworks.jmp;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import au.com.funkworks.jmp.MiniProfiler.Step;

@Aspect
public class MiniProfilerAspect {

	//@Pointcut(value="execution(public * *(..))")
	//@Pointcut("execution(* au.com.funkworks.fc..*.*(..))")
	@Pointcut(value="execution(public * *(..))")
	public void anyPublicMethod() {
	}
 
	//@Around("execution(* au.com.truelocal.service.BusinessManagerService.*(..))")
	@Around("anyPublicMethod() && @annotation(profile)")
	public Object profile(ProceedingJoinPoint pjp, Profile profile) throws Throwable {		
		Step step = MiniProfiler.step(profile.description());
		try {
			return pjp.proceed();
		} finally {
		  step.close();
		}		
	}
	
}