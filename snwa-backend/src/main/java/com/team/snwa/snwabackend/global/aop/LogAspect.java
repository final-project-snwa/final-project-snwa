package com.team.snwa.snwabackend.global.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;


/**
 * 실제로 시간을 측정하는 AOP 클래스
 *
 *
 * @author 허준형
 * @DateOfCreated 2026-02-05
 * @DateOfEdit 2026-02-05
 */
@Slf4j
@Aspect
@Component
public class LogAspect {

    // @LogExecutionTime 어노테이션이 붙은 곳을 가로채서 실행됨
    @Around("@annotation(com.team.snwa.snwabackend.global.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            return joinPoint.proceed(); // 원래 메서드 실행
        } finally {
            // 성공하든 실패하든 무조건 실행됨
            stopWatch.stop();
            log.info("⏱️ [Execution Time] {} - {}ms", joinPoint.getSignature(), stopWatch.getTotalTimeMillis());
        }
    }
}