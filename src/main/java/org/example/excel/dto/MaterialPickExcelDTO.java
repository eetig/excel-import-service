package org.example.excel.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

@Data
public class MaterialPickExcelDTO {
    @ExcelProperty("工单编号")
    private String orderNo;
    @ExcelProperty("物料名称")
    private String materialName;
    @ExcelProperty("数量")
    private Integer num;
    // 你所有Excel对应的字段
}
