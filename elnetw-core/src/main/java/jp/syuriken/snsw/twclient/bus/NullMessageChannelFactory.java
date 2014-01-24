package jp.syuriken.snsw.twclient.bus;

/**
 * null message channel factory
 */
public class NullMessageChannelFactory implements MessageChannelFactory {
	public static final MessageChannelFactory INSTANCE = new NullMessageChannelFactory();

	private NullMessageChannelFactory() {
	}

	@Override
	public MessageChannel getInstance(MessageBus messageBus, String accountId, String path) {
		return NullMessageChannel.INSTANCE;
	}
}
