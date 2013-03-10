package jp.syuriken.snsw.twclient.init;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/** Initializer Annotation. */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Initializer {
	/**
	 * Specify required initializer
	 *
	 * @return initializer's name
	 */
	String[] dependencies() default {};

	/**
	 * Specify Initializer's name
	 *
	 * @return name
	 */
	String name();

	/**
	 * Init-Phase.
	 *
	 * <dl>
	 * <dt>preinit</dt><dd>Pre-initialize phase. Use this phase to load extra library etc.</dd>
	 * <dt>init</dt><dd>Initialize phase. Default phase is this.</dd>
	 * <dt>postinit</dt><dd>Post-initialize phase.</dd>
	 * <dt>prestart</dt><dd>Before showing main frame.</dd>
	 * <dt>start</dt><dd>Showing main frame phase.</dd>
	 * </dl>
	 *
	 * @return phase (preinit, init, postinit, prestart, start)
	 */
	String phase() default "init";
}
