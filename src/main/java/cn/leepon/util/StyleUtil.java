package cn.leepon.util;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;

/**   
 * This class is used for ...   
 * @author leepon1990  
 * @version   
 *       1.0, 2016年8月25日 下午1:11:46   
 */
public class StyleUtil {
	

	public static CellStyle getStyle(SXSSFWorkbook workbook) {

		 CellStyle cellStyle = workbook.createCellStyle(); 
		// 设置背景色
		cellStyle.setFillForegroundColor(HSSFColor.GREY_40_PERCENT.index);// 设置灰色
		cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		// 设置边框
		cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);// 上边框
		cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN); // 下边框
		cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);// 左边框
		cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);// 右边框
		// 设置边框颜色
		cellStyle.setTopBorderColor(HSSFColor.BLACK.index); // 设置顶边框颜色;
		cellStyle.setBottomBorderColor(HSSFColor.BLACK.index); // 设置底边框颜色;
		cellStyle.setLeftBorderColor(HSSFColor.BLACK.index);// 设置左边框颜色;
		cellStyle.setRightBorderColor(HSSFColor.BLACK.index);// 设置右边框颜色;
		// 设置字体;
		Font font = workbook.createFont(); 
		// 设置字体大小;
		font.setFontHeightInPoints((short) 12);
		// 设置字体名字;
		font.setFontName("Courier New");
		//在样式用应用设置的字体;
		cellStyle.setFont(font);
		//设置自动换行;
		cellStyle.setWrapText(false);
		//设置水平对齐的样式为居中对齐;
		cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		//设置垂直对齐的样式为居中对齐;
		cellStyle.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
		return cellStyle; 

	}

}
