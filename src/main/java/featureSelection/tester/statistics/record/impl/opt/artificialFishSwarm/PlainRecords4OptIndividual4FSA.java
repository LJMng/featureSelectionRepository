package featureSelection.tester.statistics.record.impl.opt.artificialFishSwarm;

import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
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
 * Individual Plain Records for Artificial Fish Swarm.
 */
public class PlainRecords4OptIndividual4FSA
		extends PlainRecords4OptIndividual {

	public PlainRecords4OptIndividual4FSA(Collection<RecordFieldInfo> titles) {
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
		String id = String.format("%d of %d @%s", currentTime, times, summaryID);

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
			PlainRecord record, ProcedureParameters parameters, Statistics statistics,
			int randomSeed
	) {
		ReductionParameters reductParams =
				parameters.get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);

		// PARAM_GROUP_SIZE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_GROUP_SIZE,
				() -> reductParams.getGroupSize(),
				null
		);
		// PARAM_VISUAL
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_VISUAL,
				() -> reductParams.getVisual(),
				null
		);
		// PARAM_CROWD_FACTOR
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_CROWD_FACTOR,
				() -> reductParams.getCFactor(),
				null
		);
		// PARAM_SEARCH_TRY_NUMBER
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_SEARCH_TRY_NUMBER,
				() -> reductParams.getTryNumbers(),
				null
		);
		// PARAM_ITERATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_ITERATION,
				() -> reductParams.getIteration(),
				null
		);
		// PARAM_MAX_FISH_EXIT
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_MAX_FISH_EXIT,
				() -> reductParams.getMaxFishExit(),
				null
		);
		// PARAM_RANDOM_SEED
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_RANDOM_SEED,
				() -> randomSeed,
				null
		);
		// PARAM_4_ALG_DISTANCE_MEASURE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_DISTANCE_MEASURE,
				() -> reductParams.getDistanceCount().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FITNESS_CALCULATE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FITNESS_CALCULATE,
				() -> reductParams.getFitnessAlgorthm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FISH_GROUP_UPDATE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_UPDATE,
				() -> reductParams.getFishGroupUpdateAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FISH_GROUP_SWARM
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_SWARM,
				() -> reductParams.getFishSwarmAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FISH_GROUP_FOLLOW
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_FOLLOW,
				() -> reductParams.getFishFollowAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_FISH_GROUP_CENTER
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_ALG_FISH_GROUP_CENTER,
				() -> reductParams.getFishCenterCalculationAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_CLASS_POSITION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.PARAM_4_CLASS_POSITION,
				() -> reductParams.getPositionClass().getSimpleName(),
				null
		);

		// RECORD_EXIT_ITERATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.RECORD_EXIT_ITERATION,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_ITERATION),
				null
		);
		// RECORD_EXIT_CONVERGENCE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ArtificialFishSwarm.RECORD_EXIT_CONVERGENCE,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE),
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

}
