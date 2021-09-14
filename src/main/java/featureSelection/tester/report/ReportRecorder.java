package featureSelection.tester.report;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import common.utils.ExcelUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.tester.statistics.StatisticsIO;

import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import featureSelection.tester.utils.StatisticsUtils;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class ReportRecorder {
	
	/**
	 * Save a reports of {@link ReportMapGenerated} with <code>component.getDescription()
	 * </code> as key, report {@link Map} as value.
	 * 
	 * <pre>
	 * report: {
	 * 		component.getDescription(): {
	 * 			{xxx: yyy}, ...
	 * 		}
	 * }
	 * </pre>
	 * 
	 * @param destFile
	 * 		The report file to be written.
	 * @param reportItemOrder
	 * 		The order of report titles.
	 * @param report
	 * 		A report Map of a {@link ReportMapGenerated} by calling
	 * 		{@link ReportMapGenerated#getReport()}.
	 * @throws IOException if exceptions occur when writing report files.
	 */
	private static void saveAReport(
			File destFile, String[] reportItemOrder, Map<String, Map<String, Object>> report
	) throws IOException {
		Collection<String[]> lines = new LinkedList<>();
		String[] titles = reportTitles(report);
		lines.add(titles);
		lines.addAll(record(titles, reportItemOrder, report));
		
		log.info("	"+"Write report : {} at {}", destFile.getName(), destFile.getAbsolutePath());
		
		if (!destFile.exists())	destFile.createNewFile();
		ExcelUtils.saveFile(lines, destFile, true);
	}
	
	/* ------------------------------------- Individual Report ------------------------------------- */
	
	/**
	 * Save reports of {@link ProcedureContainer}.
	 * 
	 * @param path
	 * 		The base path for the report files.
	 * @param datasetName
	 * 		The name of the dataset.
	 * @param timesIndex
	 * 		No.<code>timesIndex</code> of executed times.
	 * @param attrOrderIndex
	 * 		No.<code>attrOrderIndex</code> of executed attribute orders.
	 * @param algorithmName
	 * 		Algorithm short name.
	 * @param container
	 * 		{@link ProcedureContainer}
	 * @throws IOException if exceptions occur when writing report files.
	 */
	public static void saveReports(
			File path, String datasetName, int timesIndex, int attrOrderIndex,
			String algorithmName, ProcedureContainer<?> container
	) throws IOException {
		LoggerUtil.printLine(log, "-", 70);
		log.info("Save reports of {} {} {} ...", datasetName, algorithmName, container.shortName());
		File reportBaseDir;
		reportBaseDir = new File(path, commonReportPath(datasetName, algorithmName)+File.separator + 
										"AttrNo_"+attrOrderIndex+"_TimeNo_"+timesIndex+"_"+container.id()+"_"+
											container.shortName()
													.replaceAll(Pattern.quote("("), "_")
													.replaceAll(Pattern.quote(")"), "")
						);
		if (!reportBaseDir.exists())	reportBaseDir.mkdirs();
		
		if (container instanceof ReportMapGenerated) {
			@SuppressWarnings("unchecked")
			ReportMapGenerated<String, Map<String, Object>> report = (ReportMapGenerated<String, Map<String, Object>>) container;
			File reportFile = new File(reportBaseDir, "main.csv");
			saveAReport(reportFile, report.getReportMapKeyOrder(), report.getReport());
		}
		for(ProcedureComponent<?> comp: container.getComponents()) {
			for (ProcedureContainer<?> subContainer: comp.getSubProcedureContainers().values()) {
				File subPath = new File(reportBaseDir, "sub reports");
				if (!subPath.exists())	subPath.mkdirs();
				saveSubReports(subPath, subContainer);
			}
		}
	}
	
	public static void saveSubReports(File reportBaseDir, ProcedureContainer<?> container)
			throws IOException
	{
		if (container instanceof ReportMapGenerated) {
			@SuppressWarnings("unchecked")
			ReportMapGenerated<String, Map<String, Object>> report = (ReportMapGenerated<String, Map<String, Object>>) container;
			String fileName = report.reportName();
			if (fileName.endsWith("."))	fileName += "csv";
			else						fileName += ".csv";
			File reportFile = new File(reportBaseDir, fileName);
			saveAReport(reportFile, report.getReportMapKeyOrder(), report.getReport());
		}
		for(ProcedureComponent<?> comp: container.getComponents()) {
			for (ProcedureContainer<?> subContainer: comp.getSubProcedureContainers().values()) {
				File subPath = new File(reportBaseDir, "sub reports");
				if (!subPath.exists())	subPath.mkdirs();
				saveSubReports(subPath, subContainer);
			}
		}
	}

	private static String commonReportPath(String datasetName, String algorithmName) {
		return ReportConstants.REPORT_FILE_BASE_FOLDER + File.separator+
				datasetName + File.separator +
				algorithmName;
	}
	
	/**
	 * Extract report titles from the given {@link ReportMapGenerated}.
	 * 
	 * @param report
	 * 		A {@link ReportMapGenerated}'s {@link Map}.
	 * @param priorityTitleOrder
	 * 		Titles in string to prioritise order.
	 * @return {@link String} Array as titles.
	 */
	private static String[] reportTitles(
			Map<String, Map<String, Object>> report, String...priorityTitleOrder
	) {
		String firstColumn = ReportConstants.REPORT_FIRST_COLUMN_DESCRIPTION;
		Collection<String> titles = new HashSet<>();
		for (Map<String, Object> items : report.values())	titles.addAll(items.keySet());
		if (priorityTitleOrder!=null && priorityTitleOrder.length>0) {
			List<String> titleList = new LinkedList<>();
			titleList.add(firstColumn);
			for (String p: priorityTitleOrder) {
				if (titles.contains(p)) {
					titleList.add(p);
					titles.remove(p);
				}
			}
			titleList.addAll(titles);
			return titleList.toArray(new String[titles.size()]);
		}else {
			String[] titleArray = new String[titles.size()+1];
			int i=0;	titleArray[i++] = firstColumn;
			for (String each : titles)	titleArray[i++] = each;
			return titleArray;
		}
	}

	private static Collection<String[]> record(
			String[] titles, String[] reportItemOrder, Map<String, Map<String, Object>> report
	) {
		Object value;
		String[] data;
		Map<String, Object> reportContent;
		Collection<String[]> datas = new ArrayList<>(report.size());
		for (String reportKey : reportItemOrder) {
			data = new String[titles.length];
			data[0] = reportKey;
			reportContent = report.get(reportKey);
			if (reportContent!=null) {
				for (int i=1; i<data.length; i++) {
					value = reportContent.get(titles[i]);
					if (value!=null) {
						data[i] = sumOfExecutionTime(titles[i], value)+
									StatisticsUtils.record(
										executionTimeFormatFilter(
											titles[i], value
										), 
										100, 
										4
									);
					}else {
						data[i] = "-";
					}
				}
				datas.add(data);
			}
		}
		return datas;
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static Object executionTimeFormatFilter(String title, Object value) {
		if (ReportConstants.Procedure.REPORT_EXECUTION_TIME.equals(title)) {
			if (value instanceof Collection) {
				if (((Collection) value).size()>1) {
					Collection<Double> list = new LinkedList<>();
					for (Long each : (Collection<Long>) value)
						list.add(TimerUtils.nanoTimeToMillis(each));
					return list;
				}else {
					Long ele = (Long) ((Collection) value).iterator().next();
					return TimerUtils.nanoTimeToMillis(ele);
				}
			}else if (value instanceof Long) {
				return TimerUtils.nanoTimeToMillis((long) value);
			}else {
				return value;
			}
		}else {
			return value;
		}
	}
	
	/**
	 * Calculate the sum of the execution time collection if the given <code>value</code> is 
	 * the correspondent time value collection.
	 * 
	 * @param title
	 * 		The title of the value.
	 * @param value
	 * 		The value to be executed.
	 * @return The sum of execution times if <code>value</code> is execution time 
	 * 		{@link Collection}. / <code>""</code> if it is not.
	 */
	private static Object sumOfExecutionTime(String title, Object value) {
		if (ReportConstants.Procedure.REPORT_EXECUTION_TIME.equals(title)) {
			if (value instanceof Collection) {
				@SuppressWarnings("unchecked")
				Collection<Number> collection = (Collection<Number>) value;
				if (collection.size()>1) {
					if (collection.iterator().next() instanceof Double) {
						double sum = 0;	for (Number v : collection)	sum+= v.doubleValue();
						return String.format("(Sum=%.4f)", TimerUtils.nanoTimeToMillis(sum));
					}else {
						long sum =0;	for (Number v: collection)	sum+= v.longValue();
						return String.format("(Sum=%.4f)", TimerUtils.nanoTimeToMillis(sum));
					}
				}else {
					return "";
				}
			}else {
				return "";
			}
		}else {
			return "";
		}
	}

	/* --------------------------------------- Average Report --------------------------------------- */
	
	public static void saveAverageExecTimeReports(
			File path, String datasetName, String algorithmName,
			ProcedureContainer<?>...containers
	) throws IOException {
		LoggerUtil.printLine(log, "-", 70);
		log.info("Save reports with average execute time ...");
		File reportBaseDir;
		reportBaseDir = new File(path, commonReportPath(datasetName, algorithmName) + File.separator + 
										containers[0].shortName()
													.replaceAll(Pattern.quote("("), "_")
													.replaceAll(Pattern.quote(")"), "")
						);
		if (!reportBaseDir.exists())	reportBaseDir.mkdirs();
		
		Map<String, String[]> orders;
		loadAverageItemOrder(orders=new HashMap<>(), containers);
		
		File file;
		Map<String, Map<String, Map<String, Object>>> averageMaps = averageExecuteTimeReport(containers);
		for (Map.Entry<String, Map<String, Map<String, Object>>> entry : averageMaps.entrySet()) {
			file = new File(reportBaseDir, containers[0].shortName().equals(entry.getKey())?
											"(average)main.csv": "(average)"+entry.getKey()+".csv"
							);
			saveAReport(file, orders.get(entry.getKey()), entry.getValue());
		}
	}
	
	private static void loadAverageItemOrder(
			Map<String, String[]> orders, ProcedureContainer<?>...containers
	){
		String[] existsTitle;
		for (ProcedureContainer<?> con: containers) {
			if (con instanceof ReportMapGenerated) {
				existsTitle = orders.get(((ReportMapGenerated<?, ?>) con).reportName());
				if (existsTitle==null) {
					orders.put(((ReportMapGenerated<?, ?>) con).reportName(), 
								((ReportMapGenerated<?, ?>) con).getReportMapKeyOrder()
					);
				}else if (((ReportMapGenerated<?, ?>) con).getReportMapKeyOrder().length>existsTitle.length) {
					orders.put(((ReportMapGenerated<?, ?>) con).reportName(), 
							((ReportMapGenerated<?, ?>) con).getReportMapKeyOrder()
					);
				}
			}
			for (ProcedureComponent<?> comp : con.getComponents()) {
				if (comp.getSubProcedureContainers()!=null) {
					for (ProcedureContainer<?> subCon : comp.getSubProcedureContainers().values()) {
						loadAverageItemOrder(orders, subCon);
					}
				}
			}
		}
	}
	
	private static Map<String, Map<String, Map<String, Object>>> averageExecuteTimeReport(
			ProcedureContainer<?>...containers
	){
		Map<String, Map<String, Map<String, Object>>> collect = new HashMap<>();
		reportWithExecutimeSum(collect, containers);
		
		long[] execTimeInfo;
		for (Map.Entry<String, Map<String, Map<String, Object>>> collectEntry: collect.entrySet()) {
			for (Map<String, Object> dataMap: collectEntry.getValue().values()) {
				execTimeInfo = (long[]) dataMap.get(
								ReportConstants.Procedure.REPORT_EXECUTION_TIME
							);
				if (execTimeInfo!=null) {
					dataMap.put(ReportConstants.Procedure.REPORT_EXECUTION_TIME,
									TimerUtils.nanoTimeToMillis(execTimeInfo[0] / (double) execTimeInfo[1])
								);
				}
			}
		}
		return collect;
	}
	
	@SuppressWarnings("unchecked")
	private static void reportWithExecutimeSum(
			Map<String, Map<String, Map<String, Object>>> collect,
			ProcedureContainer<?>...containers
	){
		Map<String, Map<String, Object>> reportMap;
		ReportMapGenerated<String, Map<String, Object>> containerReport;
		for (ProcedureContainer<?> container: containers) {
			if (container instanceof ReportMapGenerated) {
				containerReport = (ReportMapGenerated<String, Map<String, Object>>) container;
				reportMap = collect.get(containerReport.reportName());
				if (reportMap==null)	collect.put(containerReport.reportName(), reportMap=new HashMap<>());
				loadSumExecuteTimeInfo(reportMap, containerReport.getReport());
			}
			if (container.getComponents()!=null) {
				for(ProcedureComponent<?> comp: container.getComponents()) {
					for (ProcedureContainer<?> subContainer: comp.getSubProcedureContainers().values()) {
						reportWithExecutimeSum(collect, subContainer);
					}
				}
			}
		}
	}
	
	@SuppressWarnings("unchecked")
	private static void loadSumExecuteTimeInfo(
			Map<String, Map<String, Object>> collectMap,
			Map<String, Map<String, Object>> reportMap
	) {
		long[] timeInfo = null;
		Object execTimeInReport;
		Map<String, Object> collectData, reportData;
		for (String componentDesc : reportMap.keySet()) {
			collectData = collectMap.get(componentDesc);
			if (collectData==null)	collectMap.put(componentDesc, collectData=new HashMap<>());
			reportData = reportMap.get(componentDesc);
			
			for (String key: reportData.keySet()) {
				if (ReportConstants.Procedure.REPORT_EXECUTION_TIME.equals(key)){
					execTimeInReport = reportData.get(key);
					timeInfo = (long[]) collectData.get(key);
					if (timeInfo==null)	collectData.put(key, timeInfo = new long[2]);
					if (execTimeInReport instanceof Collection) {
						long sum = 0;
						for (Long t : (Collection<Long>) execTimeInReport)	sum += t;
						timeInfo[0] += sum;
						timeInfo[1]++;
					}else if (execTimeInReport instanceof Long){
						timeInfo[0] += (Long) execTimeInReport;
						timeInfo[1]++;
					}
				}else {
					if (!collectData.containsKey(key)) {
						collectData.put(key, reportData.get(key));
					}
				}
			}
		}
	}

	/* --------------------------------------- Reducts Summary --------------------------------------- */
	
	public static void saveReducts4SpecificAlgorithm(
			String algorithmName, DatasetInfo datasetInfo, Collection<IntArrayKey> reducts
	) throws IOException {
		LoggerUtil.printLine(log, "-", 70);
		log.info("Save reducts (algorithm based) ...");
		List<RecordFieldInfo> titles = StatisticsUtils.Title.Optimization.Common.initDistinctReductsTitles();
		PlainRecord[] records =
				PlainRecord.Common.getDistinctReducts(
						titles, datasetInfo, algorithmName, reducts
				);
		StatisticsIO.saveAFileRecords(
				new File(ReportConstants.REPORT_FILE_BASE_FOLDER),
				ReportConstants.REPORT_4_OPTIMIZATION_ALGORITHM_REDUCTS_LIST_FILE_NAME,
				titles, 
				records
		);
	}
	
	public static void saveReductsSummary4SpecificAlgorithm(
			String algorithmName, DatasetInfo datasetInfo, Collection<IntArrayKey> reducts
	) throws IOException {
		LoggerUtil.printLine(log, "-", 70);
		log.info("Save reducts summary reports (algorithm based) ...");
		List<RecordFieldInfo> titles = StatisticsUtils.Title.Optimization.Common.initReductsSummaryTitles(true);
		PlainRecord[] records =
				PlainRecord.Common.getDistinctReducts(
						titles, datasetInfo, algorithmName, reducts
				);
		StatisticsIO.saveAFileRecords(
				new File(ReportConstants.REPORT_FILE_BASE_FOLDER),
				ReportConstants.REPORT_4_OPTIMIZATION_ALGORITHM_REDUCTS_SUMMARY_FILE_NAME,
				titles, 
				records
		);
	}
	
	public static void saveReductsSummary4AllReducts(
			DatasetInfo datasetInfo, Collection<IntArrayKey> reducts
	) throws IOException {
		LoggerUtil.printLine(log, "-", 70);
		log.info("Save reducts summary reports (all) ...");
		List<RecordFieldInfo> titles = StatisticsUtils.Title.Optimization.Common.initReductsSummaryTitles(false);
		PlainRecord[] records =
				PlainRecord.Common.getReductsSummaryInfo(
						titles, datasetInfo, null, reducts
				);
		StatisticsIO.saveAFileRecords(
				new File(ReportConstants.REPORT_FILE_BASE_FOLDER),
				ReportConstants.REPORT_4_OPTIMIZATION_ALL_REDUCTS_SUMMARY_FILE_NAME,
				titles, 
				records
		);
	}
}