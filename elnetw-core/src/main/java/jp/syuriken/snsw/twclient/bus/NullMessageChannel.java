package jp.syuriken.snsw.twclient.bus;

/**
 * null message channel
 */
public class NullMessageChannel implements MessageChannel {
	public static final MessageChannel INSTANCE = new NullMessageChannel();

	private NullMessageChannel() {
	}

	@Override
	public void connect() {
	}

	@Override
	public void disconnect() {
	}

	@Override
	public void realConnect() {
	}
}
