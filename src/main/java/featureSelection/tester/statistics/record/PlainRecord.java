package featureSelection.tester.statistics.record;

import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

import common.utils.DateTimeUtils;
import common.utils.StringUtils;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.handler.DataValueHandling;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.DBUtils;
import featureSelection.tester.utils.ProcedureUtils;
import featureSelection.tester.utils.StatisticsUtils;
import lombok.Getter;
import lombok.ToString;

@ToString
public class PlainRecord {
	@Getter public Collection<RecordFieldInfo> titles;
	@Getter public Map<String, PlainRecordItem<?, ?>> recordItems;
	
	public PlainRecord(Collection<RecordFieldInfo> titles) {
		recordItems = new HashMap<>();
		this.titles = titles;
	}

	/**
	 * Set value of a title.
	 *
	 * @see #set(RecordFieldInfo, Object, Object)
	 * 
	 * @param <DataValue>
	 *     Type of data value.
	 * @param <DBValue>
	 *     Type of Database value.
	 * @param titleMap
	 * 		A title {@link Map} with title as keys, and correspondent {@link RecordFieldInfo}s
	 * 		as values.
	 * @param title
	 * 		The title of the value to be set.
	 * @param valueHandle
	 * 		Implemented {@link DataValueHandling} to handle value and return {@link DataValue}
	 * 		to set.
	 * @param dbValue
	 * 		The value to save into database.
	 * @return true if <code>title</code> exists in {@link #titles}.
	 */
	public <DataValue, DBValue> boolean set(
			Map<String, RecordFieldInfo> titleMap, String title,
			DataValueHandling<DataValue> valueHandle, DBValue dbValue
	){
		return set(titleMap.get(title), valueHandle.handle(), dbValue);
	}

	public <DataValue, DBValue> boolean set(
			RecordFieldInfo field, DataValue value, DBValue dbValue
	) {
		if (titles.contains(field)) {
			recordItems.put(field.getField(), new PlainRecordItem<>(field, value, dbValue));
			return true;
		}else {
			return false;
		}
	}


	/* ---------------------------------------------------------------------------------- */

	public static class Common {

		public static PlainRecord[] getReductsSummaryInfo(
				List<RecordFieldInfo> titles, DatasetInfo datasetInfo,
				String algName, Collection<IntArrayKey> reducts
		) {
			Map<String, RecordFieldInfo> titleMap = StatisticsUtils.Title.toMap(titles);

			final long totalAttrLength = reducts.stream().mapToInt(reduct -> reduct.key().length).sum();

			Date now = new Date();
			PlainRecord[] record = new PlainRecord[datasetInfo.getConditionAttributeSize()];
			for (int c = 0; c < record.length; c++) {
				final int attr = c + 1;

				record[c] = new PlainRecord(titles);

				// DATETIME
				record[c].set(
						titleMap, StatisticsConstants.PlainRecordInfo.DATETIME,
						() -> DateTimeUtils.toString(now, "MM-dd HH:mm"),
						null
				);
				// DATASET
				record[c].set(
						titleMap, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
						() -> datasetInfo.getDatasetName(),
						null
				);
				if (algName != null) {
					// Algorithm
					record[c].set(
							titleMap, StatisticsConstants.PlainRecordInfo.ALG,
							() -> algName,
							null
					);
				}
				// |Reduct|
				record[c].set(titleMap, "|Reduct|", () -> reducts.size(), null);
				// Attribute
				record[c].set(titleMap, "Attribute", () -> attr, null);
				// Contains Red. number
				record[c].set(
						titleMap, "Contains Red. number",
						() -> {
							long longV = reducts.isEmpty() ? 0 : reducts.stream().filter(
									red -> Arrays.stream(red.key()).anyMatch(a -> a == attr)
							).count();
							return longV;
						},
						null
				);
				// Contains Red. rate
				record[c].set(
						titleMap, "Contains Red. rate",
						() -> {
							double v = reducts.isEmpty() ? 0.0 :
									reducts.stream().filter(
											red -> Arrays.stream(red.key()).anyMatch(a -> a == attr)
									).count() / (double) reducts.size();
							return v;
						},
						null
				);
				// appearance(attr.) / sum(red.length)
				record[c].set(
						titleMap, "appearance(attr.) / sum(red.length)",
						() -> {
							long longV = reducts.isEmpty() ? 0 : reducts.stream().filter(
									red -> Arrays.stream(red.key()).anyMatch(a -> a == attr)
							).count();
							return reducts.isEmpty() ? 0 : longV / (double) totalAttrLength;
						},
						null
				);
			}
			return record;
		}

