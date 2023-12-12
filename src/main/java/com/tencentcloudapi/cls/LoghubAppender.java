package com.tencentcloudapi.cls;

import com.tencentcloudapi.cls.producer.AsyncProducerClient;
import com.tencentcloudapi.cls.producer.AsyncProducerConfig;
import com.tencentcloudapi.cls.producer.common.LogContent;
import com.tencentcloudapi.cls.producer.common.LogItem;
import com.tencentcloudapi.cls.producer.errors.ProducerException;
import com.tencentcloudapi.cls.producer.util.NetworkUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.ThrowableInformation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
/**
 * @author farmerx
 */
public class LoghubAppender extends AppenderSkeleton {

    /**
     * Tencent Cloud CLS Log Topic ID
     */
    private String topicId;
    /**
     * Tencent Cloud CLS Endpoint
     */
    private String endpoint;
    /**
     * Tencent Cloud Access Key ID
     */
    private String accessKeyId;
    /**
     * Tencent Cloud Access Key Secret
     */
    private String accessKeySecret;
    /**
     * User Agent log4j
     */
    private String userAgent = "log4j";
    /**
     * 来源ip
     */
    private String source = "";

    private String timeFormat = "yyyy-MM-dd'T'HH:mm:ssZ";
    private String timeZone = "UTC";

    private String totalSizeInBytes;
    private String maxBlockMs;
    private String sendThreadCount;
    private String batchSizeThresholdInBytes;
    private String batchCountThreshold;
    private String lingerMs;
    private String retries;
    private String maxReservedAttempts;
    private String baseRetryBackoffMs;
    private String maxRetryBackoffMs;

    /**
     * producer cls java sdk client
     */
    private AsyncProducerClient producer;

    /**
     * producer cls java sdk client config
     */
    private AsyncProducerConfig producerConfig;

    private DateTimeFormatter formatter;

    /**
     * getEndpoint 获取接入点
     * @return string
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * setEndpoint 设置cls 接入点
     * @param endpoint point
     */
    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    /**
     * getAccessKeyId 获取accessKeyID
     * @return string
     */
    public String getAccessKeyId() {
        return accessKeyId;
    }

    /**
     * setAccessKeyId 设置AccessKeyId
     * @param accessKeyId key
     */
    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    /**
     * getAccessKeySecret 获取AccessKeySecret
     * @return string
     */
    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    /**
     * setAccessKeySecret 设置AccessKeySecret
     * @param accessKeySecret secret key
     */
    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    /**
     * getUserAgent 获取UserAgent
     * @return user agent
     */
    public String getUserAgent() {
        return userAgent;
    }

