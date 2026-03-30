package adapter.outbound.provider;

import lombok.Getter;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LocalLocationCatalog {

    @Getter
    private final List<CountryCatalogItem> countries = List.of(
            new CountryCatalogItem("Argentina", "AR", List.of(
                    new StateCatalogItem("Buenos Aires", "B", List.of("Buenos Aires", "La Plata", "Mar del Plata")),
                    new StateCatalogItem("Córdoba", "X", List.of("Córdoba", "Villa Carlos Paz"))
            )),
            new CountryCatalogItem("Brasil", "BR", List.of(
                    new StateCatalogItem("Amazonas", "AM", List.of("Manaus", "Parintins", "Itacoatiara")),
                    new StateCatalogItem("Distrito Federal", "DF", List.of("Brasília", "Ceilândia", "Taguatinga")),
                    new StateCatalogItem("Mato Grosso do Sul", "MS", List.of("Campo Grande", "Dourados", "Três Lagoas")),
                    new StateCatalogItem("Rio de Janeiro", "RJ", List.of("Rio de Janeiro", "Niterói", "Petrópolis")),
                    new StateCatalogItem("Rio Grande do Sul", "RS", List.of("Porto Alegre", "Caxias do Sul", "Pelotas")),
                    new StateCatalogItem("São Paulo", "SP", List.of("São Paulo", "Campinas", "Santos"))
            )),
            new CountryCatalogItem("Paraguai", "PY", List.of(
                    new StateCatalogItem("Asunción", "ASU", List.of("Asunción")),
                    new StateCatalogItem("Central", "11", List.of("Luque", "San Lorenzo", "Fernando de la Mora"))
            ))
    );

    public record CountryCatalogItem(String name, String iso2, List<StateCatalogItem> states) {}

    public record StateCatalogItem(String name, String iso2, List<String> cities) {}
}
