package jp.mydns.turenar.twclient.storage;

/**
 * base for DirEntry, FileEntry
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public interface StorageEntry {
	/**
	 * get dir entry or null
	 *
	 * @return dir entry or null
	 */
	default DirEntry asDirEntry() {
		return isDirEntry() ? (DirEntry) this : null;
	}

	/**
	 * get file entry or null
	 *
	 * @return file entry or null
	 */
	default FileEntry asFileEntry() {
		return isDirEntry() ? null : (FileEntry) this;
	}

	/**
	 * check if path is exists
	 *
	 * @param path path
	 * @return exist?
	 */
	boolean exists(String path);

	/**
	 * get parent DirEntry
	 *
	 * @return DirEntry
	 */
	DirEntry getParent();

	/**
	 * get path of this
	 *
	 * @return path
	 */
	String getPath();

	/**
	 * get root of this
	 *
	 * @return root DirEntry
	 */
	DirEntry getRoot();

	/**
	 * check if this is DirEntry
	 *
	 * @return isDirEntry?
	 */
	boolean isDirEntry();

	/**
	 * get real path
	 *
	 * @return real path
	 */
	String realpath();

	/**
	 * get DirEntry size
	 *
	 * @return the number of elements
	 */
	int size();
}
