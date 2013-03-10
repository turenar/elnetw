package jp.syuriken.snsw.twclient.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation shows Class has @{@link Initializer}.
 *
 * <p>Required: Methods with @{@link Initializer}</p>
 * <p>
 * Optional: Field with @{@link InitializerInstance}. If one field with @{@link InitializerInstance},
 * methods with @{@link Initializer} will be invoked with the field. If the field was omitted, methods must be static.
 * </p>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface InitProviderClass {
}
