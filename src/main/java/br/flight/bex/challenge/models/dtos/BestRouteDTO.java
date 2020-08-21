package br.flight.bex.challenge.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@AllArgsConstructor
@Getter
@Builder
public class BestRouteDTO {

    private String path;
    private Double cost;

}
