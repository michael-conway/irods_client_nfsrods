package org.irods.nfsrods.config;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ConnectionManagementConfig
{
    // @formatter:off
    public static final String MODE_SIMPLE = "simple";
    public static final String MODE_CACHE  = "cache";
    public static final String MODE_PROXY  = "proxy";

    @JsonProperty("mode")                                                   private String mode_;
    @JsonProperty("cache_minimum_evictable_idle_time_in_milliseconds")      private int cacheMinEvictableIdleTimeInMillis_;
    @JsonProperty("cache_soft_minimum_evictable_idle_time_in_milliseconds") private int cacheSoftMinEvictableIdleTimeInMillis_;
    @JsonProperty("cache_maximum_idle_connections_per_key")                 private int cacheMaxIdleConnectionsPerKey_;
    @JsonProperty("cache_test_on_return")                                   private boolean cacheTestOnReturn_;
    @JsonProperty("cache_test_on_borrow")                                   private boolean cacheTestOnBorrow_;
    @JsonProperty("cache_enable_jmx")                                       private boolean cacheEnableJmx_;

    ConnectionManagementConfig(@JsonProperty("mode")                                                   String _mode,
                               @JsonProperty("cache_minimum_evictable_idle_time_in_milliseconds")      Integer _cacheMinEvictableIdleTimeInMillis,
                               @JsonProperty("cache_soft_minimum_evictable_idle_time_in_milliseconds") Integer _cacheSoftMinEvictableIdleTimeInMillis,
                               @JsonProperty("cache_maximum_idle_connections_per_key")                 Integer _cacheMaxIdleConnectionsPerKey,
                               @JsonProperty("cache_test_on_return")                                   Boolean _cacheTestOnReturn,
                               @JsonProperty("cache_test_on_borrow")                                   Boolean _cacheTestOnBorrow,
                               @JsonProperty("cache_enable_jmx")                                       Boolean _cacheEnableJmx)
    {
        setMode(_mode);
        
        if (MODE_CACHE.equals(mode_))
        {
            ConfigUtils.throwIfNull(cacheMinEvictableIdleTimeInMillis_, "cache_minimum_evictable_idle_time_in_milliseconds");
            ConfigUtils.throwIfNull(cacheSoftMinEvictableIdleTimeInMillis_, "cache_soft_minimum_evictable_idle_time_in_milliseconds");
            ConfigUtils.throwIfNull(cacheMaxIdleConnectionsPerKey_, "cache_maximum_idle_connections_per_key");
            ConfigUtils.throwIfNull(cacheTestOnReturn_, "cache_test_on_return");
            ConfigUtils.throwIfNull(cacheTestOnBorrow_, "cache_test_on_borrow");
            ConfigUtils.throwIfNull(cacheEnableJmx_, "cache_enable_jmx");

            cacheMinEvictableIdleTimeInMillis_ = _cacheMinEvictableIdleTimeInMillis;
            cacheSoftMinEvictableIdleTimeInMillis_ = _cacheSoftMinEvictableIdleTimeInMillis;
            cacheMaxIdleConnectionsPerKey_ = _cacheMaxIdleConnectionsPerKey;
            cacheTestOnReturn_ = _cacheTestOnReturn;
            cacheTestOnBorrow_ = _cacheTestOnBorrow;
            cacheEnableJmx_ = _cacheEnableJmx;
        }
    }
    // @formatter:on

    @JsonIgnore
    public String getMode()
    {
        return mode_;
    }

    @JsonIgnore
    public int getCacheMinimumEvictableIdleTimeInMilliseconds()
    {
        return cacheMinEvictableIdleTimeInMillis_;
    }

    @JsonIgnore
    public int getCacheSoftMinimumEvictableIdleTimeInMilliseconds()
    {
        return cacheSoftMinEvictableIdleTimeInMillis_;
    }

    @JsonIgnore
    public int getCacheMaximumIdleConnectionsPerKey()
    {
        return cacheMaxIdleConnectionsPerKey_;
    }

    @JsonIgnore
    public boolean isTestOnReturnEnabled()
    {
        return cacheTestOnReturn_;
    }

    @JsonIgnore
    public boolean isTestOnBorrowEnabled()
    {
        return cacheTestOnBorrow_;
    }

    @JsonIgnore
    public boolean isJmxEnabled()
    {
        return cacheEnableJmx_;
    }
    
    private void setMode(String _mode)
    {
        if (null == _mode)
        {
            mode_ = "simple";
            return;
        }

        if (Lists.newArrayList(MODE_SIMPLE, MODE_CACHE, MODE_PROXY)
             .parallelStream()
             .noneMatch(m -> m.equals(_mode)))
        {
            throw new IllegalArgumentException("Invalid connection management mode");
        }
        
        mode_ = _mode;
    }
}
