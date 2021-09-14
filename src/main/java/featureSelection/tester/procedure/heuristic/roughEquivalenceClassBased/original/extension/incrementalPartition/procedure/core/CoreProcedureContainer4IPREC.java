package featureSelection.tester.procedure.heuristic.roughEquivalenceClassBased.original.extension.incrementalPartition.procedure.core;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.entity.alg.rec.classSet.impl.extension.incrementalPartition.RoughEquivalenceClassDummy;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.PartitionResult;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.AttrProcessStrategyParams;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4Comb;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.impl.attributeProcessStrategy.core.attributeCombination.AttrProcessStrategy4CombInReverse;
import featureSelection.repository.entity.alg.rec.extension.incrementalPartition.interf.attributeProcessStrategy.AttributeProcessStrategy;
import featureSelection.repository.support.shrink.roughEquivalenceClassBased.extension.incrementalPartition.Shrink4RECBoundaryClassSetStays;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Core computing for <strong>Quick Reduct - Rough Equivalence Class based extension: Incremental
 * Partition</strong> Feature Selection. This procedure contains 2 {@link ProcedureComponent}s:
 * <ul>
 * 	<li>
 * 		<strong>Core controller</strong>
 * 		<p>Control loop over attributes that are not in reduct, and calculate their
 * 			inner significance. If removing a attribute from the feature subset
 * 			doesn't have any effect on the significance(i.e <code>sig(C-{a})==sig(C)
 * 			</code>), the the attribute is NOT a core attribute.
 * 	</li>
 * 	<li>
 * 		<strong>Core loop</strong>
 * 		<p>Calculate the inner significance of the attribute and determine if it is a
 * 			Core attribute.
 * 	</li>
 * </ul>
 *
 * @see RoughEquivalenceClassBasedExtensionAlgorithm.IncrementalPartition.Core.ContinuityBased
 *
 * @author Benjamin_L
 */
