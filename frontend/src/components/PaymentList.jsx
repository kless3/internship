import { useMemo } from 'react';

export default function PaymentList({
  payments,
  page,
  totalPages,
  totalElements,
  loading,
  error,
  onRefresh,
  onPageChange
}) {
  const sortedPayments = useMemo(
    () => [...payments].sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp)),
    [payments]
  );

  return (
    <div className="card shadow-sm h-100">
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-start mb-3">
          <div>
            <h5 className="card-title mb-1">Payments</h5>
            <p className="text-muted small mb-0">Your payment history by user id.</p>
          </div>
          <button className="btn btn-outline-secondary btn-sm" onClick={onRefresh}>Refresh</button>
        </div>

        {loading ? <p className="mb-0">Loading payments...</p> : null}
        {error ? <div className="alert alert-danger py-2">{error}</div> : null}

        {!loading && !error && sortedPayments.length === 0 ? <p className="text-muted mb-0">No payments yet.</p> : null}

        {!loading && !error && sortedPayments.length > 0 ? (
          <div className="vstack gap-3">
            {sortedPayments.map((payment) => (
              <div key={payment.id} className="border rounded p-3">
                <div className="d-flex justify-content-between">
                  <span className="badge text-bg-info">{payment.status}</span>
                  <strong>${Number(payment.paymentAmount ?? 0).toFixed(2)}</strong>
                </div>
                <div className="small text-muted mt-2">{new Date(payment.timestamp).toLocaleString()}</div>
                <div className="d-flex flex-wrap gap-2 mt-2">
                  <span className="badge text-bg-light border">Payment #{payment.id}</span>
                  <span className="badge text-bg-light border">Order #{payment.orderId}</span>
                </div>
              </div>
            ))}
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
              Page {page + 1} of {totalPages} · Payments: {totalElements}
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
