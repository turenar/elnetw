/*
 * The MIT License (MIT)
 * Copyright (c) 2011-2014 Turenai Project
 *
 * Permission is hereby granted, free of charge,
 *  to any person obtaining a copy of this software and associated documentation files (the "Software"),
 *  to deal in the Software without restriction, including without limitation the rights to
 *  use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 *  and to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 *  in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 *  INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
 *  IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 *  DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package jp.mydns.turenar.twclient.init.tree;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.atomic.AtomicInteger;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import jp.mydns.turenar.twclient.JobQueue;
import jp.mydns.turenar.twclient.init.InitializeException;
import jp.mydns.turenar.twclient.init.InitializeService;
import jp.mydns.turenar.twclient.init.Initializer;
import jp.mydns.turenar.twclient.init.InitializerInfo;
import jp.mydns.turenar.twclient.init.SplashScreenCtrl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Initialize Service Implementation which uses dynamic tree.
 *
 * @author Turenar (snswinhaiku dot lo at gmail dot com)
 */
public class TreeInitializeService extends InitializeService {
	/**
	 * weight for not unresolved
	 */
	protected static final int UNRESOLVED_WEIGHT = 0x10000;
	private static final int TIMEOUT = 100;
	/**
	 * do not mark final: test uses multiple instance
	 */
	@SuppressFBWarnings("MS_SHOULD_BE_FINAL")
	protected static TreeInitializeService instance = new TreeInitializeService();

	/**
	 * register this to {@link InitializeService}.
	 * If something is already registered, throw {@link IllegalStateException}.
	 *
	 * @return this instance
	 * @throws IllegalStateException something is already registered to InitializeService
	 */
	public static TreeInitializeService use() throws IllegalStateException {
		InitializeService.setService(instance);
		return instance;
	}

	/**
	 * map K: name, V: info
	 */
	protected final HashMap<String, TreeInitInfoBase> infoMap;
	/**
	 * flat tree.
	 */
	protected final TreeInfoList flatTree;
	/**
	 * unresolved relations. It should be resolved with {@link #rebuildTree()}
	 */
	protected final LinkedList<Relation> unresolvedRelations = new LinkedList<>();
	private boolean fastUninit;
	/*package*/volatile boolean treeRebuildRequired;
	private boolean isUninitialized;
	private Logger logger = LoggerFactory.getLogger(TreeInitializeService.class);
	private volatile AtomicInteger runningJobsCount = new AtomicInteger();
	private volatile JobQueue jobQueue;
	private InitializeException parallelException;

	/**
	 * For test.
	 */
	/*package*/ TreeInitializeService() {
		infoMap = new HashMap<>();
		flatTree = new TreeInfoList();
	}

	/**
	 * assert {@link #uninit(boolean)} is not called.
	 */
	protected synchronized void assertNotUninit() {
		if (isUninitialized) {
			throw new IllegalStateException();
		}
	}

	@Override
	public synchronized InitializeService enterPhase(String phase) throws InitializeException {
		assertNotUninit();
		String nameFromPhase = PhaseInitInfo.getNameFromPhase(phase);
		TreeInitInfoBase info = infoMap.get(nameFromPhase);
		if (info == null) {
			logger.warn("QA: phase {} is not registered.", phase);
			registerPhase(phase);
			rebuildTree();
			info = infoMap.get(nameFromPhase);
		}
		if (info instanceof PhaseInitInfo) {
			((PhaseInitInfo) info).resolve();
		} else {
			throw new InitializeException("unknown phase: " + phase + "; This is already registered not as phase: " + info);
		}
		treeRebuildRequired = true;
		waitConsumeQueue();
		return this;
	}

	/**
	 * finish init
	 *
	 * @param info init info
	 */
	protected synchronized void finish(TreeInitInfoBase info) {
		//noinspection ThrowableResultOfMethodCallIgnored
		InitializeException exception = info.getException();
		if (exception != null) {
			logger.error("failed initializer: {}", info.getName());
			if (parallelException == null) {
				parallelException = exception;
			} else {
				parallelException.addSuppressed(exception);
			}
			treeRebuildRequired = true;
		}
		runningJobsCount.decrementAndGet();
		notifyAll();
	}

	@Override
	public synchronized InitializerInfo getInfo(String name) {
		return infoMap.get(name);
	}

