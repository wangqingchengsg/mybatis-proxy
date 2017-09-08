package com.zj.wqc.my.proxy;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;

public class ProxyDBTManager extends DataSourceTransactionManager {

	private static final long serialVersionUID = -5711464021772115686L;

	@Override
    protected void doBegin(Object transaction, TransactionDefinition definition) {
		ProxyDBGlobal proxy = ProxyDBHolder.get();
		if(!(proxy != null && proxy.getProxyDB().equals(ProxyDBGlobal.Global.getProxyDB()))){
			ProxyDBHolder.set(ProxyDBGlobal.WRITE);
		}
        super.doBegin(transaction, definition);
    }

    @Override
    protected void doCleanupAfterCompletion(Object transaction) {
        super.doCleanupAfterCompletion(transaction);
        ProxyDBGlobal proxy = ProxyDBHolder.get();
        if(!(proxy != null && proxy.getProxyDB().equals(ProxyDBGlobal.Global.getProxyDB()))){
			ProxyDBHolder.set(ProxyDBGlobal.WRITE);
		}
    }
}
