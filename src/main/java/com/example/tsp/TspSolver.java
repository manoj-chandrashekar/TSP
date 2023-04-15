package com.example.tsp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.stage.Stage;

import java.util.concurrent.*;

import org.jgrapht.Graph;
import org.jgrapht.alg.matching.KuhnMunkresMinimalWeightBipartitePerfectMatching;
import org.jgrapht.graph.DefaultUndirectedWeightedGraph;
import org.jgrapht.graph.DefaultWeightedEdge;

import java.util.*;


public class TspSolver extends Application {
    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 800;

    private List<City> cities = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();

    private List<City> christofideTour = new ArrayList<>();

    private List<City> christofideTourAfter2Opt = new ArrayList<>();

    // Create a slider with a range of values from 0 to 1000 milliseconds

    Label solutionCostLabel = new Label("Solution cost: N/A");


    public void addCity(double x, double y, Pane canvas) {
        City city = new City(x, y);
        cities.add(city);

        if (cities.size() > 1) {
            City previousCity = cities.get(cities.size() - 2);
            Line line = new Line(previousCity.getX(), previousCity.getY(), x, y);
            lines.add(line);
            canvas.getChildren().add(line);
        }
    }


    public void clear(Pane canvas) {
        cities.clear();
        clearLines(canvas);
    }


    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane canvas = createCanvas();

        Button btnClear = new Button("Clear all");
        btnClear.setOnAction(e -> clearCanvas(canvas));

        Button btnChristofides = new Button("Christofides");
        btnChristofides.setOnAction(e -> christofidesAlgorithm(canvas));

        Button btn2Opt = new Button("2-Opt");
        btn2Opt.setOnAction(e -> twoOptOptimization(canvas, christofideTour));

        Button btnSimAnneal = new Button("Simulated Annealing");
        btnSimAnneal.setOnAction(e-> anneal(canvas, christofideTour));

        Button btnNN = new Button("Nearest neighbor method");
        btnNN.setOnAction(e -> nearestNeighbor(canvas));

        Button btnAntColony = new Button("Ant Colony");
        btnAntColony.setOnAction(e -> antColonyOpt(canvas, christofideTour));

        /*Button btnOpt2 = new Button("2-opt method");
        btnOpt2.setOnAction(e -> twoOpt(canvas));*/

