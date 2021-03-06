package org.utkuozdemir.watchdist.util;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.utkuozdemir.watchdist.app.Constants;
import org.utkuozdemir.watchdist.app.Settings;
import org.utkuozdemir.watchdist.domain.Watch;
import org.utkuozdemir.watchdist.domain.WatchPoint;
import org.utkuozdemir.watchdist.i18n.Messages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class ExcelExporter {
    private static final Logger logger = LoggerFactory.getLogger(ExcelExporter.class);

    public static void export(File saveFile, LocalDate date, Collection<Watch> watches) {
        if (saveFile == null) throw new NullPointerException();
        if (date == null) throw new NullPointerException();
        if (watches == null) throw new NullPointerException();

        try {
            Workbook workbook = getWorkbookTemplate();
            if (workbook == null) return;

            createExtraSheets(watches, workbook);
            List<Sheet> sheets = IntStream.range(0, workbook.getNumberOfSheets())
                    .mapToObj(workbook::getSheetAt).collect(Collectors.toList());

            fillTitleDateValue(date, sheets);
            fillNotesValues(date, sheets);
            fillPointNameValues(sheets, watches);
            fillHourValues(sheets);
            fillSoldierValues(sheets, watches);

            Files.createDirectories(saveFile.toPath().getParent());
            FileOutputStream outputStream = new FileOutputStream(saveFile);
            workbook.write(outputStream);
            outputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static void fillPointNameValues(Collection<Sheet> sheets, Collection<Watch> watches) {
        Stream<WatchPoint> sortedWatchPointsStream = watches.stream().map(Watch::getWatchPoint).distinct()
                .sorted((wp1, wp2) -> wp1.getOrder() - wp2.getOrder());
        Queue<String> watchPointNamesQueue = new LinkedList<>();
        sortedWatchPointsStream.forEach(wp -> IntStream.range(0,
                wp.getRequiredSoldierCount()).forEach(i -> watchPointNamesQueue.offer(wp.getName())));
        sheets.forEach(sheet -> sheet.forEach(row -> row.forEach(cell -> {
            if (Constants.TEMPLATE_POINT_NAME.equals(cell.getStringCellValue())) {
                cell.setCellValue(!watchPointNamesQueue.isEmpty() ? watchPointNamesQueue.poll() : "-");
            }
        })));
    }

    private static void fillSoldierValues(Collection<Sheet> sheets, Collection<Watch> watches) {
        List<WatchPoint> orderedWatchPoints = watches.stream().map(Watch::getWatchPoint)
                .sorted((o1, o2) -> o1.getId().compareTo(o2.getId())).distinct().collect(Collectors.toList());
        int soldierCount = orderedWatchPoints.stream().mapToInt(WatchPoint::getRequiredSoldierCount).sum();

        List<List<Cell>> soldierCells = new ArrayList<>();

        Watch[][] watchesMatrix = new Watch[Settings.getTotalWatchesInDay()][soldierCount];
        watches.forEach(w -> watchesMatrix[w.getHour()][
                IntStream.range(0,
                        orderedWatchPoints.indexOf(w.getWatchPoint()))
                        .map(i -> orderedWatchPoints.get(i).getRequiredSoldierCount()).sum() +
                        w.getWatchPointSlot()] = w
        );

        Sheet firstSheet = sheets.iterator().next();
        for (int i = 0; i < firstSheet.getPhysicalNumberOfRows(); i++) {
            List<Cell> soldierCellsRow = new ArrayList<>();
            final int finalI = i;
            sheets.forEach(sheet -> {
                Row row = sheet.getRow(finalI);
                for (int j = 0; j < row.getPhysicalNumberOfCells(); j++) {
                    Cell cell = row.getCell(j);
                    if (cell != null &&
                            Constants.TEMPLATE_SOLDIER_NAME.equals(cell.getStringCellValue())) {
                        soldierCellsRow.add(cell);
                    }
                }
            });
            if (!soldierCellsRow.isEmpty()) soldierCells.add(soldierCellsRow);
        }

        for (int i = 0; i < watchesMatrix.length; i++) {
            for (int j = 0; j < soldierCount; j++) {
                Watch watch = watchesMatrix[i][j];
                soldierCells.get(i).get(j).setCellValue(
                        watch != null ?
                                watch.getSoldier() != null ?
                                        watch.getSoldier().getFullName() :
                                        "" :
                                "");
            }
        }

        soldierCells.stream().forEach(cells -> cells.forEach(cell -> {
            if (Constants.TEMPLATE_SOLDIER_NAME.equals(cell.getStringCellValue()))
                cell.setCellValue("");
        }));
    }

    private static void createExtraSheets(Collection<Watch> watches, Workbook workbook) {
        int totalWatchPointSoldierCount = watches.stream()
                .map(Watch::getWatchPoint).distinct().mapToInt(WatchPoint::getRequiredSoldierCount).sum();
        int pageCount = ((totalWatchPointSoldierCount - 1) / 5) + 1;
        for (int i = 1; i < pageCount; i++) {
            workbook.cloneSheet(0);
        }
    }

    private static void fillTitleDateValue(LocalDate date, Collection<Sheet> sheets) {
        sheets.forEach(sheet -> {
            Cell titleCell = sheet.getRow(0).getCell(0);
            String title = titleCell.getStringCellValue();
            String dayName = DateTimeFormatter.ofPattern("EEEE", Messages.getLocale()).format(date);
            String dateString = date.toString() + " " + dayName.toUpperCase(Messages.getLocale());

            titleCell.setCellValue(title.replace(Constants.TEMPLATE_DAY_NAME, dateString));
        });
    }

    private static void fillNotesValues(LocalDate date, Collection<Sheet> sheets) {
        String note = DbManager.getNote(date);
        sheets.forEach(sheet -> sheet.forEach(row -> row.forEach(cell -> {
            if (cell.getCellType() == Cell.CELL_TYPE_STRING &&
                    Constants.TEMPLATE_NOTES_NAME.equals(cell.getStringCellValue())) {
                cell.setCellValue(note);
            }
        })));
    }

    private static void fillHourValues(Collection<Sheet> sheets) {
        sheets.forEach(sheet -> {
            final int[] i = {0};
            sheet.forEach(row -> row.forEach(cell -> {
                if (Constants.TEMPLATE_HOUR_NAME.equals(cell.getStringCellValue())) {
                    if (i[0] < Settings.getTotalWatchesInDay()) {
                        String startTime = String.format("%02d",
                                (((i[0]) * Settings.getOneWatchDurationInHours()) +
                                        Settings.getFirstWatchStartHour()) % 24);
                        String endTime = String.format("%02d",
                                (((i[0] + 1) * Settings.getOneWatchDurationInHours()) +
                                        Settings.getFirstWatchStartHour()) % 24);
                        i[0]++;
                        String minute = String.format("%02d", Settings.getFirstWatchStartMinute());
                        String hours = startTime + ":" + minute + " - " + endTime + ":" + minute;
                        cell.setCellValue(hours);
                    } else {
                        cell.setCellValue("-");
                    }
                }
            }));
        });
    }

    private static Workbook getWorkbookTemplate() {
        try {
            String templatePath = DbManager.getProperty(Constants.KEY_EXCEL_TEMPLATE_PATH_KEY);
            if (templatePath == null) {
                WindowManager.showSetExcelTemplatePathWindow(Messages.get("excel.template.path.not.set"));
            } else {
                try {
                    Path path = Paths.get(templatePath);
                    if (!Files.exists(path) || !Files.isWritable(path)) {
                        WindowManager.showSetExcelTemplatePathWindow(
                                Messages.get("excel.template.path.problem", templatePath)
                        );
                    } else {
                        return new HSSFWorkbook(Files.newInputStream(path));
                    }
                } catch (InvalidPathException | SecurityException e) {
                    logger.debug("Invalid path set for excel template: " + templatePath);
                    WindowManager.showSetExcelTemplatePathWindow(
                            Messages.get("excel.template.path.problem", templatePath)
                    );
                }
            }
            return null;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
