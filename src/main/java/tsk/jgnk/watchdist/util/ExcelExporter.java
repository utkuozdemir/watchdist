package tsk.jgnk.watchdist.util;

import com.google.common.collect.Collections2;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.*;
import org.joda.time.LocalDate;
import tsk.jgnk.watchdist.domain.Watch;
import tsk.jgnk.watchdist.domain.WatchPoint;
import tsk.jgnk.watchdist.i18n.Messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

public class ExcelExporter {
    public static void export(File saveFile, LocalDate date, Collection<Watch> watches) {
        checkNotNull(saveFile);
        checkNotNull(date);
        checkNotNull(watches);

        Set<WatchPoint> watchPoints = extractWatchPoints(watches);
        try {
            Workbook workbook = getWorkbookTemplate();
            Sheet sheet = workbook.getSheetAt(0);

            Cell titleCell = sheet.getRow(0).getCell(0);
            String title = titleCell.getStringCellValue();

            String dayName = new SimpleDateFormat(
                    "EEEE",
                    Messages.getLocale()).format(date.toDate()).toUpperCase(Messages.getLocale()
            );
            String dateString = date.toString(Constants.DATE_FORMAT) + " " + dayName;

            titleCell.setCellValue(title.replace(Constants.TEMPLATE_DAY_NAME, dateString));

            CellStyle watchPointTitleStyle = sheet.getRow(5).getCell(1).getCellStyle();
            int watchPointTitleColumnWidth = sheet.getColumnWidth(1);

            CellStyle signatureTitleStyle = sheet.getRow(5).getCell(2).getCellStyle();
            int signatureTitleWidth = sheet.getColumnWidth(2);

            CellStyle tableCellStyle = sheet.getRow(6).getCell(1).getCellStyle();


            int totalWatchPointSoldierCount = WatchPointUtil.getTotalWatchPointSoldierCount(watchPoints);
            int extraPageCount = (totalWatchPointSoldierCount / 5);

            for (int i = 0; i < extraPageCount; i++) {
                workbook.cloneSheet(0);
            }

            int pointCount = 0;
            int column = 1;
            int currentSheetNum = -1;
            List<WatchPoint> watchPointList = new ArrayList<>();
            for (WatchPoint point : watchPoints) {
                for (int i = 0; i < point.getRequiredSoldierCount(); i++) {
                    pointCount++;

                    if ((pointCount - 1) % 5 == 0) {
                        currentSheetNum++;
                        column = 1;
                    }
                    sheet = workbook.getSheetAt(currentSheetNum);

                    sheet.setColumnWidth(column, watchPointTitleColumnWidth);
                    sheet.setColumnWidth(column + 1, signatureTitleWidth);

                    Row row = sheet.getRow(5);
                    Cell pointNameCell = row.getCell(column);
                    if (pointNameCell == null) pointNameCell = row.createCell(column);
                    pointNameCell.setCellValue(point.getName());
                    pointNameCell.setCellStyle(watchPointTitleStyle);
                    watchPointList.add(point);

                    Cell signatureCell = row.getCell(column + 1);
                    if (signatureCell == null) signatureCell = row.createCell(column + 1);

                    signatureCell.setCellStyle(signatureTitleStyle);

                    signatureCell.setCellValue(Messages.get("template.SIGNATURE"));
                    column += 2;
                }
            }

            for (Watch watch : watches) {
                WatchPoint watchPoint = watch.getWatchPoint();
                int index = nthIndexOf(watchPointList, watchPoint, watch.getWatchPointSlot() + 1);

                sheet = workbook.getSheetAt(index / 5);
                index = index % 5;

                int rowNum = 6 + watch.getHour();
                int colNum = 1 + (2 * index);

                Row row = sheet.getRow(rowNum);
                Cell cell = row.getCell(colNum);
                if (cell == null) cell = row.createCell(colNum);

                cell.setCellValue(watch.getSoldier().getFullName());
            }

            for (int i = 6; i < 18; i++) {
                for (int j = 1; j < column; j++) {
                    Row row = sheet.getRow(i);
                    Cell cell = row.getCell(j);
                    if (cell == null) cell = row.createCell(j);
                    cell.setCellStyle(tableCellStyle);
                }
            }

            if (pointCount % 5 < 5) {
                sheet = workbook.getSheetAt(pointCount / 5);
                for (int i = pointCount % 5; i < 5; i++) {
                    int emptyColNum = (i * 2) + 1;

                    Row titleRow = sheet.getRow(5);
                    titleRow.getCell(emptyColNum).setCellValue("-");
                    titleRow.getCell(emptyColNum + 1).setCellValue(Messages.get("template.SIGNATURE"));

                    for (int j = 6; j < 18; j++) {
                        Row row = sheet.getRow(j);
                        Cell emptyCell = row.getCell(emptyColNum);
                        emptyCell.setCellValue("x");
                    }
                }
            }

            FileOutputStream outputStream = new FileOutputStream(saveFile);
            workbook.write(outputStream);
            outputStream.close();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static Set<WatchPoint> extractWatchPoints(Collection<Watch> watches) {
        Collection<WatchPoint> points
                = Collections2.transform(watches, input -> input != null ? input.getWatchPoint() : null);

        Set<WatchPoint> watchPoints = new TreeSet<>(Comparators.WATCH_POINT_ID_ASC_COMPARATOR);
        watchPoints.addAll(points);
        watchPoints.remove(null);
        return watchPoints;
    }

    private static Workbook getWorkbookTemplate() throws IOException {
        try {
            Path templateFilePath = Paths.get(
                    ExcelExporter.class.getProtectionDomain().getCodeSource().getLocation().toURI()
            );
            Path templateDirectory = templateFilePath.getParent();
            String templateUrl = templateDirectory.toString() + File.separator + Constants.TEMPLATE_NAME;

            Path templatePath = Paths.get(templateUrl);
            InputStream inputStream = Files.newInputStream(templatePath);
            return new HSSFWorkbook(inputStream);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static <T> int nthIndexOf(List<T> list, T object, int n) {
        checkArgument(n > 0, "n must be at least 1!");
        int i = 0;
        int index = 0;
        for (T t : list) {
            if (Objects.equals(object, t)) i++;
            if (i == n) return index;
            index++;
        }
        return -1;
    }
}
