package featureSelection.tester.procedure.heuristic.liangIncrementalAlgorithm.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.liangIncrementalAlgorithm.LiangIncrementalAlgorithm;
import featureSelection.repository.algorithm.alg.positiveApproximationAccelerator.PositiveApproximationAcceleratorOriginalAlgorithm;
import featureSelection.repository.entity.alg.liangIncrementalAlgorithm.MixedEquivalenceClassSequentialList;
import featureSelection.repository.support.calculation.alg.FeatureImportance4LiangIncremental;
import featureSelection.repository.support.calculation.alg.PositiveApproximationAcceleratorCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct inspection for <strong>Liang incremental entropy Calculation</strong> based Incremental
 * Feature Selection. This procedure contains 2 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Inspection procedure controller</strong>
 * 		<p>Control loop over attributes of reduct, and calculate their inner
 * 			significance. If removing the attribute from the feature subset doesn't
 * 			have any effect on the significance(i.e <code>dep(Red-{a})==dep(Red)
 * 			</code>), it is REDUNDANT.
 * 	</li>
 * 	<li>
 * 		<strong>Check redundancy of an attribute</strong>
 * 		<p>Calculate the inner significance of the attribute by removing it and
 * 			determine if it is redundant.
 * 	</li>
 * </ul>
 *
 * @param <Sig>
 *     Type of feature (subset) significance.
 */
@Slf4j
public class ReductInspectionProcedureContainer4MultiObjectIncremental<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	private Map<String, Object> localParameters;
	private int loopCount;
	
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private List<String> reportKeys;
	
	public ReductInspectionProcedureContainer4MultiObjectIncremental(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		
		localParameters = new HashMap<>();
		
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	}

	@Override
	public String shortName() {
		return "Inspect reduct";
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
			// 1. Inspection procedure controller
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						if (logOn){
							log.info(
									LoggerUtil.spaceFormat(1, "1/{}. {}"),
									getComponents().size(), component.getDescription()
							);
						}
						component.setLocalParameters(new Object[] {
								new HashSet<>(getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> reduct = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = (ProcedureComponent<?>) getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						localParameters.put("reduct", reduct);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						//	Loop over attributes in reduct
						int[] reductArray = reduct.stream().mapToInt(v->v).toArray();
						for (int attr: reductArray) {
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("attr", attr);
							comp1.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return reduct;
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, reduct);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								reduct
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection procedure controller"),
			// 2. Check redundancy of an attribute.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_COLLECTION_ITEM),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("staticCalculation"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								getParameters().get("previousSigWithDenominator"),
								getParameters().get("decEquClassesCMBResult"),
								localParameters.get("reduct"),
								localParameters.get("attr"),
								localParameters.get("redSig"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsRedundant = false;
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> previousUniverse =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> newInstance =
								(Collection<Instance>) parameters[p++];
						FeatureImportance4LiangIncremental<Sig> calculation =
								(FeatureImportance4LiangIncremental<Sig>) parameters[p++];
						PositiveApproximationAcceleratorCalculation<Sig> staticCalculation =
								(PositiveApproximationAcceleratorCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						boolean previousSigWithDenominator =
								(boolean) parameters[p++];
						MixedEquivalenceClassSequentialList decEquClassesCMBResult =
								(MixedEquivalenceClassSequentialList) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						int attr =
								(int) parameters[p++];
						Sig redSig =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						reduct.remove(attr);
						IntegerIterator innerRed = new IntegerCollectionIterator(reduct);
						// Compute Sig<sub>U∪{x}</sub><pub>inner</pub>(a, B, D)
						//	U/B
						Map<IntArrayKey, Collection<Instance>> redEquClassesOfPrevious =
							LiangIncrementalAlgorithm
								.Basic
								.equivalenceClass(previousUniverse, innerRed);
						//	U<sub>X</sub>/B
						Map<IntArrayKey, Collection<Instance>> redEquClassesOfNew =
							LiangIncrementalAlgorithm
								.Basic
								.equivalenceClass(newInstance, innerRed);
						//	U∪U<sub>X</sub>/B
						MixedEquivalenceClassSequentialList equClassesCMBResult =
							LiangIncrementalAlgorithm
								.Incremental.combineEquivalenceClassesOfPreviousNNew(
									redEquClassesOfPrevious, redEquClassesOfNew
								);
						// clear space
						redEquClassesOfNew = redEquClassesOfPrevious = null;
					
						//	Previous sig.
						Sig previousSig =
								staticCalculation.calculate(
										PositiveApproximationAcceleratorOriginalAlgorithm
												.Basic
												.equivalenceClass(previousUniverse, innerRed),
										innerRed.size(),
										previousUniverse.size()
								).getResult();
						//	New sig.
						Sig newSig =
								staticCalculation.calculate(
										PositiveApproximationAcceleratorOriginalAlgorithm
												.Basic
												.equivalenceClass(newInstance, innerRed),
										innerRed.size(),
										newInstance.size()
								).getResult();
						//	Inner sig.
						Sig innerSig =
								calculation.calculate(
										equClassesCMBResult, decEquClassesCMBResult, 
										previousUniverse.size(), newInstance.size(), 
										previousSig, newSig, previousSigWithDenominator,
										innerRed.size()
								).getResult();
						// if Sig<sub>U∪{x}</sub><pub>inner</pub>(a, B, D) ==0, B<-B-{a}.
						if (calculation.value1IsBetter(redSig, innerSig, sigDeviation)) {
							reduct.add(attr);
						}else {
							TimerUtils.timePause((TimeCounted) component);
							attributeIsRedundant = true;
							TimerUtils.timeContinue((TimeCounted) component);
						}
						
						equClassesCMBResult = null;
								
						return new Object[] {
							attr,
							attributeIsRedundant
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
//						int r=0;
//						int examAttr = (int) result[r++];
//						boolean attributeIsRedundant = (boolean) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Report
						/*String reportMark = reportMark(examAttr);
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						Collection<Integer> red = getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE]
						ProcedureUtils.Report.saveItem(
								report, 
								reportMark, 
								Constants.REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE,
								examAttr
						);
						//	[REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE]
						ProcedureUtils.Report.saveItem(
								report, 
								reportMark, 
								Constants.REPORT_INSPECT_ATTRIBUTE_REDUNDANT,
								attributeIsRedundant
						);//*/
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Check redundancy of an attribute"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps) {
			this.getComponents().add(each);
			reportKeys.add(each.getDescription());
		}
		return (Collection<Integer>) comps[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	public String reportMark(int examAttr) {
		return "Loop["+loopCount+"] Attr["+examAttr+"]";
	}
}