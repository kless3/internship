import { useCallback, useEffect, useState } from 'react';
import api from '../api/axios.js';
import { normalizeApiError } from '../api/error-utils.js';
import { useAuth } from '../auth/useAuth.js';
import Navbar from '../components/Navbar.jsx';
import OrderCreate from '../components/OrderCreate.jsx';
import OrderList from '../components/OrderList.jsx';
import PaymentList from '../components/PaymentList.jsx';
import UsersList from '../components/UsersList.jsx';

const FALLBACK_BIRTH_DATE = '1970-01-01';

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
  const isAdmin = Array.isArray(tokenParsed?.realm_access?.roles)
    ? tokenParsed.realm_access.roles.some((role) => String(role).toLowerCase() === 'admin')
    : false;

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

  const loadUsers = useCallback(async () => {
    if (!isAdmin) {
      setUsers([]);
      setUsersError('');
      setUsersLoading(false);
      return;
    }

    try {
      setUsersLoading(true);
      setUsersError('');
      const { data } = await api.get('/api/v1/users');
      setUsers(Array.isArray(data) ? data : []);
    } catch (e) {
      setUsersError(normalizeApiError(e, 'Failed to fetch users.'));
    } finally {
      setUsersLoading(false);
    }
  }, [isAdmin]);

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
          const { data } = await api.get(`/api/v1/users/email/${encodeURIComponent(login)}`);
          userProfile = data;
        } catch (e) {
          if (e.response?.status === 404 && profileDraft) {
            const { data } = await api.post('/api/v1/users', {
              name: profileDraft.name,
              surname: profileDraft.surname,
              birthDate: profileDraft.birthDate,
              email: profileDraft.email
            });
            userProfile = data;
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
        </div>
      </section>
    </main>
  );
}
