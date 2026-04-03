package org.example.eventmodule.request;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PlacesChangeRequest {
    @Positive
    private int places;
}
