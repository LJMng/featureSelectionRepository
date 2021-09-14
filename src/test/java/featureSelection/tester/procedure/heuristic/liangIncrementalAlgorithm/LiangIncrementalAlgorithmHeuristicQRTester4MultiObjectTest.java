package featureSelection.tester.procedure.heuristic.liangIncrementalAlgorithm;

import common.utils.ArrayUtils;
import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiangIncremental;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.liangIncrementalAlgorithm.CCECalculation4LiangIncrementalAlgorithm;
import featureSelection.repository.support.calculation.entropy.combinationConditionEntropy.positiveApproximationAccelerator.CCECalculation4ACCOriginal;
import featureSelection.repository.support.calculation.entropy.liangConditionEntropy.liangIncrementalAlgorithm.LCECalculation4LiangIncrementalAlgorithm;
import featureSelection.repository.support.calculation.entropy.liangConditionEntropy.positiveApproximationAccelerator.LCECalculation4ACCOriginal;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.liangIncrementalAlgorithm.SCECalculation4LiangIncrementalAlgorithm;
import featureSelection.repository.support.calculation.entropy.shannonConditionEnpropy.positiveApproximationAccelerator.SCECalculation4ACCOriginal;
import featureSelection.repository.support.calculation.positiveRegion.activeSampleSelection.PositiveCalculation4ActiveSampleSelection;
import featureSelection.tester.procedure.basic.SegmentalDataTester;
import featureSelection.tester.procedure.heuristic.positiveApproximationAccelerator.original.PositiveApproximationAcceleratorAlgorithmHeuristicQRTester;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * LiangIncrementalAlgorithmHeuristicQRTester4MultiObject Tester.
 *
 * @author Benjamin_L
 */
