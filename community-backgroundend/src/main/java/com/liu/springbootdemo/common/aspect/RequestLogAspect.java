package com.liu.springbootdemo.common.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Aspect
@Component
@Slf4j
public class RequestLogAspect {

    //切点：记录所有Controller层的方法调用
    @Pointcut("execution(* com.liu.springbootdemo.controller..*.*(..))")
    public void controllerPointcut() {}

    @Around("controllerPointcut()")
    public Object doAround(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();

        //获取请求信息
        ServletRequestAttributes  attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        String uri = request.getRequestURI();

        Object result = null;
        try{
            //执行目标方法
            result = joinPoint.proceed();
        } finally {
            long timeCost = System.currentTimeMillis() - startTime;

            //慢查询阈值：超过 500ms 记录为进警告日志
            if(timeCost > 500){
                log.warn("====== 慢查询警告 ======");
                log.warn("URI: {}", uri);
                log.warn("Metjhod: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
                log.warn("耗时: {} ms", timeCost);
                log.warn("=====================");
            }else{
                log.info("====== 正常请求日志 ======");
                log.info("URI: {}", uri);
                log.info("Method: {}.{}", joinPoint.getSignature().getDeclaringTypeName(), joinPoint.getSignature().getName());
                log.info("耗时: {} ms", timeCost);
                log.info("=====================");
            }
        }
        return result;
    }

}
