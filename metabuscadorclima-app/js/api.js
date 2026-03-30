const API_CONFIG = {
  BASE_URL: window.METACLIMA_API_BASE_URL || "/api",
  TIMEOUT_MS: 12000
};

async function fetchWeatherByCity(city) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT_MS);

  try {
    const url = `${API_CONFIG.BASE_URL}/weather/search?city=${encodeURIComponent(city)}`;
    const response = await fetch(url, {
      method: "GET",
      signal: controller.signal,
      headers: {
        Accept: "application/json"
      }
    });

    if (!response.ok) {
      const errorBody = await response.json().catch(() => ({}));
      const message = errorBody.message || "Falha ao consultar serviço climático.";
      throw new Error(message);
    }

    return await response.json();
  } catch (error) {
    if (error.name === "AbortError") {
      throw new Error("Tempo limite excedido. Tente novamente.");
    }
    throw error;
  } finally {
    clearTimeout(timeoutId);
  }
}

async function fetchLocationOptions(path, params = {}) {
  const controller = new AbortController();
  const timeoutId = setTimeout(() => controller.abort(), API_CONFIG.TIMEOUT_MS);

  try {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== null && value !== undefined && value !== "") {
        query.set(key, value);
      }
    });

    const url = `${API_CONFIG.BASE_URL}${path}${query.toString() ? `?${query.toString()}` : ""}`;
    const response = await fetch(url, {
      method: "GET",
      signal: controller.signal,
      headers: {
        Accept: "application/json"
      }
    });

    if (!response.ok) {
      throw new Error("Falha ao carregar opções de localização.");
    }

    const data = await response.json();
    return Array.isArray(data.items) ? data.items : [];
  } catch (error) {
    if (error.name === "AbortError") {
      throw new Error("Tempo limite excedido ao carregar localizações.");
    }
    throw error;
  } finally {
    clearTimeout(timeoutId);
  }
}

async function fetchCountries() {
  return fetchLocationOptions("/locations/countries");
}

async function fetchStates(country) {
  return fetchLocationOptions("/locations/states", { country });
}

async function fetchCities(country, state) {
  return fetchLocationOptions("/locations/cities", { country, state });
}