@DisplayName("Liang Incremental Algorithm Heuristic QR Tester 4 Multi Object Test")
@Slf4j
class LiangIncrementalAlgorithmHeuristicQRTester4MultiObjectTest
	extends SegmentalDataTester
{
	private boolean execCore = true;
	private boolean logOn = true;
	private double defaultSigDeviation = 1E-13;

	@Test
	public void testSCE() throws Exception {
		commonTest(
				SCECalculation4ACCOriginal.class,
				SCECalculation4LiangIncrementalAlgorithm.class
		);
	}

	@Test
	public void testLCE() throws Exception {
		commonTest(
				LCECalculation4ACCOriginal.class,
				LCECalculation4LiangIncrementalAlgorithm.class
		);
	}

	@Test
	public void testCCE() throws Exception {
		commonTest(
				CCECalculation4ACCOriginal.class,
				CCECalculation4LiangIncrementalAlgorithm.class
		);
	}

	private void commonTest(
			Class<? extends PositiveApproximationAcceleratorCalculation> staticDataCalculationClass,
			Class<? extends FeatureImportance4LiangIncremental> incDataCalculationClass
	) throws Exception {
		/* ==================================================================================== */

		Collection<Instance> allInstances = new LinkedList<>();

		/* ====================================== PART 1 ====================================== */

		List<Instance> execInstances = sampleParts[0];
		allInstances.addAll(execInstances);

		ExecuteResult result = execStaticData(execInstances, staticDataCalculationClass);

		LoggerUtil.printLine(log, "-", 50);

		/* ====================================== PART X ====================================== */

		Collection<Integer> previousReduct = result.getReduct();
		if (previousReduct.isEmpty())	previousReduct.add(1);	// for empty reduct.

		for (int part=1; part<sampleParts.length; part++) {
			execInstances = sampleParts[part];

			result = execIncrementalData(
					part, execInstances, allInstances, result,
					staticDataCalculationClass,
					incDataCalculationClass
			);

			allInstances.addAll(execInstances);
		}
	}

	private ExecuteResult execStaticData(
			Collection<Instance> dataset,
			Class<? extends PositiveApproximationAcceleratorCalculation> calculationClass
	) throws Exception {
		// |C|
		int attrLength = CollectionUtils.firstOf(dataset).getAttributeValues().length-1;
		// C
		int[] attributes = ArrayUtils.initIncrementalValueIntArray(attrLength, 1, 1);

		ProcedureParameters parameters = new ProcedureParameters()
				// U
				.set(true, ParameterConstants.PARAMETER_UNIVERSE_INSTANCES, dataset)
				// C
				.set(true, ParameterConstants.PARAMETER_ATTRIBUTES, attributes)
				// execute Core ?
				.setNonRoot(ParameterConstants.PARAMETER_QR_EXEC_CORE, execCore)
				// set feature (subset) importance calculation class, one of the following:
				//  SCECalculation4ACCOriginal
				.setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, calculationClass)
				// set significance deviation for calculation:
				//  PositiveCalculation4ActiveSampleSelection: double
				.setNonRoot(ParameterConstants.PARAMETER_SIG_DEVIATION, defaultSigDeviation);

		log.info("Part 1");

		// Create a procedure.
		PositiveApproximationAcceleratorAlgorithmHeuristicQRTester<Double> tester =
				new PositiveApproximationAcceleratorAlgorithmHeuristicQRTester<>(parameters, logOn);
		// Execute
		Collection<Integer> red = tester.exec();
		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+red);
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());
		System.out.println("statistics : "+ ProcedureUtils.Statistics.combineProcedureStatics(tester));

		// Save result.
		double previousSig =
				(double) calculationClass.newInstance()
							.calculate(
									PositiveApproximationAcceleratorOriginalAlgorithm
											.Basic
											.equivalenceClass(dataset, new IntegerCollectionIterator(red)),
									red.size(),
									// |U| for CCE
									dataset.size()
							).getResult();

		return new ExecuteResult(previousSig, red);
	}

	private ExecuteResult execIncrementalData(
			int part, Collection<Instance> incrementalInstances, Collection<Instance> allInstances,
			ExecuteResult previousResult,
			Class<? extends PositiveApproximationAcceleratorCalculation> staticDataCalculationClass,
			Class<? extends FeatureImportance4LiangIncremental> incDataCalculationClass
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
						// significance of the previous reduct
						.set(true, ParameterConstants.PARAMETER_PREVIOUS_REDUCT_SIG, previousResult.getSignificance())
						// set feature (subset) importance calculation class, one of the following:
						//  SCECalculation4LiangIncrementalAlgorithm
						.set(true, ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS, incDataCalculationClass)
						// set feature (subset) importance calculation class used in previous execution,
						// one of the following:
						//  SCECalculation4ACCOriginal
						.set(true, "staticCalculationClass", staticDataCalculationClass)
						// set significance deviation for calculation:
						//  SCECalculation4LiangIncrementalAlgorithm: double
						.set(true, ParameterConstants.PARAMETER_SIG_DEVIATION, defaultSigDeviation)
						// set whether the previous sig is calculated with denominator.
						.set(true, "previousSigWithDenominator", false);

		log.info("Part {}", part + 1);
		log.info(LoggerUtil.spaceFormat(1, "previous reduct: {}"), previousReduct);

		// Create a procedure.
		LiangIncrementalAlgorithmHeuristicQRTester4MultiObject<Double> tester =
				new LiangIncrementalAlgorithmHeuristicQRTester4MultiObject<>(parameters, logOn);
		// Execute
		Collection<Integer> reduct = tester.exec();

		// display all statistics
//		ProcedureUtils.Statistics.displayAll(log, tester);
		// print results.
		System.out.println("result : "+reduct);
		System.out.println("total time : "+tester.getTime());
		System.out.println("tag time : "+tester.getTimeDetailByTags());

		// Save result.
		Collection<Instance> realAllInstance = new ArrayList<>(allInstances.size()+incrementalInstances.size());
		realAllInstance.addAll(allInstances);
		realAllInstance.addAll(incrementalInstances);

		double sigOfReduct =
				(double) staticDataCalculationClass.newInstance()
							.calculate(
									PositiveApproximationAcceleratorOriginalAlgorithm
											.Basic
											.equivalenceClass(realAllInstance, new IntegerCollectionIterator(reduct)),
									reduct.size(),
									// |U| for CCE
									realAllInstance.size()
							).getResult();

		return new ExecuteResult(sigOfReduct, reduct);
	}

	@Data
	@AllArgsConstructor
	public static class ExecuteResult{
		double significance;
		Collection<Integer> reduct;
	}
}