		public static PlainRecord[] getDistinctReducts(
				List<RecordFieldInfo> titles, DatasetInfo datasetInfo,
				String algName, Collection<IntArrayKey> reducts
		) {
			Map<String, RecordFieldInfo> titleMap = StatisticsUtils.Title.toMap(titles);

			Date now = new Date();
			PlainRecord[] record = new PlainRecord[reducts.size()];
			Iterator<IntArrayKey> reductsIterator = reducts.iterator();
			for (int c = 0; c < record.length; c++) {
				record[c] = new PlainRecord(titles);
				final int finalC = c;

				// DATETIME
				record[c].set(
						titleMap, StatisticsConstants.PlainRecordInfo.DATETIME,
						() -> DateTimeUtils.toString(now, "MM-dd HH:mm"),
						null
				);
				// DATASET
				record[c].set(
						titleMap, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
						() -> datasetInfo.getDatasetName(),
						null
				);
				// Algorithm
				record[c].set(
						titleMap, StatisticsConstants.PlainRecordInfo.ALG,
						() -> algName,
						null
				);
				// |Reduct|
				record[c].set(titleMap, "|Reduct|", () -> reducts.size(), null);
				// No.
				record[c].set(titleMap, "No.", () -> finalC + 1, null);
				// Reduct
				record[c].set(titleMap, "Reduct", () -> Arrays.toString(reductsIterator.next().key()), null);
			}
			return record;
		}


