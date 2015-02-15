package jp.mydns.turenar.twclient.storage;

/**
 * File Entry Implementation
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class FileEntryImpl implements FileEntry {
	private final DirEntry parent;
	private final String basename;

	/**
	 * make instance
	 *
	 * @param parent   parent dir entry
	 * @param basename basename
	 */
	public FileEntryImpl(DirEntry parent, String basename) {
		this.parent = parent;
		this.basename = basename;
	}

	@Override
	public boolean exists(String path) {
		return parent.exists(path);
	}

	@Override
	public DirEntry getParent() {
		return parent;
	}

	@Override
	public String getPath() {
		return basename;
	}

	@Override
	public DirEntry getRoot() {
		return parent.getRoot();
	}

	@Override
	public boolean isDirEntry() {
		return false;
	}

	@Override
	public boolean readBool() {
		return parent.readBool(basename);
	}

	@Override
	public int readInt() {
		return parent.readInt(basename);
	}

	@Override
	public long readLong() {
		return parent.readLong(basename);
	}

	@Override
	public long readLong(long defaultValue) {
		return parent.readLong(basename, defaultValue);
	}

	@Override
	public String readString() {
		return parent.readString(basename);
	}

	@Override
	public String realpath() {
		return parent.realpath(basename);
	}

	@Override
	public boolean remove() {
		return parent.remove(basename);
	}

	@Override
	public int size() {
		return -1;
	}

	@Override
	public FileEntry writeBool(boolean value) {
		parent.writeBool(basename, value);
		return this;
	}

	@Override
	public FileEntry writeInt(int value) {
		parent.writeInt(basename, value);
		return this;
	}

	@Override
	public FileEntry writeLong(long value) {
		parent.writeLong(basename, value);
		return this;
	}

	@Override
	public FileEntry writeString(String data) {
		parent.writeString(basename, data);
		return this;
	}
}
