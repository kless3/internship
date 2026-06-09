import { useCallback, useEffect, useState } from 'react';
import {
  applyDiscount as applyDiscountRequest,
  fetchOrderById,
  fetchCurrentUserOrders,
  fetchOrderHistory,
  fetchOrderPriceAt,
  payOrder as payOrderRequest,
  removeDiscount as removeDiscountRequest,
  restoreOrder,
  updateOrderShippingAddress
} from '../api/orders.js';
import { fetchPaymentReceipt, fetchPaymentsByOrder, fetchPaymentsByUser } from '../api/payments.js';
import { fetchAverageDurationMetric, fetchShippingAddressChangeFrequencyMetric } from '../api/metrics.js';
import { createUser, fetchUserByEmail, fetchUsers } from '../api/users.js';
import { normalizeApiError } from '../api/error-utils.js';
import { isAdminUser } from '../auth/roles.js';
import { useAuth } from '../auth/useAuth.js';
import Navbar from '../components/Navbar.jsx';
import OrderCreate from '../components/OrderCreate.jsx';
import OrderList from '../components/OrderList.jsx';
import PaymentList from '../components/PaymentList.jsx';
import CreateToPayMetricsList from '../components/CreateToPayMetricsList.jsx';
import UsersList from '../components/UsersList.jsx';

const FALLBACK_BIRTH_DATE = '1970-01-01';
const PAYMENT_RESULT_POLL_ATTEMPTS = 8;
const PAYMENT_RESULT_POLL_DELAY_MS = 1000;

function delay(ms) {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms);
  });
}

function toProfileFromClaims(claims) {
  const email = claims?.email ?? claims?.preferred_username ?? claims?.sub ?? '';
  if (!email) {
    return null;
  }

  return {
    email,
    name: claims?.given_name ?? claims?.name ?? 'User',
    surname: claims?.family_name ?? 'User',
    birthDate: FALLBACK_BIRTH_DATE
  };
}

