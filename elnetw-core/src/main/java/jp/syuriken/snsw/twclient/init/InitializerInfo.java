package jp.syuriken.snsw.twclient.init;

import java.lang.reflect.Method;
import java.util.LinkedList;

/**
 * information of @{@link Initializer}
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface InitializerInfo {
	/**
	 * get Initializer Annotation
	 *
	 * @return annotation
	 */
	Initializer getAnnotation();

	/**
	 * get count of dependencies which is not resolved yet.
	 *
	 * @return dependency count
	 */
	int getDepCount();

	/**
	 * get all dependencies (including resolved)
	 *
	 * @return dependencies array
	 */
	String[] getDependencies();

	/**
	 * get initializer method
	 *
	 * @return method
	 */
	Method getInitializer();

	/**
	 * get initializer's name
	 *
	 * @return name
	 */
	String getName();

	/**
	 * get initializer's phase
	 *
	 * @return phase
	 */
	String getPhase();

	/**
	 * get dependencies which is not resolved yet.
	 *
	 * @return dependencies which is not resolved yet
	 */
	LinkedList<String> getRemainDependencies();
}
