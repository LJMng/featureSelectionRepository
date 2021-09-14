package featureSelection.tester.statistics.record.impl.opt.genetic;

import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.code.mutation.razaGene.NumericChromosomeRandomMutation;
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
 * Individual Plain Records for Genetic Algorithm.
 */
public class PlainRecords4OptIndividual4GA
		extends PlainRecords4OptIndividual
{

	public PlainRecords4OptIndividual4GA(Collection<RecordFieldInfo> titles) {
		super(titles);
	}

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
			loadParametersInfo4Individual(parameters, statistics, randomSeed);
		}
	}

	/**
	 * Load {@link ProcedureParameters} info. for Genetic Algorithm.
	 *
	 * @param parameters
	 * 		{@link ProcedureParameters} instance.
	 * @param statistics
	 * 		{@link Statistics} instance.
	 * @param randomSeed
	 * 		The randon seed used in execution.
	 */
	@SuppressWarnings("rawtypes")
	private void loadParametersInfo4Individual(
			ProcedureParameters parameters, Statistics statistics,
			int randomSeed
	) {
		featureSelection.repository.entity.opt.genetic.ReductionParameters reductParams =
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
				() -> randomSeed,
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
				() -> statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON),
				null
		);
		// RECORD_EXIT_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_ITERATION,
				() -> statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_ITERATION),
				null
		);
		// RECORD_EXIT_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_CONVERGENCE,
				() -> statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE),
				null
		);
		// RECORD_EXIT_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.Genetic.RECORD_EXIT_FITNESS,
				() -> statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_FITNESS),
				null
		);
	}
}
