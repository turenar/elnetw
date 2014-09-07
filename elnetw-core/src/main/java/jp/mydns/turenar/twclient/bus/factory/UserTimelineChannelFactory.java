package jp.mydns.turenar.twclient.bus.factory;

import jp.mydns.turenar.twclient.bus.MessageBus;
import jp.mydns.turenar.twclient.bus.MessageChannel;
import jp.mydns.turenar.twclient.bus.MessageChannelFactory;
import jp.mydns.turenar.twclient.bus.channel.UserTimelineChannel;

/**
 * channel factory for UserTimelineChannel
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class UserTimelineChannelFactory implements MessageChannelFactory {
	@Override
	public MessageChannel getInstance(MessageBus messageBus, String accountId, String path, String arg) {
		return new UserTimelineChannel(messageBus, accountId, path, arg);
	}
}
