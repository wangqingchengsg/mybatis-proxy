package com.zj.wqc.my.proxy;

import java.util.Locale;
import java.util.Properties;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Intercepts({
@Signature(type = Executor.class, method = "update", args = {
        MappedStatement.class, Object.class }),
@Signature(type = Executor.class, method = "query", args = {
        MappedStatement.class, Object.class, RowBounds.class,
        ResultHandler.class }) })
public class ProxyMybatisPlugin implements Interceptor {

    protected static final Logger logger = LoggerFactory.getLogger(ProxyMybatisPlugin.class);

    private static final String REGEX = ".*insert\\u0020.*|.*delete\\u0020.*|.*update\\u0020.*";

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        boolean synchronizationActive = TransactionSynchronizationManager.isSynchronizationActive();
        //使用事物自己的读写方�?
        if(synchronizationActive) {
        	return invocation.proceed();
        }
        
        //当使用全�?写操作，后面�?有的走写
        if(ProxyDBHolder.get() != null && 
        		ProxyDBHolder.get() == ProxyDBGlobal.Global){
        	return invocation.proceed();
        	
        }
        Object[] objects = invocation.getArgs();
        MappedStatement ms = (MappedStatement) objects[0];
        ProxyDBGlobal dynamicDataSourceGlobal = null;
        
        if(ms.getSqlCommandType().equals(SqlCommandType.SELECT)) {
            if(ms.getId().contains(SelectKeyGenerator.SELECT_KEY_SUFFIX)) {
                dynamicDataSourceGlobal = ProxyDBGlobal.WRITE;
            } else {
                BoundSql boundSql = ms.getSqlSource().getBoundSql(objects[1]);
                String sql = boundSql.getSql().toLowerCase(Locale.CHINA).replaceAll("[\\t\\n\\r]", " ");
                if(sql.matches(REGEX)) {
                    dynamicDataSourceGlobal = ProxyDBGlobal.WRITE;
                } else {
                    dynamicDataSourceGlobal = ProxyDBGlobal.READ;
                }
            }
        }else{
            dynamicDataSourceGlobal = ProxyDBGlobal.WRITE;
        }
        ProxyDBHolder.set(dynamicDataSourceGlobal);
        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        if (target instanceof Executor) {
            return Plugin.wrap(target, this);
        } else {
            return target;
        }
    }

    @Override
    public void setProperties(Properties properties) {}
}
