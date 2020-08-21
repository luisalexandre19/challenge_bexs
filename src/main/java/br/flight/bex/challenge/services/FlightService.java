package br.flight.bex.challenge.services;

import br.flight.bex.challenge.entities.RouteEntity;
import br.flight.bex.challenge.exceptions.ChallengeException;
import br.flight.bex.challenge.models.dtos.RouteDTO;
import br.flight.bex.challenge.repositories.RouteRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Objects.nonNull;

@Service
@RequiredArgsConstructor
public class FlightService {

    private static Logger LOG = LoggerFactory.getLogger(FlightService.class);

    private final RouteRepository routeRepository;

    private final ApplicationArguments applicationArguments;

    private List<RouteEntity> routes;
    private Set<String> settledNodes;
    private Set<String> unSettledNodes;
    private Map<String, String> predecessors;
    private Map<String, Double> distance;

    public RouteDTO addRoute(RouteDTO routeDTO) {
        routeRepository.save(RouteEntity.from(routeDTO));

        if (nonNull(applicationArguments)
                && nonNull(applicationArguments.getNonOptionArgs())
                && nonNull(applicationArguments.getNonOptionArgs().get(0))) {
            //update a new line in cvs file
            updateCSVFile(routeDTO);
        }
        return routeDTO;
    }

    private void updateCSVFile(RouteDTO routeDTO) {
        try {

            Files.write(FileSystems.getDefault().getPath(applicationArguments.getNonOptionArgs().get(0)), routeDTO.toCSV().getBytes(), StandardOpenOption.CREATE, StandardOpenOption.APPEND);

        } catch (Throwable ex) {
            LOG.error("Error -> " + ex.getMessage(), ex);
        }
    }

    public List<String> bestRoute(String source, String target) {

        init(source);

        if (predecessors.get(target) == null) {
            throw new ChallengeException("Not found a path!", HttpStatus.NOT_FOUND);
        }

        List<String> path = new ArrayList<>();
        String step = target;
        path.add(step);

        while (predecessors.get(step) != null) {
            step = predecessors.get(step);
            path.add(step);
        }

        Collections.reverse(path);

        return path;
    }

    private void init(String source) {

        this.routes = routeRepository.findAll();

        if (Objects.isNull(this.routes)
            || this.routes.isEmpty()) {
            throw new ChallengeException("Not found any routes in database!");
        }

        settledNodes = new HashSet<>();
        unSettledNodes = new HashSet<>();
        distance = new HashMap<>();
        predecessors = new HashMap<>();
        distance.put(source, 0.0);
        unSettledNodes.add(source);
        while (unSettledNodes.size() > 0) {
            String node = getMinimum(unSettledNodes);
            settledNodes.add(node);
            unSettledNodes.remove(node);
            findMinimalDistances(node);
        }
    }

    private void findMinimalDistances(String node) {
        List<String> adjacentNodes = getNeighbors(node);
        adjacentNodes.forEach(target -> {
            if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
                distance.put(target, getShortestDistance(node) + getDistance(node, target));
                predecessors.put(target, node);
                unSettledNodes.add(target);
            }
        });
    }

    private Double getDistance(String node, String target) {
        return routes.stream().filter(routeEntity -> routeEntity.getSource().equals(node) && routeEntity.getTarget().equals(target))
                .map(routeEntity -> routeEntity.getCost()).findFirst().orElseThrow(() -> new ChallengeException("Source/target not found"));
    }

    private List<String> getNeighbors(String node) {
        return routes.stream().filter(routeEntity -> routeEntity.getSource().equals(node) && !isSettled(routeEntity.getTarget()))
                .map(routeEntity -> routeEntity.getTarget()).collect(Collectors.toList());
    }

    public Double getCost(String target) {
        return distance.getOrDefault(target, 0.0);
    }

    private String getMinimum(Set<String> vertexes) {
        String minimum = vertexes.iterator().next();
        for (String vertex : vertexes) {
            if (getShortestDistance(vertex) < getShortestDistance(minimum)) {
                minimum = vertex;
            }
        }
        return minimum;
    }

    private boolean isSettled(String vertex) {
        return settledNodes.contains(vertex);
    }

    private Double getShortestDistance(String destination) {
        return distance.getOrDefault(destination, Double.MAX_VALUE);
    }

}
