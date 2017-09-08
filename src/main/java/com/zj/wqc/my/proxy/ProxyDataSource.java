package com.zj.wqc.my.proxy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class ProxyDataSource extends AbstractRoutingDataSource {

    private Object masterDB;

    private Map<Object, Object> slaveMapDB;

    private static final List<Object> slaveListDB = new ArrayList<Object>();
    //读数据源个数
    private static int SLAVE_DB_SIZE = 1;

    //获取读数据源方式�?0：随机，1：轮�?
    private int slaveAcceptWay = 1; 

    private static final AtomicLong counter = new AtomicLong(0);

    private static final Long MAX_POOL = Long.MAX_VALUE - 100000L;

    private static final Lock lock = new ReentrantLock();
    
    public Object getMasterDB() {
		return masterDB;
	}

	public void setMasterDB(Object masterDB) {
		this.masterDB = masterDB;
	}

	public Map<Object, Object> getSlaveMapDB() {
		return slaveMapDB;
	}

	public void setSlaveMapDB(Map<Object, Object> slaveMapDB) {
		this.slaveMapDB = slaveMapDB;
	}

	public int getSlaveAcceptWay() {
		return slaveAcceptWay;
	}

	public void setSlaveAcceptWay(int slaveAcceptWay) {
		this.slaveAcceptWay = slaveAcceptWay;
	}

	@Override
    public void afterPropertiesSet() {
        if (this.masterDB == null) {
            throw new IllegalArgumentException("Property 'masterDB' is required");
        }
        if (this.slaveMapDB == null) {
        	slaveMapDB = new HashMap<Object, Object>();
        }
        for (Map.Entry<Object, Object> entry : slaveMapDB.entrySet()){
        	 slaveListDB.add(entry.getKey());
        }
        if (slaveListDB.size() > 0){
        	SLAVE_DB_SIZE = slaveListDB.size();
        }
        if(!slaveMapDB.containsValue(masterDB)){
        	slaveMapDB.put(ProxyDBGlobal.WRITE.getProxyDB(), masterDB);
        }
        setDefaultTargetDataSource(masterDB);
        setTargetDataSources(slaveMapDB);
        super.afterPropertiesSet();
    }

    @Override
    protected Object determineCurrentLookupKey() {

        ProxyDBGlobal dynamicDataSourceGlobal = ProxyDBHolder.get();

        if(dynamicDataSourceGlobal == null
                || dynamicDataSourceGlobal == ProxyDBGlobal.WRITE ||
                dynamicDataSourceGlobal == ProxyDBGlobal.Global) {
            return ProxyDBGlobal.WRITE.name();
        }
        
        int index = 1;
        if(slaveAcceptWay == 1) {
            long currValue = counter.incrementAndGet();
            if((currValue + 1) >= MAX_POOL) {
                try {
                    lock.lock();
                    if(counter.incrementAndGet() >= MAX_POOL) {
                        counter.set(0);
                    }
                } finally {
                    lock.unlock();
                }
            }
            index = (int) (currValue % SLAVE_DB_SIZE);
        } else {
            index = ThreadLocalRandom.current().nextInt(0, SLAVE_DB_SIZE);
        }
        System.out.println(slaveListDB.get(index));
        return slaveListDB.get(index);
    }
}
