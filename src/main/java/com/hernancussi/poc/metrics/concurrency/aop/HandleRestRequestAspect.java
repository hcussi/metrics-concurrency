package com.hernancussi.poc.metrics.concurrency.aop;

import com.hernancussi.poc.metrics.concurrency.input.InputValidation;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Aspect
@Component
public class HandleRestRequestAspect extends LoggerAspect {

  // Define the pointcut for LogRequestResponse annotation
  @Pointcut("@annotation(com.hernancussi.poc.metrics.concurrency.aop.HandleRestRequest)")
  public void handleAnnotationPointcut() {
  }

  @Before("handleAnnotationPointcut()")
  public void processBefore(JoinPoint joinPoint) {
    var hasArgs = joinPoint.getArgs().length > 0;
    var log = getLogger(joinPoint);
    if (log.isInfoEnabled()) {
      log.info(STR."REST method: \{joinPoint.getSignature().getName()}" + (hasArgs ? STR." called with: \{joinPoint.getArgs()[0]}" : ""));
    }

    var input = Arrays.stream(joinPoint.getArgs()).filter(arg -> arg instanceof InputValidation).map(arg -> (InputValidation) arg).findAny();
    input.ifPresent(InputValidation::validate);
  }

  @AfterThrowing(value = "handleAnnotationPointcut()", throwing = "error")
  public void processAfter(JoinPoint joinPoint, Exception error) {
    throw new RuntimeException(STR."Failed for \{joinPoint.getSignature().getName()}", error);
  }

}
