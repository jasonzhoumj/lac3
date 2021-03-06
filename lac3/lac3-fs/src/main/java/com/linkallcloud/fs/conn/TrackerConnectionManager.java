package com.linkallcloud.fs.conn;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.linkallcloud.fs.proto.FdfsCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.linkallcloud.fs.domain.TrackerLocator;
import com.linkallcloud.fs.exception.FdfsConnectException;

/**
 * 管理TrackerClient连接池分配
 * 
 */
@Component
public class TrackerConnectionManager extends ConnectionManager {

    /** Tracker定位 */
    private TrackerLocator trackerLocator;

    /** tracker服务配置地址列表 */
    //FIXME 不空
    @Value("#{FDFS['fdfs.trackerList'].split(',')}")
    private List<String> trackerList = new ArrayList<String>();

    /** 构造函数 */
    public TrackerConnectionManager() {
        super();
    }

    /** 构造函数 */
    public TrackerConnectionManager(FdfsConnectionPool pool) {
        super(pool);
    }

    /** 初始化方法 */
    @PostConstruct
    public void initTracker() {
        LOGGER.debug("init trackerLocator {}", trackerList);
        trackerLocator = new TrackerLocator(trackerList);
    }

    /**
     * 获取连接并执行交易
     * 
     * @param address
     * @param command
     * @return
     */
    public <T> T executeFdfsTrackerCmd(FdfsCommand<T> command) {
        Connection conn = null;
        InetSocketAddress address = null;
        // 获取连接
        try {
            address = trackerLocator.getTrackerAddress();
            LOGGER.debug("获取到Tracker连接地址{}", address);
            conn = getConnection(address);
            trackerLocator.setActive(address);
        } catch (FdfsConnectException e) {
            trackerLocator.setInActive(address);
            throw e;
        } catch (Exception e) {
            LOGGER.error("Unable to borrow buffer from pool", e);
            throw new RuntimeException("Unable to borrow buffer from pool", e);
        }
        // 执行交易
        return execute(address, conn, command);
    }

    public List<String> getTrackerList() {
        return trackerList;
    }

    public void setTrackerList(List<String> trackerList) {
        this.trackerList = trackerList;
    }
}
