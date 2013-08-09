package jp.syuriken.snsw.twclient.filter;

import twitter4j.StatusDeletionNotice;

@edu.umd.cs.findbugs.annotations.SuppressWarnings("SE_NO_SERIALVERSIONID")
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
	public boolean equals(Object obj) {
		if (obj instanceof StatusDeletionNotice) {
			return userId == ((StatusDeletionNotice) obj).getUserId();
		} else {
			return false;
		}
	}

	@Override
	public long getStatusId() {
		return 0;
	}

	@Override
	public long getUserId() {
		return userId;
	}

	@Override
	public int hashCode() {
		return (int) userId;
	}

	public void test() {
	}
}
