package featureSelection.tester.statistics.record.impl.opt.improvedHarmonySearch;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.info.ExecutionInfoStorage;
import featureSelection.tester.statistics.info.ExitInfo;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import featureSelection.tester.statistics.record.impl.opt.PlainRecords4OptSummary;
import featureSelection.tester.utils.DBUtils;

import java.util.Arrays;
import java.util.Collection;

/**
 * Individual Plain Records for Improved Harmony Search.
 */
public class PlainRecords4OptSummary4IHS
	extends PlainRecords4OptSummary
{
	public PlainRecords4OptSummary4IHS(Collection<RecordFieldInfo> titles) {
		super(titles);
	}

	/* ---------------------------------------------------------------------------------- */

	public void load(
			DatasetInfo datasetInfo, int[] attributes,
			ProcedureParameters parameters, String testName,
			Statistics statistics,
			ExecutionInfoStorage<OptimizationReduct> executionInfoStorage
	) {
		initReductInfo4Summary(executionInfoStorage.getTimes(), executionInfoStorage.getRedCollector());

		String databaseUniqueID = DBUtils.generateUniqueID(datasetInfo.getDatasetName(), testName, parameters);

		Common.loadRecordBasicInfo(this, titleMap, executionInfoStorage.containerIDs(), databaseUniqueID);
		Common.loadDatasetAlgorithmInfo(this, titleMap, datasetInfo, attributes, statistics);
		Common.loadCommonRuntimeInfo(this, titleMap, executionInfoStorage.getTimes(), testName, testName, parameters, executionInfoStorage.getComponentTagSumTimeMap());
		loadCalculationInfo4Summary(this, executionInfoStorage.getCalculations());
		loadTimeInfo4(this, datasetInfo, executionInfoStorage.getComponentTagSumTimeMap());
		loadParametersGeneralInfo4Summary(parameters, executionInfoStorage);
	}

	/* ---------------------------------------------------------------------------------- */

	private void loadParametersGeneralInfo4Summary(
			ProcedureParameters parameters,
			ExecutionInfoStorage<OptimizationReduct> executionInfoStorage
	) {
		ReductionParameters reductParams =
				parameters.get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);

		// PARAM_GROUP_SIZE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_GROUP_SIZE,
				() -> reductParams.getGroupSize(),
				null
		);
		// PARAM_HARMONY_MEMORY_SIZE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_HARMONY_MEMORY_SIZE,
				() -> reductParams.getHarmonyMemorySize(),
				null
		);
		// PARAM_HARMONY_MEMORY_CONSIDERATION_RATE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_HARMONY_MEMORY_CONSIDERATION_RATE,
				() -> reductParams.getHarmonyMemoryConsiderationRate(),
				null
		);
		// PARAM_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_ITERATION,
				() -> reductParams.getIteration(),
				null
		);
		// PARAM_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_CONVERGENCE,
				() -> reductParams.getConvergence(),
				null
		);
		// PARAM_RANDOM_SEED
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_RANDOM_SEED,
				() -> executionInfoStorage.getRandomSeeds(),
				null
		);
		// PARAM_4_ALG_PITCH_ADJUSTMENT_RATE
		this.set(
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
										reductParams.getParAlg()
												.getMinPitchAdjustmentRate()
												.doubleValue()
								)
						);
						builder.append(",");
						builder.append(
								String.format("%.4f",
										reductParams.getParAlg()
												.getMaxPitchAdjustmentRate()
												.doubleValue()
								)
						);
						builder.append("]");
					}
					builder.append(reductParams.getParAlg().getClass().getSimpleName());
					return builder.toString();
				},
				null
		);
		// PARAM_4_ALG_BAND_WIDTH
		this.set(
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
					return builder.toString();
				},
				null
		);
		// PARAM_4_ALG_HARMONY_INITIALIZATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.PARAM_4_ALG_HARMONY_INITIALIZTION,
				() -> reductParams.getHarmonyInitializationAlg().getClass().getSimpleName(),
				null
		);
		// RECORD_EXIT_REASON
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_REASON,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getExitMark)
						.toArray(String[]::new),
				null
		);
		// RECORD_AVG_EXIT_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_AVG_EXIT_CONVERGENCE,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getConvergence)
						.reduce(Integer::sum)
						.orElse(0) / (double) executionInfoStorage.getTimes(),
				null
		);
		// RECORD_EXIT_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_CONVERGENCE,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getConvergence)
						.toArray(Integer[]::new),
				null
		);
		// RECORD_AVG_EXIT_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_AVG_EXIT_ITERATION,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getIteration)
						.reduce(Integer::sum)
						.orElse(0) / (double) executionInfoStorage.getTimes(),
				null
		);
		// RECORD_EXIT_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_ITERATION,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getIteration)
						.toArray(Integer[]::new),
				null
		);
		// RECORD_EXIT_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_AVG_EXIT_FITNESS,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getFitness)
						.reduce(Double::sum)
						.orElse(0.0) / (double) executionInfoStorage.getTimes(),
				null
		);
		// RECORD_EXIT_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ImprovedHarmonySearch.RECORD_EXIT_FITNESS,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getFitness)
						.toArray(Double[]::new),
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

}
