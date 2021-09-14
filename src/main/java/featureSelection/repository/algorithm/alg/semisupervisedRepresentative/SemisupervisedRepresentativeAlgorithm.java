package featureSelection.repository.algorithm.alg.semisupervisedRepresentative;

import featureSelection.basic.lang.dataStructure.IntArrayKey;
import featureSelection.basic.lang.dataStructure.interf.IntegerIterator;
import featureSelection.basic.model.universe.instance.Instance;
import featureSelection.repository.algorithm.alg.classic.ClassicAttributeReductionHashMapAlgorithm;
import featureSelection.repository.entity.alg.semisupervisedRepresentative.graph.DirectedAcyclicGraghWeightedEdge;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

/**
 * Algorithm repository of Semi-supervised Representative Feature Selection, which bases on the
 * paper <a href="https://linkinghub.elsevier.com/retrieve/pii/S0031320316302242">
 * "An efficient semi-supervised representatives feature selection algorithm based on information
 * theory"</a> by Yintong Wang, Jiandong Wang, Hao Liao, Haiyan Chen.
 * 
 * @author Benjamin_L
 */
public class SemisupervisedRepresentativeAlgorithm {
	
	/**
	 * Basic algorithms for Semi-supervised Representative Feature Selection. (SRFS)
	 * 
	 * @author Benjamin_L
	 */
	public static class Basic {
		/**
		 * Calculate the Equivalence Classes induced by given attributes.
		 * 
		 * @see ClassicAttributeReductionHashMapAlgorithm.Basic#equivalenceClass(Collection,
		 * 		IntegerIterator)
		 * 
		 * @param instances
		 * 		A {@link Collection} of {@link Instance}s.
		 * @param attributes
		 * 		Attributes of {@link Instance}. (starts from 1, 0 as decision attribute)
		 * @return A {@link Map} of Equivalence Classes with {@link Instance}s in {@link Collection}s.
		 */
		public static Map<IntArrayKey, Collection<Instance>> equivalenceClass(
				Collection<Instance> instances, IntegerIterator attributes
		){
			return ClassicAttributeReductionHashMapAlgorithm
					.Basic
					.equivalenceClass(instances, attributes);
		}
	}
	
	/**
	 * Execute sorting on <strong>Relevant Features</strong> in descending order by <i>F1-Relevance</i>.
	 *
	 * @param relevantFeatureF1Rels
	 * 		A {@link Map} whose keys are relevant features and values are the corresponding
	 * 		F1-Relevance values.
	 * @return relevance features in sorted <code>Integer[]</code>.
	 */
	public static Integer[] descendingSortRelevantFeatureByF1Relevance(
			Map<Integer, Double> relevantFeatureF1Rels
	) {
		return relevantFeatureF1Rels.keySet().stream()
				.sorted(
					// sort features by F1-Relevance in descending order
					(f1, f2)-> - Double.compare(
						relevantFeatureF1Rels.get(f1).doubleValue(), 
						relevantFeatureF1Rels.get(f2).doubleValue()
					)
				).toArray(Integer[]::new);
	}

	/**
	 * Obtain sub-graphs of the given graph.
	 * <p>
	 * For sub-graph, nodes are connected/grouped by pointing at or being pointed at by another one.
	 * <p>
	 * For nodes are not connected, they belong to different sub-graphs respectfully.
	 * 
	 * @see #searchGroupMembersByDFS(Collection[], Collection, Collection, boolean[], int)
	 * 
	 * @param dagEdges
	 * 		Directed Acyclic Graph edges stored in {@link Collection}s. Edges of the
	 * 		<code>i</code>th node are stored in the <code>i</code>th {@link Collection}.
	 * @return Sub-graphs of the given graph whose elements are the node index ranging from 0 to
	 * 		|node|-1.
	 */
	public static Collection<Collection<Integer>> subGraphOf(
			Collection<DirectedAcyclicGraghWeightedEdge<Integer, Double>>[] dagEdges
	) {
		Collection<Collection<Integer>> groupCollector = new LinkedList<>();
		boolean[] searched = new boolean[dagEdges.length];
		for (int i=0; i<dagEdges.length; i++) {
			if (!searched[i]) {
				// group nodes and search for members.
				searchGroupMembersByDFS(dagEdges, groupCollector, null, searched, i);
			}else {
				// already group and search the node, skip.
				continue;
			}
		}
		return groupCollector;
	}
	
	/**
	 * Search group members of <code>currentGroup</code> using <strong>Depth First Search</strong>.
	 * 
	 * @param dagEdges
	 * 		Edges of a directed graph in {@link Map} array.
	 * @param collector
	 * 		A collector to collect groups in {@link Collection}.
	 * @param currentGroup
	 * 		Current group in {@link Collection} with indexes as elements and group members. / 
	 * 		<code>null</code> to initiate a new group and the searching.
	 * @param searched
	 * 		<code>boolean[]</code> to mark nodes being searched already with a length of
	 * 		|<code>dagEdges</code>|.
	 * @param nodeIndex
	 * 		Current node index.
	 */
	private static void searchGroupMembersByDFS(
			Collection<DirectedAcyclicGraghWeightedEdge<Integer, Double>>[] dagEdges, 
			Collection<Collection<Integer>> collector, Collection<Integer> currentGroup, 
			boolean[] searched, int nodeIndex
	) {
		// mark node grouped.
		if (!searched[nodeIndex]){
			searched[nodeIndex] = true;
		}
		// Initiate group if needed.
		if (currentGroup==null) {
			collector.add(currentGroup = new HashSet<>());
			// The one who points at the others are at the same group with points being pointing at.
			currentGroup.add(nodeIndex);
		}
		// nodes being pointed at is at the same group as the one who points at.
		for (DirectedAcyclicGraghWeightedEdge<Integer, Double> edge: dagEdges[nodeIndex]) {
			// if already group and search the node, skip.
			if (searched[edge.getToNode()]) {
				continue;
			}else {
				// add index(being pointed at) into group.
				if (currentGroup.add(edge.getToNode())) {
					// succeeded in adding index, continue to add points being pointed by it.
					searchGroupMembersByDFS(
							dagEdges, collector, currentGroup, searched, edge.getToNode()
					);
				}
			}
		}
	}
}