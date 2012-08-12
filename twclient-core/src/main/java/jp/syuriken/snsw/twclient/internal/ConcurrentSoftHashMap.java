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

import jp.syuriken.snsw.twclient.ClientConfiguration;
import jp.syuriken.snsw.twclient.JobQueue.Priority;
import jp.syuriken.snsw.twclient.ParallelRunnable;

/**
 * SoftReference を使った ConcurrentHashMap を操作するクラス。
 * 
 * @author $Author$
 * @param <K> キー型パラメータ。
 *   {@link #equals(Object)} の比較に使われるため、型Vが異なれば {@link K#equals(Object)} はfalseを
 *   返さなければなりません。
 * @param <V> 値型パラメータ。hashCodeはキャッシュされるため、hashCodeが変わらないオブジェクトであることが要求されます。
 */
public class ConcurrentSoftHashMap<K, V> implements ConcurrentMap<K, V> {
	
	/**
	 * {@link ConcurrentSoftHashMap.SoftReferenceUtil} を扱うエントリーイテレータ
	 * 
	 * @author $Author$
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
	 * @author $Author$
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
	 * @author $Author$
	 */
	protected class ReferenceCleaner implements ParallelRunnable {
		
		private boolean isQueued = false;
		
		
		/**
		 * ジョブキューに追加する
		 */
		protected void queue() {
			synchronized (this) {
				if (isQueued == false) {
					configuration.getFrameApi().addJob(Priority.HIGH, this);
					isQueued = true;
				}
			}
		}
		
		@Override
		public void run() {
			Reference<? extends V> ref;
			while ((ref = referenceQueue.poll()) != null) {
				hashMap.remove(valueConverter.getKey(ref.get()));
			}
			synchronized (this) {
				isQueued = false;
			}
		}
	}
	
	/**
	 * {@link SoftReference}に {@link #equals(Object)} と {@link #hashCode()} サポートを追加する
	 * クラス。
	 * 
	 * @param <K> キー型パラメータ。
	 *   {@link #equals(Object)} の比較に使われるため、V型インスタンスが異なれば {@link K#equals(Object)} はfalseを
	 *   返さなければなりません。
	 * @param <V> 値型パラメータ。
	 *   {@link #hashCode()} のために {@link V#hashCode()} はキャッシュされます。ハッシュコードの変更は許可されません。
	 * @author $Author$
	 */
	protected static class SoftReferenceUtil<K, V> extends SoftReference<V> {
		
		private final int hashCode;
		
		private final K key;
		
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param referent リファレントオブジェクト
		 * @param key キー
		 */
		public SoftReferenceUtil(V referent, K key) {
			super(referent);
			this.key = key;
			hashCode = referent.hashCode();
		}
		
		/**
		 * インスタンスを生成する。
		 * 
		 * @param referent リファレントオブジェクト
		 * @param referenceQueue キュー
		 * @param key キー
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
	}
	
	/**
	 * 型Vを型Kに変換するためのユーティリティクラス。
	 * 
	 * @param <K> キー型パラメータ
	 * @param <V> 値型パラメータ
	 * @author $Author$
	 */
	public static interface ValueConverter<K, V> {
		
		/**
		 * Vを識別するキーを取得する。
		 * 異なるVには異なるキーを返さなければなりません。
		 * 
		 * @param value 値
		 * @return キー
		 */
		K getKey(V value);
	}
	
