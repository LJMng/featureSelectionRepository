package featureSelection.tester.procedure.heuristic.classic.sequential; 

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.support.calculation.positiveRegion.classic.PositiveRegionCalculation4ClassicSequential;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.Collection;

/** 
* ClassicAttributeReductionSequentialAlgorithmHeuristicQRTester Tester. 
* 
* @author Benjamin_L
*/
@Slf4j
public class ClassicAttributeReductionSequentialAlgorithmHeuristicQRTesterTest
	extends BasicTester
{
	private boolean execCore = false;
	private boolean logOn = true;

	@Test
	public void exec() throws Exception{
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// Load parameters.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// execute core ?
				.set(true, ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveRegionCalculation4ClassicSequential
				//  DependencyCalculation4ClassicSequential
				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveRegionCalculation4ClassicSequential.class)
				// set significance deviation for calculation:
				//  PositiveRegionCalculation4ClassicSequential: int
				//  DependencyCalculation4ClassicSequential: double
				.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);

		// Create a procedure.
		ClassicAttributeReductionSequentialAlgorithmHeuristicQRTester<Integer> tester =
				new ClassicAttributeReductionSequentialAlgorithmHeuristicQRTester<>(parameters, logOn);
		// Execute
		Collection<Integer> red = tester.exec();

		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+red);
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ProcedureUtils.Statistics.combineProcedureStatics(tester));

	}


} 
