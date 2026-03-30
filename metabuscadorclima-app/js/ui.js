const stateRefs = {
  hero: document.getElementById("heroState"),
  loading: document.getElementById("loadingState"),
  error: document.getElementById("errorState"),
  results: document.getElementById("resultsState"),
  errorMessage: document.getElementById("errorMessage"),
  resultHeader: document.getElementById("resultHeader"),
  sourcesSummary: document.getElementById("sourcesSummary"),
  sourcesCards: document.getElementById("sourcesCards")
};

function hideAllStates() {
  stateRefs.hero.classList.add("hidden");
  stateRefs.loading.classList.add("hidden");
  stateRefs.error.classList.add("hidden");
  stateRefs.results.classList.add("hidden");
}

function clearResultsState() {
  stateRefs.resultHeader.innerHTML = "";
  stateRefs.sourcesSummary.textContent = "";
  stateRefs.sourcesCards.innerHTML = "";
}

function renderInitialState() {
  hideAllStates();
  clearResultsState();
  stateRefs.hero.classList.remove("hidden");
}

function renderLoadingState() {
  hideAllStates();
  clearResultsState();
  stateRefs.loading.classList.remove("hidden");
}

function renderErrorState(message) {
  hideAllStates();
  clearResultsState();
  stateRefs.error.classList.remove("hidden");
  stateRefs.errorMessage.textContent = message || "Ocorreu um erro ao consultar as fontes climáticas.";
}

function mapSourceName(sourceName) {
  if (sourceName === "MetaClima") {
    return "MetaClima";
  }
  if (sourceName === "WeatherAPI") {
    return "WeatherAPI";
  }
  if (sourceName === "Open-Meteo") {
    return "Open-Meteo";
  }
  return safeText(sourceName);
}

function sourceStatusText(sources, consolidated) {
  const availableNames = (sources || [])
    .filter((source) => source.available)
    .map((source) => mapSourceName(source.source));

  if (consolidated && consolidated.sourcesUsed && consolidated.sourcesUsed.length) {
    return `Resumo consolidado com dados de ${consolidated.sourcesUsed.join(" e ")}.`;
  }

  if (availableNames.length) {
    return `Fontes exibidas: ${availableNames.join(", ")}.`;
  }

  return "Nenhuma fonte disponível.";
}

function calculateAverageTemperature(sources) {
  const values = (sources || [])
    .filter((source) => source.available && source.current && source.current.temperatureC !== null && source.current.temperatureC !== undefined)
    .map((source) => Number(source.current.temperatureC));

  if (!values.length) {
    return "Não disponível";
  }

  const avg = values.reduce((sum, value) => sum + value, 0) / values.length;
  return `${Math.round(avg)}°C`;
}

function getConditionIcon(condition) {
  const normalized = safeText(condition).toLowerCase();
  if (normalized.includes("sol") || normalized.includes("ensolarado") || normalized.includes("limpo")) {
    return "☀";
  }
  if (normalized.includes("chuva") || normalized.includes("garoa")) {
    return "🌧";
  }
  if (normalized.includes("tempest")) {
    return "⛈";
  }
  if (normalized.includes("nublado")) {
    return "☁";
  }
  return "⛅";
}

function renderResultHeader(location, averageTemperature) {
  const cityName = safeText(location && location.name);
  const region = [location && location.region, location && location.country].filter(Boolean).join(", ");

  stateRefs.resultHeader.innerHTML = `
    <div class="result-location-icon" aria-hidden="true">
      <svg width="20" height="20" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M12 21C12 21 19 14.75 19 9.5C19 5.91 15.87 3 12 3C8.13 3 5 5.91 5 9.5C5 14.75 12 21 12 21Z" stroke="currentColor" stroke-width="1.8"/>
        <circle cx="12" cy="9.5" r="2.5" fill="currentColor"/>
      </svg>
    </div>
    <div>
      <h2 class="result-city">${cityName}</h2>
      <p class="result-average">Temperatura média das fontes: ${averageTemperature}</p>
      ${region ? `<p class="result-meta">${region}</p>` : ""}
    </div>
  `;
}

