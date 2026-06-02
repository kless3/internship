import { useCallback, useState } from 'react';
import { normalizeApiError } from '../api/error-utils.js';

function setById(setter, id, value) {
  setter((prev) => ({ ...prev, [id]: value }));
}

export function useShippingAddressAction({ onUpdateShippingAddress, refreshTimeline }) {
  const [shippingAddressByOrderId, setShippingAddressByOrderId] = useState({});
  const [shippingAddressLoadingByOrderId, setShippingAddressLoadingByOrderId] = useState({});
  const [shippingAddressErrorByOrderId, setShippingAddressErrorByOrderId] = useState({});
  const [shippingAddressSuccessByOrderId, setShippingAddressSuccessByOrderId] = useState({});

  const changeShippingAddress = useCallback((orderId, nextValue) => {
    setById(setShippingAddressByOrderId, orderId, nextValue);
    setById(setShippingAddressErrorByOrderId, orderId, '');
    setById(setShippingAddressSuccessByOrderId, orderId, '');
  }, []);

  const saveShippingAddress = useCallback(async (orderId, fallbackAddress) => {
    if (!orderId || typeof onUpdateShippingAddress !== 'function') {
      return;
    }

    const address = (shippingAddressByOrderId[orderId] ?? fallbackAddress ?? '').trim();
    if (!address) {
      setById(setShippingAddressErrorByOrderId, orderId, 'Shipping address is required.');
      return;
    }

    try {
      setById(setShippingAddressLoadingByOrderId, orderId, true);
      setById(setShippingAddressErrorByOrderId, orderId, '');
      setById(setShippingAddressSuccessByOrderId, orderId, '');

      await onUpdateShippingAddress(orderId, address);
      await refreshTimeline?.(orderId);

      setById(setShippingAddressSuccessByOrderId, orderId, 'Shipping address updated.');
    } catch (e) {
      setById(setShippingAddressErrorByOrderId, orderId, normalizeApiError(e, 'Failed to update shipping address.'));
    } finally {
      setById(setShippingAddressLoadingByOrderId, orderId, false);
    }
  }, [
    onUpdateShippingAddress,
    refreshTimeline,
    shippingAddressByOrderId
  ]);

  return {
    shippingAddressByOrderId,
    shippingAddressLoadingByOrderId,
    shippingAddressErrorByOrderId,
    shippingAddressSuccessByOrderId,
    changeShippingAddress,
    saveShippingAddress
  };
}
