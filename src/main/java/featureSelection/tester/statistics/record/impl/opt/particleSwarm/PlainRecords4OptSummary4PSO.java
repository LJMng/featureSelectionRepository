package featureSelection.tester.statistics.record.impl.opt.particleSwarm;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.inertiaWeight.IterationBasedDoubleInertiaWeightAlgorithm;
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
 * Individual Plain Records for Particle Swarm Optimization.
 */
public class PlainRecords4OptSummary4PSO
	extends PlainRecords4OptSummary
{
	public PlainRecords4OptSummary4PSO(Collection<RecordFieldInfo> titles) {
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

	@SuppressWarnings("rawtypes")
	private void loadParametersGeneralInfo4Summary(
			ProcedureParameters parameters,
			ExecutionInfoStorage executionInfoStorage
	) {
		ReductionParameters reductParams =
				parameters.get(ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS);

		// PARAM_POPULATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_POPULATION,
				() -> reductParams.getPopulation(),
				null
		);
		// PARAM_PARTICLE_LENGTH
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_PARTICLE_LENGTH,
				() -> reductParams.getAttributes().length,
				null
		);
		// PARAM_C1
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_C1,
				() -> reductParams.getC1(),
				null
		);
		// PARAM_C2
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_C2,
				() -> reductParams.getC2(),
				null
		);
		// PARAM_R1
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_R1,
				() -> reductParams.getR1(),
				null
		);
		// PARAM_R2
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_R2,
				() -> reductParams.getR2(),
				null
		);
		// PARAM_MIN_VELOCITY
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MIN_VELOCITY,
				() -> reductParams.getVelocityMin(),
				null
		);
		// PARAM_MAX_VELOCITY
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MAX_VELOCITY,
				() -> reductParams.getVelocityMax(),
				null
		);
		// PARAM_MAX_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_MAX_FITNESS,
				() -> reductParams.getMaxFitness(),
				null
		);
		// PARAM_ITERATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_ITERATION,
				() -> reductParams.getIteration(),
				null
		);
		// PARAM_CONVERGENCE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_CONVERGENCE,
				() -> reductParams.getConvergence(),
				null
		);
		// PARAM_RANDOM_SEED
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_RANDOM_SEED,
				() -> executionInfoStorage.getRandomSeeds(),
				null
		);
		// PARAM_4_ALG_PARTICLE_INITIATION
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_4_ALG_PARTICLE_INITIATION,
				() -> String.format("%s(%s)",
						reductParams.getParticleInitAlgorithm().getClass().getSimpleName(),
						reductParams.getParticleInitAlgorithmParameters().toString()
				),
				null
		);
		// PARAM_4_ALG_PARTICLE_UPDATE
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.PARAM_4_ALG_PARTICLE_UPDATE,
				() -> reductParams.getParticleUpdateAlgorithm().getClass().getSimpleName(),
				null
		);
		// PARAM_4_ALG_INERTIA_WEIGHT
		this.set(
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
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_REASONS,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getExitMark)
						.toArray(String[]::new),
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
		// RECORD_AVG_EXIT_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_AVG_EXIT_FITNESS,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getFitness)
						.reduce(Double::sum)
						.orElse(0.0) / (double) executionInfoStorage.getTimes(),
				null
		);
		// RECORD_EXIT_FITNESS
		this.set(
				titleMap, StatisticsConstants.OptimizationInfo.ParticleSwarm.RECORD_EXIT_FITNESS,
				() -> Arrays.stream(executionInfoStorage.getExitInfos())
						.map(ExitInfo::getFitness)
						.toArray(Double[]::new),
				null
		);
	}

	/* ---------------------------------------------------------------------------------- */

}
