package featureSelection.tester.procedure.opt.genetic;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.support.calculation.FeatureImportance;
import featureSelection.basic.support.calculation.featureImportance.DependencyCalculation;
import featureSelection.repository.algorithm.opt.genetic.classic.ClassicHashMapAttributeReductionGA;
import featureSelection.repository.algorithm.opt.genetic.classic.ClassicSequentialAttributeReductionGA;
import featureSelection.repository.algorithm.opt.genetic.classic.ClassicSequentialIDAttributeReductionGA;
import featureSelection.repository.algorithm.opt.genetic.dependencyCalculation.DirectDependencyCalculationGA;
import featureSelection.repository.algorithm.opt.genetic.dependencyCalculation.HeuristicDependencyCalculationGA;
import featureSelection.repository.algorithm.opt.genetic.dependencyCalculation.IncrementalDependencyCalculationGA;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.RealtimeSimpleCountingRECBasedGA;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.incrementalPartition.AbstractIPRECBasedGA;
import featureSelection.repository.algorithm.opt.genetic.roughEquivalenceClassBased.incrementalPartition.DynamicGroupIPRECBasedGA;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedDynamicMultiAttrProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.groupNumberCalculator.DefaultPartitionFactorBasedGroupNumberCalculator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4FixedFactor;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4SqrtAttrNumber;
import featureSelection.repository.entity.opt.OptimizationReduct;
import featureSelection.repository.entity.opt.genetic.ReductionParameters;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.chromosomeEntity.razaChromosome.*;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.code.initialization.razaGene.NumericGeneChromosomeInitialization;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.code.initialization.razaGene.NumericGeneChromosomeInitializationParameters;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.code.mutation.razaGene.NumericChromosomeRandomMutation;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.code.mutation.razaGene.RazaRandomMutationParameters;
import featureSelection.repository.entity.opt.genetic.impl.chromosome.cross.ReverseSequenceChromosomeCross;
import featureSelection.repository.entity.opt.genetic.interf.ReductionAlgorithm;
import featureSelection.repository.entity.opt.genetic.interf.fitness.fitnessValue.FitnessValue;
import featureSelection.repository.support.calculation.positiveRegion.classic.PositiveRegionCalculation4ClassicHashMap;
import featureSelection.repository.support.calculation.positiveRegion.classic.PositiveRegionCalculation4ClassicSequential;
import featureSelection.repository.support.calculation.positiveRegion.classic.PositiveRegionCalculation4ClassicSequentialID;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCHash;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCSequential;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.heuristicDependencyCalculation.PositiveRegion4HDCHash;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.heuristicDependencyCalculation.PositiveRegion4HDCSequential;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.incrementalDependencyCalculation.PositiveRegion4IDC;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4IPREC;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4RSCREC;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop.DefaultGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.common.generationLoop.ReductPrioritizedGenerationLoopProcedureContainer;
import featureSelection.tester.procedure.opt.genetic.procedure.roughEquivalenceClassBased.original.gaInitiate.GeneticAlgorithmInitiateProcedureContainer4REC;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.Random;

