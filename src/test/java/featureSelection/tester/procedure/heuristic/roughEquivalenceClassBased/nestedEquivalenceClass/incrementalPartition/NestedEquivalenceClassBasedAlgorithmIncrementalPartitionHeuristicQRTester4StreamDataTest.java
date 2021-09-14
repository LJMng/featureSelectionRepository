package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition;

import common.utils.ArrayUtils;
import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.NestedEquivalenceClassBasedAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.NestedEquivalenceClass;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedDynamicMultiAttrProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.groupNumberCalculator.DefaultPartitionFactorBasedGroupNumberCalculator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4SqrtAttrNumber;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.merger.DefaultNestedEquivalentClassesMerger;
import featureSelection.repository.entity.alg.rec.nestedEC.impl.nestedEquivalenceClassesMerge.result.ReductionResult;
import featureSelection.repository.entity.alg.rec.nestedEC.reductionResult.ReductionResult4Static;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4IPNEC;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalPartition.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.basic.SegmentalDataTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.*;

/**
 * NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StreamData Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Nested Equivalence Class Based Algorithm Incremental Partition Heuristic QR Tester 4 Stream Data Test")
@Slf4j
class NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StreamDataTest
	extends SegmentalDataTester
{
	private boolean logOn = true;
	private boolean execCore = true;

	@Test
	public void testExec() throws Exception {
		// |C|
		int attrLength = CollectionUtils.firstOf(sampleParts[0]).getAttributeValues().length-1;
		// C
		int[] attributes = ArrayUtils.initIncrementalValueIntArray(attrLength, 1, 1);

		/* ==================================================================================== */

		Collection<Instance> allInstances = new LinkedList<>();

		/* ====================================== PART 1 ====================================== */

		List<Instance> execInstances = sampleParts[0];
		allInstances.addAll(execInstances);

		ReductionResult4Static<Collection<Integer>, Integer> result =
				execStaticData(execInstances);

		LoggerUtil.printLine(log, "-", 50);

		/* ====================================== PART X ====================================== */

		Collection<Integer> previousReduct = result.getReduct();
		int previousSig = result.getReductSig();

		// Obtain previous NEC.
		Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousNestedEquClasses =
				NestedEquivalenceClassBasedAlgorithm
						.Basic
						.nestedEquivalenceClass(
								NestedEquivalenceClassBasedAlgorithm
										.Basic
										.equivalenceClass(allInstances, new IntegerArrayIterator(attributes))
										.values(),
								new IntegerCollectionIterator(previousReduct)
						).getNestedEquClasses();

		for (int part=1; part<sampleParts.length; part++) {
			execInstances = sampleParts[part];

			ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>,
							Collection<Integer>, Integer>
				results = execIncrementalData(
							part, execInstances, attributes,
							previousReduct, previousSig,
							previousNestedEquClasses
				);

			previousReduct = results.getReduct();
			previousSig = results.getReductSig();
			previousNestedEquClasses = results.getWrappedInstances();

			allInstances.addAll(execInstances);
		}


	}

	private ReductionResult4Static<Collection<Integer>, Integer> execStaticData(
			Collection<Instance> dataset
	) throws Exception {
		// |C|
		int attrLength = CollectionUtils.firstOf(dataset).getAttributeValues().length-1;
		// C
		int[] attributes = ArrayUtils.initIncrementalValueIntArray(attrLength, 1, 1);

		// Prepare IP-NEC parameters.
		AttrProcessStrategyParams coreAttrProcessParam =
				new AttrProcessStrategyParams()
						.set(AttrProcessStrategy4Comb.PARAMETER_EXAM_CAPACITY_CALCULATOR,

								// Can be one of the following:
								//  CapacityCal4SqrtAttrSize
								//  CapacityCalculator4FixedAttributeSize

								new CapacityCal4SqrtAttrSize()
								//new CapacityCalculator4FixedAttributeSize(3)
						);
		AttributeProcessStrategy incPartitionAttributeProcessStrategy =
				new PartitionFactorBasedDynamicMultiAttrProcessStrategy(
						new AttrProcessStrategyParams()
								.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_PARTITION_FACTOR_STRATEGY,

										// Can be one of the following:
										//  PartitionFactorStrategy4FixedFactor
										//  PartitionFactorStrategy4SqrtAttrNumber

										//new PartitionFactorStrategy4FixedFactor(1)
										new PartitionFactorStrategy4SqrtAttrNumber()
								)
								.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_GROUP_NUMBER_CALCULATOR,
										new DefaultPartitionFactorBasedGroupNumberCalculator()
								)
				);

		// Load parameters
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, dataset)
				// C
				.setNonRoot(ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// execute Core ?
				.setNonRoot(ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveRegionCalculation4IPNEC
				.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegionCalculation4IPNEC.class)
				// set significance deviation for calculation:
				//  PositiveRegionCalculation4IPNEC: int
				.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0)
				// set parameters for execution of Core
				.setNonRoot("coreAttributeProcessStrategyParams", coreAttrProcessParam)
				// set parameters for execution of significance calculation/partitioning.
				.setNonRoot("incPartitionAttributeProcessStrategy", incPartitionAttributeProcessStrategy)
				// set parameters for execution of Inspection
				.setNonRoot("inspectAttributeProcessCapacityCalculator", new CapacityCal4SqrtAttrSize())
				// set instance shrinking for IP-NEC.
				.setNonRoot(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE, new Shrink4RECBoundaryClassSetStays());

		log.info("Part 1");

		// Create a procedure.
		NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StaticData<Integer> tester =
				new NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StaticData<>(parameters, logOn);

		// Execute
		ReductionResult4Static<Collection<Integer>, Integer> result = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+result.getReduct());
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));

		return result;
	}

	private ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>, Collection<Integer>, Integer>
		execIncrementalData(
			int part, Collection<Instance> incrementalInstances, int[] attributes,
			Collection<Integer> previousReduct, int previousSig,
			Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>> previousNestedEquClasses
	) throws Exception {

		// Obtain attributes not in the previous reduct
		int[] attributesLeft = new int[attributes.length-previousReduct.size()];
		Collection<Integer> previousReductSet = new HashSet<>(previousReduct);
		for (int i=0, j=0; i<attributes.length; i++) {
			if (!previousReductSet.contains(attributes[i])) {
				attributesLeft[j++] = attributes[i];
			}
		}

		// Prepare IP-NEC parameters.
		AttrProcessStrategyParams coreAttrProcessParam =
				new AttrProcessStrategyParams()
						.set(AttrProcessStrategy4Comb.PARAMETER_EXAM_CAPACITY_CALCULATOR,

								// Can be one of the following:
								//  CapacityCal4SqrtAttrSize
								//  CapacityCalculator4FixedAttributeSize

								new CapacityCal4SqrtAttrSize()
								//new CapacityCalculator4FixedAttributeSize(3)
						);
		AttributeProcessStrategy incPartitionAttributeProcessStrategy =
				new PartitionFactorBasedDynamicMultiAttrProcessStrategy(
						new AttrProcessStrategyParams()
								.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_PARTITION_FACTOR_STRATEGY,

										// Can be one of the following:
										//  PartitionFactorStrategy4FixedFactor
										//  PartitionFactorStrategy4SqrtAttrNumber

										//new PartitionFactorStrategy4FixedFactor(1)
										new PartitionFactorStrategy4SqrtAttrNumber()
								)
								.set(PartitionFactorBasedDynamicMultiAttrProcessStrategy.PARAMETER_GROUP_NUMBER_CALCULATOR,
										new DefaultPartitionFactorBasedGroupNumberCalculator()
								)
				);

		// Load parameters.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, incrementalInstances)
				// C
				.setNonRoot(ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// C-reduct
				.setNonRoot("attributes left", attributesLeft)
				// execute Core ?
				.setNonRoot(ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveRegionCalculation4IPNEC
				.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegionCalculation4IPNEC.class)
				// set significance deviation for calculation:
				//  PositiveRegionCalculation4IPNEC: int
				.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0)
				// set parameters for execution of Core
				.setNonRoot("coreAttributeProcessStrategyParams", coreAttrProcessParam)
				// set parameters for execution of significance calculation/partitioning.
				.setNonRoot("incPartitionAttributeProcessStrategy", incPartitionAttributeProcessStrategy)
				// set parameters for execution of Inspection
				.setNonRoot("inspectAttributeProcessCapacityCalculator", new CapacityCal4SqrtAttrSize())
				// set instance shrinking for IP-NEC.
				.setNonRoot(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE, new Shrink4RECBoundaryClassSetStays())
				// previous info.: NEC
				.setNonRoot(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM, previousNestedEquClasses)
				// previous info.: reduct
				.setNonRoot(ParameterConstants.PARAMETER_PREVIOUS_REDUCT, previousReduct)
				// previous info.: reduct significance
				.setNonRoot(ParameterConstants.PARAMETER_PREVIOUS_REDUCT_SIG, previousSig)
				// set parameter for IP-NEC incremental data NEC merging
				.setNonRoot(ParameterConstants.PARAMETER_IP_NEC_DYNAMIC_DATA_EQUIVALENCE_CLASS_MERGER, new DefaultNestedEquivalentClassesMerger());

		log.info("Part {}", part+1);
		log.info(LoggerUtil.spaceFormat(1, "previous reduct: {}"), previousReduct);

		NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StreamData tester =
				new NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StreamData<>(parameters, logOn);

		ReductionResult<Map<IntArrayKey, NestedEquivalenceClass<EquivalenceClass>>,
						Collection<Integer>, Integer>
				result = tester.exec();

		log.info(LoggerUtil.spaceFormat(1, "New reducts({}): {}"), result.getReduct().size(),
				result.getReduct());

		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+result.getReduct());
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));

		return result;
	}
}
