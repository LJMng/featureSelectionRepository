package featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental;

import featureSelection.basic.model.universe.instance.IncompleteInstance;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainerByDirectCollectNCache;
import featureSelection.repository.support.calculation.positiveRegion.toleranceClassPositiveRegionIncremental.PositiveCalculation4TCPR;
import featureSelection.tester.procedure.basic.IncompleteDataTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * ToleranceClassPositiveRegionIncrementalHeuristicQRTester4VaryObject Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Tolerance Class Positive Region Incremental Heuristic QR Tester 4 Vary Object Test")
class ToleranceClassPositiveRegionIncrementalHeuristicQRTester4VaryObjectTest
	extends IncompleteDataTester
{
	private boolean logOn = true;

	@Test
	void testVaryData() throws Exception {
		// ------------------------------ Preparations ------------------------------

		// Procedure for static data processing
		ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticData staticTester;
		// Procedure for varied data processing
		ToleranceClassPositiveRegionIncrementalHeuristicQRTester4VaryObject dynamicTester;

		// obtain attributes = {1, 2, ..., C}
		int[] attributes = getAllConditionalAttributes();

		// --------------------------------------------------------------------------

		// Load parameters.
		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, instances)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// set feature (subset) importance calculation class, one of the following:
				//  PositiveCalculation4TCPR
				.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveCalculation4TCPR.class)
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
		staticTester = new ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticData(parameters, logOn);

		System.out.println("Process static data...");

		// Execute
		Collection<Integer> previousReduct = staticTester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+previousReduct);
		System.out.println("total time : "+staticTester.getTime());
		System.out.println("tag time : "+staticTester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(staticTester));
		System.out.println();

		// -------------------------------- Vary data --------------------------------

		// Do some data varying.
		System.out.println("Vary data");
		VaryInfo varyInfo = varyAnInstance();

		// --------------------- Perform static-data algorithm ----------------------

		Collection<Integer> reduct;
		System.out.println("Static algorithm: (data varied)");

		// U
		parameters.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, varyInfo.newInstances);
		// C
		parameters.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes);
		// set feature (subset) importance calculation class, one of the following:
		//  PositiveCalculation4TCPR
		parameters.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveCalculation4TCPR.class);
		// set significance deviation for calculation
		parameters.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);
		parameters.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveCalculation4TCPR.class);
		// set tolerance class obtainer class, one of the following:
		//  ToleranceClassObtainerOriginal
		//  ToleranceClassObtainerOriginalNCache
		//  ToleranceClassObtainerOriginalUsingCollapse
		//  ToleranceClassObtainerByDirectCollect
		//  ToleranceClassObtainerByDirectCollectNCache
		parameters.set(true, "toleranceClassObtainerClass", ToleranceClassObtainerByDirectCollectNCache.class);

		// Create a procedure for data processing.
		staticTester = new ToleranceClassPositiveRegionIncrementalHeuristicQRTester4StaticData(parameters, logOn);

		// Execute
		reduct = staticTester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+reduct);
		System.out.println("total time : "+staticTester.getTime());
		System.out.println("tag time : "+staticTester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(staticTester));
		System.out.println();

		// --------------------- Perform object-vary algorithm ----------------------

		System.out.println("Object-vary algorithm: (data varied)");
		// U
		parameters.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, varyInfo.newInstances);
		// C
		parameters.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes);
		// set feature (subset) importance calculation class, one of the following:
		//  PositiveCalculation4TCPR
		parameters.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, PositiveCalculation4TCPR.class);
		// set significance deviation for calculation
		parameters.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, 0);
		// set tolerance class obtainer class, one of the following:
		//  ToleranceClassObtainerOriginal
		//  ToleranceClassObtainerOriginalNCache
		//  ToleranceClassObtainerOriginalUsingCollapse
		//  ToleranceClassObtainerByDirectCollect
		//  ToleranceClassObtainerByDirectCollectNCache
		parameters.set(true, "toleranceClassObtainerClass", ToleranceClassObtainerByDirectCollectNCache.class);
		// set varied info.
		parameters.set(true, "changedFromInstances", Arrays.asList(varyInfo.fromInstance));
		parameters.set(true, "changedToInstances", Arrays.asList(varyInfo.toInstance));
		// set previous reduct
		parameters.set(true, ParameterConstants.PARAMETER_PREVIOUS_REDUCT, previousReduct);

		// Create a procedure for varied data processing.
		dynamicTester = new ToleranceClassPositiveRegionIncrementalHeuristicQRTester4VaryObject(parameters, logOn);

		// Execute
		reduct = dynamicTester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+reduct);
		System.out.println("total time : "+staticTester.getTime());
		System.out.println("tag time : "+staticTester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(staticTester));
		System.out.println();
	}

	VaryInfo varyAnInstance(){
		// U[2] => a1.val=>1, a3.val=>1, a4.val=>1

		int attrLen = getAttributeLength();
		Instance oldIns = instances.get(2);

		// Copy (conditional & decision) attribute values of U[2].
		int[] newAttrValue = Arrays.copyOf(oldIns.getAttributeValues(), attrLen+1);
		// Vary data
		newAttrValue[1] = 1;
		newAttrValue[3] = 1;
		newAttrValue[4] = 1;

		// Use an instance to contain the varied data
		Instance newIns = new IncompleteInstance(newAttrValue);

		List<Instance> newInstances = new ArrayList<>(instances);
		newInstances.remove(oldIns);
		newInstances.add(newIns);

		System.out.println("Vary from: "+oldIns);
		System.out.println("Vary To  : "+newIns);
		System.out.println();

		return new VaryInfo(newInstances, oldIns, newIns);
	}

	@Getter
	@AllArgsConstructor
	static class VaryInfo{
		List<Instance> newInstances;
		Instance fromInstance;
		Instance toInstance;
	}
}
