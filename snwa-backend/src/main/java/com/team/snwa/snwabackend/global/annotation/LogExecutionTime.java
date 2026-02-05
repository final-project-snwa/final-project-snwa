package com.team.snwa.snwabackend.global.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 커스텀 어노테이션
 * 해당 어노테이션이 붙은 메서드만 시간을 측정
 *

 *
 * @author 허준형
 * @DateOfCreated 2026-02-05
 * @DateOfEdit 2026-02-05
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogExecutionTime {
}
