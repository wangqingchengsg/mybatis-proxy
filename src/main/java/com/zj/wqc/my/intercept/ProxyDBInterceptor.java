package com.zj.wqc.my.intercept;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;

import com.zj.wqc.my.proxy.ProxyDBHolder;
public class ProxyDBInterceptor implements MethodInterceptor{

    ThreadLocal<Integer> thre = new ThreadLocal<Integer>();

	@Override
	public Object invoke(MethodInvocation invocation) throws Throwable {
		thre.set(thre.get()== null ? 1 : thre.get() + 1);
		Object obj = null;
		try {
			obj = invocation.proceed();
		}finally{
			thre.set(thre.get() - 1);
			if(thre.get().intValue() == 0){
				ProxyDBHolder.clear();
			}
		}
		return obj;
	}

}
