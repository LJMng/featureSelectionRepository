package featureSelection.tester.procedure.heuristic.dependencyCalculation;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.directDependencyCalculation.DependencyCalculation4DDCHash;
import featureSelection.repository.support.calculation.dependency.dependencyCalculation.directDependencyCalculation.DependencyCalculation4DDCSequential;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCHash;
import featureSelection.repository.support.calculation.positiveRegion.dependencyCalculation.directDependencyCalculation.PositiveRegion4DDCSequential;
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
 * DirectDependencyCalculationAlgorithmHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Direct Dependency Calculation Algorithm Heuristic QR Tester Test")
class DirectDependencyCalculationAlgorithmHeuristicQRTesterTest
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
		//  PositiveRegion4DDCHash
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegion4DDCHash.class);
		// set feature (subset) importance calculation class, one of the following:
		//  PositiveRegion4DDCHash: int
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		DirectDependencyCalculationAlgorithmHeuristicQRTester<Integer> tester =
				new DirectDependencyCalculationAlgorithmHeuristicQRTester<>(parameters, logOn);
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
		//  DependencyCalculation4DDCHash
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, DependencyCalculation4DDCHash.class);
		// set feature (subset) importance calculation class, one of the following:
		//  DependencyCalculation4DDCHash: double
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultSigDeviation);

		// Create a procedure.
		DirectDependencyCalculationAlgorithmHeuristicQRTester<Integer> tester =
				new DirectDependencyCalculationAlgorithmHeuristicQRTester<>(parameters, logOn);
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
		//  PositiveRegion4DDCSequential
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegion4DDCSequential.class);
		// set feature (subset) importance calculation class, one of the following:
		//  PositiveRegion4DDCSequential: int
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		DirectDependencyCalculationAlgorithmHeuristicQRTester<Integer> tester =
				new DirectDependencyCalculationAlgorithmHeuristicQRTester<>(parameters, logOn);
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
		//  DependencyCalculation4DDCSequential
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, DependencyCalculation4DDCSequential.class);
		// set feature (subset) importance calculation class, one of the following:
		//  DependencyCalculation4DDCSequential: double
		parameters.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultSigDeviation);

		// Create a procedure.
		DirectDependencyCalculationAlgorithmHeuristicQRTester<Integer> tester =
				new DirectDependencyCalculationAlgorithmHeuristicQRTester<>(parameters, logOn);
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
