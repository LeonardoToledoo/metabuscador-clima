package adapter.port;

import application.usecase.location.search.SearchLocationInput;
import application.usecase.location.search.SearchLocationOutput;

public interface LocationSearchPort {

    SearchLocationOutput buscar(SearchLocationInput inputData);
}
