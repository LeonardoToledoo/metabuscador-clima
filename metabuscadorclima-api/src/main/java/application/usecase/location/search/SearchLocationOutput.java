package application.usecase.location.search;

import domain.dto.LocationOptionDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchLocationOutput {

    @Builder.Default
    private List<LocationOptionDto> items = new ArrayList<>();
}