@Slf4j
public class CoreProcedureContainer4IPREC
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
			ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public CoreProcedureContainer4IPREC(ProcedureParameters parameters, boolean logOn) {
		super(logOn? null: log, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Core(IP-REC)";
	}

	@Override
	public String staticsName() {
		return shortName();
	}

	@Override
	public String reportName() {
		return shortName();
	}
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	@Override
	public ProcedureComponent<?>[] initComponents() {
		return new ProcedureComponent<?>[] {
			// 1. Core controller.
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								getParameters().get("coreAttributeProcessStrategyParams"),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
						});
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						AttrProcessStrategyParams coreAttributeProcessStrategyParams =
								(AttrProcessStrategyParams)
								parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// initiate pointer: begin = 0, end = m-1
						AttrProcessStrategy4Comb coreAttributeProcessStrategy =
								new AttrProcessStrategy4CombInReverse(
										coreAttributeProcessStrategyParams
								).initiate(new IntegerArrayIterator(attributes));
						Collection<Integer> core = new HashSet<>();
						// Loop until all attributes are checked
						while (coreAttributeProcessStrategy.hasNext()) {
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("core", core);
							localParameters.put("attributesLength", attributes.length);
							localParameters.put("coreAttributeProcessStrategy", coreAttributeProcessStrategy);
							this.getComponents().get(1).exec();
							core = (Collection<Integer>) localParameters.get("core");
							TimerUtils.timeContinue((TimeCounted) component);
						}
						return core;
					}, 
					(component, core) -> {
						/* ------------------------------------------------------------------------------ */
						localParameters.put("core", core);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_CORE_LIST]
						statistics.put(StatisticsConstants.Procedure.STATISTIC_CORE_LIST, core.toArray(new Integer[core.size()]));
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
														
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Core controller"),
			// 2. Core loop.
			new TimeCountedProcedureComponent<Object[]>(
					ComponentTags.TAG_CORE,
					this.getParameters(), 
					(component) -> {
						component.setLocalParameters(new Object[] {
								localParameters.get("coreAttributeProcessStrategy"),
								getParameters().get("incPartitionAttributeProcessStrategy"),
								getParameters().get("equClasses"),
								localParameters.get("attributesLength"),
								localParameters.get("core"),
								getParameters().get(ParameterConstants.PARAMETER_SHRINK_INSTANCE_INSTANCE)
						});
						
						Integer loop = (Integer) localParameters.get("loop");
						if (loop==null)	loop=0;
						localParameters.put("loop", loop+1);
						//if (logOn)	log.info("		"+"Loop {}", loop+1);//*/
					},
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int inLineSize, boundaryRoughEquSortTimes = 0;
						int[] examAttributes;
						boolean[] attributeIsCore;
						/* ------------------------------------------------------------------------------ */
						int p = 0;
						AttrProcessStrategy4Comb coreAttributeProcessStrategy = 
								(AttrProcessStrategy4Comb) 
								parameters[p++];
						AttributeProcessStrategy incPartitionAttributeProcessStrategy =
								(AttributeProcessStrategy) 
								parameters[p++];
						Collection<EquivalenceClass> equClasses =
								(Collection<EquivalenceClass>) parameters[p++];
						int attributesLength =
								(int) parameters[p++];
						Collection<Integer> core =
								(Collection<Integer>) parameters[p++];
						Shrink4RECBoundaryClassSetStays shrinkInstance =
								(Shrink4RECBoundaryClassSetStays) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Extract attributes from begin to end: B
						int[] attributeGroup = coreAttributeProcessStrategy.getInLineAttr();
						
						TimerUtils.timePause((TimeCounted) component);
						inLineSize = attributeGroup.length;
						TimerUtils.timeContinue((TimeCounted) component);

						// Use the rest of the attributes to do 1st round of partitioning: U/(C-B)
						PartitionResult<Collection<Integer>, Collection<RoughEquivalenceClassDummy>> partitionResult =
								RoughEquivalenceClassBasedExtensionAlgorithm
									.IncrementalPartition
									.Basic
									.dynamicIncrementalPartition(
											incPartitionAttributeProcessStrategy.initiate(
													new IntegerArrayIterator(attributeGroup)
											), 
											equClasses
									);
						// if |0-REC|=0, attributes of begin~end are NOT core attributes.
						if (partitionResult.isEmptyBoundaryClassSetTypeClass()) {
							TimerUtils.timePause((TimeCounted) component);

							examAttributes = new int[attributesLength-attributeGroup.length];
							attributeIsCore = new boolean[examAttributes.length];
							for (int i=0; i<examAttributes.length-1; i++)
								examAttributes[i] = coreAttributeProcessStrategy.getExamingLineAttr()[i];
							examAttributes[examAttributes.length-1] = coreAttributeProcessStrategy.getExamingAttr();
							
							TimerUtils.timeContinue((TimeCounted) component);

							// update pointers begin and end.
							coreAttributeProcessStrategy.skipExamAttributes();
							coreAttributeProcessStrategy.updateInLineAttrs();
						// else continue to partition for checking attributes within groups.
						}else {
							// Remain 0-RECs only for further partitioning.
							shrinkInstance.shrink(partitionResult.getRoughClasses());

							TimerUtils.timePause((TimeCounted) component);
							int i=0;
							examAttributes = new int[attributesLength-attributeGroup.length];
							attributeIsCore = new boolean[examAttributes.length];
							TimerUtils.timeContinue((TimeCounted) component);

							// Loop over attributes in coreAttributeProcessStrategy.
							int attr;
							int[] examingLineAttr;
							int hashKeyCapacity = equClasses.size();
							while (coreAttributeProcessStrategy.hasNextExamAttribute()) {
								// next attribute.
								attr=coreAttributeProcessStrategy.getExamingAttr();
								// Use the rest of the attributes to do partitioning
								examingLineAttr = coreAttributeProcessStrategy.getExamingLineAttr();
								
								TimerUtils.timePause((TimeCounted) component);
								examAttributes[i] = attr;
								boundaryRoughEquSortTimes += partitionResult.getRoughClasses().size();
								TimerUtils.timeContinue((TimeCounted) component);
								
								for (RoughEquivalenceClassDummy roughClass: partitionResult.getRoughClasses()) {
									// if 0-REC exists.
									if (RoughEquivalenceClassBasedExtensionAlgorithm
											.IncrementalPartition
											.Basic
											.boundarySensitiveRoughEquivalenceClass(
												roughClass.getItems(),
												new IntegerArrayIterator(examingLineAttr),
												hashKeyCapacity
											)==null
									) {
										TimerUtils.timePause((TimeCounted) component);
										attributeIsCore[i] = true;
										TimerUtils.timeContinue((TimeCounted) component);
										
										// core = core U {a}.
										core.add(attr);
										break;
									}
								}
								// prepare for the next attribute to be checked.
								coreAttributeProcessStrategy.updateExamAttribute();
							}
							// prepare for the next attribute to be checked.
							coreAttributeProcessStrategy.updateInLineAttrs();
						}
						return new Object[] {
							core,
							partitionResult.isEmptyBoundaryClassSetTypeClass(),
							attributesLength - inLineSize,
							examAttributes,
							attributeIsCore,
							boundaryRoughEquSortTimes
						};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> core = (Collection<Integer>) result[r++];
						boolean boundarySkip = (boolean) result[r++];
						int examSize = (int) result[r++];
						int[] examAttributes = (int[]) result[r++];
						boolean[] attributeIsCore = (boolean[]) result[r++];
						/* ------------------------------------------------------------------------------ */
						localParameters.put("core", core);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						//	[STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH]
						Collection<Integer> examSizeRecords = (Collection<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH);
						if (examSizeRecords==null)	statistics.put(StatisticsConstants.Procedure.STATISTIC_CORE_ATTRIBUTE_EXAMED_LENGTH, examSizeRecords=new LinkedList<>());
						examSizeRecords.add(examSize);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						reportKeys.add(reportMark);
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, reportMark, 
								((Collection<?>) getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								((Collection<?>) getParameters().get("equClasses")).size(), 
								core.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report,
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_CORE_4_IDREC_0_REC_DIRECT]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_CORE_4_IDREC_0_REC_DIRECT,
								boundarySkip
						);
						//	[REPORT_CORE_CURRENT_ATTRIBUTE]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_CORE_CURRENT_ATTRIBUTE,
								examAttributes
						);
						//	[REPORT_CORE_CURRENT_ATTRIBUTE]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_CORE_INDIVIDUAL_RESULT,
								attributeIsCore
						);
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
																	
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Core loop"),
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

	private String reportMark() {
		return "Loop ["+localParameters.get("loop")+"]";
	}
}