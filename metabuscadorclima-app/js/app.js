const searchForm = document.getElementById("searchForm");
const countryInput = document.getElementById("countryInput");
const stateInput = document.getElementById("stateInput");
const cityInput = document.getElementById("cityInput");
const searchButton = document.getElementById("searchButton");
const retryBtn = document.getElementById("retryBtn");
const backSearchButton = document.getElementById("backSearchButton");
const chips = Array.from(document.querySelectorAll(".chip"));

let lastSearch = null;
let countriesCache = null;
const statesCache = new Map();
const citiesCache = new Map();

const selectedLocation = {
  country: "",
  state: "",
  city: ""
};

const countrySelect = createAutocompleteSelect({
  input: countryInput,
  dropdown: document.getElementById("countryDropdown"),
  getOptions: loadCountryOptions,
  onSelect: handleCountrySelection,
  onInputChange: () => {
    selectedLocation.country = "";
    resetDependentFields("country");
  }
});

const stateSelect = createAutocompleteSelect({
  input: stateInput,
  dropdown: document.getElementById("stateDropdown"),
  getOptions: loadStateOptions,
  onSelect: handleStateSelection,
  onInputChange: () => {
    selectedLocation.state = "";
    resetDependentFields("state");
  }
});

const citySelect = createAutocompleteSelect({
  input: cityInput,
  dropdown: document.getElementById("cityDropdown"),
  getOptions: loadCityOptions,
  onSelect: handleCitySelection,
  onInputChange: () => {
    selectedLocation.city = "";
    updateSearchButtonState();
  }
});

async function loadCountryOptions() {
  if (countriesCache) {
    return countriesCache;
  }

  countriesCache = await fetchCountries();
  return countriesCache;
}

async function loadStateOptions() {
  if (!selectedLocation.country) {
    return [];
  }

  if (statesCache.has(selectedLocation.country)) {
    return statesCache.get(selectedLocation.country);
  }

  const states = await fetchStates(selectedLocation.country);
  statesCache.set(selectedLocation.country, states);
  return states;
}

async function loadCityOptions() {
  if (!selectedLocation.country || !selectedLocation.state) {
    return [];
  }

  const cacheKey = `${selectedLocation.country}::${selectedLocation.state}`;
  if (citiesCache.has(cacheKey)) {
    return citiesCache.get(cacheKey);
  }

  const cities = await fetchCities(selectedLocation.country, selectedLocation.state);
  citiesCache.set(cacheKey, cities);
  return cities;
}

function updateSearchButtonState() {
  searchButton.disabled = !selectedLocation.city;
}

function resetDependentFields(level) {
  if (level === "country") {
    selectedLocation.state = "";
    selectedLocation.city = "";
    stateSelect.reset();
    citySelect.reset();
    stateSelect.setEnabled(Boolean(selectedLocation.country));
    citySelect.setEnabled(false);
  }

  if (level === "state") {
    selectedLocation.city = "";
    citySelect.reset();
    citySelect.setEnabled(Boolean(selectedLocation.country && selectedLocation.state));
  }

  updateSearchButtonState();
}

function handleCountrySelection(country) {
  selectedLocation.country = country;
  resetDependentFields("country");
  renderInitialState();
  stateInput.focus();
}

function handleStateSelection(state) {
  selectedLocation.state = state;
  resetDependentFields("state");
  renderInitialState();
  cityInput.focus();
}

function handleCitySelection(city) {
  selectedLocation.city = city;
  updateSearchButtonState();
  renderInitialState();
}

function buildSearchQuery() {
  return [selectedLocation.city, selectedLocation.state, selectedLocation.country]
    .filter(Boolean)
    .join(", ");
}

function resetToInitialState() {
  selectedLocation.country = "";
  selectedLocation.state = "";
  selectedLocation.city = "";

  countrySelect.reset();
  stateSelect.setEnabled(false);
  citySelect.setEnabled(false);
  searchButton.disabled = true;
  renderInitialState();
  countryInput.focus();
}

async function executeWeatherSearch() {
  if (!selectedLocation.country || !selectedLocation.state || !selectedLocation.city) {
    renderErrorState("Selecione país, estado e cidade para continuar.");
    return;
  }

  const query = buildSearchQuery();
  lastSearch = { ...selectedLocation };
  renderLoadingState();

  try {
    const data = await fetchWeatherByCity(query);
    if (!data || !data.location) {
      renderErrorState("Não há dados suficientes para exibir o resultado.");
      return;
    }
    renderResultsState(data);
  } catch (error) {
    const message = error.message || "Ocorreu um erro ao consultar as fontes climáticas.";
    renderErrorState(message);
  }
}

function applyPresetLocation(country, state, city) {
  selectedLocation.country = country;
  countrySelect.setValue(country);
  stateSelect.setEnabled(true);

  selectedLocation.state = state;
  stateSelect.setValue(state);
  citySelect.setEnabled(true);

  selectedLocation.city = city;
  citySelect.setValue(city);
  updateSearchButtonState();
}

searchForm.addEventListener("submit", (event) => {
  event.preventDefault();
  executeWeatherSearch();
});

chips.forEach((chip) => {
  chip.addEventListener("click", () => {
    applyPresetLocation(chip.dataset.country, chip.dataset.state, chip.dataset.city);
    executeWeatherSearch();
  });
});

retryBtn.addEventListener("click", () => {
  if (lastSearch) {
    applyPresetLocation(lastSearch.country, lastSearch.state, lastSearch.city);
    executeWeatherSearch();
    return;
  }
  resetToInitialState();
});

backSearchButton.addEventListener("click", () => {
  resetToInitialState();
});

countrySelect.setEnabled(true);
stateSelect.setEnabled(false);
citySelect.setEnabled(false);
renderInitialState();
