import { useCallback, useState } from 'react';
import { normalizeApiError } from '../api/error-utils.js';

function setById(setter, id, value) {
  setter((prev) => ({ ...prev, [id]: value }));
}

export function useOrderPaymentAction({ onPayOrder, refreshTimeline }) {
  const [payLoadingByOrderId, setPayLoadingByOrderId] = useState({});
  const [payErrorByOrderId, setPayErrorByOrderId] = useState({});
  const [paySuccessByOrderId, setPaySuccessByOrderId] = useState({});

  const payOrder = useCallback(async (orderId) => {
    if (!orderId || typeof onPayOrder !== 'function') {
      return;
    }

    try {
      setById(setPayLoadingByOrderId, orderId, true);
      setById(setPayErrorByOrderId, orderId, '');
      setById(setPaySuccessByOrderId, orderId, '');

      await onPayOrder(orderId);
      await refreshTimeline?.(orderId);

      setById(setPaySuccessByOrderId, orderId, 'Payment started.');
    } catch (e) {
      setById(setPayErrorByOrderId, orderId, normalizeApiError(e, 'Failed to start payment.'));
    } finally {
      setById(setPayLoadingByOrderId, orderId, false);
    }
  }, [onPayOrder, refreshTimeline]);

  return {
    payLoadingByOrderId,
    payErrorByOrderId,
    paySuccessByOrderId,
    payOrder
  };
}
