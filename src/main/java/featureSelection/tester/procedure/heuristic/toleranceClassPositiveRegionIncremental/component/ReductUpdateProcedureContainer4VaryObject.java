package featureSelection.tester.procedure.heuristic.toleranceClassPositiveRegionIncremental.component;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.impl.integerIterator.IntegerArrayIterator;
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
import featureSelection.repository.algorithm.alg.toleranceClassPositiveRegionIncremental.ToleranceClassPositiveRegionAlgorithm;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.MostSignificantAttributeResult;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.ReductCandidateResult4VariantObjects;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.instanceGroup.InstancesCollector;
import featureSelection.repository.entity.alg.toleranceClassPositiveRegionIncremental.toleranceClassObtainer.ToleranceClassObtainer;
import featureSelection.repository.support.calculation.alg.FeatureImportance4ToleranceClassPositiveRegionIncremental;
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.report.ReportConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import featureSelection.tester.utils.ProcedureUtils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct updates for <strong>Tolerance Class Positive Region Incremental (TCPR, Dynamic, (FSMV) Multiple 
 * Object vary feature values)</strong>.
 * This procedure contains 2 ProcedureComponents: 
 * <ul>
 * 	<li><strong>Reduct update controller</strong>
 * 		<p>Control to loop over to seek the most significant attributes.
 * 	</li>
 * 	<li><strong>Attribute sorting based on significance</strong>
 * 		<p>Sort attributes in descending order based on their significances: For any feature c in C-B, 
 * 			construct a descending sequence by sig<sub>2</sub>(c, B, D) and record the result by 
 * 			{ c'<sub>1</sub>, c'<sub>2</sub>, ..., c'<sub>|C-B|</sub>	}; 
 * 	</li>
 * 	<li><strong>Most significant attribute seeking</strong>
 * 		<p>Search for the attribute with the max. significance value in the rest of the attributes(i.e. 
 * 			attributes outside of the reduct), and add into reduct.
 * 	</li>
 * </ul>
 * <p>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ReductUpdateProcedureContainer4VaryObject<Sig extends Number>
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	private int loopCount;
	
	private Map<String, Object> localParameters;
	
	public ReductUpdateProcedureContainer4VaryObject(ProcedureParameters parameters, boolean logOn) {
		super(parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();
		reportKeys = new LinkedList<>();
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Vary-object Reduct update";
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
			// 1. Reduct update controller
			new TimeCountedProcedureComponent<Collection<Integer>>(
					ComponentTags.TAG_CHECK,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "1/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("newUniverseInstances"),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4AttributesOfNewUniverseInstances"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						FeatureImportance4ToleranceClassPositiveRegionIncremental calculation =
								(FeatureImportance4ToleranceClassPositiveRegionIncremental) 
								parameters[p++];
						Collection<Instance> newInstances =
								(Collection<Instance>) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4AttributesOfNewUniverseInstances =
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<Integer> comp1 = (ProcedureComponent<Integer>) getComponents().get(1);
						ProcedureComponent<?> comp2 = getComponents().get(2);
						ProcedureComponent<MostSignificantAttributeResult> comp3 = (ProcedureComponent<MostSignificantAttributeResult>) getComponents().get(3);
						/* ------------------------------------------------------------------------------ */
						int posOfAttributes = comp1.exec();
						// Sort attributes
						comp2.exec();
						Collection<Integer> newReduct = getParameters().get("newReduct");
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// Initialise reduct significance.
						int posOfReduct = 
							calculation.calculate(
								toleranceClassObtainer.obtain(
									newInstances, newInstances, new IntegerCollectionIterator(newReduct),
									completeData4AttributesOfNewUniverseInstances
								).entrySet()
							).getResult();
						
						while (posOfReduct!=posOfAttributes) {
							TimerUtils.timePause((TimeCounted) component);
							loopCount++;
							MostSignificantAttributeResult sigResult = comp3.exec();
							TimerUtils.timeContinue((TimeCounted) component);
							
							newReduct.add(sigResult.getAttribute());
							posOfReduct = sigResult.getPositiveRegion();
						}						
							
						return newReduct;
					}, 
					(component, reduct) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot(ParameterConstants.PARAMETER_REDUCT_LIST, reduct);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(1, "|Reduct Candidate| = {}"), reduct.size());
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						reportKeys.add(component.getDescription());
						//	[DatasetRealTimeInfo]
						ProcedureUtils.Report.DatasetRealTimeInfo.save(
								report, component.getDescription(), 
								((Collection<?>)getParameters().get(ParameterConstants.PARAMETER_UNIVERSE_INSTANCES)).size(), 
								0, 
								reduct.size()
						);
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection procedure controller"),
			// 2. Global positive region calculation
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "2/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("newUniverseInstances"),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4AttributesOfNewUniverseInstances"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						int[] attributes =
								(int[]) parameters[p++];
						FeatureImportance4ToleranceClassPositiveRegionIncremental calculation = 
								(FeatureImportance4ToleranceClassPositiveRegionIncremental) 
								parameters[p++];
						Collection<Instance> newInstances =
								(Collection<Instance>) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4AttributesOfNewUniverseInstances = 
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// For any feature c in C-B, construct a descending sequence by sig<sub>2</sub>(c, B, D) and 
						//	record the result by { c'<sub>1</sub>, c'<sub>2</sub>, ..., c'<sub>|C-B|</sub>	}; 
						// ---------------------------------------------------------------------------------------------
						// | where sig<sub>2</sub>(a, B, D) = POS<sub>B∪{a}</sub>(D)-POS<sub>B</sub>(D) 			   |
						// ---------------------------------------------------------------------------------------------
						int posOfAttributes = 
								calculation.calculate(
									toleranceClassObtainer.obtain(
										newInstances, newInstances, new IntegerArrayIterator(attributes),
										completeData4AttributesOfNewUniverseInstances
									).entrySet()
								).getResult();
						return posOfAttributes;
					}, 
					(component, posOfAttributes) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("globalPos", posOfAttributes);
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "Global pos = {}"), posOfAttributes);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Global positive region calculation"),
			// 3. Attribute sorting based on significance
			new TimeCountedProcedureComponent<Collection<Integer>[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "3/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get(ParameterConstants.PARAMETER_ATTRIBUTES),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("newUniverseInstances"),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4AttributesOfNewUniverseInstances"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						int[] attributes =
								(int[]) parameters[p++];
						FeatureImportance4ToleranceClassPositiveRegionIncremental calculation = 
								(FeatureImportance4ToleranceClassPositiveRegionIncremental) 
								parameters[p++];
						Collection<Instance> newInstances =
								(Collection<Instance>) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer =
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4AttributesOfNewUniverseInstances = 
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// For any feature c in C-B, construct a descending sequence by sig<sub>2</sub>(c, B, D) and 
						//	record the result by { c'<sub>1</sub>, c'<sub>2</sub>, ..., c'<sub>|C-B|</sub>	}; 
						// ---------------------------------------------------------------------------------------------
						// | where sig<sub>2</sub>(a, B, D) = POS<sub>B∪{a}</sub>(D)-POS<sub>B</sub>(D) 			   |
						// ---------------------------------------------------------------------------------------------
						ReductCandidateResult4VariantObjects sortedResult =
							ToleranceClassPositiveRegionAlgorithm
								.VariantObjects
								.descendingSequenceSortedAttributes(
									newInstances, previousReduct, attributes,
									toleranceClassObtainer,
									completeData4AttributesOfNewUniverseInstances,
									calculation
								);
						Collection<Integer> sortedReductCandidates = sortedResult.getReductCandidate();
						Collection<Integer> newReduct = sortedResult.getPreviousReductHash();
						return new Collection[] { sortedReductCandidates, newReduct	};
					}, 
					(component, result) -> {
						/* ------------------------------------------------------------------------------ */
						int r=0;
						Collection<Integer> sortedReductCandidates = result[r++];
						Collection<Integer> newReduct = result[r++];
						/* ------------------------------------------------------------------------------ */
						localParameters.put("sortedReductCandidates", sortedReductCandidates);
						getParameters().setNonRoot("newReduct", newReduct);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report.ExecutionTime.save(report, (TimeCountedProcedureComponent<?>) component);
						/* ------------------------------------------------------------------------------ */
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Attribute sorting based on significance"),
			// 4. Most significant attribute seeking.
			new TimeCountedProcedureComponent<MostSignificantAttributeResult>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component)->{
						if (logOn)	log.info(LoggerUtil.spaceFormat(1, "4/{}. {}"), getComponents().size(), component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("newUniverseInstances"),
								getParameters().get(ParameterConstants.PARAMETER_SIG_CALCULATION_INSTANCE),
								getParameters().get("invariants"),
								getParameters().get("changedFromInstances"),
								getParameters().get("changedToInstances"),
								getParameters().get("newReduct"),
								localParameters.get("sortedReductCandidates"),
								getParameters().get("toleranceClassObtainer"),
								getParameters().get("completeData4AttributesOfNewUniverseInstances"),
								getParameters().get("completeData4AttributesOfInvariances"),
								getParameters().get("completeData4AttributesOfChangedToInstances"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Instance> newInstances =
								(Collection<Instance>) parameters[p++];
						FeatureImportance4ToleranceClassPositiveRegionIncremental calculation = 
								(FeatureImportance4ToleranceClassPositiveRegionIncremental) 
								parameters[p++];
						Collection<Instance> invariants =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> changedFromInstances =
								(Collection<Instance>) parameters[p++];
						Collection<Instance> changedToInstances =
								(Collection<Instance>) parameters[p++];
						Collection<Integer> newReduct = (Collection<Integer>) parameters[p++];
						Collection<Integer> sortedReductCandidates = (Collection<Integer>) parameters[p++];
						ToleranceClassObtainer toleranceClassObtainer = 
								(ToleranceClassObtainer) parameters[p++];
						InstancesCollector completeData4AttributesOfNewUniverseInstances = 
								(InstancesCollector) parameters[p++];
						InstancesCollector completeData4AttributesOfInvariances = 
								(InstancesCollector) parameters[p++];
						InstancesCollector completeData4AttributesOfChangedToInstances = 
								(InstancesCollector) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// for h=1 to |C-B| do
						//	let B<-B{c'[h]} and compute POS<sub>B</sub><sup>U'</sup> by Equ.(4)
						// -----------------------------------------------------------------------------------
						// | POS<sub>B</sub><sup>U'</sup>(D) = POS<sub>B</sub><sup>U</sup>(D)				 |
						// | 									∪ POS<sub>B</sub><sup>Ux'</sup>(D)			 |
						// | 									∪ {xp | |S<sub>B</sub>(xp)/IND(D)|==1 }		 |
						// | 									- Ux										 |
						// | 									- {xi | |S'<sub>B</sub>(xi)/IND(D)|!=1 }	 |
						// | 									- {u'k | |S'<sub>B</sub>(u'k)/IND(D)|!=1 }	 |
						// | (1<=p<=r, 1<=i<=e, 1<=k<=e')													 |
						// -----------------------------------------------------------------------------------
						MostSignificantAttributeResult sigResult = 
							ToleranceClassPositiveRegionAlgorithm
								.VariantObjects
								.mostSiginificantAttribute(
									newInstances, invariants,
									changedFromInstances, changedToInstances, 
									newReduct, sortedReductCandidates, 
									toleranceClassObtainer, 
									completeData4AttributesOfNewUniverseInstances, 
									completeData4AttributesOfInvariances,
									completeData4AttributesOfChangedToInstances, 
									calculation
								);
						return sigResult;
					}, 
					(component, sigResult) -> {
						/* ------------------------------------------------------------------------------ */
						if (logOn) {
							log.info(LoggerUtil.spaceFormat(2, "Loop {} | + attribute {}, pos = {}"), 
									loopCount, sigResult.getAttribute(), sigResult.getPositiveRegion()
							);
						}
						if (sigResult.getAttribute()==-1) {
							Collection<Integer> reduct = getParameters().get("newReduct");
							throw new IllegalStateException(
								"Failed to seek the most significant attribute:  attr="+
								sigResult.getAttribute()+", |reduct|="+reduct
							);
						}
						/* ------------------------------------------------------------------------------ */
						// Statistic
						//	[STATISTIC_POS_HISTORY]
						List<Integer> increment = (List<Integer>) statistics.get(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY);
						if (increment==null)	statistics.put(StatisticsConstants.Procedure.STATISTIC_SIG_HISTORY, increment = new LinkedList<>());
						increment.add(sigResult.getPositiveRegion());
						//	[STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH]
						Collection<Integer> reduct = getParameters().get("newReduct");
						ProcedureUtils.Statistics.push(
							statistics.getData(),
							StatisticsConstants.Procedure.STATISTIC_HEURISTIC_REDUCT_SIG_CAL_ATTR_LENGTH,
							reduct.size()+1
						);
						/* ------------------------------------------------------------------------------ */
						// Report
						String reportMark = reportMark();
						//	[REPORT_EXECUTION_TIME]
						ProcedureUtils.Report
									.ExecutionTime
									.save(localParameters, 
											report, 
											reportMark, 
											(TimeCountedProcedureComponent<?>) component
						);
						//	[REPORT_POSITIVE_INCREMENT_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_SIG_HISTORY,
								sigResult.getPositiveRegion()
						);
						//	[REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY]
						ProcedureUtils.Report.saveItem(
								report, reportMark,
								ReportConstants.Procedure.REPORT_REDUCT_MAX_SIG_ATTRIBUTE_HISTORY,
								sigResult.getAttribute()
						);
						/* ------------------------------------------------------------------------------ */
						warnReportMemory();
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
							
					@Override public String staticsName() {
						return shortName()+" | 4. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Most significant attribute seeking"),
		};
	}

	@SuppressWarnings("unchecked")
	@Override
	public Collection<Integer> exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each : comps)	this.getComponents().add(each);
		return (Collection<Integer>) comps[0].exec();
	}
	
	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	private String reportMark() {
		return "Loop["+loopCount+"]";
	}

	private void warnReportMemory() {
//		if (logOn) {
//			log.warn(LoggerUtil.spaceFormat(2, "Memory: used={}, max={}, committed={}."), 
//					NumberUtils.ComputerSizes.fromByte(
//						JavaVirtualMachineUtils.Memory.usedInByte()
//					),
//					NumberUtils.ComputerSizes.fromByte(
//						JavaVirtualMachineUtils.Memory.maxInByte()
//					),
//					NumberUtils.ComputerSizes.fromByte(
//						JavaVirtualMachineUtils.Memory.committedInByte()
//					)
//			);
//		}
	}
}