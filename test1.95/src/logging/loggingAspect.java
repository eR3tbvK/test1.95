package logging;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

@Aspect
public class loggingAspect {
	@Before("execution(public void duplicateUserMsg(..))")
	public void duplicateAdvice(){
		System.out.println("duplicate username attempted");
	}
}
