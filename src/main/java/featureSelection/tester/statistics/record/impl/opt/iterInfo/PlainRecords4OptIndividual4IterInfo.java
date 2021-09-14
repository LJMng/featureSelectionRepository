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
import java.util.Comparator;
import java.util.Map;

public class PlainRecords4OptIndividual4IterInfo
		extends PlainRecord
{
	private PlainRecord[] records;

	public PlainRecords4OptIndividual4IterInfo(Collection<RecordFieldInfo> titles) {
		super(titles);
	}

	/* ---------------------------------------------------------------------------------- */

	/**
	 * Load all individual record for iteration info.s.
	 * <p>
	 * For iteration info.s that stay unchanged comparing to the last one
	 * at the last iteration, "-" is used to marked instead of actual values.
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
		int totalInfoNums = iterationInfos.stream().mapToInt(
								iterInfo -> iterInfo.getOptimizationEntityBasicInfo().length
							).sum();

		int r = 0;
		records = new PlainRecord[totalInfoNums];
		BasicIterationInfo4Optimization<? extends Number> previousIterInfo = null;
		for (BasicIterationInfo4Optimization<? extends Number> iterInfo : iterationInfos) {
			int entityIndex = 1;
			OptEntityBasicInfo<? extends Number>[] entityInfos = iterInfo.getOptimizationEntityBasicInfo();
			for (int i = 0; i < entityInfos.length; i++) {
				/* --------------------------------------------------------------------------------- */
				final int finalEntityIndex = entityIndex++, finalI = i;
				@SuppressWarnings({"rawtypes", "unchecked"})
				boolean entityUnchanged =
						previousIterInfo != null &&
								!previousIterInfo.getOptimizationEntityBasicInfo()[i]
										.differ((OptEntityBasicInfo)
														iterInfo.getOptimizationEntityBasicInfo()[i],
												Comparator.comparingDouble(Number::doubleValue)
										);
				/* --------------------------------------------------------------------------------- */
				records[r] = new PlainRecord(titles);
				/* --------------------------------------------------------------------------------- */
				// DATETIME
				records[r].set(
						titleMap, StatisticsConstants.PlainRecordInfo.DATETIME,
						() -> dateTimeStr,
						null
				);
				/* --------------------------------------------------------------------------------- */
				// CONTAINER_ID
				records[r].set(
						titleMap, StatisticsConstants.PlainRecordInfo.CONTAINER_ID,
						() -> containerID,
						null
				);
				/* --------------------------------------------------------------------------------- */
				// DATASET
				records[r].set(
						titleMap, StatisticsConstants.PlainRecordInfo.DATASET_NAME,
						() -> datasetInfo.getDatasetName(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				// Algorithm
				records[r].set(
						titleMap, StatisticsConstants.PlainRecordInfo.ALG,
						() -> algName,
						null
				);
				// PARAMETER_ID
				records[r].set(
						titleMap, StatisticsConstants.PlainRecordInfo.PARAMETER_ID,
						() -> {
							int parameterID = DBUtils.ParameterID.get(procedureParams);
							return parameterID >= 0 ? parameterID + "" : "";
						},
						null
				);
				/* --------------------------------------------------------------------------------- */
				// GENERATION
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.GENERATION,
						() -> iterInfo.getIteration(),
						null
				);
				// CONVERGENCE
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.CONVERGENCE,
						() -> iterInfo.getConvergence(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				// RECORD_NUM
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_NUM,
						() -> iterInfo.getRecordNumber(),
						null
				);
				// RECORD_CLASS
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.RECORD_CLASS,
						() -> iterInfo.getRecordClass() == null ?
								null : iterInfo.getRecordClass().getSimpleName(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				// ENTITY_NO
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_NO,
						() -> finalEntityIndex,
						null
				);
				// ENTITY_CODING_ATTR_LEN
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_CODING_ATTR_LEN,
						() -> entityUnchanged ? "-" : entityInfos[finalI].getEntityAttributeLength(),
						null
				);
				// ENTITY_FINAL_ATTR_LEN
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR_LEN,
						() -> entityUnchanged ? "-" : entityInfos[finalI].getFinalAttributes().length,
						null
				);
				// ENTITY_FINAL_ATTR
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FINAL_ATTR,
						() -> entityUnchanged ? "-" :
								ArrayUtils.intArrayToString(
										entityInfos[finalI].getFinalAttributes(),
										100
								),
						null
				);
				// ENTITY_INSPECTED_ATTR_LEN
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR_LEN,
						() -> entityUnchanged ? "-" :
								entityInfos[finalI].getInspectedReduct() == null ?
										"" : entityInfos[finalI].getInspectedReduct().size(),
						null
				);
				// ENTITY_INSPECTED_ATTR
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_INSPECTED_ATTR,
						() -> entityUnchanged ? "-" :
								entityInfos[finalI].getInspectedReduct() == null ?
										"" : entityInfos[finalI].getInspectedReduct(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				// ENTITY_FITNESS_VALUE
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_FITNESS_VALUE,
						() -> entityUnchanged ? "-" : entityInfos[finalI].getCurrentFitnessValue(),
						null
				);
				// ENTITY_IS_SOLUTION
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_IS_SOLUTION,
						() -> entityUnchanged ? "-" : entityInfos[finalI].isSolution(),
						null
				);
				// ENTITY_SUPREME_MARK
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_SUPREME_MARK,
						() -> entityUnchanged ? "-" :
								entityInfos[finalI].getSupremeMark() == null ?
										SupremeMarkType.NONE :
										entityInfos[finalI].getSupremeMark().getSupremeMarkType(),
						null
				);
				// ENTITY_SUPREME_MARK_NO
				records[r].set(
						titleMap, StatisticsConstants.OptimizationInfo.IterationInfos.ENTITY_SUPREME_MARK_NO,
						() -> entityUnchanged ? "-" :
								entityInfos[finalI].getSupremeMark() == null ?
										"" : entityInfos[finalI].getSupremeMark().getRank(),
						null
				);
				/* --------------------------------------------------------------------------------- */
				r++;
				/* --------------------------------------------------------------------------------- */
			}
			previousIterInfo = iterInfo;
		}
	}

	/* ---------------------------------------------------------------------------------- */


}
