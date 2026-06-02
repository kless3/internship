import { useCallback, useMemo, useState } from 'react';
import { useDiscountAction } from '../hooks/useDiscountAction.js';
import { useOrderPaymentAction } from '../hooks/useOrderPaymentAction.js';
import { useOrderPriceLookup } from '../hooks/useOrderPriceLookup.js';
import { useOrderTimeline } from '../hooks/useOrderTimeline.js';
import { useShippingAddressAction } from '../hooks/useShippingAddressAction.js';
import { normalizeApiError } from '../api/error-utils.js';

function setById(setter, id, value) {
  setter((prev) => ({ ...prev, [id]: value }));
}

function toInclusiveRestoreTimestamp(date) {
  if (!date) return '';
  if (date.length === 16) return `${date}:59.999`;
  if (date.length === 19) return `${date}.999`;
  return date;
}

export default function OrderList({
  orders,
  isAdmin,
  page,
  totalPages,
  totalElements,
  loading,
  error,
  onRefresh,
  onPageChange,
  loadOrderHistory,
  onLoadOrderPrice,
  onRestoreOrder,
  onPayOrder,
  onUpdateShippingAddress,
  onApplyDiscount
}) {
  const {
    expandedOrderIds,
    historyByOrderId,
    historyLoadingByOrderId,
    historyErrorByOrderId,
    refreshTimeline,
    toggleTimeline
  } = useOrderTimeline(loadOrderHistory);

  const [restoreDateByOrderId, setRestoreDateByOrderId] = useState({});
  const [restoreLoadingByOrderId, setRestoreLoadingByOrderId] = useState({});
  const [restoreErrorByOrderId, setRestoreErrorByOrderId] = useState({});
  const [restoreSuccessByOrderId, setRestoreSuccessByOrderId] = useState({});

  const changeRestoreDate = useCallback((orderId, nextValue) => {
    setById(setRestoreDateByOrderId, orderId, nextValue);
    setById(setRestoreErrorByOrderId, orderId, '');
    setById(setRestoreSuccessByOrderId, orderId, '');
  }, []);

  const restoreOrder = useCallback(async (orderId) => {
    if (!orderId || typeof onRestoreOrder !== 'function') {
      return;
    }

    const inclusiveRestoreTimestamp = toInclusiveRestoreTimestamp(restoreDateByOrderId[orderId]);
    if (!inclusiveRestoreTimestamp) {
      setById(setRestoreErrorByOrderId, orderId, 'Select date and time first.');
      return;
    }

    try {
      setById(setRestoreLoadingByOrderId, orderId, true);
      setById(setRestoreErrorByOrderId, orderId, '');
      setById(setRestoreSuccessByOrderId, orderId, '');

      await onRestoreOrder(orderId, inclusiveRestoreTimestamp);
      await Promise.resolve(onRefresh?.());
      await refreshTimeline?.(orderId);

      setById(setRestoreSuccessByOrderId, orderId, 'Status restored successfully.');
    } catch (e) {
      setById(setRestoreErrorByOrderId, orderId, normalizeApiError(e, 'Failed to restore order status.'));
    } finally {
      setById(setRestoreLoadingByOrderId, orderId, false);
    }
  }, [onRefresh, onRestoreOrder, refreshTimeline, restoreDateByOrderId]);

  const {
    payLoadingByOrderId,
    payErrorByOrderId,
    paySuccessByOrderId,
    payOrder
  } = useOrderPaymentAction({
    onPayOrder,
    refreshTimeline
  });

  const {
    shippingAddressByOrderId,
    shippingAddressLoadingByOrderId,
    shippingAddressErrorByOrderId,
    shippingAddressSuccessByOrderId,
    changeShippingAddress,
    saveShippingAddress
  } = useShippingAddressAction({
    onUpdateShippingAddress,
    refreshTimeline
  });

  const {
    discountByOrderId,
    discountLoadingByOrderId,
    discountErrorByOrderId,
    discountSuccessByOrderId,
    changeDiscount,
    applyDiscount,
    removeDiscount
  } = useDiscountAction({
    onRefresh,
    onApplyDiscount
  });

  const {
    priceDateByOrderId,
    priceLoadingByOrderId,
    priceErrorByOrderId,
    priceResultByOrderId,
    changePriceDate,
    loadPriceAt
  } = useOrderPriceLookup({
    onLoadOrderPrice
  });

  const sortedOrders = useMemo(
    () => [...orders].sort((a, b) => new Date(b.creationDate) - new Date(a.creationDate)),
    [orders]
  );

  const formatMoney = (value) => {
    const amount = Number(value ?? 0);
    return Number.isFinite(amount) ? `$${amount.toFixed(2)}` : '$0.00';
  };

  const formatPercent = (value) => {
    const percent = Number(value ?? 0);
    return Number.isFinite(percent) ? percent.toFixed(2).replace(/\.?0+$/, '') : '0';
  };

  const toLabel = (value) =>
    String(value ?? '')
      .toLowerCase()
      .replaceAll('_', ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase());

  return (
    <div className="card shadow-sm h-100">
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-start mb-3">
          <div>
            <h5 className="card-title mb-1">Order History</h5>
            <p className="text-muted small mb-0">All your orders across statuses.</p>
          </div>
          <button className="btn btn-outline-secondary btn-sm" onClick={onRefresh}>Refresh</button>
        </div>

        {loading ? <p className="mb-0">Loading orders...</p> : null}
        {error ? <div className="alert alert-danger py-2">{error}</div> : null}

        {!loading && !error && sortedOrders.length === 0 ? <p className="text-muted mb-0">No orders yet.</p> : null}

        {!loading && !error && sortedOrders.length > 0 ? (
          <div className="vstack gap-3">
            {sortedOrders.map((order) => {
              const total = (order.orderItems ?? []).reduce(
                (sum, line) => sum + (line.item?.price ?? 0) * line.quantity,
                0
              );
              const priceResult = priceResultByOrderId[order.id];

              return (
                <div key={order.id ?? `${order.creationDate}-${order.status}-${order.userId}`} className="border rounded p-3">
                  <div className="d-flex justify-content-between align-items-start">
                    <div className="d-flex flex-column gap-1">
                      <span className="badge text-bg-primary">{order.status}</span>
                      {order.discountPercent > 0 ? (
                        <span className="badge text-bg-success">Discount {order.discountPercent}%</span>
                      ) : null}
                    </div>
                    <div className="text-end">
                      {order.discountPercent > 0 ? (
                        <>
                          <div className="text-muted text-decoration-line-through small">${total.toFixed(2)}</div>
                          <strong>${(total * (1 - order.discountPercent / 100)).toFixed(2)}</strong>
                        </>
                      ) : (
                        <strong>${total.toFixed(2)}</strong>
                      )}
                    </div>
                  </div>
                  <div className="small text-muted mt-2">{new Date(order.creationDate).toLocaleString()}</div>
                  <div className="small mt-1">
                    <span className="text-muted">Shipping:</span> {order.shippingAddress || '-'}
                  </div>
                  <div className="d-flex flex-wrap gap-2 mt-2">
                    {(order.orderItems ?? []).map((line) => (
                      <span key={`${order.id}-${line.item?.id}`} className="badge text-bg-light border">
                        {line.item?.name} x{line.quantity}
                      </span>
                    ))}
                  </div>

                  <div className="mt-3">
                    <div className="d-flex gap-2">
                      {order.status === 'PENDING' ? (
                        <button
                          className="btn btn-sm btn-success"
                          type="button"
                          onClick={() => payOrder(order.id)}
                          disabled={Boolean(payLoadingByOrderId[order.id])}
                        >
                          {payLoadingByOrderId[order.id] ? 'Starting payment...' : 'Pay'}
                        </button>
                      ) : null}
                      <button
                        className="btn btn-sm btn-outline-primary"
                        type="button"
                        onClick={() => toggleTimeline(order.id)}
                      >
                        {expandedOrderIds[order.id] ? 'Hide timeline' : 'Show timeline'}
                      </button>
                    </div>
                    {payErrorByOrderId[order.id] ? (
                      <div className="text-danger small mt-1">{payErrorByOrderId[order.id]}</div>
                    ) : null}
                    {paySuccessByOrderId[order.id] ? (
                      <div className="text-success small mt-1">{paySuccessByOrderId[order.id]}</div>
                    ) : null}
                  </div>

                  <div className="mt-3 pt-3 border-top">
                    <label className="form-label small mb-1">Price by date and time</label>
                    <div className="input-group input-group-sm">
                      <input
                        className="form-control"
                        type="datetime-local"
                        step="1"
                        value={priceDateByOrderId[order.id] ?? ''}
                        onChange={(e) => changePriceDate(order.id, e.target.value)}
                      />
                      <button
                        className="btn btn-outline-secondary"
                        type="button"
                        onClick={() => loadPriceAt(order.id)}
                        disabled={Boolean(priceLoadingByOrderId[order.id])}
                      >
                        {priceLoadingByOrderId[order.id] ? 'Loading...' : 'Show price'}
                      </button>
                    </div>
                    {priceErrorByOrderId[order.id] ? (
                      <div className="text-danger small mt-1">{priceErrorByOrderId[order.id]}</div>
                    ) : null}
                    {priceResult ? (
                      <div className="border rounded bg-light p-2 mt-2 small">
                        <div className="d-flex justify-content-between">
                          <span className="text-muted">At</span>
                          <span>
                            {priceResult.date
                              ? new Date(priceResult.date).toLocaleString()
                              : '-'}
                          </span>
                        </div>
                        <div className="d-flex justify-content-between">
                          <span className="text-muted">Subtotal</span>
                          <span>{formatMoney(priceResult.subtotal)}</span>
                        </div>
                        <div className="d-flex justify-content-between">
                          <span className="text-muted">Discount</span>
                          <span>{formatPercent(priceResult.discountPercent)}%</span>
                        </div>
                        <div className="d-flex justify-content-between fw-semibold">
                          <span>Total</span>
                          <span>{formatMoney(priceResult.total)}</span>
                        </div>
                        {Array.isArray(priceResult.items) &&
                        priceResult.items.length > 0 ? (
                          <div className="mt-2 d-flex flex-column gap-1">
                            {priceResult.items.map((item) => (
                              <div
                                key={`${order.id}-${item.itemId}`}
                                className="d-flex justify-content-between text-muted"
                              >
                                <span>{item.itemName} x{item.quantity}</span>
                                <span>{formatMoney(item.unitPrice)} / {formatMoney(item.subtotal)}</span>
                              </div>
                            ))}
                          </div>
                        ) : null}
                      </div>
                    ) : null}
                  </div>

                  {order.status === 'PENDING' ? (
                    <div className="mt-3 pt-3 border-top">
                      <label className="form-label small mb-1">Update shipping address</label>
                      <div className="input-group input-group-sm">
                        <input
                          className="form-control"
                          type="text"
                          maxLength="500"
                          value={shippingAddressByOrderId[order.id] ?? order.shippingAddress ?? ''}
                          onChange={(e) => changeShippingAddress(order.id, e.target.value)}
                          disabled={Boolean(shippingAddressLoadingByOrderId[order.id])}
                        />
                        <button
                          className="btn btn-outline-primary"
                          type="button"
                          onClick={() => saveShippingAddress(order.id, order.shippingAddress)}
                          disabled={Boolean(shippingAddressLoadingByOrderId[order.id])}
                        >
                          {shippingAddressLoadingByOrderId[order.id] ? 'Saving...' : 'Save address'}
                        </button>
                      </div>
                      {shippingAddressErrorByOrderId[order.id] ? (
                        <div className="text-danger small mt-1">{shippingAddressErrorByOrderId[order.id]}</div>
                      ) : null}
                      {shippingAddressSuccessByOrderId[order.id] ? (
                        <div className="text-success small mt-1">{shippingAddressSuccessByOrderId[order.id]}</div>
                      ) : null}
                    </div>
                  ) : null}

                  {isAdmin && order.status === 'PENDING' ? (
                    <div className="mt-3 pt-3 border-top">
                      <label className="form-label small mb-1">Admin: apply discount (%)</label>
                      <div className="input-group input-group-sm">
                        <input
                          className="form-control"
                          type="number"
                          min="0"
                          max="100"
                          step="0.01"
                          placeholder="0 – 100"
                          value={discountByOrderId[order.id] ?? ''}
                          onChange={(e) => changeDiscount(order.id, e.target.value)}
                          disabled={Boolean(discountLoadingByOrderId[order.id])}
                        />
                        <button
                          className="btn btn-outline-success"
                          type="button"
                          onClick={() => applyDiscount(order.id)}
                          disabled={Boolean(discountLoadingByOrderId[order.id])}
                        >
                          {discountLoadingByOrderId[order.id] ? 'Saving...' : 'Apply'}
                        </button>
                        {order.discountPercent > 0 ? (
                          <button
                            className="btn btn-outline-danger"
                            type="button"
                            onClick={() => removeDiscount(order.id)}
                            disabled={Boolean(discountLoadingByOrderId[order.id])}
                          >
                            Remove
                          </button>
                        ) : null}
                      </div>
                      {discountErrorByOrderId[order.id] ? (
                        <div className="text-danger small mt-1">{discountErrorByOrderId[order.id]}</div>
                      ) : null}
                      {discountSuccessByOrderId[order.id] ? (
                        <div className="text-success small mt-1">{discountSuccessByOrderId[order.id]}</div>
                      ) : null}
                    </div>
                  ) : null}

                  {isAdmin ? (
                    <div className="mt-3 pt-3 border-top">
                      <label className="form-label small mb-1">Admin: restore status by date and time</label>
                      <div className="input-group input-group-sm">
                        <input
                          className="form-control"
                          type="datetime-local"
                          step="1"
                          value={restoreDateByOrderId[order.id] ?? ''}
                          onChange={(e) => changeRestoreDate(order.id, e.target.value)}
                        />
                        <button
                          className="btn btn-outline-warning"
                          type="button"
                          onClick={() => restoreOrder(order.id)}
                          disabled={Boolean(restoreLoadingByOrderId[order.id])}
                        >
                          {restoreLoadingByOrderId[order.id] ? 'Restoring...' : 'Restore'}
                        </button>
                      </div>
                      {restoreErrorByOrderId[order.id] ? (
                        <div className="text-danger small mt-1">{restoreErrorByOrderId[order.id]}</div>
                      ) : null}
                      {restoreSuccessByOrderId[order.id] ? (
                        <div className="text-success small mt-1">{restoreSuccessByOrderId[order.id]}</div>
                      ) : null}
                    </div>
                  ) : null}

                  {expandedOrderIds[order.id] ? (
                    <div className="mt-3 pt-3 border-top">
                      {historyLoadingByOrderId[order.id] ? (
                        <p className="text-muted small mb-0">Loading timeline...</p>
                      ) : null}

                      {historyErrorByOrderId[order.id] ? (
                        <div className="alert alert-danger py-2 mb-0">{historyErrorByOrderId[order.id]}</div>
                      ) : null}

                      {!historyLoadingByOrderId[order.id] &&
                      !historyErrorByOrderId[order.id] &&
                      Array.isArray(historyByOrderId[order.id]) &&
                      historyByOrderId[order.id].length > 0 ? (
                        <div className="d-flex flex-column gap-3">
                          {historyByOrderId[order.id].map((event, index) => {
                            const eventKey = `${order.id}-${event.eventTimestamp}-${event.status}-${index}`;
                            const isLast = index === historyByOrderId[order.id].length - 1;

                            return (
                              <div key={eventKey} className="d-flex align-items-start gap-3">
                                <div className="d-flex flex-column align-items-center">
                                  <span className="rounded-circle bg-primary" style={{ width: 10, height: 10 }} />
                                  {!isLast ? (
                                    <span
                                      className="bg-secondary-subtle mt-1"
                                      style={{ width: 2, height: 30 }}
                                    />
                                  ) : null}
                                </div>
                                <div className="pb-1">
                                  <div className="fw-semibold">{toLabel(event.status)}</div>
                                  <div className="small text-muted">
                                    {event.eventTimestamp
                                      ? new Date(event.eventTimestamp).toLocaleString()
                                      : 'Unknown time'}
                                  </div>
                                </div>
                              </div>
                            );
                          })}
                        </div>
                      ) : null}

                      {!historyLoadingByOrderId[order.id] &&
                      !historyErrorByOrderId[order.id] &&
                      Array.isArray(historyByOrderId[order.id]) &&
                      historyByOrderId[order.id].length === 0 ? (
                        <p className="text-muted small mb-0">No timeline events for this order yet.</p>
                      ) : null}
                    </div>
                  ) : null}
                </div>
              );
            })}
          </div>
        ) : null}

        {!loading && !error ? (
          <div className="d-flex justify-content-between align-items-center mt-3">
            <button
              className="btn btn-outline-secondary btn-sm"
              type="button"
              disabled={page <= 0}
              onClick={() => onPageChange(Math.max(0, page - 1))}
            >
              Prev
            </button>
            <small className="text-muted">
              Page {page + 1} of {totalPages} · Orders: {totalElements}
            </small>
            <button
              className="btn btn-outline-secondary btn-sm"
              type="button"
              disabled={page >= totalPages - 1}
              onClick={() => onPageChange(Math.min(totalPages - 1, page + 1))}
            >
              Next
            </button>
          </div>
        ) : null}
      </div>
    </div>
  );
}
