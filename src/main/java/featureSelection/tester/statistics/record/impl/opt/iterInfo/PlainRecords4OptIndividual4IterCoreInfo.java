package featureSelection.tester.statistics.record.impl.opt.iterInfo;

import common.utils.ArrayUtils;
import common.utils.DateTimeUtils;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.info.iteration.optimization.BasicIterationInfo4Optimization;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.OptEntityBasicInfo;
import featureSelection.tester.statistics.info.iteration.optimization.optimizationEntity.SupremeMarkType;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import featureSelection.tester.utils.DBUtils;
import featureSelection.tester.utils.StatisticsUtils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

public class PlainRecords4OptIndividual4IterCoreInfo
		extends PlainRecord {
	private PlainRecord[] records;

	public PlainRecords4OptIndividual4IterCoreInfo(Collection<RecordFieldInfo> titles) {
		super(titles);
	}

	/* ---------------------------------------------------------------------------------- */

	/**
	 * Load all individual record for iteration info.s.
	 * <p>
	 * This method only loads info.s of the last global best entities.
	 *
	 * @param titles          {@link RecordFieldInfo} {@link Collection}.
	 * @param datasetInfo     {@link DatasetInfo} instance.
	 * @param algName         The name of the Feature Selection algorithm.
	 * @param containerID     The ID of the container used to execute.
	 * @param procedureParams {@link ProcedureParameters} instance.
	 * @param iterationInfos  {@link BasicIterationInfo4Optimization} {@link Collection} that
	 *                        contains iteration info.s.
	 * @return loaded {@link PlainRecord[]}
	 * @see StatisticsUtils.Title.Optimization.IterationInfos#individualTitles()
	 */
	public void load(
			Collection<RecordFieldInfo> titles,
			DatasetInfo datasetInfo, String algName,
			Object containerID, ProcedureParameters procedureParams,
			Collection<BasicIterationInfo4Optimization<? extends Number>> iterationInfos
	) {
		if (iterationInfos == null) {
			records = new PlainRecord[0];
			return;
		}

		Map<String, RecordFieldInfo> titleMap = StatisticsUtils.Title.toMap(titles);
		String dateTimeStr = DateTimeUtils.currentDateTimeString("MM-dd HH:mm");

		boolean breakLoop = false;
		Collection<PlainRecord> recordCollection = new LinkedList<>();
		LinkedList<BasicIterationInfo4Optimization<? extends Number>> iterationInfosList =
				new LinkedList<>(iterationInfos);
		while (!iterationInfosList.isEmpty() && !breakLoop) {
			BasicIterationInfo4Optimization<? extends Number> iterInfo =
					iterationInfosList.pollLast();
			OptEntityBasicInfo<? extends Number>[] entityInfos =
					iterInfo.getOptimizationEntityBasicInfo();
			for (int i = 0; i < entityInfos.length; i++) {
				/* --------------------------------------------------------------------------------- */
				final int finalI = i;
				/* --------------------------------------------------------------------------------- */
				if (entityInfos[i].getSupremeMark() == null ||
						!SupremeMarkType.GLOBAL_BEST.equals(entityInfos[i].getSupremeMark().getSupremeMarkType())
				) {
					continue;
				}
				if (!breakLoop && entityInfos[i].getSupremeMark().getRank() == 1L) {
					breakLoop = true;
				}
				/* --------------------------------------------------------------------------------- */
				PlainRecord record = new PlainRecord(titles);
				recordCollection.add(record);
				/* --------------------------------------------------------------------------------- */
				// DATETIME
				record.set(
						titleMap, StatisticsConstants.PlainRecordInfo.DATETIME,
						() -> dateTimeStr,
						null
				);
				/* --------------------------------------------------------------------------------- */
				// CONTAINER_ID
				record.set(
						titleMap, StatisticsConstants.PlainRecordInfo.CONTAINER_ID,
						() -> containerID,
						null
				);
				/* --------------------------------------------------------------------------------- */
				// DATASET
				record.set(
						titleMap, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
						() -> datasetInfo.getDatasetName(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				// Algorithm
				record.set(
						titleMap, StatisticsConstants.PlainRecordInfo.ALG,
						() -> algName,
						null
				);
				// PARAMETER_ID
				record.set(
						titleMap, StatisticsConstants.PlainRecordInfo.PARAMETER_ID,
						() -> {
							int parameterID = DBUtils.ParameterID.get(procedureParams);
							return parameterID >= 0 ? parameterID + "" : "";
						},
						null
				);
				/* --------------------------------------------------------------------------------- */
				// GENERATION
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.GENERATION,
						() -> iterInfo.getIteration(),
						null
				);
				// CONVERGENCE
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.CONVERGENCE,
						() -> iterInfo.getConvergence(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				// RECORD_NUM
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_NUM,
						() -> iterInfo.getRecordNumber(),
						null
				);
				// RECORD_CLASS
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_CLASS,
						() -> iterInfo.getRecordClass() == null ?
								null : iterInfo.getRecordClass().getSimpleName(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				// ENTITY_NO
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_NO,
						() -> finalI + 1,
						null
				);
				// ENTITY_CODING_ATTR_LEN
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_CODING_ATTR_LEN,
						() -> entityInfos[finalI].getEntityAttributeLength(),
						null
				);
				// ENTITY_FINAL_ATTR_LEN
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR_LEN,
						() -> entityInfos[finalI].getFinalAttributes().length,
						null
				);
				// ENTITY_FINAL_ATTR
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR,
						() -> ArrayUtils.intArrayToString(
								entityInfos[finalI].getFinalAttributes(),
								100
						),
						null
				);
				// ENTITY_INSPECTED_ATTR_LEN
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR_LEN,
						() -> entityInfos[finalI].getInspectedReduct() == null ?
								"" : entityInfos[finalI].getInspectedReduct().size(),
						null
				);
				// ENTITY_INSPECTED_ATTR
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR,
						() -> entityInfos[finalI].getInspectedReduct() == null ?
								"" : entityInfos[finalI].getInspectedReduct(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				// ENTITY_FITNESS_VALUE
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FITNESS_VALUE,
						() -> entityInfos[finalI].getCurrentFitnessValue(),
						null
				);
				// ENTITY_IS_SOLUTION
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_IS_SOLUTION,
						() -> entityInfos[finalI].isSolution(),
						null
				);
				// ENTITY_SUPREME_MARK
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_SUPREME_MARK,
						() -> entityInfos[finalI].getSupremeMark() == null ?
								SupremeMarkType.NONE :
								entityInfos[finalI].getSupremeMark().getSupremeMarkType(),
						null
				);
				// ENTITY_SUPREME_MARK_NO
				record.set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_SUPREME_MARK_NO,
						() -> entityInfos[finalI].getSupremeMark() == null ?
								"" : entityInfos[finalI].getSupremeMark().getRank(),
						null
				);
				/* --------------------------------------------------------------------------------- */
			}
		}
		// add summary.
		recordCollection.add(
				summaryOf(recordCollection, titles, titleMap, dateTimeStr, datasetInfo, algName,
						containerID, procedureParams)
		);
		records = recordCollection.stream().toArray(PlainRecord[]::new);
	}

	/* ---------------------------------------------------------------------------------- */

	/**
	 * Summary of the given {@link PlainRecord}s. Summary by average for
	 * most of the values except
	 * {@link StatisticsConstants.OptimizationInfo.IterationInfos#GENERATION}
	 * and
	 * {@link StatisticsConstants.OptimizationInfo.IterationInfos#CONVERGENCE}
	 * by max value.
	 *
	 * @param records         {@link PlainRecord} {@link Collection}
	 * @param titles          {@link RecordFieldInfo} {@link Collection}.
	 * @param titleMap        {@link RecordFieldInfo} {@link Map}.
	 * @param dateTimeStr     Date and time in String.
	 * @param datasetInfo     {@link DatasetInfo} instance.
	 * @param algName         The name of the Feature Selection algorithm.
	 * @param containerID     The ID of the container used to execute.
	 * @param procedureParams {@link ProcedureParameters} instance.
	 * @return Loaded {@link PlainRecord} instance.
	 */
	private static PlainRecord summaryOf(
			Collection<PlainRecord> records,
			Collection<RecordFieldInfo> titles,
			Map<String, RecordFieldInfo> titleMap,
			String dateTimeStr, DatasetInfo datasetInfo, String algName,
			Object containerID, ProcedureParameters procedureParams
	) {
		/* --------------------------------------------------------------------------------- */
		final PlainRecord record = new PlainRecord(titles);
		/* --------------------------------------------------------------------------------- */
		// DATETIME
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.DATETIME,
				() -> dateTimeStr,
				null
		);
		/* --------------------------------------------------------------------------------- */
		// CONTAINER_ID
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.CONTAINER_ID,
				() -> containerID,
				null
		);
		/* --------------------------------------------------------------------------------- */
		// DATASET
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
				() -> datasetInfo.getDatasetName(),
				null
		);
		/* --------------------------------------------------------------------------------- */
		// Algorithm
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.ALG,
				() -> algName,
				null
		);
		// PARAMETER_ID
		record.set(
				titleMap, StatisticsConstants.PlainRecordInfo.PARAMETER_ID,
				() -> {
					int parameterID = DBUtils.ParameterID.get(procedureParams);
					return parameterID >= 0 ? parameterID + "" : "";
				},
				null
		);
		/* --------------------------------------------------------------------------------- */
		// GENERATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.GENERATION,
				() -> records.stream().mapToInt(
						r -> (Integer) (r.getRecordItems().get(
								titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.GENERATION)
										.getField()
						).getValue())
				).max().orElse(0),
				null
		);
		// CONVERGENCE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.CONVERGENCE,
				() -> records.stream().mapToInt(
						r -> (Integer) (r.getRecordItems().get(
								titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.CONVERGENCE)
										.getField()
						).getValue())
				).max().orElse(0),
				null
		);
		/* --------------------------------------------------------------------------------- */
		// RECORD_NUM
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_NUM,
				() -> records.iterator().next().getRecordItems().get(
						titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_NUM)
								.getField()
				).getValue(),
				null
		);
		// RECORD_CLASS
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_CLASS,
				() -> records.iterator().next().getRecordItems().get(
						titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_CLASS)
								.getField()
				).getValue(),
				null
		);
		/* --------------------------------------------------------------------------------- */
		// ENTITY_NO
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_NO,
				() -> "AVG",
				null
		);
		// ENTITY_CODING_ATTR_LEN
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_CODING_ATTR_LEN,
				() -> records.stream().mapToInt(
						r -> (Integer) (r.getRecordItems().get(
								titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_CODING_ATTR_LEN)
										.getField()
						).getValue())
				).average().orElse(0.0),
				null
		);
		// ENTITY_FINAL_ATTR_LEN
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR_LEN,
				() -> records.stream().mapToInt(
						r -> (Integer) (r.getRecordItems().get(
								titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR_LEN)
										.getField()
						).getValue())
				).average().orElse(0.0),
				null
		);
		// ENTITY_FINAL_ATTR
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR,
				() -> "-",
				null
		);
		// ENTITY_INSPECTED_ATTR_LEN
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR_LEN,
				() -> records.stream()
						.filter(
								r -> !(r.getRecordItems().get(
										titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR_LEN)
												.getField()
								).getValue() instanceof String)
						).mapToInt(
								r -> (Integer) (r.getRecordItems().get(
										titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR_LEN)
												.getField()
								).getValue()
								)
						).average().orElse(0.0),
				null
		);
		// ENTITY_INSPECTED_ATTR
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR,
				() -> "-",
				null
		);
		/* --------------------------------------------------------------------------------- */
		// ENTITY_FITNESS_VALUE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FITNESS_VALUE,
				() -> records.stream().mapToDouble(
						r -> ((Number) (r.getRecordItems().get(
								titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FITNESS_VALUE)
										.getField()
						).getValue())
						).doubleValue()
				).average().orElse(0.0),
				null
		);
		// ENTITY_IS_SOLUTION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_IS_SOLUTION,
				() -> records.stream().filter(
						r -> (Boolean) r.getRecordItems().get(
								titleMap.get(StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_IS_SOLUTION)
										.getField()
						).getValue()
				).count(),
				null
		);
		// ENTITY_SUPREME_MARK
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_SUPREME_MARK,
				() -> records.size(),
				null
		);
		// ENTITY_SUPREME_MARK_NO
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_SUPREME_MARK_NO,
				() -> "-",
				null
		);
		/* --------------------------------------------------------------------------------- */
		return record;
	}

}
