import { useCallback, useState } from 'react';
import { normalizeApiError } from '../api/error-utils.js';

function setById(setter, id, value) {
  setter((prev) => ({ ...prev, [id]: value }));
}

function toInclusiveRequestTimestamp(date) {
  if (!date) return '';
  if (date.length === 16) return `${date}:59.999`;
  if (date.length === 19) return `${date}.999`;
  return date;
}

export function useOrderPriceLookup({ onLoadOrderPrice }) {
  const [priceDateByOrderId, setPriceDateByOrderId] = useState({});
  const [priceLoadingByOrderId, setPriceLoadingByOrderId] = useState({});
  const [priceErrorByOrderId, setPriceErrorByOrderId] = useState({});
  const [priceResultByOrderId, setPriceResultByOrderId] = useState({});

  const changePriceDate = useCallback((orderId, nextValue) => {
    setById(setPriceDateByOrderId, orderId, nextValue);
    setById(setPriceErrorByOrderId, orderId, '');
  }, []);

  const loadPriceAt = useCallback(async (orderId) => {
    if (!orderId || typeof onLoadOrderPrice !== 'function') {
      return;
    }

    const date = toInclusiveRequestTimestamp(priceDateByOrderId[orderId]);
    if (!date) {
      setById(setPriceErrorByOrderId, orderId, 'Select date and time first.');
      return;
    }

    try {
      setById(setPriceLoadingByOrderId, orderId, true);
      setById(setPriceErrorByOrderId, orderId, '');

      const price = await onLoadOrderPrice(orderId, date);
      setById(setPriceResultByOrderId, orderId, price);
    } catch (e) {
      setById(setPriceErrorByOrderId, orderId, normalizeApiError(e, 'Failed to load order price.'));
    } finally {
      setById(setPriceLoadingByOrderId, orderId, false);
    }
  }, [onLoadOrderPrice, priceDateByOrderId]);

  return {
    priceDateByOrderId,
    priceLoadingByOrderId,
    priceErrorByOrderId,
    priceResultByOrderId,
    changePriceDate,
    loadPriceAt
  };
}
