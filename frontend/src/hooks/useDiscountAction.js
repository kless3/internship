import { useCallback, useState } from 'react';
import { normalizeApiError } from '../api/error-utils.js';

function setById(setter, id, value) {
  setter((prev) => ({ ...prev, [id]: value }));
}

export function useDiscountAction({ onRefresh, onApplyDiscount }) {
  const [discountByOrderId, setDiscountByOrderId] = useState({});
  const [discountLoadingByOrderId, setDiscountLoadingByOrderId] = useState({});
  const [discountErrorByOrderId, setDiscountErrorByOrderId] = useState({});
  const [discountSuccessByOrderId, setDiscountSuccessByOrderId] = useState({});

  const changeDiscount = useCallback((orderId, nextValue) => {
    setById(setDiscountByOrderId, orderId, nextValue);
    setById(setDiscountErrorByOrderId, orderId, '');
    setById(setDiscountSuccessByOrderId, orderId, '');
  }, []);

  const applyDiscount = useCallback(async (orderId) => {
    if (!orderId || typeof onApplyDiscount !== 'function') return;

    const raw = discountByOrderId[orderId];
    const value = raw !== undefined && raw !== '' ? Number(raw) : null;
    if (value === null || isNaN(value) || value < 0 || value > 100) {
      setById(setDiscountErrorByOrderId, orderId, 'Enter a value between 0 and 100.');
      return;
    }

    try {
      setById(setDiscountLoadingByOrderId, orderId, true);
      setById(setDiscountErrorByOrderId, orderId, '');
      setById(setDiscountSuccessByOrderId, orderId, '');

      await onApplyDiscount(orderId, value);
      await Promise.resolve(onRefresh?.());

      setById(setDiscountSuccessByOrderId, orderId, `Discount ${value}% applied.`);
    } catch (e) {
      setById(setDiscountErrorByOrderId, orderId, normalizeApiError(e, 'Failed to apply discount.'));
    } finally {
      setById(setDiscountLoadingByOrderId, orderId, false);
    }
  }, [discountByOrderId, onApplyDiscount, onRefresh]);

  const removeDiscount = useCallback(async (orderId) => {
    if (!orderId || typeof onApplyDiscount !== 'function') return;

    try {
      setById(setDiscountLoadingByOrderId, orderId, true);
      setById(setDiscountErrorByOrderId, orderId, '');
      setById(setDiscountSuccessByOrderId, orderId, '');

      await onApplyDiscount(orderId, 0);
      await Promise.resolve(onRefresh?.());

      setById(setDiscountSuccessByOrderId, orderId, 'Discount removed.');
    } catch (e) {
      setById(setDiscountErrorByOrderId, orderId, normalizeApiError(e, 'Failed to remove discount.'));
    } finally {
      setById(setDiscountLoadingByOrderId, orderId, false);
    }
  }, [onApplyDiscount, onRefresh]);

  return {
    discountByOrderId,
    discountLoadingByOrderId,
    discountErrorByOrderId,
    discountSuccessByOrderId,
    changeDiscount,
    applyDiscount,
    removeDiscount
  };
}
