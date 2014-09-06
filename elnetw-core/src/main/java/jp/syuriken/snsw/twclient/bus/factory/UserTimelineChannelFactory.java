package jp.syuriken.snsw.twclient.bus.factory;

import jp.syuriken.snsw.twclient.bus.MessageBus;
import jp.syuriken.snsw.twclient.bus.MessageChannel;
import jp.syuriken.snsw.twclient.bus.MessageChannelFactory;
import jp.syuriken.snsw.twclient.bus.channel.UserTimelineChannel;

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
