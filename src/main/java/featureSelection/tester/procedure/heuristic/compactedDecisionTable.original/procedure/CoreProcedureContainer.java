package featureSelection.tester.procedure.heuristic.compactedDecisionTable.original.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import common.utils.LoggerUtil;
import common.utils.StringUtils;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.algorithm.alg.compactedDecisionTable.original.CompactedDecisionTableHashAlgorithm;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.compactedTable.InstanceBasedCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.impl.original.equivalenceClass.EquivalenceClassCompactedTableRecord;
import featureSelection.repository.entity.alg.compactedDecisionTable.interf.DecisionNumber;
import featureSelection.repository.support.calculation.alg.CompactedDecisionTableCalculation;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Core searching for <strong>Compacted Decision Table</strong> Feature Selection. 
 * This procedure contains 3 ProcedureComponents: 
 * <ul>
 * 	<li>
 * 		<strong>Core procedure controller</strong>
 * 		<p>Control loop over attributes that are not in reduct, and calculate their
 * 			inner significance. If removing the attribute from the feature subset
 * 			doesn't have any effect on the significance(i.e <code>sig(C-{a})==sig(C)
 * 			</code>), it is NOT a core attribute.
 * 	</li>
 * 	<li>
 * 		<strong>Equivalence Class partition</strong>
 * 		<p>Use the given feature subset to partition {@link Instance}s and get
 * 			the correspondent Equivalence Class Compacted Table records as the
 * 			Equivalence Classes partitioned by the feature subset.
 * 	</li>
 * 	<li>
 * 		<strong>Core</strong>
 * 		<p>Calculate the inner significance of the attribute and determine if it is a
 * 			Core attribute.
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class CoreProcedureContainer<Sig extends Number, DN extends DecisionNumber>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private List<String> reportKeys;
	
	private Map<String, Object> localParameters;
	private int loopCount;
	
	public CoreProcedureContainer(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
	
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Core";
	}

	@SuppressWarnings("unchecked")
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1 Core procedure controller
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributesArray =
								(int[]) parameters[p++];
						IntegerArrayIterator attributes =
								new IntegerArrayIterator(attributesArray);
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> com1 = getComponents().get(1);
						ProcedureComponent<?> com2 = getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// core = {}
						Set<Integer> core = new HashSet<>(attributesArray.length);
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("core", core);
						localParameters.put("attributes", attributes);
						TimerUtils.timeContinue((TimeCounted) component);
						// Loop over all attributes in C
						int[] examAttributes = new int[attributes.size()-1];
						attributes.skip(1);
						for (int i=0; i<examAttributes.length; i++)	examAttributes[i] = attributes.next();
						TimerUtils.timePause((TimeCounted) component);
						localParameters.put("examAttributes", examAttributes);
						TimerUtils.timeContinue((TimeCounted) component);
						
						attributes.reset();
						while (attributes.hasNext()) {
							TimerUtils.timePause((TimeCounted) component);
							loopCount++;
							com1.exec();
							com2.exec();
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return core;
						/* ------------------------------------------------------------------------------ */
					}, 
					(component, core) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, core);
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(1, "|core|={}, core={}"),
									core.size(), 
									StringUtils.numberToString(core, 50, 0)
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						int[] attributes = getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES);
						//	[STATISTIC_CORE_LIST]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_CORE_LIST, core.toArray(new Integer[core.size()]));
						//	[STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH, attributes.length);
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						Collection<?> compactedTable = getParameters().get("compactedTable");
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								compactedTable.size(), 
								core.size()
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
				}.setDescription("Core procedure controller"),
			// 2 Equivalence Class partition
			new TimeCountedProcedureComponent<Collection<EquivalenceClassCompactedTableRecord<DN>>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get("compactedTable"),
								localParameters.get("examAttributes"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<InstanceBasedCompactedTableRecord<DN>> compactedTable =
								(Collection<InstanceBasedCompactedTableRecord<DN>>) parameters[p++];
						int[] examAttributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Calculate significance of C-{a}.
						return CompactedDecisionTableHashAlgorithm
								.Basic
								.equivalenceClassOfCompactedTable(
									compactedTable, 
									new IntegerArrayIterator(examAttributes)
								);
					}, 
					(component, equTable) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("equTable", equTable);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Equivalence Class partition"),
			// 3 Core
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component)->{
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("globalSig"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_DEVIATION),
								localParameters.get("attributes"),
								localParameters.get("core"),
								localParameters.get("examAttributes"),
								localParameters.get("equTable"),
								getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						boolean attributeIsCore = false;
						int p=0;
						CompactedDecisionTableCalculation<Sig> calculation =
								(CompactedDecisionTableCalculation<Sig>) parameters[p++];
						Sig globalSig =
								(Sig) parameters[p++];
						Sig sigDeviation =
								(Sig) parameters[p++];
						IntegerArrayIterator attributes =
								(IntegerArrayIterator) parameters[p++];
						Collection<Integer> core =
								(Collection<Integer>) parameters[p++];
						int[] examAttributes =
								(int[]) parameters[p++];
						Collection<EquivalenceClassCompactedTableRecord<DN>> equTable =
								(Collection<EquivalenceClassCompactedTableRecord<DN>>)
								parameters[p++];
						Collection<Instance> instances =
								(Collection<Instance>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						int i = attributes.currentIndex(), examAttribute = attributes.next();
						Sig innerSig = (Sig) calculation.calculate(equTable, examAttributes.length, instances.size())
														.getResult();
						if (attributes.hasNext())	examAttributes[i] = examAttribute;
						// if a.innerSig!=C.sig
						//	core = core U {a}
						if (calculation.value1IsBetter(globalSig, innerSig, sigDeviation)) {
							core.add(examAttribute);
							attributeIsCore = true;
						}
						return new Object[] {
								examAttribute,
								attributeIsCore
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						int examAttribute = (int) result[r++];
						boolean attributeIsCore = (boolean) result[r++];
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						Collection<?> compactedTable = getParameters().get("compactedTable");
						//	[DatasetRealTimeInfo]
						Collection<Integer> core = (Collection<Integer>) localParameters.get("core");
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								compactedTable.size(), 
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(
								localParameters, report,
								reportMark,
								(TimeCountedProcedureComponent<?>) component
							);
						//	[REPORT_CORE_INDIVIDUAL_RESULT]
						ProcedureUtils.Report.saveItem(
								report, reportMark(),
								ReportConstants.Procedure.REPORT_CORE_INDIVIDUAL_RESULT,
								attributeIsCore
						);
						//	[REPORT_CORE_CURRENT_ATTRIBUTE]
						ProcedureUtils.Report.saveItem(
								report, reportMark(),
								ReportConstants.Procedure.REPORT_CORE_CURRENT_ATTRIBUTE,
								examAttribute
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}

					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
			}.setDescription("Core"),
		};	
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] componentArray = initComponents();
		for (ProcedureComponent<?> each : componentArray) {
			this.getComponents().add(each);
			//reportKeys.add(each.getDescription());
		}
		return (Collection<Integer>) componentArray[0].exec();
	}

	@Override
	public String staticsName() {
		return shortName();
	}
	
	@Override
	public String reportName() {
		return shortName();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	private String reportMark() {
		return "Loop["+loopCount+"]";
	}
}