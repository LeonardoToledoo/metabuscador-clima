package application.usecase.location.search;

import adapter.port.LocationSearchPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SearchLocationUseCaseImpl implements SearchLocationUseCase {

    private final LocationSearchPort dataProvider;

    @Override
    public SearchLocationOutput executar(SearchLocationInput inputData) {
        return dataProvider.buscar(inputData);
    }
}
