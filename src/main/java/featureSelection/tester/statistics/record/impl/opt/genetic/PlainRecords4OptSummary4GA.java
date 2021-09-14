package featureSelection.tester.statistics.record.impl.opt.genetic;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.code.mutation.razaGene.NumericChromosomeRandomMutation;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.DatasetInfo;
import featureSelection.tester.statistics.info.ExecutionInfoStorage;
import featureSelection.tester.statistics.info.ExitInfo;
import featureSelection.tester.statistics.record.RecordFieldInfo;
import featureSelection.tester.statistics.record.impl.opt.PlainRecords4OptSummary;
import featureSelection.tester.utils.DBUtils;

import java.util.*;

/**
 * Summary Plain Records for Genetic Algorithm.
 */
public class PlainRecords4OptSummary4GA
	extends PlainRecords4OptSummary
{
	public PlainRecords4OptSummary4GA(Collection<RecordFieldInfo> titles) {
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
		loadParametersGeneralInfo4Summary(parameters, executionInfoStorage.getRandomSeeds(), executionInfoStorage.getExitInfos());
	}

	/* ---------------------------------------------------------------------------------- */

	@SuppressWarnings("rawtypes")
	private void loadParametersGeneralInfo4Summary(
			ProcedureParameters parameters, int[] randomSeeds,
			ExitInfo[] exitInfos
	) {
		int times = randomSeeds.length;

		ReductionParameters reductParams =
				parameters.get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);
		Integer mutateNum = null;
		Double mutateRate = null;

		if (reductParams.getMutationAlgorithm() instanceof NumericChromosomeRandomMutation) {
			mutateNum =
					((NumericChromosomeRandomMutation) reductParams.getMutationAlgorithm())
							.getParameters()
							.getMutateSize();
			mutateRate =
					((NumericChromosomeRandomMutation) reductParams.getMutationAlgorithm())
							.getParameters()
							.getMutateRate();
		}

		final Integer finalMutateNum = mutateNum;
		final Double finalMutateRate = mutateRate;

		// PARAM_POPULATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_POPULATION,
				() -> reductParams.getPopulation(),
				null
		);
		// PARAM_GENE_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_GENE_LENGTH,
				() -> reductParams.getChromosomeLength(),
				null
		);
		// PARAM_CHROMOSOME_GENE_SWITCH_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_CHROMOSOME_GENE_SWITCH_NUMBER,
				() -> reductParams.getChromosomeSwitchNum(),
				null
		);
		// PARAM_CHROMOSOME_RESERVE_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_CHROMOSOME_RESERVE_NUMBER,
				() -> reductParams.getReserveNum(),
				null
		);
		// PARAM_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_ITERATION,
				() -> reductParams.getIterateNum(),
				null
		);
		// PARAM_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_CONVERGENCE,
				() -> reductParams.getConvergenceLimit(),
				null
		);
		// PARAM_MAX_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_MAX_FITNESS,
				() -> reductParams.getMaxFitness().getValue(),
				null
		);
		// PARAM_GENE_MUTATE_NUMBER
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_GENE_MUTATE_NUMBER,
				() -> finalMutateNum,
				null
		);
		// PARAM_GENE_MUTATE_PROBABILITY
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_GENE_MUTATE_PROBABILITY,
				() -> finalMutateRate,
				null
		);
		// PARAM_RANDOM_SEED
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_RANDOM_SEED,
				() -> randomSeeds,
				null
		);
		// PARAM_4_ALG_CHROMOSOME_INITIATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_4_ALG_CHROMOSOME_INITIATION,
				() -> reductParams.getChromosomeInitAlgorithm()
						.getClass()
						.getSimpleName(),
				null
		);
		// PARAM_4_ALG_CHROMOSOME_CROSS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_4_ALG_CHROMOSOME_CROSS,
				() -> reductParams.getCrossAlgorithm()
						.getClass()
						.getSimpleName(),
				null
		);
		// PARAM_4_ALG_CHROMOSOME_CLASS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.PARAM_4_ALG_CHROMOSOME_CLASS,
				() -> reductParams.getChromosomeInitAlgorithm()
						.getParameters()
						.getChromosomeClass()
						.getSimpleName(),
				null
		);

		// RECORD_EXIT_REASON
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_REASON,
				() -> Arrays.stream(exitInfos)
						.map(ExitInfo::getExitMark)
						.toArray(String[]::new),
				null
		);
		// RECORD_AVG_EXIT_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_AVG_EXIT_CONVERGENCE,
				() -> Arrays.stream(exitInfos)
						.map(ExitInfo::getConvergence)
						.reduce(Integer::sum)
						.orElse(0) / (double) times,
				null
		);
		// RECORD_EXIT_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_CONVERGENCE,
				() -> Arrays.stream(exitInfos)
						.map(ExitInfo::getConvergence)
						.toArray(Integer[]::new),
				null
		);
		// RECORD_AVG_EXIT_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_AVG_EXIT_ITERATION,
				() -> Arrays.stream(exitInfos)
						.map(ExitInfo::getIteration)
						.reduce(Integer::sum)
						.orElse(0) / (double) times,
				null
		);
		// RECORD_EXIT_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_ITERATION,
				() -> Arrays.stream(exitInfos)
						.map(ExitInfo::getIteration)
						.toArray(Integer[]::new),
				null
		);
		// RECORD_EXIT_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_AVG_EXIT_FITNESS,
				() -> Arrays.stream(exitInfos)
						.map(ExitInfo::getFitness)
						.reduce(Double::sum)
						.orElse(0.0) / (double) times,
				null
		);
		// RECORD_EXIT_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_FITNESS,
				() -> Arrays.stream(exitInfos)
						.map(ExitInfo::getFitness)
						.toArray(Double[]::new),
				null
		);
		// RECORD_MEDIAN_ORDER_BY_ITERATION_EXIT_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_MEDIAN_ORDER_BY_ITERATION_EXIT_FITNESS,
				() -> {
					double medianFitness = 0;
					if (exitInfos.length != 0) {
						Arrays.sort(exitInfos, (info1, info2) -> info1.getIteration() - info2.getIteration());
						medianFitness =
								exitInfos.length % 2 == 0 ?
										// 偶
										(exitInfos[exitInfos.length / 2 - 1].getFitness() +
												exitInfos[exitInfos.length / 2].getFitness()
										) / 2.0 :
										// 奇
										exitInfos[exitInfos.length / 2].getFitness();
					}
					return medianFitness;
				},
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

}
