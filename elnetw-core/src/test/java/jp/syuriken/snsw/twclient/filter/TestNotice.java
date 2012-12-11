package jp.syuriken.snsw.twclient.filter;

import twitter4j.StatusDeletionNotice;

@SuppressWarnings("serial")
class TestNotice implements StatusDeletionNotice {
	
	private final long userId;
	
	
	public TestNotice(long userId) {
		this.userId = userId;
	}
	
	@Override
	public int compareTo(StatusDeletionNotice o) {
		return 0;
	}
	
	@Override
	public long getStatusId() {
		return 0;
	}
	
	@Override
	public long getUserId() {
		return userId;
	}
	
}