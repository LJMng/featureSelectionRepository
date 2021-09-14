package featureSelection.tester.procedure.opt.improvedHarmonySearch;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.basic.support.calculation.featureImportance.DependencyCalculation;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.ReductionAlgorithm4IHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.classic.ClassicHashMapAttributeReductionIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.classic.ClassicSequentialAttributeReductionIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.classic.ClassicSequentialIDAttributeReductionIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.dependencyCalculation.DirectDependencyCalculationIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.dependencyCalculation.HeuristicDependencyCalculationIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.dependencyCalculation.IncrementalDependencyCalculationIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.RealtimeSimpleCountingRECBasedIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.incrementalPartition.AbstractIPRECBasedIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.incrementalPartition.DynamicGroupIPRECBasedIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.nestedEquivalenceClassBased.incrementalPartition.AbstractIPNECBasedIHS;
import featureSelection.repository.algorithm.opt.improvedHarmonySearch.roughEquivalenceClassBased.nestedEquivalenceClassBased.incrementalPartition.DynamicGroupIPNECBasedIHS;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedDynamicMultiAttrProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.groupNumberCalculator.DefaultPartitionFactorBasedGroupNumberCalculator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4SqrtAttrNumber;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.improvedHarmonySearch.ReductionParameters;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.calculation.DynamicBandWidthAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.calculation.DynamicPitchAdjustmentRateAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.code.BinaryHarmonyInitialization;
import featureSelection.repository.entity.opt.improvedHarmonySearch.impl.harmony.entity.BinaryHarmony;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation.BandWidthAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.calculation.PitchAdjustmentRateAlgorithm;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.code.HarmonyInitialization;
import featureSelection.repository.entity.opt.improvedHarmonySearch.interf.harmony.entity.Harmony;
import featureSelection.repository.support.calculation.dependency.roughEquivalentClassBased.DependencyCalculation4RSCREC;
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
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.improvedHarmonySearch.component.roughEquivalenceClassBased.original.ihsInitiate.ImprovedHarmonySearchInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * ImprovedHarmonySearchFeatureSelectionTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Improved Harmony Search Feature Selection Tester Test")
class ImprovedHarmonySearchFeatureSelectionTesterTest
	extends BasicTester
{
	private boolean logOn = true;
	private int sigDeviation = 0;

	private int randomSeed = 0;
	private int groupSize = 40;
	private double harmonyMemoryConsiderationRate = 0.95;
	private int iteration = 100;
	private int convergence = 30;

	@Test
	void testClassicHash() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new ClassicHashMapAttributeReductionIHS<>(),

						// available:
						//  PositiveRegionCalculation4ClassicHashMap
						//  DependencyCalculation4ClassicHashMap

						PositiveRegionCalculation4ClassicHashMap.class
//						DependencyCalculation4ClassicHashMap.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	void testClassicSequential() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new ClassicSequentialAttributeReductionIHS<>(),

						// available:
						//  PositiveRegionCalculation4ClassicSequential
						//  DependencyCalculation4ClassicSequential

						PositiveRegionCalculation4ClassicSequential.class
//						DependencyCalculation4ClassicSequential.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	void testClassicSequentialID() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new ClassicSequentialIDAttributeReductionIHS<>(),

						// available:
						//  PositiveRegionCalculation4ClassicSequentialID
						//  DependencyCalculation4ClassicSequentialID

						PositiveRegionCalculation4ClassicSequentialID.class
//						DependencyCalculation4ClassicSequentialID.class
				);

		// Execute.
		commonExec(parameters);
	}


	@Test
	void testDDCHash() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new DirectDependencyCalculationIHS<>(),

						// available:
						//  PositiveRegion4DDCHash
						//  DependencyCalculation4DDCHash

						PositiveRegion4DDCHash.class
//						DependencyCalculation4DDCHash.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	void testDDCSequential() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new DirectDependencyCalculationIHS<>(),

						// available:
						//  PositiveRegion4DDCSequential
						//  DependencyCalculation4DDCSequential

						PositiveRegion4DDCSequential.class
