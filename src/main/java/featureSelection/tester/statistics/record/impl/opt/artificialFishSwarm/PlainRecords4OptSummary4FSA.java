package featureSelection.tester.statistics.record.impl.opt.artificialFishSwarm;

import common.utils.ArrayUtils;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
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
 * Summary Plain Records for Artificial Fish Swarm.
 */
public class PlainRecords4OptSummary4FSA
	extends PlainRecords4OptSummary
{
	public PlainRecords4OptSummary4FSA(Collection<RecordFieldInfo> titles) {
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
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_GROUP_SIZE,
				() -> reductParams.getGroupSize(),
				null
		);
		// PARAM_VISUAL
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_VISUAL,
				() -> reductParams.getVisual(),
				null
		);
		// PARAM_CROWD_FACTOR
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_CROWD_FACTOR,
				() -> reductParams.getCFactor(),
				null
		);
		// PARAM_SEARCH_TRY_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_SEARCH_TRY_NUMBER,
				() -> reductParams.getTryNumbers(),
				null
		);
		// PARAM_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_ITERATION,
				() -> reductParams.getIteration(),
				null
		);
		// PARAM_MAX_FISH_EXIT
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_MAX_FISH_EXIT,
				() -> reductParams.getMaxFishExit(),
				null
		);
		// PARAM_RANDOM_SEED
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_RANDOM_SEED,
				() -> ArrayUtils.intArrayToString(executionInfoStorage.getRandomSeeds(), 100),
				null
		);
		// PARAM_4_ALG_DISTANCE_MEASURE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_DISTANCE_MEASURE,
				() -> reductParams.getDistanceCount().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FITNESS_CALCULATE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FITNESS_CALCULATE,
				() -> reductParams.getFitnessAlgorthm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FISH_GROUP_UPDATE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_UPDATE,
				() -> reductParams.getFishGroupUpdateAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FISH_GROUP_SWARM
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_SWARM,
				() -> reductParams.getFishSwarmAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FISH_GROUP_FOLLOW
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_FOLLOW,
				() -> reductParams.getFishFollowAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FISH_GROUP_CENTER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_CENTER,
				() -> reductParams.getFishCenterCalculationAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_CLASS_POSITION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_CLASS_POSITION,
				() -> reductParams.getPositionClass().getSimpleName(),
				null
		);
		// RECORD_AVG_EXIT_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_AVG_EXIT_CONVERGENCE,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getConvergence)
						.reduce(Integer::sum)
						.orElse(0) / (double) executionInfoStorage.getTimes(),
				null
		);
		// RECORD_EXIT_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_CONVERGENCE,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getConvergence)
						.toArray(Integer[]::new),
				null
		);
		// RECORD_AVG_EXIT_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_AVG_EXIT_ITERATION,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getIteration)
						.reduce(Integer::sum)
						.orElse(0) / (double) executionInfoStorage.getTimes(),
				null
		);
		// RECORD_EXIT_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_ITERATION,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getIteration)
						.toArray(Integer[]::new),
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

}
