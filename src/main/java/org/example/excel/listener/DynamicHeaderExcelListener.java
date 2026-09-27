package org.example.excel.listener;
import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import lombok.Getter;
import org.example.dto.GoodsMoveExcelDTO;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
public class DynamicHeaderExcelListener extends AnalysisEventListener<Map<Integer, String>> {
    // 存储表头映射：下标 -> 表头名称
    private List<String> headerNameList = new ArrayList<>();
    // 解析完成后的业务数据
    private final List<GoodsMoveExcelDTO> dataList = new ArrayList<>();
    private final List<String> errorMsgList = new ArrayList<>();

    // 读取表头回调
    @Override
    public void invokeHeadMap(Map<Integer, String> headMap, AnalysisContext context) {
        headerNameList = new ArrayList<>(headMap.values());
    }

    // 逐行读取数据
    @Override
    public void invoke(Map<Integer, String> rowMap, AnalysisContext context) {
        GoodsMoveExcelDTO dto = new GoodsMoveExcelDTO();
        Map<String,Object> extMap = dto.getExtMap();

        // 遍历该行所有单元格，按表头名填充
        for (int i = 0; i < headerNameList.size(); i++) {
            String header = headerNameList.get(i) == null ? "" : headerNameList.get(i).trim();
            String cellValue = rowMap.getOrDefault(i, "");
            switch (header) {
                case "订单": dto.setOrderNo(cellValue); break;
                case "物料": dto.setMaterialCode(cellValue); break;
                case "移动标识": dto.setMovementFlag(cellValue); break;
                case "项目": dto.setItem(cellValue); break;
                case "物料描述": dto.setMaterialDesc(cellValue); break;
                case "以录入单位表示的数量": dto.setEntryQuantity(cellValue); break;
                case "批次": dto.setBatchNo(cellValue); break;
                case "存储地点": dto.setStorageLocation(cellValue); break;
                case "基本计量单位": dto.setUnit(cellValue); break;
                case "移动类型": dto.setMovementType(cellValue); break;
                case "物料凭证": dto.setMaterialDoc(cellValue); break;
                case "借/贷标识": dto.setCreditFlag(cellValue); break;
                case "数量": dto.setQuantity(cellValue); break;
                case "过账日期": dto.setPostingDate(cellValue); break;
                default:
                    // 不在固定字段里的，全部放进扩展Map
                    extMap.put(header, cellValue);
                    break;
            }
        }
        dataList.add(dto);
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext context) {
        System.out.println("货物移动Excel解析完成，总行："+dataList.size());
    }

    public void addErrorMsg(String msg){
        errorMsgList.add(msg);
    }
}
