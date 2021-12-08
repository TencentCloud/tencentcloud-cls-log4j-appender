package com.tencentcloudapi.cls;

import com.tencentcloudapi.cls.producer.Callback;
import com.tencentcloudapi.cls.producer.Result;
import com.tencentcloudapi.cls.producer.common.LogItem;
import org.apache.log4j.helpers.LogLog;

/**
 * @author farmerx
 */
public class LoghubAppenderCallback  implements Callback {

    private String topicId;

    private String source;

    private LogItem logItem;

    public LoghubAppenderCallback( String topicId, String source, LogItem logItem) {
        super();
        this.topicId = topicId;
        this.source = source;
        this.logItem = logItem;
    }

    @Override
    public void onCompletion(Result result) {
        if (!result.isSuccessful()) {
            LogLog.error(
                    "Failed to send log, topic="  + topicId
                            + ", source=" + source
                            + ", logItem=" + logItem
                            + ", errorCode=" + result.getErrorCode()
                            + ", errorMessage=" + result.getErrorMessage());
        }
    }
}
