export const formatDateTime = (value) => (value ? String(value).replace('T', ' ') : '-');

export const formatLabel = (map, value, fallback = '-') => {
  if (value === null || value === undefined || value === '') {
    return fallback;
  }
  return map?.[value] ?? String(value);
};

export const toTrimmedOrUndefined = (value) => {
  const trimmed = String(value ?? '').trim();
  return trimmed ? trimmed : undefined;
};
