package com.demo.place.aop;

import java.util.Arrays;

import com.demo.place.annotation.Log;

import jakarta.annotation.Priority;
import jakarta.interceptor.AroundInvoke;
import jakarta.interceptor.Interceptor;
import jakarta.interceptor.InvocationContext;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Log
@Priority(Interceptor.Priority.APPLICATION)
@Interceptor
public class LogInterceptor {
	
	@AroundInvoke
    public Object logMethod(InvocationContext ctx) throws Exception {
		
		StringBuilder method = new StringBuilder();
		
		method.append(ctx.getMethod().getDeclaringClass().getSimpleName());
		method.append("#"); 
		method.append(ctx.getMethod().getName());
		
        String args = Arrays.toString(ctx.getParameters());

        log.info("➡️ [BEFORE] Method {} called with args {}", method, args);

        try {
            Object result = ctx.proceed();

            log.info("⬅️ [AFTER] Method {} executed successfully", method);
            if (result != null) {
                log.info("📤 Return: {}", result);
            }

            return result;
        } catch (Exception ex) {
            log.error("❌ [AFTER THROWING] Method error {}: {} ({})", method, ex.getMessage(), ex.getClass().getSimpleName());
            throw ex;
        }
    }

}
