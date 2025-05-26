import Link from 'next/link';
import LogoutButton from '../auth/components/LogoutButton';
import { useKeycloak } from "../auth/provider/KeycloakProvider";
import { isAdminFunc } from "../auth/components/Helpers";
import { useMessage } from "../provider/MessageContextProvider";
import buildClient from "../api/build-client";
import { useEffect, useState } from "react";
import { v4 as uuidv4 } from 'uuid';

export default () => {
    const { user } = useKeycloak();
    const { message, clearMessage } = useMessage();
    const [balance, setBalance] = useState(null);
    const { showMessage } = useMessage();

    useEffect(() => {
        if (!user?.name) return;

        const fetchCustomerBalance = async () => {
            try {
                const client = buildClient({ req: {}, currentUser: user });
                const requestId = uuidv4();
                const response = await client.get(`/api/customer/get-or-create-customer/${user.name}/${requestId}`);
                setBalance(response.data.balance); // assuming response contains `balance`
            } catch (error) {
                const errorMsg =
                    error.response?.data?.message ||
                    error.message ||
                    "Unexpected error fetching customer balance.";
                showMessage(errorMsg, 'error');
            }
        };

        fetchCustomerBalance();
    }, [user]);

    const alertClass =
        message?.type === 'error'
            ? 'alert-danger'
            : message?.type === 'success'
                ? 'alert-success'
                : 'alert-info';

    const links = [
        user && { label: 'My Bets', href: `/bets/view/player/${user.name}` },
        user && isAdminFunc(user) && { label: 'Players', href: '/players' },
    ]
        .filter(Boolean)
        .map(({ label, href }) => (
            <li key={href} className="nav-item">
                <Link className="nav-link" href={href}>
                    {label}
                </Link>
            </li>
        ));

    return (
        <>
            <nav className="navbar navbar-light bg-light d-flex align-items-center justify-content-between px-4">
                <Link className="navbar-brand" href="/">
                    SkyComposer 5
                </Link>

                <ul className="nav d-flex align-items-center">{links}</ul>

                {balance !== null && (
                    <span className="me-4 text-muted">
                        Your Current Balance: <strong>{balance.toFixed(2)}</strong>
                    </span>
                )}

                <LogoutButton />
            </nav>

            {message && (
                <div className={`alert ${alertClass} text-center mb-0 rounded-0`} role="alert">
                    {message.text}
                    <button
                        type="button"
                        className="btn-close ms-2"
                        onClick={clearMessage}
                        aria-label="Close"
                    ></button>
                </div>
            )}
        </>
    );
};