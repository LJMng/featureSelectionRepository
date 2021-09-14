package featureSelection.tester.procedure.opt.artificialFishSwarm;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.classic.ClassicHashMapAttributeReductionFSA;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.classic.ClassicSequentialAttributeReductionFSA;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.classic.ClassicSequentialIDAttributeReductionFSA;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.dependencyCalculation.DirectDependencyCalculationFSA;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.dependencyCalculation.HeuristicDependencyCalculationFSA;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.dependencyCalculation.IncrementalDependencyCalculationFSA;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.RealtimeSimpleCountingRoughEquivalenceClassBasedFSA;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.incrementalPartition.AbstractIPRECBasedFSA;
import featureSelection.repository.algorithm.opt.artificialFishSwarm.roughEquivalentClassBased.incrementalPartition.DynamicGroupIPRECBasedFSA;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedDynamicMultiAttrProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.groupNumberCalculator.DefaultPartitionFactorBasedGroupNumberCalculator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4SqrtAttrNumber;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.artificialFishSwarm.ReductionParameters;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.distance.HammingDistanceAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.follow.FishFollowAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.swarm.FishSwarmAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.action.update.FishGroupUpdateAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fish.centerCalculation.PositionFishCenterCalculationAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.fitness.algorithm.FitnessAlgorithm4ByteArray;
import featureSelection.repository.entity.opt.artificialFishSwarm.impl.position.ByteArrayPosition;
import featureSelection.repository.entity.opt.artificialFishSwarm.interf.ReductionAlgorithm;
import featureSelection.repository.support.calculation.dependency.classic.DependencyCalculation4ClassicHashMap;
import featureSelection.repository.support.calculation.dependency.classic.DependencyCalculation4ClassicSequential;
import featureSelection.repository.support.calculation.dependency.classic.DependencyCalculation4ClassicSequentialID;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.directDependencyCalculation.DependencyCalculation4DDCHash;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.directDependencyCalculation.DependencyCalculation4DDCSequential;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.heuristicDependencyCalculation.DependencyCalculation4HDCHash;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.heuristicDependencyCalculation.DependencyCalculation4HDCSequential;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.incrementalDependencyCalculation.DependencyCalculation4IDC;
import featureSelection.repository.support.calculation.dependency.roughEquivalentClassBased.DependencyCalculation4IPREC;
import featureSelection.repository.support.calculation.dependency.roughEquivalentClassBased.DependencyCalculation4RSCREC;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.opt.artificialFishSwarm.component.roughEquivalenceClassBased.original.fsaInitiate.ArtificialFishSwarmInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.apache.commons.math3.util.FastMath;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * ArtificialFishSwarmFeatureSelectionTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Artificial Fish Swarm Feature Selection Tester Test")
class ArtificialFishSwarmFeatureSelectionTesterTest
	extends BasicTester
{
	private boolean logOn = true;
	private double sigDeviation = 10E-13;

	private int randomSeed = 0;
	private int iteration = 3;

	private double cFactor=0.9;
	private int tryNumber = -1;

	private double fitnessDependencyProportion = 0.9;
	private int groupSize = 10;


	@Test
	void testClassicHashMap() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new ClassicHashMapAttributeReductionFSA(),
				DependencyCalculation4ClassicHashMap.class
		);

		commonExec(parameters);
	}

	@Test
	void testClassicSequential() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new ClassicSequentialAttributeReductionFSA(),
				DependencyCalculation4ClassicSequential.class
		);

		commonExec(parameters);
	}

	@Test
	void testClassicSequentialID() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new ClassicSequentialIDAttributeReductionFSA(),
				DependencyCalculation4ClassicSequentialID.class
		);

		commonExec(parameters);
	}


	@Test
	void testDDCHash() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new DirectDependencyCalculationFSA(),
				DependencyCalculation4DDCHash.class
		);

		commonExec(parameters);
	}

	@Test
	void testDDCSequential() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new DirectDependencyCalculationFSA(),
				DependencyCalculation4DDCSequential.class
		);

		commonExec(parameters);
	}

	@Test
	void testHDCHash() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new HeuristicDependencyCalculationFSA(),
				DependencyCalculation4HDCHash.class
		);

		commonExec(parameters);
	}

	@Test
	void testHDCSequential() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new HeuristicDependencyCalculationFSA(),
				DependencyCalculation4HDCSequential.class
		);

		commonExec(parameters);
	}

	@Test
	void testIDC() throws Exception {
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new IncrementalDependencyCalculationFSA(),
				DependencyCalculation4IDC.class
		);

		commonExec(parameters);
	}


	@Test
	void testRSCREC() throws Exception{
		// get loaded parameters
		ProcedureParameters parameters = getProcedureParameters(
				new RealtimeSimpleCountingRoughEquivalenceClassBasedFSA(instances.size()),
				DependencyCalculation4RSCREC.class
		);

		// Create a procedure.
		ArtificialFishSwarmFeatureSelectionTester<Double, byte[], ByteArrayPosition>
				tester = new ArtificialFishSwarmFeatureSelectionTester<>(parameters, logOn);

		// set initiate procedure for S-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"ArtificialFishSwarmInitiateProcedureContainer",
						new ArtificialFishSwarmInitiateProcedureContainer4REC<>(parameters, logOn)
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
	void testIPREC() throws Exception {
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
		AbstractIPRECBasedFSA redAlg =
				// available:
				//  DynamicGroupIPRECBasedFSA
				//  InTurnIPRECBasedFSA
				new DynamicGroupIPRECBasedFSA(instances.size());
		if (redAlg instanceof DynamicGroupIPRECBasedFSA) {
			((DynamicGroupIPRECBasedFSA) redAlg)
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
		ProcedureParameters parameters = getProcedureParameters(redAlg,	DependencyCalculation4IPREC.class);

		// Create a procedure.
		ArtificialFishSwarmFeatureSelectionTester<Double, byte[], ByteArrayPosition>
				tester = new ArtificialFishSwarmFeatureSelectionTester<>(parameters, logOn);

		// set initiate procedure for S-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"ArtificialFishSwarmInitiateProcedureContainer",
						new ArtificialFishSwarmInitiateProcedureContainer4REC<>(parameters, logOn)
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


	<Sig extends Number> ProcedureParameters getProcedureParameters(
			ReductionAlgorithm reductionAlgorithm,
			Class<? extends FeatureImportance<Sig>> calculationClass
	){
		int attrLength = getAttributeLength();
		int[] attributes = getAllConditionalAttributes();

		int visual =  (int) FastMath.max(1, FastMath.ceil(attrLength * 0.5));

		FitnessAlgorithm4ByteArray<DependencyCalculation4ClassicHashMap, Double> fitnessAlg =
				new FitnessAlgorithm4ByteArray<>();
		fitnessAlg.setA(fitnessDependencyProportion);
		fitnessAlg.setLen(attrLength);

		PositionFishCenterCalculationAlgorithm4ByteArray centerAlg =
				new PositionFishCenterCalculationAlgorithm4ByteArray();
		centerAlg.setPositionLength(attrLength);

		ReductionParameters params = new ReductionParameters();
		params.setGroupSize(groupSize);
		params.setVisual(visual);
		params.setCFactor(cFactor);
		params.setTryNumbers(tryNumber);
		params.setIteration(iteration);
		params.setMaxFishExit(-1);
		params.setDistanceCount(new HammingDistanceAlgorithm4ByteArray());
		params.setFitnessAlgorthm(fitnessAlg);
		params.setFishGroupUpdateAlgorithm(new FishGroupUpdateAlgorithm4ByteArray());
		params.setFishSwarmAlgorithm(new FishSwarmAlgorithm4ByteArray<>());
		params.setFishFollowAlgorithm(new FishFollowAlgorithm4ByteArray<>());
		params.setFishCenterCalculationAlgorithm(centerAlg);
		params.setPositionClass(ByteArrayPosition.class);
		params.setReductionAlgorithm(reductionAlgorithm);

		ProcedureParameters parameters =
				new ProcedureParameters()
						// U
						.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
						// C
						.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)

						.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, calculationClass)
						.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, sigDeviation)
						// FSA parameters
						.set(true, ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS, params)
						.set(true, ParameterConstants.PARAMETER_RANDOM_INSTANCE, new Random(randomSeed));

		return parameters;
	}

	void commonExec(ProcedureParameters parameters) throws Exception {
		// Create a procedure.
		ArtificialFishSwarmFeatureSelectionTester<Double, byte[], ByteArrayPosition>
				tester = new ArtificialFishSwarmFeatureSelectionTester<>(parameters, logOn);

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
