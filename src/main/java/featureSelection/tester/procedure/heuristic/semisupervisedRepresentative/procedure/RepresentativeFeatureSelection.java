package featureSelection.tester.procedure.heuristic.semisupervisedRepresentative.procedure;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import common.utils.LoggerUtil;
import featureSelection.basic.procedure.ProcedureComponent;
import featureSelection.basic.procedure.component.TimeCountedProcedureComponent;
import featureSelection.basic.procedure.container.DefaultProcedureContainer;
import featureSelection.basic.procedure.parameter.ProcedureParameters;
import featureSelection.basic.procedure.report.ReportMapGenerated;
import featureSelection.basic.procedure.statistics.Statistics;
import featureSelection.basic.procedure.statistics.StatisticsCalculated;
import featureSelection.basic.procedure.timer.TimeCounted;
import featureSelection.basic.procedure.timer.TimerUtils;
import featureSelection.repository.algorithm.alg.semisupervisedRepresentative.SemisupervisedRepresentativeAlgorithm;
import featureSelection.repository.entity.alg.semisupervisedRepresentative.graph.DirectedAcyclicGraghWeightedEdge;
import featureSelection.tester.procedure.ComponentTags;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

/**
 * Representative Feature Selection for <strong>Semi-supervised Representative Feature
 * Selection</strong>. This procedure contains 3 ProcedureComponents:
 * <ul>
 * 	<li>
 * 		<strong>Representative selection controller</strong>
 * 		<p>Control loop over features in each generated sub-graph of Directed Acyclic
 * 			Graph(DAG), and select the one with max. F1-Rel value as the representative
 * 			of the sub-graph(group) according to the original paper
 * 			<a href="https://linkinghub.elsevier.com/retrieve/pii/S0031320316302242">
 * 			"An efficient semi-supervised representatives feature selection algorithm
 * 			based on information theory"</a>, and finally as an attribute of the reduct.
 * 	</li>
 * 	<li>
 * 		<strong>Subgraph generation</strong>
 * 		<p>Generate sub-graphs of the Directed Acyclic Graph(DAG). Nodes connected to
 * 			each other are at the same sub-graph(group). Using Deep First Search(DFS)
 * 			strategy when searching.
 * 	</li>
 * 	<li>
 * 		<strong>Representative selection</strong>
 * 		<p>Select the select nodes with max. F1-Rel value as the representatives of the
 * 			sub-graphs(groups).
 * 	</li>
 * </ul>
 * 
 * @author Benjamin_L
 */
