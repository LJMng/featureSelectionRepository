package featureSelection.tester.procedure.heuristic.activeSampleSelection.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerCollectionIterator;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
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
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.activeSampleSelection.ActiveSampleSelectionAlgorithm;
import featureSelection.repository.entity.alg.activeSampleSelection.EquivalenceClass;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ActiveSampleSelection;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct inspection for <strong>Sample Pair Selection</strong> for Attribute Reduction
 * (for <strong> Static Data</strong>)
 * This procedure contains 3 ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Inspection procedure controller</strong>
 * 		<p>Control to loop over attributes in reduct to inspect redundant.
 * 	</li>
 * 	<li>
 * 		<strong>Calculate global feature importance</strong>
 * 		<p>Calculate the global feature importance using all attributes.
 * 	</li>
 * 	<li>
 * 		<strong>Check redundancy of an attribute</strong>
 * 		<p>Check the redundancy of an attribute by calculating its inner significance. Considered
 * 	    	redundant if the inner significance is 0(i.e. sig(red)=sig(red-{a})).
 * 	</li>
 * </ul>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_UNIVERSE_INSTANCES}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_SIG_CALCULATION_INSTANCE}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_ATTRIBUTES}</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ReductInspectionProcedureContainer<Sig>
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
	
	public ReductInspectionProcedureContainer(
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
						Collection<Integer> red =
								(Collection<Integer>) parameters[0];
						/* ------------------------------------------------------------------------------ */
						IntegerIterator reduct = new IntegerCollectionIterator(red);
						ProcedureComponent<Sig> comp1 = (ProcedureComponent<Sig>) getComponents().get(1);
						ProcedureComponent<Sig> comp2 = (ProcedureComponent<Sig>) getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Calculate global feature importance
						Sig globalSig = comp1.exec();
						// Preparation.
						LinkedList<Integer> examQueue = new LinkedList<>();
						reduct.reset();	for (int i=0; i<reduct.size(); i++)	examQueue.add(reduct.next());
						
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("globalSig", globalSig);
						localParameters.put("examQueue", examQueue);
						TimerUtils.timeContinue((TimeCounted) component);

						// Go through attributes in reduct.
						int originalSize = reduct.size();
						for (int i=0; i<originalSize; i++) {
							comp2.exec();
						}
						return examQueue;
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST_AFTER_INSPECTATION, result);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_RED_AFTER_INSPECT]
						statistics.put(
								StatisticsConstants.Procedure.STATISTIC_RED_AFTER_INSPECT,
								result
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
				}.setDescription("Inspection procedure controller"),
			// 2. Calculate global feature importance.
			new TimeCountedProcedureComponent<Sig>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						FeatureImportance4ActiveSampleSelection<Sig> calculation = 
								(FeatureImportance4ActiveSampleSelection<Sig>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						Collection<EquivalenceClass> equClasses = 
							ActiveSampleSelectionAlgorithm
								.Basic
								.equivalenceClasses(instances, new IntegerArrayIterator(attributes))
								.values();
						return calculation.calculate(equClasses).getResult();
					}, 
					(component, sig) -> {
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils
								.Report
								.ExecutionTime
								.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Calculate global feature importance"),
			// 3. Check redundancy of an attribute.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						//if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								localParameters.get("examQueue"),
								localParameters.get("globalSig"),
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
						FeatureImportance4ActiveSampleSelection<Sig> calculation =
								(FeatureImportance4ActiveSampleSelection<Sig>) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						LinkedList<Integer> examQueue =
								(LinkedList<Integer>) parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int examAttr = examQueue.pollFirst();
						// if pos(reduct-{a}) has intersection with every element in M*
						//	a is redundant: reduct = reduct - {a}
						Collection<EquivalenceClass> equClasses =
								ActiveSampleSelectionAlgorithm
									.Basic
									.equivalenceClasses(instances, new IntegerCollectionIterator(examQueue))
									.values();
						Sig examSig = calculation.calculate(equClasses).getResult();
						attributeIsRedundant = !calculation.value1IsBetter(globalSig, examSig, sigDeviation);
						if (!attributeIsRedundant)	examQueue.addLast(examAttr);
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
						String reportMark = reportMark(examAttr);
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						Collection<Integer> red =
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST);
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(),
								0, 
								red.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils
								.Report
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
								ReportConstants.Procedure.REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE,
								examAttr
						);
						//	[REPORT_INSPECT_CURRENT_ATTRIBUTE_VALUE]
						ProcedureUtils.Report.saveItem(
								report, 
								reportMark,
								ReportConstants.Procedure.REPORT_INSPECT_ATTRIBUTE_REDUNDANT,
								attributeIsRedundant
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
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