export default function UsersList({ users, loading, error, onRefresh }) {
  return (
    <div className="card shadow-sm h-100">
      <div className="card-body">
        <div className="d-flex justify-content-between align-items-start mb-3">
          <div>
            <h5 className="card-title mb-1">Users</h5>
            <p className="text-muted small mb-0">Visible only for admins!!!!</p>
          </div>
          <button className="btn btn-outline-secondary btn-sm" onClick={onRefresh}>Refresh</button>
        </div>

        {loading ? <p className="mb-0">Loading users...</p> : null}
        {error ? <div className="alert alert-danger py-2">{error}</div> : null}

        {!loading && !error && users.length === 0 ? <p className="text-muted mb-0">No users found.</p> : null}

        {!loading && !error && users.length > 0 ? (
          <div className="table-responsive">
            <table className="table table-sm align-middle mb-0">
              <thead>
                <tr>
                  <th scope="col">ID</th>
                  <th scope="col">Name</th>
                  <th scope="col">Email</th>
                  <th scope="col">Birth date</th>
                </tr>
              </thead>
              <tbody>
                {users.map((user) => (
                  <tr key={user.id ?? user.email}>
                    <td>{user.id ?? '-'}</td>
                    <td>{`${user.name ?? ''} ${user.surname ?? ''}`.trim() || '-'}</td>
                    <td>{user.email ?? '-'}</td>
                    <td>{user.birthDate ?? '-'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        ) : null}
      </div>
    </div>
  );
}