	/**
	 * get provider from providerName
	 *
	 * @param providerName provider name
	 * @return provider
	 */
	protected synchronized ProviderInitInfo getProvider(String providerName) {
		TreeInitInfoBase info = infoMap.get(providerName);
		if (info == null) {
			info = new ProviderInitInfo(providerName);
			infoMap.put(providerName, info);
			flatTree.add(info);
		}
		if (info instanceof ProviderInitInfo) {
			return (ProviderInitInfo) info;
		} else {
			throw new IllegalArgumentException("'" + providerName + "' is already registered not as provider: " + info);
		}
	}

	/**
	 * should do fast uninit?
	 *
	 * @return should do fast uninit?
	 */
	protected synchronized boolean isFastUninit() {
		return fastUninit;
	}

	@Override
	public synchronized boolean isInitialized(String name) {
		TreeInitInfoBase info = infoMap.get(name);
		return info != null && info.isInitialized();
	}

	@Override
	public synchronized boolean isRegistered(String name) {
		return infoMap.containsKey(name);
	}

	@Override
	public synchronized boolean isUninitialized() {
		return isUninitialized;
	}

	@Override
	public synchronized InitializeService provideInitializer(String name, boolean force) {
		assertNotUninit();
		TreeInitInfoBase info = infoMap.get(name);
		if (info == null) {
			info = new VirtualInitInfo(name);
			infoMap.put(name, info);
			flatTree.add(info);
		} else if (force) {
			info.provide();
		} else {
			throw new IllegalArgumentException(name + " is already registered!!!");
		}
		treeRebuildRequired = true;
		return this;
	}

	/**
	 * try to resolve all unresolved dependencies and rebuild flat tree
	 */
	protected synchronized void rebuildTree() {
		for (Iterator<Relation> iterator = unresolvedRelations.iterator(); iterator.hasNext(); ) {
			Relation unresolvedRelation = iterator.next();
			if (unresolvedRelation.tryResolve()) {
				iterator.remove();
			}
		}
		flatTree.rebuild();
	}

	@Override
	protected synchronized void register(Object instance, Method method,
			Initializer initializer) throws IllegalArgumentException {
		assertNotUninit();
		TreeInitInfo info = new TreeInitInfo(instance, method, initializer);
		infoMap.put(initializer.name(), info);
		flatTree.add(info);
		treeRebuildRequired = true;
	}

	@Override
	public synchronized InitializeService registerPhase(String phase) {
		assertNotUninit();
		PhaseInitInfo info = new PhaseInitInfo(phase);
		infoMap.put(info.getName(), info);
		flatTree.add(info);
		treeRebuildRequired = true;
		return this;
	}

	@Override
	public synchronized void setJobQueue(JobQueue jobQueue) {
		this.jobQueue = jobQueue;
	}

	@Override
	public synchronized void uninit(boolean fastUninit) throws InitializeException {
		this.fastUninit = fastUninit;
		isUninitialized = true;
		TreeInitInfoBase info;
		while ((info = flatTree.prev()) != null) {
			info.uninit(fastUninit);
		}
	}

	@Override
	public synchronized InitializeService waitConsumeQueue() throws IllegalStateException, InitializeException {
		assertNotUninit();
		while (treeRebuildRequired) {
			rebuildTree();
			treeRebuildRequired = false;
			TreeInitInfoBase info;

			REBUILD:
			while ((info = flatTree.next()) != null) {
				if (jobQueue == null) {
					info.invoke();
				} else {
					while (!info.isAllDependenciesInitialized()) {
						try {
							wait(TIMEOUT);
							if (treeRebuildRequired) {
								flatTree.unnext();
								break REBUILD; // back to rebuild tree
							}
						} catch (InterruptedException e) {
							Thread.currentThread().interrupt();
						}
					}
					runningJobsCount.incrementAndGet();
					jobQueue.addJob(JobQueue.PRIORITY_MAX, info);
				}
				SplashScreenCtrl.setProgress(flatTree.nowIndex(), flatTree.size());
				if (treeRebuildRequired) {
					break; // back to rebuild tree
				}
			}
			while (runningJobsCount.get() > 0) {
				try {
					wait(TIMEOUT);
				} catch (InterruptedException e) {
					Thread.currentThread().interrupt();
				}
			} // wait for parallel initialization: otherwise, we cause dead-lock uninit job queue.
			if (parallelException != null) {
				throw parallelException;
			}
		}
		return this;
	}
}