        Button btnRandom = new Button("Generate random TSP");
        btnRandom.setOnAction(e -> {
            TextInputDialog dialog = new TextInputDialog("10");
            dialog.setTitle("Random TSP Generator");
            dialog.setHeaderText("Enter the number of cities");
            dialog.setContentText("Number of cities:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(numberOfCities -> {
                try {
                    int numCities = Integer.parseInt(numberOfCities);
                    generateRandomTsp(numCities, canvas);
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Please enter a valid number.");
                    alert.showAndWait();
                }
            });
        });


        HBox buttons = new HBox(10, btnClear, btnNN, btnChristofides, btn2Opt, btnSimAnneal, btnAntColony, btnRandom);
        buttons.setSpacing(10);

        VBox root = new VBox(10, canvas, buttons, solutionCostLabel);
        root.setSpacing(10);


        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT + 50);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TSP Solver");
        primaryStage.show();
    }

    private double calculateSolutionCost() {
        double cost = 0.0;
        for (int i = 0; i < cities.size(); i++) {
            City currentCity = cities.get(i);
            City nextCity = cities.get((i + 1) % cities.size());
            cost += currentCity.distanceTo(nextCity);
        }
        return cost;
    }

    private void generateRandomTsp(int numCities, Pane canvas) {
        clearCanvas(canvas);
        Random rand = new Random();
        for (int i = 0; i < numCities; i++) {
            double x = rand.nextDouble() * (CANVAS_WIDTH - 20);
            double y = rand.nextDouble() * (CANVAS_HEIGHT - 120);
            addCityWithoutLine(x, y, canvas);

            Circle circle = new Circle(x, y, 5, Color.BLUE);
            canvas.getChildren().add(circle);

            // Add a label for the city index
            Label cityIndexLabel = new Label(Integer.toString(cities.size() - 1));
            cityIndexLabel.setLayoutX(x + 5);
            cityIndexLabel.setLayoutY(y - 5);
            canvas.getChildren().add(cityIndexLabel);
        }

        // Initialize lines
        for (int i = 0; i < cities.size() - 1; i++) {
            City startCity = cities.get(i);
            City endCity = cities.get(i + 1);

            Line line = new Line(startCity.getX(), startCity.getY(),
                    endCity.getX(), endCity.getY());
            lines.add(line);
            canvas.getChildren().add(line);
        }

        // Connect the last city to the first one
        City firstCity = cities.get(0);
        City lastCity = cities.get(cities.size() - 1);

        Line closingLine = new Line(firstCity.getX(), firstCity.getY(),
                lastCity.getX(), lastCity.getY());
        lines.add(closingLine);
        canvas.getChildren().add(closingLine);
        solutionCostLabel.setText("Solution cost: " + String.format("%.2f", calculateSolutionCost()));
    }


    private void resetLinesColor() {
        for (Line line : lines) {
            line.setStroke(Color.BLACK);
        }
    }


    private Pane createCanvas() {
        Pane canvas = new Pane();
        canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        canvas.setStyle("-fx-background-color: white; -fx-border-color: black;");

        canvas.setOnMouseClicked(event -> {
            if (event.getButton() == MouseButton.PRIMARY) {
                double x = event.getX();
                double y = event.getY();
                City city = new City(x, y);
                cities.add(city);

                Circle circle = new Circle(x, y, 5, Color.BLUE);
                canvas.getChildren().add(circle);

                // Add a label for the city index
                Label cityIndexLabel = new Label(Integer.toString(cities.size() - 1));
                cityIndexLabel.setLayoutX(x + 5);
                cityIndexLabel.setLayoutY(y - 5);
                canvas.getChildren().add(cityIndexLabel);
            }
        });


        return canvas;
    }

    private void clearCanvas(Pane canvas) {
        cities.clear();
        lines.forEach(line -> canvas.getChildren().remove(line));
        lines.clear();

        // Remove all children (circles and labels) from the canvas except for the lines
        canvas.getChildren().removeIf(child -> !(child instanceof Line));
    }


    private void nearestNeighbor(Pane canvas) {
        if (cities.size() < 2) {
            return;
        }
        clearLines(canvas);
        resetLinesColor(); // Reset line colors

        for (int i = 0; i < cities.size() - 1; i++) {
            City currentCity = cities.get(i);
            double minDistance = Double.MAX_VALUE;
            int minIndex = i + 1;

            for (int j = i + 1; j < cities.size(); j++) {
                double distance = currentCity.distanceTo(cities.get(j));
                if (distance < minDistance) {
                    minDistance = distance;
                    minIndex = j;
                }
            }

            City tempCity = cities.get(i + 1);
            cities.set(i + 1, cities.get(minIndex));
            cities.set(minIndex, tempCity);

            Line line = new Line(currentCity.getX(), currentCity.getY(), cities.get(i + 1).getX(), cities.get(i + 1).getY());
            lines.add(line);
            canvas.getChildren().add(line);

            // Add delay between iterations
        }

        City firstCity = cities.get(0);
        City lastCity = cities.get(cities.size() - 1);

        Line line = new Line(firstCity.getX(), firstCity.getY(), lastCity.getX(), lastCity.getY());
        lines.add(line);
        canvas.getChildren().add(line);

        highlightSolution(Color.RED);
        solutionCostLabel.setText("Solution cost: " + String.format("%.2f", calculateSolutionCost()));
    }

    private void twoOpt(Pane canvas) {
        if (cities.size() < 4) {
            return;
        }

        clearLines(canvas);

        // Recreate the lines list
        for (int i = 0; i < cities.size(); i++) {
            City startCity = cities.get(i);
            City endCity = cities.get((i + 1) % cities.size());
            Line line = new Line(startCity.getX(), startCity.getY(), endCity.getX(), endCity.getY());
            lines.add(line);
            canvas.getChildren().add(line); // Add this line to the canvas
        }

        resetLinesColor();


        boolean improvement = true;
        while (improvement) {
            improvement = false;
            for (int i = 0; i < cities.size() - 1; i++) {
                for (int j = i + 2; j < cities.size() - (i == 0 ? 1 : 0); j++) {
                    if (swapIsBetter(i, j)) {
                        swap(i + 1, j);
                        updateLines(canvas, i, j);

                        improvement = true;
                    }
                }
            }
        }
        highlightSolutionTwoOpt();
        solutionCostLabel.setText("Solution cost: " + String.format("%.2f", calculateSolutionCost()));


    }

    private void christofidesAlgorithm(Pane canvas) {
        List<Edge> mst = minimumSpanningTree(cities);
        List<City> oddVertices = oddDegreeVertices(cities, mst);
        List<Edge> matching = minimumWeightPerfectMatching(oddVertices);
        List<Edge> multigraph = combineMSTAndMatching(mst, matching);
        List<City> eulerianTour = findEulerianCircuit(multigraph);
        List<City> hamiltonianTour = convertEulerianToHamiltonian(eulerianTour);

        christofideTour = new ArrayList<>(hamiltonianTour);
        displayData(canvas, hamiltonianTour, Color.AQUA);
    }

    private void displayData(Pane canvas, List<City> cities, Color color) {
        if (cities.size() < 2) {
            return;
        }
        clearLines(canvas);
        resetLinesColor();

        for (int i = 0; i < cities.size() - 1; i++) {
            City from = cities.get(i);
            City to = cities.get(i + 1);

            Line line = new Line(from.getX(), from.getY(), to.getX(), to.getY());
            lines.add(line);
            canvas.getChildren().add(line);
        }

        double cost = 0.0;
        for (int i = 0; i < cities.size(); i++) {
            City currentCity = cities.get(i);
            City nextCity = cities.get((i + 1) % cities.size());
            cost += currentCity.distanceTo(nextCity);
        }
        highlightSolution(color);
        solutionCostLabel.setText("Solution cost: " + String.format("%.2f", cost));
    }

    private boolean swapIsBetter(int i, int j) {
        City a = cities.get(i);
        City b = cities.get(i + 1);
        City c = cities.get(j);
        City d = cities.get((j + 1) % cities.size());

        double ab = a.distanceTo(b);
        double cd = c.distanceTo(d);
        double ac = a.distanceTo(c);
        double bd = b.distanceTo(d);

        return ab + cd > ac + bd;
    }

    private void swap(int i, int j) {
        while (i < j) {
            City temp = cities.get(i);
            cities.set(i, cities.get(j));
            cities.set(j, temp);
            i++;
            j--;
        }
    }

    private void highlightSolution(Color color) {
        for (Line line : lines) {
            line.setStroke(color);
        }
    }

    private void highlightSolutionTwoOpt() {
        for (Line line : lines) {
            line.setStroke(Color.FORESTGREEN);
        }
    }

    public void addCityWithoutLine(double x, double y, Pane canvas) {
        City city = new City(x, y);
        cities.add(city);
    }

    public void clearLines(Pane canvas) {
        lines.forEach(canvas.getChildren()::remove);
        lines.clear();
    }


    private void updateLines(Pane canvas, int i, int j) {
        City a = cities.get(i);
        City b = cities.get(i + 1);
        City c = cities.get(j);
        City d = cities.get((j + 1) % cities.size());

        lines.get(i).setEndX(c.getX());
        lines.get(i).setEndY(c.getY());

        lines.get(j).setStartX(b.getX());
        lines.get(j).setStartY(b.getY());

        if (i != 0 || j != cities.size() - 1) {
            for (int k = i + 1; k < j; k++) {
                Line oldLine = lines.get(k);
                canvas.getChildren().remove(oldLine);

                City startCity = cities.get(k);
                City endCity = cities.get((k + 1) % cities.size());
                Line newLine = new Line(startCity.getX(), startCity.getY(),
                        endCity.getX(), endCity.getY());
                lines.set(k, newLine);
                canvas.getChildren().add(newLine);
            }
        }
    }

    private List<Edge> minimumSpanningTree(List<City> cities) {
        List<Edge> mst = new ArrayList<>();
        Set<City> visitedCities = new HashSet<>();
        PriorityQueue<Edge> minHeap = new PriorityQueue<>();

        // Pick the first city as a starting point
        City startingCity = cities.get(0);
        visitedCities.add(startingCity);

        // Add all edges connected to the starting city to the minHeap
        for (City city : cities) {
            if (!city.equals(startingCity)) {
                minHeap.offer(new Edge(startingCity, city, startingCity.distanceTo(city)));
            }
        }

        while (!minHeap.isEmpty() && mst.size() < cities.size() - 1) {
            // Get the edge with the smallest weight
            Edge currentEdge = minHeap.poll();

            // If the target city has not been visited, add it to the MST
            if (!visitedCities.contains(currentEdge.target)) {
                mst.add(currentEdge);
                visitedCities.add(currentEdge.target);

                // Add all edges connected to the new city that are not yet in the MST to the minHeap
                for (City city : cities) {
                    if (!visitedCities.contains(city)) {
                        minHeap.offer(new Edge(currentEdge.target, city, currentEdge.target.distanceTo(city)));
                    }
                }
            }
        }

        return mst;
    }

    private List<City> oddDegreeVertices(List<City> cities, List<Edge> mst) {
        Map<City, Integer> degreeMap = new HashMap<>();

        // Initialize the degree map
        for (City city : cities) {
            degreeMap.put(city, 0);
        }

        // Count the degrees of each city in the MST
        for (Edge edge : mst) {
            degreeMap.put(edge.source, degreeMap.get(edge.source) + 1);
            degreeMap.put(edge.target, degreeMap.get(edge.target) + 1);
        }

        // Collect cities with odd degrees
        List<City> oddDegreeCities = new ArrayList<>();
        for (City city : cities) {
            if (degreeMap.get(city) % 2 != 0) {
                oddDegreeCities.add(city);
            }
        }

        return oddDegreeCities;
    }

    private List<Edge> minimumWeightPerfectMatching(List<City> oddDegreeVertices) {
        List<Edge> matching = new ArrayList<>();
        List<City> unmatchedVertices = new ArrayList<>(oddDegreeVertices);

        while (!unmatchedVertices.isEmpty()) {
            double minDistance = Double.MAX_VALUE;
            Edge minEdge = null;
            int minIndex = -1;

            for (int i = 0; i < unmatchedVertices.size(); i++) {
                City city1 = unmatchedVertices.get(i);
                for (int j = i + 1; j < unmatchedVertices.size(); j++) {
                    City city2 = unmatchedVertices.get(j);
                    double distance = city1.distanceTo(city2);
                    if (distance < minDistance) {
                        minDistance = distance;
                        minEdge = new Edge(city1, city2, distance);
                        minIndex = j;
                    }
                }
            }

            matching.add(minEdge);
            unmatchedVertices.remove(minIndex);
            unmatchedVertices.remove(0);
        }

        return matching;
    }
    /*private List<Edge> minimumWeightPerfectMatching(List<City> oddDegreeVertices) {
        Graph<City, DefaultWeightedEdge> graph = new DefaultUndirectedWeightedGraph<>(DefaultWeightedEdge.class);

        // Add vertices to the graph
        for (City city : oddDegreeVertices) {
            graph.addVertex(city);
        }

        // Add edges with weights to the graph
        for (int i = 0; i < oddDegreeVertices.size(); i++) {
            for (int j = i + 1; j < oddDegreeVertices.size(); j++) {
                City city1 = oddDegreeVertices.get(i);
                City city2 = oddDegreeVertices.get(j);
                double weight = city1.distanceTo(city2);
                DefaultWeightedEdge edge = graph.addEdge(city1, city2);
                graph.setEdgeWeight(edge, weight);
            }
        }

        // Apply the Blossom algorithm to find a minimum weight perfect matching
        KuhnMunkresMinimalWeightBipartitePerfectMatching<City, DefaultWeightedEdge> matcher =
                new KuhnMunkresMinimalWeightBipartitePerfectMatching<>(graph, new HashSet<>(oddDegreeVertices), new HashSet<>(oddDegreeVertices));
        Set<DefaultWeightedEdge> matching = matcher.getMatching().getEdges();

        // Convert the matching to a list of edges
        List<Edge> result = new ArrayList<>();
        for (DefaultWeightedEdge edge : matching) {
            City source = graph.getEdgeSource(edge);
            City target = graph.getEdgeTarget(edge);
            double weight = graph.getEdgeWeight(edge);
            result.add(new Edge(source, target, weight));
        }

        return result;
    }*/

    private List<Edge> combineMSTAndMatching(List<Edge> mst, List<Edge> matching) {
        List<Edge> multigraph = new ArrayList<>(mst);
        multigraph.addAll(matching);
        return multigraph;
    }

    private List<City> findEulerianCircuit(List<Edge> multigraph) {
        Map<City, List<City>> adjacencyList = buildAdjacencyList(multigraph);
        List<City> tour = new ArrayList<>();
        Stack<City> stack = new Stack<>();
        City startVertex = multigraph.get(0).source;

        stack.push(startVertex);
        while (!stack.isEmpty()) {
            City currentVertex = stack.peek();
            if (adjacencyList.get(currentVertex).size() > 0) {
                City nextVertex = adjacencyList.get(currentVertex).iterator().next();
                stack.push(nextVertex);
                adjacencyList.get(currentVertex).remove(nextVertex);
                adjacencyList.get(nextVertex).remove(currentVertex);
            } else {
                stack.pop();
                tour.add(currentVertex);
            }
        }
        return tour;
    }

    private Map<City, List<City>> buildAdjacencyList(List<Edge> edges) {
        Map<City, List<City>> adjacencyList = new HashMap<>();
        for (Edge edge : edges) {
            adjacencyList.computeIfAbsent(edge.source, k -> new ArrayList<>()).add(edge.target);
            adjacencyList.computeIfAbsent(edge.target, k -> new ArrayList<>()).add(edge.source);
        }
        return adjacencyList;
    }

    private List<City> convertEulerianToHamiltonian(List<City> eulerianCircuit) {
        List<City> hamiltonianCycle = new ArrayList<>();
        Set<Integer> visitedCityIds = new HashSet<>();

        for (City city : eulerianCircuit) {
            if (visitedCityIds.add(city.getId())) {
                hamiltonianCycle.add(city);
            }
        }
        hamiltonianCycle.add(eulerianCircuit.get(0)); // Close the cycle by adding the starting city
        return hamiltonianCycle;
    }

    public void twoOptOptimization(Pane canvas, List<City> tour) {
        boolean improved = true;

        while (improved) {
            improved = false;

            for (int i = 0; i < tour.size() - 2; i++) {
                for (int j = i + 2; j < tour.size() - 1; j++) {
                    double oldDistance = tour.get(i).distanceTo(tour.get(i + 1)) + tour.get(j).distanceTo(tour.get(j + 1));
                    double newDistance = tour.get(i).distanceTo(tour.get(j)) + tour.get(i + 1).distanceTo(tour.get(j + 1));

                    if (newDistance < oldDistance) {
                        reverseSubList(tour, i + 1, j);
                        improved = true;
                    }
                }
            }
        }
        christofideTourAfter2Opt = new ArrayList<>(tour);
        displayData(canvas, tour, Color.DARKMAGENTA);
    }

    private void reverseSubList(List<City> list, int start, int end) {
        while (start < end) {
            Collections.swap(list, start, end);
            start++;
            end--;
        }
    }

    public List<City> simulatedAnnealingOptimization(List<City> tour, double coolingRate, long maxExecutionTimeMillis, int maxStagnation) {
        List<City> currentTour = new ArrayList<>(tour);
        List<City> bestTour = new ArrayList<>(tour);
        double currentEnergy = calculateTourDistance(currentTour);
        double bestEnergy = currentEnergy;

        double initialTemperature = calculateInitialTemperature(currentTour);
        double temperature = initialTemperature;

        long startTime = System.currentTimeMillis();
        Random random = new Random();

        int stagnationCounter = 0;

        while (System.currentTimeMillis() - startTime < maxExecutionTimeMillis && stagnationCounter < maxStagnation) {
            int index1 = random.nextInt(tour.size());
            int index2 = random.nextInt(tour.size());

            City city1 = currentTour.get(index1);
            City city2 = currentTour.get(index2);

            double oldDistance = getDistanceBetweenAdjacentCities(currentTour, index1) +
                    getDistanceBetweenAdjacentCities(currentTour, index2);

            Collections.swap(currentTour, index1, index2);

            double newDistance = getDistanceBetweenAdjacentCities(currentTour, index1) +
                    getDistanceBetweenAdjacentCities(currentTour, index2);

            double deltaEnergy = newDistance - oldDistance;

            if (deltaEnergy < 0 || acceptanceProbability(deltaEnergy, temperature) > random.nextDouble()) {
                currentEnergy += deltaEnergy;
                if (currentEnergy < bestEnergy) {
                    bestTour = new ArrayList<>(currentTour);
                    bestEnergy = currentEnergy;
                    stagnationCounter = 0;
                } else {
                    stagnationCounter++;
                }
            } else {
                Collections.swap(currentTour, index1, index2);
                stagnationCounter++;
            }

            temperature = annealingSchedule(temperature, coolingRate, initialTemperature, System.currentTimeMillis() - startTime, maxExecutionTimeMillis);
        }

        return bestTour;
    }

    private double calculateInitialTemperature(List<City> tour) {
        double avgDistance = calculateTourDistanceSim(tour) / tour.size();
        return avgDistance / 10;
    }

    private double getDistanceBetweenAdjacentCities(List<City> tour, int index) {
        City city1 = tour.get(index);
        City city2 = tour.get((index + 1) % tour.size());
        return city1.distanceTo(city2);
    }

    private double acceptanceProbability(double energyDifference, double temperature) {
        return Math.exp(-energyDifference / temperature);
    }

    private double annealingSchedule(double temperature, double coolingRate, double initialTemperature, long elapsedTime, long maxExecutionTime) {
        return initialTemperature * Math.pow(coolingRate, elapsedTime / (double) maxExecutionTime);
    }

    private double calculateTourDistanceSim(List<City> tour) {
        double totalDistance = 0;
        for (int i = 0; i < tour.size(); i++) {
            totalDistance += getDistanceBetweenAdjacentCities(tour, i);
        }
        return totalDistance;
    }

    private void anneal(Pane canvas, List<City> tour) {
        double initialTemperature = 1000;
        double finalTemperature = 0.01;
        double coolingRate = 0.9995;
        long maxExecutionTimeMillis = 30000; // 30 seconds
        int maxStagnation = 5000;
        //List<City> bestTour = simulatedAnnealingOptimization(tour, coolingRate, maxExecutionTimeMillis, maxStagnation);
        List<City> bestTour = simAnneal(tour, initialTemperature, coolingRate);
        displayData(canvas, bestTour, Color.CHOCOLATE);
    }

    private void antColonyOpt(Pane canvas, List<City> tour) {
        int numAnts = 50;
        int numIterations = 1000;
        double alpha = 1.0;
        double beta = 5.0;
        double evaporationRate = 0.1;
        List<City> optimizedTour = antColonyOptimization(tour, numAnts, numIterations, alpha, beta, evaporationRate);
        displayData(canvas, optimizedTour, Color.INDIGO);
    }

    private double calculateTourDistance(List<City> tour) {
        double totalDistance = 0;
        for (int i = 0; i < tour.size() - 1; i++) {
            totalDistance += tour.get(i).distanceTo(tour.get(i + 1));
        }
        return totalDistance;
    }

    /*public List<City> antColonyOptimization(List<City> initialTour, int numAnts, int numIterations, double alpha, double beta, double evaporationRate) {
        Map<Integer, Integer> cityIndices = new HashMap<>();
        for (int i = 0; i < initialTour.size(); i++) {
            cityIndices.put(initialTour.get(i).getId(), i);
        }

        double[][] pheromoneLevels = initializePheromoneLevels(initialTour.size());
        double[][] distances = calculateDistances(initialTour);

        List<City> bestTour = new ArrayList<>(initialTour);
        double bestTourDistance = calculateTourDistance(initialTour);

        for (int iteration = 0; iteration < numIterations; iteration++) {
            List<List<City>> antTours = new ArrayList<>();

            for (int i = 0; i < numAnts; i++) {
                List<City> antTour = constructAntTour(initialTour, distances, pheromoneLevels, alpha, beta, cityIndices);
                antTours.add(antTour);
                double antTourDistance = calculateTourDistance(antTour);

                if (antTourDistance < bestTourDistance) {
                    bestTour = new ArrayList<>(antTour);
                    bestTourDistance = antTourDistance;
                }
            }

            updatePheromoneLevels(pheromoneLevels, antTours, evaporationRate, cityIndices);
        }

        return bestTour;
    }*/

    public List<City> antColonyOptimization(List<City> initialTour, int numAnts, int numIterations, double alpha, double beta, double evaporationRate) {
        Map<Integer, Integer> cityIndices = new HashMap<>();
        for (int i = 0; i < initialTour.size(); i++) {
            cityIndices.put(initialTour.get(i).getId(), i);
        }

        double[][] pheromoneLevels = initializePheromoneLevels(initialTour.size());
        double[][] distances = calculateDistances(initialTour);

        List<City> bestTour = new ArrayList<>(initialTour);
        double bestTourDistance = calculateTourDistance(initialTour);

        ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());

        for (int iteration = 0; iteration < numIterations; iteration++) {
            List<Future<List<City>>> antTourFutures = new ArrayList<>();

            for (int i = 0; i < numAnts; i++) {
                Callable<List<City>> callable = () -> constructAntTour(initialTour, distances, pheromoneLevels, alpha, beta, cityIndices);
                Future<List<City>> future = executor.submit(callable);
                antTourFutures.add(future);
            }

            List<List<City>> antTours = new ArrayList<>();
            for (Future<List<City>> future : antTourFutures) {
                try {
                    List<City> antTour = future.get();
                    antTours.add(antTour);
                    double antTourDistance = calculateTourDistance(antTour);

                    if (antTourDistance < bestTourDistance) {
                        bestTour = new ArrayList<>(antTour);
                        bestTourDistance = antTourDistance;
                    }
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }

            updatePheromoneLevels(pheromoneLevels, antTours, evaporationRate, cityIndices);
        }

        executor.shutdown();
        return bestTour;
    }

    private double[][] initializePheromoneLevels(int numCities) {
        double[][] pheromoneLevels = new double[numCities][numCities];
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                pheromoneLevels[i][j] = 1.0;
            }
        }
        return pheromoneLevels;
    }

    private double[][] calculateDistances(List<City> cities) {
        int numCities = cities.size();
        double[][] distances = new double[numCities][numCities];
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                distances[i][j] = cities.get(i).distanceTo(cities.get(j));
            }
        }
        return distances;
    }

    private List<City> constructAntTour(List<City> cities, double[][] distances, double[][] pheromoneLevels, double alpha, double beta, Map<Integer, Integer> cityIndices) {
        List<City> tour = new ArrayList<>();
        List<City> remainingCities = new ArrayList<>(cities);

        City startCity = remainingCities.remove(0);
        tour.add(startCity);
        City currentCity = startCity;

        Random random = new Random();
        while (!remainingCities.isEmpty()) {
            double totalProbability = 0;
            for (City city : remainingCities) {
                int i = cityIndices.get(currentCity.getId());
                int j = cityIndices.get(city.getId());
                totalProbability += Math.pow(pheromoneLevels[i][j], alpha) * Math.pow(1 / distances[i][j], beta);
            }

            double selectionValue = random.nextDouble() * totalProbability;
            double accumulatedProbability = 0;
            City nextCity = null;
            Iterator<City> iterator = remainingCities.iterator();
            while (iterator.hasNext() && nextCity == null) {
                City city = iterator.next();
                int i = cityIndices.get(currentCity.getId());
                int j = cityIndices.get(city.getId());
                accumulatedProbability += Math.pow(pheromoneLevels[i][j], alpha) * Math.pow(1 / distances[i][j], beta);
                if (accumulatedProbability >= selectionValue) {
                    nextCity = city;
                    iterator.remove();
                }
            }

            tour.add(nextCity);
            currentCity = nextCity;
        }

        tour.add(startCity);
        return tour;
    }

    private void updatePheromoneLevels(double[][] pheromoneLevels, List<List<City>> antTours, double evaporationRate, Map<Integer, Integer> cityIndices) {
        int numCities = pheromoneLevels.length;

        // Evaporate pheromones
        for (int i = 0; i < numCities; i++) {
            for (int j = 0; j < numCities; j++) {
                pheromoneLevels[i][j] *= (1 - evaporationRate);
            }
        }

        // Add new pheromones
        for (List<City> tour : antTours) {
            double tourDistance = calculateTourDistance(tour);

            for (int i = 0; i < tour.size() - 1; i++) {
                City city1 = tour.get(i);
                City city2 = tour.get(i + 1);

                int id1 = cityIndices.get(city1.getId());
                int id2 = cityIndices.get(city2.getId());

                pheromoneLevels[id1][id2] += 1 / tourDistance;
                pheromoneLevels[id2][id1] += 1 / tourDistance;
            }
        }
    }

    private List<City> simAnneal(List<City> tour, double temperature, double coolingRate) {
        List<City> currentTour = new ArrayList<>(tour);
        Collections.shuffle(currentTour);
        List<City> bestTour = new ArrayList<>(currentTour);
        Collections.shuffle(bestTour);

        for (double t = temperature; t > 1; t *= coolingRate) {
            List<City> neighbor = new ArrayList<>(currentTour);
            Collections.shuffle(neighbor);

            int index1 = (int) (neighbor.size() * Math.random());
            int index2 = (int) (neighbor.size() * Math.random());

            Collections.swap(neighbor, index1, index2);

            double currentLength = calculateTourDistance(currentTour);
            double neighborLength = calculateTourDistance(neighbor);

            if (Math.random() < probability(currentLength, neighborLength, t)) {
                currentTour = new ArrayList<>(neighbor);
                Collections.shuffle(currentTour);
            }

            if (calculateTourDistance(currentTour) < calculateTourDistance(bestTour)) {
                bestTour = new ArrayList<>(currentTour);
                Collections.shuffle(bestTour);
            }
        }
        return bestTour;
    }

    private double probability(double f1, double f2, double temp) {
        if (f2 < f1) return 1;
        return Math.exp((f1 - f2) / temp);
    }
}