		public static void loadRecordBasicInfo(
				PlainRecord record, Map<String, RecordFieldInfo> titleMap,
				Object containerID, String databasedUniqueID
		) {
			// DATETIME
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.DATETIME,
					() -> DateTimeUtils.currentDateTimeString("MM-dd HH:mm:ss"),
					null
			);
			// CONTAINER_ID
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.CONTAINER_ID,
					() -> containerID,
					null
			);
			// DATABASE_UNIQUE_ID
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.DATABASE_UNIQUE_ID,
					() -> databasedUniqueID,
					null
			);
		}

		/**
		 * Load statistics of the fields below:
		 * <ul>
		 * 	<li><strong>{@link StatisticsConstants.PlainRecordInfo#DATASET_ID}</strong>
		 * 	 	<p>DBUtils.DatasetID.get({@link DatasetInfo#getDatasetName()})
		 * 	</li>
		 * 	<li><strong>{@link StatisticsConstants.PlainRecordInfo#DATASET_NAME}</strong>
		 * 	 	<p>{@link DatasetInfo#getDatasetName()}
		 * 	</li>
		 * 	<li><strong>{@link StatisticsConstants.PlainRecordInfo#ORIGINAL_UNIVERSE_SIZE}</strong>
		 * 	 	<p>{@link DatasetInfo#getUniverseSize()}
		 * 	</li>
		 * 	<li><strong>{@link StatisticsConstants.PlainRecordInfo#PURE_UNIVERSE_SIZE}</strong>
		 * 	 	<p>Distinct universe instance size
		 * 	</li>
		 * 	<li><strong>{@link StatisticsConstants.PlainRecordInfo#UNIVERSE_PURE_RATE}</strong>
		 * 	 	<p>Distinct universe instance rate
		 * 	</li>
		 * 	<li><strong>{@link StatisticsConstants.PlainRecordInfo#ATTRIBUTE_SIZE}</strong>
		 * 	 	<p>Attribute size of {@link Instance}: {@code attributes.size()}.
		 * 	</li>
		 * 	<li><strong>{@link StatisticsConstants.PlainRecordInfo#ATTRIBUTE_LIST}</strong>
		 * 	 	<p>Attributes of {@link Instance}: {@code attributes}.
		 * 	</li>
		 * 	<li><strong>{@link StatisticsConstants.PlainRecordInfo#DECISION_VALUE_NUMBER}</strong>
		 * 	 	<p>{@link DatasetInfo#getDecisionAttributeSize()}
		 * 	</li>
		 * </ul>
		 *
		 * @param record      {@link PlainRecord} instance.
		 * @param titleMap    A {@link Map} of titles with title names as keys and correspondent
		 *                    {@link RecordFieldInfo}s as values.
		 * @param datasetInfo {@link DatasetInfo} instance.
		 * @param attributes  Attributes of {@link Instance}.
		 * @param statistics  A {@link Map} that contains statistics of {@link ProcedureContainer}.
		 */
		public static void loadDatasetAlgorithmInfo(
				PlainRecord record, Map<String, RecordFieldInfo> titleMap,
				DatasetInfo datasetInfo, int[] attributes, Statistics statistics
		) {
			BasicExecutionInstanceInfo.Builder basicExecutionInstanceInfoBuilder =
					statistics.get(
							StatisticsConstants.Procedure
									.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER
					);
			BasicExecutionInstanceInfo basicExecutionInstanceInfo =
					basicExecutionInstanceInfoBuilder.build();
			/* ----------------------------------------------------------------------------------- */
			// DATASET_ID
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.DATASET_ID,
					() -> {
						int datasetID = DBUtils.DatasetID.get(datasetInfo.getDatasetName());
						return datasetID >= 0 ? datasetID + "" : "";
					},
					null
			);
			// DATASET
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
					() -> datasetInfo.getDatasetName(),
					null
			);
			/* ----------------------------------------------------------------------------------- */
			// ORIGINAL_UNIVERSE_SIZE
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.ORIGINAL_UNIVERSE_SIZE,
					() -> basicExecutionInstanceInfo.getCurrentInstanceNumber(),
					null
			);
			// PURE_UNIVERSE_SIZE
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.PURE_UNIVERSE_SIZE,
					() -> basicExecutionInstanceInfo.getCompressedInstanceNumber() == 0 ?
							basicExecutionInstanceInfo.getCurrentInstanceNumber() :
							basicExecutionInstanceInfo.getCompressedInstanceNumber(),
					null
			);
			// UNIVERSE_PURE_RATE
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.UNIVERSE_PURE_RATE,
					() -> {
						double rate;
						if (basicExecutionInstanceInfo.getCompressedInstanceNumber() == 0) {
							rate = 100;
						} else {
							rate = (((double) basicExecutionInstanceInfo.getCompressedInstanceNumber()) /
									basicExecutionInstanceInfo.getCurrentInstanceNumber() * 100.0);
						}
						return String.format("%.4f%%", rate);
					},
					null
			);
			// EXEC_INSTANCE_SIZE
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.EXEC_INSTANCE_SIZE,
					() -> basicExecutionInstanceInfo.getExecutedRecordNumber(),
					null
			);
			// EXEC_INSTANCE_SIZE
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.EXEC_INSTANCE_UNIT,
					() -> basicExecutionInstanceInfo.getExecutedRecordClass() == null ?
							null : basicExecutionInstanceInfo.getExecutedRecordClass().getSimpleName(),
					null
			);
			// ATTRIBUTE_SIZE
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.ATTRIBUTE_SIZE,
					() -> basicExecutionInstanceInfo.getCurrentConditionalAttributeNumber(),
					null
			);
			// ATTRIBUTE_LIST
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.ATTRIBUTE_LIST,
					() -> StringUtils.intToString(attributes, 100),
					null
			);
			// DECISION_VALUE_NUMBER
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.DECISION_VALUE_NUMBER,
					() -> datasetInfo.getDecisionAttributeSize(),
					null
			);
			/* ----------------------------------------------------------------------------------- */
		}

		public static void loadCommonRuntimeInfo(
				PlainRecord record, Map<String, RecordFieldInfo> titleMap,
				int times, String testName,
				String algorithmName, ProcedureParameters parameters,
				Map<String, Long> componentTagTimeMap
		) {
			// ALG_ID
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.ALG_ID,
					() -> {
						int algID = DBUtils.AlgorithmID.get(algorithmName);
						return algID >= 0 ? algID + "" : "";
					},
					null
			);
			// ALG
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.ALG,
					() -> testName,
					null
			);
			// PARAMETER_ID
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.PARAMETER_ID,
					() -> {
						int parameterID = DBUtils.ParameterID.get(parameters);
						return parameterID >= 0 ? parameterID + "" : "";
					},
					null
			);
			// TOTAL_TIME
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.TOTAL_TIME,
					() -> {
						Long total = componentTagTimeMap.get(ProcedureUtils.Time.SUM_TIME);
						return total == null ? 0 : TimerUtils.nanoTimeToMillis(total);
					},
					null
			);
			// PURE_TIME
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.PURE_TIME,
					() -> {
						Long total = componentTagTimeMap.get(ProcedureUtils.Time.SUM_TIME);
						Long inspectTime = componentTagTimeMap.get(ComponentTags.TAG_CHECK);
						if (inspectTime == null) inspectTime = (long) 0;
						return total == null ? 0 : TimerUtils.nanoTimeToMillis(total - inspectTime);
					},
					null
			);
			// RUN_TIMES
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.RUN_TIMES,
					() -> times,
					null
			);
		}

		public static void loadCalculationInfo(
				PlainRecord record, Map<String, RecordFieldInfo> titleMap,
				ProcedureParameters parameters
		) {
			Calculation<?> calculation = parameters.get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE);
			// CALCULATION_TIMES
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.CALCULATION_TIMES,
					() -> calculation == null ? 0 : calculation.getCalculationTimes(),
					null
			);
			// CALCULATION_ATTR_LEN
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.CALCULATION_ATTR_LEN,
					() -> {
						String method = "getCalculationAttributeLength";
						try {
							return calculation == null ?
									0 :
									calculation.getClass()
											.getMethod(method)
											.invoke(calculation);
						} catch (IllegalAccessException | IllegalArgumentException |
								InvocationTargetException | NoSuchMethodException |
								SecurityException e
						) {
							// No such method: getCalculationAttributeLength().
							return "?";
						}
					},
					null
			);
		}

		public static void loadProcedureInfo(
				PlainRecord record, Map<String, RecordFieldInfo> titleMap,
				ProcedureContainer<?> container
		) {
			// PROCEDURE_CLASSES_INFO
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.PROCEDURE_CLASSES_INFO,
					() -> {
						Collection<Class<?>> classes =
								ProcedureUtils
										.Statistics
										.classesOfProcedureContainer(container);
						Collection<String> classString =
								classes.stream()
										.map(Class::getSimpleName)
										.filter(name -> name.length() > 0)
										.collect(Collectors.toSet());
						return StringUtils.toString(classString, 50);
					},
					null
			);
			// REDUCT_SIZE
			record.set(
					titleMap, StatisticsConstants.PlainRecordInfo.PROCEDURE_INFO,
					() -> ProcedureUtils.Statistics.toJSONInfo(container),
					null
			);
		}
	}
}