package featureSelection.tester.statistics;

import common.utils.ExcelUtils;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.PlainRecordItem;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import featureSelection.tester.utils.StatisticsUtils;
import lombok.experimental.UtilityClass;

import java.io.File;
import java.io.IOException;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

@UtilityClass
public class StatisticsIO {
	public final static String FILE_NAME_ALL_RECORDS = "All Records.csv";
	public final static String FILE_NAME_AVERAGE_RECORDS = "Average Records(Comprehensive).csv";
	public final static String FILE_NAME_SUMMARY = "Summary.csv";
	public final static String FILE_NAME_ITERATION_INFOS_ALL = "All Iteration Infos.csv";
	public final static String FILE_NAME_ITERATION_INFOS_CORE = "Core Iteration Infos.csv";
	
	/**
	 * Save {@link PlainRecord}s into the given file.
	 * 
	 * @see #saveAFileRecords(File, String, Collection, PlainRecord...)
	 * 
	 * @param path
	 * 		The folder to save into in {@link File}.
	 * @param titles
	 * 		Titles of the file in {@link RecordFieldInfo} {@link List}.
	 * @param record
	 * 		{@link PlainRecord}s to be saved.
	 * @throws IOException if exceptions occur when calling {@link ExcelUtils#saveFile(
	 * 		Collection, File, boolean)}.
	 */
	public static void saveAFileRecords(
			File path, List<RecordFieldInfo> titles, PlainRecord...record
	) throws IOException {
		saveAFileRecords(path, FILE_NAME_ALL_RECORDS, titles, record);
	}
	
	/**
	 * Save {@link PlainRecord}s into the given file.
	 * 
	 * @param path
	 * 		The folder to save into in {@link File}.
	 * @param fileName
	 * 		The name of the file to save into.
	 * @param titles
	 * 		Titles of the file in {@link RecordFieldInfo} {@link List}.
	 * @param record
	 * 		{@link PlainRecord}s to be saved.
	 * @throws IOException if exceptions occur when calling {@link ExcelUtils#saveFile(
	 * 		Collection, File, boolean)}.
	 */
	public static void saveAFileRecords(
			File path, String fileName, Collection<RecordFieldInfo> titles,
			PlainRecord...record
	) throws IOException {
		if (path!=null && !path.exists())	path.mkdirs();
		File normFile = new File(path, fileName);
	
		List<String[]> normText = new ArrayList<>(record.length+(normFile.exists()?0: 4));
		if (!normFile.exists())	normText.addAll(recordTitle(titles));
		for (PlainRecord tr : record) normText.add(record(titles, tr));
		ExcelUtils.saveFile(normText, normFile, !normFile.exists());
	}
	
	
	public static void saveOrderDetailAverageFileRecords(
			File path, List<RecordFieldInfo> titles, PlainRecord...record
	) throws IOException {
		if (!path.exists())	path.mkdirs();
		File aveFile = new File(path, "Average Records(Order Details).csv");
	
		List<String[]> aveText = new ArrayList<>(record.length+(aveFile.exists()? 0: 4));
		if (!aveFile.exists())	aveText.addAll(recordTitle(titles));
		aveText.add(averageRecord(titles, record));
		ExcelUtils.saveFile(aveText, aveFile, !aveFile.exists());
	}
	
	public static void saveAlgorithmFileAverage(
			File path, List<RecordFieldInfo> titles, PlainRecord...record
	) throws IOException {
		saveAlgorithmFileAverage(path, FILE_NAME_AVERAGE_RECORDS, titles, record);
	}
	
	public static void saveAlgorithmFileAverage(
			File path, String fileName, List<RecordFieldInfo> titles, PlainRecord...record
	) throws IOException {
		if (!path.exists())	path.mkdirs();
		File aveFile = new File(path, fileName);
	
		List<String[]> aveText = new ArrayList<>(record.length+(aveFile.exists()?0: 4));
		if (!aveFile.exists())	aveText.addAll(recordTitle(titles));
		aveText.add(averageRecord(titles, record));
		ExcelUtils.saveFile(aveText, aveFile, !aveFile.exists());
	}
	

	private static Collection<String[]> recordTitle(Collection<RecordFieldInfo> titles) {
		Collection<String[]> titleCollection = new ArrayList<>(4);		
		int i;
		String[] titleData;
		// title database field
		i=0;	titleCollection.add(titleData = new String[titles.size()]);
		for (RecordFieldInfo title : titles)	titleData[i++] = title.getDbField();
		// title database field
		i=0;	titleCollection.add(titleData = new String[titles.size()]);
		for (RecordFieldInfo title : titles)	titleData[i++] = title.getDbTable();
		// title desc
		i=0;	titleCollection.add(titleData = new String[titles.size()]);
		for (RecordFieldInfo title : titles)	titleData[i++] = title.getDesc();
		// normal title
		i=0;	titleCollection.add(titleData = new String[titles.size()]);
		for (RecordFieldInfo title : titles)	titleData[i++] = title.getField();
		return titleCollection;
	}
		
	private static String[] averageRecord(
			List<RecordFieldInfo> titles, PlainRecord[] records
	) {
		// Initiate average record.
		Map<String, Double> sumMap = new HashMap<>();
		Double sumValue;
		Object recordValue;
		PlainRecordItem<?, ?> recordItem;
		for (PlainRecord record : records) {
			for (RecordFieldInfo title : titles) {
				recordItem = record.getRecordItems().get(title.getField());
				if (recordItem!=null) {
					recordValue = recordItem.getValue();
					if (recordValue!=null && recordValue instanceof Number) {
						sumValue = sumMap.get(title.getField());
						if (sumValue==null)	sumValue = 0.0;
						sumValue += ((Number) recordValue).doubleValue();
						sumMap.put(title.getField(), sumValue);
					}
				}
			}
		}
		PlainRecord aveRecord = new PlainRecord(titles);
		for (RecordFieldInfo title: titles) {
			sumValue = sumMap.get(title.getField());
			if (sumValue!=null) {
				aveRecord.set(title, sumValue / records.length, null);
			}else {
				if (records[0].getRecordItems().get(title.getField())!=null &&
					records[0].getRecordItems().get(title.getField()).getValue()!=null
				) {
					aveRecord.set(title, records[0].getRecordItems().get(title.getField()).getValue(), null);
				}
			}
		}
		// record text
		return record(titles, aveRecord);
	}
	
	private static String[] record(Collection<RecordFieldInfo> titles, PlainRecord record) {
		int i=0; 
		String[] lineData = new String[titles.size()];
		for (RecordFieldInfo title: titles)	{
			lineData[i++] = StatisticsUtils.record(title, record, 500, 2);
		}
		return lineData;
	}
}