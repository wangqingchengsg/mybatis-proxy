package com.zj.wqc.my.proxy;

public enum ProxyDBGlobal {
    READ("read","slave DB"), WRITE("write","master DB"),
    
    Global("global_write","global_write master DB");
    
    private String proxyDB;

    private String proxyDBMsg;

    ProxyDBGlobal(String proxyDB, String proxyDBMsg){
    	this.proxyDB = proxyDB;
    	this.proxyDBMsg = proxyDBMsg;
    }

	public String getProxyDB() {
		return proxyDB;
	}

	public void setProxyDB(String proxyDB) {
		this.proxyDB = proxyDB;
	}

	public String getProxyDBMsg() {
		return proxyDBMsg;
	}

	public void setProxyDBMsg(String proxyDBMsg) {
		this.proxyDBMsg = proxyDBMsg;
	}
}