export default function OrdersPage() {
  const { tokenParsed } = useAuth();
  const isAdmin = isAdminUser(tokenParsed);

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
  const [users, setUsers] = useState([]);
  const [usersLoading, setUsersLoading] = useState(false);
  const [usersError, setUsersError] = useState('');
  const [metricsByUserId, setMetricsByUserId] = useState({});
  const [metricsLoading, setMetricsLoading] = useState(false);
  const [metricsError, setMetricsError] = useState('');

  const loadOrders = useCallback(async (targetPage = 0) => {
    try {
      setOrdersLoading(true);
      setOrdersError('');

      const data = await fetchCurrentUserOrders(targetPage, ordersPageSize);

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

  const loadCreateToPayMetrics = useCallback(async (usersList) => {
    if (!isAdmin || !Array.isArray(usersList) || usersList.length === 0) {
      setMetricsByUserId({});
      setMetricsError('');
      setMetricsLoading(false);
      return;
    }

    try {
      setMetricsLoading(true);
      setMetricsError('');

      const metricEntries = await Promise.all(
        usersList.map(async (user) => {
          try {
            const [averageDurationResponse, shippingAddressFrequencyResponse] = await Promise.all([
              fetchAverageDurationMetric(user.id),
              fetchShippingAddressChangeFrequencyMetric(user.id)
            ]);

            return [
              user.id,
              {
                ...(averageDurationResponse ?? {}),
                ...(shippingAddressFrequencyResponse ?? {})
              }
            ];
          } catch {
            return [
              user.id,
              {
                samplesCount: 0,
                averageDurationMs: 0,
                averageDurationSeconds: 0,
                totalCreatedOrders: 0,
                ordersWithAddressChanges: 0,
                changeRatePercent: 0
              }
            ];
          }
        })
      );

      setMetricsByUserId(Object.fromEntries(metricEntries));
    } catch (e) {
      setMetricsError(normalizeApiError(e, 'Failed to fetch metrics.'));
    } finally {
      setMetricsLoading(false);
    }
  }, [isAdmin]);

  const loadUsers = useCallback(async () => {
    if (!isAdmin) {
      setUsers([]);
      setUsersError('');
      setUsersLoading(false);
      setMetricsByUserId({});
      setMetricsError('');
      setMetricsLoading(false);
      return;
    }

    try {
      setUsersLoading(true);
      setUsersError('');
      const resolvedUsers = await fetchUsers();
      setUsers(resolvedUsers);
      await loadCreateToPayMetrics(resolvedUsers);
    } catch (e) {
      setUsersError(normalizeApiError(e, 'Failed to fetch users.'));
      setMetricsByUserId({});
      setMetricsError('');
      setMetricsLoading(false);
    } finally {
      setUsersLoading(false);
    }
  }, [isAdmin, loadCreateToPayMetrics]);

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
      const data = await fetchPaymentsByUser(userId, targetPage, paymentsPageSize);

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


  const loadOrderHistory = useCallback(async (orderId) => {
    return fetchOrderHistory(orderId);
  }, []);

  const loadOrderPriceAt = useCallback(async (orderId, date) => {
    return fetchOrderPriceAt(orderId, date);
  }, []);

  const restoreOrderStatus = useCallback(async (orderId, date) => {
    return restoreOrder(orderId, date);
  }, []);

  const waitForPaymentResult = useCallback(async (orderId) => {
    for (let attempt = 0; attempt < PAYMENT_RESULT_POLL_ATTEMPTS; attempt += 1) {
      await delay(PAYMENT_RESULT_POLL_DELAY_MS);

      const [order, orderPayments] = await Promise.all([
        fetchOrderById(orderId).catch(() => null),
        fetchPaymentsByOrder(orderId).catch(() => [])
      ]);

      await Promise.all([loadOrders(0), loadPayments(profile?.id, 0)]);

      const hasPayment = Array.isArray(orderPayments) && orderPayments.length > 0;
      const status = order?.status;

      if (hasPayment && status && status !== 'PENDING' && status !== 'PROCESSING') {
        return true;
      }
    }

    return false;
  }, [loadOrders, loadPayments, profile?.id]);

  const payOrder = useCallback(async (orderId) => {
    await payOrderRequest(orderId);
    await Promise.all([loadOrders(0), loadPayments(profile?.id, 0)]);
    return waitForPaymentResult(orderId);
  }, [loadOrders, loadPayments, profile?.id, waitForPaymentResult]);

  const openPaymentReceipt = useCallback(async (orderId) => {
    const receiptWindow = window.open('', '_blank');
    if (!receiptWindow) {
      throw new Error('Allow popups to open the receipt.');
    }

    try {
      const orderPayments = await fetchPaymentsByOrder(orderId);
      const latestPayment = [...orderPayments]
        .filter((payment) => payment?.id)
        .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))[0];

      if (!latestPayment) {
        throw new Error('No payment found for this order yet.');
      }

      if (!latestPayment.receiptKey) {
        throw new Error('Receipt is not available for this payment yet.');
      }

      const receipt = await fetchPaymentReceipt(latestPayment.id);
      const receiptUrl = URL.createObjectURL(receipt);

      receiptWindow.location.href = receiptUrl;
      window.setTimeout(() => URL.revokeObjectURL(receiptUrl), 60000);
    } catch (error) {
      receiptWindow.close();
      throw error;
    }
  }, []);

  const updateShippingAddress = useCallback(async (orderId, shippingAddress) => {
    await updateOrderShippingAddress(orderId, shippingAddress);
    await loadOrders(0);
  }, [loadOrders]);

  const handleApplyDiscount = useCallback(async (orderId, discountPercent) => {
    if (discountPercent === 0) {
      await removeDiscountRequest(orderId);
    } else {
      await applyDiscountRequest(orderId, discountPercent);
    }
    await loadOrders(0);
  }, [loadOrders]);


  useEffect(() => {
    const loadProfileAndData = async () => {
      try {
        const profileDraft = toProfileFromClaims(tokenParsed);
        const login = profileDraft?.email;

        if (!login) {
          const error = 'Invalid token payload.';
          setOrdersError(error);
          setPaymentsError(error);
          setOrdersLoading(false);
          setPaymentsLoading(false);
          return;
        }

        let userProfile;

        try {
          userProfile = await fetchUserByEmail(login);
        } catch (e) {
          if (e.response?.status === 404 && profileDraft) {
            userProfile = await createUser({
              name: profileDraft.name,
              surname: profileDraft.surname,
              birthDate: profileDraft.birthDate,
              email: profileDraft.email
            });
          } else {
            throw e;
          }
        }

        setProfile(userProfile);
        await Promise.all([loadOrders(), loadPayments(userProfile.id), loadUsers()]);
      } catch (e) {
        const normalizedMessage = normalizeApiError(e, 'Failed to load user profile.');
        setOrdersError(normalizedMessage);
        setPaymentsError(normalizedMessage);
        setOrdersLoading(false);
        setPaymentsLoading(false);
        setUsersLoading(false);
      }
    };

    loadProfileAndData();
  }, [tokenParsed, loadOrders, loadPayments, loadUsers]);

  return (
    <main className="bg-body-tertiary min-vh-100">
      <Navbar userEmail={profile?.email ?? 'unknown'} />

      <section className="container-fluid py-4">
        <div className="row g-4">
          <div className="col-12 col-xl-3">
            <OrderCreate
              userProfile={profile}
              isAdmin={isAdmin}
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
              isAdmin={isAdmin}
              page={ordersPage}
              totalPages={ordersTotalPages}
              totalElements={ordersTotalElements}
              loading={ordersLoading}
              error={ordersError}
              onRefresh={() => loadOrders(ordersPage)}
              loadOrderHistory={loadOrderHistory}
              onLoadOrderPrice={loadOrderPriceAt}
              onRestoreOrder={restoreOrderStatus}
              onPayOrder={payOrder}
              onOpenPaymentReceipt={openPaymentReceipt}
              onUpdateShippingAddress={updateShippingAddress}
              onApplyDiscount={handleApplyDiscount}
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

          {isAdmin ? (
            <div className="col-12">
              <UsersList
                users={users}
                loading={usersLoading}
                error={usersError}
                onRefresh={loadUsers}
              />
            </div>
          ) : null}

          {isAdmin ? (
            <div className="col-12">
              <CreateToPayMetricsList
                users={users}
                metricsByUserId={metricsByUserId}
                loading={metricsLoading}
                error={metricsError}
                onRefresh={() => loadCreateToPayMetrics(users)}
              />
            </div>
          ) : null}

        </div>
      </section>
    </main>
  );
}