@Slf4j
public class RepresentativeFeatureSelection 
	extends DefaultProcedureContainer<int[]>
	implements StatisticsCalculated,
				ReportMapGenerated<String, Map<String, Object>>
{
	private boolean logOn;
	@Getter private Statistics statistics;
	@Getter private Map<String, Map<String, Object>> report;
	private Collection<String> reportKeys;
	
	private Map<String, Object> localParameters;
	
	public RepresentativeFeatureSelection(ProcedureParameters parameters, boolean logOn) {
		super(logOn? log: null, parameters);
		this.logOn = logOn;
		statistics = new Statistics();
		report = new HashMap<>();

		reportKeys = new LinkedList<>();
		localParameters = new HashMap<>();
	}

	@Override
	public String shortName() {
		return "Representative Feature Selection(SRFS)";
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
			// 1. Representative selection controller
			new TimeCountedProcedureComponent<int[]>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
//						if (logOn)	log.info("1. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("relevantFeatures"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Integer[] relevantFeatures = (Integer[]) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						ProcedureComponent<?> comp1 = getComponents().get(1);
						ProcedureComponent<?> comp2 = getComponents().get(2);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timePause((TimeCounted) component);
						Collection<Collection<Integer>> subGraphs =
								(Collection<Collection<Integer>>) comp1.exec();
						TimerUtils.timeContinue((TimeCounted) component);
						
						// 17: S = NULL
						int[] representatives = new int[subGraphs.size()];
						// 18: for each subgraph G[i] in DAG do:
						int repIndex = 0;
						for (Collection<Integer> subgraph: subGraphs) {
							// 19: Rep = arg<sub>F<sub>j</sub>&isin;G<sub>i</sub></sub> maxF1_Rel(F<sub>j</sub>, C)
							//		i.e. Rep = F[m] where F1_Rel(F[m]) = max(F1_Rel({G[i]})) and F[i], F[j] in G[i]
							// 20: S = S U {Rep)
							TimerUtils.timePause((TimeCounted) component);
							localParameters.put("subgraph", subgraph);
							representatives[repIndex++] = relevantFeatures[((Integer) comp2.exec()).intValue()];
							TimerUtils.timeContinue((TimeCounted) component);
						}
						/* ------------------------------------------------------------------------------ */
						return representatives;
					}, 
					(component, relevantFeatureF1Rels) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("relevantFeatureF1Rels", relevantFeatureF1Rels);
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 1. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Representative selection controller"),
			// 2. Subgraph generation.
			new TimeCountedProcedureComponent<Collection<Collection<Integer>>>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
//						if (logOn)	log.info("2. "+component.getDescription());
						component.setLocalParameters(new Object[] {
								getParameters().get("dagEdges"),
						});
					}, 
					false,
					(component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Collection<DirectedAcyclicGraghWeightedEdge<Integer, Double>>[] dagEdges =
								(Collection<DirectedAcyclicGraghWeightedEdge<Integer, Double>>[])
								parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return SemisupervisedRepresentativeAlgorithm.subGraphOf(dagEdges);
					}, 
					(component, subGraphs) -> {
						/* ------------------------------------------------------------------------------ */
						getParameters().setNonRoot("subGraphs", subGraphs);
						if (logOn) {
							log.info(
									LoggerUtil.spaceFormat(1, "x {} sub-graph(s) in total."),
									subGraphs.size()
							);
//							int index = 1;
//							for (Collection<Integer> subgraph: subGraphs) {
//								log.info(LoggerUtil.spaceFormat(1, "sub graph {} size = {}"), index++, subgraph.size());
//							}
						}
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 2. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Subgraph generation"),
			// 3. Representative selection.
			new TimeCountedProcedureComponent<Integer>(
					ComponentTags.TAG_SIG,
					this.getParameters(), 
					(component) -> {
//						if (logOn)	log.info("3. "+component.getDescription());
						component.setLocalParameters(new Object[] {
							getParameters().get("relevantFeatures"),
							getParameters().get("relevantFeatureF1Rels"),
							localParameters.get("subgraph"),
						});
					}, 
					false, (component, parameters) -> {
						/* ------------------------------------------------------------------------------ */
						int p=0;
						Integer[] relevantFeatures =
								(Integer[]) parameters[p++];
						Map<Integer, Double> relevantFeatureF1Rels =
								(Map<Integer, Double>) parameters[p++];
						Collection<Integer> subgraph =
								(Collection<Integer>) parameters[p++];
						/* ------------------------------------------------------------------------------ */
						TimerUtils.timeStart((TimeCounted) component);
						/* ------------------------------------------------------------------------------ */
						return subgraph.stream().max((f1, f2)->{
									double f1Rel = relevantFeatureF1Rels.get(relevantFeatures[f1]);
									double f2Rel = relevantFeatureF1Rels.get(relevantFeatures[f2]);
									return Double.compare(f1Rel, f2Rel);
								}).get().intValue();
					}, 
					(component, representative) -> {
						/* ------------------------------------------------------------------------------ */
						// Statistics
						/* ------------------------------------------------------------------------------ */
						// Report
						/* ------------------------------------------------------------------------------ */
					}
				) {
					@Override public void init() {}
								
					@Override public String staticsName() {
						return shortName()+" | 3. of "+getComponents().size()+"."+" "+getDescription();
					}
				}.setDescription("Representative selection"),
		};
	}

	@Override
	public String[] getReportMapKeyOrder() {
		return reportKeys.toArray(new String[reportKeys.size()]);
	}

	@Override
	public int[] exec() throws Exception {
		ProcedureComponent<?>[] comps = initComponents();
		for (ProcedureComponent<?> each: comps)	this.getComponents().add(each);
		return (int[]) comps[0].exec();
	}
}