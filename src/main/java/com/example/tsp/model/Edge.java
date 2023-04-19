package com.example.tsp.model;

public class Edge implements Comparable<Edge> {
    private City source;
    private City target;
    private double weight;

    public Edge(City source, City target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    public City getSource() {
        return source;
    }

    public void setSource(City source) {
        this.source = source;
    }

    public City getTarget() {
        return target;
    }

    public void setTarget(City target) {
        this.target = target;
    }

    public double getWeight() {
        return weight;
    }

    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge other) {
        return Double.compare(this.weight, other.weight);
    }
}