import { useEffect, useMemo, useState } from 'react';
import api from '../api/axios.js';
import { normalizeApiError } from '../api/error-utils.js';

function ItemPriceChart({ points }) {
  if (!Array.isArray(points) || points.length === 0) {
    return null;
  }

  const width = 320;
  const height = 120;
  const padding = 16;

  const prices = points.map((point) => Number(point.price ?? 0));
  const min = Math.min(...prices);
  const max = Math.max(...prices);
  const range = max - min || 1;

  const xFor = (index) => {
    if (points.length === 1) {
      return width / 2;
    }
    return padding + (index / (points.length - 1)) * (width - padding * 2);
  };

  const yFor = (price) => padding + ((max - price) / range) * (height - padding * 2);

  const polyline = points
    .map((point, index) => `${xFor(index)},${yFor(Number(point.price ?? 0))}`)
    .join(' ');

  return (
    <div className="border rounded p-2 bg-light-subtle">
      <svg width="100%" viewBox={`0 0 ${width} ${height}`} preserveAspectRatio="none" role="img" aria-label="Item price chart for last 6 months">
        <polyline fill="none" stroke="#0d6efd" strokeWidth="2.5" points={polyline} />
      </svg>
      <div className="d-flex justify-content-between small text-muted mt-1">
        <span>Min: ${min.toFixed(2)}</span>
        <span>Max: ${max.toFixed(2)}</span>
      </div>
    </div>
  );
}

