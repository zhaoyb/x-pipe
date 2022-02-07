package com.ctrip.xpipe.lifecycle;

import com.ctrip.xpipe.api.lifecycle.ComponentRegistry;
import com.ctrip.xpipe.api.lifecycle.Lifecycle;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 
 * @author wenchao.meng
 *
 * Jun 12, 2016
 */
public class CreatedComponentRedistry extends AbstractComponentRegistry implements ComponentRegistry{
	
	private Map<String, Object> components = new ConcurrentHashMap<>();
	
	private NameCreater nameCreator = new DefaultNameCreator();
	
	@Override
	public String doAdd(Object component) throws Exception {

		// 获取组件名称
		String name = getName(component);
		// 添加到组件集合
		doAdd(name, component);
		return name;
	}

	private String getName(Object component) {
		return nameCreator.getName(component);
	}

	
	@Override
	protected Object doRemoveOfName(String name) {
		return components.remove(name);
	}
	
	@Override
	public boolean doRemove(Object component) throws Exception {
		
		String name = null;
		for(Entry<String, Object> entry : components.entrySet()){
			if(entry.getValue() == component){
				name = entry.getKey();
				break;
			}
		}
		
		if(name == null){
			logger.info("[doRemove][can not find component]{}", component);
			return false;
		}
		
		logger.info("[doRemove]{}, {}" , name, component);
		components.remove(name);
		
		if(component instanceof Lifecycle){
			Lifecycle lifecycle = (Lifecycle) component;
			
			if(lifecycle.getLifecycleState() != null){
				if(lifecycle.getLifecycleState().canStop()){
					lifecycle.stop();
				}
				
				if(lifecycle.getLifecycleState().canDispose()){
					lifecycle.dispose();
				}
			}
		}
		return true;
	}

	@Override
	public Object getComponent(String name) {
		return components.get(name);
	}


	@SuppressWarnings("unchecked")
	@Override
	protected <T> Map<String, T> doGetComponents(Class<T> clazz) {
		
		Map<String, T> result = new HashMap<>();
		for(Entry<String, Object> entry : components.entrySet()){
			if(clazz.isAssignableFrom(entry.getValue().getClass())){
				result.put(entry.getKey(), (T) entry.getValue());
			}
		}
		return result;
	}

	/**
	 *  添加一个组件， 在添加进去一个组件后， 同时调用了初始化昂发
	 *
	 * @param name
	 * @param component
	 * @throws Exception
	 */
	@Override
	protected void doAdd(String name, Object component) throws Exception {

		// 如果这个组件有生命周期
		if(component instanceof Lifecycle){
			
			Lifecycle lifecycle = (Lifecycle) component;
			if(lifecycle.getLifecycleState() != null){
				if(getLifecycleState().isInitializing() || getLifecycleState().isInitialized()){
					// 组件能够初始化，
					if(lifecycle.getLifecycleState().canInitialize()){
						// 组件初始化
						lifecycle.initialize();
					}
				}
				
				if(getLifecycleState().isStarting() || getLifecycleState().isStarted()){
					if(lifecycle.getLifecycleState().canStart()){
						lifecycle.start();
					}
				}
				
				if(getLifecycleState().isStopping() || getLifecycleState().isPositivelyStopped()){
					if(lifecycle.getLifecycleState().canStop()){
						lifecycle.stop();
					}
				}
				
				if(getLifecycleState().isDisposing() || getLifecycleState().isPositivelyDisposed()){
					if(lifecycle.getLifecycleState().canDispose()){
						lifecycle.dispose();
					}
				}

			}
		}
		// 添加到map
		components.put(name, component);
	}

	@Override
	public Map<String, Object> allComponents() {
		return new HashMap<>(components);
	}

	@Override
	public List<Lifecycle> lifecycleCallable() {
		
		List<Lifecycle> result = new LinkedList<>();
		
		for(Entry<String, Object> entry : components.entrySet()){
			if(entry.getValue() instanceof Lifecycle){
				result.add((Lifecycle)entry.getValue());
			}
		}
		return sort(result);
	}

	@Override
	public void cleanComponents() {
		components.clear();
	}
	
}
