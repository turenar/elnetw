package jp.syuriken.snsw.twclient.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Instance field for @{@link Initializer}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface InitializerInstance {
}
