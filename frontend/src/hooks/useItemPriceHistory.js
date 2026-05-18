import { useCallback, useEffect, useState } from 'react';
import { fetchItemPriceHistory } from '../api/items.js';
import { normalizeApiError } from '../api/error-utils.js';

export function useItemPriceHistory(itemId, months = 6) {
  const [priceHistory, setPriceHistory] = useState([]);
  const [priceHistoryLoading, setPriceHistoryLoading] = useState(false);
  const [priceHistoryError, setPriceHistoryError] = useState('');

  const loadPriceHistory = useCallback(async () => {
    if (!itemId) {
      setPriceHistory([]);
      setPriceHistoryError('');
      setPriceHistoryLoading(false);
      return [];
    }

    try {
      setPriceHistoryLoading(true);
      setPriceHistoryError('');
      const data = await fetchItemPriceHistory(itemId, months);
      setPriceHistory(data);
      return data;
    } catch (e) {
      setPriceHistory([]);
      setPriceHistoryError(normalizeApiError(e, 'Failed to load item price history.'));
      return [];
    } finally {
      setPriceHistoryLoading(false);
    }
  }, [itemId, months]);

  useEffect(() => {
    loadPriceHistory();
  }, [loadPriceHistory]);

  return {
    priceHistory,
    priceHistoryLoading,
    priceHistoryError,
    refreshPriceHistory: loadPriceHistory
  };
}
