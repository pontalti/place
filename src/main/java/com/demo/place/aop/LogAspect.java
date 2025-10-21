package com.demo.place.aop;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Aspect
@Component
public class LogAspect {

	@Before("@annotation(com.demo.place.aop.Log)")
	public void beforeCall(JoinPoint joinPoint) {
		String method = joinPoint.getSignature().toShortString();
		Object[] args = joinPoint.getArgs();
		log.info("➡️ [BEFORE] Calling method: {}", method);
		if (args.length > 0) {
			Arrays.stream(args).forEach(arg -> {
				if (arg != null)
					log.info("📦 Method content: {}", arg);
			});
		} else {
			log.info("⚠️ No args.");
		}
	}

	@AfterReturning(value = "@annotation(com.demo.place.aop.Log)", returning = "result")
	public void afterCall(JoinPoint joinPoint, Object result) {
		String method = joinPoint.getSignature().toShortString();
		log.info("⬅️ [AFTER] Method {} executed successfully", method);
		if (result != null) {
			log.info("📤 Return: {}", result);
		}
	}

	@AfterThrowing(value = "@annotation(com.demo.place.aop.Log)", throwing = "ex")
	public void afterThrowing(JoinPoint joinPoint, Throwable ex) {
		String method = joinPoint.getSignature().toShortString();
		log.error("❌ [AFTER THROWING] Method error {}: {} ({})", method, ex.getMessage(),
				ex.getClass().getSimpleName());
	}
}
