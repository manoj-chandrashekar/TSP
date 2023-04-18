package com.example.tsp;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextInputDialog;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.*;


public class TspSolver extends Application {
    private static final double CANVAS_WIDTH = 800;
    private static final double CANVAS_HEIGHT = 800;

    private static final String MAP_HTML = "/map.html";

    private static final double CANVAS_MARGIN = 50;

    private List<City> cities = new ArrayList<>();
    private List<Line> lines = new ArrayList<>();

    private List<City> christofideTour = new ArrayList<>();
    private List<City> tourAfter2Opt = new ArrayList<>();

    Label solutionCostLabel = new Label("Solution cost: N/A");

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        Pane pane = new Pane();
        Canvas citiesCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        Canvas linesCanvas = new Canvas(CANVAS_WIDTH, CANVAS_HEIGHT);
        pane.getChildren().addAll(linesCanvas, citiesCanvas);
        //canvas.setPrefSize(CANVAS_WIDTH, CANVAS_HEIGHT);
        citiesCanvas.setTranslateX(0);
        citiesCanvas.setTranslateY(0);

        Button btnClear = new Button("Clear all");
        btnClear.setOnAction(e -> clearCanvas(citiesCanvas, linesCanvas));

        Button btnChristofides = new Button("Christofides");
        btnChristofides.setOnAction(e -> christofidesAlgorithm(linesCanvas));

        Button btnRandomSwap = new Button("Random swap");
        btnRandomSwap.setOnAction(e -> randomSwapping(linesCanvas, christofideTour));

        Button btn2Opt = new Button("2-Opt");
        btn2Opt.setOnAction(e -> twoOptOptimization(linesCanvas, christofideTour));

        Button btn3Opt = new Button("3-Opt");
        btn3Opt.setOnAction(e -> opt3(linesCanvas, christofideTour));

        Button btnSimAnneal = new Button("Simulated Annealing");
        btnSimAnneal.setOnAction(e -> anneal(linesCanvas, christofideTour));

        Button btnNN = new Button("NN");
        btnNN.setOnAction(e -> nearestNeighbor(linesCanvas));

        Button btnAntColony = new Button("Ant Colony");
        btnAntColony.setOnAction(e -> antColonyOpt(linesCanvas, christofideTour));

        Button btnUpload = new Button("Upload Excel");
        Label lblStatus = new Label();
        btnUpload.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx", "*.xls"));
            File selectedFile = fileChooser.showOpenDialog(primaryStage);