	/**
	 * {@link ConcurrentSoftHashMap.SoftReferenceUtil} をラップするバリューイテレータ
	 * 
	 * @author $Author$
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
	 * @author $Author$
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
	 * @author $Author$
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
			return expandReference(entry.setValue(wrapReference(value)));
		}
		
	}
	
	
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
	
	/** 値→キーするユーティリティクラス。 */
	protected final ValueConverter<K, V> valueConverter;
	
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 * @param valueConverter {@link ValueConverter}インスタンス
	 */
	public ConcurrentSoftHashMap(ClientConfiguration configuration, ValueConverter<K, V> valueConverter) {
		this.configuration = configuration;
		this.valueConverter = valueConverter;
		hashMap = new ConcurrentHashMap<K, SoftReferenceUtil<K, V>>();
		referenceQueue = new ReferenceQueue<V>();
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 * @param valueConverter {@link ValueConverter}インスタンス
	 * @param initialCapacity 初期容量多数の要素に適合するよう、実装は内部のサイズ設定を実行する
	 * @throws IllegalArgumentException 初期容量が負である場合
	 */
	public ConcurrentSoftHashMap(ClientConfiguration configuration, ValueConverter<K, V> valueConverter,
			int initialCapacity) {
		this.configuration = configuration;
		this.valueConverter = valueConverter;
		hashMap = new ConcurrentHashMap<K, SoftReferenceUtil<K, V>>(initialCapacity);
		referenceQueue = new ReferenceQueue<V>();
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 * @param valueConverter {@link ValueConverter}インスタンス
	 * @param initialCapacity 初期容量多数の要素に適合するよう、実装は内部のサイズ設定を実行する
	 * @param loadFactor
	 *   サイズ変更の制御に使用される負荷係数のしきい値。
	 *   サイズ変更は、ビンごとの要素の平均数がこのしきい値を超えた場合に実行できる
	 * @throws IllegalArgumentException 初期容量が負であるか、負荷係数が正ではない場合
	 */
	public ConcurrentSoftHashMap(ClientConfiguration configuration, ValueConverter<K, V> valueConverter,
			int initialCapacity, float loadFactor) {
		this.configuration = configuration;
		this.valueConverter = valueConverter;
		hashMap = new ConcurrentHashMap<K, SoftReferenceUtil<K, V>>(initialCapacity, loadFactor);
		referenceQueue = new ReferenceQueue<V>();
	}
	
	/**
	 * インスタンスを生成する。
	 * 
	 * @param configuration 設定
	 * @param valueConverter {@link ValueConverter}インスタンス
	 * @param initialCapacity 初期容量多数の要素に適合するよう、実装は内部のサイズ設定を実行する
	 * @param loadFactor
	 *   サイズ変更の制御に使用される負荷係数のしきい値。
	 *   サイズ変更は、ビンごとの要素の平均数がこのしきい値を超えた場合に実行できる
	 * @param concurrencyLevel 並行して更新中のスレッドの推定数。多数のスレッドに適合するよう、実装は内部のサイズ設定を実行する
	 * @throws IllegalArgumentException 初期容量が負であるか、負荷係数または concurrencyLevel が正ではない場合
	 */
	public ConcurrentSoftHashMap(ClientConfiguration configuration, ValueConverter<K, V> valueConverter,
			int initialCapacity, float loadFactor, int concurrencyLevel) throws IllegalArgumentException {
		this.configuration = configuration;
		this.valueConverter = valueConverter;
		hashMap = new ConcurrentHashMap<K, SoftReferenceUtil<K, V>>(initialCapacity, loadFactor, concurrencyLevel);
		referenceQueue = new ReferenceQueue<V>();
	}
	
	@Override
	public void clear() {
		hashMap.clear();
	}
	
	@Override
	public boolean containsKey(Object key) {
		return hashMap.containsKey(key);
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean containsValue(Object value) throws ClassCastException {
		return hashMap.containsValue(wrapReference((V) value));
	}
	
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
	
	@Override
	public Set<K> keySet() {
		if (keySet == null) {
			keySet = hashMap.keySet();
		}
		return keySet;
	}
	
	@Override
	public V put(K key, V value) {
		return expandReference(hashMap.put(key, wrapReference(value)));
	}
	
	@Override
	public void putAll(Map<? extends K, ? extends V> m) {
		for (Map.Entry<? extends K, ? extends V> e : m.entrySet()) {
			hashMap.put(e.getKey(), wrapReference(e.getValue()));
		}
	}
	
	@Override
	public V putIfAbsent(K key, V value) {
		return expandReference(hashMap.putIfAbsent(key, wrapReference(value)));
	}
	
	private void queueCleaner() {
		referenceCleaner.queue();
	}
	
	@Override
	public V remove(Object key) {
		return expandReference(hashMap.remove(key));
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object key, Object value) throws ClassCastException {
		return hashMap.remove(key, wrapReference((V) value));
	}
	
	@Override
	public V replace(K key, V value) {
		return expandReference(hashMap.replace(key, wrapReference(value)));
	}
	
	@Override
	public boolean replace(K key, V oldValue, V newValue) {
		return hashMap.replace(key, wrapReference(oldValue), wrapReference(newValue));
	}
	
	@Override
	public int size() {
		return hashMap.size();
	}
	
	@Override
	public Values values() {
		if (values == null) {
			values = new Values(hashMap.values());
		}
		return values;
	}
	
	private final SoftReferenceUtil<K, V> wrapReference(V obj) {
		return new SoftReferenceUtil<K, V>(obj, referenceQueue, valueConverter.getKey(obj));
	}
}