function renderMetricsBlock(source) {
  return [
    {
      icon: "💧",
      label: "Umidade",
      value: formatHumidity(source.humidity)
    },
    {
      icon: "🌀",
      label: "Vento",
      value: formatWind(source.windSpeed)
    },
    {
      icon: "👁",
      label: "Visibilidade",
      value: safeMetric(source.visibility, " km")
    },
    {
      icon: "◔",
      label: "Pressão",
      value: safeMetric(source.pressure, " hPa")
    }
  ].map((metric) => `
      <div class="metric-item">
        <div class="metric-icon" aria-hidden="true">${metric.icon}</div>
        <div>
          <span class="metric-label">${metric.label}</span>
          <span class="metric-value">${metric.value}</span>
        </div>
      </div>
    `).join("");
}

function normalizeSourceForCard(source) {
  const current = source.current || {};
  const firstForecast = source.forecast && source.forecast.length ? source.forecast[0] : {};

  return {
    sourceName: mapSourceName(source.source),
    updatedAtText: safeText(source.updatedAtText),
    condition: safeText(current.condition),
    icon: getConditionIcon(current.condition),
    temperature: formatTempCompact(current.temperatureC),
    feelsLike: formatTempCompact(current.feelsLikeC),
    maxTemp: formatTempCompact(current.maxTempC ?? firstForecast.maxTempC),
    minTemp: formatTempCompact(current.minTempC ?? firstForecast.minTempC),
    humidity: current.humidity,
    windSpeed: current.windKph,
    visibility: current.visibilityKm,
    pressure: current.pressureHpa
  };
}

function renderWeatherCard(source) {
  const normalized = normalizeSourceForCard(source);
  return `
    <article class="weather-card">
      <div class="weather-card-top">
        <div>
          <h3 class="weather-card-source">${normalized.sourceName}</h3>
          <p class="weather-card-update">${normalized.updatedAtText}</p>
        </div>
        <div class="weather-card-condition">
          <div class="weather-card-icon" aria-hidden="true">${normalized.icon}</div>
          <p class="weather-card-condition-text">${normalized.condition}</p>
        </div>
      </div>

      <div class="weather-card-main">
        <div class="weather-card-temperature-line">
          <span class="weather-card-temperature">${normalized.temperature}</span>
          <span class="weather-card-feels-like">Sensação: ${normalized.feelsLike}</span>
        </div>
        <p class="weather-card-range">Máx: ${normalized.maxTemp} &nbsp;&nbsp; Min: ${normalized.minTemp}</p>
      </div>

      <hr class="weather-card-divider" />

      <div class="weather-card-details">
        ${renderMetricsBlock(normalized)}
      </div>
    </article>
  `;
}

function buildConsolidatedCard(data) {
  if (!data.consolidated || !data.consolidated.current) {
    return null;
  }

  const sourceNames = (data.consolidated.sourcesUsed || []).map(mapSourceName);
  return {
    source: "MetaClima",
    updatedAtText: sourceNames.length
      ? `Média entre ${sourceNames.join(" e ")}`
      : "Resumo consolidado",
    current: data.consolidated.current,
    forecast: data.forecast || []
  };
}

function buildRenderableCards(data) {
  const cards = [];
  const consolidatedCard = buildConsolidatedCard(data);
  const availableSources = (data.sources || []).filter((source) => source.available && source.current);

  if (consolidatedCard) {
    cards.push(consolidatedCard);
  }

  availableSources.forEach((source) => {
    cards.push({
      source: source.source,
      updatedAtText: "Atualizado agora",
      current: source.current,
      forecast: source.forecast || []
    });
  });

  return cards;
}

function renderWeatherResults(data) {
  const availableSources = (data.sources || []).filter((source) => source.available && source.current);
  const cards = buildRenderableCards(data);

  if (!cards.length) {
    renderErrorState("Não há dados suficientes para exibir o resultado.");
    return;
  }

  renderResultHeader(data.location, calculateAverageTemperature(availableSources));
  stateRefs.sourcesSummary.textContent = sourceStatusText(availableSources, data.consolidated);
  stateRefs.sourcesCards.innerHTML = cards.map(renderWeatherCard).join("");
}

function renderResultsState(data) {
  hideAllStates();
  stateRefs.results.classList.remove("hidden");
  renderWeatherResults(data);
}
