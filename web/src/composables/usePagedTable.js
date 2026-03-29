import { reactive, ref } from 'vue';
import { DEFAULT_PAGE, DEFAULT_PAGE_SIZE, normalizePageData } from '../utils/page';

export const usePagedTable = (fetcher, options = {}) => {
  const rows = ref([]);
  const loading = ref(false);
  const total = ref(0);

  const pagination = reactive({
    page: options.page ?? DEFAULT_PAGE,
    pageSize: options.pageSize ?? DEFAULT_PAGE_SIZE,
    itemCount: 0,
    showSizePicker: false,
    onChange: (page) => {
      pagination.page = page;
      void fetchList();
    }
  });

  const fetchList = async () => {
    try {
      loading.value = true;
      const res = await fetcher({ page: pagination.page, pageSize: pagination.pageSize });
      const pageData = normalizePageData(res?.data, {
        page: pagination.page,
        pageSize: pagination.pageSize
      });
      rows.value = pageData.list;
      total.value = pageData.total;
      pagination.itemCount = pageData.total;
      pagination.page = pageData.page;
      pagination.pageSize = pageData.pageSize;
      return res;
    } finally {
      loading.value = false;
    }
  };

  const search = async () => {
    pagination.page = DEFAULT_PAGE;
    return fetchList();
  };

  const resetPaging = () => {
    pagination.page = DEFAULT_PAGE;
  };

  return {
    rows,
    loading,
    total,
    pagination,
    fetchList,
    search,
    resetPaging
  };
};
