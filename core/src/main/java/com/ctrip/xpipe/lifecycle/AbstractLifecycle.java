package com.ctrip.xpipe.lifecycle;

import com.ctrip.xpipe.api.lifecycle.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author wenchao.meng
 *
 * 2016年4月21日 下午4:59:41
 */
public abstract class AbstractLifecycle implements Lifecycle, LifecycleStateAware{
	
	protected Logger logger = LoggerFactory.getLogger(getClass());
	
	private LifecycleState lifecycleState;
	private LifecycleController lifecycleController;
	
	public AbstractLifecycle() {
		this.lifecycleController = new DefaultLifecycleController();
		this.lifecycleState = new DefaultLifecycleState(this, lifecycleController);
	}
	
	
	
	@Override
	public void initialize() throws Exception {

		// 获取声明周期 阶段名称
		String phaseName = lifecycleState.getPhaseName();
		// 如果 阶段名称为空 或者 结束， 则表示可以开始， 否则抛出一个异常
		if(!lifecycleController.canInitialize(phaseName)){
			logger.error("[initialize][can not init]{}, {}", phaseName, this);
			throw new IllegalStateException("can not initialize:" + phaseName + "," + this);
		}
		
		try{
			// 设置为 开始 状态
			lifecycleState.setPhaseName(Initializable.PHASE_NAME_BEGIN);
			// 执行初始化
			doInitialize();
			// 设置为 结束 状态
			lifecycleState.setPhaseName(Initializable.PHASE_NAME_END);
		}catch(Exception e){
			lifecycleState.rollback(e);
			throw e;
		}
	}
	
	protected void doInitialize() throws Exception{
		
	}

	@Override
	public void start() throws Exception {

		String phaseName = lifecycleState.getPhaseName();
		if(!lifecycleController.canStart(phaseName)){
			logger.error("[initialize][can not start]{},{}", phaseName, this);
			throw new IllegalStateException("can not start:" + phaseName + ", " + this);
		}
		
		try{
			lifecycleState.setPhaseName(Startable.PHASE_NAME_BEGIN);
			doStart();
			lifecycleState.setPhaseName(Startable.PHASE_NAME_END);
		}catch(Exception e){
			lifecycleState.rollback(e);
			throw e;
		}
	}
	protected void doStart() throws Exception{
		
	}

	@Override
	public void stop() throws Exception {

		String phaseName = lifecycleState.getPhaseName();
		if(!lifecycleController.canStop(phaseName)){
			logger.error("[initialize][can not stop]{}, {}" , phaseName, this);
			throw new IllegalStateException("can not stop:" + phaseName + "," + this);
		}
		
		try{
			lifecycleState.setPhaseName(Stoppable.PHASE_NAME_BEGIN);
			doStop();
			lifecycleState.setPhaseName(Stoppable.PHASE_NAME_END);
		}catch(Exception e){
			lifecycleState.rollback(e);
			throw e;
		}
	}

	protected void doStop() throws Exception{
		
	}

	@Override
	public void dispose() throws Exception {

		String phaseName = lifecycleState.getPhaseName();
		if(!lifecycleController.canDispose(phaseName)){
			logger.error("[initialize][can not stop]{}, {}" , phaseName, this);
			throw new IllegalStateException("can not dispose:" + phaseName + "," + this);
		}
		try{
			lifecycleState.setPhaseName(Disposable.PHASE_NAME_BEGIN);
			doDispose();
			lifecycleState.setPhaseName(Disposable.PHASE_NAME_END);
		}catch(Exception e){
			lifecycleState.rollback(e);
			throw e;
		}
	}

	protected void doDispose() throws Exception {
		
	}

	@Override
	public LifecycleState getLifecycleState(){
		
		return this.lifecycleState;
	}
	
	@Override
	public String toString() {
		
		return getClass().getSimpleName() + ", phase:" + lifecycleState.getPhaseName();
	}
	
	@Override
	public int getOrder() {
		return 0;
	}
}
