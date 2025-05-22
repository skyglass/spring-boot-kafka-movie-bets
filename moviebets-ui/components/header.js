import Link from 'next/link';
import LogoutButton from '../auth/components/LogoutButton';
import { useKeycloak } from "../auth/provider/KeycloakProvider";
import { isAdminFunc } from "../auth/components/Helpers";
import { useMessage } from "../provider/MessageContextProvider";

export default () => {
    const { user } = useKeycloak();
    const { message, clearMessage } = useMessage();

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
                    SkyComposer
                </Link>

                <ul className="nav d-flex align-items-center">{links}</ul>

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