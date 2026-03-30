function formatTemp(value) {
  return value === null || value === undefined ? "N/D" : `${Number(value).toFixed(1)}°C`;
}

function normalizeText(value) {
  return String(value || "")
    .normalize("NFD")
    .replace(/[\u0300-\u036f]/g, "")
    .toLowerCase()
    .trim();
}

function filterOptionsByPrefix(options, query) {
  const normalizedQuery = normalizeText(query);
  const items = Array.isArray(options) ? options : [];

  if (!normalizedQuery) {
    return [...items].sort((left, right) => left.localeCompare(right, "pt-BR"));
  }

  const startsWith = [];
  const contains = [];

  items.forEach((option) => {
    const normalizedOption = normalizeText(option);
    if (normalizedOption.startsWith(normalizedQuery)) {
      startsWith.push(option);
    } else if (normalizedOption.includes(normalizedQuery)) {
      contains.push(option);
    }
  });

  return startsWith
    .sort((left, right) => left.localeCompare(right, "pt-BR"))
    .concat(contains.sort((left, right) => left.localeCompare(right, "pt-BR")));
}

function formatTempCompact(value) {
  if (value === null || value === undefined) {
    return "Não disponível";
  }

  const rounded = Math.round(Number(value));
  return `${rounded}°`;
}

function formatWind(value) {
  return value === null || value === undefined ? "Não disponível" : `${Number(value).toFixed(1)} km/h`;
}

function formatHumidity(value) {
  return value === null || value === undefined ? "Não disponível" : `${value}%`;
}

function safeText(value) {
  return value === null || value === undefined || value === "" ? "Não disponível" : String(value);
}

function safeMetric(value, suffix = "") {
  if (value === null || value === undefined || value === "") {
    return "Não disponível";
  }

  if (typeof value === "number") {
    return `${Number(value).toFixed(1).replace(".0", "")}${suffix}`;
  }

  return `${value}${suffix}`;
}

function formatDate(isoDate) {
  if (!isoDate) return "Não disponível";
  const [year, month, day] = isoDate.split("-");
  if (!year || !month || !day) return isoDate;
  return `${day}/${month}`;
}