export default function OrderCreate({ userProfile, isAdmin, onCreated }) {
  const [catalog, setCatalog] = useState([]);
  const [catalogLoading, setCatalogLoading] = useState(true);
  const [catalogPage, setCatalogPage] = useState(0);
  const [catalogSize] = useState(10);
  const [totalPages, setTotalPages] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [selectedId, setSelectedId] = useState('');
  const [quantity, setQuantity] = useState(1);
  const [cart, setCart] = useState([]);
  const [shippingAddress, setShippingAddress] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [priceHistory, setPriceHistory] = useState([]);
  const [priceHistoryLoading, setPriceHistoryLoading] = useState(false);
  const [priceHistoryError, setPriceHistoryError] = useState('');
  const [newPrice, setNewPrice] = useState('');
  const [updatingPrice, setUpdatingPrice] = useState(false);
  const [updatePriceError, setUpdatePriceError] = useState('');

  const selectedProduct = useMemo(
    () => catalog.find((item) => item.id === Number(selectedId)),
    [catalog, selectedId]
  );

  const totalAmount = cart.reduce((sum, line) => sum + line.item.price * line.quantity, 0);

  useEffect(() => {
    const loadCatalog = async () => {
      try {
        setCatalogLoading(true);
        setError('');

        const { data } = await api.get('/api/v1/items', {
          params: { page: catalogPage, size: catalogSize }
        });

        const content = Array.isArray(data?.content) ? data.content : [];
        const resolvedTotalPages =
          Number.isFinite(Number(data?.totalPages)) && Number(data.totalPages) > 0
            ? Number(data.totalPages)
            : 1;

        setCatalog(content);
        setTotalPages(resolvedTotalPages);
        setTotalElements(Number.isFinite(Number(data?.totalElements)) ? Number(data.totalElements) : content.length);

        const backendPage = Number.isFinite(Number(data?.number)) ? Number(data.number) : catalogPage;
        if (backendPage !== catalogPage) {
          setCatalogPage(backendPage);
        }
      } catch (e) {
        setError(normalizeApiError(e, 'Failed to load catalog.'));
      } finally {
        setCatalogLoading(false);
      }
    };

    loadCatalog();
  }, [catalogPage, catalogSize]);

  useEffect(() => {
    setSelectedId('');
  }, [catalogPage]);

  useEffect(() => {
    const loadPriceHistory = async () => {
      if (!selectedProduct?.id) {
        setPriceHistory([]);
        setPriceHistoryError('');
        setPriceHistoryLoading(false);
        return;
      }

      try {
        setPriceHistoryLoading(true);
        setPriceHistoryError('');
        const { data } = await api.get(`/api/v1/items/${selectedProduct.id}/price/history`, {
          params: { months: 6 }
        });
        setPriceHistory(Array.isArray(data) ? data : []);
      } catch (e) {
        setPriceHistory([]);
        setPriceHistoryError(normalizeApiError(e, 'Failed to load item price history.'));
      } finally {
        setPriceHistoryLoading(false);
      }
    };

    loadPriceHistory();
  }, [selectedProduct?.id]);

  useEffect(() => {
    setNewPrice(selectedProduct?.price != null ? String(selectedProduct.price) : '');
    setUpdatePriceError('');
  }, [selectedProduct?.id, selectedProduct?.price]);

  const addItem = () => {
    if (!selectedProduct || quantity < 1) return;

    const exists = cart.some((line) => line.item.id === selectedProduct.id);
    if (exists) {
      setError('Item already exists in current order.');
      return;
    }

    setError('');
    setCart((prev) => [...prev, { item: selectedProduct, quantity: Number(quantity) }]);
    setSelectedId('');
    setQuantity(1);
  };

  const removeItem = (itemId) => {
    setCart((prev) => prev.filter((line) => line.item.id !== itemId));
  };

  const submitOrder = async () => {
    if (!userProfile) return;
    if (cart.length === 0) {
      setError('Add at least one item.');
      return;
    }
    if (!shippingAddress.trim()) {
      setError('Shipping address is required.');
      return;
    }

    try {
      setSubmitting(true);
      setError('');

      const payload = {
        userId: userProfile.id,
        userEmail: userProfile.email,
        shippingAddress: shippingAddress.trim(),
        orderItems: cart.map((line) => ({
          item: {
            id: line.item.id,
            name: line.item.name,
            price: line.item.price
          },
          quantity: line.quantity
        }))
      };

      await api.post('/api/v1/orders', payload);

      setCart([]);
      setShippingAddress('');
      onCreated();
    } catch (e) {
      setError(normalizeApiError(e, 'Failed to create order.'));
    } finally {
      setSubmitting(false);
    }
  };

  const updateItemPrice = async () => {
    if (!selectedProduct?.id) {
      return;
    }

    const parsedPrice = Number(newPrice);
    if (!Number.isFinite(parsedPrice) || parsedPrice <= 0) {
      setUpdatePriceError('Enter a valid price greater than 0.');
      return;
    }

    try {
      setUpdatingPrice(true);
      setUpdatePriceError('');

      const { data } = await api.put(`/api/v1/items/${selectedProduct.id}/price`, {
        price: Number(parsedPrice.toFixed(2))
      });

      setCatalog((prev) =>
        prev.map((item) => (item.id === selectedProduct.id ? { ...item, price: data?.price ?? item.price } : item))
      );

      const { data: historyData } = await api.get(`/api/v1/items/${selectedProduct.id}/price/history`, {
        params: { months: 6 }
      });
      setPriceHistory(Array.isArray(historyData) ? historyData : []);
    } catch (e) {
      setUpdatePriceError(normalizeApiError(e, 'Failed to update item price.'));
    } finally {
      setUpdatingPrice(false);
    }
  };

  return (
    <div className="card shadow-sm h-100">
      <div className="card-body">
        <h5 className="card-title">Create Order</h5>
        <p className="text-muted small">Pick products from catalog and submit one order.</p>

        <div className="mb-3">
          <label className="form-label">Product</label>
          <select className="form-select" value={selectedId} onChange={(e) => setSelectedId(e.target.value)}>
            <option value="">Choose product</option>
            {catalog.map((item) => (
              <option key={item.id} value={item.id}>
                {item.name} - ${item.price}
              </option>
            ))}
          </select>
        </div>

        <div className="d-flex justify-content-between align-items-center mb-3">
          <button
            className="btn btn-outline-secondary btn-sm"
            type="button"
            disabled={catalogLoading || catalogPage <= 0}
            onClick={() => setCatalogPage((prev) => Math.max(0, prev - 1))}
          >
            Prev
          </button>
          <small className="text-muted">
            Page {catalogPage + 1} of {totalPages} · Items: {totalElements}
          </small>
          <button
            className="btn btn-outline-secondary btn-sm"
            type="button"
            disabled={catalogLoading || catalogPage >= totalPages - 1}
            onClick={() => setCatalogPage((prev) => Math.min(totalPages - 1, prev + 1))}
          >
            Next
          </button>
        </div>

        <div className="row g-2 mb-3 align-items-end">
          <div className="col-5">
            <label className="form-label">Qty</label>
            <input
              className="form-control"
              type="number"
              min="1"
              value={quantity}
              onChange={(e) => setQuantity(e.target.value)}
            />
          </div>
          <div className="col-7">
            <button
              className="btn btn-outline-primary w-100"
              type="button"
              onClick={addItem}
              disabled={!selectedProduct || catalogLoading}
            >
              Add item
            </button>
          </div>
        </div>

        <div className="mb-3">
          <label className="form-label">Shipping address</label>
          <input
            className="form-control"
            type="text"
            maxLength="500"
            placeholder="Enter shipping address"
            value={shippingAddress}
            onChange={(e) => setShippingAddress(e.target.value)}
          />
        </div>

        <ul className="list-group mb-3">
          {cart.map((line) => (
            <li key={line.item.id} className="list-group-item d-flex justify-content-between align-items-start gap-2">
              <div>
                <div className="fw-semibold">{line.item.name}</div>
                <small className="text-muted">x{line.quantity} · ${(line.item.price * line.quantity).toFixed(2)}</small>
              </div>
              <button className="btn btn-sm btn-outline-danger" onClick={() => removeItem(line.item.id)}>Remove</button>
            </li>
          ))}
        </ul>

        {selectedProduct ? (
          <div className="mb-3">
            <div className="d-flex justify-content-between align-items-center mb-2">
              <span className="fw-semibold small">Price history for {selectedProduct.name}</span>
              <span className="badge text-bg-light border">6 months</span>
            </div>
            {priceHistoryLoading ? <div className="text-muted small mb-2">Loading price history...</div> : null}
            {priceHistoryError ? <div className="alert alert-danger py-2 mb-2">{priceHistoryError}</div> : null}
            {!priceHistoryLoading && !priceHistoryError && priceHistory.length > 0 ? (
              <ItemPriceChart points={priceHistory} />
            ) : null}
            {!priceHistoryLoading && !priceHistoryError && priceHistory.length === 0 ? (
              <div className="text-muted small">No price history points available yet.</div>
            ) : null}
          </div>
        ) : null}

        {isAdmin && selectedProduct ? (
          <div className="mb-3 border rounded p-2">
            <label className="form-label mb-1">Admin: update price</label>
            <div className="input-group input-group-sm">
              <span className="input-group-text">$</span>
              <input
                className="form-control"
                type="number"
                min="0.01"
                step="0.01"
                value={newPrice}
                onChange={(e) => setNewPrice(e.target.value)}
                disabled={updatingPrice}
              />
              <button
                className="btn btn-outline-primary"
                type="button"
                disabled={updatingPrice}
                onClick={updateItemPrice}
              >
                {updatingPrice ? 'Saving...' : 'Save'}
              </button>
            </div>
            {updatePriceError ? <div className="text-danger small mt-1">{updatePriceError}</div> : null}
          </div>
        ) : null}

        <div className="d-flex justify-content-between mb-3">
          <span className="text-muted">Total</span>
          <strong>${totalAmount.toFixed(2)}</strong>
        </div>

        {catalogLoading ? <div className="text-muted small mb-2">Loading items...</div> : null}
        {!catalogLoading && catalog.length === 0 ? (
          <div className="text-muted small mb-2">No catalog items on this page.</div>
        ) : null}
        {error ? <div className="alert alert-danger py-2">{error}</div> : null}
        <button className="btn btn-primary w-100" onClick={submitOrder} disabled={submitting || !userProfile}>
          {submitting ? 'Submitting...' : 'Place order'}
        </button>
      </div>
    </div>
  );
}