            if (selectedFile != null) {
                List<City> cities = readCitiesFromExcel(selectedFile);
                this.cities = cities;
                GraphicsContext citiesGc = citiesCanvas.getGraphicsContext2D();
                GraphicsContext linesGc = linesCanvas.getGraphicsContext2D();
                plotCities(citiesGc, linesGc, citiesCanvas, linesCanvas, cities);
                lblStatus.setText("File uploaded and points plotted");
            } else {
                lblStatus.setText("No file selected");
            }
        });

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
                    generateRandomTsp(numCities, linesCanvas, citiesCanvas);
                } catch (NumberFormatException ex) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Invalid Input");
                    alert.setContentText("Please enter a valid number.");
                    alert.showAndWait();
                }
            });
        });


        HBox buttons = new HBox(10, btnClear, btnNN, btnChristofides, btnRandomSwap, btn2Opt, btn3Opt, btnSimAnneal, btnAntColony, btnRandom, btnUpload);
        buttons.setSpacing(10);

        VBox root = new VBox(10, pane, buttons, solutionCostLabel);

        root.setSpacing(10);


        Scene scene = new Scene(root, CANVAS_WIDTH, CANVAS_HEIGHT);
        primaryStage.setScene(scene);
        primaryStage.setTitle("TSP Solver");
        primaryStage.show();
    }

    private List<City> readCitiesFromExcel(File file) {
        List<City> cities = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            int rows = sheet.getPhysicalNumberOfRows();

            for (int r = 1; r < rows; r++) {
                Row row = sheet.getRow(r);
                if (row != null) {
                    Cell cellCrimeId = row.getCell(0);
                    Cell cellLongitude = row.getCell(1);
                    Cell cellLatitude = row.getCell(2);
                    if (cellCrimeId != null && cellLongitude != null && cellLatitude != null) {
                        String crimeIdLong = cellCrimeId.getStringCellValue();
                        String crimeId = crimeIdLong.substring(crimeIdLong.length() - 5);
                        double longitude = cellLongitude.getNumericCellValue();
                        double latitude = cellLatitude.getNumericCellValue();
                        cities.add(new City(longitude, latitude, latitude, longitude, crimeId));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return cities;
    }

    private void plotCities(GraphicsContext citiesGc, GraphicsContext linesGc, Canvas citiesCanvas, Canvas linesCanvas, List<City> cities) {
        citiesGc.clearRect(0, 0, citiesCanvas.getWidth(), citiesCanvas.getHeight());
        linesGc.clearRect(0, 0, linesCanvas.getWidth(), linesCanvas.getHeight());

        // Calculate the min and max values for longitude and latitude
        double minX = cities.stream().mapToDouble(City::getX).min().orElse(0.0);
        double maxX = cities.stream().mapToDouble(City::getX).max().orElse(0.0);
        double minY = cities.stream().mapToDouble(City::getY).min().orElse(0.0);
        double maxY = cities.stream().mapToDouble(City::getY).max().orElse(0.0);

        // Padding
        double padding = 60;

        // Calculate the scaling factors and offsets
        double scaleX = (citiesCanvas.getWidth() - padding) / (maxX - minX);
        double scaleY = (citiesCanvas.getHeight() - 2 * padding) / (maxY - minY);
        double offsetX = minX;
        double offsetY = maxY;

        double pointRadius = 2.5;

        for (int i = 0; i < cities.size(); i++) {
            City city = cities.get(i);
            double canvasX = 20 + (city.getX() - offsetX) * scaleX;
            double canvasY = 20 + (offsetY - city.getY()) * scaleY;

            city.setX(canvasX); // Update the city's X coordinate
            city.setY(canvasY); // Update the city's Y coordinate

            citiesGc.setFill(Color.BLUE);
            citiesGc.fillOval(canvasX - pointRadius, canvasY - pointRadius,  pointRadius * 2, pointRadius * 2);

            // Draw labels
            /*String labelText = city.getCrimeId();
            citiesGc.setFill(Color.BLACK);
            citiesGc.setFont(Font.font(10));
            citiesGc.fillText(labelText, canvasX + pointRadius * 2, canvasY - pointRadius);*/
        }

        // Draw lines between cities on the linesCanvas
        for (int i = 1; i < cities.size(); i++) {
            City from = cities.get(i - 1);
            City to = cities.get(i);
            linesGc.setStroke(Color.BLACK);
            linesGc.setLineWidth(1);
            linesGc.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
        }

        // Connect the last city to the first one
        City firstCity = cities.get(0);
        City lastCity = cities.get(cities.size() - 1);
        linesGc.setStroke(Color.BLACK);
        linesGc.setLineWidth(1);
        linesGc.strokeLine(firstCity.getX(), firstCity.getY(), lastCity.getX(), lastCity.getY());
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

    public static double calculateSolutionCost(List<City> cities) {
        double cost = 0.0;
        for (int i = 0; i < cities.size(); i++) {
            City currentCity = cities.get(i);
            City nextCity = cities.get((i + 1) % cities.size());
            cost += currentCity.distanceTo(nextCity);
        }
        return cost;
    }

    private void generateRandomTsp(int numCities, Canvas linesCanvas, Canvas citiesCanvas) {
        GraphicsContext linesGc = linesCanvas.getGraphicsContext2D();
        GraphicsContext citiesGc = citiesCanvas.getGraphicsContext2D();
        linesGc.clearRect(0, 0, linesCanvas.getWidth(), linesCanvas.getHeight());
        citiesGc.clearRect(0, 0, citiesCanvas.getWidth(), citiesCanvas.getHeight());
        Random rand = new Random();
        for (int i = 0; i < numCities; i++) {
            double x = rand.nextDouble() * (CANVAS_WIDTH - 20);
            double y = rand.nextDouble() * (CANVAS_HEIGHT - 120);
            addCityWithoutLine(x, y);

            citiesGc.setFill(Color.BLUE);
            citiesGc.fillOval(x - 2.5, y - 2.5, 5, 5);

            // Add a label for the city index
            citiesGc.setFill(Color.BLACK);
            citiesGc.fillText(Integer.toString(cities.size() - 1), x + 5, y + 5);
        }

        // Initialize lines
        linesGc.setStroke(Color.BLACK);
        linesGc.setLineWidth(1);
        for (int i = 0; i < cities.size() - 1; i++) {
            City startCity = cities.get(i);
            City endCity = cities.get(i + 1);

            linesGc.strokeLine(startCity.getX(), startCity.getY(), endCity.getX(), endCity.getY());
        }

        // Connect the last city to the first one
        City firstCity = cities.get(0);
        City lastCity = cities.get(cities.size() - 1);

        linesGc.strokeLine(firstCity.getX(), firstCity.getY(), lastCity.getX(), lastCity.getY());
        solutionCostLabel.setText("Solution cost: " + String.format("%.2f", calculateSolutionCost()));
    }

    private void clearCanvas(Canvas citiesCanvas, Canvas linesCanvas) {
        GraphicsContext citiesGc = citiesCanvas.getGraphicsContext2D();
        GraphicsContext linesGc = linesCanvas.getGraphicsContext2D();

        citiesGc.clearRect(0, 0, citiesCanvas.getWidth(), citiesCanvas.getHeight());
        linesGc.clearRect(0, 0, linesCanvas.getWidth(), linesCanvas.getHeight());

        cities.clear();
        christofideTour.clear();
        lines.clear();

        solutionCostLabel.setText("Solution cost: N/A");
    }


    private void nearestNeighbor(Canvas canvas) {
        if (cities.size() < 2) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        gc.setStroke(Color.RED);

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

            gc.strokeLine(currentCity.getX(), currentCity.getY(), cities.get(i + 1).getX(), cities.get(i + 1).getY());
        }

        City firstCity = cities.get(0);
        City lastCity = cities.get(cities.size() - 1);

        gc.strokeLine(firstCity.getX(), firstCity.getY(), lastCity.getX(), lastCity.getY());

        solutionCostLabel.setText("Solution cost: " + String.format("%.2f", calculateSolutionCost()));
    }

    private void christofidesAlgorithm(Canvas canvas) {
        if (cities.size() < 2) {
            return;
        }
        List<Edge> mst = minimumSpanningTree(cities);
        List<City> oddVertices = oddDegreeVertices(cities, mst);
        List<Edge> matching = minimumWeightPerfectMatching(oddVertices);
        List<Edge> multigraph = combineMSTAndMatching(mst, matching);
        List<City> eulerianTour = findEulerianCircuit(multigraph);
        List<City> hamiltonianTour = convertEulerianToHamiltonian(eulerianTour);

        christofideTour = new ArrayList<>(hamiltonianTour);
        displayData(canvas, hamiltonianTour, Color.AQUA);
    }

    private void displayData(Canvas canvas, List<City> cities, Color color) {
        if (cities.size() < 2) {
            return;
        }

        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());

        gc.setStroke(color);
        gc.setLineWidth(2);

        for (int i = 0; i < cities.size() - 1; i++) {
            City from = cities.get(i);
            City to = cities.get(i + 1);

            gc.strokeLine(from.getX(), from.getY(), to.getX(), to.getY());
        }

        City firstCity = cities.get(0);
        City lastCity = cities.get(cities.size() - 1);
        gc.strokeLine(firstCity.getX(), firstCity.getY(), lastCity.getX(), lastCity.getY());

        double cost = calculateSolutionCost(cities);
        solutionCostLabel.setText("Solution cost: " + String.format("%.2f", cost));
    }

    private void randomSwapping(Canvas canvas, List<City> tour) {
        List<City> optimizedTour = RandomSwapping.optimize(tour, 10000);
        displayData(canvas, optimizedTour, Color.DARKGREEN);
    }

    public void addCityWithoutLine(double x, double y) {
        City city = new City(x, y, y, x, "");
        cities.add(city);
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

    public void twoOptOptimization(Canvas canvas, List<City> tour) {
        if(tour.size() < 2) return;
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
        tourAfter2Opt = new ArrayList<>(tour);
        displayData(canvas, tour, Color.DARKMAGENTA);
    }

    private void reverseSubList(List<City> list, int start, int end) {
        while (start < end) {
            Collections.swap(list, start, end);
            start++;
            end--;
        }
    }

    public void opt3(Canvas canvas, List<City> tour) {
        List<City> optimizedTour = TSP3Opt.optimize(tour);
        displayData(canvas, optimizedTour, Color.RED);
    }

    public void threeOpt(Canvas canvas, List<City> christofideTour, int maxIterations) {
        int iterations = 0;
        boolean improved = true;
        while (improved && iterations < maxIterations) {
            improved = false;
            for (int i = 0; i < christofideTour.size() - 2; i++) {
                for (int j = i + 1; j < christofideTour.size() - 1; j++) {
                    for (int k = j + 1; k < christofideTour.size(); k++) {
                        double delta = calculateDelta(christofideTour, i, j, k);
                        if (delta < 0) {
                            swap(christofideTour, i + 1, j, k);
                            improved = true;
                        }
                    }
                }
            }
            System.out.println("Iteration: "+iterations);
            iterations++;
        }
        displayData(canvas, christofideTour, Color.RED);
    }

    public static void swap(List<City> tour, int i, int j, int k) {
        List<City> temp = new ArrayList<>(tour.size());

        for (int a = 0; a <= i - 1; a++) {
            temp.add(tour.get(a));
        }

        int dec = 0;
        for (int a = i; a <= j; a++) {
            temp.add(tour.get(j - dec));
            dec++;
        }

        for (int a = j + 1; a <= k; a++) {
            temp.add(tour.get(a));
        }

        for (int a = k + 1; a < tour.size(); a++) {
            temp.add(tour.get(a));
        }

        for (int a = 0; a < tour.size(); a++) {
            tour.set(a, temp.get(a));
        }
    }

    private double calculateDelta(List<City> tour, int i, int j, int k) {
        City city_i = tour.get(i);
        City city_i1 = tour.get((i + 1) % tour.size());
        City city_j = tour.get(j);
        City city_j1 = tour.get((j + 1) % tour.size());
        City city_k = tour.get(k);
        City city_k1 = tour.get((k + 1) % tour.size());

        double before = city_i.distanceTo(city_i1) + city_j.distanceTo(city_j1) + city_k.distanceTo(city_k1);
        double after = city_i.distanceTo(city_j) + city_j1.distanceTo(city_k) + city_k1.distanceTo(city_i1);

        return after - before;
    }

    private void anneal(Canvas canvas, List<City> tour) {
        double initialTemperature = 1000;
        double finalTemperature = 0.01;
        double coolingRate = 0.9995;
        long maxExecutionTimeMillis = 30000; // 30 seconds
        int maxStagnation = 5000;
        //List<City> bestTour = simAnneal(tour, initialTemperature, coolingRate);
        SimulatedAnnealingOptimizer optimizer = new SimulatedAnnealingOptimizer(100, 0.995, 10000);
        List<City> optimizedTour = optimizer.optimizeTour(tour);
//        List<City> optimizedTour = SimulatedAnnealing.optimize(tour, initialTemperature, coolingRate, 1000);
        displayData(canvas, optimizedTour, Color.DEEPSKYBLUE);
    }

    private void antColonyOpt(Canvas canvas, List<City> tour) {
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

}