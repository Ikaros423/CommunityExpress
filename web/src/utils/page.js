export const DEFAULT_PAGE = 1;
export const DEFAULT_PAGE_SIZE = 15;

const toSafeNumber = (value, fallback) => {
  const parsed = Number(value);
  return Number.isFinite(parsed) && parsed > 0 ? parsed : fallback;
};

export const normalizePageData = (raw, fallback = {}) => {
  const fallbackPage = toSafeNumber(fallback.page, DEFAULT_PAGE);
  const fallbackPageSize = toSafeNumber(fallback.pageSize, DEFAULT_PAGE_SIZE);

  if (Array.isArray(raw)) {
    return {
      list: raw,
      total: raw.length,
      page: fallbackPage,
      pageSize: fallbackPageSize
    };
  }

  if (raw && typeof raw === 'object') {
    const list = Array.isArray(raw.list) ? raw.list : [];
    const total = Number.isFinite(Number(raw.total)) ? Number(raw.total) : list.length;
    return {
      list,
      total: total < 0 ? 0 : total,
      page: toSafeNumber(raw.page, fallbackPage),
      pageSize: toSafeNumber(raw.pageSize, fallbackPageSize)
    };
  }

  return {
    list: [],
    total: 0,
    page: fallbackPage,
    pageSize: fallbackPageSize
  };
};
