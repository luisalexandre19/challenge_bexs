package br.flight.bex.challenge.entities;

import br.flight.bex.challenge.models.dtos.RouteDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Builder
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "routes")
public class RouteEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    private String source;
    private String target;
    private Double cost;

    public RouteEntity(String source, String target, Double cost) {
        this.source = source;
        this.target = target;
        this.cost = cost;
    }

    public static RouteEntity from(RouteDTO routeDTO) {
        return RouteEntity
                .builder()
                .source(routeDTO.getSource())
                .target(routeDTO.getTarget())
                .cost(routeDTO.getCost())
                .build();
    }

}
