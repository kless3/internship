import { useCallback, useEffect, useState } from 'react';
import api from '../api/axios.js';
import { normalizeApiError } from '../api/error-utils.js';
import { useAuth } from '../auth/useAuth.js';
import Navbar from '../components/Navbar.jsx';
import OrderCreate from '../components/OrderCreate.jsx';
import OrderList from '../components/OrderList.jsx';
import PaymentList from '../components/PaymentList.jsx';

function parseJwt(token) {
  try {
    const base64Payload = token.split('.')[1];
    return JSON.parse(atob(base64Payload));
  } catch {
    return null;
  }
}

export default function OrdersPage() {
  const [profile, setProfile] = useState(null);
  const [orders, setOrders] = useState([]);
  const [ordersPage, setOrdersPage] = useState(0);
  const [ordersPageSize] = useState(10);
  const [ordersTotalPages, setOrdersTotalPages] = useState(1);
  const [ordersTotalElements, setOrdersTotalElements] = useState(0);
  const [ordersLoading, setOrdersLoading] = useState(true);
  const [ordersError, setOrdersError] = useState('');
  const [payments, setPayments] = useState([]);
  const [paymentsPage, setPaymentsPage] = useState(0);
  const [paymentsPageSize] = useState(10);
  const [paymentsTotalPages, setPaymentsTotalPages] = useState(1);
  const [paymentsTotalElements, setPaymentsTotalElements] = useState(0);
  const [paymentsLoading, setPaymentsLoading] = useState(true);
  const [paymentsError, setPaymentsError] = useState('');
  const { accessToken } = useAuth();

  const loadOrders = useCallback(async (targetPage = 0) => {
    try {
      setOrdersLoading(true);
      setOrdersError('');

      const { data } = await api.get('/api/v1/orders/current', {
        params: {
          page: targetPage,
          size: ordersPageSize
        }
      });

      const content = Array.isArray(data?.content) ? data.content : [];
      const currentPage =
        Number.isFinite(Number(data?.number)) && Number(data.number) >= 0
          ? Number(data.number)
          : targetPage;
      const totalPages =
        Number.isFinite(Number(data?.totalPages)) && Number(data.totalPages) > 0
          ? Number(data.totalPages)
          : 1;
      const totalElements =
        Number.isFinite(Number(data?.totalElements)) && Number(data.totalElements) >= 0
          ? Number(data.totalElements)
          : content.length;

      setOrders(content);
      setOrdersPage(currentPage);
      setOrdersTotalPages(totalPages);
      setOrdersTotalElements(totalElements);
    } catch (e) {
      setOrdersError(normalizeApiError(e, 'Failed to fetch orders.'));
    } finally {
      setOrdersLoading(false);
    }
  }, [ordersPageSize]);

  const loadPayments = useCallback(async (userId, targetPage = 0) => {
    if (!userId) {
      setPayments([]);
      setPaymentsPage(0);
      setPaymentsTotalPages(1);
      setPaymentsTotalElements(0);
      setPaymentsLoading(false);
      return;
    }

    try {
      setPaymentsLoading(true);
      setPaymentsError('');
      const { data } = await api.get(`/api/v1/payments/user/${userId}`, {
        params: {
          page: targetPage,
          size: paymentsPageSize
        }
      });

      const content = Array.isArray(data?.content) ? data.content : [];
      const currentPage =
        Number.isFinite(Number(data?.number)) && Number(data.number) >= 0
          ? Number(data.number)
          : targetPage;
      const totalPages =
        Number.isFinite(Number(data?.totalPages)) && Number(data.totalPages) > 0
          ? Number(data.totalPages)
          : 1;
      const totalElements =
        Number.isFinite(Number(data?.totalElements)) && Number(data.totalElements) >= 0
          ? Number(data.totalElements)
          : content.length;

      setPayments(content);
      setPaymentsPage(currentPage);
      setPaymentsTotalPages(totalPages);
      setPaymentsTotalElements(totalElements);
    } catch (e) {
      if (e.response?.status === 404) {
        setPayments([]);
        setPaymentsPage(0);
        setPaymentsTotalPages(1);
        setPaymentsTotalElements(0);
        setPaymentsError('');
      } else {
        setPaymentsError(normalizeApiError(e, 'Failed to fetch payments.'));
      }
    } finally {
      setPaymentsLoading(false);
    }
  }, [paymentsPageSize]);

  useEffect(() => {
    const loadProfileAndData = async () => {
      try {
        if (!accessToken) {
          setOrdersError('Missing access token. Please sign in again.');
          setPaymentsError('Missing access token. Please sign in again.');
          setOrdersLoading(false);
          setPaymentsLoading(false);
          return;
        }

        const payload = parseJwt(accessToken);
        const login = payload?.sub;

        if (!login) {
          setOrdersError('Invalid token payload.');
          setPaymentsError('Invalid token payload.');
          setOrdersLoading(false);
          setPaymentsLoading(false);
          return;
        }

        const { data } = await api.get(`/api/v1/users/email/${encodeURIComponent(login)}`);
        setProfile(data);
        await Promise.all([loadOrders(0), loadPayments(data.id, 0)]);
      } catch (e) {
        const normalizedMessage = normalizeApiError(e, 'Failed to load user profile.');
        setOrdersError(normalizedMessage);
        setPaymentsError(normalizedMessage);
        setOrdersLoading(false);
        setPaymentsLoading(false);
      }
    };

    loadProfileAndData();
  }, [accessToken, loadOrders, loadPayments]);

  return (
    <main className="bg-body-tertiary min-vh-100">
      <Navbar userEmail={profile?.email ?? 'unknown'} />

      <section className="container-fluid py-4">
        <div className="row g-4">
          <div className="col-12 col-xl-3">
            <OrderCreate
              userProfile={profile}
              onCreated={() => {
                if (profile?.id || profile?.email) {
                  loadOrders(0);
                  loadPayments(profile.id, 0);
                }
              }}
            />
          </div>

          <div className="col-12 col-xl-5">
            <OrderList
              orders={orders}
              page={ordersPage}
              totalPages={ordersTotalPages}
              totalElements={ordersTotalElements}
              loading={ordersLoading}
              error={ordersError}
              onRefresh={() => loadOrders(ordersPage)}
              onPageChange={loadOrders}
            />
          </div>

          <div className="col-12 col-xl-4">
            <PaymentList
              payments={payments}
              page={paymentsPage}
              totalPages={paymentsTotalPages}
              totalElements={paymentsTotalElements}
              loading={paymentsLoading}
              error={paymentsError}
              onRefresh={() => loadPayments(profile?.id, paymentsPage)}
              onPageChange={(nextPage) => loadPayments(profile?.id, nextPage)}
            />
          </div>
        </div>
      </section>
    </main>
  );
}
