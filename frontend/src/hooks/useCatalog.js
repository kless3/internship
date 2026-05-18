import { useCallback, useEffect, useState } from 'react';
import { fetchItems } from '../api/items.js';
import { normalizeApiError } from '../api/error-utils.js';

export function useCatalog(pageSize = 10) {
  const [catalog, setCatalog] = useState([]);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogPage, setCatalogPage] = useState(0);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [catalogError, setCatalogError] = useState('');

  const loadCatalog = useCallback(async (targetPage = 0) => {
    try {
      setCatalogLoading(true);
      setCatalogError('');

      const data = await fetchItems(targetPage, pageSize);
      const content = Array.isArray(data?.content) ? data.content : [];
      const resolvedTotalPages =
        Number.isFinite(Number(data?.totalPages)) && Number(data.totalPages) > 0
          ? Number(data.totalPages)
          : 1;
      const backendPage = Number.isFinite(Number(data?.number)) ? Number(data.number) : targetPage;

      setCatalog(content);
      setTotalPages(resolvedTotalPages);
      setTotalElements(Number.isFinite(Number(data?.totalElements)) ? Number(data.totalElements) : content.length);
      setCatalogPage(backendPage);
    } catch (e) {
      setCatalogError(normalizeApiError(e, 'Failed to load catalog.'));
    } finally {
      setCatalogLoading(false);
    }
  }, [pageSize]);

  useEffect(() => {
    loadCatalog(catalogPage);
  }, [catalogPage, loadCatalog]);

  const refreshCatalog = useCallback(() => loadCatalog(catalogPage), [catalogPage, loadCatalog]);

  const updateCatalogItem = useCallback((itemId, patch) => {
    setCatalog((prev) =>
      prev.map((item) => (item.id === itemId ? { ...item, ...patch } : item))
    );
  }, []);

  return {
    catalog,
    catalogLoading,
    catalogPage,
    totalPages,
    totalElements,
    catalogError,
    setCatalogPage,
    refreshCatalog,
    updateCatalogItem
  };
}
