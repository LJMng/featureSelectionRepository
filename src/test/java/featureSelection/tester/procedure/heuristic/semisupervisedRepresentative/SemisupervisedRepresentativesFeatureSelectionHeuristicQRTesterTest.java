package featureSelection.tester.procedure.heuristic.semisupervisedRepresentative;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.entity.alg.semisupervisedRepresentative.calculationPack.SemisupervisedRepresentativeCalculations4EntropyBased;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Collection;

/**
 * SemisupervisedRepresentativesFeatureSelectionHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Semi-supervised Representatives Feature Selection Heuristic QR Tester Test")
class SemisupervisedRepresentativesFeatureSelectionHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;

	@Test
	void testExec() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// Load parameters.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// labeled U
				.setNonRoot(ParameterConstants.PARAMETER_LABELED_UNIVERSE_INSTANCES, instances)
				// un-labeled U
				.setNonRoot(ParameterConstants.PARAMETER_UNLABELED_UNIVERSE_INSTANCES, new ArrayList<>(0))
				// C
				.setNonRoot(ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// &alpha (optional)
				.setNonRoot("featureRelevantThreshold", 0.0)
				// &beta
				.setNonRoot("tradeOff", 0.0)
				// set feature (subset) importance calculation class, one of the following:
				//  SemisupervisedRepresentativeCalculations4EntropyBased
				.setNonRoot(
						ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS,
						SemisupervisedRepresentativeCalculations4EntropyBased.class
				);

		// Create a procedure.
		SemisupervisedRepresentativesFeatureSelectionHeuristicQRTester tester =
				new SemisupervisedRepresentativesFeatureSelectionHeuristicQRTester(parameters, logOn);

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
