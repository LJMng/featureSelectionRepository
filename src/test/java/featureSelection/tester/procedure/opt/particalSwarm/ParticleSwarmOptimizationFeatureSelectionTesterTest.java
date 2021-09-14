package featureSelection.tester.procedure.opt.particalSwarm;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.basic.support.calculation.featureImportance.DependencyCalculation;
import featureSelection.repository.algorithm.opt.particleSwarm.classic.ClassicHashMapAttributeReductionHannahPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.classic.ClassicSequentialAttributeReductionHannahPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.classic.ClassicSequentialIDAttributeReductionHannahPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.dependencyCalculation.DirectDependencyCalculationHannahPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.dependencyCalculation.HeuristicDependencyCalculationHannahPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.dependencyCalculation.IncrementalDependencyCalculationHannahPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.RealtimeSimpleCountingRECBasedPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.incrementalPartition.AbstractIPRECBasedPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.incrementalPartition.DynamicGroupIPRECBasedPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.nestedEquivalenceClassBased.incrementalPartition.AbstractIPNECBasedPSO;
import featureSelection.repository.algorithm.opt.particleSwarm.roughEquivalenceClassBased.nestedEquivalenceClassBased.incrementalPartition.DynamicGroupIPNECBasedPSO;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedDynamicMultiAttrProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.groupNumberCalculator.DefaultPartitionFactorBasedGroupNumberCalculator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4SqrtAttrNumber;
import featureSelection.repository.entity.opt.particleSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.inertiaWeight.DefaultInertiaWeightAlgorithmParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.inertiaWeight.IterationBasedDoubleInertiaWeightAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.code.initialization.hannah.HannahInitializationParameters;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.code.initialization.hannah.HannahParticleInitialization;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.code.update.hannah.HannahParticleUpdateAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.impl.particle.entity.position.HannahPosition;
import featureSelection.repository.entity.opt.particleSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.particleSwarm.interf.fitness.value.FitnessValue;
import featureSelection.repository.support.calculation.positiveRegion.classic.PositiveRegionCalculation4ClassicHashMap;
import featureSelection.repository.support.calculation.positiveRegion.classic.PositiveRegionCalculation4ClassicSequential;
import featureSelection.repository.support.calculation.positiveRegion.classic.PositiveRegionCalculation4ClassicSequentialID;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCHash;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCSequential;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.heuristicDependencyCalculation.PositiveRegion4HDCHash;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.heuristicDependencyCalculation.PositiveRegion4HDCSequential;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.incrementalDependencyCalculation.PositiveRegion4IDC;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4IPNEC;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4IPREC;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4RSCREC;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.particalSwarm.procedure.roughEquivalenceClassBased.original.psoInitiate.ParticleSwarmOptimizationInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

