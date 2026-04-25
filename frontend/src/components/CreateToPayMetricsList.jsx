export default function CreateToPayMetricsList({
  users,
  metricsByUserId,
  loading,
  error,
  onRefresh
}) {
  return (
    <div className="card shadow-sm h-100">
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-start mb-3">
          <div>
            <h5 className="card-title mb-1">Create to Pay Metric</h5>
            <p className="text-muted small mb-0">Average time between order creation and pay click.</p>
          </div>
          <button className="btn btn-outline-secondary btn-sm" onClick={onRefresh}>Refresh</button>
        </div>

        {loading ? <p className="mb-0">Loading metric...</p> : null}
        {error ? <div className="alert alert-danger py-2">{error}</div> : null}

        {!loading && !error && users.length === 0 ? <p className="text-muted mb-0">No users found.</p> : null}

        {!loading && !error && users.length > 0 ? (
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <thead>
                <tr>
                  <th scope="col">User</th>
                  <th scope="col">Samples</th>
                  <th scope="col">Average (ms)</th>
                  <th scope="col">Average (sec)</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => {
                  const metric = metricsByUserId[user.id] ?? {};
                  const samplesCount = Number(metric.samplesCount ?? 0);
                  const averageDurationMs = Number(metric.averageDurationMs ?? 0);
                  const averageDurationSeconds = metric.averageDurationSeconds ?? 0;

                  return (
                    <tr key={`metric-${user.id ?? user.email}`}>
                      <td>{user.email ?? '-'}</td>
                      <td>{samplesCount}</td>
                      <td>{averageDurationMs}</td>
                      <td>{Number(averageDurationSeconds).toFixed(3)}</td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>
    </div>
  );
}
