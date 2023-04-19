package com.example.tsp.tactical;

import com.example.tsp.model.City;
import com.example.tsp.model.Edge;
import org.jgrapht.Graph;
import org.jgrapht.alg.matching.blossom.v5.KolmogorovMinimumWeightPerfectMatching;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;

import java.util.*;

public class ChristofidesTSP {

    public static double totalWeight = 0.0;

    public static List<City> optimize(List<City> cities) {
        if (cities.size() < 2) {
            return new ArrayList<>();
        }
        // Step 1: Create a minimum spanning tree (MST) for the given graph (cities).
        List<Edge> mstEdges = createMinimumSpanningTree(cities);
        System.out.println("MST: " + mstEdges.size());

        // Step 2: Find the set of vertices (cities) with odd degrees in the MST.
        List<City> oddDegreeVertices = findOddDegreeVertices(cities, mstEdges);
        System.out.println("Odd degree vertices: " + oddDegreeVertices.size());

        // Step 3: Find the minimum-weight perfect matching (MWPM) of the odd-degree vertices.
        List<Edge> mwpmEdges = findMinimumWeightPerfectMatching(oddDegreeVertices);
        System.out.println("MWPM: " + mwpmEdges.size());

        // Step 4: Combine the edges of the MST and the MWPM to form a multigraph.
        List<Edge> combinedEdges = combineMSTAndMWPM(mstEdges, mwpmEdges);
        System.out.println("Combined edges: " + combinedEdges.size());

        // Step 5: Find an Eulerian circuit (a closed loop visiting every edge exactly once) in the multigraph.
        List<City> eulerianCircuit = findEulerianCircuit(combinedEdges);
        System.out.println("Eulerian circuit: " + eulerianCircuit.size());


        // Step 6: Convert the Eulerian circuit into a Hamiltonian cycle (a closed loop visiting every vertex exactly once) by skipping visited vertices.
        List<City> hamiltonianCycle = convertEulerianToHamiltonian(eulerianCircuit);
        System.out.println("Hamiltonian cycle: " + hamiltonianCycle.size());

        // Return the list of cities in the order they appear in the Hamiltonian cycle
        return hamiltonianCycle;
    }

    private static City findParent(City city, Map<City, City> parentMap) {
        if (!parentMap.get(city).equals(city)) {
            parentMap.put(city, findParent(parentMap.get(city), parentMap));
        }
        return parentMap.get(city);
    }

    private static List<Edge> createMinimumSpanningTree(List<City> cities) {
        List<Edge> mstEdges = new ArrayList<>();
        List<Edge> allEdges = new ArrayList<>();
        Map<City, City> parentMap = new HashMap<>();
        totalWeight = 0.0;

        // Create a list of all edges with their weights
        for (City city1 : cities) {
            for (City city2 : cities) {
                if (!city1.equals(city2)) {
                    double weight = city1.distanceTo(city2);
                    allEdges.add(new Edge(city1, city2, weight));
                }
            }
        }

        // Sort the edges based on their weights
        Collections.sort(allEdges);

        // Initialize the parent map
        for (City city : cities) {
            parentMap.put(city, city);
        }

        // Kruskal's algorithm
        for (Edge edge : allEdges) {
            City sourceParent = findParent(edge.getSource(), parentMap);
            City targetParent = findParent(edge.getTarget(), parentMap);

            if (!sourceParent.equals(targetParent)) {
                mstEdges.add(edge);
                totalWeight += edge.getWeight();
                if (mstEdges.size() == cities.size() - 1) {
                    break;
                }
                parentMap.put(sourceParent, targetParent);
            }
        }
        System.out.println("Total weight of the MST: " + totalWeight);
        return mstEdges;
    }

    public static double calculateSolutionCost(List<City> cities) {
        double cost = 0.0;
        for (int i = 0; i < cities.size(); i++) {
            City currentCity = cities.get(i);
            City nextCity = cities.get((i + 1) % cities.size());
            cost += currentCity.distanceTo(nextCity);
        }
        return cost;
    }