    /**
     * setUserAgent 设置UserAgent
     * @param userAgent user agent
     */
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }

    public String getTotalSizeInBytes() {
        return totalSizeInBytes;
    }

    public void setTotalSizeInBytes(String totalSizeInBytes) {
        this.totalSizeInBytes = totalSizeInBytes;
    }

    public String getMaxBlockMs() {
        return maxBlockMs;
    }

    public void setMaxBlockMs(String maxBlockMs) {
        this.maxBlockMs = maxBlockMs;
    }

    public String getSendThreadCount() {
        return sendThreadCount;
    }

    public void setSendThreadCount(String sendThreadCount) {
        this.sendThreadCount = sendThreadCount;
    }

    public String getBatchSizeThresholdInBytes() {
        return batchSizeThresholdInBytes;
    }

    public void setBatchSizeThresholdInBytes(String batchSizeThresholdInBytes) {
        this.batchSizeThresholdInBytes = batchSizeThresholdInBytes;
    }

    public String getBatchCountThreshold() {
        return batchCountThreshold;
    }

    public void setBatchCountThreshold(String batchCountThreshold) {
        this.batchCountThreshold=batchCountThreshold;
    }

    public String getLingerMs() {
        return lingerMs;
    }

    public void setLingerMs(String lingerMs) {
        this.lingerMs = lingerMs;
    }

    public String getRetries() {
        return retries;
    }

    public void setRetries(String retries) {
        this.retries = retries;
    }

    public String getMaxReservedAttempts() {
        return maxReservedAttempts;
    }

    public void setMaxReservedAttempts(String maxReservedAttempts) {
        this.maxReservedAttempts = maxReservedAttempts;
    }

    public String getBaseRetryBackoffMs() {
        return baseRetryBackoffMs;
    }

    public void setBaseRetryBackoffMs(String baseRetryBackoffMs) {
        this.baseRetryBackoffMs = baseRetryBackoffMs;
    }

    public String getMaxRetryBackoffMs() {
        return maxRetryBackoffMs;
    }

    public void setMaxRetryBackoffMs(String maxRetryBackoffMs) {
        this.maxRetryBackoffMs = maxRetryBackoffMs;
    }

    public String getTopicID() {
        return topicId;
    }

    public void setTopicID(String topicId) {
        this.topicId = topicId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTimeFormat() {
        return timeFormat;
    }

    public void setTimeFormat(String timeFormat) {
        this.timeFormat = timeFormat;
    }

    public String getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(String timeZone) {
        this.timeZone = timeZone;
    }

    @Override
    public void activateOptions() {
        super.activateOptions();
        formatter = DateTimeFormat.forPattern(timeFormat).withZone(DateTimeZone.forID(timeZone));
        if (source==null || source.isEmpty()) {
            source = NetworkUtils.getLocalMachineIP();
        }

        producerConfig = new AsyncProducerConfig(endpoint, accessKeyId, accessKeySecret, source);
        this.setProduceConfig();
        producer = new AsyncProducerClient(producerConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                doClose();
            } catch (Exception e) {
                LogLog.error("Failed to close LoghubAppender.", e);
            }
        }));
    }

    @Override
    public void close() {
        try {
            doClose();
        } catch (Exception e) {
            LogLog.error("Failed to close LoghubAppender.", e);
        }
    }

    private void doClose() throws ProducerException, InterruptedException {
        producer.close();
    }

    @Override
    public boolean requiresLayout() {
        return true;
    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        LogItem logItem = new LogItem(loggingEvent.getTimeStamp());
        DateTime dateTime = new DateTime(loggingEvent.getTimeStamp());
        logItem.PushBack("time", dateTime.toString(formatter));
        logItem.PushBack(new LogContent("level", loggingEvent.getLevel().toString()));
        logItem.PushBack(new LogContent("thread", loggingEvent.getThreadName()));
        logItem.PushBack(new LogContent("location", loggingEvent.getLocationInformation().fullInfo));

        Object message = loggingEvent.getMessage();
        if(message==null){
            logItem.PushBack(new LogContent("message", ""));
        }else{
            logItem.PushBack(new LogContent("message", loggingEvent.getMessage().toString()));
        }

        String throwable = getThrowableStr(loggingEvent);
        if (throwable != null) {
            logItem.PushBack(new LogContent("throwable", throwable));
        }

        if (getLayout() != null) {
            logItem.PushBack(new LogContent("log", getLayout().format(loggingEvent)));
        }

        Map properties = loggingEvent.getProperties();
        if (properties.size() > 0) {
            Object[] keys = properties.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                logItem.PushBack(
                        new LogContent(key.toString(), properties.get(key).toString())
                );
            }
        }

        try {
            List<LogItem> logItems = new ArrayList<>();
            logItems.add(logItem);
            producer.putLogs(
                    topicId,
                    logItems,
                    new LoghubAppenderCallback(topicId, source, logItem)
            );
        } catch (Exception e) {
            LogLog.error("Failed to send log, topicId=" + topicId
                    + ", source=" + source
                    + ", logItem=" + logItem, e);
        }
    }
    private String getThrowableStr(LoggingEvent event) {
        ThrowableInformation throwable = event.getThrowableInformation();
        if (throwable == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        boolean isFirst = true;
        for (String s : throwable.getThrowableStrRep()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sb.append(System.getProperty("line.separator"));
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private void setProduceConfig() {
        if (totalSizeInBytes != null && !totalSizeInBytes.isEmpty()) {
            producerConfig.setTotalSizeInBytes(Integer.parseInt(totalSizeInBytes));
        }
        if (maxBlockMs != null && !maxBlockMs.isEmpty()) {
            producerConfig.setMaxBlockMs(Long.parseLong(maxBlockMs));
        }
        if (sendThreadCount != null && !sendThreadCount.isEmpty()) {
            producerConfig.setSendThreadCount(Integer.parseInt(sendThreadCount));
        }
        if (batchSizeThresholdInBytes != null && !batchSizeThresholdInBytes.isEmpty()) {
            producerConfig.setBatchSizeThresholdInBytes(Integer.parseInt(batchSizeThresholdInBytes));
        }
        if (batchCountThreshold != null && !batchCountThreshold.isEmpty()) {
            producerConfig.setBatchCountThreshold(Integer.parseInt(batchCountThreshold));
        }
        if (lingerMs != null && !lingerMs.isEmpty()) {
            producerConfig.setLingerMs(Integer.parseInt(lingerMs));
        }
        if (retries != null && !retries.isEmpty()) {
            producerConfig.setRetries(Integer.parseInt(retries));
        }
        if (maxReservedAttempts != null && !maxReservedAttempts.isEmpty()) {
            producerConfig.setMaxReservedAttempts(Integer.parseInt(maxReservedAttempts));
        }
        if (baseRetryBackoffMs !=null && !baseRetryBackoffMs.isEmpty()) {
            producerConfig.setBaseRetryBackoffMs(Long.parseLong(baseRetryBackoffMs));
        }
        if (maxRetryBackoffMs !=null && !maxRetryBackoffMs.isEmpty()) {
            producerConfig.setMaxRetryBackoffMs(Long.parseLong(maxRetryBackoffMs));
        }
    }
}
