package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.simpleCounting.realtime.procedure.inspect;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.HashMap;
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
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct inspection for <strong>Quick Reduct - Rough Equivalence Class based extension: Simple
 * Counting(Real-time)</strong> Feature Selection. This procedure contains 2
 * {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Inspection procedure controller</strong>
 * 		<p>Control to loop over attributes in reduct to inspect redundant.
 * 	</li>
 * 	<li>
 * 		<strong>Inspect reduct</strong>
 * 		<p>Check the redundancy of an attribute by calculating its inner significance.
 * 			Considered redundant if the inner significance is 0(i.e.
 * 			<code>sig(red)=sig(red-{a})</code>).
 * 	</li>
 * </ul>
 *
 * @author Benjamin_L
 */
@Slf4j
public class ClassicReductInspectionProcedureContainer<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	private int loopCount;
	
	public ClassicReductInspectionProcedureContainer(
			ProcedureParameters paramaters, boolean logOn
	) {
		super(logOn? null: log, paramaters);
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Reduct inspect(Classic)";
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
							getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
							new HashSet<Integer>(getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)),
							getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
							getParameters().get("equClasses"),
						});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Collection<Integer> reduct =
								(Collection<Integer>) parameters[p++];
						RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>)
								parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Object[]> comp1 = (ProcedureComponent<Object[]>) getComponents().get(1);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Calculate the global significance.
						Sig globalSig = calculation.calculate(equClasses, new IntegerCollectionIterator(reduct), instances.size())
													.getResult();
						Collection<Integer> redSet = new HashSet<>(reduct);
						
						int examAttr;
						Iterator<Integer> iterator = reduct.iterator();
						while (iterator.hasNext()) {
							redSet.remove(examAttr=iterator.next());
							
							TimerUtils.timePause((TimeCounted) component);
							
							loopCount++;
							localParameters.put("globalSig", globalSig);
							localParameters.put("examAttr", examAttr);
							localParameters.put("redSet", redSet);
							localParameters.put("iterator", iterator);
							localParameters.put("red", reduct);
							comp1.exec();
							
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return reduct;
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
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								localParameters.get("globalSig"),
								getParameters().get("equClasses"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								localParameters.get("redSet"),
								localParameters.get("examAttr"),
								localParameters.get("iterator"),
						});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsRedundant = false;
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig> calculation =
								(RoughEquivalenceClassBasedRealtimeSimpleCountingExtensionCalculation<Sig>)
								parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						Collection<Integer> redSet =
								(Collection<Integer>) parameters[p++];
						int examAttr =
								(int) parameters[p++];
						Iterator<Integer> iterator =
								(Iterator<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Sig examPos = calculation.calculate(equClasses, new IntegerCollectionIterator(redSet), instances.size())
												.getResult();
						if (!calculation.value1IsBetter(globalSig, examPos, sigDeviation)) {
							iterator.remove();
							
							TimerUtils.timePause((TimeCounted) component);
							attributeIsRedundant = true;
							TimerUtils.timeContinue((TimeCounted) component);
						}else {
							redSet.add(examAttr);
						}
						return new Object[] {
								examAttr,
								attributeIsRedundant
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						int examAttr = (int) result[r++];
						boolean attributeIsRedundant = (boolean) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = "Loop["+loopCount+"] Attr["+examAttr+"]";
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(),
								((Collection<?>) getParameters().get("equClasses")).size(),
								((Collection<?>) localParameters.get("red")).size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_INSPECT_ATTRIBUTE_REDUNDANT]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE,
								examAttr
						);
						//	[REPORT_INSPECT_ATTRIBUTE_REDUNDANT]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_ATTRIBUTE_REDUNDANT,
								attributeIsRedundant
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