    private static List<City> findOddDegreeVertices(List<City> cities, List<Edge> mstEdges) {
        Map<City, Integer> degreeMap = new HashMap<>();

        // Initialize the degree map
        for (City city : cities) {
            degreeMap.put(city, 0);
        }

        // Calculate the degree of each vertex
        for (Edge edge : mstEdges) {
            degreeMap.put(edge.getSource(), degreeMap.get(edge.getSource()) + 1);
            degreeMap.put(edge.getTarget(), degreeMap.get(edge.getTarget()) + 1);
        }

        // Find the vertices with an odd degree
        List<City> oddDegreeVertices = new ArrayList<>();
        for (Map.Entry<City, Integer> entry : degreeMap.entrySet()) {
            if (entry.getValue() % 2 != 0) {
                oddDegreeVertices.add(entry.getKey());
            }
        }

        return oddDegreeVertices;
    }

    private static List<Edge> findMinimumWeightPerfectMatching(List<City> oddDegreeVertices) {
        Graph<City, DefaultWeightedEdge> graph = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);

        // Add vertices to the graph
        for (City city : oddDegreeVertices) {
            graph.addVertex(city);
        }

        // Add weighted edges to the graph
        for (City city1 : oddDegreeVertices) {
            for (City city2 : oddDegreeVertices) {
                if (!city1.equals(city2)) {
                    double weight = city1.distanceTo(city2);
                    DefaultWeightedEdge edge = graph.addEdge(city1, city2);
                    if(edge != null) {
                        graph.setEdgeWeight(edge, weight);
                    }
                }
            }
        }

        // Find the minimum weight perfect matching using the Blossom algorithm
        KolmogorovMinimumWeightPerfectMatching<City, DefaultWeightedEdge> matchingAlgorithm =
                new KolmogorovMinimumWeightPerfectMatching<>(graph);
        Set<DefaultWeightedEdge> matchingEdges = matchingAlgorithm.getMatching().getEdges();

        // Convert the matching edges to a list of Edge objects
        List<Edge> minimumWeightPerfectMatching = new ArrayList<>();
        for (DefaultWeightedEdge edge : matchingEdges) {
            City source = graph.getEdgeSource(edge);
            City target = graph.getEdgeTarget(edge);
            double weight = graph.getEdgeWeight(edge);
            minimumWeightPerfectMatching.add(new Edge(source, target, weight));
        }

        return minimumWeightPerfectMatching;
    }

    private static List<Edge> combineMSTAndMWPM(List<Edge> mstEdges, List<Edge> mwpmEdges) {
        List<Edge> combinedEdges = new ArrayList<>(mstEdges);
        combinedEdges.addAll(mwpmEdges);
        return combinedEdges;
    }

    public static List<City> findEulerianCircuit(List<Edge> combinedEdges) {
        Map<City, List<Edge>> adjList = new HashMap<>();

        for (Edge edge : combinedEdges) {
            adjList.computeIfAbsent(edge.getSource(), k -> new ArrayList<>()).add(edge);
            adjList.computeIfAbsent(edge.getTarget(), k -> new ArrayList<>()).add(edge);
        }

        List<City> circuit = new ArrayList<>();
        Deque<City> stack = new ArrayDeque<>();
        City startCity = combinedEdges.get(0).getSource();
        stack.push(startCity);

        while (!stack.isEmpty()) {
            City currentCity = stack.peek();
            if (adjList.get(currentCity).isEmpty()) {
                circuit.add(currentCity);
                stack.pop();
            } else {
                Edge nextEdge = adjList.get(currentCity).remove(0);
                stack.push(nextEdge.getSource() == currentCity ? nextEdge.getTarget() : nextEdge.getSource());
                adjList.get(nextEdge.getSource() == currentCity ? nextEdge.getTarget() : nextEdge.getSource()).remove(nextEdge);
            }
        }

        return circuit;
    }

    private static List<City> convertEulerianToHamiltonian(List<City> eulerianCircuit) {
        List<City> hamiltonianPath = new ArrayList<>();
        Set<City> visitedCities = new HashSet<>();

        for (City city : eulerianCircuit) {
            if (visitedCities.add(city)) {
                hamiltonianPath.add(city);
            }
        }

        // Connect the last city back to the first city to form a Hamiltonian cycle
        if (!hamiltonianPath.isEmpty()) {
            hamiltonianPath.add(hamiltonianPath.get(0));
        }

        return hamiltonianPath;
    }

}

