package com.zj.wqc.my.proxy;

public final class ProxyDBHolder {

    private static final ThreadLocal<ProxyDBGlobal> holder = new ThreadLocal<ProxyDBGlobal>();

    private ProxyDBHolder() {}

    public static void set(ProxyDBGlobal dataSource){
        holder.set(dataSource);
    }

    public static ProxyDBGlobal get(){
        return holder.get();
    }

    public static void clear() {
        holder.remove();
    }

}
