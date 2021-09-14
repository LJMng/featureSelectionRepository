package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.inspect;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
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
import featureSelection.repository.algorithm.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedExtensionAlgorithm;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.EquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalDecision.procedure.core.ClassicCoreProcedureContainer;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct inspection for <strong>Incremental Decision Rough Equivalence Class based
 * (ID-REC)</strong> Feature Selection. <strong>Class improved version</strong>.
 * (Check out the description of ProcedureComponent 2 and 3)
 * <p>
 * This procedure contains 3 ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Inspection procedure controller</strong>
 * 		<p>Control to loop over attributes in reduct to inspect redundant.
 * 	</li>
 * 	<li>
 * 		<strong>Rough Equivalence Class partition</strong>
 * 		<p>Use the given feature subset to partition EquivalenceClasses and get the
 * 			correspondent Rough Equivalence Classes. Different from the normal version,
 * 			it is {@link ClassSetType#BOUNDARY} sensitive and it returns <code>null</code>
 * 			if encountering one.
 * 		<p>In this case, using <code>C-{a}</code> to partition EquivalenceClasses and
 * 			get at least 1 Rough Equivalence class which is {@link ClassSetType#BOUNDARY}
 * 			means without the attribute <code>{a}</code> is indispensable and it is NOT
 * 			redundant.
 * 	</li>
 *	 <li>
 * 		<strong>Inspect reduct redundancy</strong>
 * 		<p>Check the redundancy of an attribute by calculating its inner significance.
 * 			Considered redundant if the inner significance is 0(i.e.
 * 			sig(red)=sig(red-{a})). In the calculation,
 * 			{@link EquivalenceClassDecMapXtension#getSingleSigMark()} is used to avoid
 * 			the re-calculating entropy of rough equivalence class is
 * 			{@link ClassSetType#NEGATIVE} and contains only 1 equivalence class. Which
 * 			is an accelerating strategy to inspect reduct.
 * 		<p>Different from {@link ClassicCoreProcedureContainer}, a strategy bases on
 * 			{@link ClassSetType#BOUNDARY} sensitive rough equivalence class partition
 * 			strategy is used to accelerate the process of determining whether an
 * 			attribute is redundant or not. That is, it is expecting to encounter
 * 			{@link ClassSetType#BOUNDARY} in the partitioning by <code>Red-{a}</code>
 * 			if {a} is not a redundant attribute.
 * 	</li>
 * </ul>
 * 
 * 
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalDecision.Core.ClassicImproved
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalDecision.InspectReduct#classicImproved(
 * 		int, Collection, Number, Collection, RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation,
 * 		Number)
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ClassicImprovedReductInspectionProcedureContainer<Sig extends Number> 
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	private int loopCount, boundarySkipCount;
	
	public ClassicImprovedReductInspectionProcedureContainer(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? null: log, parameters);
		
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Reduct inspect(Classic.Imp)";
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
			// 1. Inspection procedure controller.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								new HashSet<Integer>(getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)),
						});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> red = (Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Object[]> comp1 = (ProcedureComponent<Object[]>) getComponents().get(1);
						localParameters.put("red", red);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Go through a in R
						Integer[] redCopy = red.toArray(new Integer[red.size()]);
						for (int attr: redCopy) {
							TimerUtils.timePause((TimeCounted) component);

							loopCount++;
							localParameters.put("attr", attr);
							comp1.exec();
							
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return red;
					}, 
					(component, red) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								red
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						//	[REPORT_INSPECT_4_IDREC_0_REC_DIRECT]
						ProcedureUtils.Report.saveItem(
								report, component.getDescription(),
								ReportConstants.Procedure.REPORT_INSPECT_4_IDREC_0_REC_DIRECT,
								boundarySkipCount
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection procedure controller"),
			// 2. Inspect reduct.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								getParameters().get("globalSig"),
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								localParameters.get("attr"),
								localParameters.get("red"),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsRedundant = true, boundaryDiscovered = false;
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Sig globalSig =
								(Sig) parameters[p++];
						Collection<EquivalenceClassDecMapXtension<Sig>> equClasses =
								(Collection<EquivalenceClassDecMapXtension<Sig>>)
								parameters[p++];
						RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						int attr =
								(int) parameters[p++];
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// calculate Sig(R-{a}).
						red.remove(attr);
						Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses =
								RoughEquivalenceClassBasedExtensionAlgorithm
									.IncrementalDecision
									.Core
									.ClassicImproved
									.boundarySensitiveRoughEquivalenceClass(
											equClasses, 
											new IntegerCollectionIterator(red)
									);
						// * Doesn't contains 0-REC, otherwise, current attribute is not redundant
						//	 (for it can not be removed).
						if (roughClasses!=null) {
							calculation.calculate(roughClasses, red.size(), instances.size());
							Sig examSig = calculation.getResult();
							// 3 if (R-{a}.sig==C.sig)
							if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)) {
								// 4 R = R-{a}
								red.add(attr);
								
								TimerUtils.timePause((TimeCounted) component);
								attributeIsRedundant = false;
								TimerUtils.timeContinue((TimeCounted) component);
							}
						}else {
							red.add(attr);
							
							TimerUtils.timePause((TimeCounted) component);
							boundaryDiscovered = true;
							boundarySkipCount++;
							attributeIsRedundant = false;
							TimerUtils.timeContinue((TimeCounted) component);
						}
						
						return new Object[] {
							attr,
							attributeIsRedundant,
							boundaryDiscovered, 
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						int attr = (int) result[r++];
						boolean attributeIsRedundant = (boolean) result[r++];
						boolean boundaryDiscovered = (boolean) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = "Loop["+loopCount+"] Attr["+attr+"]";
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						Collection<Integer> red = (Collection<Integer>) localParameters.get("red");
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						Long lastComponentTime = (Long) localParameters.get("lastComponentTime");
						if (lastComponentTime==null)	lastComponentTime = 0L;
						long currentComponentTime = ((TimeCountedProcedureComponent<?>) component).getTime();
						ProcedureUtils.Report.ExecutionTime.save(report, reportMark, currentComponentTime - lastComponentTime);
						localParameters.put("lastComponentTime", currentComponentTime);
						//	[REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE]
						ProcedureUtils.Report.saveItem(
								report, reportMark, 
								ReportConstants.Procedure.REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE,
								attr
						);
						//	[REPORT_INSPECT_ATTRIBUTE_REDUNDANT]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_ATTRIBUTE_REDUNDANT,
								attributeIsRedundant
						);
						//	[REPORT_INSPECT_4_IDREC_0_REC_DIRECT]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_4_IDREC_0_REC_DIRECT,
								boundaryDiscovered
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
											
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspect reduct"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] components = initComponents();
		for (ProcedureComponent<?> each : components)	this.getComponents().add(each);
		return (Collection<Integer>) components[0].exec();
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}
}