package org.example.excel.controller;

import com.alibaba.excel.EasyExcel;
import org.example.dto.ExcelResult;
import org.example.excel.dto.MaterialPickExcelDTO;
import org.example.excel.listener.CommonExcelListener;
import org.example.excel.listener.DynamicHeaderExcelListener;
import org.example.excel.listener.WorkOrderExcelListener;
import org.example.excel.util.ExcelCleanUtil;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/excel")
public class ExcelImportController {

    @PostMapping("/parse")
    public ExcelResult<?> parseExcel(@RequestParam("file") MultipartFile file,
                                     @RequestParam("templateCode") String templateCode) throws Exception {
        byte[] bytes;
        try {
            // 清洗：SAP 导出数值单元格可能带首尾空格（<v>0 </v>），先去掉再解析
            bytes = file.getBytes();
            String filename = file.getOriginalFilename();
            if (filename != null && filename.toLowerCase().endsWith(".xlsx")) {
                bytes = ExcelCleanUtil.clean(bytes);
            }
        } catch (Exception e) {
            return fail("文件读取/清洗失败：" + describeError(e));
        }

        if ("goods_move".equals(templateCode)) {
            DynamicHeaderExcelListener listener = new DynamicHeaderExcelListener();
            try {
                EasyExcel.read(new ByteArrayInputStream(bytes), listener)
                        .sheet()
                        .doRead();
            } catch (Exception e) {
                return fail("货物移动台账解析失败（已解析 " + listener.getDataList().size() + " 行）："
                        + describeError(e));
            }
            return success(listener.getDataList(), listener.getErrorMsgList(), "货物移动台账解析成功");
        } else if ("work_order".equals(templateCode)) {
            WorkOrderExcelListener listener = new WorkOrderExcelListener();
            try {
                EasyExcel.read(new ByteArrayInputStream(bytes), listener)
                        .sheet()
                        .doRead();
            } catch (Exception e) {
                return fail("生产工单解析失败（已解析 " + listener.getDataList().size() + " 行）："
                        + describeError(e));
            }
            return success(listener.getDataList(), listener.getErrorMsgList(), "生产工单解析成功");
        } else if ("material_pick".equals(templateCode)) {
            CommonExcelListener<MaterialPickExcelDTO> listener = new CommonExcelListener<>();
            try {
                EasyExcel.read(new ByteArrayInputStream(bytes))
                        .head(MaterialPickExcelDTO.class)
                        .sheet()
                        .registerReadListener(listener)
                        .doRead();
            } catch (Exception e) {
                return fail("物料拾取台账解析失败（已解析 " + listener.getDataList().size() + " 行）："
                        + describeError(e));
            }
            return success(listener.getDataList(), listener.getErrorMsgList(), "物料拾取台账解析成功");
        }
        return fail("未知模板类型：" + templateCode);
    }

    private <T> ExcelResult<T> success(List<T> dataList, List<String> errorMsgList, String msg) {
        ExcelResult<T> result = new ExcelResult<>();
        result.setSuccess(true);
        result.setDataList(dataList);
        result.setErrorMsgList(errorMsgList);
        result.setTotalRow(dataList.size());
        result.setSuccessRow(dataList.size());
        result.setMsg(msg);
        return result;
    }

    private ExcelResult<Void> fail(String msg) {
        ExcelResult<Void> result = new ExcelResult<>();
        result.setSuccess(false);
        result.setMsg(msg);
        return result;
    }

    // EasyExcel 底层异常会层层包装，取最里层的 cause 才是真正的报错原因
    private String describeError(Exception e) {
        Throwable root = e;
        while (root.getCause() != null && root.getCause() != root) {
            root = root.getCause();
        }
        if (root instanceof NumberFormatException) {
            return "文件中存在含非法字符的数字单元格（常见于数量/重量列里混入空格或千分位），"
                    + "请检查该列数据格式。原始错误：" + root.getMessage();
        }
        return root.getMessage() == null ? root.getClass().getSimpleName() : root.getMessage();
    }
}
