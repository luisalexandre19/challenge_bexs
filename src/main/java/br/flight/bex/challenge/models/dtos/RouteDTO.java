package br.flight.bex.challenge.models.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PositiveOrZero;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class RouteDTO {

    @NotEmpty(message = "Task source must not be blank!")
    private String source;

    @NotEmpty(message = "Task target must not be blank!")
    private String target;

    @NotNull(message = "Task cost must not be null!")
    @PositiveOrZero(message = "Task cost must be positive or zero!")
    private Double cost;

    public String toCSV() {
        return String.format("\n%s,%s,%s", source, target, String.valueOf(cost));
    }

}
