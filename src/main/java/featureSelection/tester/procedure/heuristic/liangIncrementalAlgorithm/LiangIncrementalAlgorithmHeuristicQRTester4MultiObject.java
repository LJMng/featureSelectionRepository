package featureSelection.tester.procedure.heuristic.liangIncrementalAlgorithm;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import common.utils.CollectionUtils;
import common.utils.LoggerUtil;
import featureSelection.basic.annotation.theory.RoughSet;
import featureSelection.basic.annotation.thread.ThreadSafetyNotSecured;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.ProcedureContainer;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimeSum;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.basic.support.alg.LiangIncrementalAlgorithmStrategy;
import featureSelection.basic.support.calculation.Calculation;
import featureSelection.basic.support.reductMiningStrategy.heuristic.QuickReductHeuristicReductStrategy;
import featureSelection.basic.support.searchStrategy.HashSearchStrategy;
import featureSelection.repository.algorithm.alg.liangIncrementalAlgorithm.LiangIncrementalAlgorithm;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.MixedEquivalenceClassSequentialList;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiangIncremental;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.liangIncrementalAlgorithm.procedure.ReductInspectionProcedureContainer4MultiObjectIncremental;
import featureSelection.tester.procedure.heuristic.liangIncrementalAlgorithm.procedure.SignificantAttributeSeekingLoopProcedureContainer4MultiObjectIncremental;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.statistics.info.execInstance.BasicExecutionInstanceInfo;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Tester Procedure for <strong>Liang incremental entropy Calculation</strong> based Incremental
 * Feature Selection.
 * <p>
 * Original paper: <a href="https://ieeexplore.ieee.org/document/6247431/">"A Group Incremental
 * Approach to Feature Selection Applying Rough Set Technique"</a> by Jiye Liang, Feng Wang,
 * Chuangyin Dang, Yuhua Qian.
 * <p>
 * This is a {@link DefaultProcedureContainer}. Procedure contains 6 {@link ProcedureComponent}s,
 * refer to steps:
 * <ul>
 * 	<li>
 * 		<strong>Procedure Controller</strong>: 
 * 		<p>Control the proceedings of the Liang Group Incremental entropy calculation based
 * 			Feature Selection.
 * 	</li>
 * 	<li>
 * 		<strong>Initiate</strong>: 
 * 		<p>Initiate fields for later usage: <i>B <- RED<sub>U</sub></i>, <i>U/B</i>,
 * 			<i>U/C</i>, <i>U<sub>X</sub>/B</i>, <i>U<sub>X</sub>/C</i>;
 * 			compute <i>(U∪U<sub>X</sub>)/B</i>, <i>(U∪U<sub>X</sub>)/C</i>.
 * 	</li>
 * 	<li>
 * 		<strong>Check if able to exit</strong>: 
 * 		<p>Check if current situation needs further reduct updating by checking if 
 * 			both (U∪U<sub>X</sub>)/B and (U∪U<sub>X</sub>)/C contains no mixed equivalence
 * 			class(i.e. equivalence classes that contains previous and new {@link Instance})
 * 			&& ME<sub>U<sub>X</sub></sub>(D|B) == ME<sub>U<sub>X</sub></sub>(D|C)
 * 	</li>
 * 	<li>
 * 		<strong>Prepare for sig loop</strong>: 
 * 		<p>Preparations for significant attribute searching loop. Initiate fields:
 * 			<i>U/D</i>, <i>U<sub>X</sub>/D</i>, <i>U∪U<sub>X</sub>/D</i>,
 * 			<i>ME<sub>U</sub>(D|B)</i>, <i>ME<sub>U∪U<sub>X</sub></sub>(D|B)</i>,
 * 			<i>ME<sub>U∪U<sub>X</sub></sub>(D|C)</i>.
 * 	</li>
 * 	<li>
 * 		<strong>Sig loop</strong>: 
 * 		<p>Loop and search for significant attributes as reduct attributes until exit.
 * 		<p><code>SignificantAttributeSeekingLoopProcedureContainer4MultiObjectIncremental</code>
 * 	</li>
 * 	<li>
 * 		<strong>Inspection</strong>: 
 * 		<p>Inspect the reduct and remove redundant attributes.
 * 		<p><code>ReductInspectionProcedureContainer4MultiObjectIncremental</code>
 * 	</li>
 * </ul>
 * <p>
 * In this {@link ProcedureContainer}(only!), the following basic parameters are used in
 * {@link #getParameters()}:
 * <ul>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_COLLECTION_ITEM}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_CLASS}</li>
 * </ul>
 * <p>
 * <strong>Extra parameters</strong>(preset) besides above standard parameters(e.g. attributes, etc.).
 * <ul>
 * 	<li><strong>staticCalculationClass</strong>: {@link Calculation} {@link Class}
 * 		<p>Class of static {@link Calculation} for some static data calculations.
 * 	</li>
 * 	<li><strong>previousSigWithDenominator</strong>: <code>boolean</code>
 * 		<p>Whether the previous significance of reduct are calculated with denominator or not.
 * 	</li>
 * </ul>
 *
 * @param <Sig>
 *     Type of feature (subset) significance.
 * 
 * @see SignificantAttributeSeekingLoopProcedureContainer4MultiObjectIncremental
 * @see ReductInspectionProcedureContainer4MultiObjectIncremental
 * 
 * @author Benjamin_L
 */
