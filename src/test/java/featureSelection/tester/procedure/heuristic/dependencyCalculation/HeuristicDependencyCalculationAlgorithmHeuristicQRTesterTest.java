package featureSelection.tester.procedure.heuristic.dependencyCalculation;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.heuristicDependencyCalculation.DependencyCalculation4HDCHash;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.heuristicDependencyCalculation.DependencyCalculation4HDCSequential;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.heuristicDependencyCalculation.PositiveRegion4HDCHash;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.heuristicDependencyCalculation.PositiveRegion4HDCSequential;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Collection;

/**
 * HeuristicDependencyCalculationAlgorithmHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Heuristic Dependency Calculation Algorithm Heuristic QR Tester Test")
class HeuristicDependencyCalculationAlgorithmHeuristicQRTesterTest
	extends BasicTester
{
	private boolean execCore = true;
	private boolean logOn = true;
	private double defaultSigDeviation = 1E-13;

	@Test
	public void execPositiveRegionHash() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = getCommonParameters(execCore);
		// set feature (subset) importance calculation class, one of the following:
		//  PositiveRegion4HDCHash
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegion4HDCHash.class);
		// set feature (subset) importance calculation class, one of the following:
		//  PositiveRegion4HDCHash: int
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		HeuristicDependencyCalculationAlgorithmHeuristicQRTester<Integer> tester =
				new HeuristicDependencyCalculationAlgorithmHeuristicQRTester<>(parameters, logOn);
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

	@Test
	public void execDependencyHash() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = getCommonParameters(execCore);
		// set feature (subset) importance calculation class, one of the following:
		//  DependencyCalculation4HDCHash
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, DependencyCalculation4HDCHash.class);
		// set feature (subset) importance calculation class, one of the following:
		//  DependencyCalculation4HDCHash: double
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultSigDeviation);

		// Create a procedure.
		HeuristicDependencyCalculationAlgorithmHeuristicQRTester<Integer> tester =
				new HeuristicDependencyCalculationAlgorithmHeuristicQRTester<>(parameters, logOn);
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

	@Test
	public void execPositiveRegionSequential() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = getCommonParameters(execCore);
		// set feature (subset) importance calculation class, one of the following:
		//  PositiveRegion4HDCSequential
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegion4HDCSequential.class);
		// set feature (subset) importance calculation class, one of the following:
		//  PositiveRegion4HDCSequential: int
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		HeuristicDependencyCalculationAlgorithmHeuristicQRTester<Integer> tester =
				new HeuristicDependencyCalculationAlgorithmHeuristicQRTester<>(parameters, logOn);
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

	@Test
	public void execDependencySequential() throws Exception {
		// Load parameters.
		ProcedureParameters parameters = getCommonParameters(execCore);
		// set feature (subset) importance calculation class, one of the following:
		//  DependencyCalculation4HDCSequential
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, DependencyCalculation4HDCSequential.class);
		// set feature (subset) importance calculation class, one of the following:
		//  DependencyCalculation4HDCSequential: double
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultSigDeviation);

		// Create a procedure.
		HeuristicDependencyCalculationAlgorithmHeuristicQRTester<Integer> tester =
				new HeuristicDependencyCalculationAlgorithmHeuristicQRTester<>(parameters, logOn);
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


	private static ProcedureParameters getCommonParameters(boolean execCore){
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// Load parameters.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.setNonRoot(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.setNonRoot(ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// execute core ?
				.setNonRoot(ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore);
		return parameters;
	}
}