/**
 * ParticleSwarmOptimizationFeatureSelectionTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Particle Swarm Optimization Feature Selection Tester Test")
class ParticleSwarmOptimizationFeatureSelectionTesterTest
	extends BasicTester
{
	private boolean logOn = true;
	private int sigDeviation = 0;

	private int randomSeed = 1;
	private int population = 10;
	private int iteration = 50;
	private int convergence = 5;

	private double minInertiaWeight = 0.9;
	private double maxInertiaWeight = 1.2;

	private double c1 = 0.5;
	private double c2 = 0.5;
	private int minVelocity = 1;
	private double r1 = -1;
	private double r2 = -1;

	private double initAttrRate = 0.5;

	@Test
	public void testClassicHashMap() throws Exception {
		ProcedureParameters parameters = getProcedureParameters(
				new ClassicHashMapAttributeReductionHannahPSO<>(),
				PositiveRegionCalculation4ClassicHashMap.class
		);

		commonExecution(parameters);
	}

	@Test
	public void testClassicSequential() throws Exception {
		ProcedureParameters parameters = getProcedureParameters(
				new ClassicSequentialAttributeReductionHannahPSO<>(),
				PositiveRegionCalculation4ClassicSequential.class
		);

		commonExecution(parameters);
	}

	@Test
	public void testClassicSequentialID() throws Exception {
		ProcedureParameters parameters = getProcedureParameters(
				new ClassicSequentialIDAttributeReductionHannahPSO<>(),
				PositiveRegionCalculation4ClassicSequentialID.class
		);

		commonExecution(parameters);
	}


	@Test
	public void testDDCHash() throws Exception {
		ProcedureParameters parameters = getProcedureParameters(
				new DirectDependencyCalculationHannahPSO<>(),
				// available:
				//  PositiveRegion4DDCHash
				//  DependencyCalculation4DDCHash
				PositiveRegion4DDCHash.class
//				DependencyCalculation4DDCHash.class
		);

		commonExecution(parameters);
	}

	@Test
	public void testDDCSequential() throws Exception {
		ProcedureParameters parameters = getProcedureParameters(
				new DirectDependencyCalculationHannahPSO<>(),
				// available:
				//  PositiveRegion4DDCSequential
				//  DependencyCalculation4DDCSequential
				PositiveRegion4DDCSequential.class
//				DependencyCalculation4DDCSequential.class
		);

		commonExecution(parameters);
	}


	@Test
	public void testHDCHash() throws Exception {
		ProcedureParameters parameters = getProcedureParameters(
				new HeuristicDependencyCalculationHannahPSO<>(),
				// available:
				//  PositiveRegion4HDCHash
				//  DependencyCalculation4HDCHash
				PositiveRegion4HDCHash.class
//				DependencyCalculation4HDCHash.class
		);

		commonExecution(parameters);
	}

	@Test
	public void testHDCSequential() throws Exception{
		ProcedureParameters parameters = getProcedureParameters(
				new HeuristicDependencyCalculationHannahPSO<>(),
				// available:
				//  PositiveRegion4HDCSequential
				//  DependencyCalculation4HDCSequential
				PositiveRegion4HDCSequential.class
//				DependencyCalculation4HDCSequential.class
		);

		commonExecution(parameters);
	}


	@Test
	public void testIDC() throws Exception {
		ProcedureParameters parameters = getProcedureParameters(
				new IncrementalDependencyCalculationHannahPSO<>(),
				// available:
				//  PositiveRegion4HDCSequential
				PositiveRegion4IDC.class
//				DependencyCalculation4IDC.class
		);

		commonExecution(parameters);
	}


	@Test
	public <Sig extends Number> void testRSCREC() throws Exception {
		ProcedureParameters parameters = getProcedureParameters(
				new RealtimeSimpleCountingRECBasedPSO<>(instances.size()),
				// available:
				//  PositiveRegion4HDCSequential
				//  DependencyCalculation4RSCREC
				PositiveRegionCalculation4RSCREC.class
//				DependencyCalculation4RSCREC.class
		);

		// Create a procedure.
		ParticleSwarmOptimizationFeatureSelectionTester<Integer, HannahPosition, FitnessValue<Sig>>
				tester = new ParticleSwarmOptimizationFeatureSelectionTester<>(parameters, logOn);

		tester.getComponent("Generation loops")
				.setSubProcedureContainer(
						"DefaultGenerationLoopProcedureContainer",

						// available:
						//  (default) DefaultGenerationLoopProcedureContainer
						//  ReductPrioritizedGenerationLoopProcedureContainer
						//  GlobalBestPrioritizedGenerationLoopProcedureContainer

						new DefaultGenerationLoopProcedureContainer<>(parameters, logOn)
//						new ReductPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
//						new GlobalBestPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
				);

		// set initiate procedure for S-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"ParticleSwarmOptimizationInitiateProcedureContainer",
						new ParticleSwarmOptimizationInitiateProcedureContainer4REC<>(parameters, logOn)
				);

		// Execute
		Collection<IntArrayKey> reducts = tester.exec().keySet();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		int i=1;
		System.out.println("distinct reduct list: ");
		for (IntArrayKey distinctReduct: reducts){
			System.out.println((i++)+": "+ Arrays.toString(distinctReduct.getKey()));
			if (i>20){
				break;
			}
		}
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));
	}

	@Test
	public <Sig extends Number> void testIPREC() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// Set IP-REC parameters
		AttrProcessStrategyParams incAttrProcessParam =
				new AttrProcessStrategyParams()
						.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_PARTITION_FACTOR_STRATEGY,

								// Can be one of the following:
								//  CapacityCal4SqrtAttrSize
								//  CapacityCalculator4FixedAttributeSize

								//new PartitionFactorStrategy4FixedFactor(1)
								new PartitionFactorStrategy4SqrtAttrNumber()
						)
						.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_GROUP_NUMBER_CALCULATOR,
								new DefaultPartitionFactorBasedGroupNumberCalculator()
						);//*/
		AbstractIPRECBasedPSO<Sig> redAlg =
				// available:
				//  DynamicGroupIPRECBasedGA
				//  InTurnIPRECBasedGA
				new DynamicGroupIPRECBasedPSO<>(instances.size());
