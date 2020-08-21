package br.flight.bex.challenge.resources.v1;

import br.flight.bex.challenge.models.dtos.BestRouteDTO;
import br.flight.bex.challenge.models.dtos.RouteDTO;
import br.flight.bex.challenge.services.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = FlightResource.BASE_URL)
@RequiredArgsConstructor
public class FlightResource {

    public static final String BASE_URL = "/flights/v1";

    private final FlightService flightService;

    @GetMapping("/routes/source/{source}/target/{target}")
    public ResponseEntity<BestRouteDTO> bestRoute(@PathVariable("source") String source,
                                                  @PathVariable("target") String target) {

        List<String> pathRoutes = flightService.bestRoute(source, target);

        return ResponseEntity.ok(BestRouteDTO
                .builder()
                .path(pathRoutes.stream().collect(Collectors.joining(" - ")))
                .cost(flightService.getCost(target))
                .build());
    }

    @PostMapping("/routes")
    public ResponseEntity<RouteDTO> addRoute(@Valid @RequestBody RouteDTO route) {

        flightService.addRoute(route);

        return ResponseEntity.ok(route);
    }

}

