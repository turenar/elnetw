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

package jp.mydns.turenar.twclient.internal;

import java.io.ByteArrayInputStream;
import java.util.Locale;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import jp.mydns.turenar.lib.parser.ArgParser;
import jp.mydns.turenar.lib.parser.ArgumentType;
import jp.mydns.turenar.lib.parser.ParsedArguments;
import jp.mydns.turenar.twclient.ClientConfiguration;
import org.slf4j.LoggerFactory;

import static jp.mydns.turenar.twclient.i18n.LocalizationResource.tr;

/**
 * Logging Configurator: set logger as app arg
 *
 * This class supports --quiet(-q), --log-level, --verbose(-v), --color-log, --root-log-level
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class LoggingConfigurator {
	/**
	 * configure logger
	 *
	 * @param parsed ParsedArguments instance
	 */
	public static void configureLogger(ParsedArguments parsed) {
		new LoggingConfigurator(parsed).configure();
	}

	/**
	 * notify option information to ArgParser
	 *
	 * @param parser ArgParser instance
	 */
	public static void setOpts(ArgParser parser) {
		parser.addOption("--log-level").argType(ArgumentType.REQUIRED_ARGUMENT).argName("LEVEL")
				.description(tr("set LEVEL as application log level\nLEVEL: one of <trace,all,debug,info,warn,error,off>"));
		parser.addOption("--root-log-level").argType(ArgumentType.REQUIRED_ARGUMENT).argName("LEVEL")
				.description(tr("set LEVEL as root log level. see --log-level"));
		parser.addOption("-q", "--quiet").argType(ArgumentType.NO_ARGUMENT)
				.description(tr("disable informational output. This conflicts with --verbose"));
		parser.addOption("-v", "--verbose").argType(ArgumentType.NO_ARGUMENT).group("--quiet")
				.description(tr("enable more annoying messages. This conflicts with --quiet"));
		parser.addOption("--color-log").argType(ArgumentType.NO_ARGUMENT)
				.description(tr("colorize logging output. This don't work in Windows"));
	}

	private final ParsedArguments parsed;
	private final StringBuilder builder;

	private LoggingConfigurator(ParsedArguments parsed) {
		this.parsed = parsed;
		builder = new StringBuilder();
	}

	private void addAppenderConsole() {
		builder.append(
				"<appender name=\"FILE\" class=\"ch.qos.logback.core.FileAppender\">\n"
						+ "  <file>${elnetw.home}/log/elnetw-${bySecond}.log</file>\n"
						+ "  <encoder>\n"
						+ "    <Pattern>\n"
						+ "      %d{HH:mm:ss.SSS} [%-5level %-35logger{35}] %msg%n\n"
						+ "    </Pattern>\n"
						+ "  </encoder>\n"
						+ "</appender>\n"
						+ "<appender name=\"ASYNC_FILE\" class=\"jp.mydns.turenar.twclient.internal.AsyncAppender\">\n"
						+ "  <appender-ref ref=\"FILE\" />\n"
						+ "  <discardingThreshold>10</discardingThreshold>\n"
						+ "</appender>"
		);
	}

	private void addAppenderFile() {
		if (!parsed.hasOpt("--quiet")) {
			builder.append(
					"<appender name=\"STDOUT\" class=\"ch.qos.logback.core.ConsoleAppender\">\n"
							+ "  <Target>System.out</Target>\n"
							+ "  <encoder>\n"
							+ "    <Pattern>\n"
			);
			if (parsed.hasOpt("--color-log") || parsed.hasOpt("--debug")) {
				builder.append(
						"%yellow(%d{HH:mm:ss.SSS}) "
								+ "[%highlight(%-5level) %cyan(%-35.35logger{35}) %magenta(%12.12thread)] %msg%n%xEx"
				);
			} else {
				builder.append("%d{HH:mm:ss.SSS} [%-5level %-35logger{35}] %msg%n\n");
			}
			builder.append("    </Pattern>\n"
					+ "  </encoder>\n"
					+ "</appender>");
		}
	}

	private void addAppenders() {
		addAppenderConsole();
		addAppenderFile();
	}

	private void addTimestampProperty() {
		builder.append("<timestamp key='bySecond' datePattern=\"yyyyMMdd'T'HHmmss\"/>\n");
	}

	private void configure() {
		rootConfiguration();
		LoggerContext context = (LoggerContext) LoggerFactory.getILoggerFactory();
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			configurator.doConfigure(new ByteArrayInputStream(builder.toString().getBytes(ClientConfiguration.UTF8_CHARSET)));
		} catch (JoranException je) {
			// StatusPrinter will handle this
		}
	}

	private String getApplicationLogLevel() {
		String arg = parsed.getOptArg("--log-level");
		// if --log-level is not passed fallback to --root-log-level
		return arg == null ? getRootLogLevel() : getLogLevel(arg);
	}

	private String getLogLevel(String optArg) {
		if (optArg == null) {
			return parsed.hasOptGroup("--debug") ? "TRACE" : "INFO";
		}
		optArg = optArg.toUpperCase(Locale.ENGLISH);
		switch (optArg) {
			case "TRACE":
			case "ALL":
				return "TRACE";
			case "DEBUG":
				return "DEBUG";
			case "INFO":
				return "INFO";
			case "WARN":
				return "WARN";
			case "ERROR":
				return "ERROR";
			case "NONE":
			case "OFF":
			case "QUIET":
				return "OFF";
			default:
				throw new IllegalArgumentException("--log-level/--root-log-level <level>: "
						+ "level must be one of trace,all,debug,info,warn,error,none,off,quiet");
		}
	}

	private String getRootLogLevel() {
		String arg = parsed.getOptArg("--root-log-level");
		return arg == null ? "INFO" : getLogLevel(arg);
	}

	private void rootConfiguration() {
		builder.append("<configuration");
		if (parsed.hasOptGroup("--verbose")) {
			builder.append(" debug='true'");
		}
		builder.append(">");
		addTimestampProperty();
		addAppenders();
		setLogLevel();
		builder.append("</configuration>");
	}

	private void setLogLevel() {
		builder.append(
				"<logger name=\"jp.mydns.turenar\">\n"
						+ "  <level value=\"")
				.append(getApplicationLogLevel())
				.append("\" />\n"
						+ "</logger>\n"
						// root log level
						+ "<root>\n"
						+ "  <level value=\"")
				.append(getRootLogLevel())
				.append("\" />\n"
						+ "  <appender-ref ref=\"STDOUT\" />\n"
						+ "  <appender-ref ref=\"ASYNC_FILE\" />\n"
						+ "</root>");
	}
}
