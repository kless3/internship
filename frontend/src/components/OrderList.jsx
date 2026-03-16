import { useMemo } from 'react';

export default function OrderList({ orders, loading, error, onRefresh }) {
  const sortedOrders = useMemo(
    () => [...orders].sort((a, b) => new Date(b.creationDate) - new Date(a.creationDate)),
    [orders]
  );

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
                </div>
              );
            })}
          </div>
        ) : null}
      </div>
    </div>
  );
}
