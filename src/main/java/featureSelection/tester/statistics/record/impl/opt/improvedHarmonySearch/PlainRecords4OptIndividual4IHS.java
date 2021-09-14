package featureSelection.tester.statistics.record.impl.opt.improvedHarmonySearch;

import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.record.PlainRecord;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import featureSelection.tester.statistics.record.impl.opt.PlainRecords4OptIndividual;
import featureSelection.tester.utils.DBUtils;

import java.util.Collection;
import java.util.Map;

/**
 * Individual Plain Records for Improved Harmony Search.
 */
public class PlainRecords4OptIndividual4IHS
		extends PlainRecords4OptIndividual
{

	public PlainRecords4OptIndividual4IHS(Collection<RecordFieldInfo> titles) {
		super(titles);
	}

	/* ---------------------------------------------------------------------------------- */

	@Override
	protected void load(
			String summaryID, DatasetInfo datasetInfo, int[] attributes, int times,
			int currentTime, int randomSeed, ProcedureParameters parameters,
			String testName, Statistics statistics, Map<String, Long> componentTagTimeMap,
			ProcedureContainer<?> container
	) {
		String id = currentTime + " of " + times + " @" + summaryID;

		initReductInfo4Individual(statistics, id);

		String databaseUniqueID;
		for (PlainRecord record : records) {
			databaseUniqueID = DBUtils.generateUniqueID(datasetInfo.getDatasetName(), testName, parameters);

			Common.loadRecordBasicInfo(record, titleMap, container.id(), databaseUniqueID);
			Common.loadDatasetAlgorithmInfo(record, titleMap, datasetInfo, attributes, statistics);
			Common.loadCommonRuntimeInfo(record, titleMap, times, testName, container.shortName(), parameters, componentTagTimeMap);
			Common.loadCalculationInfo(record, titleMap, parameters);
			Common.loadProcedureInfo(record, titleMap, container);
			loadTimeInfo4(record, datasetInfo, componentTagTimeMap);
			loadParametersInfo4Individual(record, parameters, statistics, randomSeed);
		}
	}

	/* ---------------------------------------------------------------------------------- */

	private void loadParametersInfo4Individual(
			PlainRecord record,
			ProcedureParameters parameters, Statistics statistics,
			int randomSeed
	) {
		@SuppressWarnings("rawtypes")
		featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters reductParams =
				parameters.get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);

		// PARAM_GROUP_SIZE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_GROUP_SIZE,
				() -> reductParams.getGroupSize(),
				null
		);
		// PARAM_HARMONY_MEMORY_SIZE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_HARMONY_MEMORY_SIZE,
				() -> reductParams.getHarmonyMemorySize(),
				null
		);
		// PARAM_HARMONY_MEMORY_CONSIDERATION_RATE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_HARMONY_MEMORY_CONSIDERATION_RATE,
				() -> reductParams.getHarmonyMemoryConsiderationRate(),
				null
		);
		// PARAM_ITERATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_ITERATION,
				() -> reductParams.getIteration(),
				null
		);
		// PARAM_CONVERGENCE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_CONVERGENCE,
				() -> reductParams.getConvergence(),
				null
		);
		// PARAM_RANDOM_SEED
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_RANDOM_SEED,
				() -> randomSeed,
				null
		);
		// PARAM_4_ALG_PITCH_ADJUSTMENT_RATE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_4_ALG_PITCH_ADJUSTMENT_RATE,
				() -> {
					StringBuilder builder = new StringBuilder();
					if (featureSelection.repository.entity.opt.improvedHarmonySearch
							.impl.calculation.DynamicPitchAdjustmentRateAlgorithm
							.class
							.equals(reductParams.getParAlg().getClass())
					) {
						builder.append("[");
						builder.append(
								String.format("%.4f",
										(reductParams.getParAlg())
												.getMinPitchAdjustmentRate()
												.doubleValue()
								)
						);
						builder.append(",");
						builder.append(
								String.format("%.4f",
										(reductParams.getParAlg())
												.getMaxPitchAdjustmentRate()
												.doubleValue()
								)
						);
						builder.append("]");
					}
					builder.append(reductParams.getParAlg().getClass().getSimpleName());
					return builder;
				},
				null
		);
		// PARAM_4_ALG_BAND_WIDTH
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_4_ALG_BAND_WIDTH,
				() -> {
					StringBuilder builder = new StringBuilder();
					if (featureSelection.repository.entity.opt.improvedHarmonySearch
							.impl.calculation.DynamicBandWidthAlgorithm
							.class
							.equals(reductParams.getParAlg().getClass())
					) {
						builder.append("[");
						builder.append(
								String.format("%.4f",
										((featureSelection.repository.entity.opt
												.improvedHarmonySearch.impl.calculation
												.DynamicBandWidthAlgorithm)
												reductParams.getParAlg())
												.getMinBW()
								)
						);
						builder.append(",");
						builder.append(
								String.format("%.4f",
										((featureSelection.repository.entity.opt
												.improvedHarmonySearch.impl.calculation
												.DynamicBandWidthAlgorithm)
												reductParams.getParAlg())
												.getMaxBW()
								)
						);
						builder.append("]");
					}
					builder.append(reductParams.getParAlg().getClass().getSimpleName());
					return builder;
				},
				null
		);
		// PARAM_4_ALG_HARMONY_INITIALIZTION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_4_ALG_HARMONY_INITIALIZTION,
				() -> reductParams.getHarmonyInitializationAlg().getClass().getSimpleName(),
				null
		);

		// RECORD_EXIT_REASON
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_REASON,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON),
				null
		);
		// RECORD_EXIT_ITERATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_ITERATION,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_ITERATION),
				null
		);
		// RECORD_EXIT_CONVERGENCE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_CONVERGENCE,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE),
				null
		);
		// RECORD_EXIT_FITNESS
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_FITNESS,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_FITNESS),
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

}
