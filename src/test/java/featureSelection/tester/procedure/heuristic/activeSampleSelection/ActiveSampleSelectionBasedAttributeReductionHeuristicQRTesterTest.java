package featureSelection.tester.procedure.heuristic.activeSampleSelection;

import common.utils.ArrayUtils;
import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.entity.alg.activeSampleSelection.incrementalAttributeReductionResult.ASSResult4Incremental;
import featureSelection.repository.support.calculation.positiveRegion.activeSampleSelection.PositiveCalculation4ActiveSampleSelection;
import featureSelection.tester.procedure.basic.SegmentalDataTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * ActiveSampleSelectionBasedAttributeReductionHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Active Sample Selection Based Attribute Reduction Heuristic QR Tester Test")
@Slf4j
class ActiveSampleSelectionBasedAttributeReductionHeuristicQRTesterTest
	extends SegmentalDataTester
{
	private boolean logOn = true;

	@Test
	public void exec() throws Exception{

		/* ==================================================================================== */

		Collection<Instance> allInstances = new LinkedList<>();

		/* ====================================== PART 1 ====================================== */

		List<Instance> execInstances = sampleParts[0];
		allInstances.addAll(execInstances);

		ASSResult4Incremental results = execStaticData(execInstances);

		LoggerUtil.printLine(log, "-", 50);

		/* ====================================== PART X ====================================== */

		Collection<Integer> previousReduct = results.getReduct();
		if (previousReduct.isEmpty())	previousReduct.add(1);	// for empty reduct.

		for (int part=1; part<sampleParts.length; part++) {
			execInstances = sampleParts[part];

			results = execIncrementalData(part, execInstances, allInstances, results);

			allInstances.addAll(execInstances);
		}
	}

	public ASSResult4Incremental execStaticData(Collection<Instance> dataset) throws Exception {
		// |C|
		int attrLength = CollectionUtils.firstOf(dataset).getAttributeValues().length-1;
		// C
		int[] attributes = ArrayUtils.initIncrementalValueIntArray(attrLength, 1, 1);

		ProcedureParameters parameters =
				new ProcedureParameters()
						// U
						.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, dataset)
						// C
						.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
						// set feature (subset) importance calculation class, one of the following:
						//  PositiveCalculation4ActiveSampleSelection
						.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveCalculation4ActiveSampleSelection.class)
						// set significance deviation for calculation:
						//  PositiveCalculation4ActiveSampleSelection: int
						.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		log.info("Part 1");

		SamplePairSelectionBasedAttributeReductionHeuristicQRTester tester1 =
				new SamplePairSelectionBasedAttributeReductionHeuristicQRTester(parameters, true);

		ASSResult4Incremental results = tester1.exec();

		return results;
	}

	public ASSResult4Incremental execIncrementalData(
			int part, Collection<Instance> incrementalInstances, Collection<Instance> allInstances,
			ASSResult4Incremental previousResult
	) throws Exception {
		// |C|
		int attrLength = CollectionUtils.firstOf(incrementalInstances).getAttributeValues().length-1;
		// C
		int[] attributes = ArrayUtils.initIncrementalValueIntArray(attrLength, 1, 1);
		// previous reduct
		Collection<Integer> previousReduct = previousResult.getReduct();

		ProcedureParameters parameters =
				new ProcedureParameters()
						// incremental instances
						.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, incrementalInstances)
						// all instances
						.set(true, ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM, allInstances)
						// C
						.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
						// previous reduct
						.set(true, ParameterConstants.PARAMETER_PREVIOUS_REDUCT, previousReduct)
						// set feature (subset) importance calculation class, one of the following:
						//  PositiveCalculation4ActiveSampleSelection
						.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveCalculation4ActiveSampleSelection.class)
						// set significance deviation for calculation:
						//  PositiveCalculation4ActiveSampleSelection: int
						.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		if (previousResult != null) {
			parameters.set(false, ParameterConstants.PARAMETER_ASE_SAMPLE_PAIR_SELECTION, previousResult);
		}

		log.info("Part {}", part + 1);
		log.info(LoggerUtil.spaceFormat(1, "previous reduct: {}"), previousReduct);

		ActiveSampleSelectionBasedAttributeReductionHeuristicQRTester tester =
				new ActiveSampleSelectionBasedAttributeReductionHeuristicQRTester(parameters, logOn);

		ASSResult4Incremental results = tester.exec();

		previousReduct = results.getReduct();

		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+previousReduct);
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());

		return results;
	}
}