//				new InTurnIPRECBasedPSO<>(instances.size());

		if (redAlg instanceof DynamicGroupIPRECBasedPSO) {
			((DynamicGroupIPRECBasedPSO<Sig>) redAlg)
					.setIncPartitionAttributeProcessStrategy(
							new PartitionFactorBasedDynamicMultiAttrProcessStrategy(incAttrProcessParam)
					);
		}

		redAlg.setInspectAttributeProcessStrategyClass(AttrProcessStrategy4Comb.class);
		redAlg.setInspectAttributeProcessCapacityCalculator(
				// available:
				//  CapacityCal4SqrtAttrSize
				//  CapacityCalculator4FixedAttributeSize

				new CapacityCal4SqrtAttrSize()
				//new CapacityCalculator4FixedAttributeSize(3)
		);

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						redAlg,
						// available:
						//  PositiveRegionCalculation4IPREC
						//  DependencyCalculation4RSCREC
						PositiveRegionCalculation4IPREC.class
//						DependencyCalculation4IPREC.class
				);

		// Create a procedure.
		ParticleSwarmOptimizationFeatureSelectionTester<Integer, HannahPosition, FitnessValue<Sig>>
				tester = new ParticleSwarmOptimizationFeatureSelectionTester<>(parameters, logOn);

		tester.getComponent("Generation loops")
				.setSubProcedureContainer(
						"DefaultGenerationLoopProcedureContainer",

						// available:
						//  (default) DefaultGenerationLoopProcedureContainer
						//  ReductPrioritizedGenerationLoopProcedureContainer
						//  GlobalBestPrioritizedGenerationLoopProcedureContainer

						new DefaultGenerationLoopProcedureContainer<>(parameters, logOn)
//						new ReductPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
//						new GlobalBestPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
				);

		// set initiate procedure for S-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"ParticleSwarmOptimizationInitiateProcedureContainer",
						new ParticleSwarmOptimizationInitiateProcedureContainer4REC<>(parameters, logOn)
				);

		// Execute
		Collection<IntArrayKey> reducts = tester.exec().keySet();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		int i=1;
		System.out.println("distinct reduct list: ");
		for (IntArrayKey distinctReduct: reducts){
			System.out.println((i++)+": "+ Arrays.toString(distinctReduct.getKey()));
			if (i>20){
				break;
			}
		}
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));

	}

	@Test
	public <Sig extends Number> void testIPNEC() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// Set IP-NEC parameters
		AttrProcessStrategyParams incAttrProcessParam =
				new AttrProcessStrategyParams()
						.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_PARTITION_FACTOR_STRATEGY,

								// Can be one of the following:
								//  CapacityCal4SqrtAttrSize
								//  CapacityCalculator4FixedAttributeSize

								//new PartitionFactorStrategy4FixedFactor(1)
								new PartitionFactorStrategy4SqrtAttrNumber()
						)
						.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_GROUP_NUMBER_CALCULATOR,
								new DefaultPartitionFactorBasedGroupNumberCalculator()
						);//*/
		AbstractIPNECBasedPSO<EquivalenceClass, Sig> redAlg =
				// available:
				//  DynamicGroupIPRECBasedGA
				//  InTurnIPRECBasedGA
				new DynamicGroupIPNECBasedPSO<>(instances.size());

		((DynamicGroupIPNECBasedPSO<?, ?>) redAlg)
				.setIncPartitionAttributeProcessStrategy(
						new PartitionFactorBasedDynamicMultiAttrProcessStrategy(incAttrProcessParam)
				);

		redAlg.setInspectAttributeProcessStrategyClass(AttrProcessStrategy4Comb.class);
		redAlg.setInspectAttributeProcessCapacityCalculator(
				// available:
				//  CapacityCal4SqrtAttrSize
				//  CapacityCalculator4FixedAttributeSize

				new CapacityCal4SqrtAttrSize()
				//new CapacityCalculator4FixedAttributeSize(3)
		);

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						redAlg,
						// available:
						//  PositiveRegionCalculation4IPNEC
						PositiveRegionCalculation4IPNEC.class
				);

		// Create a procedure.
		ParticleSwarmOptimizationFeatureSelectionTester<Integer, HannahPosition, FitnessValue<Sig>>
				tester = new ParticleSwarmOptimizationFeatureSelectionTester<>(parameters, logOn);

		tester.getComponent("Generation loops")
				.setSubProcedureContainer(
						"DefaultGenerationLoopProcedureContainer",

						// available:
						//  (default) DefaultGenerationLoopProcedureContainer
						//  ReductPrioritizedGenerationLoopProcedureContainer
						//  GlobalBestPrioritizedGenerationLoopProcedureContainer

						new DefaultGenerationLoopProcedureContainer<>(parameters, logOn)
//						new ReductPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
//						new GlobalBestPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
				);

		// set initiate procedure for S-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"ParticleSwarmOptimizationInitiateProcedureContainer",
						new ParticleSwarmOptimizationInitiateProcedureContainer4REC<>(parameters, logOn)
				);

		// Execute
		Collection<IntArrayKey> reducts = tester.exec().keySet();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		int i=1;
		System.out.println("distinct reduct list: ");
		for (IntArrayKey distinctReduct: reducts){
			System.out.println((i++)+": "+ Arrays.toString(distinctReduct.getKey()));
			if (i>20){
				break;
			}
		}
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));
	}


	private <Sig extends Number> ProcedureParameters getProcedureParameters(
			ReductionAlgorithm reductionAlgorithm,
			Class<? extends FeatureImportance<Sig>> calculationClass
	){
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		int maxVelocity = (int) FastMath.max(1, FastMath.ceil(attrLength * 0.33));

		ReductionParameters<Integer, HannahPosition, FitnessValue<Integer>> params = new ReductionParameters<>();
		params.setPopulation(population);
		params.setAttributes(attributes);

		params.setInertiaWeightAlgorithm(new IterationBasedDoubleInertiaWeightAlgorithm());
		params.setInertiaWeightAlgorithmParameters(
				new DefaultInertiaWeightAlgorithmParameters(minInertiaWeight, maxInertiaWeight)
		);
		params.setC1(c1);
		params.setC2(c2);
		params.setVelocityMin(minVelocity);
		params.setVelocityMax(maxVelocity);
		params.setMaxFitness(null);
		params.setConvergence(convergence);
		params.setIteration(iteration);
		params.setR1(r1);
		params.setR2(r2);

		params.setParticleInitAlgorithm(new HannahParticleInitialization<>());
		params.setParticleInitAlgorithmParameters(
				new HannahInitializationParameters<>(
						params.getPopulation(),
						attributes.length,
						minVelocity, maxVelocity,
						initAttrRate
				)
		);
		params.setParticleUpdateAlgorithm(new HannahParticleUpdateAlgorithm<>());

		params.setReductionAlgorithm(reductionAlgorithm);

		Number sigDeviation = this.sigDeviation;
		if (DependencyCalculation.class.isAssignableFrom(calculationClass)){
			sigDeviation = sigDeviation.doubleValue()+0.0;
		}

		ProcedureParameters parameters =
				new ProcedureParameters()
						.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
						.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
						.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, calculationClass)
						.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, sigDeviation)

						.set(true, ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS, params)
						.set(true, ParameterConstants.PARAMETER_RANDOM_INSTANCE, new Random(randomSeed));

		return parameters;
	}

	private <Sig extends Number> void commonExecution(ProcedureParameters parameters) throws Exception {
		// Create a procedure.
		ParticleSwarmOptimizationFeatureSelectionTester<Integer, HannahPosition, FitnessValue<Sig>>
				tester = new ParticleSwarmOptimizationFeatureSelectionTester<>(parameters, logOn);

		tester.getComponent("Generation loops")
				.setSubProcedureContainer(
						"DefaultGenerationLoopProcedureContainer",

						// available:
						//  (default) DefaultGenerationLoopProcedureContainer
						//  ReductPrioritizedGenerationLoopProcedureContainer
						//  GlobalBestPrioritizedGenerationLoopProcedureContainer

						new DefaultGenerationLoopProcedureContainer<>(parameters, logOn)
//						new ReductPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
//						new GlobalBestPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
				);

		// Execute
		Collection<IntArrayKey> reducts = tester.exec().keySet();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		int i=1;
		System.out.println("distinct reduct list: ");
		for (IntArrayKey distinctReduct: reducts){
			System.out.println((i++)+": "+ Arrays.toString(distinctReduct.getKey()));
			if (i>20){
				break;
			}
		}
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));
	}
}
