package featureSelection.tester.procedure.heuristic.liuQuickHash;

import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.tester.procedure.basic.BasicTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;

/**
 * LiuQuickHashHeuristicQRTester Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Liu Quick Hash Heuristic QR Tester Test")
@Slf4j
class LiuQuickHashHeuristicQRTesterTest
	extends BasicTester
{
	private boolean logOn = true;

	@Test
	void testExec() throws Exception {
		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		ProcedureParameters params = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes);

		// Create a procedure.
		LiuQuickHashHeuristicQRTester tester = new LiuQuickHashHeuristicQRTester(params, logOn);
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
