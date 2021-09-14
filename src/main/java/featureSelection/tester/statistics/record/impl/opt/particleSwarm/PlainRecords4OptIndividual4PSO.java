package featureSelection.tester.statistics.record.impl.opt.particleSwarm;

import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.inertiaWeight.IterationBasedDoubleInertiaWeightAlgorithm;
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
 * Individual Plain Records for Particle Swarm Optimization.
 */
public class PlainRecords4OptIndividual4PSO
		extends PlainRecords4OptIndividual
{

	public PlainRecords4OptIndividual4PSO(Collection<RecordFieldInfo> titles) {
		super(titles);
	}

	/* ---------------------------------------------------------------------------------- */

	@Override
	protected void load(
			String summaryID, DatasetInfo datasetInfo, int[] attributes, int times,
			int currentTime, int randomSeed, ProcedureParameters parameters,
			String testName, Statistics statistics,
			Map<String, Long> componentTagTimeMap, ProcedureContainer<?> container
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
			PlainRecord record, ProcedureParameters parameters, Statistics statistics,
			int randomSeed
	) {
		@SuppressWarnings("rawtypes")
		ReductionParameters reductParams =
				parameters.get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);

		// PARAM_POPULATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_POPULATION,
				() -> reductParams.getPopulation(),
				null
		);
		// PARAM_PARTICLE_LENGTH
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_PARTICLE_LENGTH,
				() -> reductParams.getAttributes().length,
				null
		);
		// PARAM_C1
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_C1,
				() -> reductParams.getC1(),
				null
		);
		// PARAM_C2
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_C2,
				() -> reductParams.getC2(),
				null
		);
		// PARAM_R1
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_R1,
				() -> reductParams.getR1(),
				null
		);
		// PARAM_R2
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_R2,
				() -> reductParams.getR2(),
				null
		);
		// PARAM_MIN_VELOCITY
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MIN_VELOCITY,
				() -> reductParams.getVelocityMin(),
				null
		);
		// PARAM_MAX_VELOCITY
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MAX_VELOCITY,
				() -> reductParams.getVelocityMax(),
				null
		);
		// PARAM_MAX_FITNESS
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MAX_FITNESS,
				() -> reductParams.getMaxFitness(),
				null
		);
		// PARAM_ITERATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_ITERATION,
				() -> reductParams.getIteration(),
				null
		);
		// PARAM_CONVERGENCE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_CONVERGENCE,
				() -> reductParams.getConvergence(),
				null
		);
		// PARAM_RANDOM_SEED
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_RANDOM_SEED,
				() -> randomSeed,
				null
		);
		// PARAM_4_ALG_PARTICLE_INITIATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_4_ALG_PARTICLE_INITIATION,
				() -> reductParams.getParticleInitAlgorithm()
						.getClass()
						.getSimpleName(),
				null
		);
		// PARAM_4_ALG_PARTICLE_UPDATE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_4_ALG_PARTICLE_UPDATE,
				() -> reductParams.getParticleUpdateAlgorithm()
						.getClass()
						.getSimpleName(),
				null
		);
		// PARAM_4_ALG_INERTIA_WEIGHT
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_4_ALG_INERTIA_WEIGHT,
				() -> {
					String inertiaWeightValue;
					if (IterationBasedDoubleInertiaWeightAlgorithm.class.equals(
							reductParams.getInertiaWeightAlgorithm().getClass())
					) {
						inertiaWeightValue =
								String.format("[%.2f, %.2f] %s",
										reductParams.getInertiaWeightAlgorithmParameters()
												.getMinInertiaWeight(),
										reductParams.getInertiaWeightAlgorithmParameters()
												.getMaxInertiaWeight(),
										IterationBasedDoubleInertiaWeightAlgorithm.class.getSimpleName()
								);
					} else {
						inertiaWeightValue =
								reductParams.getInertiaWeightAlgorithm()
										.getClass()
										.getSimpleName();
					}
					return inertiaWeightValue;
				},
				null
		);
		// RECORD_EXIT_REASONS
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_REASONS,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_REASON),
				null
		);
		// RECORD_EXIT_ITERATION
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_ITERATION,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_ITERATION),
				null
		);
		// RECORD_EXIT_CONVERGENCE
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_CONVERGENCE,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_CONVERGENCE),
				null
		);
		// RECORD_EXIT_FITNESS
		record.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_FITNESS,
				statistics.get(StatisticsConstants.OptimizationInfo.Exit.STATISTIC_OPTIMIZATION_EXIT_FITNESS),
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

}
