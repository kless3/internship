import { useCallback, useState } from 'react';

export function useOrderTimeline(loadOrderHistory) {
  const [expandedOrderIds, setExpandedOrderIds] = useState({});
  const [historyByOrderId, setHistoryByOrderId] = useState({});
  const [historyLoadingByOrderId, setHistoryLoadingByOrderId] = useState({});
  const [historyErrorByOrderId, setHistoryErrorByOrderId] = useState({});

  const refreshTimeline = useCallback(async (orderId) => {
    if (!orderId || typeof loadOrderHistory !== 'function') {
      return [];
    }

    try {
      setHistoryLoadingByOrderId((prev) => ({ ...prev, [orderId]: true }));
      setHistoryErrorByOrderId((prev) => ({ ...prev, [orderId]: '' }));

      const history = await loadOrderHistory(orderId);
      setHistoryByOrderId((prev) => ({ ...prev, [orderId]: history }));
      return history;
    } catch (error) {
      setHistoryErrorByOrderId((prev) => ({ ...prev, [orderId]: 'Failed to load order timeline.' }));
      throw error;
    } finally {
      setHistoryLoadingByOrderId((prev) => ({ ...prev, [orderId]: false }));
    }
  }, [loadOrderHistory]);

  const toggleTimeline = useCallback(async (orderId) => {
    if (!orderId) {
      return;
    }

    const isOpen = Boolean(expandedOrderIds[orderId]);
    if (isOpen) {
      setExpandedOrderIds((prev) => ({ ...prev, [orderId]: false }));
      return;
    }

    setExpandedOrderIds((prev) => ({ ...prev, [orderId]: true }));

    if (historyByOrderId[orderId] || historyLoadingByOrderId[orderId] || typeof loadOrderHistory !== 'function') {
      return;
    }

    await refreshTimeline(orderId).catch(() => null);
  }, [expandedOrderIds, historyByOrderId, historyLoadingByOrderId, loadOrderHistory, refreshTimeline]);

  return {
    expandedOrderIds,
    historyByOrderId,
    historyLoadingByOrderId,
    historyErrorByOrderId,
    refreshTimeline,
    toggleTimeline
  };
}
