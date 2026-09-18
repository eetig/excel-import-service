package org.example.excel.dto;
import lombok.Data;
import java.util.HashMap;
import java.util.Map;

@Data
public class GoodsMoveExcelDTO {
    private String orderNo;          // 订单
    private String materialCode;     // 物料
    private String movementFlag;     // 移动标识
    private String item;             // 项目
    private String materialDesc;     // 物料描述
    private String entryQuantity;    // 以录入单位表示的数量
    private String batchNo;          // 批次
    private String storageLocation;  // 存储地点
    private String unit;             // 基本计量单位
    private String movementType;     // 移动类型
    private String materialDoc;      // 物料凭证
    private String creditFlag;       // 借/贷标识
    private String quantity;         // 数量
    private String postingDate;      // 过账日期

    // 用来存放WPS表格里额外新增的动态扩展列
    private Map<String, Object> extMap = new HashMap<>();
}
