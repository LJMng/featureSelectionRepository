package featureSelection.tester.procedure.heuristic.activeSampleSelection.procedure;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.lang.dataStructure.IntArrayKey;
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
import featureSelection.tester.procedure.ComponentTags;
import featureSelection.tester.procedure.param.ParameterConstants;
import featureSelection.tester.statistics.StatisticsConstants;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Reduct inspection for <strong>Sample Pair Selection</strong> for Attribute Reduction(
 * for <strong> Static Data</strong>)
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
 * 		<p>Check the redundancy of an attribute by calculating its inner significance. Considered redundant if 
 * 			the inner significance is 0(i.e. sig(red)=sig(red-{a})).
 * 	</li>
 * </ul>
 * In this {@link ProcedureContainer}(only!), the following parameters are used in
 * {@link #getParameters()}:
 * <ul>
 *  <li>[preset] {@link ParameterConstants#PARAMETER_PREVIOUS_REDUCT}</li>
 * 	<li>[preset] {@link ParameterConstants#PARAMETER_REDUCT_LIST}</li>
 * 	<li>inspectAttributes</li>
 * 	<li>minimalElements</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class ReductInspectionProcedureContainer4MinimalElements
	extends DefaultProcedureContainer<Collection<Integer>>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private List<String> reportKeys;
	
	public ReductInspectionProcedureContainer4MinimalElements(
			ProcedureParameters parameters, boolean logOn
	) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		
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
			// 1. Inspection
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
								getParameters().get(ParameterConstants.PARAMETER_REDUCT_LIST),
								getParameters().get(ParameterConstants.PARAMETER_PREVIOUS_REDUCT),
								getParameters().get("inspectAttributes"),
								getParameters().get("minimalElements"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<Integer> newReduct =
								(Collection<Integer>) parameters[p++];
						Collection<Integer> previousReduct =
								(Collection<Integer>) parameters[p++];
						int[] inspectAttributes =
								(int[]) parameters[p++];
						Collection<IntArrayKey> minimalElements =
								(Collection<IntArrayKey>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						// red = red<sub>x</sub>
						//  if B(<code>newReduct</code>) is null
						//  else red<sub>x</sub> ∪ B.
						Collection<Integer> reduct;
						if (newReduct==null || newReduct.isEmpty()) {
							reduct = previousReduct==null?
										Collections.EMPTY_SET:
										new HashSet<>(previousReduct);
						}else if (previousReduct==null || previousReduct.size()==0){
							reduct = new HashSet<>(newReduct);
						}else {
							reduct = new HashSet<>(previousReduct.size()+newReduct.size());
							reduct.addAll(previousReduct);
							reduct.addAll(newReduct);
						}
						// Loop over <code>inspectAttributes</code> and check
						//  redundant attributes.
						LoopA:
						for (int attr: inspectAttributes) {
							// Get red - {a}
							if (!reduct.remove(attr)) {
								throw new IllegalStateException(
										"Fail to remove attribute " + attr +
										" in reduct: " + reduct
								);
							}
							// Loop over minimal elements in ME
							//	Check if red - {a} ∩ c' is null, where c' is a minimal
							//   element in ME'.
							//	If all of them(c') is NOT null: remove {a} 
							for (IntArrayKey me: minimalElements) {
								if (ActiveSampleSelectionAlgorithm
										.Basic
										.Mathematicals
										.intersectionOf(me.getKey(), reduct)
										.isEmpty()
								) {
									// do not remove {a}
									reduct.add(attr);
									continue LoopA;
								}
							}
							// remove {a}
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
						/* ------------------------------------------------------------------------------ */
					}
				){
					@Override public void init() {}
				
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Inspection"),
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
}