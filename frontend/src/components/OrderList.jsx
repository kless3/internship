import { useMemo, useState } from 'react';
import { normalizeApiError } from '../api/error-utils.js';

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
  onRestoreOrder
}) {
  const [expandedOrderIds, setExpandedOrderIds] = useState({});
  const [historyByOrderId, setHistoryByOrderId] = useState({});
  const [historyLoadingByOrderId, setHistoryLoadingByOrderId] = useState({});
  const [historyErrorByOrderId, setHistoryErrorByOrderId] = useState({});
  const [restoreDateByOrderId, setRestoreDateByOrderId] = useState({});
  const [restoreLoadingByOrderId, setRestoreLoadingByOrderId] = useState({});
  const [restoreErrorByOrderId, setRestoreErrorByOrderId] = useState({});
  const [restoreSuccessByOrderId, setRestoreSuccessByOrderId] = useState({});

  const sortedOrders = useMemo(
    () => [...orders].sort((a, b) => new Date(b.creationDate) - new Date(a.creationDate)),
    [orders]
  );

  const toLabel = (value) =>
    String(value ?? '')
      .toLowerCase()
      .replaceAll('_', ' ')
      .replace(/\b\w/g, (char) => char.toUpperCase());

  const toggleTimeline = async (orderId) => {
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

    try {
      setHistoryLoadingByOrderId((prev) => ({ ...prev, [orderId]: true }));
      setHistoryErrorByOrderId((prev) => ({ ...prev, [orderId]: '' }));

      const history = await loadOrderHistory(orderId);
      setHistoryByOrderId((prev) => ({ ...prev, [orderId]: history }));
    } catch {
      setHistoryErrorByOrderId((prev) => ({ ...prev, [orderId]: 'Failed to load order timeline.' }));
    } finally {
      setHistoryLoadingByOrderId((prev) => ({ ...prev, [orderId]: false }));
    }
  };

  const toRestoreRequestDate = (date) => {
    if (!date) {
      return '';
    }
    return date.length === 16 ? `${date}:59` : date;
  };

  const restoreOrder = async (orderId) => {
    if (!orderId || typeof onRestoreOrder !== 'function') {
      return;
    }

    const selectedDate = restoreDateByOrderId[orderId];
    const requestDate = toRestoreRequestDate(selectedDate);
    if (!requestDate) {
      setRestoreErrorByOrderId((prev) => ({ ...prev, [orderId]: 'Select date and time first.' }));
      return;
    }

    try {
      setRestoreLoadingByOrderId((prev) => ({ ...prev, [orderId]: true }));
      setRestoreErrorByOrderId((prev) => ({ ...prev, [orderId]: '' }));
      setRestoreSuccessByOrderId((prev) => ({ ...prev, [orderId]: '' }));

      await onRestoreOrder(orderId, requestDate);
      await Promise.resolve(onRefresh?.());

      if (typeof loadOrderHistory === 'function') {
        setHistoryLoadingByOrderId((prev) => ({ ...prev, [orderId]: true }));
        const refreshedHistory = await loadOrderHistory(orderId);
        setHistoryByOrderId((prev) => ({ ...prev, [orderId]: refreshedHistory }));
      }

      setRestoreSuccessByOrderId((prev) => ({ ...prev, [orderId]: 'Status restored successfully.' }));
    } catch (e) {
      setRestoreErrorByOrderId((prev) => ({
        ...prev,
        [orderId]: normalizeApiError(e, 'Failed to restore order status.')
      }));
    } finally {
      setRestoreLoadingByOrderId((prev) => ({ ...prev, [orderId]: false }));
      setHistoryLoadingByOrderId((prev) => ({ ...prev, [orderId]: false }));
    }
  };

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

              return (
                <div key={order.id ?? `${order.creationDate}-${order.status}-${order.userId}`} className="border rounded p-3">
                  <div className="d-flex justify-content-between">
                    <span className="badge text-bg-primary">{order.status}</span>
                    <strong>${total.toFixed(2)}</strong>
                  </div>
                  <div className="small text-muted mt-2">{new Date(order.creationDate).toLocaleString()}</div>
                  <div className="d-flex flex-wrap gap-2 mt-2">
                    {(order.orderItems ?? []).map((line) => (
                      <span key={`${order.id}-${line.item?.id}`} className="badge text-bg-light border">
                        {line.item?.name} x{line.quantity}
                      </span>
                    ))}
                  </div>

                  <div className="mt-3">
                    <button
                      className="btn btn-sm btn-outline-primary"
                      type="button"
                      onClick={() => toggleTimeline(order.id)}
                    >
                      {expandedOrderIds[order.id] ? 'Hide timeline' : 'Show timeline'}
                    </button>
                  </div>

                  {isAdmin ? (
                    <div className="mt-3 pt-3 border-top">
                      <label className="form-label small mb-1">Admin: restore status by date and time</label>
                      <div className="input-group input-group-sm">
                        <input
                          className="form-control"
                          type="datetime-local"
                          step="1"
                          value={restoreDateByOrderId[order.id] ?? ''}
                          onChange={(e) => {
                            const nextValue = e.target.value;
                            setRestoreDateByOrderId((prev) => ({ ...prev, [order.id]: nextValue }));
                            setRestoreErrorByOrderId((prev) => ({ ...prev, [order.id]: '' }));
                            setRestoreSuccessByOrderId((prev) => ({ ...prev, [order.id]: '' }));
                          }}
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
