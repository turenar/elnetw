/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.conf;

/**
 * property update event
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class PropertyUpdateEvent {
	private final ClientProperties source;
	/**
	 * name of the property that changed.
	 */
	private final String propertyKey;
	/**
	 * New value for property.
	 */
	private final String newValue;
	/**
	 * Previous value for property.
	 */
	private final String oldValue;

	/**
	 * create instance
	 *
	 * @param source      ClientProperties
	 * @param propertyKey The key of property that was changed.
	 * @param oldValue    The old value of the property.
	 * @param newValue    The new value of the property.
	 */
	public PropertyUpdateEvent(ClientProperties source, String propertyKey,
			String oldValue, String newValue) {
		this.source = source;
		this.propertyKey = propertyKey;
		this.newValue = newValue;
		this.oldValue = oldValue;
	}

	/**
	 * get new value for key
	 *
	 * @return new value
	 */
	public String getNewValue() {
		return newValue;
	}

	/**
	 * Gets the old value for the property, expressed as an Object.
	 *
	 * @return The old value for the property, expressed as an Object.
	 * May be null if multiple properties have changed.
	 */
	public String getOldValue() {
		return oldValue;
	}

	/**
	 * get property key
	 *
	 * @return property key.
	 */
	public String getPropertyName() {
		return propertyKey;
	}

	/**
	 * get client properties
	 *
	 * @return ClientProperties instance
	 */
	public ClientProperties getSource() {
		return source;
	}

	@Override
	public String toString() {
		return getClass().getName() + "{key=" + propertyKey + ",newValue='" + newValue + "',oldValue='" + oldValue + "'}";
	}

}