/**
 * GeneticAlgorithmFeatureSelectionTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Genetic Algorithm Feature Selection Tester Test")
class GeneticAlgorithmFeatureSelectionTesterTest
	extends BasicTester
{
	private boolean logOn = true;

	private int randomSeed = 1;
	private int population = 10;
	private int reserveNum = 5;
	private int iteration = 50;
	private int convergence = 10;
	private int sigDeviation = 0;

	private double initGeneRate = 0.2;

	private int mutateSize = 2;			// mutateSize = (int) attr / 5 & mutateSize>=1
	private double mutateRate = 0.5;

	@Test
	void testClassicHashMap() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaClassicChromosome.class,
						new ClassicHashMapAttributeReductionGA<>(RazaClassicChromosome.class),
						// available:
						//  PositiveRegionCalculation4ClassicHashMap
						//  DependencyCalculation4ClassicHashMap
						PositiveRegionCalculation4ClassicHashMap.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	void testClassicSequential() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaClassicChromosome.class,
						new ClassicSequentialAttributeReductionGA<>(RazaClassicChromosome.class),
						// available:
						//  PositiveRegionCalculation4ClassicSequential
						//  DependencyCalculation4ClassicSequential
						PositiveRegionCalculation4ClassicSequential.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	void testClassicSequentialID() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaClassicChromosome.class,
						new ClassicSequentialIDAttributeReductionGA<>(RazaClassicChromosome.class),
						// available:
						//  PositiveRegionCalculation4ClassicSequentialID:
						//  DependencyCalculation4ClassicSequentialID:
						PositiveRegionCalculation4ClassicSequentialID.class
				);

		// Execute.
		commonExec(parameters);
	}


	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDDCHash() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaDDCChromosome.class,
						new DirectDependencyCalculationGA<>(RazaDDCChromosome.class),
						// available:
						//  PositiveRegion4DDCHash
						//  DependencyCalculation4DDCHash
						PositiveRegion4DDCHash.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testDDCSequential() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaDDCChromosome.class,
						new DirectDependencyCalculationGA<>(RazaDDCChromosome.class),
						// available:
						//  PositiveRegion4DDCSequential
						//  DependencyCalculation4DDCSequential
						PositiveRegion4DDCSequential.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testHDCHash() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaHDCChromosome.class,
						new HeuristicDependencyCalculationGA<>(RazaHDCChromosome.class),
						// available:
						//  PositiveRegion4HDCHash
						//  DependencyCalculation4HDCHash
						PositiveRegion4HDCHash.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testHDCSequential() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaHDCChromosome.class,
						new HeuristicDependencyCalculationGA<>(RazaHDCChromosome.class),
						// available:
						//  PositiveRegion4HDCSequential
						//  DependencyCalculation4HDCSequential
						PositiveRegion4HDCSequential.class
				);

		// Execute.
		commonExec(parameters);
	}

	@Test
	@SuppressWarnings({ "unchecked", "rawtypes" })
	void testIDC() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaIDCChromosome.class,
						new IncrementalDependencyCalculationGA<>(RazaIDCChromosome.class),
						// available:
						//  PositiveRegion4IDC
						//  DependencyCalculation4IDC
						PositiveRegion4IDC.class
				);

		// Execute.
		commonExec(parameters);
	}


	@Test
	void testRSCREC() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// get loaded parameters
		ProcedureParameters parameters =
				getProcedureParameters(
						attrLength, attributes,
						RazaRECChromosome.class,
						new RealtimeSimpleCountingRECBasedGA<>(RazaRECChromosome.class, instances.size()),
						// available:
						//  PositiveRegionCalculation4RSCREC
						//  DependencyCalculation4RSCREC
						PositiveRegionCalculation4RSCREC.class
//						DependencyCalculation4RSCREC.class
				);

		// Create a procedure.
		GeneticAlgorithmFeatureSelectionTester<RazaRECChromosome, FitnessValue<Double>>
				tester = new GeneticAlgorithmFeatureSelectionTester<>(parameters, logOn);

		// set initiate procedure for S-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"GeneticAlgorithmInitiateProcedureContainer",
						new GeneticAlgorithmInitiateProcedureContainer4REC<>(tester.getParameters(), logOn)
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
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

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
		AbstractIPRECBasedGA<RazaRECChromosome, Double> redAlg =
				// available:
				//  DynamicGroupIPRECBasedGA
				//  InTurnIPRECBasedGA
				new DynamicGroupIPRECBasedGA<>(RazaRECChromosome.class, instances.size());
		if (redAlg instanceof DynamicGroupIPRECBasedGA) {
			((DynamicGroupIPRECBasedGA<RazaRECChromosome, Double>) redAlg)
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
						attrLength, attributes,
						RazaRECChromosome.class,
						redAlg,
						PositiveRegionCalculation4IPREC.class
				);

		// Create a procedure.
		GeneticAlgorithmFeatureSelectionTester<RazaRECChromosome, FitnessValue<Double>>
				tester = new GeneticAlgorithmFeatureSelectionTester<>(parameters, logOn);

		// set initiate procedure for IP-REC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"GeneticAlgorithmInitiateProcedureContainer",
						new GeneticAlgorithmInitiateProcedureContainer4REC<>(tester.getParameters(), logOn)
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
	void testIPNEC() throws Exception {

		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();
		// |C|
		int attrLength = getAttributeLength();

		// set & check mutateSize
		mutateSize = attrLength / 5;
		if (mutateSize<=0){
			mutateSize=1;
		}

		// Set IP-REC parameters
		AttrProcessStrategyParams incAttrProcessParam =
				new AttrProcessStrategyParams()
						.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_PARTITION_FACTOR_STRATEGY,

								// Can be one of the following:
								//  CapacityCal4SqrtAttrSize
								//  CapacityCalculator4FixedAttributeSize

								new PartitionFactorStrategy4FixedFactor(2)
//								new PartitionFactorStrategy4SqrtAttrNumber()
						)
						.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_GROUP_NUMBER_CALCULATOR,
								new DefaultPartitionFactorBasedGroupNumberCalculator()
						);//*/
		AbstractIPRECBasedGA<RazaRECChromosome, Double> redAlg =
				// available:
				//  DynamicGroupIPRECBasedGA
				//  InTurnIPRECBasedGA
				new DynamicGroupIPRECBasedGA<>(RazaRECChromosome.class, instances.size());
		if (redAlg instanceof DynamicGroupIPRECBasedGA) {
			((DynamicGroupIPRECBasedGA<RazaRECChromosome, Double>) redAlg)
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
						attrLength, attributes,
						RazaRECChromosome.class,
						redAlg,
						PositiveRegionCalculation4IPREC.class
				);

		// Create a procedure.
		GeneticAlgorithmFeatureSelectionTester<RazaRECChromosome, FitnessValue<Double>>
				tester = new GeneticAlgorithmFeatureSelectionTester<>(parameters, logOn);

		// set initiate procedure for IP-NEC
		tester.getComponent("Initiate")
				.setSubProcedureContainer(
						"GeneticAlgorithmInitiateProcedureContainer",
						new GeneticAlgorithmInitiateProcedureContainer4REC<>(tester.getParameters(), logOn)
				);

		// set generation loops procedure (optional)
		tester.getComponent("Generation loops")
				.setSubProcedureContainer(
						"DefaultGenerationLoopProcedureContainer",

						// available:
						//  (default) DefaultGenerationLoopProcedureContainer
						//  ReductPrioritizedGenerationLoopProcedureContainer
						//  GlobalBestPrioritizedGenerationLoopProcedureContainer

//						new DefaultGenerationLoopProcedureContainer<>(tester.getParameters(), logOn)
						new ReductPrioritizedGenerationLoopProcedureContainer<>(tester.getParameters(), logOn)
//						new GlobalBestPrioritizedGenerationLoopProcedureContainer(tester.getParameters(), logOn)
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


	private <FI extends FeatureImportance<Sig>, Sig extends Number, CollectionItem,
			 Chr extends RazaChromosome> ProcedureParameters
		getProcedureParameters(
			int attrLength, int[] attributes,
			Class<Chr> chromosomeClass, ReductionAlgorithm reductionAlgorithm,
			Class<FI> calculationClass
	){
		// Set parameters for GA
		NumericGeneChromosomeInitialization<Chr> chromosomeInitAlg =
				new NumericGeneChromosomeInitialization<>(
						new NumericGeneChromosomeInitializationParameters<>(
								population, attrLength, attributes, initGeneRate,
								chromosomeClass
						)
				);
		NumericChromosomeRandomMutation<Chr> mutationAlg =
				new NumericChromosomeRandomMutation<>(
						new RazaRandomMutationParameters<>(
								chromosomeClass,
								mutateSize, mutateRate,
								attributes
						)
				);

		// Load GA parameters
		ReductionParameters<FI, Sig, CollectionItem, Chr, FitnessValue<Sig>> params =
				new ReductionParameters<>();
		params.setPopulation(population);
		params.setChromosomeLength(attrLength);
		params.setChromosomeSwitchNum(attrLength/2);
		params.setReserveNum(reserveNum);
		params.setIterateNum(iteration);
		params.setConvergenceLimit(convergence);
		params.setMaxFitness(null);
		params.setChromosomeInitAlgorithm(chromosomeInitAlg);
		params.setMutationAlgorithm(mutationAlg);
		params.setCrossAlgorithm(new ReverseSequenceChromosomeCross<>());
		params.setReductionAlgorithm(reductionAlgorithm);

		Number sigDeviation = this.sigDeviation;
		if (DependencyCalculation.class.isAssignableFrom(calculationClass)){
			sigDeviation = sigDeviation.doubleValue() + 0.0;
		}

		// Load parameters for procedure.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)

				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, calculationClass)
				.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, sigDeviation)

				.set(true, ParameterConstants.PARAMETER_OPTIMIZATION_PARAMETERS, params)
				.set(true, ParameterConstants.PARAMETER_RANDOM_INSTANCE, new Random(randomSeed));

		return parameters;
	}

	private <Chr extends RazaChromosome, Sig extends Number> void commonExec(
			ProcedureParameters parameters
	) throws Exception {
		// Create a procedure.
		GeneticAlgorithmFeatureSelectionTester<Chr, FitnessValue<Sig>>
				tester = new GeneticAlgorithmFeatureSelectionTester<>(parameters, logOn);

		// set generation loops procedure (optional)
		tester.getComponent("Generation loops")
				.setSubProcedureContainer(
						"DefaultGenerationLoopProcedureContainer",

						// available:
						//  (default) DefaultGenerationLoopProcedureContainer
						//  ReductPrioritizedGenerationLoopProcedureContainer
						//  GlobalBestPrioritizedGenerationLoopProcedureContainer

						new DefaultGenerationLoopProcedureContainer<>(tester.getParameters(), logOn)
//						new ReductPrioritizedGenerationLoopProcedureContainer<>(tester.getParameters(), logOn)
//						new GlobalBestPrioritizedGenerationLoopProcedureContainer(tester.getParameters(), logOn)
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
