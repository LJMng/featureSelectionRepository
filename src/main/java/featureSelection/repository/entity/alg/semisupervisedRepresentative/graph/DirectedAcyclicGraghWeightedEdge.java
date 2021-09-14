package featureSelection.repository.entity.alg.semisupervisedRepresentative.graph;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * An entity for implementation of weighted edge for Directed Acyclic Graph algorithm.
 *
 * @param <Node>
 *     Type of graph node.
 * @param <W>
 *     Type of edge weight.
 */
@Data
@AllArgsConstructor
public class DirectedAcyclicGraghWeightedEdge<Node, W> {
	private Node toNode;
	private W weight;
}
