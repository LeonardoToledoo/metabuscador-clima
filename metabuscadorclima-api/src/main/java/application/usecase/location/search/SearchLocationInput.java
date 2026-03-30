package application.usecase.location.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchLocationInput {

    private LocationSearchEnum type;
    private String country;
    private String state;
    private String query;
}
