package br.flight.bex.challenge.exceptions.validation;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ValidationError {

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> errors;

    public ValidationError(List<String> errors) {
        this.errors = errors;
    }

}