@Slf4j
@RoughSet
@ThreadSafetyNotSecured
public class LiangIncrementalAlgorithmHeuristicQRTester4MultiObject<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements TimeSum,
				ReportMapGenerated<String, Map<String, Object>>,
				HashSearchStrategy,
				StatisticsCalculated,
				LiangIncrementalAlgorithmStrategy,
				QuickReductHeuristicReductStrategy
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	
	public LiangIncrementalAlgorithmHeuristicQRTester4MultiObject(
			ProcedureParameters paramaters, boolean logOn
	) {
		super(logOn? log: null, paramaters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
	}
	
	@Override
	public String shortName() {
		return "QR-Liang-GIARC"+"("+ ProcedureUtils.ShortName.calculation(getParameters())+")";
	}
	
	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Procedure Controller.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("1. "+component.getDescription());
						}
					}, 
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						ProcedureComponent<Object[]> comp2 = (ProcedureComponent<Object[]>) getComponents().get(2);
						ProcedureComponent<Object[]> comp3 = (ProcedureComponent<Object[]>) getComponents().get(3);
						ProcedureComponent<?> comp4 = getComponents().get(4);
						ProcedureComponent<?> comp5 = getComponents().get(5);
						
						comp1.exec();
						if (!(Boolean) comp2.exec()[0]) {
							comp3.exec();
							comp4.exec();
							comp5.exec();
						}
						return getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION);
					},
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								reduct
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Procedure Controller"),
			// 2. Initiate.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("2. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_CLASS),
								getParameters().get("staticCalculationClass"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> previousInstances =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> newInstances =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						Class<? extends Calculation<?>> calculationClass =
								(Class<? extends Calculation<?>>) parameters[p++];
						Class<? extends Calculation<?>> staticCalculationClass =
								(Class<? extends Calculation<?>>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						//	B <- RED<sub>U</sub>
						Collection<Integer> newReduct = new HashSet<>(attributes.length);
						newReduct.addAll(previousReduct);
						//	Compute U/B, U/C, U<sub>X</sub>/B, U<sub>X</sub>/C
						Map<IntArrayKey, Collection<Instance>> redEquClassesOfPrevious;
						Map<IntArrayKey, Collection<Instance>> globalEquClassesOfPrevious;
						Map<IntArrayKey, Collection<Instance>> redEquClassesOfNew;
						Map<IntArrayKey, Collection<Instance>> globalEquClassesOfNew;
						//		U/B
						redEquClassesOfPrevious = 
							LiangIncrementalAlgorithm
								.Basic
								.equivalenceClass(
										previousInstances,
										new IntegerCollectionIterator(previousReduct)
								);
						//		U/C
						globalEquClassesOfPrevious = 
								LiangIncrementalAlgorithm
									.Basic
									.equivalenceClass(
											previousInstances,
											new IntegerArrayIterator(attributes)
									);
						//		U<sub>X</sub>/B
						redEquClassesOfNew = 
								LiangIncrementalAlgorithm
									.Basic
									.equivalenceClass(
											newInstances,
											new IntegerCollectionIterator(previousReduct)
									);
						//		U<sub>X</sub>/C
						globalEquClassesOfNew = 
								LiangIncrementalAlgorithm
									.Basic
									.equivalenceClass(
											newInstances,
											new IntegerArrayIterator(attributes)
									);
						//	Compute (U∪U<sub>X</sub>)/B = {X[1]', ..., X[k]', X[k+1], ..., X[m], M[k+1], ..., M[m']}
						MixedEquivalenceClassSequentialList redEquClassesCMBResult =
								LiangIncrementalAlgorithm
									.Incremental
									.combineEquivalenceClassesOfPreviousNNew(
											redEquClassesOfPrevious,
											redEquClassesOfNew
									);
						//	Compute (U∪U<sub>X</sub>)/C = {X[1]', ..., X[k']', X[k'+1], ..., X[m], M[k'+1], ..., M[m']}
						MixedEquivalenceClassSequentialList globalEquClassesCMBResult =
								LiangIncrementalAlgorithm
									.Incremental
									.combineEquivalenceClassesOfPreviousNNew(
											globalEquClassesOfPrevious,
											globalEquClassesOfNew
									);
						return new Object[] {
								newReduct, 
								redEquClassesOfPrevious, globalEquClassesOfPrevious,
								redEquClassesOfNew, globalEquClassesOfNew,
								redEquClassesCMBResult, globalEquClassesCMBResult,
								calculationClass.newInstance(), staticCalculationClass.newInstance()
						};
					},
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> newReduct = (Collection<Integer>) result[r++];
						//	Compute U/B, U/C, U<sub>X</sub>/B, U<sub>X</sub>/C
						Map<IntArrayKey, Collection<Instance>> redEquClassesOfPrevious =
								(Map<IntArrayKey, Collection<Instance>>) result[r++];
						Map<IntArrayKey, Collection<Instance>> globalEquClassesOfPrevious =
								(Map<IntArrayKey, Collection<Instance>>) result[r++];
						Map<IntArrayKey, Collection<Instance>> redEquClassesOfNew =
								(Map<IntArrayKey, Collection<Instance>>) result[r++];
						Map<IntArrayKey, Collection<Instance>> globalEquClassesOfNew =
								(Map<IntArrayKey, Collection<Instance>>) result[r++];
						MixedEquivalenceClassSequentialList redEquClassesCMBResult =
								(MixedEquivalenceClassSequentialList) result[r++];
						MixedEquivalenceClassSequentialList globalEquClassesCMBResult =
								(MixedEquivalenceClassSequentialList) result[r++];
						Calculation<?> calculation = (Calculation<?>) result[r++];
						Calculation<?> staticCalculation = (Calculation<?>) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, newReduct);
						getParameters().setNonRoot("redEquClassesOfPrevious", redEquClassesOfPrevious);
						getParameters().setNonRoot("globalEquClassesOfPrevious", globalEquClassesOfPrevious);
						getParameters().setNonRoot("redEquClassesOfNew", redEquClassesOfNew);
						getParameters().setNonRoot("globalEquClassesOfNew", globalEquClassesOfNew);
						getParameters().setNonRoot("redEquClassesCMBResult", redEquClassesCMBResult);
						getParameters().setNonRoot("globalEquClassesCMBResult", globalEquClassesCMBResult);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE, calculation);
						getParameters().setNonRoot("staticCalculation", staticCalculation);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_BASIC_UNIVERSE_INFO_BUILDER]
						Collection<Instance> previousInstances = getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM);
						Collection<Instance> Instances = getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES);
						statistics.put(
							StatisticsConstants.Procedure.STATISTIC_BASIC_UNIVERSE_INFO_BUILDER,
							BasicExecutionInstanceInfo.Builder.newBuilder()
								.loadCurrentInfo(Instances, false)
								.setPreviousInstanceNumber(previousInstances.size())
								.setExecutedRecordNumberNumber(
									previousInstances.size()+Instances.size(),
									Instance.class
								)
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Initiate"),
			// 3. Check if able to exit.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("3. "+component.getDescription());
						}
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get("redEquClassesCMBResult"),
								getParameters().get("globalEquClassesCMBResult"),
								getParameters().get("staticCalculation"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> newInstance =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> newReduct =
								(Collection<Integer>) parameters[p++];
						MixedEquivalenceClassSequentialList redEquClassesCMBResult =
								(MixedEquivalenceClassSequentialList) parameters[p++];
						MixedEquivalenceClassSequentialList globalEquClassesCMBResult =
								(MixedEquivalenceClassSequentialList) parameters[p++];
						PositiveApproximationAcceleratorCalculation<Sig> staticCalculation =
								(PositiveApproximationAcceleratorCalculation<Sig>) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Sig globalSigOfNew = staticCalculation.calculate(
								PositiveApproximationAcceleratorOriginalAlgorithm
									.Basic
									.equivalenceClass(
											newInstance,
											new IntegerArrayIterator(attributes)
									),
								attributes.length,
								newInstance.size()
							).getResult();
						if (redEquClassesCMBResult.getMixed()==0 && globalEquClassesCMBResult.getMixed()==0) {
							// Compute ME<sub>U<sub>X</sub></sub>(D|B) and ME<sub>U<sub>X</sub></sub>(D|C)
							Sig redSigOfNew = staticCalculation.calculate(
									PositiveApproximationAcceleratorOriginalAlgorithm
										.Basic
										.equivalenceClass(
												newInstance,
												new IntegerCollectionIterator(previousReduct)
										),
									previousReduct.size(),
									newInstance.size()
							).getResult();
							// if ME<sub>U<sub>X</sub></sub>(D|B) == ME<sub>U<sub>X</sub></sub>(D|C)
							//      turn to Step 7 in the algorithm in the paper
							// else
							//      turn to Step 5 in the algorithm in the paper
							if (!staticCalculation.value1IsBetter(globalSigOfNew, redSigOfNew, sigDeviation)) {
								// Step 7.
								return new Object[] {	true, newReduct, null	};
							}
						}
						return new Object[] { false, null, globalSigOfNew };
					},
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						boolean exit = (boolean) result[r++];
						Collection<Integer> newReduct = (Collection<Integer>) result[r++];
						Sig globalSigOfNew = (Sig) result[r++];
						/* ------------------------------------------------------------------------------ */
						if (exit) {
							getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, newReduct);
							getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, newReduct);
							if (logOn)	log.info(LoggerUtil.spaceFormat(1, "Exit!"));
						}else {
							getParameters().setNonRoot("globalSigOfNew", globalSigOfNew);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						if (exit) {
							//	[STATISTIC_RED_BEFORE_INSPECT]
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
									newReduct
							);
							statistics.put(
									StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
									newReduct
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								0
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check if able to exit"),
			// 4. Prepare for sig loop.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn)	log.info("4. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get("redEquClassesCMBResult"),
								getParameters().get("globalEquClassesCMBResult"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("staticCalculation"),
								getParameters().get("previousSigWithDenominator"),
								getParameters().get("globalSigOfNew"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> previousUniverse =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> newInstance =
								(Collection<Instance>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						MixedEquivalenceClassSequentialList redEquClassesCMBResult =
								(MixedEquivalenceClassSequentialList) parameters[p++];
						MixedEquivalenceClassSequentialList globalEquClassesCMBResult =
								(MixedEquivalenceClassSequentialList) parameters[p++];
						FeatureImportance4LiangIncremental<Sig> calculation = 
								(FeatureImportance4LiangIncremental<Sig>) parameters[p++];
						PositiveApproximationAcceleratorCalculation<Sig> staticCalculation = 
								(PositiveApproximationAcceleratorCalculation<Sig>) parameters[p++];
						boolean previousSigWithDenominator =
								(boolean) parameters[p++];
						Sig globalSigOfNew =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Sig redSigOfCMB, globalSigOfCMB;
						//	Obtain U/D
						Map<IntArrayKey, Collection<Instance>> decEquClassesOfPrevious =
							LiangIncrementalAlgorithm
								.Basic
								.equivalenceClass(
										previousUniverse,
										new IntegerArrayIterator(new int[] {0})
								);
						//	Obtain U<sub>X</sub>/D
						Map<IntArrayKey, Collection<Instance>> decEquClassesOfNew =
							LiangIncrementalAlgorithm
								.Basic
								.equivalenceClass(
										newInstance,
										new IntegerArrayIterator(new int[] {0})
								);
						//	Obtain U∪U<sub>X</sub>/D
						MixedEquivalenceClassSequentialList decEquClassesCMBResult =
								LiangIncrementalAlgorithm
									.Incremental
									.combineEquivalenceClassesOfPreviousNNew(decEquClassesOfPrevious, decEquClassesOfNew);
						//	Compute ME<sub>U</sub>(D|B)
						Sig previousSig =
								staticCalculation.calculate(
										PositiveApproximationAcceleratorOriginalAlgorithm
												.Basic
												.equivalenceClass(
														previousUniverse,
														new IntegerCollectionIterator(previousReduct)
												),
										previousReduct.size(),
										previousUniverse.size()
								).getResult();
						//	Compute ME<sub>U∪U<sub>X</sub></sub>(D|B)
						redSigOfCMB =
								calculation.calculate(
										redEquClassesCMBResult,
										decEquClassesCMBResult, 
										previousUniverse.size(), newInstance.size(), 
										previousSig, globalSigOfNew, 
										previousSigWithDenominator, 
										attributes.length
								).getResult();
						//	Compute ME<sub>U∪U<sub>X</sub></sub>(D|C)
						globalSigOfCMB =
								calculation.calculate(
										globalEquClassesCMBResult, 
										decEquClassesCMBResult, 
										previousUniverse.size(), newInstance.size(), 
										previousSig, globalSigOfNew, 
										previousSigWithDenominator, 
										attributes.length
								).getResult();
						return new Object[] {
								redSigOfCMB, globalSigOfCMB, decEquClassesCMBResult,
								previousSig
						};
					},
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Sig redSigOfCMB = (Sig) result[r++];
						Sig globalSigOfCMB = (Sig) result[r++];
						MixedEquivalenceClassSequentialList decEquClassesCMBResult = (MixedEquivalenceClassSequentialList) result[r++];
						Sig previousSig = (Sig) result[r++];
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("redSigOfCMB", redSigOfCMB);
						getParameters().setNonRoot("globalSigOfCMB", globalSigOfCMB);
						getParameters().setNonRoot("decEquClassesCMBResult", decEquClassesCMBResult);
						getParameters().setNonRoot(ParameterConstants.PARAMETER_PREVIOUS_REDUCT_SIG, previousSig);
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(1, "Previous sig = {}"),
									String.format("%.4f", previousSig.doubleValue())
							);
							log.info(
									LoggerUtil.spaceFormat(1, "Global sig = {} | Reduct sig = {}"),
									String.format("%.4f", globalSigOfCMB.doubleValue()),
									String.format("%.4f", redSigOfCMB.doubleValue())
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Prepare for sig loop"),
			// 5. Sig loop.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("5. "+component.getDescription());
						}
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					},
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_BEFORE_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_BEFORE_INSPECT,
								reduct
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 5. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Sig loop")
				.setSubProcedureContainer(
					"SignificantAttributeSeekingLoopProcedureContainer4MultiObjectIncremental", 
					new SignificantAttributeSeekingLoopProcedureContainer4MultiObjectIncremental<>(getParameters(), logOn)
				),
			// 6. Inspection.
			new ProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
						if (logOn){
							log.info("6. "+component.getDescription());
						}
					},
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						return (Collection<Integer>)
								CollectionUtils.firstOf(component.getSubProcedureContainers().values())
												.exec();
					},
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, reduct);
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
									
					@Override public String staticsName() {
						return shortName()+" | 6. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection")
				.setSubProcedureContainer(
					"ReductInspectionProcedureContainer", 
					new ReductInspectionProcedureContainer4MultiObjectIncremental<>(getParameters(), logOn)
				),
		};
	}

	public long getTime() {
		return getComponents().stream()
				.map(comp->ProcedureUtils.Time.sumProcedureComponentTimes(comp))
				.reduce(Long::sum).orElse(0L);
	}

	@Override
	public Map<String, Long> getTimeDetailByTags() {
		return ProcedureUtils.Time.sumProcedureComponentsTimesByTags(this);
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return getComponents().stream().map(ProcedureComponent::getDescription).toArray(String[]::new);
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each: components)	getComponents().add(each);
		return (Collection<Integer>) components[0].exec();
	}
}