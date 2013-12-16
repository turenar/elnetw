package jp.syuriken.snsw.twclient.internal;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.AbstractCollection;
import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.annotation.Nonnull;

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.ParallelRunnable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SoftReference を使った ConcurrentHashMap を操作するクラス。
 *
 * @param <K> キー型パラメータ。
 *            {@link #equals(Object)} の比較に使われるため、型Vが異なれば {@link Object#equals(Object)} はfalseを
 *            返さなければなりません。
 * @param <V> 値型パラメータ。hashCodeはキャッシュされるため、hashCodeが変わらないオブジェクトであることが要求されます。
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class ConcurrentSoftHashMap<K, V> implements ConcurrentMap<K, V> {

	/**
	 * {@link SoftReference}に {@link #equals(Object)} と {@link #hashCode()} サポートを追加する
	 * クラス。
	 *
	 * @param <K> キー型パラメータ。
	 *            {@link #equals(Object)} の比較に使われるため、V型インスタンスが異なれば {@link Object#equals(Object)} はfalseを
	 *            返さなければなりません。
	 * @param <V> 値型パラメータ。
	 *            {@link #hashCode()} のために {@link Object#hashCode()} はキャッシュされます。ハッシュコードの変更は許可されません。
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected static class SoftReferenceUtil<K, V> extends SoftReference<V> {

		private final int hashCode;

		/** キー */
		protected final K key;

		/**
		 * インスタンスを生成する。
		 *
		 * @param referent リファレントオブジェクト
		 * @param key      キー
		 */
		public SoftReferenceUtil(V referent, K key) {
			super(referent);
			this.key = key;
			hashCode = referent.hashCode();
		}

		/**
		 * インスタンスを生成する。
		 *
		 * @param referent       リファレントオブジェクト
		 * @param referenceQueue キュー
		 * @param key            キー
		 */
		public SoftReferenceUtil(V referent, ReferenceQueue<V> referenceQueue, K key) {
			super(referent, referenceQueue);
			this.key = key;
			hashCode = referent.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof SoftReferenceUtil) {
				SoftReferenceUtil<?, ?> reference = (SoftReferenceUtil<?, ?>) obj;
				return hashCode == reference.hashCode && key.equals(reference.key);
			} else {
				return false;
			}
		}

		@Override
		public int hashCode() {
			return hashCode;
		}

		@Override
		public String toString() {
			return "SoftReferenceUtil{hash=" + hashCode + ",key=" + key + "}";
		}
	}

	/**
	 * {@link ConcurrentSoftHashMap.SoftReferenceUtil} を扱うエントリーイテレータ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class EntryIterator implements Iterator<Entry<K, V>> {

		private final Iterator<Entry<K, SoftReferenceUtil<K, V>>> iterator;

		/**
		 * インスタンスを生成する。
		 *
		 * @param iterator 親イテレータ
		 */
		public EntryIterator(Iterator<Entry<K, SoftReferenceUtil<K, V>>> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public Entry<K, V> next() {
			return new WrapEntry(iterator.next());
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}

	/**
	 * {@link ConcurrentSoftHashMap.SoftReferenceUtil} を扱うエントリーセット
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class EntrySet extends AbstractSet<Entry<K, V>> {

		private final Set<Entry<K, SoftReferenceUtil<K, V>>> set;

		/**
		 * インスタンスを生成する。
		 *
		 * @param entrySet 親エントリーセット
		 */
		public EntrySet(Set<Entry<K, SoftReferenceUtil<K, V>>> entrySet) {
			set = entrySet;
		}

		@Nonnull
		@Override
		public EntryIterator iterator() {
			return new EntryIterator(set.iterator());
		}

		@Override
		public int size() {
			return set.size();
		}
	}

	/**
	 * リファレンス掃除するクラス。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	protected class ReferenceCleaner implements ParallelRunnable {
		private final Logger logger = LoggerFactory.getLogger(ReferenceCleaner.class);
		private volatile boolean isQueued = false;

		/** ジョブキューに追加する */
		protected void queue() {
			synchronized (this) {
				if (!isQueued) {
					isQueued = true;
					configuration.addJob(Priority.LOW, this);
				}
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public void run() {
			Reference<? extends V> ref;
			while ((ref = referenceQueue.poll()) != null) {
				if (ref instanceof SoftReferenceUtil) {
					if (hashMap.remove(((SoftReferenceUtil<K, V>) ref).key, ref)) {
						logger.trace("remove {}", ref);
					}
				} else {
					throw new AssertionError("ref must be SoftReferenceUtil");
				}
			}
			synchronized (this) {
				isQueued = false;
			}
		}
	}

	/**
	 * {@link ConcurrentSoftHashMap.SoftReferenceUtil} をラップするバリューイテレータ
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class ValueIterator implements Iterator<V> {

		private Iterator<SoftReferenceUtil<K, V>> iterator;

		/**
		 * インスタンスを生成する。
		 *
		 * @param iterator 親イテレータ
		 */
		public ValueIterator(Iterator<SoftReferenceUtil<K, V>> iterator) {
			this.iterator = iterator;
		}

		@Override
		public boolean hasNext() {
			return iterator.hasNext();
		}

		@Override
		public V next() {
			return expandReference(iterator.next());
		}

		@Override
		public void remove() {
			iterator.remove();
		}
	}

	/**
	 * {@link ConcurrentSoftHashMap.SoftReferenceUtil}を扱うバリューセット
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class Values extends AbstractCollection<V> {

		private Collection<SoftReferenceUtil<K, V>> values;

		/**
		 * インスタンスを生成する。
		 *
		 * @param values 親values
		 */
		public Values(Collection<SoftReferenceUtil<K, V>> values) {
			this.values = values;
		}

		@Nonnull
		@Override
		public ValueIterator iterator() {
			return new ValueIterator(values.iterator());
		}

		@Override
		public int size() {
			return values.size();
		}
	}

	/**
	 * エントリーをラップするクラス。
	 *
	 * @author Turenar (snswinhaiku dot lo at gmail dot com)
	 */
	public class WrapEntry implements Entry<K, V> {

		private final Entry<K, SoftReferenceUtil<K, V>> entry;

		/**
		 * インスタンスを生成する。
		 *
		 * @param entry 親エントリ
		 */
		public WrapEntry(Entry<K, SoftReferenceUtil<K, V>> entry) {
			this.entry = entry;
		}

		@Override
		public K getKey() {
			return entry.getKey();
		}

		@Override
		public V getValue() {
			return expandReference(entry.getValue());
		}

		@Override
		public V setValue(V value) {
			return expandReference(entry.setValue(wrapReference(getKey(), value)));
		}
	}

	/**
	 * The default initial capacity for this table,
	 * used when not otherwise specified in a constructor.
	 */
	protected static final int DEFAULT_INITIAL_CAPACITY = 16;
	/**
	 * The default load factor for this table, used when not
	 * otherwise specified in a constructor.
	 */
	protected static final float DEFAULT_LOAD_FACTOR = 0.75f;
	/**
	 * The default concurrency level for this table, used when not
	 * otherwise specified in a constructor.
	 */
	protected static final int DEFAULT_CONCURRENCY_LEVEL = 16;
	/*package*/static final Logger logger = LoggerFactory.getLogger(ConcurrentSoftHashMap.class);
	/** ClientConfigurationインスタンス */
	protected final ClientConfiguration configuration;
	/** ごにょごにょするハッシュマップ */
	protected final ConcurrentHashMap<K, SoftReferenceUtil<K, V>> hashMap;
	/** リファレンスキュー */
	protected final ReferenceQueue<V> referenceQueue;
	/** {@link #referenceQueue}掃除機 */
	protected final ReferenceCleaner referenceCleaner = new ReferenceCleaner();
	/** キーセット (キャッシュ) */
	protected transient Set<K> keySet;
	/** エントリーセット (キャッシュ) */
	protected transient EntrySet entrySet;
	/** バリューセット (キャッシュ) */
	protected transient Values values;

	/** インスタンスを生成する。 */
	public ConcurrentSoftHashMap() {
		this(DEFAULT_INITIAL_CAPACITY, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param initialCapacity 初期容量多数の要素に適合するよう、実装は内部のサイズ設定を実行する
	 * @throws IllegalArgumentException 初期容量が負である場合
	 */
	public ConcurrentSoftHashMap(int initialCapacity) {
		this(initialCapacity, DEFAULT_LOAD_FACTOR, DEFAULT_CONCURRENCY_LEVEL);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param initialCapacity 初期容量多数の要素に適合するよう、実装は内部のサイズ設定を実行する
	 * @param loadFactor      サイズ変更の制御に使用される負荷係数のしきい値。
	 *                        サイズ変更は、ビンごとの要素の平均数がこのしきい値を超えた場合に実行できる
	 * @throws IllegalArgumentException 初期容量が負であるか、負荷係数が正ではない場合
	 */
	public ConcurrentSoftHashMap(int initialCapacity, float loadFactor) {
		this(initialCapacity, loadFactor, DEFAULT_CONCURRENCY_LEVEL);
	}

	/**
	 * インスタンスを生成する。
	 *
	 * @param initialCapacity  初期容量多数の要素に適合するよう、実装は内部のサイズ設定を実行する
	 * @param loadFactor       サイズ変更の制御に使用される負荷係数のしきい値。
	 *                         サイズ変更は、ビンごとの要素の平均数がこのしきい値を超えた場合に実行できる
	 * @param concurrencyLevel 並行して更新中のスレッドの推定数。多数のスレッドに適合するよう、実装は内部のサイズ設定を実行する
	 * @throws IllegalArgumentException 初期容量が負であるか、負荷係数または concurrencyLevel が正ではない場合
	 */
	public ConcurrentSoftHashMap(int initialCapacity, float loadFactor, int concurrencyLevel)
			throws IllegalArgumentException {
		this.configuration = ClientConfiguration.getInstance();
		hashMap = new ConcurrentHashMap<>(initialCapacity, loadFactor, concurrencyLevel);
		referenceQueue = new ReferenceQueue<>();
	}

	@Override
	public void clear() {
		hashMap.clear();
	}

	@Override
	public boolean containsKey(Object key) {
		return hashMap.containsKey(key);
	}

	/**
	 * この実装では、常に {@link UnsupportedOperationException} を投げる。
	 * {@link #containsKey(Object)}を用いること。
	 *
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsValue(Object value) throws UnsupportedOperationException {
		throw new UnsupportedOperationException();
	}

	@Nonnull
	@Override
	public Set<Entry<K, V>> entrySet() {
		if (entrySet == null) {
			entrySet = new EntrySet(hashMap.entrySet());
		}
		return entrySet;
	}

	/**
	 * リファレンスを展開する
	 *
	 * @param reference リファレンス
	 * @return 値
	 */
	protected V expandReference(SoftReferenceUtil<K, V> reference) {
		if (reference == null) {
			return null;
		}
		V obj = reference.get();
		if (obj == null) {
			queueCleaner();
		}
		return obj;
	}

	@Override
	public V get(Object key) {
		return expandReference(hashMap.get(key));
	}

	@Override
	public boolean isEmpty() {
		return hashMap.isEmpty();
	}

	@Nonnull
	@Override
	public Set<K> keySet() {
		if (keySet == null) {
			keySet = hashMap.keySet();
		}
		return keySet;
	}

	@Override
	public V put(K key, V value) {
		return expandReference(hashMap.put(key, wrapReference(key, value)));
	}

	@Override
	public void putAll(@Nonnull Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			hashMap.put(e.getKey(), wrapReference(e.getKey(), e.getValue()));
		}
	}

	@Override
	public V putIfAbsent(@Nonnull K key, V value) {
		return expandReference(hashMap.putIfAbsent(key, wrapReference(key, value)));
	}

	private void queueCleaner() {
		referenceCleaner.queue();
	}

	@Override
	public V remove(Object key) {
		queueCleaner();
		return expandReference(hashMap.remove(key));
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(@Nonnull Object key, Object value) throws ClassCastException {
		if (value == null) {
			return false;
		}
		queueCleaner();
		return hashMap.remove(key, wrapReference((K) key, (V) value));
	}

	@Override
	public V replace(@Nonnull K key, @Nonnull V value) {
		return expandReference(hashMap.replace(key, wrapReference(key, value)));
	}

	@Override
	public boolean replace(@Nonnull K key, @Nonnull V oldValue, @Nonnull V newValue) {
		return hashMap.replace(key, wrapReference(key, oldValue), wrapReference(key, newValue));
	}

	@Override
	public int size() {
		return hashMap.size();
	}

	@Nonnull
	@Override
	public Values values() {
		if (values == null) {
			values = new Values(hashMap.values());
		}
		return values;
	}

	private SoftReferenceUtil<K, V> wrapReference(K key, V obj) {
		return new SoftReferenceUtil<>(obj, referenceQueue, key);
	}
}
