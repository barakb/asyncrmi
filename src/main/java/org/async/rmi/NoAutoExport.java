package org.async.rmi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Barak Bar Orion
 * 11/15/14.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
/**
 * The @NoAutoExport annotation instruct the underline RMI system to
 * treat Object of this class as Serialize rather then Remote although they can implement Remote
 * They will never be replaced with a proxy to them in the RMI serialization.
 */
public @interface NoAutoExport {
}
