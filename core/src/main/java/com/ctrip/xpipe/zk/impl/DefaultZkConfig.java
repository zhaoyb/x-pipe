package com.ctrip.xpipe.zk.impl;

import com.ctrip.xpipe.api.codec.Codec;
import com.ctrip.xpipe.utils.XpipeThreadFactory;
import com.ctrip.xpipe.zk.ZkConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.CuratorFrameworkFactory.Builder;
import org.apache.curator.retry.RetryNTimes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @author wenchao.meng
 *
 * Jun 23, 2016
 */
public class DefaultZkConfig implements ZkConfig{
	
	private Logger logger = LoggerFactory.getLogger(getClass());

	/**
	 * zk 命名空间 key
	 */
	public static String KEY_ZK_NAMESPACE = "key_zk_namespace";

	/**
	 * session 超时时间
	 *
	 */
	private int zkSessionTimeoutMillis = Integer.parseInt(System.getProperty("ZK.SESSION.TIMEOUT", "5000"));
	/**
	 * 连接超时时间
	 *
	 */
	private int zkConnectionTimeoutMillis = Integer.parseInt(System.getProperty("ZK.CONN.TIMEOUT", "3000"));
	/**
	 * 重试次数
	 *
	 */
	private int zkRetries = 3;
	/**
	 * 默认从环境变量中读取zk 命名空间
	 *
	 */
	private String zkNameSpace = System.getProperty(KEY_ZK_NAMESPACE, DEFAULT_ZK_NAMESPACE);
	
	@Override
	public int getZkConnectionTimeoutMillis() {
		return zkConnectionTimeoutMillis;
	}

	public void setZkConnectionTimeoutMillis(int zkConnectionTimeoutMillis) {
		this.zkConnectionTimeoutMillis = zkConnectionTimeoutMillis;
	}

	@Override
	public int getZkCloseWaitMillis() {
		return 1000;
	}

	@Override
	public String getZkNamespace() {
		return zkNameSpace;
	}
	
	public void setZkNameSpace(String zkNameSpace) {
		this.zkNameSpace = zkNameSpace;
	}

	@Override
	public int getZkRetries() {
		return zkRetries;
	}

	public void setZkRetries(int zkRetries) {
		this.zkRetries = zkRetries;
	}
	
	@Override
	public int getSleepMsBetweenRetries() {
		return 100;
	}

	@Override
	public int getZkSessionTimeoutMillis() {
		return zkSessionTimeoutMillis;
	}
	
	public void setZkSessionTimeoutMillis(int zkSessionTimeoutMillis) {
		this.zkSessionTimeoutMillis = zkSessionTimeoutMillis;
	}

	@Override
	public int waitForZkConnectedMillis() {
		return 5000;
	}

	/**
	 *  创建zk连接，使用的是Curator客户端
	 *
	 * @param address
	 * @return
	 * @throws InterruptedException
	 */
	@Override
	public CuratorFramework create(String address) throws InterruptedException {

		Builder builder = CuratorFrameworkFactory.builder();
		builder.connectionTimeoutMs(getZkConnectionTimeoutMillis());
		builder.connectString(address);
		builder.maxCloseWaitMs(getZkCloseWaitMillis());
		builder.namespace(getZkNamespace());
		builder.retryPolicy(new RetryNTimes(getZkRetries(), getSleepMsBetweenRetries()));
		builder.sessionTimeoutMs(getZkSessionTimeoutMillis());
		// 自定义线程工厂
		builder.threadFactory(XpipeThreadFactory.create("Xpipe-ZK-" + address, true));

		logger.info("[create]{}, {}", Codec.DEFAULT.encode(this), address);
		CuratorFramework curatorFramework = builder.build();
		curatorFramework.start();
		curatorFramework.blockUntilConnected(waitForZkConnectedMillis(), TimeUnit.MILLISECONDS);
		
		return curatorFramework;
	}
	
}
