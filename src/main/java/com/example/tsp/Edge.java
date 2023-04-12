package com.example.tsp;

class Edge implements Comparable<Edge> {
    City source;
    City target;
    double weight;

    Edge(City source, City target, double weight) {
        this.source = source;
        this.target = target;
        this.weight = weight;
    }

    @Override
    public int compareTo(Edge other) {
        return Double.compare(this.weight, other.weight);
    }
}