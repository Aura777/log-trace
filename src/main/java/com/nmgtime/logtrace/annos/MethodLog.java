package com.nmgtime.logtrace.annos;

import org.slf4j.event.Level;

import java.lang.annotation.*;

/**
 * Methods with this annotation will have trace logs added to their internal conditional statements,
 * and optionally, try-catch statements can also be appended as needed.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
public @interface MethodLog {

    /**
     * If true, catch and print exception.
     * If the original method has already caught and handled the exception, it will be invalid.
     */
    boolean exceptionLog() default false;

    /**
     * Use with {@link MethodLog#exceptionLog()}, if true, only catch exception but not throw.
     */
    boolean noThrow() default false;

    /**
     * If true, the duration to execute this method will be printed.
     */
    boolean dur() default false;

    /**
     * If true, the if,switch statement's logs will not print, only print variables decorated with {@link com.nmgtime.logtrace.annos.VarLog}.
     */
    boolean onlyVar() default false;

    /**
     * Level of trace log.
     */
    Level traceLevel() default Level.INFO;

    /**
     * Like {@link TraceLog#isOpen()}, but the priority is higher.
     */
    String isOpen() default "";
}
