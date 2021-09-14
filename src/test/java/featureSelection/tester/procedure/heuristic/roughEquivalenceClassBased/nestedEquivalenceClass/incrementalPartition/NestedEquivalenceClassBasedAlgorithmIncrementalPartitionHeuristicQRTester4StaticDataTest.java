package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.nestedEquivalenceClass.incrementalPartition;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedDynamicMultiAttrProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.groupNumberCalculator.DefaultPartitionFactorBasedGroupNumberCalculator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4SqrtAttrNumber;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.entity.alg.rec.nestedEC.reductionResult.ReductionResult4Static;
import featureSelection.repository.support.calculation.positiveRegion.roughEquivalenceClassBased.PositiveRegionCalculation4IPNEC;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalPartition.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;

/**
 * NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StaticData Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Nested Equivalence Class Based Algorithm Incremental Partition Heuristic QR Tester 4 Static Data Test")
class NestedEquivalenceClassBasedAlgorithmIncrementalPartitionHeuristicQRTester4StaticDataTest
	extends BasicTester
{
	private boolean logOn = true;
	private boolean execCore = true;

	@Test
	void testExec() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

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
				.setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
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
	}

}
