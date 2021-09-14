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
import featureSelection.repository.entity.alg.rec.classSet.impl.EquivalenceClass;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.EquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.decisionMap.RoughEquivalenceClassDecMapXtension;
import featureSelection.repository.entity.alg.rec.classSet.type.ClassSetType;
import featureSelection.repository.support.calculation.alg.roughEquivalenceClassBased.RoughEquivalenceClassBasedIncrementalDecisionExtensionCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct inspection for <strong>Incremental Decision Rough Equivalence Class based
 * (ID-REC)</strong> Feature Selection. This procedure contains 2 ProcedureComponents:
 * <ul>
 * 	<li>
 * 		<strong>Inspection procedure controller</strong>
 * 		<p>Control to loop over attributes in reduct to inspect redundant.
 * 	</li>
 * 	<li>
 * 		<strong>Rough Equivalence Class partition</strong>
 * 		<p>Use the given feature subset to partition {@link EquivalenceClass}es and get
 * 			the correspondent Rough Equivalence Classes.
 * 		<p>In this case, using <code>C-{a}</code> to partition {@link EquivalenceClass}es
 * 			and get at least 1vRough Equivalence class which is
 * 			{@link ClassSetType#BOUNDARY} means without the attribute <code>{a}</code> is
 * 			in-dispensable, otherwise, remove the redundant attribute {a} from reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Inspect reduct redundancy</strong>
 * 		<p>Check the redundancy of an attribute by calculating its inner significance.
 * 			Considered redundant if the inner significance is 0(i.e. sig(red)=sig(red-{a})).
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
								new HashSet<Integer>(getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST)),
						});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						Collection<Integer> red = (Collection<Integer>) parameters[0];
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
								localParameters.get("red"),
								localParameters.get("attr"),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
					},
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsRedundant = true;
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
						Collection<Integer> red =
								(Collection<Integer>) parameters[p++];
						int attr =
								(int) parameters[p++];
						Collection<Instance> universes =
								(Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Go through a in R
						Sig examSig;
						Collection<RoughEquivalenceClassDecMapXtension<Sig>> roughClasses;
						// calculate Sig(R-{a}).
						red.remove(attr);
						roughClasses =
								RoughEquivalenceClassBasedExtensionAlgorithm
										.IncrementalDecision
										.Basic
										.roughEquivalenceClass(
												equClasses,
												new IntegerCollectionIterator(red)
										);
						calculation.calculate(roughClasses, red.size(), universes.size());
						examSig = calculation.getResult();
						// if (R-{a}.sig==C.sig)
						if (calculation.value1IsBetter(globalSig, examSig, sigDeviation)) {
							// R = R-{a}
							red.add(attr);
							
							TimerUtils.timePause((TimeCounted) component);
							attributeIsRedundant = false;
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return new Object[] {
								attr,
								attributeIsRedundant
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						int attr = (int) result[r++];
						boolean attributeIsRedundant = (boolean) result[r++];
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
						//	[REPORT_INSPECT_ATTRIBUTE_REDUNDANT]
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