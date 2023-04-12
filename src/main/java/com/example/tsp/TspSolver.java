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

        Button btnNN = new Button("Nearest neighbor method");
        btnNN.setOnAction(e -> nearestNeighbor(canvas));

        Button btnOpt2 = new Button("2-opt method");
        btnOpt2.setOnAction(e -> twoOpt(canvas));

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


        HBox buttons = new HBox(10, btnClear, btnNN, btnOpt2, btnRandom);
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

        highlightSolution();
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

    private void highlightSolution() {
        for (Line line : lines) {
            line.setStroke(Color.RED);
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
//            result.add(new Edge(source, target));
        }

        return result;
    }

}