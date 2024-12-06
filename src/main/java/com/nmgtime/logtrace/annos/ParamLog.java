package com.nmgtime.logtrace.annos;

import java.lang.annotation.*;

/**
 * The parameter annotated with {@link ParamLog} will not be printed in the log.
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.PARAMETER})
public @interface ParamLog {
}
