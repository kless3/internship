import { useCallback, useEffect, useState } from 'react';
import api from '../api/axios.js';
import { normalizeApiError } from '../api/error-utils.js';
import Navbar from '../components/Navbar.jsx';
import OrderCreate from '../components/OrderCreate.jsx';
import OrderList from '../components/OrderList.jsx';
import PaymentList from '../components/PaymentList.jsx';
import { useAuth } from '../auth/useAuth.js';

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

  const [profile, setProfile] = useState(null);
  const [orders, setOrders] = useState([]);
  const [ordersLoading, setOrdersLoading] = useState(true);
  const [ordersError, setOrdersError] = useState('');
  const [payments, setPayments] = useState([]);
  const [paymentsLoading, setPaymentsLoading] = useState(true);
  const [paymentsError, setPaymentsError] = useState('');

  const loadOrders = useCallback(async () => {
    try {
      setOrdersLoading(true);
      setOrdersError('');

      const { data } = await api.get('/api/v1/orders/my');
      setOrders(Array.isArray(data) ? data : []);
    } catch (e) {
      setOrdersError(normalizeApiError(e, 'Failed to fetch orders.'));
    } finally {
      setOrdersLoading(false);
    }
  }, []);

  const loadPayments = useCallback(async (userId) => {
    if (!userId) {
      setPayments([]);
      setPaymentsLoading(false);
      return;
    }

    try {
      setPaymentsLoading(true);
      setPaymentsError('');
      const { data } = await api.get(`/api/v1/payments/user/${userId}`);
      setPayments(Array.isArray(data) ? data : []);
    } catch (e) {
      if (e.response?.status === 404) {
        setPayments([]);
        setPaymentsError('');
      } else {
        setPaymentsError(normalizeApiError(e, 'Failed to fetch payments.'));
      }
    } finally {
      setPaymentsLoading(false);
    }
  }, []);

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
        await Promise.all([loadOrders(), loadPayments(userProfile.id)]);
      } catch (e) {
        const normalizedMessage = normalizeApiError(e, 'Failed to load user profile.');
        setOrdersError(normalizedMessage);
        setPaymentsError(normalizedMessage);
        setOrdersLoading(false);
        setPaymentsLoading(false);
      }
    };

    loadProfileAndData();
  }, [tokenParsed, loadOrders, loadPayments]);

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
                  loadOrders();
                  loadPayments(profile.id);
                }
              }}
            />
          </div>

          <div className="col-12 col-xl-5">
            <OrderList
              orders={orders}
              loading={ordersLoading}
              error={ordersError}
              onRefresh={loadOrders}
            />
          </div>

          <div className="col-12 col-xl-4">
            <PaymentList
              payments={payments}
              loading={paymentsLoading}
              error={paymentsError}
              onRefresh={() => loadPayments(profile?.id)}
            />
          </div>
        </div>
      </section>
    </main>
  );
}
