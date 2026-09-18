package org.example.excel.listener;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import java.util.ArrayList;
import java.util.List;

// 重点：增加<T>泛型参数
public class CommonExcelListener<T> extends AnalysisEventListener<T> {

    private final List<T> dataList = new ArrayList<>();
    private final List<String> errorMsgList = new ArrayList<>();

    @Override
    public void invoke(T data, AnalysisContext context) {
        dataList.add(data);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
    }

    public List<T> getDataList() {
        return dataList;
    }

    public List<String> getErrorMsgList() {
        return errorMsgList;
    }
}
