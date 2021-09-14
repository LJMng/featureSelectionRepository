package featureSelection.tester.procedure.heuristic.xieDynamicIncompleteDSReduction;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.algorithm.alg.xieDynamicIncompleteDSReduction.DynamicIncompleteDecisionSystemReductionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainerByDirectCollectNCache;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateInfoPack;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.DynamicUpdateStrategy;
import featureSelection.repository.entity.alg.xieDynamicIncompleteDSReduction.PreviousInfoPack;
import featureSelection.repository.support.calculation.inConsistency.xieDynamicIncompleteDSReduction.InConsistencyCalculation4DIDSRFixed;
import featureSelection.repository.support.calculation.inConsistency.xieDynamicIncompleteDSReduction.InConsistencyCalculation4DIDSROriginal;
import featureSelection.tester.procedure.basic.IncompleteDataTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * XieDynamicIncompleteDSReductionHeuristicQRTester4DynamicData Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Xie Dynamic Incomplete DS Reduction Heuristic QR Tester 4 Dynamic Data Test")
class XieDynamicIncompleteDSReductionHeuristicQRTester4DynamicDataTest
	extends IncompleteDataTester
{
	private boolean logOn = true;
	private boolean execCore = true;

	@Test
	void testObjectRelatedUpdate() throws Exception {
		testUpdate(DynamicUpdateStrategy.OBJECT_RELATED);
	}

	@Test
	void testAttributeRelatedUpdate() throws Exception {
		testUpdate(DynamicUpdateStrategy.ATTRIBUTE_RELATED);
	}

	@Test
	void testBothRelatedUpdate() throws Exception {
		testUpdate(DynamicUpdateStrategy.BOTH_RELATED);
	}


	<Sig extends Number> void testUpdate(DynamicUpdateStrategy updateStrategy) throws Exception {
		// ------------------------------ Preparations ------------------------------

		// Procedure for static data processing
		XieDynamicIncompleteDSReductionHeuristicQRTester4StaticData<Sig> staticTester;
		// Procedure for varied data processing
		XieDynamicIncompleteDSReductionHeuristicQRTester4DynamicData<Sig> dynamicTester;

		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// --------------------------------------------------------------------------

		// Load parameters.
		ProcedureParameters staticParams = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// execute Core ?
				.set(true, ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
				// set feature (subset) importance calculation class, one of the following:
				//  InConsistencyCalculation4DIDSROriginal
				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, InConsistencyCalculation4DIDSROriginal.class)
				// set significance deviation for calculation
				.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0)
				// set tolerance class obtainer class, one of the following:
				//  ToleranceClassObtainerOriginal
				//  ToleranceClassObtainerOriginalNCache
				//  ToleranceClassObtainerOriginalUsingCollapse
				//  ToleranceClassObtainerByDirectCollect
				//  ToleranceClassObtainerByDirectCollectNCache
				.set(true, "toleranceClassObtainerClass", ToleranceClassObtainerByDirectCollectNCache.class);

		// Create a procedure for static data processing.
		staticTester = new XieDynamicIncompleteDSReductionHeuristicQRTester4StaticData<>(staticParams, logOn);

		System.out.println("Process static data...");

		// Execute
		Collection<Integer> previousReduct = staticTester.exec();
		// print results.
		System.out.println("result : "+previousReduct);
		System.out.println();

		// --------------------------------------------------------------------------

		// Prepare previous info.s
		ToleranceClassObtainer toleranceClassObtainer =
				new ToleranceClassObtainerByDirectCollectNCache();

		// Obtain the tolerance classes of conditional attributes
		Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs =
				DynamicIncompleteDecisionSystemReductionAlgorithm
						.Basic
						.toleranceClass(
								instances, new IntegerArrayIterator(attributes),
								toleranceClassObtainer,
								toleranceClassObtainer.getCacheInstanceGroups(
										instances, new IntegerArrayIterator(attributes)
								)
						);
		// Obtain tolerance classes of conditional and decision attributes
		Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttr =
				DynamicIncompleteDecisionSystemReductionAlgorithm
						.Basic
						.toleranceClassConsideringDecisionValue(previousTolerancesOfCondAttrs);
		// Obtain tolerance classes of previous reduct
		Map<Instance, Collection<Instance>> previousTolerancesOfReduct =
				DynamicIncompleteDecisionSystemReductionAlgorithm
						.Basic
						.toleranceClass(
								instances, new IntegerCollectionIterator(previousReduct),
								toleranceClassObtainer,
								toleranceClassObtainer.getCacheInstanceGroups(
										instances, new IntegerCollectionIterator(previousReduct)
								)
						);
		// Obtain tolerance classes of previous reduct and decision attribute
		Map<Instance, Collection<Instance>> previousTolerancesOfReductNDecAttr =
				DynamicIncompleteDecisionSystemReductionAlgorithm
						.Basic
						.toleranceClassConsideringDecisionValue(previousTolerancesOfReduct);

		// -------------------------------- Vary data --------------------------------

		// Do some data varying.
		System.out.println("Vary data");
		DynamicUpdateInfoPack updateInfo;

		switch (updateStrategy){
			case OBJECT_RELATED:
				updateInfo = varyObjects(
						previousTolerancesOfCondAttrs, previousTolerancesOfCondAttrsNDecAttr,
						previousReduct, previousTolerancesOfReduct,
						previousTolerancesOfReductNDecAttr
				);
				break;
			case ATTRIBUTE_RELATED:
				updateInfo = varyAttribute(
						previousTolerancesOfCondAttrs, previousTolerancesOfCondAttrsNDecAttr,
						previousReduct, previousTolerancesOfReduct,
						previousTolerancesOfReductNDecAttr
				);
				break;
			case BOTH_RELATED:
				updateInfo = varyBothObjectNAttribute(
						previousTolerancesOfCondAttrs, previousTolerancesOfCondAttrsNDecAttr,
						previousReduct, previousTolerancesOfReduct,
						previousTolerancesOfReductNDecAttr
				);
				break;
			default:
				throw new IllegalStateException("Un-handled case: "+updateStrategy);
		}


		// ----------------------- Perform xxx-vary algorithm -----------------------

		System.out.println(updateStrategy.name()+": (data varied)");

		ProcedureParameters dynamicParams = new ProcedureParameters()
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// set feature (subset) importance calculation class, one of the following:
				//  InConsistencyCalculation4DIDSRDefault
				//  InConsistencyCalculation4DIDSROriginal
				//  InConsistencyCalculation4DIDSRFixed
				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, InConsistencyCalculation4DIDSRFixed.class)
				// set significance deviation for calculation
				.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0)
				// set tolerance class obtainer class, one of the following:
				//  ToleranceClassObtainerOriginal
				//  ToleranceClassObtainerOriginalNCache
				//  ToleranceClassObtainerOriginalUsingCollapse
				//  ToleranceClassObtainerByDirectCollect
				//  ToleranceClassObtainerByDirectCollectNCache
				.set(true, "toleranceClassObtainerClass", ToleranceClassObtainerByDirectCollectNCache.class)
				// Update strategy: object related update
				.set(true, "updateStrategy", updateStrategy)
				// Set update info.
				.set(true, "updateInfo", updateInfo);

		switch (updateStrategy){
			case OBJECT_RELATED:
				// U
				dynamicParams.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, updateInfo.getAlteredObjectAppliedInstances());
				break;
			case ATTRIBUTE_RELATED:
				// U
				dynamicParams.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, updateInfo.getAlteredAttrValAppliedInstances());
				break;
			case BOTH_RELATED:
				// U
				dynamicParams.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, updateInfo.getAlteredMixAppliedInstances());
				break;
			default:
				throw new IllegalStateException("Un-handled case: "+updateStrategy);
		}

		// Create a procedure.
		dynamicTester = new XieDynamicIncompleteDSReductionHeuristicQRTester4DynamicData<>(dynamicParams, logOn);

		// Execute
		Collection<Integer> red = dynamicTester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+red);
		System.out.println("total time : "+dynamicTester.getTime());
		System.out.println("tag time : "+dynamicTester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(dynamicTester));
	}

	DynamicUpdateInfoPack varyObjects(
			Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs,
			Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttr,
			Collection<Integer> previousReduct,
			Map<Instance, Collection<Instance>> previousTolerancesOfReduct,
			Map<Instance, Collection<Instance>> previousTolerancesOfReductNDecAttr
	){
		DynamicUpdateInfoPack updateInfo = new DynamicUpdateInfoPack(
				DynamicUpdateStrategy.OBJECT_RELATED,
				new PreviousInfoPack(
						instances,
						previousTolerancesOfCondAttrs, previousTolerancesOfCondAttrsNDecAttr,
						previousReduct,
						previousTolerancesOfReduct, previousTolerancesOfReductNDecAttr
				)
		);
		updateInfo.alter(instances.get(2), 3, 2);
		return updateInfo;
	}

	DynamicUpdateInfoPack varyAttribute(
			Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs,
			Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttr,
			Collection<Integer> previousReduct,
			Map<Instance, Collection<Instance>> previousTolerancesOfReduct,
			Map<Instance, Collection<Instance>> previousTolerancesOfReductNDecAttr
	){
		DynamicUpdateInfoPack updateInfo = new DynamicUpdateInfoPack(
				DynamicUpdateStrategy.ATTRIBUTE_RELATED,
				new PreviousInfoPack(
						instances,
						previousTolerancesOfCondAttrs, previousTolerancesOfCondAttrsNDecAttr,
						previousReduct,
						previousTolerancesOfReduct, previousTolerancesOfReductNDecAttr
				)
		);
		updateInfo.alter(instances.get(2), 3, 2);
		updateInfo.alter(instances.get(3), 2, 1);

		// select some un-altered attributes for DIDS.
		Collection<Integer> selectedAttrInReduct = new ArrayList<>(1);
		for (int a: previousReduct)	{
			if (!updateInfo.getAlteredAttributeValues().attributeValueIsAltered(a)) {
				selectedAttrInReduct.add(a);
			}
		}
		if (selectedAttrInReduct.isEmpty()) {
			throw new IllegalStateException("All attributes in reduct altered!");
		}
		updateInfo.setSelectednUnalteredAttribute4AlteredAttributeValues(selectedAttrInReduct);

		return updateInfo;
	}

	DynamicUpdateInfoPack varyBothObjectNAttribute(
			Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrs,
			Map<Instance, Collection<Instance>> previousTolerancesOfCondAttrsNDecAttr,
			Collection<Integer> previousReduct,
			Map<Instance, Collection<Instance>> previousTolerancesOfReduct,
			Map<Instance, Collection<Instance>> previousTolerancesOfReductNDecAttr
	){
		DynamicUpdateInfoPack updateInfo = new DynamicUpdateInfoPack(
				DynamicUpdateStrategy.BOTH_RELATED,
				new PreviousInfoPack(
						instances,
						previousTolerancesOfCondAttrs, previousTolerancesOfCondAttrsNDecAttr,
						previousReduct,
						previousTolerancesOfReduct, previousTolerancesOfReductNDecAttr
				)
		);
		updateInfo.alter(instances.get(2), 3, 2);
		updateInfo.alter(instances.get(2), 3, 2);
		updateInfo.alter(instances.get(3), 2, 1);

		return updateInfo;
	}
}