//						DependencyCalculation4DDCSequential.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	void testHDCHash() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new HeuristicDependencyCalculationIHS<>(),

						// available:
						//  PositiveRegion4HDCHash
						//  DependencyCalculation4HDCHash

						PositiveRegion4HDCHash.class
//						DependencyCalculation4HDCHash.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	void testHDCSequential() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new HeuristicDependencyCalculationIHS<>(),

						// available:
						//  PositiveRegion4HDCSequential
						//  DependencyCalculation4HDCSequential

						PositiveRegion4HDCSequential.class
//						DependencyCalculation4HDCSequential.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	void testIDC() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new IncrementalDependencyCalculationIHS<>(),

						// available:
						//  PositiveRegion4IDC

						PositiveRegion4IDC.class
				);

		// Execute.
		commonExec(parameters);
	}


	@Test
	<Hrmny extends Harmony<?>, FValue extends FitnessValue<?>> void testRSCREC() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						new RealtimeSimpleCountingRECBasedIHS<>(instances.size()),

						// available:
						//  PositiveRegionCalculation4RSCREC
						//  DependencyCalculation4RSCREC

//						PositiveRegionCalculation4RSCREC.class
						DependencyCalculation4RSCREC.class
				);

		// Create a procedure.
		ImprovedHarmonySearchFeatureSelectionTester<Hrmny, FValue> tester =
				new ImprovedHarmonySearchFeatureSelectionTester<>(parameters, logOn);

		// set initiate procedure for S-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"ImprovedHarmonySearchInitiateProcedureContainer",
						new ImprovedHarmonySearchInitiateProcedureContainer4REC<>(parameters, logOn)
				);

		// Execute
		Map<IntArrayKey, Collection<OptimizationReduct>> reducts = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		int i=1;
		System.out.println("distinct reduct list: ");
		for (IntArrayKey distinctReduct: reducts.keySet()){
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
	<Hrmny extends Harmony<?>, FValue extends FitnessValue<?>, Sig extends Number> void testIPREC()
			throws Exception
	{
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
		AbstractIPRECBasedIHS<Sig> redAlg =
				// available:
				//  DynamicGroupIPRECBasedIHS
				//  InTurnIPRECBasedIHS
				new DynamicGroupIPRECBasedIHS<>(instances.size());
		if (redAlg instanceof DynamicGroupIPRECBasedIHS) {
			((DynamicGroupIPRECBasedIHS<Sig>) redAlg)
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

						PositiveRegionCalculation4IPREC.class
				);

		// Create a procedure.
		ImprovedHarmonySearchFeatureSelectionTester<Hrmny, FValue> tester =
				new ImprovedHarmonySearchFeatureSelectionTester<>(parameters, logOn);

		// set initiate procedure for IP-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"ImprovedHarmonySearchInitiateProcedureContainer",
						new ImprovedHarmonySearchInitiateProcedureContainer4REC<>(parameters, logOn)
				);

		// Execute
		Map<IntArrayKey, Collection<OptimizationReduct>> reducts = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		int i=1;
		System.out.println("distinct reduct list: ");
		for (IntArrayKey distinctReduct: reducts.keySet()){
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
	<Hrmny extends Harmony<?>, FValue extends FitnessValue<?>, Sig extends Number> void testIPNEC()
			throws Exception
	{
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
		AbstractIPNECBasedIHS<Sig, EquivalenceClass> redAlg =
				// available:
				//  DynamicGroupIPNECBasedIHS
				//  InTurnIPNECBasedIHS
				new DynamicGroupIPNECBasedIHS<>(instances.size());
		if (redAlg instanceof DynamicGroupIPNECBasedIHS) {
			((DynamicGroupIPNECBasedIHS<Sig, EquivalenceClass>) redAlg)
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
						//  PositiveRegionCalculation4IPNEC

						PositiveRegionCalculation4IPNEC.class
				);

		// Create a procedure.
		ImprovedHarmonySearchFeatureSelectionTester<Hrmny, FValue> tester =
				new ImprovedHarmonySearchFeatureSelectionTester<>(parameters, logOn);

		// set initiate procedure for IP-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"ImprovedHarmonySearchInitiateProcedureContainer",
						new ImprovedHarmonySearchInitiateProcedureContainer4REC<>(parameters, logOn)
				);

		// Execute
		Map<IntArrayKey, Collection<OptimizationReduct>> reducts = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		int i=1;
		System.out.println("distinct reduct list: ");
		for (IntArrayKey distinctReduct: reducts.keySet()){
			System.out.println((i++)+": "+ Arrays.toString(distinctReduct.getKey()));
			if (i>20){
				break;
			}
		}
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));
	}


	private <FValue extends FitnessValue<Sig>, Sig extends Number> ProcedureParameters
		getProcedureParameters(
			ReductionAlgorithm4IHS reductionAlgorithm,
			Class<? extends FeatureImportance<Sig>> calculationClass
	){
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		double minPitchAdjustmentRate = 0.4;
		double maxPitchAdjustmentRate = 0.9;

		double minBandWidth = 0.001;
		double maxBandWidth = 0.1;

		PitchAdjustmentRateAlgorithm parAlg =
				new DynamicPitchAdjustmentRateAlgorithm(
						minPitchAdjustmentRate,
						maxPitchAdjustmentRate,
						iteration
				);
		BandWidthAlgorithm bwAlg =
				new DynamicBandWidthAlgorithm(
						minBandWidth,
						maxBandWidth,
						iteration
				);
		HarmonyInitialization<BinaryHarmony> initAlg = new BinaryHarmonyInitialization();
		initAlg.setMinPossibleValueBoundOfHarmonyBit(0);
		initAlg.setMaxPossibleValueBoundOfHarmonyBit(1);

		ReductionParameters<Sig, BinaryHarmony, FValue> params = new ReductionParameters<>();
		params.setGroupSize(groupSize);
		params.setHarmonyMemorySize(attrLength);
		params.setHarmonyMemoryConsiderationRate(harmonyMemoryConsiderationRate);
		params.setIteration(iteration);
		params.setConvergence(convergence);
		params.setMaxFitness(null);
		params.setAttributes(attributes);

		params.setParAlg(parAlg);
		params.setBwAlg(bwAlg);
		params.setRedAlg(reductionAlgorithm);
		params.setHarmonyInitializationAlg(initAlg);
//		params.setFitnessClass(DefaultFitness.class);

		Number sigDeviation = this.sigDeviation;
		if (DependencyCalculation.class.isAssignableFrom(calculationClass)){
			sigDeviation = sigDeviation.doubleValue() + 0.0;
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

	private <Hrmny extends Harmony<?>, FValue extends FitnessValue<?>> void commonExec(
			ProcedureParameters parameters
	) throws Exception {
		// Create a procedure.
		ImprovedHarmonySearchFeatureSelectionTester<Hrmny, FValue> tester =
				new ImprovedHarmonySearchFeatureSelectionTester<>(parameters, logOn);

		// set generation loops procedure (optional)
		tester.getComponent("Generation loops")
				.setSubProcedureContainer(
						"GenerationLoopProcedureContainer",

						// available:
						//  (Default) DefaultGenerationLoopProcedureContainer
						//  GlobalBestPrioritizedGenerationLoopProcedureContainer
						//  ReductPrioritizedGenerationLoopProcedureContainer

						new DefaultGenerationLoopProcedureContainer<>(parameters, logOn)
//						new GlobalBestPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
//						new ReductPrioritizedGenerationLoopProcedureContainer<>(parameters, logOn)
				);

		// Execute
		Map<IntArrayKey, Collection<OptimizationReduct>> reducts = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		int i=1;
		System.out.println("distinct reduct list: ");
		for (IntArrayKey distinctReduct: reducts.keySet()){
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
