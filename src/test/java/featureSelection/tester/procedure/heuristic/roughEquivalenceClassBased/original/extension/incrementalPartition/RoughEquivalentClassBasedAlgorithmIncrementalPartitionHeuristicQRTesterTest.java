package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.capacityCalculator.CapacityCal4SqrtAttrSize;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.PartitionFactorBasedDynamicMultiAttrProcessStrategy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.groupNumberCalculator.DefaultPartitionFactorBasedGroupNumberCalculator;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.partitionFactorBasedDynamicMultiAttribute.partitionFactorStrategy.PartitionFactorStrategy4SqrtAttrNumber;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;

/**
 * RoughEquivalentClassBasedAlgorithmIncrementalPartitionHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Rough Equivalent Class Based Algorithm Incremental Partition Heuristic QR Tester Test")
class RoughEquivalentClassBasedAlgorithmIncrementalPartitionHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;
	private boolean execCore = true;

	@Test
	void testExec() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// Prepare IP-REC parameters
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
				// set parameters for execution of Core
				.setNonRoot("coreAttributeProcessStrategyParams", coreAttrProcessParam)
				// set parameters for execution of significance calculation/partitioning.
				.setNonRoot("incPartitionAttributeProcessStrategy", incPartitionAttributeProcessStrategy)
				// set parameters for execution of Inspection
				.setNonRoot("inspectAttributeProcessCapacityCalculator", new CapacityCal4SqrtAttrSize());

		// Create a procedure.
		RoughEquivalentClassBasedAlgorithmIncrementalPartitionHeuristicQRTester tester =
				new RoughEquivalentClassBasedAlgorithmIncrementalPartitionHeuristicQRTester(parameters, logOn);

		// Execute
		Collection<Integer> red = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+red);
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));
	}

}
